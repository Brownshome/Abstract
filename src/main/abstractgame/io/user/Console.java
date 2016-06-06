package abstractgame.io.user;

import java.text.SimpleDateFormat;
import java.util.Date;

import abstractgame.Client;
import abstractgame.Common;
import abstractgame.util.ApplicationException;

public class Console {
	private static int level = 0;
	private static String format = "HH:mm:ss";
	
	public static void setLevel(int level) {
		Console.level = level;
	}
	
	public static void setFormat(String format) {
		Console.format = format;
	}
	
	public synchronized static void error(Throwable throwable) {
		error(Thread.currentThread(), throwable);
	}
	
	/** Don't use this, throw ApplicationException instead */
	public synchronized static void error(Thread thread, Throwable throwable) {
		if(level <= 3)
			if(throwable instanceof ApplicationException) {
				ApplicationException ae = (ApplicationException) throwable;
				System.out.println(getFormatString("ERROR", thread.getName().toUpperCase() + "-" + ae.section) + ae.getMessage());
			} else {
				System.out.println(getFormatString("ERROR", thread.getName().toUpperCase()) + "An unexpected error has occured");
			}
		
		crash(throwable);
	}

	public synchronized static void warn(String str, String name) {
		if(level > 2) return;
		System.out.println(getFormatString("WARNING", name) + str);
	}

	public synchronized static void inform(String str, String name) {
		if(level > 1) return;
		System.out.println(getFormatString("INFO", name) + str);
	}

	public synchronized static void fine(String str, String name) {
		if(level > 0) return;
		System.out.println(getFormatString("FINE", name) + str);
	}

	static String getFormatString(String level, String name) {
		return "[" + new SimpleDateFormat(format).format(new Date()) + "][" + (Common.isServerSide() ? "SERVER" : "CLIENT") + "][" + name + "][" + level + "]";
	}

	/**Please use Console.error(Throwable t, String name) instead */
	static void report(Throwable ex) {
		System.out.println("***************** STACK TRACE *****************");
		ex.printStackTrace(System.out);
		System.out.println("****************** TRACE END ******************");
	}
	
	static void crash(Throwable ex) {
		if(level < 4) 
			report(ex);
		
		Client.close();
	}
}
