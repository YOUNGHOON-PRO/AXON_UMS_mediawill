package messager.center.repository;

import java.util.*;

import messager.center.config.*;
import messager.center.result.*;
import messager.common.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Message의 발송정보와 상태를 관리 하는  MessageStatus객체,
 * Unit를 관리하는 UnitFileManager  발송 결과Unit의를 관리하는 ResultFileManager
 * UnitQueue에 저장된 Unit를 관리하는 UnitSyncManager 객체를 통합적으로 관리한다.
 * Unit의 Load, Message의 상태정보, 발송결과를 처리는 MessageHandler의 인스턴스로
 * Message별로 관리된다.
 * @author Administrator
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MessageHandler
{
	private static final Logger LOGGER = LogManager.getLogger(MessageHandler.class.getName());
	
    /** MessageHandler 가 등록되어진다. */
    static MessageMap messageMap;
    /** 하나의 Unit에 포함될수 있는 최대의 대상자 정보 수 */
    static int maxToSize;

    static {
        messageMap = MessageMap.getInstance();
        maxToSize = ConfigLoader.getInt("unit.to.size", 500);
    }

    /** Message ID */
    private String messageID;

    /** Unit이 저장된 디렉토리를 관리하는 객체로 Unit의 로드, 삭제를 처리한다. */
    private UnitFileManager unitFileMgr;

    /**
     * Generator로 전달 되지 않은 Unit을 일시적으로 저장하는 큐에 Unit이 존재할 경우
     * Unit의 재작업을 위해 Unit 파일을 생성하여 관리한다.
     * 발송이 시작 상태로 될때 리스트를 읽어온다.
     */
    private UnitSyncManager unitSyncMgr;

    /** 발송 결과 파일들을 관리한다. */
    private ResultFileManager resultMgr;

    /** Message이 상태 정보, 발송 정보 (Message 객체와 Contents 객체)를 파일에 저장하거나 읽어온다. */
    private MessageStatus messageStatus;

    /** Message 객체 */
    private Message message;

    /** UnitSyncManager객체로 읽어온 Unit의 파일 리스트 */
    private ArrayList unitList;

    /** 쓰레드의 동기화를 관리한다 */
    private Object lock;

    /**
     * 이미 생성된 MessageStatus 객체로 MessageHandler 객체를 생성한다.
     * Message객체는 MessageStatus 객체를 이용하여 파일에서 읽어온다.
     *
     * @param messageID 메시지 아이디
     * @param msgStatus 메시지 상태 객체
     * @throws Exception
     */
    public MessageHandler(String messageID, MessageStatus msgStatus)
        throws Exception {
        this.messageID = messageID;
        this.messageStatus = msgStatus;
        lock = new Object();
        unitFileMgr = new UnitFileManager(messageID);
        unitSyncMgr = new UnitSyncManager(messageID);
        resultMgr = new ResultFileManager(messageID);
        message = messageStatus.readMessage();
    }

    /**
     * messageID로  MessageHandler 객체를 생성한다.
     * MessageStatus 객체는 messageID를 이용하여 생성하고 파일에서 읽어온다.
     *
     * @param messageID 메시지 아이디
     * @throws Exception
     */
    public MessageHandler(String messageID)
        throws Exception {
        this.messageID = messageID;
        lock = new Object();
        messageStatus = new MessageStatus(messageID);
        unitFileMgr = new UnitFileManager(messageID);
        unitSyncMgr = new UnitSyncManager(messageID);
        unitList = unitSyncMgr.unitList();
        resultMgr = new ResultFileManager(messageID);
        message = messageStatus.readMessage();
        messageStatus.readFile();
    }

    /**
     * Message ID를 얻는다.
     *
     * @return messageID
     */
    public String getMessageID() {
        return messageID;
    }

    /**
     * 업무 번호를 얻는다.
     *
     * @return 업무 번호
     */
    public int getTaskNo() {
        return message.taskNo;
    }

    /**
     * 보조 업무 번호를 얻는다.
     *
     * @return 보조 업무 번호
     */
    public int getSubTaskNo() {
        return message.subTaskNo;
    }

    /**
     * Message의 발송 상태를 얻는다.
     * 발송 상태는 RUN, WAIT, PAUSE, STOP 의 상태가 있다.
     *
     * @return 발송 상태
     */
    public int getSendStatus() {
        return messageStatus.getSendStatus();
    }

    /**
     * Message의 Unit의 작업 완료 상태를 나타낸다.
     * 상태값은 LOAD_RUN, LOAD_END 상태가 있다.
     *
     * @return Unit의 작업 완료 상태
     */
    public int getUnitStatus() {
        return messageStatus.getUnitStatus();
    }

    /**
     * Message의 발송 대상자 총수를 얻는다.
     *
     * @return 발송 대상자 총수
     */
    public int getTotalCount() {
        return messageStatus.getTotalCount();
    }

    /**
     * Generator로 전달된 대상자의 수를 얻는다.
     * 발송 완료가 되면 0으로 초기화 되어서
     * 재발송이 이루어질 경우 0부터 증가된다.
     *
     * @return Generator로 전달된 대상자의 수
     */
    public int getDeliveryCount() {
        return messageStatus.getDeliveryCount();
    }

    /**
     * 발송 완료된 대상자의 수를 얻는다.
     * 이 수는 발송이 완료된 Unit에서 가져온다.
     *
     * @return 발송 완료된 대상자 수
     */
    public int getSendCount() {
        return messageStatus.getSendCount();
    }

    /**
     * 발송 성공된 대상자 수를 얻는다.
     * 이 수는 발송이 완료된 Unit에서 가져온다.
     *
     * @return 발송 성공된 대상자 수
     */
    public int getSuccessCount() {
        return messageStatus.getSuccessCount();
    }

    /**
     * Message의 발송을 일시 정지 시킨다.
     * 큐에있는 Unit도 제거한다.
     * Generator 에 전달된 Unit에 대해서는 변경이 되지 않는다.
     *
     * @return 상태 변경이 성공하면 true
     */
    public boolean setPause() {
        boolean isSuccess = true;

        try {
            messageStatus.setSendStatus(MessageStatus.SEND_PAUSE);
        }
        catch (Exception ex) {
        	LOGGER.error(ex);
            isSuccess = false;
        }
        removeQueue();

        return isSuccess;
    }

    /**
     * Message의 발송을 중지 시킨다.
     * 큐에있는 Unit도 제거한다.
     * Generator 에 전달된 Unit에 대해서는 변경이 되지 않는다.
     *
     * @return 상태 변경이 성공하면 true
     */
    public boolean setStop() {
        boolean isSuccess = true;

        synchronized (lock) {
            int lastUnitID = messageStatus.getLastUnitID();
            int sendNo = messageStatus.getSendNo();

            try {
                messageStatus.setSendStatus(MessageStatus.SEND_STOP);
            }
            catch (Exception ex) {
            	LOGGER.error(ex);
                isSuccess = false;
            }

            removeQueue();
            unitSyncMgr.remove(true);
            for (int i = 1; i <= lastUnitID; i++) {
                unitFileMgr.deleteUnit(i, sendNo);
            }
            unitFileMgr.delete(sendNo);
        }

        return isSuccess;
    }

    /**
     * Queue에 있는 Unit를 제거한다.
     */
    private void removeQueue() {
        ArrayList unitList = unitSyncMgr.unitList();
        for (int i = 0; i < unitList.size(); i++) {
            String unitID = (String) unitList.get(i);
            String unitName = messageID + "^" + unitID;
            UnitQueue.remove(unitName);
        }
    }

    /**
     * 일시 정지 되었거나 재발송을 위해 대기중인 메세지의 발송을 재시작한다.
     * 재발송을 위해 대기중이면 현재 시간이
     * retryEndTime(수신 응답 완료 시간보다 하루 작은 시간) 보다 작을 경우에만
     * 재 시작 된다.
     *
     * @return 발송이 재시작되면 true
     */
    public boolean setStart() {
        boolean isSuccess = false;
        boolean isRun = false;

        int sendStatus = messageStatus.getSendStatus();
        switch (sendStatus) {
            case MessageStatus.SEND_PAUSE:
                isRun = true;
                break;
            case MessageStatus.SEND_WAIT:
                if (!message.isTest) {
                    long retryEndTime = message.retryEndTime;
                    if (retryEndTime > System.currentTimeMillis()) {
                        isRun = true;
                    }
                }
                break;
        }

        if (isRun) {
            try {
                messageStatus.setSendStatus(MessageStatus.SEND_RUN);
                isSuccess = true;
            }
            catch (Exception ex) {
            	LOGGER.error(ex);
                //ex.printStackTrace();
            }
        }

        if (isSuccess) {
            unitList = unitSyncMgr.unitList();
            messageMap.add(messageID);
        }

        return isSuccess;
    }

    /**
     * 다음 Unit이 존재하는지 확인한다.
     *
     * @return 다음 Unti이 존재하면 true
     */
    public boolean existsNextUnit() {
        boolean existsUnit = false;
        int msgStatus = messageStatus.getSendStatus();
        if (msgStatus == MessageStatus.SEND_RUN) {
            int unitStatus = messageStatus.getUnitStatus();
            if (unitStatus == MessageStatus.UNIT_LOAD_RUN) {
                existsUnit = true;
            }
            else {
                if (unitList != null && unitList.size() > 0) {
                    existsUnit = true;
                }
            }
        }
        return existsUnit;
    }

    /**
     * 다음 Unit에 대한 UnitInfo 객체를 읽는다.
     * UnitSyncManager객체로 가져온 Unit 리스트가 존재하면 여기에서 가져오고
     * MessageStatus 에서 Unit의 상태 정보와 Unit의 ID 를 가져와서 Unit를 읽는다.
     * MessageID와 sendNo(발송 수)의 조합으로 생성된 디렉토리에서 Unit를 읽어온다.
     *
     * @return 읽을 Unit이 존재 하면 UnitInfo 객체, 존재하지 않으면 null
     */
    public UnitInfo nextUnit() {
        String unitID = null;
        UnitInfo unit = null;
        int sendNo = messageStatus.getSendNo();

        do {
            synchronized (lock) {
                //이전에 Loading이 되었던 Unit이 존재하는지 확인
                if (unitList != null && unitList.size() > 0) {
                    //UnitID를 리스트에서 얻는다.
                    unitID = (String) unitList.remove(0);
                }
                else {
                    //이전에 Loading이 되었던 Unit이 존재하지 않을 경우
                    int id = messageStatus.nextUnit();

                    if (id != -1) {
                        try {
                            messageStatus.syncUnit();
                            unitID = Integer.toString(id);
                            unitSyncMgr.create(unitID);
                        }
                        catch (RepositoryException ex) {
                        	LOGGER.error(ex);
                        }
                    }
                    else {
                        break;
                    }
                }

                if (unitID == null) {
                }
                else {
                    //Unit의 동기화를 위한 파일을 생성한다.

                }
            }
            if (unitID != null) {
                //Unit을 읽는다.
                try {
                    unit = unitFileMgr.readUnit(unitID, sendNo);
                }
                catch (RepositoryException ex) {
                	LOGGER.error(ex);
                    unitSyncMgr.remove(unitID);
                    //System.err.println(ex.getMessage());
                }
            }
        }
        while (unit == null);

        return unit;
    }

    /**
     * 유닛을 읽어들인다.
     * @param unitID 유닛아이디
     * @return 유닛 정보 객체
     */
    public UnitInfo readUnit(String unitID) {
        UnitInfo unit = null;
        int sendNo = messageStatus.getSendNo();

        try {
            unit = unitFileMgr.readUnit(unitID, sendNo);
        }
        catch (RepositoryException ex) {
        	LOGGER.error(ex);
            //ex.printStackTrace();
        }

        return unit;
    }

    public MessageInfo readMessageInfo()
        throws Exception {
        Contents contents = messageStatus.readContent();
        
        // 웹에이전트 보안 HTML
        if("Y".equals(message.webagent_secuYn) && message.webagent_sourceUrl != null && "HTML".equals(message.webagent_secuAttTyp)) {
        	Contents2 contents2 = messageStatus.readContent2();
        	return new MessageInfo(message, contents, contents2);
    	
    	// 웹에이전트 보안 PDF
        }else if("Y".equals(message.webagent_secuYn) && message.webagent_sourceUrl != null && "PDF".equals(message.webagent_secuAttTyp)) {
        	Contents2 contents2 = messageStatus.readContent2();
        	return new MessageInfo(message, contents, contents2);
    	
    	// 웹에이전트 보안 EXCEL
        }else if("Y".equals(message.webagent_secuYn) && message.webagent_sourceUrl != null && "EXCEL".equals(message.webagent_secuAttTyp)) {
        	Contents2 contents2 = messageStatus.readContent2();
        	return new MessageInfo(message, contents, contents2);
        
        }else {
        	return new MessageInfo(message, contents);	
        }
    }

    /**
     * Unit이 Generator로 전달이 완료되었음을 확인한다. Unit에 대한 동기화 파일을 삭제하고 delivery Count를
     * 증가한다. Message에 대한 Unit의 Delivery가 완료되었을 경우 unit의 동기화 파일을 저장하기 위해 생성한
     * 디렉토리를 삭제한다.
     *
     * @param unitID
     *            Delivery가 완료된 Unit의 UnitID
     * @param size
     *            Unit에 포함된 대상자의 수
     */
    public void deliveryUnit(String unitID, int size) {
        unitSyncMgr.remove(unitID);

        synchronized (lock) {
            try {
                messageStatus.increaseDelivery(size);
            }
            catch (Exception ex) {
            	LOGGER.error(ex);
            }
        }
    }

    public void writeResult(ResultUnit result)
        throws Exception {
        if (resultMgr != null) {
            resultMgr.writeResult(result);
        }
    }

    /**
     * Unit의 발송 결과를 읽어서 대상자들중 재발송 대상을 추출하여 UnitInfo객체를 재생성한다.
     * 발송 결과에 포함된 Unit의 발송 완료 시간을 얻어서 Message의 상태를 저장한 MessageStatus 객체에
     * 가장 최근 발송 시간으로 발송 완료 시간을 업데이트한다.
     */
    public void handleResult() {
        String[] list = resultMgr.list();
        if (list == null) {
            return;
        }
        int createUnitID = messageStatus.getCreateUnitID();
        int sendNo = messageStatus.getSendNo();
        int nextSendNo = sendNo + message.retryCount + 1;
        long msgTime = messageStatus.getEndTime();

        for (int i = 0; i < list.length; i++) {
            String name = list[i];
            ResultUnit result = null;
            int successCount = 0;
            int sendCount = 0;
            boolean isWriteUnitID = false;

            try {
                result = resultMgr.readResult(name);
                sendCount = result.getCount();
                long unitTime = result.getSendTime();

                if (msgTime < unitTime) {
                    msgTime = unitTime;
                    messageStatus.setEndTime(unitTime);
                }

                int sendUnitID = result.getUnitID();
                UnitInfo sendUnit
                    = unitFileMgr.readUnit(Integer.toString(sendUnitID), sendNo);

                for (int rowNo = 0; rowNo < sendUnit.size(); rowNo++) {
                    ReceiverInfo receiverInfo
                        = (ReceiverInfo) sendUnit.get(rowNo);
                    if (!result.isRetry(rowNo)) {
                        successCount++;
                    }
                }

                unitFileMgr.deleteUnit(sendUnitID, sendNo);		//unit 폴더 삭제
                messageStatus.putSendCount(sendCount, successCount);

            }
            catch (Exception ex) {
            	LOGGER.error(ex);
            }
            
            resultMgr.delete(name);

            //발송 진행 정보 입력
            int[] setData = new int[5];
            setData[0] = message.taskNo;
            setData[1] = message.subTaskNo;
            setData[2] = sendCount;
            setData[3] = successCount;
            setData[4] = sendCount - successCount;
            ProgressInsert.setData(setData);

        }
    }

    /**
     * Message의 발송 완료 여부를 확인한다.
     *
     * @return 발송이 완료 되면 true
     */
    public boolean isSendEnd() {
        boolean isEnd = false;

        synchronized (lock) {
            int sendStatus = messageStatus.getSendStatus();
            int unitStatus = messageStatus.getUnitStatus();

            if (unitStatus == MessageStatus.UNIT_LOAD_END) {
                int sendNo = messageStatus.getSendNo();

                if (!unitFileMgr.existsUnit(sendNo)) {
                    isEnd = true;
                }
            }
        }
        return isEnd;
    }

    /**
     * 발송 완료 시간과 발송 상태 코드를 업데이트 한다.
     * 재발송을 필요한 대상자가 존재하지 않을 경우 Message의 정보파일들을 삭제한다.
     *
     * @param endTimeManager DB에 업데이트를 실행할 객체
     */
    public void updateEndTime(EndTimeManager endTimeManager) {
        synchronized (lock) {
            long endTime = messageStatus.getEndTime();

            int taskNo = message.taskNo;
            int subTaskNo = message.subTaskNo;

            try {
                endTimeManager.update(message.taskNo, message.subTaskNo, endTime);
                messageStatus.setWait(message.retryCount);

                int createUnitID = messageStatus.getCreateUnitID();
                int lastUnitID = messageStatus.getLastUnitID();

                boolean isCreateUnit = false;
                if (createUnitID > (lastUnitID + 1)) {
                    isCreateUnit = true;
                }

                if (!isCreateUnit) {
                    unitSyncMgr.remove(false); 	//sync_unit 폴더 삭제
                    resultMgr.delete(); 		//result 폴더 삭제
                    messageStatus.delete();		//message 폴더 삭제
                    messageMap.remove(messageID);
                }
            }
            catch (Exception ex) {
            	LOGGER.error(ex);
                //ex.printStackTrace();
            }

        }
    }

    /**
     * 발송 중지 되었거나, 발송 대기중인 Message중에서 재발송 완료 시간이 지난 Message를 삭제한다.
     * 재 발송 완료 시간은 수신 완료 시간에서 하루 전인 시간으로 처리한다.
     * 수신 완료 시간 하루 전보다 이후에는 재발송이 되지 않는다.
     * 테스트 메일은 재발송 대상이 아니므로 삭제한다.
     * 삭제 과정은 sync_unit 디렉토리 삭제 -> result 디렉토리 삭제 -> unit 디렉토리 삭제 -> message 파일 삭제의
     * 순서대로 실행하고 마지막으로 MessageMap 객체에서 MessageHandler 객체를 제거한다.
     */
    public void cleanMessage() {
        boolean isClean = false;

        synchronized (lock) {
            //Message의 발송 상태 검사
            int sendStatus = messageStatus.getSendStatus();
            if (sendStatus == MessageStatus.SEND_STOP) { //발송 중지된 상태
                isClean = true;
            }
            else if (sendStatus == MessageStatus.SEND_WAIT) { //발송 완료된 후 대기 상태
                if (message.isTest) {
                    isClean = true; //테스트 발송
                }
                else {
                    long currentTime = System.currentTimeMillis();
                    long retryEndTime = message.retryEndTime; // 재발송 완료 시간
                    if (currentTime > retryEndTime) {
                        isClean = true;
                    }
                }
            }

            if (isClean) {
                int sendNo = messageStatus.getSendNo();
                unitSyncMgr.remove(true);
                resultMgr.delete();
                unitFileMgr.delete(sendNo);
                messageStatus.delete();
                messageMap.remove(messageID);
            }
        }
    }
}
