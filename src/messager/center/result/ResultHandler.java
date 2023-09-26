package messager.center.result;

import java.util.*;

import messager.center.config.*;
import messager.center.repository.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 발송 결과 디렉토리를 검색하여 Message별로 MessageHandler를 이용하여
 * 발송 실패된 대상자를 재발송 가능 하도록 Unit를 다시 생성하고
 * Message의 발송 완료 일시를 상태 파일에 업데이트 한다.
 * 발송 완료된 Message는 발송 완료 일시를 DB에 업데이트 한다.
 *
 * @author Administrator
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ResultHandler
    extends Thread
{
	private static final Logger LOGGER = LogManager.getLogger(ResultHandler.class.getName());
	
    /** MessageHandler를 얻는다 */
    private MessageMap messageMap;

    /** 작업 주기 */
    private long period;

    /** DB에 발송 완료일시와 상태를 업데이트 */
    private EndTimeManager endTimeManager;

    /**
     * 객체를 생성한다.
     *
     * @throws Exception
     */
    public ResultHandler()
        throws Exception {
        messageMap = MessageMap.getInstance();
        endTimeManager = new EndTimeManager();
        period = ConfigLoader.getInt("result.period", 2 * 60) * 1000;
    }

    /**
     * 쓰레드를 실행한다.
     */
    public void run() {
        while (true) {
            try {
                work();
            }
            catch (Exception ex) {
            	LOGGER.error(ex);
            }
            catch (Error err) {LOGGER.error(err);}

            try {
                sleep(period);
            }
            catch (Exception ex) {LOGGER.error(ex);
            }
        }
    }

    private void work() {
        ArrayList list = messageMap.handlerList();
        ArrayList endList = new ArrayList();

        for (int i = 0; i < list.size(); i++) {
            MessageHandler msgHandler = (MessageHandler) list.get(i);

            msgHandler.handleResult();   //unit 폴더 삭제
            if (msgHandler.isSendEnd()) {
                endList.add(msgHandler);
            }
        }

        if (endList.size() > 0) {
            try {
                endTimeManager.open(); 
                for (int i = 0; i < endList.size(); i++) {
                    MessageHandler msgHandler = (MessageHandler) endList.get(i);
                    msgHandler.updateEndTime(endTimeManager); //진행완료로 데이터 업데이트 및 center/하위 파일 삭제 (message , result, sysc_unit)
                }
            }
            catch (Exception ex) {
            	LOGGER.error(ex);
                //ex.printStackTrace();
            }
            finally {
                endTimeManager.close();
            }
        }
    }
}
