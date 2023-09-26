package messager.generator.request;

import java.util.*;

import messager.common.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * MessageInfo객체를 cache 한다. MessageCenter에서 MessageInfo객체를 매번 요청하지 않고 여기에 저장을 한
 * 후 같은 Message일 경우 여기에서 MessageInfo 객체를 얻는다. MessageInfo객체의 remove 시점은 10간동안
 * MessageInfo 객체를 접근하지 않았을 경우 remove된다.
 */
class MessageInfoCache
{
	private static final Logger LOGGER = LogManager.getLogger(MessageInfoCache.class.getName());

    //Instance
    private static MessageInfoCache messageInfoCache;

    //MessageInfo를 저장한다.(messageID를 Key)
    private HashMap messageMap;

    //동기화를 위해 사용되는 객체
    private Object lock;

    //시간동안 접근 하지 않았을 경우 MessageInfo 객체를 삭제한다.
    private long cleanningTime = 10 * 60 * 1000;

    //messageMap에서 접근 시간을 검사해서 MessageInfo 객체를 삭제하는 Thread
    private CacheClenner clennerThread;

    //Instance를 생성한다.
    public synchronized static MessageInfoCache getInstance() {

        if (messageInfoCache == null) {

            messageInfoCache = new MessageInfoCache();

        }

        return messageInfoCache;

    }

    //객체를 생성한다.
    private MessageInfoCache() {

        messageMap = new HashMap();

        lock = new Object();

        //MessageInfo 객체를 제거하는 Thread 실행
        clennerThread = new CacheClenner(messageMap, cleanningTime);

        clennerThread.start();

    }

    /*
     * MessageInfo 객체를 검색하고 검색된 MessageInfo객체에 대한 접근시간을 현재시간으로 수정한다. @param
     * messageID @return MessageInfo, MessageInfo를 검색하지 못했을 경우 null를 리턴
     */
    public MessageInfo lookup(String messageID) {

        long currentTime = System.currentTimeMillis();

        MessageInfo messageInfo = null;

        CacheObject cacheObject = null;

        synchronized (lock) {

            //CacheObject로 작업한다.
            cacheObject = (CacheObject) messageMap.get(messageID);

            if (cacheObject != null) {

                //현재시간을 접근 시간으로 한다.
                cacheObject.accessTime = currentTime;

                messageInfo = cacheObject.messageInfo;

            }

        }

        return messageInfo;

    }

    /*
     * CacheObject를 검색한다. @param messageID @return CacheObject
     */
    private CacheObject search(String messageID) {

        CacheObject cacheObject = null;

        synchronized (lock) {

            cacheObject = (CacheObject) messageMap.get(messageID);

        }

        return cacheObject;

    }

    /**
     * 새로운 MessageInfo 객체를 저장한다.
     *
     * @param messageInfo
     */
    public void put(MessageInfo messageInfo) {

        String messageID = messageInfo.getMessageID();

        CacheObject cacheObject = new CacheObject(messageInfo);

        synchronized (lock) {

            messageMap.put(messageID, cacheObject);

        }
    }

    /**
     * 저장된 MessageInfo 객체를 삭제한다.
     *
     * @param messageID
     */
    private void remove(String messageID) {

        CacheObject cacheObject = null;

        synchronized (lock) {

            cacheObject = (CacheObject) messageMap.remove(messageID);

        }

        if (cacheObject != null) {

            cacheObject.messageInfo = null;

            cacheObject = null;

        }

    }

    /**
     * MessageInfo의 접근 시간을 지정할 클래스
     */
    class CacheObject
    {

        public MessageInfo messageInfo;

        //messageInfo의 접근시간
        public long accessTime;

        /**
         * 객체를 생성할 때 접근 시간을 현재시간으로 한다.
         */
        public CacheObject(MessageInfo aMessageInfo) {

            messageInfo = aMessageInfo;

            accessTime = System.currentTimeMillis();

        }
    }

    /**
     * 접근 시간이 지정된 시간보다 경과한 MessageInfo 객체를 삭제하는 작업을 담당하는 Thread
     */
    class CacheClenner
        extends Thread
    {

        //HashMap에 저장되고 있는 키의 셋트뷰
        private Set keyset;

        // Thread의 작업 주기 (대기시간)
        private long waitTime;

        /**
         * 객체 생성
         *
         * @param hashMap
         *            MessageInfo 객체가 저장된 HashMap
         * @param aTime
         *            MessageInfo의 삭제하기 위한 최대 접근 시간
         */
        public CacheClenner(HashMap hashMap, long aTime) {

            keyset = hashMap.keySet();

            waitTime = aTime;

        }

        /**
         * 최대 접근 시간이 경과한 MessageInfo객체를 삭제한다.
         */
        private void mainLoop() {

            //keyset에 대한 Iterator객체를 얻는다.(순차적으로 key를 얻기 위해)
            Iterator iterator = keyset.iterator();

            //접근 경과 시간을 비교하기 위하여 현재시각을 얻는다.
            long currentTime = System.currentTimeMillis();

            while (true) {

                try {

                    while (messageInfoCache != null && iterator.hasNext()) {

                        //Iterator에서 messageID를 꺼낸다.
                        String messageID = (String) iterator.next();

                        // MessageID를 키로 해서 MessageInfoCache instance에서
                        // CacheObject를 검색한다.
                        CacheObject cacheObject = messageInfoCache
                            .search(messageID);

                        if (cacheObject != null) {

                            long time = currentTime - cacheObject.accessTime;

                            //접근 경과 시간을 얻는다.

                            //접근경과 시간이 최대 접근 경과 시간이 지났는지 확인
                            if (time > waitTime) {

                                //최대 접근 시간이 지났으므로 MessageInfo가 저장된 CacheObject
                                // 객체를 삭제한다.
                                messageInfoCache.remove(messageID);

                            }

                        }

                    }

                }
                catch (Exception ex) {
                	LOGGER.error(ex);
                }

                // 지정된 시간만큼 대기한다.
                try {

                    sleep(waitTime);

                }
                catch (InterruptedException ex) {
                	LOGGER.error(ex);
                }

            }

        }

        /**
         * run
         */
        public void run() {

            mainLoop();

        }
    }
}