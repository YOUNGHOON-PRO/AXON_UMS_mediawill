/**
 * 클래스명: Config_File_Receiver.java
 * 버전정보: JDK 1.4.1
 * 요약설명: 설정 파일
 * 작성일자: 2003-04-04 하광범
 */


//2003.10.1 영맨고침
//싱글톤 모델로 바꿈
package messager.response;

import java.io.*;
import java.util.*;

import messager.response.LogWriter;

public class Config_File_Receiver
{
	private static  Config_File_Receiver instance;

	LogWriter logWriter;

	public  static String RESPONSE_CONFIRM_FULL_PATH;
	public  static String RESPONSE_LOG_PATH;
	public  static int RESPONSE_INSERT_PERIOD;
	
	StringTokenizer st;

	//객체를 얻는다.
	public static Config_File_Receiver getInstance()
	{
		if( instance == null )//최초 실행시
		{
			instance = new Config_File_Receiver();
			return instance;

		}
		else	//두번째 이후...
		{
			return instance;
		}
	}

        /**
         *  load configuration.
         */
        public Config_File_Receiver()
	{
		logWriter = new LogWriter();

                //FileInputStream for configuration.
                FileInputStream config = null;
                FileInputStream dbconf = null;
                FileInputStream resconf = null;

                Properties props = new Properties();

		try
		{
			
			resconf = new FileInputStream("./conf/Response.conf");
			props.load(resconf);
			
			RESPONSE_INSERT_PERIOD = Integer.parseInt(props.getProperty("RESPONSE_INSERT_PERIOD"));
			RESPONSE_CONFIRM_FULL_PATH = props.getProperty("RESPONSE_CONFIRM_FULL_PATH");
			RESPONSE_LOG_PATH = props.getProperty("RESPONSE_LOG_PATH");
		}
		catch(Exception e)
		{
			logWriter.logWrite("CONFIG_FILE_RECEIVER ERROR","construct",e);
		}finally{
                  try{
                    if (config != null)
                      config.close();
                    if (dbconf != null)
                      dbconf.close();
                  }catch(Exception e){}
                }
	}
}
