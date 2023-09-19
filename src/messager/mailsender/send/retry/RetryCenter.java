package messager.mailsender.send.retry;

import java.util.*;

import messager.common.*;
import messager.mailsender.code.*;
import messager.mailsender.config.*;
import messager.mailsender.connect.*;
import messager.mailsender.message.*;
import messager.mailsender.send.*;
import messager.mailsender.send.dns.*;
import messager.mailsender.sendlog.*;
import messager.mailsender.util.*;

public class RetryCenter
    extends Thread
{
    /*************** ConfigLoader Values ****************/
    private int RETRY_SENDER_AGENT; // 재발송 AGENT수
    private int RETRY_TIMES; // 재발송 횟수
    private int RETRY_WAIT_TIME; // 재발송 중간 지연 시간

    /**************** Inner Function Values **************/
    private static Vector mxRecord = new Vector(); // 도메인의 mx 레코드의 IP 집합
    private static Vector retryList; // 로그 파일에서 추출한 재발송 대상자
//	private AgentControler retryAgentWatcher;
    private UnitThreadCounter unitGuard;
    private byte[] rAttachData;
    private Hashtable mxRecordTable = new Hashtable();

    /****************** Class Values ********************/
    private SendLogFileAccess rAccess; // 로그 파일 객체
    private DMBundle rBundle; // 재발송 유닛 객체
    private DomainMessage rdm; // Bundle내의 도메인별 객체
    private DMSendUnit DMSU; // 발송 객체
    private CountManager retryCManager; // 재발송 카운터 객체

    /***************** Common Values *******************/
    private String RMODE; // 재발송 모드(True : 발송 , False : 가상 발송)
    private String unitName; // 유닛 네임
    private String sender; // 재발송 보내는이
    private String beforeDomain; // 도메인별로 분류된 객체를 발송할때 이전 발송한 도메인과 비교하기위한 도메인
    private int RSOCKET_TIMEOUT; // 재발송 소켓 연결 시간
    private int sendNo = 0;
    private int retryCount;
    private static int BLOCK_DEFAULTSESSION;

    public RetryCenter(String aUnitName, SendLogFileAccess access, Vector rList, byte[] attachData) {

        RETRY_SENDER_AGENT = ConfigLoader.RETRY_SENDER_AGENT;
        RETRY_WAIT_TIME = ConfigLoader.RETRY_WAIT_TIME;
        unitName = aUnitName;
        retryList = rList;
        rAccess = access;
        rAttachData = attachData;
        unitGuard = new UnitThreadCounter(); // 현재 유닛의 발송 쓰레드
        mxRecordTable = Passage.mxRecordTable;

    }

    public void run() {
        rBundle = remakeBundle(unitName, retryList);
        Vector dmRList = new Vector();
        dmRList = rBundle.getMessage();

        System.out.println("Retry send start ..... [UNIT NAME:" + unitName + "][DOMAIN COUNT:" + dmRList.size() + "][UNIT COUNT:" + retryList.size() + "]");

        boolean bLoop = false;
        String currentDomain = "";

        retryCManager = new CountManager();
        retryCManager.setTotalCount(retryList.size());

        if (rBundle.sendNo > 0) {
            sendNo = rBundle.sendNo + rBundle.retryCount;
            retryCount = sendNo + rBundle.retryCount;
        }
        else {
            retryCount = rBundle.retryCount;
        }

        /*************************
         * 재발송 등록 정보 DEBUG 출력
         * writed by ?????
         */
        /*********************************************************************
                        System.out.println(new StringBuffer()
                                           .append("RetryCenter.java : ").append("\r\n")
                                           .append("\n++++++++++++++++++++++").append("\r\n")
                                           .append("unitName : ").append(unitName).append("\r\n")
                                           .append("taskNo : ").append(rBundle.taskNo).append("\r\n")
                                           .append("subTaskNo : ").append(rBundle.subTaskNo).append("\r\n")
                                           .append("Total Account : ").append(retryList.size()).append("\r\n")
                                           .append("Start No : ").append(sendNo).append("\r\n")
                                           .append("retryCount : ").append(retryCount).append("\r\n")
                                           .append("Appended File : ").append(rBundle.appendedFile).append("\r\n")
                                           .append("rSendMode : ").append(rBundle.sendMode).append("\r\n")
                                           .append("rSendTest : ").append(rBundle.sendTest).append("\r\n")
                                           .append("DMCount : ").append(dmRList.size()).append("\r\n")
                                           .append("++++++++++++++++++++++\n").append("\r\n")
                                           .toString()
                                           );
         *********************************************************************/
        int i = 0;
        while (dmRList.size() > 0) {
            i++;
            if (unitGuard.getSize() < RETRY_SENDER_AGENT) {
                bLoop = false;
                rdm = (DomainMessage) dmRList.remove(0);
                currentDomain = rdm.domain;
                String retryAgentNM = unitName + "(R)" + "_" + currentDomain + "_" + i;
                unitGuard.addUnitFactor(retryAgentNM);
                if (!currentDomain.equals(beforeDomain)) {
                    beforeDomain = currentDomain;
                    mxRecord = rdm.MXRecord;
                }

                DMSU = new DMSendUnit();
                DMSU.setDeptNo(rBundle.deptNo);
                DMSU.setUserNo(rBundle.userNo);
                DMSU.setCampTyNo(rBundle.campTyNo);
                DMSU.setCampNo(rBundle.campNo);
                DMSU.setTaskNo(rBundle.taskNo);
                DMSU.setSubTaskNo(rBundle.subTaskNo);
                DMSU.setMessage(rdm);
                DMSU.setSendLogFileAccess(rAccess);
                DMSU.setCountManager(retryCManager);
                DMSU.setAppendedFile(rAttachData);
                DMSU.setIsAppended(rBundle.appendedFile);
                DMSU.setSocketTimeOut(rBundle.socketTimeOut);
                DMSU.setSendMode(rBundle.sendMode);
                DMSU.setSendTest(rBundle.sendTest);
                DMSU.setmxRecord(mxRecord);
                DMSU.setSender(rBundle.senderEmail);
                DMSU.setUnitName(unitName);
                DMSU.setSendNo(sendNo);
                DMSU.setRetryCount(retryCount);
                DMSU.setAgentName(retryAgentNM);
                DMSU.setTargetGrpTY(rBundle.target_grp_ty);
                RetrySender retrySender = new RetrySender(DMSU, unitGuard);
                retrySender.setName(retryAgentNM);
                retrySender.start();
            }
            else {
                i--;
            }
        }
    }

    private DMBundle remakeBundle(String unitName, Vector rList) {
        DMBundle retryBundle = new DMBundle();
        DomainMessage dMessage;
        UserMessage uMessage;
        Vector receivers = new Vector();
        Vector dmList = new Vector();
        Vector mxRecord = new Vector(); // 해당 도메인의 IP LIST

        ObjectManager objManager = new ObjectManager();
        String ERROR_TYPE = objManager.readObject(unitName);
        if (ERROR_TYPE.equals(ErrorCode.SUCCESS)) {
            SendUnit unitObj = objManager.getSendUnit();

            BLOCK_DEFAULTSESSION = unitObj.connPerCount;

            if (unitObj != null) {
                String senderEmail = unitObj.senderEmail;
                retryBundle.setDeptNo(unitObj.deptNo);
                retryBundle.setUserNo(unitObj.userNo);
                retryBundle.setCampTyNo(unitObj.campaignType);
                retryBundle.setCampNo(unitObj.campaignNo);
                retryBundle.setTaskNo(unitObj.taskNo);
                retryBundle.setSubTaskNo(unitObj.subTaskNo);
                retryBundle.setAppendedFile(unitObj.existsFileContent);
                retryBundle.setSocketTimeOut(unitObj.socketTimeout * 1000);
                retryBundle.setUnitName(unitName);
                retryBundle.setSenderEmail(unitObj.senderEmail);
                retryBundle.setSendMode(unitObj.sendMode);
                retryBundle.setSendNo(unitObj.sendNo);
                retryBundle.setRetryCount(unitObj.retryCount);
                retryBundle.setTargetGrpTY(unitObj.target_grp_ty);

                Iterator deGroup = unitObj.iterator();
                int rowID;
                String domain; 		// 도메인
                String antID; 		// 회원 아이디
                String emailAddr; 	// 회원 이메일
                String antName; 	// 회원 이름
                String encKey;		// 보안메일 암호키
                String bizKey;		// BIZ KEY

                int recvCount;
                int blockSession;
                int DefaultSession = 0;
//				retryUnitEntireAccounts = 0;

                String tempRowID = null;
                // DomainElement 갯수만큼 반복
                while (deGroup.hasNext()) {
                    SendDomain dElement = (SendDomain) deGroup.next();
                    domain = dElement.getName();
                    boolean isEmpty = true;
                    boolean ipCheck = false;

                    /*
                     * 해당 도메인이 MXRecord Table에 있는지 검사
                     * [domain , MX RECORD(Vector)]
                     * [domain , MX RECORD(Vector)]
                     * [domain , MX RECORD(Vector)]
                     */
                    mxRecord = (Vector) mxRecordTable.get(domain.trim());

                    if (mxRecord == null) {
                        LookupCaller look = new LookupCaller(ConfigLoader.NAME_SERVER, domain);
                        mxRecord = look.getMxRecords();
                        mxRecordTable.put(domain.trim(), mxRecord);
                    }

                    if (ConfigLoader.BLOCK_SESSION.get(domain) != null) {
                        blockSession = Integer.parseInt( (String) ConfigLoader.BLOCK_SESSION.get(domain));
                    }
                    else {
                        blockSession = 0;
                    }

                    DefaultSession = BLOCK_DEFAULTSESSION;

                    ArrayList userList = dElement;
                    recvCount = userList.size();
                    SendTo uElement;

                    if ( (blockSession == 0) || (blockSession > recvCount)) {
                        int index = 0;

                        int dmCount = ( (recvCount / DefaultSession) + 1);

                        for (int j = 0; j < dmCount; j++) {
                            if ( (j + 1) == dmCount) {
                                DefaultSession = (recvCount % DefaultSession);
                            }
                            dMessage = new DomainMessage();
                            dMessage.setDomain(domain);
                            dMessage.setMXRecord(mxRecord);
                            // UserElement 갯수만큼 반복
                            for (int k = 0; k < DefaultSession; k++) {
                                uMessage = new UserMessage();
                                uElement = (SendTo) userList.get(index++);
                                rowID = uElement.rowID;
                                if (rList.contains(String.valueOf(rowID))) {
                                    antID = uElement.id;
                                    antName = uElement.name;
                                    emailAddr = uElement.email;
                                    bizKey = uElement.bizKey;
                                    encKey = uElement.encKey;
                                    
                                    uMessage.setRowID(rowID);
                                    uMessage.setAccountID(antID);
                                    uMessage.setEmailAddr(emailAddr);
                                    uMessage.setAccountName(antName);
                                    uMessage.setBizkey(bizKey);
                                    uMessage.setEnckey(encKey);
                                    
                                    dMessage.addRecvList(uMessage);
                                    isEmpty = false; // 재발송 대상자가 있을때만 dMessage를 retryBundle에 추가 한다.
//									retryUnitEntireAccounts++;
                                }
                            }
                            if (!isEmpty) {
                                retryBundle.addMessage(dMessage);
                                isEmpty = true;
                            }

                        }
                    }
                    else {
                        int index = 0;
                        int dmCount = ( (recvCount / blockSession) + 1);

                        // 결국 UserElement 갯수만큼 반복
                        for (int j = 0; j < dmCount; j++) {
                            if ( (j + 1) == dmCount) {
                                blockSession = (recvCount % blockSession);
                            }
                            dMessage = new DomainMessage();
                            dMessage.setDomain(domain);
                            dMessage.setMXRecord(mxRecord);
                            for (int k = 0; k < blockSession; k++) {
                                uMessage = new UserMessage();
                                uElement = (SendTo) userList.get(index++);
                                rowID = uElement.rowID;
                                if (rList.contains(String.valueOf(rowID))) {
                                    antID = uElement.id;
                                    antName = uElement.name;
                                    emailAddr = uElement.email;
                                    bizKey = uElement.bizKey;
                                    encKey = uElement.encKey;
                                    
                                    uMessage.setRowID(rowID);
                                    uMessage.setAccountID(antID);
                                    uMessage.setEmailAddr(emailAddr);
                                    uMessage.setAccountName(antName);
                                    uMessage.setBizkey(bizKey);
                                    uMessage.setEnckey(encKey);
                                    
                                    dMessage.addRecvList(uMessage);
                                    isEmpty = false;
//									retryUnitEntireAccounts++;
                                }
                            }
                            if (!isEmpty) {
                                retryBundle.addMessage(dMessage);
                                isEmpty = true;
                            }
                        }
                    }
                }
            }
        }
        else {
            LogWriter.writeError("reading objcet file for RETRY", "run()", ERROR_TYPE, "1001 : 이미 존재함 , 1003 : 오브젝트 읽는데 실패");
        }
        return retryBundle;
    }
}
