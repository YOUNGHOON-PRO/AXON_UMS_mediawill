package messager.center.creator;

import java.io.*;
import java.sql.*;
import java.util.*;

import messager.center.creator.parse.*;
import messager.center.db.*;
import messager.center.repository.*;
import messager.common.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * QMessage[메일 큐테이블의 내용] 객체에 대한 Message를 생성 시키고 결과 물인 MessageInfo 객체와 대상자를 포함한
 * UnitInfo 객체를 생성후 저장한다 진행 순서 1. CampaignExe, Segment, Template 테이블 검색 2. 정기메일일
 * 경우 MailQueuePre 테이블에 인서트 3. 템플릿, 첨부파일 요청및 템플릿 분석 4. 회원 DB검색 또는 Address파일 가져오기
 * 5. MailQueue테이블 인서트
 */
public class MessageCreator
    extends Thread
{
	
	private static final Logger LOGGER = LogManager.getLogger(MessageCreator.class.getName());
	
    private static MessageMap messageMap;
    static {
        messageMap = MessageMap.getInstance();
    }

    private Message message;

    // 생성된 Unit의 총수
    private int unitCount;

    // 대상자의 총수
    private int count;

    // Log Table에 Insert를 위한 객체
    // 정기 메일일 경우 Queue 테이블에도 인서트
    //      private InsertManager insertManager;

    // 발생된 에러 코드
    private int errorCode;

    //Work DB Connection
    private JdbcConnection connection;

    /** 대상자 정보를 가져오기 위한 객체 */
    private SendToFetcher sendToFetcher;

    /** 컨텐츠를 분석하기 위한 객체 */
    private TemplateParser parser;

    /** 컨텐츠 파일, 첨부파일, Address File를 가져오기 위한 객체 */
    private FileRequester fileRequester;

    /**
     * @param threadGroup
     * @param messageID
     * @param taskMap
     */
    public MessageCreator(ThreadGroup thGroup, Message message) {
        super(thGroup, message.messageID);
        this.message = message;
        try {
            connection = JdbcConnection.getWorkConnection();
        }
        catch (Exception ex) {
        	LOGGER.error(ex);
            //ex.printStackTrace();
        }
    }

    public void run() {
        String messageID = message.messageID;  //task아이디  345-1
        MessageStatus messageStatus = new MessageStatus(messageID);

        // 등록되어 있으면 종료한다.
        if (messageStatus.exists()) {
            return;
        }

        //Work DB에 접속
        try {
            connection.openConnection();
        }
        catch (Exception ex) {
        	LOGGER.error(ex);
            //ex.printStackTrace();
            return;
        }

        String resultCode = null;

        TaskFetcher taskFetcher = new TaskFetcher(message);
        try {
            //NEO_TASK 테이블을 검색한다.
            taskFetcher.fetch(connection);  //DB에 있는 데이터(neo_task, neo_segment)를 다 읽어 Message 객체에 (VO)에 적재

            //debug
            //System.out.println("============");
            //System.out.println(message.keyMap);
            //System.out.println("------------");
            //System.out.println(message.taskMap);
            //System.out.println("============");
            
            LOGGER.info("============");
            LOGGER.info(message.keyMap);
            LOGGER.info("------------");
            LOGGER.info(message.taskMap);
            LOGGER.info("============");

            parser = new TemplateParser(message);

            //제목에 대한 분석(Merge키와 일반 텍스트의 구분으로 Template 객체 생성)
            message.subject = subjectParse();
            message.toName = toNameParse();
            try {
                fileRequester = new FileRequester();
            }
            catch (IOException ex) {
            	LOGGER.error(ex);
                String detail = "FileRequester open Fail";
                throw new FetchException(detail, ErrorCode.FILE_CONNECT_FAIL);
            }
            Contents contents = fetchContents();  // 메일본문 + 첨부파일 템플릿 리스트 + 첨부파일 리스트
            
            
            // 웹에이전트 보안메일 일 경우
            Contents2 contents2 = null;
            
            // 웹에이전트 보안 HTML
            if("Y".equals(message.webagent_secuYn) && message.webagent_sourceUrl != null && "HTML".equals(message.webagent_secuAttTyp)) {
            	contents2 = fetchContents2();  	
        	
        	// 웹에이전트 보안 PDF
            }else if("Y".equals(message.webagent_secuYn) && message.webagent_sourceUrl != null && "PDF".equals(message.webagent_secuAttTyp)) {
            	contents2 = fetchContents2();  	
        	
        	// 웹에이전트 보안 EXCEL	
            }else if("Y".equals(message.webagent_secuYn) && message.webagent_sourceUrl != null && "EXCEL".equals(message.webagent_secuAttTyp)) {
            	contents2 = fetchContents2();  
            }
            
            SendToFetcher sendToFetcher = createSendToFetcher();
            sendToFetcher.fetch();  // unit 디렉토리 생성 및 데이터 생성 
            int receiverCount = sendToFetcher.getReceiverCount(); //수신자대상수
            int unitCount = sendToFetcher.getUnitCount();  // unit 파일 수 
            int lastUnitID = sendToFetcher.getLastUnitID();  // 마지막 unit 번호

            if (receiverCount <= 0) {
                String detail = "[" + message.messageID + "] Not Exists Target Member ";
                throw new FetchException(detail, ErrorCode.NOT_EXISTS_MEMBER);
            }

            try {
                messageStatus.createFile(unitCount, lastUnitID, receiverCount);  //mesage 폴더 생성 및 초기 데이터 파일 생성
                //messageStatus.writeMessage(message, contents);	// message 폴더에 파일 내용 업데이트
                
                // 웹에이전트 보안 HTML 추가
                if("Y".equals(message.webagent_secuYn) && message.webagent_sourceUrl != null && "HTML".equals(message.webagent_secuAttTyp)) {
                	messageStatus.writeMessage(message, contents, contents2);	
                
            	// 웹에이전트 보안 PDF 추가
                }else if("Y".equals(message.webagent_secuYn) && message.webagent_sourceUrl != null && "PDF".equals(message.webagent_secuAttTyp)) {
                	messageStatus.writeMessage(message, contents, contents2);	
                
            	// 웹에이전트 보안 EXCEL 추가
                }else if("Y".equals(message.webagent_secuYn) && message.webagent_sourceUrl != null && "EXCEL".equals(message.webagent_secuAttTyp)) {
                	messageStatus.writeMessage(message, contents, contents2);	
                
                }else {
                	messageStatus.writeMessage(message, contents);
                }
                
            }
            catch (Exception ex) {
            	LOGGER.error(ex);
                String detail = "[" + message.messageID
                    + "] MessageStatus File: " + ex.getMessage();
                throw new FetchException(detail, ErrorCode.EXCEPTION);
            }
        }
        catch (FetchException ex) {
        	LOGGER.error(ex);
            //ex.printStackTrace();
            resultCode = ex.getErrorCode();
        }
        finally {
            if (fileRequester != null) {
                fileRequester.close();
            }
        }

        try {
            TaskStatusManager.update(connection, message.taskNo,
                                     message.subTaskNo, resultCode);   //발송중으로 DB 업데이트
        }
        catch (SQLException ex) {
        	LOGGER.error(ex);
            ex.printStackTrace();
        }

        connection.close();
        if (resultCode == null) {
            if (!messageMap.registry(messageID, messageStatus)) {
                System.err.println("작업등록 실패 : " + messageID);
            }
        }
    }

    private Template subjectParse()
        throws FetchException {
        HashMap taskMap = message.taskMap;
        String subject = (String) taskMap.remove("NEO_TASK.MAIL_TITLE");
        Template sTemplate = null;

        if (subject != null) {
            try {
                sTemplate = parser.parse(subject);
            }
            catch (Exception ex) {
            	LOGGER.error(ex);
                String detail = "[" + message.messageID
                    + "] Subject Parse Error " + ex.getMessage();
                throw new FetchException(detail, ErrorCode.INVALID_SUBJECT);
            }
        }
        else {
            String detail = "[" + message.messageID + "] Not Found SUBJECT";
            throw new FetchException(detail, ErrorCode.INVALID_SUBJECT);
        }

        return sTemplate;
    }

    private Template toNameParse()
        throws FetchException {
        HashMap taskMap = message.taskMap;
        String toName = (String) taskMap.remove("NEO_TASK.NM_MERGE");
        Template tnTemplate = null;

        if (toName != null) {
            try {
                tnTemplate = parser.parse(toName);
            }
            catch (Exception ex) {
            	LOGGER.error(ex);
                String detail = "[" + message.messageID
                    + "] Invalid NM_MERGE: " + ex.getMessage();
                throw new FetchException(detail, ErrorCode.INVALID_NM_MERGE);
            }
        }
        else {
            String detail = "[" + message.messageID + "] Not Found NM_MERGE";
            throw new FetchException(detail, ErrorCode.INVALID_NM_MERGE);
        }
        return tnTemplate;
    }

    private Contents fetchContents()
        throws FetchException {
        HashMap taskMap = message.taskMap;
        int taskNo = message.taskNo;
        String path = (String) taskMap.get("NEO_TASK.CONT_FL_PATH");
        String contentTypeCode = (String) taskMap.get("CONT_TY");
        String charsetCode = message.charsetCode;
        String bodyEncCode = message.bodyEncodingCode;
        String headerEncCode = message.headerEncodingCode;
        TemplateContent mainContent = null;
        if (path == null || path.length() == 0) {
            String detail = "[" + message.messageID
                + "] Not Found NEO_TASK.CONT_FL_PATH";
            throw new FetchException(detail, ErrorCode.INVALID_CONTENT_PATH);
        }

        try {
            ContentFetcher contentFetcher =
                new ContentFetcher(path, contentTypeCode, charsetCode, bodyEncCode);
            mainContent = contentFetcher.fetch(fileRequester, parser);
        }
        catch (Exception ex) {
        	LOGGER.error(ex);
            if (ex instanceof FetchException) {
                throw (FetchException) ex;
            }
            String detail = "[" + message.messageID
                + "] Content Fetch Exception : " + ex.getMessage();
            throw new FetchException(detail, ErrorCode.CONTENT_FETCH_FAIL);
        }

        if (mainContent == null) {
            String detail = "[" + message.messageID + "] Not Exists Content";
            throw new FetchException(detail, ErrorCode.CONTENT_FETCH_FAIL);
        }
        
        ArrayList templateList = null;
        ArrayList fileList = null;
        int attachCnt = ( (Integer) taskMap.remove("NEO_TASK.ATT_CNT"))
            .intValue();
        if (attachCnt > 0) {
            AttachContentFetcher attachFetcher
                = new AttachContentFetcher(connection, taskNo, attachCnt, charsetCode, headerEncCode, message);
            attachFetcher.fetch(fileRequester, parser);
            templateList = attachFetcher.getTemplateList();
            fileList = attachFetcher.getFileContentList();
        }
        return new Contents(mainContent, templateList, fileList);
    }
    
    
    private Contents2 fetchContents2()
            throws FetchException {
            HashMap taskMap = message.taskMap;
            int taskNo = message.taskNo;
            String path = (String) taskMap.get("NEO_TASK.CONT_FL_PATH");
            String contentTypeCode = (String) taskMap.get("CONT_TY");
            String charsetCode = message.charsetCode;
            String bodyEncCode = message.bodyEncodingCode;
            String headerEncCode = message.headerEncodingCode;
            TemplateContent2 mainContent2 = null;
            
//            if (path == null || path.length() == 0) {
//                String detail = "[" + message.messageID
//                    + "] Not Found NEO_TASK.CONT_FL_PATH";
//                throw new FetchException(detail, ErrorCode.INVALID_CONTENT_PATH);
//            }

            try {
                ContentFetcher contentFetcher2 =
                    new ContentFetcher(path, contentTypeCode, charsetCode, bodyEncCode);
                mainContent2 = contentFetcher2.fetch2(fileRequester, parser, message);
                
            }
            catch (Exception ex) {
            	LOGGER.error(ex);
                if (ex instanceof FetchException) {
                    throw (FetchException) ex;
                }
                String detail = "[" + message.messageID
                    + "] Content Fetch Exception : " + ex.getMessage();
                throw new FetchException(detail, ErrorCode.CONTENT_FETCH_FAIL);
            }


            ArrayList templateList = null;
            ArrayList fileList = null;
//            int attachCnt = ( (Integer) taskMap.remove("NEO_TASK.ATT_CNT"))
//                .intValue();
//            if (attachCnt > 0) {
//                AttachContentFetcher attachFetcher
//                    = new AttachContentFetcher(connection, taskNo, attachCnt, charsetCode, headerEncCode);
//                attachFetcher.fetch(fileRequester, parser);
//                templateList = attachFetcher.getTemplateList();
//                fileList = attachFetcher.getFileContentList();
//            }
            return new Contents2(mainContent2, templateList, fileList);
        }

    /**
     * Message의 대상자를 가져와서 UnitInfo객체를 생성한다.
     *
     * @param fileRequester
     *            대상자 그룹이 파일에 저장되어 있을 경우 File를 가져올때 사용된다.
     * @exception CreatorException
     *                대상자를 가져올 경우나 DB 검색이 실패할 경우
     */
    private SendToFetcher createSendToFetcher()
        throws FetchException {
        HashMap taskMap = message.taskMap;
        
        String creatType = (String) taskMap.remove("NEO_SEGMENT.CREATE_TY");

        if (creatType.equals("003")) { //FileGroup일 경우
        	
            sendToFetcher = new FileSendToFetcher(message, fileRequester);
        }
        else {
        	
            sendToFetcher = new DBSendToFetcher(message, connection);
        }

        /*********************************
         * 004 연계 처리를 위한 코드 삽입할것
         * writed by 오범석
         */

        return sendToFetcher;
    }
}
