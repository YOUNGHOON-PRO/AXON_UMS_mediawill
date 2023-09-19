/*
* 클래스명: LogWriter.java
* 버전정보: JDK 1.3.1
* 요약설명: Exception 처리 및 Exception 로그 파일 기록
* 작성일자: 2003-04-04 하광범
 */

package messager.response;

import java.io.*;
import java.util.*;
import java.text.*;

public class LogWriter
{
	//File logHistoryFile;
	private FileWriter fw = null;
	private StringBuffer sb = null;

	public LogWriter()
	{
	}

	private synchronized String getLogDate()
	{
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy_MM_dd");
		return fmt.format(new java.util.Date());
	}

	private synchronized String getLogTime()
	{
		String tmpDateStr = "";
		String returnVal = "";
		Calendar rightNow = Calendar.getInstance();
		int tmpHour = 0;
		if( rightNow.get(Calendar.AM_PM) == 1 ) {
			tmpHour = 12;
		}

		sb = new StringBuffer();
		tmpDateStr= sb.append(tmpHour).append(rightNow.get(Calendar.HOUR)).append(":")
			.append(rightNow.get(Calendar.MINUTE)).append(":")
			.append(rightNow.get(Calendar.SECOND)).toString();

		sb = new StringBuffer();
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy/MM/dd");
		returnVal = sb.append(fmt.format(new java.util.Date())).append(" ").append(tmpDateStr).toString();
		sb = null;
		return returnVal;
	}

	public synchronized void logWrite(String classFile, String classMethod, Exception errorStr)
	{
		PrintWriter historyWriter = null;
		try
		{
			sb = new StringBuffer();
			System.out.println(sb.append(classFile).append(" - ").append(classMethod)
							   .append(" : ").append(errorStr).toString());

			sb = new StringBuffer();
			//logHistoryFile=new File("NeoQueueInfo"+File.separator+"error"+File.separator+getLogDate()+".err");
			fw = new FileWriter(sb.append("NeoQueueInfo").append(File.separator)
								.append("error").append(File.separator).append(getLogDate())
								.append(".err").toString(), true);
			historyWriter = new PrintWriter(fw);

			sb = new StringBuffer();
			historyWriter.print(sb.append("[ ").append(getLogTime()).append(" ]")
								.append(classFile).append(" - ").append(classMethod)
								.append(" : ").append(errorStr)
								.append(System.getProperty("line.separator"))
								.append(System.getProperty("line.separator")).toString());
		}
		catch(Exception e) {
			System.out.println("LogWriter logWrite() : "+e);
		}
		finally {
			sb = null;
			try {
				if( fw != null ) {
					fw.close();
					fw = null;
				}
			}
			catch(Exception e) {}

			try {
				if( historyWriter != null ) {
					historyWriter.close();
					historyWriter = null;
				}
			}
			catch(Exception e) {}
		}
	}
}
