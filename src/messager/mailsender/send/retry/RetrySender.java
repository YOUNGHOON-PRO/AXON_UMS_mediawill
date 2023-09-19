package messager.mailsender.send.retry;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

import messager.mailsender.code.*;
import messager.mailsender.config.*;
import messager.mailsender.message.*;
import messager.mailsender.send.*;
import messager.mailsender.sendlog.*;
import messager.mailsender.util.*;

public class RetrySender
    extends Thread
{
    /*************** ConfigLoader Values ****************/
    private int RETRY_WAIT_TIME; // 재발송 대기 시간
    private String SEND_DOMAIN;
    /**************** Inner Function Values **************/
    private InetAddress address; // 발송 IP
    private Vector mxRecord; // 해당 도메인의 메일 서버 IP LIST
    private SimpleDateFormat fmt;
    private byte[] attachData;

    /****************** Class Values ********************/
    private DomainMessage message; // 도메인 메시지 객체
    private SendLogFileAccess rAccessor; // 로그 파일 객체
    private ConnectManager socket; // 도메인에  생성할 소켓
    private PatternUnit pUnit; // 서버로 부터 받은 응답메지를 패턴 검사 모듈로 보내기 위한 요소들을 객체화.
    private UserMessage element; // 도메인별 발송 대상자 객체
    private SendLogRecord record; // 로그 파일 포맷 객체
    private CountManager rCManager;
    private PatternSearch filter = new PatternSearch();

    /***************** Common Values *******************/
    private boolean rIsAppended;
    private String senderEmail; // 재발송 보내는 이
    private String currentAgent; // 현재 실행중인 Thread Name
    private String domain; // Passage로 부터 받은 도메인 객체의 해당 도메인
    private String response; // 메일 서버로 부터 받은 응답메시지
    private String responseDigit; // 메일 서버로 부터 받은 응답코드
    private String resultCode; // 발송 결과 코드
    private int rowID; // Row ID of UserMessage
    private String accountID;
    private String name;
    private String email;
    private int taskNo;
    private int subTaskNo;
    private int retrySCount = 0; // 재발송 성공 수
    private int retryFCount = 0; // 재발송 실패 수
    private int retryTimes;
    private int rSocketTimeOut;
    private boolean rMode; // 발송 모드
    private boolean rTest; // 테스트 발송 모드
    private String unitName;
//	private AgentControler retryAgentWatcher;
    private UnitThreadCounter unitGuard;
    private String retryAgentNM;
    private int deptNo;
    private String userNo;
    private String campTyNo;
    private int campNo;
    private int sendNo;
    private String target_grp_ty;
    private String bizkey;

    public RetrySender(DMSendUnit DMSU, UnitThreadCounter ug) {
        retryTimes = DMSU.retryCount;

        sendNo = DMSU.sendNo;
        rSocketTimeOut = DMSU.socketTimeOut;
        rMode = DMSU.sendMode;
        rTest = DMSU.sendTest;

        RETRY_WAIT_TIME = ConfigLoader.RETRY_WAIT_TIME;
        SEND_DOMAIN = ConfigLoader.SEND_DOMAIN;

        message = DMSU.message;
        senderEmail = DMSU.sender;

        // Send Log
        rAccessor = DMSU.access;
        rCManager = DMSU.cManager;

        // init()
        mxRecord = DMSU.mxRecord;

        unitName = DMSU.unitName;
        attachData = DMSU.appendedFile;
        taskNo = DMSU.taskNo;
        subTaskNo = DMSU.subTaskNo;

        deptNo = DMSU.deptNo;
        userNo = DMSU.userNo;
        campTyNo = DMSU.campTyNo;
        campNo = DMSU.campNo;

        rIsAppended = DMSU.isAppended;

        unitGuard = ug;
        retryAgentNM = DMSU.agentNM;
        target_grp_ty = DMSU.target_grp_ty;
    }

    /*
     * Excuting Agent Thread
     */
    public void run() {
        if (sendMail()) {
            exitThread();
        }
        else {
            LogWriter.writeError("DMSender", "run()", "sendMail()에서 불완전한 종료 ", " ");
        }
    }

    /*
     * 소켓 초기화
     * ipVec의 IP를 한개씩 꺼내와 소켓을 오픈한다.
     * 소켓 오픈에 실패하면 그다음 IP로 재차 시도한다.
     */
    private ConnectManager makeConnection() {
        ConnectManager sock = null;
        int mxLength = 0;
        String mxValue = "";
        if (mxRecord.size() > 0) {
            mxLength = mxRecord.size();
        }
        if (mxLength > 0) {
            for (int i = 0; i < mxLength; i++) {
                mxValue = (String) mxRecord.elementAt(i);
                sock = new ConnectManager(mxValue, 25);
                if (sock.initConnect(rSocketTimeOut)) {
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
     */
    private boolean sendMail() {
        Vector receivers = new Vector();
        fmt = new SimpleDateFormat("yyyyMMddHHmm", Locale.US);
        domain = message.domain;
        receivers = message.recvList; // UserMessage 들의 Vector
        int receiversSize = receivers.size();

        for (int k = (sendNo + 1); k <= retryTimes; k++) {
            socket = makeConnection();
            if (socket.isConnected()) {
                if (socket.cmdHelo(SEND_DOMAIN)) {
                    sendSMTP(receivers, k);
                    k = retryTimes + 1;
                }
                else {
                    response = socket.getResponseMessage();
                    responseDigit = responseDigit(socket.getResponseCode());
                    pUnit = new PatternUnit("002", responseDigit, response);
                    resultCode = filter.filterMessage(pUnit);
                    writeHeloErrorLog(receivers, response, responseDigit, resultCode, k);
                }
            }
            else {
                writeSocketErrorLog(receivers, socket.getConnectErrorCode(),
                                    socket.getErrorMessage(), k);
                socket.initialization();
            }
        }

        boolean endFlag = false;
        synchronized (rCManager) {
            unitGuard.delUnitFactor(retryAgentNM);
            rCManager.increaseSCount(retrySCount);
            rCManager.increaseEndCount(retryFCount + retrySCount); // 실패된 대상자를 현재 발송된 수에 더함.
            if (unitGuard.getSize() < 1) {
                endFlag = true;
            }
            else {
                endFlag = false;
            }
        }

        if (endFlag) {

            System.out.println(new StringBuffer("#############################################################").append("\r\n")
                               .append("\tRETRY Finished Time : " + getTime()).append("\r\n")
                               .append("\tNEO_TASK : " + unitName).append("\r\n")
                               .append("\tTotal Count : " + rCManager.getTotalCount()).append("\r\n")
                               .append("\tSuccess Count : " + rCManager.getSCount()).append("\r\n")
                               .append("\tFail Count : " + (rCManager.getEndCount() - rCManager.getSCount())).append("\r\n")
                               .append("#############################################################").append("\r\n")
                               .toString());
            System.out.flush();
            rAccessor.writeUnitLogEndTime(System.currentTimeMillis());
            rAccessor.close();
            FileManager.deleteUnitFiles(unitName, rIsAppended);
        }
        else {
            /*************************************************
                  System.out.println(new StringBuffer("---------------------------------------------").append("\r\n")
                    .append("\tRETRY Current Time : " + getTime()).append("\r\n")
                    .append("\tNEO_TASK : " + unitName).append("\r\n")
                    .append("\t" + retryAgentNM + " is finished").append("\r\n")
                    .append("\tActive Threads : " + unitGuard.getSize()).append("\r\n")
                    .append("\tTotal Count : " + rCManager.getTotalCount()).append("\r\n")
                    .append("\tSuccess Count : " + rCManager.getSCount()).append("\r\n")
                    .append("\tFail Count : " + (rCManager.getEndCount() - rCManager.getSCount())).append("\r\n")
                    .append("---------------------------------------------")
                 .toString());
                  System.out.flush();
             *************************************************/
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
     */
    private void sendSMTP(Vector receivers, int startNo) {
        String receiver = "";
        Vector tempReceivers = null;
        byte[] emlData;
        for (int j = startNo; j <= retryTimes; j++) {
            if (tempReceivers != null) {
                receivers = (Vector) tempReceivers.clone();
            }
            tempReceivers = new Vector();
            int recvCount = receivers.size();
            if (recvCount > 0) { // 재발송 대상자가 없으면 루프를 실행할 필요 없음
                for (int i = 0; i < recvCount; i++) {
                    element = (UserMessage) receivers.remove(0);
                    rowID = element.getRowID();
                    receiver = element.getEmailAddr();
                    if (socket.cmdRset()) {
                        if (socket.cmdMailFrom(senderEmail)) {
                            if (socket.cmdRcptTo(receiver)) {
                                if (rMode) {
                                    if (socket.cmdData()) {
                                        try {
                                            StringBuffer sb = new StringBuffer();
                                            String srcFile = new StringBuffer(ConfigLoader.TRANSFER_ROOT_DIR)
                                                .append(ConfigLoader.TRANSFER_REPOSITORY_DIR)
                                                .append("content/").append(unitName).append(File.separator)
                                                .append(rowID).append(".mcf").toString();
                                            
                                            //System.out.println("###11### srcFile : "+srcFile);
                                            emlData = FileManager.loadEml(srcFile);
                                            /**
                                             * 송진우 2004.11.18
                                             * 발송내용없음 체크
                                             */
                                            if (emlData.length == 0) {
                                                throw new FileNotFoundException("발송데이터 로드시 에러");
                                            }
                                            socket.sendEmailData(emlData);
                                            if (rIsAppended) {
                                                socket.sendEmailData(attachData);
                                            }

                                            if (socket.cmdDataTransferComplete()) {
                                                response = socket.getResponseMessage();
                                                responseDigit = responseDigit(socket.getResponseCode());
                                                writeOneRecord(element, "000", ErrorCode.HARDERROR, ErrorCode.HDP_DOT,
                                                    responseDigit, fmt.format(new java.util.Date()), response, j);
                                                /**
                                                 * 송진우 2004.11.22
                                                 * 테스트발송의 경우 발송데이터를 공유하므로 바로 지우면 않됨.
                                                 */
                                                if (!rTest) {
                                                    FileManager.deleteEmlFiles(srcFile, rowID);
                                                }
                                            }
                                            else {
                                                // pCode = 6;
                                                tempReceivers.add(element);
                                                response = socket.getResponseMessage();
                                                responseDigit = responseDigit(socket.getResponseCode());
                                                pUnit = new PatternUnit("006", responseDigit, response);
                                                resultCode = filter.filterMessage(pUnit);
                                                writeOneRecord(element, resultCode, ErrorCode.HARDERROR, ErrorCode.HDP_DOT,
                                                    responseDigit, fmt.format(new java.util.Date()), response, j);
                                            }
                                        }
                                        catch (Exception e) {
                                            if (e instanceof FileNotFoundException) {
                                                writeOneRecord(element, "009", ErrorCode.HARDERROR, ErrorCode.HDP_DOT,
                                                    "999", fmt.format(new java.util.Date()), "File Not Found Exception", j);
                                                LogWriter.writeException("retrySender", "sendSMTP()", "eml 파일을 읽는데 실패", e);
                                            }
                                            else if (e instanceof SocketException) {
                                                writeOneRecord(element, "009", ErrorCode.HARDERROR, ErrorCode.HDP_DOT,
                                                    "999", fmt.format(new java.util.Date()), e.getMessage(), j);
                                                LogWriter.writeException("retrySender", "sendSMTP()", "SocketException이 발생",
                                                    e);
                                            }
                                            else {
                                                writeOneRecord(element, "009", ErrorCode.HARDERROR, ErrorCode.HDP_DOT,
                                                    "999", fmt.format(new java.util.Date()),
                                                    e.getMessage() + ErrorCode.HDR_NETWORK, j);
                                                LogWriter.writeException("retrySender", "sendSMTP()", "기타 에러", e);
                                            }
                                        }
                                    }
                                    else { // pCode = 5;
                                        tempReceivers.add(element);
                                        response = socket.getResponseMessage();
                                        responseDigit = responseDigit(socket.getResponseCode());
                                        pUnit = new PatternUnit("005", responseDigit, response);
                                        resultCode = filter.filterMessage(pUnit);
                                        writeOneRecord(element, resultCode, ErrorCode.HARDERROR, ErrorCode.HDP_DATA,
                                            responseDigit, fmt.format(new java.util.Date()), response, j);
                                    }
                                }
                                else { // 가상 발송 성공
                                    response = socket.getResponseMessage();
                                    responseDigit = responseDigit(socket.getResponseCode());
                                    writeOneRecord(element, "000", "002", "004", responseDigit,
                                        fmt.format(new java.util.Date()), response, j);
                                }
                            }
                            else { // pCode = 4
                                tempReceivers.add(element);
                                response = socket.getResponseMessage();
                                responseDigit = responseDigit(socket.getResponseCode());
                                pUnit = new PatternUnit("004", responseDigit, response);
                                resultCode = filter.filterMessage(pUnit);
                                writeOneRecord(element, resultCode, ErrorCode.HARDERROR, ErrorCode.HDP_RCPTTO,
                                               responseDigit, fmt.format(new java.util.Date()), response, j);
                            }
                        }
                        else { // pCode = 3
                            tempReceivers.add(element);
                            response = socket.getResponseMessage();
                            responseDigit = responseDigit(socket.getResponseCode());
                            pUnit = new PatternUnit("003", responseDigit, response);
                            resultCode = filter.filterMessage(pUnit);
                            writeOneRecord(element, resultCode, ErrorCode.HARDERROR, ErrorCode.HDP_MAILFROM,
                                           responseDigit, fmt.format(new java.util.Date()), response, j);
                        }
                    }
                    else {
                        tempReceivers.add(element);
                        response = socket.getResponseMessage();
                        responseDigit = responseDigit(socket.getResponseCode());
                        pUnit = new PatternUnit("007", responseDigit, response);
                        resultCode = filter.filterMessage(pUnit);
                        writeOneRecord(element, resultCode, ErrorCode.HARDERROR, ErrorCode.HDP_RSET,
                                       responseDigit, fmt.format(new java.util.Date()), response, j);
                    }
                } // End Inner For Loop
            }
            // 재방송 횟수가 1이상일 경우 RETRY_WAIT_TIME 후 다시 시도 한다.
            if (retryTimes > 1) {
                try {
                    sleep(RETRY_WAIT_TIME);
                }
                catch (InterruptedException e) {
                    LogWriter.writeException("RetryCenter", "run()", retryTimes + "번째를 위해 대기중 발생", e);
                }
            }
        } // End Outer For Loop
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
     * @param retryTimes :	재발송 횟수
     */
    private void writeOneRecord(UserMessage message, String resultCode, String majorCode,
                                String pCode, String responseDigit, String currentTime, String responseMsg, int retryTimes) {
        rowID = message.getRowID();
        accountID = message.geAccountID();
        email = message.getEmailAddr();
        name = message.getAccountName();
        bizkey = message.getBizkey();

        if (responseMsg.trim().equals("")) {
            responseMsg = "NO MESSAGE";
        }
        record = new SendLogRecord(rTest, rowID, deptNo, userNo, campTyNo, campNo, taskNo, subTaskNo,
                                   accountID, retryTimes, email, name, currentTime, resultCode, majorCode, pCode,
                                   String.valueOf(responseDigit), responseMsg, target_grp_ty, bizkey);
        rAccessor.writeSendLogRecord(record);
        rAccessor.writeUnitLog(rowID, resultCode);
        // 성공 카운터 증가
        if (resultCode.equals("000")) {
            retrySCount++;
        }
        else {
            retryFCount++;
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
     * @param retryTimes : 재발송 횟수
     */
    private void writeHeloErrorLog(Vector vec, String responseMsg, String responseDigit,
                                   String resultCode, int retryTimes) {
        int vecSize = vec.size();
        int rowID;
        if (responseMsg.trim().equals("")) {
            responseMsg = "NO MESSAGE";
        }
        if (responseDigit.equals("999")) {
            responseMsg = "999 Network Error(" + responseMsg + ")";
        }
        for (int i = 0; i < vecSize; i++) {
            element = (UserMessage) vec.elementAt(i);
            accountID = element.geAccountID();
            email = element.getEmailAddr();
            name = element.getAccountName();
            bizkey = element.getBizkey();
            rowID = element.getRowID();
            record = new SendLogRecord(rTest, rowID, deptNo, userNo, campTyNo, campNo, taskNo, subTaskNo,
                                       accountID, retryTimes, email, name, fmt.format(new java.util.Date()), resultCode,
                                       ErrorCode.HARDERROR, ErrorCode.HDP_HELO, String.valueOf(responseDigit), responseMsg, target_grp_ty, bizkey);
            rAccessor.writeSendLogRecord(record);
            rAccessor.writeUnitLog(rowID, resultCode);
        }
        retryFCount = retryFCount + vecSize;
    }

    /**
     * 해당 도메인에 소켓 연결을 맺을때 에러가 발생했을때 DMSender class로 넘어가지 않고
     * 로그를 기록 한다..
     * @param dm : DomainMessage  객체
     * @param access : 로그 파일 객체
     * @param cManager : 카운터 객체
     * @param retryTimes : 재발송 횟수
     */
    private void writeSocketErrorLog(Vector recV, String sCode, String rMsg, int retryTimes) {
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
            message = (UserMessage) recV.elementAt(i);
            rowID = message.getRowID();
            accountID = message.geAccountID();
            email = message.getEmailAddr();
            name = message.getAccountName();
            bizkey = message.getBizkey();
//			004 : 네트웍 에러 (결과 코드)
//			003 : 도메인 없음
            record = new SendLogRecord(rTest, rowID, deptNo, userNo, campTyNo, campNo,
                                       taskNo, subTaskNo, accountID, retryTimes,
                                       email, name, fmt.format(new java.util.Date()),
                                       resultCode, ErrorCode.SOFTERROR, pCode,
                                       sCode, responseM, target_grp_ty, bizkey);
            rAccessor.writeSendLogRecord(record);
            rAccessor.writeUnitLog(rowID, resultCode);
        }
        retryFCount = retryFCount + vecSize;
    }

    /**
     *  발송 완료된 domainMessage에 대해 Thread를 종료한다.
     */
    private void exitThread() {
        Thread currentThread = RetrySender.currentThread();
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
            LogWriter.writeException("RetrySender", "closeChannel()", "소켓을 닫는데 실패 했습니다.", e);
            return false;
        }
        finally {
            try {
                if (socket != null) {
                    socket.closeConnect();
                }
            }
            catch (Exception e) {}
        }
        return true;
    }

    public static String getTime() {
        try {
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy년 MM월 dd일 a HH시 mm분");
            return fmt.format(new Date());
        }
        catch (Exception e) {
            return "Date Error";
        }
    }
}
