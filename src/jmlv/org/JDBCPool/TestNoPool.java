package jmlv.org.JDBCPool;

import java.io.IOException;
import java.sql.SQLException;
import jmlv.org.jdbconnection.JDBConnection;

public class TestNoPool implements Runnable {
	private Thread t;
	private String threadName;
	private Integer priority;
	private static Integer max = 80;

	TestNoPool(String name, Integer Priority) {
		threadName = name;
		priority = Priority;
		System.out.println("Creating " + threadName);
	}

	public void run() {
		long startTime = System.currentTimeMillis();
		JDBConnection w = new JDBConnection();
		Timer time = new Timer();
		try {
			w.setConnection("localhost", "5432", "framework", "postgres", "masterkey");
			w.executeQuery("SELECT * FROM usuarios");
			long endTime = System.currentTimeMillis();
			Long x = (long) endTime - startTime;
			time.setTime(x);
		} catch (Exception e) {
			System.out.println("-------ERROR!! Total Time " + time.getTime() + "ms");
		}
		System.out.println("-------Total Time " + priority + " " + time.getTime() + "ms");
	}

	public void start() {
		System.out.println("Starting " + threadName);
		if (t == null) {
			t = new Thread(this, threadName);
			t.start();
		}
	}

	public static void main(String args[]) throws SQLException, IOException {

		for (int i = 0; i < max; i++) {
			TestNoPool R1 = new TestNoPool("Thread " + i, i);
			R1.start();
		}

	}

}
