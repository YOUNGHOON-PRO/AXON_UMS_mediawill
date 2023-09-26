package messager.generator.send;

import java.util.*;

import messager.generator.config.*;
import messager.generator.repository.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 생성완료된 Unit의 UnitName(MessageID^UnitID)를 담는 Queue MailSender의 요청이 있을 때 꺼내어서
 * 보내준다. MailSender는 UnitName을 UnitEnvelope 객체파일과 eml(컨텐츠) 디렉토리, 발송 결과 파일을 지정된
 * 경로에서 읽어서 작업한다.
 */
public class SendQueue
{
	private static final Logger LOGGER = LogManager.getLogger(SendQueue.class.getName());
	
	//Unit의 UnitName(MessageID^UnitID)를 저장할 list
    private static ArrayList list;

    //접근하는 쓰레드동기화를 위해서 필요
    private static Object lock;

    //저장할수 있는 최대 Unit의 수 (디폴트로 10을 사용)
    private static int queueSize;

    //저장된 Unit이 최대 Unit의 수에 도달 했을경우 push하는 쓰레드의 대기시간
    private static long waitTime;
    static {
        String str = ConfigLoader.getProperty("send.queue.size");
        try {
            queueSize = Integer.parseInt(str);
        }
        catch (Exception ex) {
        	LOGGER.error(ex);
            queueSize = 10;
        }
        str = ConfigLoader.getProperty("send.queue.wait.time");
        try {
            waitTime = Long.parseLong(str);
        }
        catch (Exception ex) {
        	LOGGER.error(ex);
            waitTime = 0;
        }
        list = new ArrayList(queueSize);
        lock = new Object();
        //생성이 완료된 Unit의 Recovery
        recovery();
    }

    /**
     * 생성완료된 Unit의 Recovery 를 실행한다. 발송 진행중인가 체크해서 발송진행중이 아니면 SendQueue에 넣는다.
     */
    private static void recovery() {
        //생성완료된 Unit 리스트 로드(UnitEnvelope객체 파일리스트 로드)
        ArrayList list = SendUnitFile.loadList();

        while (list.size() > 0) {
            String unitName = (String) list.remove(0);

            //MailSender가 발송중인가 확인
            if (!SendSyncUnitFile.exists(unitName)) {
                //MailSender가 접근 하지 않았으면 SendQueue에 push
                push(unitName, true);
            }
        }
    }

    /**
     * Unit를 Queue에 넣는다.
     *
     * @param unitName
     *            MessageID^UnitID
     */
    public static void push(String unitName) {

        //성공할때 까지 loop를 돈다.
        //Queue에 넣는 작업이 성공하면 false가 되고 loop에서 벗어난다.
        boolean bLoop = true;

        do {
            synchronized (lock) {

                //저장 할 수 있는가 확인한다.
                if (list.size() < queueSize) {
                    list.add(unitName);
                    bLoop = false;
                }
                else {

                    // 저장 할 수 없으면 깨울 때까지 wait한다.
                    try {
                        if (waitTime > 0) {
                            lock.wait(waitTime);
                        }
                        else {
                            lock.wait();
                        }
                    }
                    catch (InterruptedException ex) {
                    	LOGGER.error(ex);
                    }
                }
            }
        }
        while (bLoop);
    }

    /**
     * Queue의 size를 검사하지 않고 Unit의 UnitName(MessageID^UnitID)를 넣는다.
     *
     * @param untName
     *            MessageID^UnitID
     * @param flag
     *            true이면 Queue의 size를 검사하지 않고 false이면 Queue의 size를 검사한다.
     */
    public static void push(String unitName, boolean flag) {
        if (flag) {
            list.add(unitName);
        }
        else {
            push(unitName);
        }
    }

    /**
     * Queue의 가장 앞의 Unit의 UnitName(MessageID^UnitID)를 꺼내서 리턴한다.
     *
     * @return 가장 먼저 push되었던 Unit의 UnitName(MessageID^UnitID)
     */
    public static String pop() {
        String unitName = null;
        synchronized (lock) {
            if (list.size() > 0) {
                unitName = (String) list.remove(0);
            }
            lock.notifyAll();
        }
        return unitName;
    }

    /**
     * 현재 Queue에 저장된 Unit의 수가 최대값에 도달 했는지 확인한다.
     *
     * @return 저장된 Unit의 수가 최대값보다 작으면 true
     */
    public static boolean isFulled() {
        if (list.size() >= queueSize) {
            return true;
        }
        return false;
    }
}
