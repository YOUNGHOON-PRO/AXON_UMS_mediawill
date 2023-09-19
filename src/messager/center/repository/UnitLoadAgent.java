package messager.center.repository;

import messager.common.*;

public class UnitLoadAgent
    extends Thread
{
    private static MessageMap messageMap;

    static {
        messageMap = MessageMap.getInstance();
    }

    public UnitLoadAgent(ThreadGroup agentGroup, String threadName) {
        super(agentGroup, threadName);
    }

    /**
     * Message
     */
    public void run() {
        MessageHandler msgHandler = null;
        while (true) {
            msgHandler = messageMap.nextHandler();
            if (msgHandler != null) {
                loadUnit(msgHandler);
            }
            else {
                //작업할 Message가 존재하지 않을 경우 Thread를 종료한다.
                break;
            }
        }
    }

    /**
     * 유닛을 큐에 적재한다.
     * @param msgHandler 메시지 핸들러 객체
     */
    private void loadUnit(MessageHandler msgHandler) {
        UnitInfo unit = msgHandler.nextUnit();
        if (unit != null) {
            UnitQueue.push(unit);
        }
    }
}
