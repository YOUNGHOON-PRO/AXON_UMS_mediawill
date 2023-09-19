package messager.center.repository;

import messager.center.config.*;

/**
 * 주기적으로 발송 중인 Message의 리스트를 체크하여 발송 중이 Message의 Unit를 로딩하는
 * UnitLoadAgent 쓰래드를 생성 실행한다.
 *
 *
 * @author Administrator
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class UnitAgentManager
    extends Thread
{
    private static final ThreadGroup agentGroup = new ThreadGroup("unit_group");

    private static final Object lock = new Object();

    private static UnitAgentManager agentManager;

    /** 인스턴스를 생성하고 쓰래드를 실행한다 */
    public static void execute() {
        synchronized (lock) {
            if (agentManager == null) {
                agentManager = new UnitAgentManager();
            }
            if (!agentManager.isAlive()) {
                agentManager.start();
            }
        }
    }

    /** UnitLoadAgent 쓰래드의 최대 실행 가능 수 */
    private int maxAgentSize;

    /** 발송 중인 Message가 존재하는지 UnitLoadAgent 쓰래드를 실행 할 수 있는지를 체크 주기 */
    private int checkPeriod;

    /** 발송 중인 Message의 리스트를 가져온다 */
    private MessageMap messageMap;

    /** 객체를 생성한다 */
    private UnitAgentManager() {
        maxAgentSize = ConfigLoader.getInt("unit.load.agent.size", 5);
        checkPeriod = ConfigLoader.getInt("unit.load.agent.check.period", 10) * 1000;
        messageMap = MessageMap.getInstance();
    }

    /** 쓰레드를 실행한다 */
    public void run() {
        while (true) {

            //UnitLoadAgent 쓰래드 실행
            executeAgent();
            try {
                sleep(checkPeriod);
            }
            catch (InterruptedException ex) {}
        }
    }

    /**
     * 발송 메세지의 유무를 체크해서 여유 UnitLoadAgent 쓰래드 수만큼 쓰래드를 생성하여 실행한다
     */
    private void executeAgent() {
        try {
            if (messageMap.isEmpty()) {
                return;
            }

            int curCount = agentGroup.activeCount();
            if (curCount < maxAgentSize) {
                String threadName = "unit_agent";
                int count = maxAgentSize - curCount;

                for (int i = 0; i < count; i++) {
                    UnitLoadAgent unitAgent =
                        new UnitLoadAgent(agentGroup, threadName);
                    unitAgent.start();
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}