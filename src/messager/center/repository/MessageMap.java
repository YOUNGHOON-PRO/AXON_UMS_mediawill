package messager.center.repository;

import java.util.*;

/**
 * 발송 진행중이거나 대기중 중지된 상태, 일시 정지된 상태 모든 상태에 대하여 Message의
 * MessageHandler 객체를 저장하고 있다.
 * 발송 진행중인 상태일 경우 runList에 messageID가 저장되어서 HashMap에 저장된 MessageHandler를 이용해
 * Unit을 가져온다.
 *
 * @author Administrator
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MessageMap
{
    private static MessageMap instance;

    /**
     * MessageMap의 인스턴스를 얻는다.
     *
     * @return MessageMap
     */
    public static MessageMap getInstance() {
        synchronized (MessageMap.class) {
            if (instance == null) {
                instance = new MessageMap();
                instance.loadList();
            }
        }
        return instance;
    }

    /** MessageID 리스트 */
    private ArrayList runList;

    /** MessageID와 MessageHandler의 맵핑 */
    private HashMap handlerMap;

    /** 동기화에 사용되는 lock 객체 */
    private Object lock = null;

    private MessageMap() {
        runList = new ArrayList(100);
        handlerMap = new HashMap();
        lock = new Object();
    }

    /**
     * 저장소에 저장된 Message들을 로드한다.
     * Message의 상태와 정보 파일을 관리하는 MessageStatus를 이용하여 MessageID 리스트를 얻고
     * MessageID로 MessageHandler 객체를 생성하여 등록한다.
     *
     */
    private void loadList() {
        String[] msgList = MessageStatus.list();
        for (int i = 0; i < msgList.length; i++) {
            try {
                String messageID = msgList[i];
                MessageHandler messageHandler = new MessageHandler(messageID);
                registry(messageHandler);
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 발송 할 Message가 존재하는지 확인 한다.
     *
     * @return 발송할 Message가 존재하면 false를 리턴한다.
     */
    public boolean isEmpty() {
        boolean isEmpty = true;
        synchronized (lock) {
            if (runList.size() > 0) {
                isEmpty = false;
            }
        }
        return isEmpty;
    }

    /**
     * 발송 할 Message의 다음 작업할 Message의 MessageHandler 객체를 얻는다.
     *
     * @return 발송 할 Message가 존재하지 않으면 null를 리턴하고, 발송할 Message가 존재하면 Message의
     *         MessageHandler 객체를 리턴한다.
     */
    public MessageHandler nextHandler() {
        MessageHandler msgHandler = null;
        synchronized (lock) {
            while (runList.size() > 0) {
                //List에서 MessageID를 얻는다.
                String messageID = (String) runList.remove(0);

                //MessageID로 MessageHandler객체를 얻는다.
                msgHandler = (MessageHandler) handlerMap.get(messageID);

                //Message의 MessageHandler객체가 존재하지 않으면 list에서 MessageID는 제거된다.
                if (msgHandler != null) {
                    boolean existsUnit = msgHandler.existsNextUnit();
                    if (existsUnit) {
                        runList.add(messageID);
                        break;
                    }
                }
            }
        }
        return msgHandler;
    }

    /**
     * Message의 상태파일을 이용하여 MessageHandler 객체를 생성한 후 등록한다.
     *
     * @param messageID 메시지 아이디
     * @param messageStatus 메시지 상태
     * @return 등록된 Message가 아니고 등록이 성공하면  true
     */
    public boolean registry(String messageID, MessageStatus messageStatus) {
        boolean success = false;
        MessageHandler msgHandler = null;
        try {
            msgHandler = new MessageHandler(messageID, messageStatus);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        if (msgHandler != null) {
            synchronized (lock) {
                if (!handlerMap.containsKey(messageID)) {
                    handlerMap.put(messageID, msgHandler);
                    runList.add(messageID);
                    success = true;
                }
            }
        }
        return success;
    }

    /**
     * Message를 맵과 발송 리스트에 등록한다.
     *
     * @param msgHandler 등록할 Message의 MessageHandler 객체
     * @return Message가 등록되면 true리턴하고 이미 MessageHandler가 존재하여 등록을 실패하면 false 리턴
     */
    public boolean registry(MessageHandler msgHandler) {
        boolean isSuccess = false;
        String messageID = msgHandler.getMessageID();
        synchronized (lock) {
            if (!handlerMap.containsKey(messageID)) {
                handlerMap.put(messageID, msgHandler);
            }
            runList.add(messageID);
            isSuccess = true;
        }
        return isSuccess;
    }

    /**
     * 발송의 재 시작을 위하여 runList에 등록된 Message의 ID를 추가한다.
     *
     * @param messageID 메시지 아이디
     * @return 메시지 아이디를 동작 리스트에 입력하면 true, 그렇지 않으면 false
     */
    public boolean add(String messageID) {
        boolean isSuccess = false;
        synchronized (lock) {
            if (handlerMap.containsKey(messageID)) {
                runList.add(messageID);
                isSuccess = true;
            }
        }
        return isSuccess;
    }

//	public boolean registry(MessageHandler msgHandler) {
//		return registry(msgHandler, false);
//	}

    /**
     * 발송중인 MessageID 리스트를 얻는다.
     *
     * @return 발송 중인 MessageID를 element로 갖는 ArrayList를 리턴한다.
     */
    public ArrayList list() {
        ArrayList list = new ArrayList();
        synchronized (lock) {
            if (handlerMap.size() > 0) {
                Set set = handlerMap.keySet();
                Iterator iterator = set.iterator();
                while (iterator.hasNext()) {
                    list.add(iterator.next());
                }
            }
        }
        return list;
    }

    /**
     * 발송 중인 Message의 MessageHandler 리스트를 얻는다.
     *
     * @return 발송중인 Message의 MessageHandler객체를 element로 갖는 ArrayList를 리턴한다.
     */
    public ArrayList handlerList() {
        ArrayList list = new ArrayList();
        synchronized (lock) {
            if (handlerMap.size() > 0) {
                Collection values = handlerMap.values();
                Iterator iterator = values.iterator();
                while (iterator.hasNext()) {
                    list.add(iterator.next());
                }
            }
        }
        return list;
    }

    /**
     * Message의 ID로 등록된 MessageHandler를 얻는다.
     *
     * @param messageID 메시지 아이디
     * @return MessageHandler객체
     */
    public MessageHandler lookup(String messageID) {
        MessageHandler msgHandler = null;
        synchronized (lock) {
            msgHandler = (MessageHandler) handlerMap.get(messageID);
        }
        return msgHandler;
    }

    /**
     * taskNo와 subTaskNo로 MessageID를 생성한다.
     *
     * @param taskNo 작업 번호
     * @param subTaskNo 부 작업 번호
     * @return taskNo + '-' + subTaskNo 로 이루어진 MessageID
     */
    public String createMessageID(int taskNo, int subTaskNo) {
        StringBuffer sb = new StringBuffer();
        sb.append(Integer.toString(taskNo)).append('-').append(
            Integer.toString(subTaskNo));
        return sb.toString();
    }

    /**
     * 등록된 Message를 제거한다.
     *
     * @param messageID 메시지 아이디
     */
    public void remove(String messageID) {
        handlerMap.remove(messageID);
    }
}
