package jmlv.org.JDBCPool;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import jmlv.org.jbuilder.JBuilder;


public class TestPool  implements Runnable {
	 private JDBCPool pool;
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub 
		 new TestPool("03"); 
	}

	  public TestPool(String con) throws IOException {
		    try {
		      pool = new JDBCPool(con, true);
		      for(int i=0; i<80; i++) {
		        Thread t = new Thread(this);
		        t.start();
		      }
		    } catch(SQLException sqle) {
		      System.err.println("Error building pool: " + sqle);
		    }
		  }

	public void run() {
		    try {
		    	long startTime = System.currentTimeMillis();  
		    	Timer time = new Timer();
		        Connection connection = pool.getConnection();
//		        System.out.println("pool=" + pool);
				pool.executeQuery(connection,"testQuery");
		        pool.free(connection);
		        long endTime = System.currentTimeMillis();
				Long x = (long) endTime - startTime;
				time.setTime(x);
				System.out.println("-------Total Time "+time.getTime()+"ms");
		    } catch(SQLException sqle) {
		      System.err.println("Error : " + sqle);
		    }
	}
		
	 public void pause(int millis) {
		    try {
		      Thread.sleep((int)(Math.random()*millis));
		    } catch(InterruptedException ie) {}
		  }
}
