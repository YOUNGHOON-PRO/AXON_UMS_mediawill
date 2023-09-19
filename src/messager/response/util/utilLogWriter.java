package messager.response.util;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
//에러 로그 파일의 포맷을 생성하는 클래스
import java.util.Hashtable;

import messager.response.Config_File_Receiver;

/**
 * 에러 로그 관리 클래스
 * @version 1.0
 * @author ymkim
 */
public class utilLogWriter
{
	
	public utilLogWriter() {
		// TODO Auto-generated constructor stub
		Config_File_Receiver config_File_Receiver;
		config_File_Receiver = Config_File_Receiver.getInstance();
	
	}
	
	
	  /**로그 파일 확장자*/
	  public static String LOG_EXT = ".log";
	  /**백업파일명*/
	  public static String BACK_EXT = ".bak";
	  /**에러로그가 쌓이는 폴더*/
	  public static String ERROR_LOG_FOLDER = Config_File_Receiver.RESPONSE_LOG_PATH;
	  //public static String ERROR_LOG_FOLDER = ".\\response\\";
	  

	  
	/**
	 * 로그 파일을 만들어준다.
	 * @version 1.0
	 * @author ymkim
	 * @param errorClassName 에러가 발생한 클래스명
	 * @param errorType 에러종류
	 * @param comment 에러메시지
	 * @param etcInfo 기타에러정보
	 */
	public static void setLogFormat(String errorClassName, String errorType, String comment,String etcInfo)
	{
		String errorTime = LogDateWriter.getLogTime(); //에러가 발생한 시점

		String demimiter = "``";
		StringBuffer sb = new StringBuffer();
		sb.append(errorTime).append(demimiter).append(errorClassName)
				.append(demimiter).append(errorType).append(demimiter)
				.append(comment).append(demimiter).append(etcInfo);

		//생성된 에러로그를 넣어준다.
		setInfoInsert(sb.toString());
		sb = null;
	}
	
	/**
	   * 에러로그 처리를 파일에 저장한다.
	   * @version 1.0
	   * @author ymkim
	   * @param errorInfo 에러 정보 내용
	   * @return boolean true - 파일 생성 성공, false - 파일 생성 실패
	   */
	  public static boolean setInfoInsert(String errorInfo) {
	    String errorLogName = LogDateWriter.getLogFolderName() + LOG_EXT;
	    String errorLogFullPath = ERROR_LOG_FOLDER + errorLogName;
	    PrintWriter pw = null;

	    boolean return_value = false;

	    try {
	      pw = new PrintWriter(new FileWriter(errorLogFullPath, true));
	      pw.println(errorInfo);
	      pw.flush();

	      return_value = true;
	    }
	    catch (Exception e) {
	      e.printStackTrace();
	      return_value = false;
	    }
	    finally {
	      if (pw != null) {
	        pw.close();
	      }
	    }
	    return return_value;
	  }
	
}