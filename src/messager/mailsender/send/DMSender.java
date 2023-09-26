package messager.mailsender.send;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

import messager.mailsender.code.*;
import messager.mailsender.config.*;
import messager.mailsender.message.*;
import messager.mailsender.send.retry.*;
import messager.mailsender.sendlog.*;
import messager.mailsender.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DMSender
    extends Thread
{

	private static final Logger LOGGER = LogManager.getLogger(DMSender.class.getName());
	 	 
    /*************** ConfigLoader Values ****************/
    private String SEND_DOMAIN; // 발송 도메인

    /**************** Inner Function Values **************/
    private File logFile; // 로그 파일 객체
    private Vector mxRecord = new Vector(); // 도메인의 mx 레코드의 IP 집합
    private Vector retryList = new Vector(); // 재발송 대상자
    private byte[] attachData;
    private SimpleDateFormat fmt;

    /****************** Class Values ********************/
    private DomainMessage dm; // bundle내의 도메인별 객체
    private ConnectManager socket; // 도메인에  생성할 소켓
    private UserMessage element; // 도메인별 발송 대상자 객체
    private PatternUnit pUnit; // 서버로 부터 받은 응답메지를 패턴 검사 모듈로 보내기 위한 요소들을 객체화.
    private SendLogFileAccess accessor; // 로그 파일을 기록하는 클래스
    private SendLogRecord record; // 로그 파일 포맷 객체
    private CountManager cManager; // 발송 카운터를 관리하는 클래스
    private PatternSearch filter = new PatternSearch();

    /***************** Common Values *******************/
    private boolean sendMode; // 발송 형태
    private boolean sendTest; // 테스트 발송 여부
    private boolean isAppended;
    private String domain; // Passage로 부터 받은 도메인 객체의 해당 도메인
    private String senderEmail; // 보내는 이의 이메일 주소
    private String responseMsg; // 메일 서버로 부터 받은 응답메시지
    private String currentAgent; // 현재 실행중인 Thread Name
    private String accountID; // 대상자 아이디
    private String email; // 대상자 이메일
    private String name; // 대상자 이름
    private String unitName; // 유닛 이름
    private String responseDigit; // 메일 서버로 부터 받은 응답코드
    private String resultCode; // 발송 결과 코드
    private int sCount = 0; // 성공 수
    private int fCount = 0; // 실패 수 -  로그 파일에는 기록하지 않지만 모니터링시 실패 수를 확인하기 위함
    private int socketTimeOut; // 소켓 연결 시간
    private int taskNo; // 업무 번호
    private int subTaskNo; // 서브 업무 번호
    private int rowID; // 일련번호
    private int deptNo; // 부서번호
    private String userNo; // 사용자번호
    private String campTyNo; // 캠페인 타입 번호
    private int campNo; // 캠페인 번호
    private int sendNo;
    private int retryCount; // 재발송 횟수
    private AgentControler agentWatcher;
    private UnitThreadCounter unitGuard;
    private String AgentNM;
    private String target_grp_ty;
    private String bizkey;

    /**
     * DMSender Constructor
     * @param message : 발송할 도메인(대상자 포함)
     * @param sender : 보내는이
     * @param access : 로그 파일 객체
     * @param msg : 발송 Message ID
     * @param unit :  발송 Unit ID
     * @param cmanager : 발송 카운터 객체
     * @param acontrol : 발송 AGENT CONTROLER
     * @param ipVec : 해당 도메인의 IP LIST
     */
    public DMSender(DMSendUnit DMSU, AgentControler aControler, UnitThreadCounter uc) {
        SEND_DOMAIN = ConfigLoader.SEND_DOMAIN;
        dm = DMSU.message;
        senderEmail = DMSU.sender; // Send Log
        accessor = DMSU.access; // Count
        cManager = DMSU.cManager; // init()
        mxRecord = DMSU.mxRecord;
        attachData = DMSU.appendedFile;
        socketTimeOut = DMSU.socketTimeOut;
        sendMode = DMSU.sendMode;
        sendTest = DMSU.sendTest;
        isAppended = DMSU.isAppended;
        deptNo = DMSU.deptNo;
        userNo = DMSU.userNo;
        campTyNo = DMSU.campTyNo;
        campNo = DMSU.campNo;
        taskNo = DMSU.taskNo;
        subTaskNo = DMSU.subTaskNo;
        unitName = DMSU.unitName;
        sendNo = DMSU.sendNo;
        retryCount = DMSU.retryCount;
        fmt = DMSU.fmt; // Date Format
        AgentNM = DMSU.agentNM;
        target_grp_ty = DMSU.target_grp_ty;
        agentWatcher = aControler;
        unitGuard = uc;
    }

    /**
     * Excuting Agent Thread
     */
    public void run() {
        if (sendMail()) {
            exitThread();
        }
        else {
            LogWriter.writeError("DMSender", "run()", "sendMail()에서 불완전한 종료 ", " ");
            closeChannel();
            exitThread();
        }

    }

    /**
     * 소켓 초기화
     * ipVec의 IP를 한개씩 꺼내와 소켓을 오픈한다.
     * 소켓 오픈에 실패하면 그다음 IP로 재차 시도한다.
     */
    private ConnectManager makeConnection() {
        ConnectManager sock = null;
        int mxLength = 0;
        if (mxRecord != null) {
            mxLength = mxRecord.size();
        }
        String mxValue = "";
        if (mxLength > 0) {
            for (int i = 0; i < mxLength; i++) {
                mxValue = (String) mxRecord.elementAt(i);

                sock = new ConnectManager(mxValue, 25);
                if (sock.initConnect(socketTimeOut)) {
                    return sock;
                }
                else {
                    sock.closeConnect();
                }
            }
        }
        else {
            //도메인에 대한 정보가 없음
            sock = new ConnectManager(mxValue, 25);
            sock.Connect_ErrorCode = "unhost";
            sock.initConnect_errorMsg = "MX RECORD NOT EXIST.";
        }
        return sock;
    }

    /**
     *  발송 시작전 해당 메일서버에 접속후 HELO 단계만 진행하고 sendSMTP()를 실행 한다.
     *  > helo sender-domain
     *  이후에는 HELO 단계가 없이 RSET후 다음 대상자를 넘어간다.
     * @throws Exception
     */
    private boolean sendMail() {
        Vector receivers = new Vector();

        receivers = dm.recvList; // UserMessage 들의 Vector
        socket = makeConnection();
        if (socket.isConnected()) {
            if (socket.cmdHelo(SEND_DOMAIN)) {
                sendSMTP(receivers);
            }
            else {
                responseMsg = socket.getResponseMessage();
                responseDigit = responseDigit(socket.getResponseCode());
                pUnit = new PatternUnit("002", responseDigit, responseMsg);
                resultCode = filter.filterMessage(pUnit);
                writeHeloErrorLog(receivers, responseMsg, responseDigit, resultCode);
            }
        }
        else {
            writeSocketErrorLog(receivers, socket.getConnectErrorCode(),
                                socket.getErrorMessage());
            socket.initialization();
        }

        /*
         * 한개의 도메인의 발송 시도가 끝난 시점에서 카운트 갱신한다.
         * sCount : 발송성공
         * fCount : 발송 실패
         * EndCount : 현재 유닛에서 발송된 수
         */
        boolean endFlag = false;

        synchronized (cManager) {
        	
        	//System.out.println("");
        	//System.out.println("");
        	//System.out.println("### KYH 111###  DMSender.java ####");
        	//System.out.println("unitGuard.getSize() : "+unitGuard.getSize());
        	//System.out.println("");
        	
            cManager.increaseSCount(sCount);
            cManager.increaseEndCount(fCount + sCount); // 실패된 대상자를 현재 발송된 수에 더함.
            agentWatcher.removeAgentGroup(AgentNM);
            unitGuard.delUnitFactor(AgentNM);
            
        	//System.out.println("### KYH 222###  DMSender.java ####");
        	//System.out.println("성공수1 : "+sCount);
        	//System.out.println(new StringBuffer().append("성공수2 : ").append(cManager.getSCount()));
        	//System.out.println("실패수1 : "+fCount);
        	//System.out.println(new StringBuffer().append("실패수2 : ").append(cManager.getEndCount() - cManager.getSCount()));
        	//int q =fCount + sCount;
        	//System.out.println("AgentNM(도메인) : "+AgentNM);
        	//System.out.println("unitGuard.getSize() : "+unitGuard.getSize());
        	//System.out.println("");
            
            //System.out.println("###333###  DMSender.java ####");
            //System.out.println("cManager.getEndCount() : " +cManager.getEndCount());
            //System.out.println("cManager.getTotalCount() : " +cManager.getTotalCount());
            //System.out.println("unitGuard.getSize() : "+unitGuard.getSize());
            //System.out.println("");
            
            //if ( (unitGuard.getSize() < 1) || (cManager.getEndCount() == cManager.getTotalCount())) {
            if ((cManager.getEndCount() == cManager.getTotalCount())) {
            
//            System.out.println("###444-true###  DMSender.java ####");
//            System.out.println("cManager.getEndCount() : " +cManager.getEndCount());
//            System.out.println("cManager.getTotalCount() : " +cManager.getTotalCount());
//            System.out.println("성공수 : " +cManager.getSCount());
//            System.out.println(new StringBuffer().append("실패수 : ").append(cManager.getEndCount() - cManager.getSCount()));
//            System.out.println("unitGuard.getSize() : "+unitGuard.getSize());
//            System.out.println("");
        	
            	endFlag = true;
            }
            else {
//            System.out.println("###444-false###  DMSender.java ####");
//            System.out.println("cManager.getEndCount() : " +cManager.getEndCount());
//            System.out.println("cManager.getTotalCount() : " +cManager.getTotalCount());
//            System.out.println("unitGuard.getSize() : "+unitGuard.getSize());
//            System.out.println("");
        	
                endFlag = false;
            }
        }
        /*
         * < 발 송 완 료 >
         * 발송된 이메일 수가 유닛의 총계와 같으면 Thread를 닫고
         * 해당 유닛 파일을 지운다.
         * UNIT의 모든 Agent 가 완료 -> Close Log File -> Delete Obj File(2개)
         * 발송이 끝난 유닛을 RetryCenter로 보낸다.
         * 발송 결과를 읽어 재발송 대상자를 생성한다.
         * 재발송 대상자에 해당하는 회원이 없을 경우 SKIP한다.
         */
        if (endFlag) {
            printCount();

            if (retryCount > 0) { // 재발송 횟수가 0이상인가 검사
                retryList = extractMembers(unitName);

                if (retryList.size() > 0) {
                    RetryCenter rc = new RetryCenter(unitName, accessor, retryList, attachData);
                    rc.start();
                }
                else {
                    if (!accessor.writeUnitLogEndTime(System.currentTimeMillis())) {
                        LogWriter.writeError("DMSender", "sendMail()", "유닛 로그 파일을 작성하는데 문제가 있습니다.", "최종시간 기록");
                    }
                    //System.out.println(taskNo + "_" + subTaskNo + " Non Exsist Retry list");
                    LOGGER.info(taskNo + "_" + subTaskNo + " Non Exsist Retry list");
                    
                    accessor.close();
//                    System.out.println("###3### unitName :" +unitName);
//                    System.out.println("###4### isAppended :" +isAppended);
                    FileManager.deleteUnitFiles(unitName, isAppended);

                }
            }
            else {
                if (!accessor.writeUnitLogEndTime(System.currentTimeMillis())) {
                    LogWriter.writeError("DMSender", "sendMail()", "유닛 로그 파일을 작성하는데 문제가 있습니다.", "최종시간 기록");
                }
                accessor.close();
//                System.out.println("###5### unitName :" +unitName);
//                System.out.println("###6### isAppended :" +isAppended);
                FileManager.deleteUnitFiles(unitName, isAppended);
            }
        }
        else {
            /***************************************
                  System.out.println(new StringBuffer()
                      .append("---------------------------------------------").append("\r\n")
                      .append("\tAmount of current Active Threads " + agentWatcher.getSize()).append("\r\n")
                      .append("\tNEO_TASK : ").append(unitName).append("\r\n")
                      .append("\t").append(AgentNM).append(" is finished").append("\r\n")
                      .append("\tTotal Count : ").append(cManager.getTotalCount()).append("\r\n")
                      .append("\tSuccess Count : ").append(cManager.getSCount()).append("\r\n")
                      .append("\tFail Count : ").append(cManager.getEndCount() -
                      cManager.getSCount()).append("\r\n")
                      .append("---------------------------------------------").append("\r\n")
                      .toString()
                      );
             ***************************************/
        }

        if (!closeChannel()) { // 활성화된 현재의 소켓을 종료 한다.
            LogWriter.writeError("DMSender", "sendMail()", "소켓을 닫을수 없습니다.", "closeChannel()함수 체크 ");
            return false;
        }
        return true;
    }

    /**
     * 발송 : RSET -> MAIL FROM -> RCPT TO -> DATA(contents 전송, 첨부파일 전송)
     * -> DOT의 순으로 진행
     * @see cmdRset() : 연결을 초기화 한다.
     * @see cmdMailFrom() : 보내는 이를 전송
     * @see cmdRcptTo() : 받는이 전송
     * @see cmdData() : 메일 내용 전송
     * @see cmdDataTransferComplete() : 연결을 종료 한다.
     * @param receivers : 발송 대상자 리스트
     * @value byte [] emlData : 실제 발송 Content File
     *
     */
    private void sendSMTP(Vector receivers) {
        int recvCount = receivers.size();
        byte[] emlData;
        String receiver = "";
        try {
	
            for (int i = 0; i < recvCount; i++) {
            	            	
                element = (UserMessage) receivers.remove(0);
                rowID = element.getRowID();
                receiver = element.getEmailAddr();
                //System.out.println("###111### KYH 수신자이메일주소  : "+receiver + ",  recvCount : " +recvCount + ", for문 : " +i);
                if (socket.cmdRset()) {
                    if (socket.cmdMailFrom(senderEmail)) {
                        if (socket.cmdRcptTo(receiver)) {
                            if (sendMode) { // 가상발송 필터 (true  -  실 발송 ,  false - 가상발송)
                                if (socket.cmdData()) {
                                    try {
                                        StringBuffer sb = new StringBuffer();
                                        String srcFile = new StringBuffer(ConfigLoader.TRANSFER_ROOT_DIR).append(
                                            ConfigLoader.TRANSFER_REPOSITORY_DIR)
                                            .append("content").append(File.separator)
                                            .append(unitName).append(File.separator)
                                            .append(rowID).append(".mcf").toString();

                                        //System.out.println("###222### KYH 발송 가능한 MCF만 조회 ###  :"+srcFile);
                                        emlData = FileManager.loadEml(srcFile);
                                        /**
                                         * 송진우 - 발송할 데이터가 없을 때는 에러로 처리
                                         */
                                        if (emlData.length == 0) {
                                            //System.out.println("data:" + emlData.length);
                                            throw new FileNotFoundException("발송데이터 로드시 에러");
                                        }

                                        socket.sendEmailData(emlData);
                                        // 첨부 파일이 있는 경우 데이터를 전송후 재전송
                                        if (isAppended) {
                                            socket.sendEmailData(attachData);
                                        }

                                        // DOT
                                        if (socket.cmdDataTransferComplete()) {
                                            responseMsg = socket.getResponseMessage();
                                            responseDigit = responseDigit(socket.getResponseCode());
                                            writeOneRecord(element, "000", "002", "006", responseDigit,
                                                fmt.format(new java.util.Date()), responseMsg);
                                            /**
                                             * 송진우 2004.11.22
                                             * 테스트발송의 경우 발송데이터를 공유하므로 바로 지우면 않됨.
                                             */
                                            if (!sendTest) {
                                            	//System.out.println("###333### KYH 실제 발송 (.)### srcFile :" +srcFile + ",  rowID(csv행번호) : "+ rowID);
                                                FileManager.deleteEmlFiles(srcFile, rowID);
                                            }
                                        }
                                        else {
                                            // pCode = 6;
                                            responseMsg = socket.getResponseMessage();
                                            responseDigit = responseDigit(socket.getResponseCode());
                                            pUnit = new PatternUnit("006", responseDigit, responseMsg);
                                            resultCode = filter.filterMessage(pUnit);
                                            writeOneRecord(element, resultCode, ErrorCode.HARDERROR, ErrorCode.HDP_DOT,
                                                responseDigit, fmt.format(new java.util.Date()), responseMsg);
                                        }
                                    }
                                    catch (Exception e) {
                                    	LOGGER.error(e);
                                        if (e instanceof FileNotFoundException) {
                                            writeOneRecord(element, "009", ErrorCode.HARDERROR, ErrorCode.HDP_DOT, "999",
                                                fmt.format(new java.util.Date()), "File Not Found Exception");
                                            LogWriter.writeException("DMSender", "sendSMTP", "eml 파일을 읽는데 실패", e);
                                        }
                                        else if (e instanceof SocketException) {
                                            writeOneRecord(element, "009", ErrorCode.HARDERROR, ErrorCode.HDP_DOT, "999",
                                                fmt.format(new java.util.Date()), "Socket Exception");
                                            LogWriter.writeException("DMSender", "sendSMTP", "SocketException 에러 발생", e);
                                        }
                                        else {
                                            writeOneRecord(element, "009", ErrorCode.HARDERROR, ErrorCode.HDP_DOT, "999",
                                                fmt.format(new java.util.Date()), e.getMessage() + ErrorCode.HDR_NETWORK);
                                            LogWriter.writeException("DMSender", "sendSMTP", "기타 에러", e);
                                        }
                                    }
                                }
                                else { // pCode = 5;
                                    responseMsg = socket.getResponseMessage();
                                    responseDigit = responseDigit(socket.getResponseCode());
                                    pUnit = new PatternUnit("005", responseDigit, responseMsg);
                                    resultCode = filter.filterMessage(pUnit);
                                    writeOneRecord(element, resultCode, ErrorCode.HARDERROR, ErrorCode.HDP_DATA,
                                        responseDigit, fmt.format(new java.util.Date()), responseMsg);
                                }
                            }
                            else { // 가상 발송
                                responseMsg = socket.getResponseMessage();
                                responseDigit = responseDigit(socket.getResponseCode());
                                writeOneRecord(element, "000", "002", "004", responseDigit,
                                               fmt.format(new java.util.Date()), responseMsg);
                            }
                        }
                        else { // RCPT TO
                            responseMsg = socket.getResponseMessage();
                            responseDigit = responseDigit(socket.getResponseCode());
                            pUnit = new PatternUnit("004", responseDigit, responseMsg);
                            resultCode = filter.filterMessage(pUnit);
                            writeOneRecord(element, resultCode, ErrorCode.HARDERROR, ErrorCode.HDP_RCPTTO,
                                           responseDigit, fmt.format(new java.util.Date()), responseMsg);
                        }
                    }
                    else { // MAIL FROM
                        responseMsg = socket.getResponseMessage();
                        responseDigit = responseDigit(socket.getResponseCode());
                        pUnit = new PatternUnit("003", responseDigit, responseMsg);
                        resultCode = filter.filterMessage(pUnit);
                        writeOneRecord(element, resultCode, ErrorCode.HARDERROR, ErrorCode.HDP_MAILFROM,
                                       responseDigit, fmt.format(new java.util.Date()), responseMsg);
                    }
                }
                else { // RSET
                    responseMsg = socket.getResponseMessage();
                    responseDigit = responseDigit(socket.getResponseCode());
                    pUnit = new PatternUnit("007", responseDigit, responseMsg);
                    resultCode = filter.filterMessage(pUnit);
                    writeOneRecord(element, resultCode, ErrorCode.HARDERROR, ErrorCode.HDP_RSET,
                                   responseDigit, fmt.format(new java.util.Date()), responseMsg);
                }
            } // End for loop
        }
        catch (Exception e) {
        	LOGGER.error(e);
            LogWriter.writeException("DMSender", "sendSMTP()", " ", e);
        }
    }

    private String responseDigit(String digit) {
        if (digit.equals("000")) {
            digit = "999";
        }
        return digit;
    }

    /**
     * 메일 발송 로그 기록
     * 현재 발송한 메일 사용자에 대한 로그를 기록 한다.
     * @param resultCode : PatternSearch클래스를 통해 리턴된 값
     * @param majorCode : 대분류 코드
     * @param pCode : 중분류 (오류 시점의 단계 표시)
     * @param responseDigit : 소분류(응답코드)
     * @param responseMessage : 응답메시지
     */
    private void writeOneRecord(UserMessage message,
                                String resultCode,
                                String majorCode,
                                String pCode,
                                String responseDigit,
                                String currentTime,
                                String responseMsg) {
//		if(!sendTest)
//		{
        rowID = message.getRowID();
        accountID = message.geAccountID();
        email = message.getEmailAddr();
        name = message.getAccountName();
        bizkey = message.getBizkey();
        
        if (responseMsg.trim().equals("")) {
            responseMsg = "NO MESSAGE";
        }

        record = new SendLogRecord(sendTest, rowID, deptNo, userNo, campTyNo, campNo, taskNo,
                                   subTaskNo, accountID, sendNo, email, name, currentTime, resultCode, "002", pCode,
                                   String.valueOf(responseDigit), responseMsg, target_grp_ty, bizkey);
        accessor.writeSendLogRecord(record);
        accessor.writeUnitLog(rowID, resultCode);

        // 성공 카운터 증가
        if (resultCode.equals("000")) {
            sCount++;
        }
        else {
            fCount++;
        }
    }

    /**
     * 메일 발송 로그 기록
     * Helo 단계에서 에러난 대상자는 대상자 전체를 같은 에러코드로 기록한다.
     * @param vec : 도메인 객체
     * @param responseMsg : Helo 단계에서 받은 응답 메시지
     * @param responseDigit : Helo 단계에서 받은 응답 코드
     * @param resultCode :  패턴 검사후의 발송 결과 코드
     * @param majorCode = 2 : 대분류 코드
     * @param pCode = 2 : 중분류 코드
     */
    private void writeHeloErrorLog(Vector vec, String responseMsg, String responseDigit,
                                   String resultCode) {
        int vecSize = vec.size();
        int rowID;
        if (responseMsg.trim().equals("")) {
            responseMsg = "NO MESSAGE";
        }
        if (responseDigit.equals("999")) {
            responseMsg = "999 Network Error(" + responseMsg + ")";
        }
        for (int i = 0; i < vecSize; i++) {
            element = (UserMessage) vec.remove(0);
            accountID = element.geAccountID();
            email = element.getEmailAddr();
            name = element.getAccountName();
            bizkey = element.getBizkey();
            rowID = element.getRowID();
            // HELO 단계에서는 발송결과 코드가 5,6,7밖에 없으므로 5,6일때만 체크 한다.
            // 발송결과 4,5일때만 재발송 그룹이다.
            if (!resultCode.equals("004") && !resultCode.equals("005") && !resultCode.equals("006")) {
                record = new SendLogRecord(sendTest, rowID, deptNo, userNo, campTyNo, campNo, taskNo,
                                           subTaskNo, accountID, sendNo, email, name, fmt.format(new java.util.Date()), resultCode,
                                           ErrorCode.HARDERROR, ErrorCode.HDP_HELO, String.valueOf(responseDigit), responseMsg, target_grp_ty, bizkey);
                accessor.writeSendLogRecord(record);
            }
            else {
                try {
                    record = new SendLogRecord(sendTest, rowID, deptNo, userNo, campTyNo, campNo, taskNo,
                                               subTaskNo, accountID, sendNo, email, name, fmt.format(new java.util.Date()),
                                               resultCode, ErrorCode.HARDERROR, ErrorCode.HDP_HELO, String.valueOf(responseDigit),
                                               responseMsg, target_grp_ty, bizkey);
                    if (record == null) {
                        System.err.println("record null");
                    }
                    if (accessor == null) {
                        System.err.println("accessor null");

                    }
                    accessor.writeSendLogRecord(record);
                }
                catch (Exception e) {
                	LOGGER.error(e);
                    //e.printStackTrace();
                }
            }
            accessor.writeUnitLog(rowID, resultCode);
        }
        fCount = fCount + vecSize;
    }

    /**
     * 해당 도메인에 소켓 연결을 맺을때 에러가 발생했을때 DMSender class로 넘어가지 않고
     * 로그를 기록 한다..
     * @param dm : DomainMessage  객체
     * @param access : 로그 파일 객체
     * @param cManager : 카운터 객체
     */
    private void writeSocketErrorLog(Vector recV, String sCode, String rMsg) {

        int vecSize = recV.size();
        String pCode = "006";
        UserMessage message;
        resultCode = ErrorCode.STR_NETWORKERROR;
        String responseM;

        if (sCode.equals("unhost")) {
            pCode = "005";
            sCode = "001";
            resultCode = ErrorCode.STR_DOMAINERROR;
            responseM = "001 Network_UnknownHostException(" + rMsg + ")";
        }
        else if (sCode.equals(ErrorCode.STS_SocketException)) {
            responseM = "003 NetWork_SocketException(" + rMsg + ")";
        }
        else if (sCode.equals(ErrorCode.STS_BindException)) {
            responseM = "003 Network_BindException(" + rMsg + ")";
        }
        else if (sCode.equals(ErrorCode.STS_NoRouteToHostException)) {
            responseM = "002 Network_NoRouteToHostException(" + rMsg + ")";
        }
        else if (sCode.equals(ErrorCode.STS_ConnectException)) {
            responseM = "001 Network_ConnectException(" + rMsg + ")";
        }
        else if (sCode.equals(ErrorCode.STS_ProtocolException)) {
            responseM = "006 Network_ProtocolException(" + rMsg + ")";
        }
        else if (sCode.equals(ErrorCode.STS_MalformedURLException)) {
            responseM = "004 Network_MalformedURLException(" + rMsg + ")";
        }
        else if (sCode.equals(ErrorCode.STS_UnknownServiceException)) {
            responseM = "006 Network_UnknownServiceException(" + rMsg + ")";
        }
        else if (sCode.equals(ErrorCode.STS_SockTimeoutException)) {
            responseM = "005 Network_SockTimeoutException(" + rMsg + ")";
        }
        else if (sCode.equals(ErrorCode.STS_NetworkETC)) {
            responseM = "999 Network_ETC(" + rMsg + ")"; // 999
        }
        else {
            sCode = ErrorCode.STS_NetworkETC;
            responseM = "999 Network_ETC(" + rMsg + ")"; // 999
        }

        for (int i = 0; i < vecSize; i++) {
            message = (UserMessage) recV.remove(0);
            rowID = message.getRowID();
            accountID = message.geAccountID();
            email = message.getEmailAddr();
            name = message.getAccountName();
            bizkey = message.getBizkey();

//				 4 : 네트웍 에러 (결과 코드)	 3 : 도메인 없음 (결과 코드)
            if (!resultCode.equals("004") && !resultCode.equals("005") && !resultCode.equals("006")) {
                record = new SendLogRecord(sendTest, rowID, deptNo, userNo, campTyNo, campNo, taskNo,
                                           subTaskNo, accountID, sendNo, email, name, fmt.format(new java.util.Date()), resultCode,
                                           ErrorCode.SOFTERROR, pCode, sCode, responseM, target_grp_ty, bizkey);
                accessor.writeSendLogRecord(record);
            }
            else {
                record = new SendLogRecord(sendTest, rowID, deptNo, userNo, campTyNo, campNo, taskNo,
                                           subTaskNo, accountID, sendNo, email, name, fmt.format(new java.util.Date()), resultCode,
                                           ErrorCode.SOFTERROR, pCode, sCode, responseM, target_grp_ty, bizkey);
                accessor.writeSendLogRecord(record);
            }
            accessor.writeUnitLog(rowID, resultCode);
        }
        fCount = vecSize;
    }

    /**
     * 발송결과를 출력 한다.
     */
    private void printCount() {
//        System.out.println(new StringBuffer()
//                           .append("#############################################################").append("\r\n")
//                           .append("\t현재 쓰레드 그룹의 활성화된 쓰레드 수 : ").append(agentWatcher.getSize()).append("\r\n")
//                           .append("\tFinished Time : ").append(getTime()).append("\r\n")
//                           .append("\tNEO_TASK : ").append(unitName).append("\r\n")
//                           .append("\tTotal Count : ").append(cManager.getTotalCount()).append("\r\n")
//                           .append("\tSuccess Count : ").append(cManager.getSCount()).append("\r\n")
//                           .append("\tFail Count : ").append(cManager.getEndCount() -
//            cManager.getSCount()).append("\r\n")
//                           .append("#############################################################")
//                           .toString()
//                           );
        
        LOGGER.info(new StringBuffer()
                .append("#############################################################").append("\r\n")
                .append("\t현재 쓰레드 그룹의 활성화된 쓰레드 수 : ").append(agentWatcher.getSize()).append("\r\n")
                .append("\tFinished Time : ").append(getTime()).append("\r\n")
                .append("\tNEO_TASK : ").append(unitName).append("\r\n")
                .append("\tTotal Count : ").append(cManager.getTotalCount()).append("\r\n")
                .append("\tSuccess Count : ").append(cManager.getSCount()).append("\r\n")
                .append("\tFail Count : ").append(cManager.getEndCount() -
           cManager.getSCount()).append("\r\n")
                .append("#############################################################")
                .toString()
                );
    }
    

    

    /**
     *  발송 완료된 domainMessage에 대해 Thread를 종료한다.
     */
    private void exitThread() {
        Thread currentThread = DMSender.currentThread();
        currentThread.interrupt();
        currentThread = null;
    }

    /**
     * 발송 완료된 domainMessage에 대해 소켓을 닫는다.
     */
    private boolean closeChannel() {
        try {
            if (socket != null) {
                if (socket.cmdQuit()) {
                }
            }
        }
        catch (Exception e) {
        	LOGGER.error(e);
            LogWriter.writeException("DMSender", "closeChannel()", "소켓을 닫는데 실패 했습니다.", e);
            return false;
        }
        finally {
            try {
                if (socket != null) {
                    socket.closeConnect();
                }
            }
            catch (Exception e) {LOGGER.error(e);}
        }
        return true;
    }

    private static short bytes2Short(byte[] buf, int offset) {
        int i = offset;
        return (short) ( ( (short) buf[i++] << 8) | ( (short) (buf[i] & 0xFF) << 0));
    }

    public static String getTime() {
        try {
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy년 MM월 dd일 a HH시 mm분");
            return fmt.format(new Date());
        }
        catch (Exception e) {
        	LOGGER.error(e);
            return "Date Error";
        }
    }

    /**
     * 로그 파일을 읽어서 재발송 대상자 RowID를 추출
     * 재발송 대상자 조건 : 발송 결과 코드(resultCode = 004 or 005)
     * @param access : 로그 파일 객체
     * @values resultCode 4 : 네트웍 에러(SW)
     * @values resultCode 5 : 네트웍 에러(HD)
     * @values resultCode 6 : 트랜잭션 에러
     */
    private Vector extractMembers(String unitName) {
        Vector rList = new Vector();
        RandomAccessFile unitFile = null;
        int nIndex = 0;
        int unitEntireCount;
        byte[] total = new byte[2];
        try {
            unitFile = accessor.getUnitFile();
            unitFile.seek(36);
            unitFile.read(total, 0, 2);
            unitEntireCount = bytes2Short(total, 0);
            byte[] data = new byte[unitEntireCount];
            unitFile.seek(64);
            unitFile.read(data, 0, unitEntireCount);
            while (nIndex < unitEntireCount) {
                int code = (int) data[nIndex];
                if (code == 4) {
                    try {
                        rList.addElement(String.valueOf(nIndex));
                    }
                    catch (Exception e) {
                    	LOGGER.error(e);
                        LogWriter.writeException("DMSender", "extractMembers()", "재발송 대상을 리스트에 담는데 실패했습니다.", e);
                    }
                }
                nIndex++;
            }
        }
        catch (IOException e) {
        	LOGGER.error(e);
            LogWriter.writeException("DMSender", "extractMembers", "재발송 대상자를 만드는데 실패 했습니다.", e);
        }
        catch (NullPointerException e) {
        	LOGGER.error(e);
            LogWriter.writeException("DMSender", "extractMembers",
                                     "재발송 대상자가 없습니다.(" + unitName + ") " + rList.size(), e);
        }
        return rList;
    }
}
