package jmlv.org.JDBCPool;

import java.io.IOException;
import java.sql.*;
import java.util.*;


public class JDBCPool implements Runnable {
 private String driver, url, username, password;
 private int maxCnx;
 private boolean waitIfBusy;
 private Vector availableCnx, busyCnx;
 private boolean connectionPending = false;
 private Connection connection;
 private Statement statement;
 private PreparedStatement preparedStatement;
 private ResultSet resultSet;
 private String[] columns;
 private String[][] rows;
 private DB dbc = new DB();

 public JDBCPool(String id,
  boolean Wait)

 throws SQLException, IOException {
  String con[] = dbc.getConn(id);
  this.driver = con[0];
  this.url = con[1];
  this.username = con[2];
  this.password = con[3];
  this.maxCnx = Integer.parseInt(con[5]);
  int initCnx = Integer.parseInt(con[4]);
  this.waitIfBusy = Wait;
  System.out.println(waitIfBusy);
  if (initCnx > maxCnx) {
   initCnx = maxCnx;
  }
  availableCnx = new Vector(initCnx);
  busyCnx = new Vector();
  for (int i = 0; i < initCnx; i++) {
   availableCnx.addElement(makeNewConnection());
  }
 }

 public synchronized Connection getConnection()
 throws SQLException {
  if (!availableCnx.isEmpty()) {
   Connection existingConnection =
    (Connection) availableCnx.lastElement();
   int lastIndex = availableCnx.size() - 1;
   availableCnx.removeElementAt(lastIndex);

   if (existingConnection.isClosed()) {
    notifyAll(); 
    return (getConnection());
   } else {
    busyCnx.addElement(existingConnection);
    return (existingConnection);
   }
  } else {


   if ((totalConnections() < maxCnx) &&
    !connectionPending) {   
    makeBackgroundConnection();
   } else if (!waitIfBusy) {
    throw new SQLException("Connection limit reached");
   }

   try {
    wait();
   } catch (InterruptedException ie) {}
   return (getConnection());
  }
 }


 private void makeBackgroundConnection() {
  connectionPending = true;
  try {
   Thread connectThread = new Thread(this);
   connectThread.start();
  } catch (OutOfMemoryError oome) {
  }
 }

 public void run() {
  try {
   Connection connection = makeNewConnection();
   synchronized(this) {
    availableCnx.addElement(connection);
    connectionPending = false;
    notifyAll();
   }
  } catch (Exception e) {
  }
 }

 private Connection makeNewConnection()
 throws SQLException {
  try {
   Class.forName(driver);
   Connection connection =
    DriverManager.getConnection(url, username, password);
   return (connection);
   
  } catch (ClassNotFoundException cnfe) {
   throw new SQLException("Can't find class for driver: " +
    driver);
  }
 }

 public synchronized void free(Connection connection) {
  busyCnx.removeElement(connection);
  availableCnx.addElement(connection);
  notifyAll();
 }

 public synchronized int totalConnections() {

  return (availableCnx.size() +
   busyCnx.size());
 }


 public synchronized void closeAllConnections() {
  closeConnections(availableCnx);
  availableCnx = new Vector();
  closeConnections(busyCnx);
  busyCnx = new Vector();
 }

 private void closeConnections(Vector connections) {
  try {
   for (int i = 0; i < connections.size(); i++) {
    Connection connection =
     (Connection) connections.elementAt(i);
    if (!connection.isClosed()) {
     connection.close();
    }
   }
  } catch (SQLException sqle) {
  }
 }

 public synchronized String toString() {
  String info =
   "ConnectionPool(" + url + "," + username + ")" +
   ", available=" + availableCnx.size() +
   ", busy=" + busyCnx.size() +
   ", max=" + maxCnx;
  return (info);
 }


 public JDBCPool executeQuery(Connection connection, String query) {

  try {
   query = dbc.getQuery(query).toUpperCase();
   if (String.valueOf(query.charAt(0)).equals("S")) {
    this.statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
     ResultSet.CONCUR_READ_ONLY);
    this.resultSet = this.statement.executeQuery(query);
   }

  } catch (Exception e) {
   e.printStackTrace();
  }
  return this;
 }

 public JDBCPool executeQueryX(Connection connection, String query, Object...params) {
  try {
   query = dbc.getQuery(query).toUpperCase();
   this.preparedStatement = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_SENSITIVE,
    ResultSet.CONCUR_READ_ONLY);
   for (int i = 0; i < params.length; i++) {
    this.preparedStatement.setObject((i + 1), params[i]);
   }
   this.resultSet = this.preparedStatement.executeQuery();

  } catch (Exception e) {
   e.printStackTrace();
  }
  return this;
 }

 public JDBCPool execute(Connection connection, String query, Object...params) {
  try {
   query = dbc.getQuery(query).toUpperCase();
   if (String.valueOf(query.charAt(0)) != "S") {
    this.preparedStatement = connection.prepareStatement(query);
    for (int i = 0; i < params.length; i++) {
     this.preparedStatement.setObject((i + 1), params[i]);
    }
    this.preparedStatement.execute();
   }

  } catch (Exception e) {
   e.printStackTrace();
  }
  return this;
 }



 public ResultSet getResultSet() {
  return this.resultSet;
 }

 public String[][] getTable() {
  String[][] tabla = this.create(this.resultSet);
  return tabla;
 }


 public String[][] create(ResultSet rs) {
  try {
   ResultSetMetaData rsmd = rs.getMetaData();
   rs.last();
   int rows = rs.getRow();
   this.columns = new String[rsmd.getColumnCount()];
   this.rows = new String[rows + 1][this.columns.length];
   rs.beforeFirst();
   for (int i = 0; i < this.columns.length; i++) {
    this.columns[i] = rsmd.getColumnName(i + 1);
   }

   for (int i = -1; rs.next(); i++) {
    for (int j = 0; j < this.columns.length; j++) {
     if (i == (-1)) {
      this.rows[i + 1][j] = this.columns[j];
      this.rows[i + 2][j] = rs.getObject(j + 1).toString();
     } else {
      this.rows[i + 2][j] = rs.getObject(j + 1).toString();
     }
    }
   }
  } catch (Exception e) {
   e.printStackTrace();
  }
  return this.rows;
 }

 public void close() {
  try {
   if (this.connection != null) this.connection.close();
   if (this.statement != null) this.statement.close();
   if (this.preparedStatement != null) this.preparedStatement.close();
   if (this.resultSet != null) this.resultSet.close();
  } catch (SQLException sqlE) {
   sqlE.printStackTrace();
  }
 }
}