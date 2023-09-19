package messager.mailsender.connect;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

import messager.common.*;
import messager.mailsender.code.*;
import messager.mailsender.config.*;
import messager.mailsender.message.*;
import messager.mailsender.send.*;
import messager.mailsender.send.dns.*;
import messager.mailsender.sendlog.*;
import messager.mailsender.util.*;

/**
 * Message Generator에서 메일발송 아이템을 얻어오는 클래스임
 * +------------+              +-----------+
 * |            |   <-------   |           |  Make Thread  +-------------+
 * | Generator  |   REQUEST    | Transfer  |  ---------->  |Sending Email|
 * |            |   ------->   |           |               +-------------+
 * +------------+   RESPONSE   +-----------+
 *
 * Mail Transfer는 Message Generator에 지정된 포트로 연결하여 일정 시간 간격마다
 * "REQUEST"명령을 전달한다. 발송할 메일항목이 존재하면 Message Generator는
 * Mail Transfer에 UNIT NAME 데이터를 전달하고, 항목이 존재하지 않으면
 * "NOP" 데이터를 전달한다.
 * 전달받은 UNIT NAME에 해당하는 OBJECT를 읽어 들여 도메인별로 분류후 발송한다.
 *
 * REQUEST
 * S : UNITNAME
 * F : NOP
 */
public class Passage
    extends Thread
{
    /******* ConfigLoader Values ***********/
    private static int AGENT_LIMIT; // Thread 생성 제한값
    private static int GEN_PORT; // Generator과의 통신 포트
    private static String IP; // Generator과의 통신 IP
    private static String MAILSENDER_ID; // MailSender ID

    /********* Inner Function Values *******/
    private Socket sock; // Generator과 통신할 소켓
    private BufferedReader in; // Generator로 명령을 받을 스트림
    private PrintStream out; // Generator로 명령을 보낼 스트림
    private InputStream iStream; // Generator로 명령을 보낼 스트림
    private OutputStream oStream; // Generator로 명령을 보낼 스트림
    public static Hashtable mxRecordTable; // 도메인별 MXRECORD를 보관하는 테이블
    private Vector recoveryAccount; // 복구 대상자
    private AgentControler agentWatcher; // 전체 발송 쓰레드 관리 객체

    /************ Class Values *************/
    private DMBundle bundle; // Object를 Transfer가 사용할수 있는 단위 객체
    private SendLogFileAccess access; // 로그 파일을 기록하는 클래스
    private SendLogRecord record; // 로그 파일 포맷 객체

    private UnitThreadCounter unitGuard; // 현재 발송 중인 유닛의 쓰레드 관리
    /************ Common Values ************/
    private boolean isRecovery = false; // 현재 작업이 복구 작업
    private boolean occurEmailError = false; // 이메일 문법 에러가 존재하고 대상자 추출 되었음 (true : 에러 발생 , false: 에러 없슴)
    private String ERROR_TYPE; // Object를 읽는중에 발생하는 에러 타입
    private String PROCESS_PATH; // Process 파일 경로
    private String info; // Generator로 받은 명령
    private String[] recoveryList; // 복구할 UNITNAME 리스트
    private int unitEntireAccounts; // UNIT 전체 대상자 수
    private int recoveryEntireAccount; // 복구 UNIT 전체 대상자 수
    private int emailErrorAccounts; // 이메일 문법 에러 대상자 수
    private int BLOCK_DEFAULTSESSION; // MERGE_DOMAIN_SLICE

    public Passage() {
    	IP = ConfigLoader.GENERATOR_IP;
        GEN_PORT = Integer.parseInt(ConfigLoader.GENERATOR_PORT);
        AGENT_LIMIT = ConfigLoader.MESSAGE_SENDER_AGENT;
        MAILSENDER_ID = ConfigLoader.MAILSENDER_ID;
        PROCESS_PATH = new StringBuffer(ConfigLoader.TRANSFER_ROOT_DIR)
            .append(ConfigLoader.TRANSFER_REPOSITORY_DIR)
            .append("send/").append(MAILSENDER_ID)
            .append(File.separator).toString();

        System.out.println("CURRENT MAILDSENDER_ID : " + MAILSENDER_ID);
        mxRecordTable = ConfigLoader.cachingDomain();
    }

    /**ㄹ
     *  Generator와 통신 Thread 실행
     * @see java.lang.Runnable#run()
     */
    public void run() {
        boolean revokeSocket = false;
        while (true) {
            try {
                if (revokeSocket) {
                    System.out.println("Trying to reconnect.....");
                    System.out.println("IP : " + IP);
                    System.out.println("GEN_PORT : " + GEN_PORT);
                }

                //sock = new Socket("localhost", GEN_PORT);
                sock = new Socket(IP, GEN_PORT);
                
                iStream = sock.getInputStream();
                oStream = sock.getOutputStream();
                out = new PrintStream(oStream);
                in = new BufferedReader(new InputStreamReader(iStream));

                if (out != null) {
                    System.out.println("Successfully connect to Generator");
                }
                else {
                    System.out.println("Fail connect to Generator");
                }
                out.println(MAILSENDER_ID); // Mail Transfer ID를 Generator에게 보낸다.
                out.flush();

                // Recovery Loop
                try {
                    searchRecoveryFiles();
                    if (recoveryList.length > 0) {
                        isRecovery = true;
                    }
                    else {
                        isRecovery = false;
                        System.out.println("We cann't find any recovery stuffs. So, now!!! let's start normal processes");
                    }
                }
                catch (Exception e) {
                    LogWriter.writeException("Passage", "run()", "There is something wrong, as we are searching recovery list", e);
                }

                boolean aLoop = true;
                while (aLoop) {
                    if (isRecovery) {
                        System.out.println("Beginnig to the units of recovery that were failed");
                        for (int k = 0; k < recoveryList.length; k++) {
                            Secretary(recoveryList[k], isRecovery);
                        }
                        System.out.println("Finished a job of recovery");
                        isRecovery = false;
                    }
                    else {
                        out.println("REQUEST");
                        out.flush();
                        info = in.readLine().trim();
                        if (!info.equalsIgnoreCase("NOP")) {
                            System.err.println("Unit start -> " + info);
                            if (Secretary(info, isRecovery)) { 
                                out.println("OK+");
                                out.flush();
                            }
                            else {
                                out.println("NOP");
                                out.flush();
                            }
                        }
                        try {
                            if (info.equalsIgnoreCase("NOP")) { // queue에 대기된 작업이 없을때는 5초간 휴면
                                sleep(5000);
                            }
                            else {
                                sleep(1000);
                            }
                        }
                        catch (InterruptedException e) {}
                        // Generator과 통신이 끊어 졌을경우
                        if (in == null || out == null) {
                            aLoop = false;
                            revokeSocket = true;
                            LogWriter.writeError("Passage", "run()", "cannot read unitName from Generator", "Generator의 상태를 확인하세요");
                        }
                    }
                }
            }
            catch (Exception e) {
            	e.printStackTrace();
                System.out.println("Cannot access Message Generator..retry!");
                System.out.flush();
                revokeSocket = true;
            }
            finally {
                if (in != null) {
                    try {
                        in.close();
                    }
                    catch (Exception e) {}
                    finally {
                        in = null;
                    }
                }

                if (out != null) {
                    try {
                        out.close();
                    }
                    catch (Exception e) {}
                    finally {
                        out = null;
                    }
                }

                if (sock != null) {
                    try {
                        sock.close();
                    }
                    catch (Exception e) {}
                    finally {
                        sock = null;
                    }
                }
            }
            try {
                sleep(1000);
            }
            catch (InterruptedException e) {}
        }
    }

    // PROCESS 파일을 검색하여 이전 실행되었던 작업을 복구 한다.
    private String[] searchRecoveryFiles() {
        recoveryList = null;
        String recoveryPath = new StringBuffer(ConfigLoader.TRANSFER_ROOT_DIR)
            .append(ConfigLoader.TRANSFER_REPOSITORY_DIR)
            .append("send/").append(ConfigLoader.MAILSENDER_ID)
            .append(File.separator).toString();
        try {
            recoveryList = new java.io.File(recoveryPath).list();
            return recoveryList;
        }
        catch (Exception e) {
            return recoveryList;
        }
    }

    /**
     * Object파일을 읽어 Bundle를 생성 -> ControlTower()를 실행한다.
     * @param unitName : Message Generator로 부터 받은 unitName.
     * @param isRecovery : 현재 진행할 프로세스가 복구 작업 또는 일반 작업인지의 구분자.
     */
    private boolean Secretary(String unitName, boolean isRecovery) {
        ERROR_TYPE = FileManager.makeNameFile(PROCESS_PATH, unitName);
        bundle = new DMBundle();
        if (isRecovery) {
            recoveryAccount = new Vector();
            recoveryAccount = recoveryMembers(unitName);
            if (recoveryAccount.size() < 1) {
                System.out.println("There are nothing, already these unit were done");
                FileManager.deleteUnitFiles(unitName, false);
                return false;
            }
        }

        ObjectManager objManager = new ObjectManager();
        ERROR_TYPE = objManager.readObject(unitName);
        if (ERROR_TYPE.equals(ErrorCode.SUCCESS)) {
            SendUnit unitObj = objManager.getSendUnit();

            if (unitObj != null) {
                bundle = makeDMBundle(unitObj, unitName, isRecovery);
                new ControlTower(bundle);
                return true;
            }
        }
        else {
            LogWriter.writeError("Secretary", "run()", ERROR_TYPE, "UNIT NAME : " + unitName + "  1001 : 이미 존재함 , 1003 : 오브젝트 읽는데 실패, 2001 : 오브젝트 파일이 없음");
            return false;
        }
        return false;
    }

    /**
     * Message Generator의해 생성된 Object를 발송 단위로 재 생성
     * @param unit : object 객체
     * @param unitName : unitName
     * @param isRecovery : 복구 대상 여부 (true : 복구 대상 , false : 복구 대상 아님)
     */
    public DMBundle makeDMBundle(SendUnit unit, String unitName, boolean isRecovery) {
        BLOCK_DEFAULTSESSION = unit.connPerCount; // Default Session Value
        DMBundle tempbundle = new DMBundle();
        DomainMessage dMessage;
        UserMessage uMessage;

        // 해당 도메인의 IP LIST
        tempbundle.setDeptNo(unit.deptNo);
        tempbundle.setUserNo(unit.userNo);
        tempbundle.setCampTyNo(unit.campaignType);
        tempbundle.setCampNo(unit.campaignNo);
        tempbundle.setTaskNo(unit.taskNo);
        tempbundle.setSubTaskNo(unit.subTaskNo);
        tempbundle.setAppendedFile(unit.existsFileContent);
        tempbundle.setSocketTimeOut(unit.socketTimeout * 1000);
        tempbundle.setUnitName(unitName);
        tempbundle.setSenderEmail(unit.senderEmail);
        tempbundle.setSendMode(unit.sendMode);
        tempbundle.setSendTest(unit.isTest);
        tempbundle.setSendNo(unit.sendNo);
        tempbundle.setRetryCount(unit.retryCount);
        tempbundle.setTargetGrpTY(unit.target_grp_ty);

        Iterator deGroup = unit.iterator();
        int rowID;
        String domain; // 도메인
        String antID; //	회원 아이디
        String emailAddr; // 회원 이메일
        String antName; // 회원 이름
        String enckey; // 회원 이름
        String bizkey; // 회원 이름

        unitEntireAccounts = 0;
        recoveryEntireAccount = 0;
        emailErrorAccounts = 0;
        int recvCount;
        int blockSession;
        int defaultSession;
        boolean emailError = true; // 이메일 에러가 존재하는지 검사 한다.
        System.out.println("------<" + unitName + " Making DMBundle  >------");

        // DomainElement 갯수만큼 반복
        while (deGroup.hasNext()) {
            SendDomain dElement = (SendDomain) deGroup.next();
            domain = dElement.getName().trim();
            boolean isEmpty = true; // dMessage가 비어 있는지 체크
            Vector mxRecord = new Vector();

            /*
             * 해당 도메인이 MXRecord Table에 있는지 검사
             * [key    , contain values   ]
             * [domain , MX RECORD(Vector)]
             * [domain , MX RECORD(Vector)]
             * [domain , MX RECORD(Vector)]
             */
            mxRecord = (Vector) mxRecordTable.get(domain);
            if (mxRecord == null) {
                LookupCaller look = new LookupCaller(ConfigLoader.NAME_SERVER, domain);
                mxRecord = look.getMxRecords();
                mxRecordTable.put(domain, mxRecord);
            }

            if (ConfigLoader.BLOCK_SESSION.get(domain) != null) {
                blockSession = Integer.parseInt( (String) ConfigLoader.BLOCK_SESSION.get(domain));
            }
            else {
                blockSession = 0;
            }

            defaultSession = BLOCK_DEFAULTSESSION;

            ArrayList userList = dElement;
            recvCount = userList.size();
            //System.out.println("");
            //System.out.println("### KYH 111### Passage.java ### : recvCount : " + recvCount);
            //System.out.println("");
            
            unitEntireAccounts = unitEntireAccounts + recvCount; // Unit전체 대상자를 구함
            //System.out.println("### KYH 222### Passage.java ### unitEntireAccounts : " +unitEntireAccounts);
            //System.out.println("");
            

            SendTo uElement;

            if ( (blockSession == 0) || (blockSession > recvCount)) {
                int index = 0;
                int dmCount = ( (recvCount / defaultSession) + 1);
                for (int j = 0; j < dmCount; j++) {
                    if ( (j + 1) == dmCount) {
                        defaultSession = (recvCount % defaultSession);
                    }
                    dMessage = new DomainMessage();
                    dMessage.setDomain(domain);
                    dMessage.setMXRecord(mxRecord);
                    for (int k = 0; k < defaultSession; k++) {
                        uMessage = new UserMessage();
                        uElement = (SendTo) userList.get(index++);
                        rowID = uElement.rowID;
                        antID = uElement.id;
                        antName = uElement.name.trim();
                        emailAddr = uElement.email.trim();
                        enckey = uElement.encKey.trim();
                        bizkey = uElement.bizKey.trim();
                        if (isRecovery) {
                            if (recoveryAccount.contains(String.valueOf(rowID))) {
                                emailError = emailChecker(emailAddr); //true: 이상없슴, false: 잘못된 이메일
                                uMessage.setRowID(rowID);
                                uMessage.setAccountID(antID);
                                uMessage.setEmailAddr(emailAddr);
                                uMessage.setAccountName(antName);
                                uMessage.setEnckey(enckey);
                                uMessage.setBizkey(bizkey);
                                recoveryEntireAccount++;
                                isEmpty = false;
                                if (emailError) {
                                    dMessage.addRecvList(uMessage);
                                }
                                else {
                                    emailErrorAccounts++;
                                    tempbundle.addEmailError(uMessage);
                                    occurEmailError = true;
                                }
                            }
                            else {
                                isEmpty = true;
                            }
                        }
                        else {
                            emailError = emailChecker(emailAddr); //true: 이상없음, false: 잘못된 이메일
                            uMessage.setRowID(rowID);
                            uMessage.setAccountID(antID);
                            uMessage.setEmailAddr(emailAddr);
                            uMessage.setAccountName(antName);
                            uMessage.setEnckey(enckey);
                            uMessage.setBizkey(bizkey);
                            isEmpty = false;
                            if (emailError) {
                                dMessage.addRecvList(uMessage);
                            }
                            else {
                                emailErrorAccounts++;
                                tempbundle.addEmailError(uMessage);
                                occurEmailError = true;
                            }
                        }
                    }
                    if (!isEmpty) {
                        tempbundle.addMessage(dMessage);
                    }
                }
            }
            else {
                int index = 0;
                int dmCount = ( (recvCount / blockSession) + 1);
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
                        antID = uElement.id;
                        antName = uElement.name.trim();
                        emailAddr = uElement.email.trim();
                        enckey = uElement.encKey.trim();
                        bizkey = uElement.bizKey.trim();
                        
                        
                        
                        if (isRecovery) {
                            if (recoveryAccount.contains(String.valueOf(rowID))) {
                                emailError = emailChecker(emailAddr);
                                uMessage.setRowID(rowID);
                                uMessage.setAccountID(antID);
                                uMessage.setEmailAddr(emailAddr);
                                uMessage.setAccountName(antName);
                                uMessage.setEnckey(enckey);
                                uMessage.setBizkey(bizkey);
                                recoveryEntireAccount++;
                                isEmpty = false;
                                if (emailError) {
                                    dMessage.addRecvList(uMessage);
                                }
                                else {
                                    emailErrorAccounts++;
                                    tempbundle.addEmailError(uMessage);
                                    occurEmailError = true;
                                }
                            }
                            else {
                                isEmpty = true;
                            }
                        }
                        else {
                            emailError = emailChecker(emailAddr);
                            uMessage.setRowID(rowID);
                            uMessage.setAccountID(antID);
                            uMessage.setEmailAddr(emailAddr);
                            uMessage.setAccountName(antName);
                            uMessage.setEnckey(enckey);
                            uMessage.setBizkey(bizkey);
                            isEmpty = false;
                            if (emailError) {
                                dMessage.addRecvList(uMessage);
                            }
                            else {
                                emailErrorAccounts++;
                                tempbundle.addEmailError(uMessage);
                                occurEmailError = true;
                            }
                        }
                    }
                    if (!isEmpty) {
                        tempbundle.addMessage(dMessage);
                    }
                }
            }

        }
        System.out.println("\n------<" + unitName + " Finish Making  DMBundle  >------ ");
        return tempbundle;
    }

    /**
     * 이메일의 문법 검사
     * @param emailAddr
     * @return true : 이메일 이상 없음
     * @return false : 잘못된 이메일
     */
    public static boolean emailChecker(String emailAddr) {
        StringTokenizer st = new StringTokenizer(emailAddr, "@");
        int count = st.countTokens();
        if (count == 2) {
            String emailID = st.nextToken();
            String domain = st.nextToken();
            if ( (emailID.indexOf(" ") == -1) && (domain.indexOf(" ") == -1)) {
                return true;
            }
            else {
                return false;
            }
        }
        else {
            return false;
        }
    }

    public class ControlTower
    {
        private StringBuffer sb; // 임시 스트링 버퍼
        private byte[] appendedFile; // 첨부 파일
        private SimpleDateFormat fmt; // 로그 날짜 포맷

        private CountManager cManager; // 발송 카운터를 관리하는 클래스
        private DomainMessage dm; // 도메인 메시지 객체
        private DMSendUnit DMSU; // 발송 Thread로 넘겨줄 UNIT 객체;
        private Vector mxRecord;

        private String currentDomain; // 현재 도메인
        private String beforeDomain = ""; // 도메인별로 분류된 객체를 발송할때 이전 발송한 도메인과 비교하기위한 도메인
        private String sendLogPath; // 발송 로그 경로
        private String unitLogPath; // UNIT 로그 경로
        private String appendedFilePath; // 첨부 파일 경로
        private int sendNo = 0; // 발송 시작 숫자

        /**
         * Bundle을 도메인별 객체별로 분리 하여 순차적으로 발송 Thread를 실행한다.
         * @param collect : DMBundle 객체
         */
        public ControlTower(DMBundle collect) {
            bundle = collect;
            fmt = new SimpleDateFormat("yyyyMMddHHmm", Locale.US);

            // 로그 파일 경로(sendlog, unitlog)
            sb = new StringBuffer();
            sendLogPath = sb.append(ConfigLoader.TRANSFER_ROOT_DIR).append(ConfigLoader.TRANSFER_REPOSITORY_DIR).append("sendlog/").toString();
            sb = new StringBuffer();
            unitLogPath = sb.append(ConfigLoader.TRANSFER_ROOT_DIR).append(ConfigLoader.TRANSFER_REPOSITORY_DIR).append("unitlog/").toString();

            if (bundle.appendedFile) {
                sb = new StringBuffer();
                appendedFilePath = sb.append(ConfigLoader.TRANSFER_ROOT_DIR).append(ConfigLoader.TRANSFER_REPOSITORY_DIR).append("attach")
                    .append(File.separator).append(bundle.unitName).toString();
                appendedFile = FileManager.loadAttachFile(appendedFilePath);
            }
            else {
                appendedFile = null;
            }

            try {
                sb = new StringBuffer();
                File logFile = new File(sb.append(sendLogPath).append(bundle.unitName).toString());
                sb = new StringBuffer();
                File unitFile = new File(sb.append(unitLogPath).append(bundle.unitName).toString());
                /*
                 * 시스템의 내부 문제로 로그 파일이 둘중 한개만 생성 되었을
                 * 경우를 대비하여 두 파일의 존재 여부를 차례대로 검사 한다.
                 */
                if (logFile.exists()) {
                    if (unitFile.exists()) {
                        access = new SendLogFileAccess(logFile, unitFile);
                    }
                    else {
                        unitFile.createNewFile();
                        access = new SendLogFileAccess(logFile, unitFile);
                    }
                }
                else {
                    logFile.createNewFile();
                    if (unitFile.exists()) {
                        access = new SendLogFileAccess(logFile, unitFile);
                    }
                    else {
                        unitFile.createNewFile();
                        access = new SendLogFileAccess(logFile, unitFile);
                    }
                }
            }
            catch (Exception e) {
                LogWriter.writeException("ControlTower", "ConfigLoader()", " 로그 파일 검색 루틴", e);
            }

            // 카운터 객체 생성
            cManager = new CountManager();
            if (isRecovery) {
                cManager.setTotalCount(recoveryEntireAccount); // Counter Manager의 전체 대상자를 초기화 한다.(1개의 UNIT에 대해 고정)
            }
            else {
                cManager.setTotalCount(unitEntireAccounts); // Counter Manager의 전체 대상자를 초기화 한다.(1개의 UNIT에 대해 고정)
            }

            // sendNo 결정
            if (bundle.sendNo > 0) {
                sendNo = bundle.sendNo + bundle.retryCount;
            }

            // Email 문법 오류가있는  검사후 로그를 기록한다.(occurEmailError : true(이메일 에러 존재))
            // 이메일 문법 오류 해당 로그를 기록
            Vector emailErrorList = new Vector();
            emailErrorList = bundle.getErrorEmail();
            Iterator emGroup = emailErrorList.iterator();

            if (emailErrorAccounts > 0) {
                cManager.increaseEndCount(emailErrorAccounts);
            }
            if (occurEmailError) {
                while (emGroup.hasNext()) {
                    UserMessage um = (UserMessage) emGroup.next();
                    int rowID = um.getRowID();
                    String ID = um.geAccountID();
                    String name = um.getAccountName();
                    String email = um.getEmailAddr();
                    String bizkey = um.getBizkey();
                    String[] temp = email.split("@");
                    //String domain = temp[temp.length - 1];
                    for (int k = sendNo; k <= bundle.retryCount; k++) {
                        SendLogRecord rd = new SendLogRecord(bundle.sendTest, rowID, bundle.deptNo, bundle.userNo, bundle.campTyNo, bundle.campNo, bundle.taskNo, bundle.subTaskNo,
                            ID, k, email, name, fmt.format(new java.util.Date()), ErrorCode.STR_EMAILEROR, ErrorCode.SOFTERROR, ErrorCode.STP_SYNTAX, ErrorCode.STS_WRONGEMAIL, "006 EMAIl SYNTAX ERROR", bundle.target_grp_ty, bizkey);
                        access.writeSendLogRecord(rd);
                    }
                }
                occurEmailError = false;
            }

            Vector dmList = new Vector();
            dmList = bundle.getMessage();
            int i = 0;
            // 발송 쓰레드  관리 그룹
            agentWatcher = new AgentControler(); // 전체 쓰레드 관리
            unitGuard = new UnitThreadCounter(); // 현재 유닛의 발송 쓰레드

            while (dmList.size() > 0) {
                if (agentWatcher.getSize() < AGENT_LIMIT) {
                    i++;
                    dm = (DomainMessage) dmList.remove(0);
                    currentDomain = dm.domain;
                    String agentNM = bundle.unitName + "_" + currentDomain.trim() + "_" + i;    //364-1^1_enders.co.kr_1
                    agentWatcher.addAgentGroup(agentNM);
                    if (!currentDomain.equals(beforeDomain)) {
                        beforeDomain = currentDomain;
                        mxRecord = dm.MXRecord;
                    }

                    unitGuard.addUnitFactor(agentNM);
                    DMSU = new DMSendUnit();
                    DMSU.setDeptNo(bundle.deptNo);
                    DMSU.setUserNo(bundle.userNo);
                    DMSU.setCampTyNo(bundle.campTyNo);
                    DMSU.setCampNo(bundle.campNo);
                    DMSU.setTaskNo(bundle.taskNo);
                    DMSU.setSubTaskNo(bundle.subTaskNo);
                    DMSU.setMessage(dm);
                    DMSU.setSendLogFileAccess(access);
                    DMSU.setCountManager(cManager);
                    DMSU.setAppendedFile(appendedFile);
                    DMSU.setIsAppended(bundle.appendedFile);
                    DMSU.setSocketTimeOut(bundle.socketTimeOut);
                    DMSU.setSendMode(bundle.sendMode);
                    DMSU.setSendTest(bundle.sendTest);
                    DMSU.setmxRecord(mxRecord);
                    DMSU.setSender(bundle.senderEmail);
                    DMSU.setUnitName(bundle.unitName);
                    DMSU.setDateFormatter(fmt);
                    DMSU.setSendNo(sendNo);
                    DMSU.setRetryCount(bundle.retryCount);
                    DMSU.setAgentName(agentNM);
                    DMSU.setTargetGrpTY(bundle.target_grp_ty);
                    DMSender send = new DMSender(DMSU, agentWatcher, unitGuard);
                    send.setName(agentNM);
                    send.start();
                }
                else {
                    try {
                        Thread.sleep(500);
                    }
                    catch (Exception e) {}
                }
            } // End while loop
            System.out.println("An unit is done ");
        }
    } // End - ControlTower Class

    private Vector recoveryMembers(String unitName) {
        ConfigLoader.load();
        Vector rList = new Vector();
        RandomAccessFile unitFile;
        int nIndex = 0;
        int unitEntireCount = 0;
        byte[] total = new byte[2];

        File unitLogFile = new File(new StringBuffer(ConfigLoader.TRANSFER_ROOT_DIR)
                                    .append(ConfigLoader.TRANSFER_REPOSITORY_DIR)
                                    .append("unitlog/").append(unitName).toString());
        try {
            unitFile = new RandomAccessFile(unitLogFile, "r");
            unitFile.seek(36);
            unitFile.read(total, 0, 2);
            unitEntireCount = bytes2Short(total, 0);
            byte[] data = new byte[unitEntireCount];
            unitFile.seek(64);
            unitFile.read(data, 0, unitEntireCount);
            while (nIndex < unitEntireCount) {
                int code = (int) data[nIndex];
                if (code == 0) {
                    try {
                        rList.addElement(String.valueOf(nIndex));
                    }
                    catch (Exception e) {
                        LogWriter.writeException("Passage", "recoveryMembers()", "복구 대상을 리스트에 담는데 실패했습니다.", e);
                    }
                }
                nIndex++;
            }
            unitFile.close();
        }
        catch (Exception e) {
            LogWriter.writeException("Passage", "recoveryMembers", "복구 상자를 만드는데 실패 했습니다.", e);
        }
        System.out.println("Completed a recovery list. Entire unit accounts : " + unitEntireCount + " , restored accounts : " + rList.size());
        return rList;
    }

    private static short bytes2Short(byte[] buf, int offset) {
        int i = offset;
        return (short) ( ( (short) buf[i++] << 8) | ( (short) (buf[i] & 0xFF) << 0));
    }
}
