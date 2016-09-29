package jmlv.org.JDBCPool;

public class Timer {
	private static Long time = (long) 0;
	
	public synchronized void setTime(Long x){
		time +=x;
	}
	public Long getTime(){
		return time;
	}
}
