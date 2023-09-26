package messager.center.repository;

import java.util.*;

import messager.center.config.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 더이상 재발송이 필요없거나 발송 중지된 Message의 중간 작업 파일이나 디렉토리를 삭제하는
 * 정리작업을 실행한다.
 * @author Administrator
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MessageCleaner
    extends Thread
{
	
	private static final Logger LOGGER = LogManager.getLogger(MessageCleaner.class.getName());
	
    private static MessageCleaner instance;

    /**
     * 인스턴스를 생성하고 Thread를 실행한다.
     */
    public static void executeThread() {
        synchronized (MessageCleaner.class) {
            if (instance == null) {
                instance = new MessageCleaner();
            }

            if (!instance.isAlive()) {
                instance.start();
            }
        }
    }

    /**
     * Message 정리 작업을 실행한 주기
     */
    private long period;
    /**
     * 등록된 Message의 리스트를 얻기위해
     */
    private MessageMap messageMap;

    /**
     * Thread에 대한 인스턴스를 생성한다.
     */
    private MessageCleaner() {
        //default period = 2시간
        period = ConfigLoader.getInt("repository.clean.period", (60 * 60 * 2)) * 1000;
        messageMap = MessageMap.getInstance();
    }

    /**
     * Thread의 작업을 실행한다.
     */
    public void run() {
        while (true) {
            cleanMessage();
            try {
                sleep(period);
            }
            catch (Exception ex) {LOGGER.error(ex);}
        }
    }

    /**
     * MessageMap에 등록된 MessageHandler 리스트를 가져와서 정리 작업을 실행한다.
     */
    private void cleanMessage() {
        try {
            ArrayList handlerList = messageMap.handlerList();

            for (int i = 0; i < handlerList.size(); i++) {
                MessageHandler msgHandler = (MessageHandler) handlerList.get(i);
                msgHandler.cleanMessage();
            }
        }
        catch (Exception ex) {
        	LOGGER.error(ex);
        }
    }
}
