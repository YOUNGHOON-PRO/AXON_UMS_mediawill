package messager.mailsender.util;

import java.io.*;
import java.text.*;
import java.util.*;

import messager.mailsender.config.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LogWriter
{
	
	private static final Logger LOGGER = LogManager.getLogger(LogWriter.class.getName());
	
    public static void writeError(String className, String methodName, String error, String advice) {
        StringBuffer sb = new StringBuffer();
        String file_path = sb.append(ConfigLoader.TRANSFER_ROOT_DIR)
            .append("log/").append(getFileName())
            .append("_ERR.log").toString();
        sb = null;

        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new FileWriter(file_path, true));

            sb = new StringBuffer();
            sb.append("[").append(getTime()).append("]").append("\r\n");
            sb.append("\t CLASS NAME : ").append(className).append("\r\n");
            sb.append("\t FUNCTION : ").append(methodName).append("\r\n");
            sb.append("\t ERROR : ").append(error).append("\r\n");
            sb.append("\t ADVICE : ").append(advice);

            //System.out.println(sb.toString());
            LOGGER.info(sb.toString());
            
            pw.println(sb.toString());

            pw.flush();
        }
        catch (Exception e) {
        	LOGGER.error(e);
            //e.printStackTrace();
        }
        finally {
            try {
                if (pw != null) {
                    pw.close();
                }
            }
            catch (Exception e) {LOGGER.error(e);}
        }
    }

    public static String getFileName() {
        try {
            SimpleDateFormat simple = new SimpleDateFormat("yyyy_MM_dd");
            return simple.format(new Date());
        }
        catch (Exception e) {
        	LOGGER.error(e);
            return "Date Error";
        }
    }

    public static String getTime() {
        try {
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy년 MM월 dd일 a HH시 mm분 ss초");
            return fmt.format(new Date());
        }
        catch (Exception e) {
        	LOGGER.error(e);
            return "Date Error";
        }
    }

    public static void writeException(String className, String methodName, String advice, Exception e) {
        StringBuffer sb = new StringBuffer();
        String file_path = sb.append(ConfigLoader.TRANSFER_ROOT_DIR)
            .append("log/").append(getFileName())
            .append("_ERR.log").toString();
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new FileWriter(file_path, true));
            sb = new StringBuffer();

            sb.append("[").append(getTime()).append("]").append("\r\n");
            sb.append("\t CLASS NAME : ").append(className).append("\r\n");
            sb.append("\t FUNCTION : ").append(methodName).append("\r\n");
            sb.append("\t EXCEPTION : ").append(e.getLocalizedMessage()).append("\r\n");
            sb.append("\t ADVICE : ").append(advice);

            //System.out.println(sb.toString());
            LOGGER.info(sb.toString());
            
            pw.println(sb.toString());

            pw.flush();
        }
        catch (Exception ex) {
        	LOGGER.error(e);
            //ex.printStackTrace();
        }
        finally {
            try {
                if (pw != null) {
                    pw.close();
                }
            }
            catch (Exception exw) {LOGGER.error(exw);}
        }
    }
}
