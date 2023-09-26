package messager.generator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mysql.jdbc.log.Log4JLogger;

import messager.center.DemonCheck_Center;
import messager.center.repository.ProgressInsert;
import messager.generator.config.ConfigLoader;
import messager.generator.request.*;
import messager.generator.send.SenderListener;
import messager.generator.send.UnitLogSender;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Main Class
 */
public class Main {
	private static final Logger LOGGER = LogManager.getLogger(Main.class.getName());
	/**
	 * main
	 */
	public static void main(String[] args) {

		//환경 설정 파일 load
		ConfigLoader.load();
		
		//발송 결과를 center로 unitlog 파일 전달 및 unitlog 파일 삭제
		//./repository/transfer/unitlog 폴더에 파일이 있다면 center에 소켓통신으로 unitlog 파일을 전송하여 리턴값을 정상으로 수신시 unitlog 파일을 삭제   
		UnitLogSender.executeThread();
		
		
		//MailSender의 접속을 처리하는 SendListener 실행
		SenderListener.execute();
		
		
		//MessageCenter에서 Unit을 요청하기 위해 Requester 실행
		Requester.execute();
		
		
		new DemonCheck_Generator("Generator").start();
	}

        public static void shutdown(){
          //System.out.println("Generator shutdown.");
          System.exit(0);
          LOGGER.info("Generator shutdown.");
          
        }

}

