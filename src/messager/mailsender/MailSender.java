package messager.mailsender;

import messager.generator.DemonCheck_Generator;
import messager.mailsender.config.*;
import messager.mailsender.connect.*;
import messager.mailsender.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MailSender
{
	
	private static final Logger LOGGER = LogManager.getLogger(MailSender.class.getName());
	
    /**
     * Constructor
     * 1. default.conf File Loading
     * 2. Passage Class 호출
     */
    public MailSender() {
        ConfigLoader.load();
        new ConfigLoader().start(); // 환경 설정 파일 감시자 - 설정 변경시 새로 파일을 로딩한다.
        FileManager.makeDirectory(ConfigLoader.MAILSENDER_ID); // Mail Transfer의 고유 번호 디렉토리를 생성한다.
        new Passage().start();
        new LogFileManager().start();
        new DemonCheck_MailSender("MailSender").start();
    }

    /**
     * main method
     */
    public static void main(String[] args) {
        new MailSender();
    }

    public static void shutdown() {
        //System.out.println("MailSender shutdown. : ID is " + ConfigLoader.MAILSENDER_ID);
    	LOGGER.info("MailSender shutdown. : ID is " + ConfigLoader.MAILSENDER_ID);
        System.exit(0);
    }

}
