package com.pdf.convert;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
//import java.util.Hashtable;
//import com.iwn.imj.lib.util.DateForm;

public class Logger
{
	
	private static SimpleDateFormat SDF = new SimpleDateFormat("yyyyMMddHHmmss");
	public static final char PATH_GUBUN = File.separatorChar;
	
	private static boolean enable = false;
	private static String logDir = "";
	private static String logName = "";
	

	public void setEnable(boolean enable) {
		Logger.enable = enable;
	}

	public void setLogDir(String logDir) {
		Logger.logDir = logDir;
	}

	public void setLogName(String logName) {
		Logger.logName = logName;
	}

	public synchronized void error(String methodName, String message)
	{
		File file = null;
		String logFile;
		Date curDate = new Date();
		String curTime = getDate(curDate.getTime());
		String logTime = getLogTime(curDate.getTime());
		//String curTime = new String(DateForm.convert(curDate.getTime(), "%Y%M%D"));
		//String logTime = new String(DateForm.convert(curDate.getTime(), "%Y/%M/%D %h:%m:%s"));
		String log = null;
		long threadId = 0;
		
		if (!enable) return;
		
		if (logDir == null) logDir = ".";
		file = new File(logDir);
		if (!file.exists()) {
			if(!file.mkdirs()){
				System.out.println("[chHtmlToPdf] Log Folder create Failed("+logDir+")");
			}
		}
		if (logName == null || logName.trim().length() == 0) logName = "imj";
		if (message == null) message = "";
		
		try {
			threadId = Thread.currentThread().getId();
		} catch (Exception e) {
			threadId = 0;
		}
		logFile = String.format("%s%s%s.%s.log", logDir, PATH_GUBUN, logName, curTime);
		log = String.format("[%s][ERROR][%10d][%s][%s]",  logTime, threadId,  methodName, message);
		
		PrintStream stream = null;
		try {
			stream = new PrintStream(new FileOutputStream(logFile, true), true);
			stream.println(log);
			stream.flush();
		} catch (Exception e) {
			System.out.println("Error :"+e.getMessage());
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (Exception e) {}
				stream = null;
			}
		}
		
		log = null;
		logFile = null;
		
	}
	
	public synchronized void error(String methodName, String format, Object... args)
	{
		File file = null;
		String logFile;
		Date curDate = new Date();
		String curTime = getDate(curDate.getTime());
		String logTime = getLogTime(curDate.getTime());
		//String curTime = new String(DateForm.convert(curDate.getTime(), "%Y%M%D"));
		//String logTime = new String(DateForm.convert(curDate.getTime(), "%Y/%M/%D %h:%m:%s"));
		String log = null;
		long threadId = 0;
		String message = null;
		
		if (!enable) return;
		
		if (logDir == null) logDir = ".";
		file = new File(logDir);
		if (!file.exists()) {
			if(!file.mkdirs()){
				System.out.println("[chHtmlToPdf] Log Folder create Failed("+logDir+")");
			}
		}
		if (logName == null || logName.trim().length() == 0) logName = "imj";
		
		message = String.format(format, args);
		if (message == null) message = "";
		
		try {
			threadId = Thread.currentThread().getId();
		} catch (Exception e) {
			threadId = 0;
		}
		logFile = String.format("%s%s%s.%s.log", logDir, PATH_GUBUN, logName, curTime);
		log = String.format("[%s][ERROR][%10d][%s][%s]",  logTime, threadId, methodName, message);
		
		PrintStream stream = null;
		try {
			stream = new PrintStream(new FileOutputStream(logFile, true), true);
			stream.println(log);
			stream.flush();
		} catch (Exception e) {
			System.out.println("Error :"+e.getMessage());
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (Exception e) {}
				stream = null;
			}
		}
		
		log = null;
		logFile = null;
		
	}
	
	public synchronized void info(String methodName, String message)
	{
		File file = null;
		String logFile;
		Date curDate = new Date();
		String curTime = getDate(curDate.getTime());
		String logTime = getLogTime(curDate.getTime());
		//String curTime = new String(DateForm.convert(curDate.getTime(), "%Y%M%D"));
		//String logTime = new String(DateForm.convert(curDate.getTime(), "%Y/%M/%D %h:%m:%s"));
		String log = null;
		long threadId = 0;
		
		if (!enable) return;
		
		if (logDir == null) logDir = ".";
		file = new File(logDir);
		if (!file.exists()) {
			if(!file.mkdirs()){
				System.out.println("[chHtmlToPdf] Log Folder create Failed("+logDir+")");
			}
		}
		if (logName == null || logName.trim().length() == 0) logName = "imj";
		if (message == null) message = "";
		
		try {
			threadId = Thread.currentThread().getId();
		} catch (Exception e) {
			threadId = 0;
		}
		logFile = String.format("%s%s%s.%s.log", logDir, PATH_GUBUN, logName, curTime);
		log = String.format("[%s][INFO ][%10d][%s][%s]",  logTime, threadId,  methodName, message);
		
		PrintStream stream = null;
		try {
			stream = new PrintStream(new FileOutputStream(logFile, true), true);
			stream.println(log);
			stream.flush();
		} catch (Exception e) {
			System.out.println("Error :"+e.getMessage());
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (Exception e) {}
				stream = null;
			}
		}
		
		log = null;
		logFile = null;
		
	}
	
	public synchronized void info(String methodName, String format, Object... args)
	{
		File file = null;
		String logFile;
		Date curDate = new Date();
		String curTime = getDate(curDate.getTime());
		String logTime = getLogTime(curDate.getTime());
		//String curTime = new String(DateForm.convert(curDate.getTime(), "%Y%M%D"));
		//String logTime = new String(DateForm.convert(curDate.getTime(), "%Y/%M/%D %h:%m:%s"));
		String log = null;
		long threadId = 0;
		String message = null;
		
		if (!enable) return;
		
		if (logDir == null) logDir = ".";
		file = new File(logDir);
		if (!file.exists()) {
			if(!file.mkdirs()){
				System.out.println("[chHtmlToPdf] Log Folder create Failed("+logDir+")");
			}
		}
		if (logName == null || logName.trim().length() == 0) logName = "imj";
		
		message = String.format(format, args);
		if (message == null) message = "";
		
		try {
			threadId = Thread.currentThread().getId();
		} catch (Exception e) {
			threadId = 0;
		}
		logFile = String.format("%s%s%s.%s.log", logDir, PATH_GUBUN, logName, curTime);
		log = String.format("[%s][INFO ][%10d][%s][%s]",  logTime, threadId, methodName, message);
		
		PrintStream stream = null;
		try {
			stream = new PrintStream(new FileOutputStream(logFile, true), true);
			stream.println(log);
			stream.flush();
		} catch (Exception e) {
			System.out.println("Error :"+e.getMessage());
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (Exception e) {}
				stream = null;
			}
		}
		
		log = null;
		logFile = null;
		
	}
	
	public void info(String methodName, Exception e)
	{
		info(methodName, e.getMessage());
	}
	
	private static String getDate(long time)
	{
		String dateStr = null;
		String newDate = null;	
		
		dateStr = SDF.format(time);
		newDate = String.format("%s", dateStr.substring(0, 8));
	
		dateStr = null;	
		
		return newDate;
	}
	
	private static String getLogTime(long time)
	{
		String dateStr = null;
		String newDate = null;	
		
		dateStr = SDF.format(time);
		newDate = String.format("%s/%s/%s %s:%s:%s", dateStr.substring(0, 4), dateStr.substring(4, 6), dateStr.substring(6, 8),
				dateStr.substring(8, 10), dateStr.substring(10, 12), dateStr.substring(12, 14));
		
		dateStr = null;		
		return newDate;
	}

	public static void main(String[] args)
	{
		getDate(System.currentTimeMillis());
		getLogTime(System.currentTimeMillis());
	}
}
