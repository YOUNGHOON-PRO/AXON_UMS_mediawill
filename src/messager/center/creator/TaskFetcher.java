package messager.center.creator;

import java.sql.*;
import java.util.*;

import messager.center.db.*;
import messager.common.*;
import messager.common.util.*;
import com.custinfo.safedata.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Neo_Task테이블과 Neo_SubTask 테이블을 검색하여 발송 정보를 저장하는
 * Message 객체의 항목을 채우고 정기메일일 경우 새로운 업무를 Neo_SubTask에 인서트를
 * 실행한다.
 * @author Administrator
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
class TaskFetcher
{
	
	private static final Logger LOGGER = LogManager.getLogger(TaskFetcher.class.getName());
	
    private final static int charsetColumn = 1;

    /**
     * 검색할 컬럼 리스트
     */
    
    private final static Column[] columns = new Column[] {
        new Column(null, 0),
        new Column("CHARSET",
                   "(SELECT CD_NM FROM NEO_CD WHERE CD_GRP = 'C022' AND USE_YN = 'Y' AND UILANG = '000' AND CD = NEO_TASK.CHARSET) CHARSET",
                   Column.STRING),
        new Column("NEO_TASK.TASK_NO", Column.INTEGER),
        new Column("NEO_TASK.MAIL_FROM_NM", Column.STRING),
        new Column("NEO_TASK.MAIL_FROM_EM", Column.STRING),
        new Column("NEO_TASK.REPLY_TO_EM", Column.STRING),
        new Column("NEO_TASK.RETURN_EM", Column.STRING),
        //new Column("NEO_TASK.SEND_TO_NM_MERGE", Column.STRING),
        new Column("NEO_TASK.NM_MERGE", Column.STRING),
        new Column("NEO_TASK.MAIL_TITLE", Column.STRING),
        new Column("NEO_TASK.ATT_CNT", Column.INTEGER),
        new Column("NEO_TASK.SEND_REPEAT", Column.STRING_INTEGER),
        new Column("NEO_TASK.SEND_TERM_END_DT", Column.STRING),
        new Column("NEO_TASK.SEND_TERM_LOOP", Column.STRING_INTEGER),
        new Column("NEO_TASK.SEND_TERM_LOOP_TY", Column.STRING),
        new Column("NEO_TASK.RESP_LOG", Column.INTEGER),
        new Column("NEO_TASK.SOCKET_TIMEOUT", Column.INTEGER),
        new Column("NEO_TASK.CONN_PER_CNT", Column.INTEGER),
        new Column("NEO_TASK.RETRY_CNT", Column.INTEGER),
        new Column("NEO_TASK.SEND_MODE", Column.STRING),

        new Column("HEADER_ENC",
                   "(SELECT CD_NM FROM NEO_CD WHERE CD_GRP = 'C021' AND USE_YN = 'Y' AND UILANG = '000' AND CD = NEO_TASK.HEADER_ENC) HEADER_ENC",
                   Column.STRING),
        new Column("BODY_ENC",
                   "(SELECT CD_NM FROM NEO_CD WHERE CD_GRP = 'C021' AND USE_YN = 'Y' AND UILANG = '000' AND CD = NEO_TASK.BODY_ENC) BODY_ENC",
                   Column.STRING),

        new Column("NEO_TASK.CONT_FL_PATH", Column.STRING),
        new Column("CONT_TY",
                   "(SELECT CD_NM FROM NEO_CD WHERE CD_GRP = 'C018' AND USE_YN = 'Y' AND UILANG = '000' AND CD = NEO_TASK.CONT_TY) CONT_TY",
                   Column.STRING),
        new Column("NEO_TASK.CAMP_TY", Column.STRING),
        new Column("NEO_TASK.CAMP_NO", Column.INTEGER),
        new Column("NEO_TASK.DEPT_NO", Column.INTEGER),
        new Column("NEO_TASK.USER_ID", Column.STRING),
        new Column("NEO_TASK.TARGET_GRP_TY", Column.STRING),
        new Column("NEO_SEGMENT.SEG_NO", Column.INTEGER),
        new Column("NEO_SEGMENT.DB_CONN_NO", Column.INTEGER),
        new Column("NEO_SEGMENT.MERGE_KEY", Column.STRING),
        new Column("NEO_SEGMENT.SEG_FL_PATH", Column.STRING),
        new Column("NEO_SEGMENT.CREATE_TY", Column.STRING),
        new Column("NEO_SEGMENT.SEG_NM", Column.STRING),
        //new Column("NEO_SEGMENT.QUERY", Column.STRING),   
        new Column("NEO_SEGMENT.QUERY","CASE WHEN ? = '001' THEN NEO_SEGMENT_RETRY.QUERY  WHEN ? = '002' THEN NEO_SEGMENT_REAL.QUERY ELSE NEO_SEGMENT.QUERY END AS QUERY", Column.STRING),
        new Column("NEO_SEGMENT.SEPARATOR_", Column.STRING),
        new Column("NEO_TASK.TITLE_CHK_YN", Column.STRING),
        new Column("NEO_TASK.BODY_CHK_YN", Column.STRING),
        new Column("NEO_TASK.ATTACH_FILE_CHK_YN", Column.STRING),
        new Column("NEO_TASK.SECU_MAIL_CHK_YN", Column.STRING),
        //2006-11-24 생성 // 2007.10.09 재 주석
//        new Column("NEO_SEGMENT.DECODE", Column.STRING)
    };

    /**
     * Neo_Task 테이블과 Neo_Segment 테이블을 검색할 쿼리문
     */
    private static String taskFetchSQL;

    //Neo_Task 테이블과 Neo_Segment 테이블을 검색할 쿼리문을 생성한다.
    static {
        StringBuffer buffer = new StringBuffer();
        
        buffer.append("SELECT");
        buffer.append(' ').append(columns[1].query);
        for (int i = 2; i < columns.length; i++) {
            buffer.append(", ").append(columns[i].query);
        }
//        buffer.append(" FROM NEO_TASK, NEO_SEGMENT").append(
//                " WHERE NEO_TASK.TASK_NO = ? AND ").append(
//                "NEO_TASK.SEG_NO = NEO_SEGMENT.SEG_NO ");  //chk cskim 07.09.05

        buffer.append(" FROM NEO_TASK ").append(
        		" INNER JOIN NEO_SEGMENT NEO_SEGMENT ON NEO_TASK.SEG_NO = NEO_SEGMENT.SEG_NO ").append(
        		" LEFT OUTER JOIN NEO_SEGMENT_RETRY NEO_SEGMENT_RETRY ON NEO_TASK.SEG_NO = NEO_SEGMENT_RETRY.SEG_NO" ).append(
        		" LEFT OUTER JOIN NEO_SEGMENT_REAL NEO_SEGMENT_REAL ON NEO_TASK.SEG_NO = NEO_SEGMENT_REAL.SEG_NO" ).append(
                " WHERE NEO_TASK.TASK_NO = ? AND ").append(
                "NEO_TASK.SEG_NO = NEO_SEGMENT.SEG_NO ");  //chk cskim 07.09.05
        
        taskFetchSQL = buffer.toString();
        //System.out.println(taskFetchSQL);
        LOGGER.info(taskFetchSQL);
        
        
        
    }

    /**
     * 발송 정보를 저장할 Message 객체
     */
    Message message;

    /**
     * Neo_Task와 Neo_Segment 테이블을 검색하기 위한 TaskFetcher 객체를 생성한다.
     * @param message
     */
    public TaskFetcher(Message message) {
        this.message = message;
    }

    /**
     * Message에 대한 발송 정보를 검색한다. NEO_SUBTASK에서 검색된 TASK_NO (업무번호)를 조건으로 하여 <br>
     * NEO_TASK와 NEO_SEGMENT를 Join하여 Message의 발송 정보를 가져온다.
     *
     * @param connection
     *            Work DB의 컨넥션
     * @return HashMap 데이타의 필드명을 key로 하여 검색된 데이타를 저장한 HashMap
     * @exception CreatorException
     *                데이타가 존재하지 않을 경우 또는 <br>
     *                검색시 SQLException이 발생한 경우 <br>
     *                charactor encoding 이 실패할 경우
     */
    public void fetch(JdbcConnection connection)
        throws FetchException {
        PreparedStatement pstmt = null;
        ResultSet rset = null;
        Exception exception = null;
        int taskNo = message.taskNo;
        
        //재발송 관련 변수 선언
        int subTaskNo = message.subTaskNo;
        String rty_typ = message.rty_typ; 				//재발송유형
        String rty_task_no = message.rty_task_no;			//재발송캠페인업무정의등록번호
        String rty_sub_task_no = message.rty_sub_task_no;	//재발송보조업무번호
        String rty_code = message.rty_code;					//재발송대상코드
        
        if(rty_code == null || "".equals(rty_code)) {
        	// null체크
        }else {
        	 /*
             * rty_code 값이 단건 일 경우 
             * 적용전  : 001
             * 적용후  : '001',
             * 
             * try_code 값이 여러건 일 경우
             * 적용전  : 001,002,003
             * 적용후  : '001','002','003',
             */
            StringBuffer str2 = new StringBuffer();
            String[] arr_rtyCode= rty_code.split(",");
            for(int i=0;i<arr_rtyCode.length;i++) {
            	str2.append(  "'"+arr_rtyCode[i]+"'" + (arr_rtyCode.toString().length() >= 0 ? "," : "" ));
            	}

            
            /* 마지막 콤마 제거 작업
             * 적용전  : '001','002','003',
             * 적용후  : '001','002','003'
             */
            rty_code = str2.toString().substring(0, str2.toString().length() - 1);
            
        }
       
        String test_send_id = message.test_send_id;
        
        HashMap keyMap = message.keyMap;
        HashMap taskMap = message.taskMap;
        
        String SEND_TEST_YN="";
        String mergeStr="";
        try {
            //PreparedStatement 객체 생성한다.
            pstmt = connection.prepareStatement(taskFetchSQL); 

            //조건인 taskNo(업무 번호) 설정
            //pstmt.setInt(1, taskNo);
            pstmt.setString(1, rty_typ);
            pstmt.setString(2, rty_typ);
            pstmt.setInt(3, taskNo);

            //ResultSet 객체를 얻는다.
            rset = pstmt.executeQuery();
            if (rset.next()) {
                Column column = null;
                String str = null;

                for (int i = 1; i < columns.length; i++) {
                    column = columns[i];
                    Object value = null;
                    switch (column.type) {
                        case Column.INTEGER:
                            value = new Integer(rset.getInt(i));
                            break;

                        case Column.LONG:
                            value = new Long(rset.getLong(i));
                            break;

                        case Column.STRING:
                            str = rset.getString(i);
                            if (str != null) {
                                try {
                                    value = connection.fromDB(str.trim());
                                }
                                catch (java.io.UnsupportedEncodingException ex) {
                                	LOGGER.error(ex);
                                    String detail = "[" + message.messageID +
                                        "] UnsupportedEncodingExcepiton: " + column.name;
                                    throw new FetchException(detail,
                                        ErrorCode.UNSUPPORTED_CHARSET);
                                }
                            }
                            break;

                        case Column.CHARACTER:
                            str = rset.getString(i);
                            if (str != null && str.length() > 0) {
                                char c = str.charAt(0);
                                value = new Character(c);
                            }
                            break;

                        case Column.STRING_INTEGER:
                            str = rset.getString(i);
                            if (str != null && str.length() > 0) {
                                try {
                                    value = new Integer(Integer.parseInt(str.trim()));
                                }
                                catch (NumberFormatException ex) {
                                	LOGGER.error(ex);
                                }
                            }
                            break;
                    }

                    if (value != null) {
                        String keyName = MessageKey.getName(column.name);
                        if (keyName != null) {
                            keyMap.put(keyName, value.toString());
                        }
                        //-----------------------------------------------------------------------------
                        // 재발송용 쿼리 데이터 치환 처리
                        if("NEO_SEGMENT.QUERY".equals(column.name)) {
                        	//System.out.println("재발송용 쿼리 데이터 치환 처리 >>");
                        	//System.out.println("전 : "+value.toString());
                        	
                        	if(rty_code ==null || "".equals(rty_code)) {
                        		//null체크
                        		mergeStr = ((String) value).replace("$:TASK_NO:$", ""+rty_task_no+"");
                            	mergeStr = mergeStr.replace("$:SUB_TASK_NO:$", ""+rty_sub_task_no+"");	
                        	}else {
                        		mergeStr = value.toString().replace("$:BIZKEY:$", rty_code);
                            	mergeStr = mergeStr.replace("$:RCODE:$", rty_code);
                            	mergeStr = mergeStr.replace("$:TASK_NO:$", ""+rty_task_no+"");
                            	mergeStr = mergeStr.replace("$:SUB_TASK_NO:$", ""+rty_sub_task_no+"");
                        	}
                        	//System.out.println("후 : "+mergeStr);
                        	taskMap.put(column.name, ((Object)mergeStr));
                        //-----------------------------------------------------------------------------
                        }else {
                        	taskMap.put(column.name, value);
                        }
                        
                    }
                }
            }
            else {
                String detail = "[" + message.messageID + "] Not Found TASK";
                throw new FetchException(detail, ErrorCode.NOT_FOUND_TASK);
            }
        }
        catch (Exception ex) {
        	LOGGER.error(ex);
            //ex.printStackTrace();
            exception = ex;
        }
        finally {
            if (rset != null) {
                try {
                    rset.close();
                }
                catch (SQLException ex) {
                	LOGGER.error(ex);
                }
                rset = null;
            }
            
            if (pstmt != null) {
                try {
                    pstmt.close();
                }
                catch (SQLException ex) {
                	LOGGER.error(ex);
                }
                pstmt = null;
            }
        }

        if (exception != null) {
            if (exception instanceof FetchException) {
                throw (FetchException) exception;
            }
            else if (exception instanceof SQLException) {
                String detail = "[" + message.messageID + "] "
                    + exception.getMessage();
                throw new FetchException(detail, ErrorCode.SQL_EXCEPTION);
            }
            else {
                String detail = "[" + message.messageID + "] "
                    + exception.getMessage();
                throw new FetchException(detail, ErrorCode.EXCEPTION);
            }
        }

       //테스트발송이 아니면 정기발송 등록  (false 이면 정기발송)
       if(!message.isTest) {
    	   taskInsert(connection);  // 정기메일 인서트
       }
       taskMapToMessage();  // taskmap = neo_task, neo_segment 데이터를 message객체에 세팅하고 keymap 에는 머지키를 추가적으로 세팅하는데 머지컬럼 명과, 머지키커럼의 위치 값을 저장
       
    }

    /**
     * Email Address의 유효성 체크 <br>
     *
     * @의 존재여부와 중복여부만 체크한다.
     * @param address
     *            email address
     * @return email이 유효하면 true
     */
    private boolean isValidAddress(String address) {
        boolean isValid = false;

        if (address != null) {
            address = address.trim();
            int len = address.length();
            int inx = address.indexOf('@');

            if (inx > 0 || (len - inx) > 0) {
                char ch;
                isValid = true;
                for (int i = 0; i < inx; i++) {
                    ch = address.charAt(i);
                    if (ch <= ' ') {
                        isValid = false;
                        break;
                    }
                }
                for (int i = ++inx; isValid && i < len; i++) {
                    ch = address.charAt(i);
                    if (ch == '@' || ch < 32) {
                        isValid = false;
                    }
                }
            }
        }

        return isValid;
    }

    /**
     * Neo_Task와 Neo_Segment 테이블에서 검색된 데이타를 Message객체의 항목에 채운다
     *
     * @throws FetchException 존재하여야 하는 항목이 존재 하지 않을 경우
     */
    private void taskMapToMessage()
        throws FetchException {
        HashMap keyMap = message.keyMap;  	// task 정보   			{TASK_NO=345, CAMP_NO=1, SUB_TASK_NO=1, RESP_END_DT=202109061818, USER_ID=ADMIN, CAMP_TY=003, MAIL_FROM_NM=ADMIN, DEPT_NO=1}
        HashMap taskMap = message.taskMap;  // task + segment 정보	{NEO_SEGMENT.SEG_NM=test, NEO_SUBTASK.SEND_DT=202108251826, NEO_SEGMENT.SEG_FL_PATH=addressfile/ADMIN/1626159110771-sample01.csv, NEO_TASK.TASK_NO=345, NEO_TASK.MAIL_FROM_EM=AXon@enders.co.kr, NEO_SEGMENT.SEG_NO=61, NEO_TASK.SEND_MODE=001, NEO_TASK.RETURN_EM=AXon@enders.co.kr, NEO_TASK.SOCKET_TIMEOUT=30, NEO_SEGMENT.MERGE_KEY=EMAIL,NAME,ID,AGE,GENDER,BIZ, NEO_SEGMENT.CREATE_TY=003, HEADER_ENC=8bit, NEO_TASK.USER_ID=ADMIN, NEO_SUBTASK.SUB_TASK_NO=1, NEO_SUBTASK.TASK_NO=345, NEO_SEGMENT.SEPARATOR_=,, NEO_TASK.CAMP_NO=1, NEO_TASK.NM_MERGE=$:NAME:$, NEO_TASK.ATT_CNT=0, NEO_TASK.CAMP_TY=003, BODY_ENC=8bit, NEO_TASK.MAIL_TITLE=test, NEO_TASK.CONT_FL_PATH=content/ADMIN/202108251827580711.tmp, NEO_SEGMENT.DB_CONN_NO=0, NEO_TASK.CONN_PER_CNT=10, CONT_TY=HTML, NEO_TASK.DEPT_NO=1, NEO_SUBTASK.RESP_END_DT=202109061818, NEO_SUBTASK.SEND_TEST_CNT=0, CHARSET=euc-kr, NEO_TASK.RETRY_CNT=0, NEO_TASK.REPLY_TO_EM=AXon@enders.co.kr, NEO_TASK.MAIL_FROM_NM=ADMIN, NEO_TASK.RESP_LOG=7}

        CustInfoSafeData safeDbEnc = new CustInfoSafeData();
        
        message.charsetCode = (String) taskMap.remove("CHARSET");

        String messageID = message.messageID;

        String fromName = (String) taskMap.remove("NEO_TASK.MAIL_FROM_NM");
        if (fromName == null) {
            String detail = "[" + messageID + "] Invalid FROM_NAME: " + fromName;
            throw new FetchException(detail, ErrorCode.INVALID_FROM_NAME);
        }
        else {
            message.fromName = fromName;
        }

        String fromEmail = (String) taskMap.remove("NEO_TASK.MAIL_FROM_EM");
        
        //복호화
//        try {
//			fromEmail = safeDbEnc.getDecrypt(fromEmail, "NOT_RNNO");
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
        
        if (!isValidAddress(fromEmail)) {
            String detail =
                "[" + messageID + "] Invalid From Email: " + fromEmail;
            throw new FetchException(detail, ErrorCode.INVALID_FROM_EMAIL);
        }
        else {
            message.fromEmail = fromEmail;
        }
        String reply_email =  (String) taskMap.remove("NEO_TASK.REPLY_TO_EM");
        
        //복호화
//        try {
//			reply_email = safeDbEnc.getDecrypt(reply_email, "NOT_RNNO");
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
        
        message.replyTo = reply_email;
        
        
        if (!isValidAddress(message.replyTo)) {
            String detail = "[" + messageID + "] Invalid ReplyTo: " + message.replyTo;
            throw new FetchException(detail, ErrorCode.INVALID_REPLY_TO);
        }

        String return_email = (String) taskMap.remove("NEO_TASK.RETURN_EM");
       
        //복호화
//        try {
//			return_email = safeDbEnc.getDecrypt(return_email, "NOT_RNNO");
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
        
        message.returnPath = return_email;
        
        
        if (!isValidAddress(message.returnPath)) {
            String detail = "[" + messageID + "] Invalid ReturnPath: " +
                message.returnPath;
            throw new FetchException(detail, ErrorCode.INVALID_RETURN_PATH);
        }
        //message.toNameSuffix = (String) taskMap.remove("NEO_TASK.SEND_TO_NM_MERGE");
        message.campaignType = (String) taskMap.remove("NEO_TASK.CAMP_TY");
        message.campaignNo = intValue(taskMap.remove("NEO_TASK.CAMP_NO"));
        message.userNo = (String) taskMap.remove("NEO_TASK.USER_ID");
        message.deptNo = intValue(taskMap.remove("NEO_TASK.DEPT_NO"));
        message.socketTimeout = intValue(taskMap.remove("NEO_TASK.SOCKET_TIMEOUT"));
        message.connPerCount = intValue(taskMap.remove("NEO_TASK.CONN_PER_CNT"));
        message.retryCount = intValue(taskMap.remove("NEO_TASK.RETRY_CNT"));
        
        //개인정보체크 
        message.title_chk_yn = (String) taskMap.remove("NEO_TASK.TITLE_CHK_YN");
        message.body_chk_yn = (String) taskMap.remove("NEO_TASK.BODY_CHK_YN");
        message.attach_file_chk_yn = (String) taskMap.remove("NEO_TASK.ATTACH_FILE_CHK_YN");
        message.secu_mail_chk_yn = (String) taskMap.remove("NEO_TASK.SECU_MAIL_CHK_YN");

        //2006-11-24 추가 // 2007.10.09 재 주석
        //message.decode = (String) taskMap.remove("NEO_SEGMENT.DECODE");

        //encoding
        message.headerEncodingCode = (String) taskMap.get("HEADER_ENC");
        message.bodyEncodingCode = (String) taskMap.get("BODY_ENC");

        int sendmode = intValue(taskMap.get("NEO_TASK.SEND_MODE"));
        if (sendmode != 0) {
            message.sendMode = true;

        }
        message.target_grp_ty = (String) taskMap.remove("NEO_TASK.TARGET_GRP_TY");

        int index = 1;
        String receiverKeyList = (String) taskMap.remove("NEO_SEGMENT.MERGE_KEY");
        if (receiverKeyList == null || receiverKeyList.length() == 0) {
            String detail =
                "[" + messageID + "] NEO_SEGMENT.MERGE_KEY Field Not Found : " +
                receiverKeyList;
            throw new FetchException(detail, ErrorCode.INVALID_MERGE_KEY);
        }

        StringTokenizer st = new StringTokenizer(receiverKeyList, ", \t\r\n");  //머지키  EMAIL,NAME,ID,AGE,GENDER,BIZ
        while (st.hasMoreTokens()) {
            String name = st.nextToken();
            keyMap.put(name, new Integer(index++));
            message.keySize++;
        }

        if (!existsBasicInfo(keyMap)) {
            String detail = "[" + messageID + "] Not Exists TO Basic Info";
            throw new FetchException(detail, ErrorCode.NOT_EXISTS_TO_BASIC_INFO);
        }
    }

    /**
     * String 또는 Integer 객체에 저장된 int 형의 값을 얻는다.
     *
     * @param obj int형의 값이 저장된 Integer 객체 또는 String 객체
     * @return int형의 값
     */
    private int intValue(Object obj) {
        int value = 0;
        if (obj != null) {
            if (obj instanceof Integer) {
                value = ( (Integer) obj).intValue();
            }
            else if (obj instanceof String) {
                try {
                    value = Integer.parseInt( (String) obj);
                }
                catch (NumberFormatException ex) {
                	LOGGER.error(ex);
                }
            }
        }
        return value;
    }

    /**
     * 대상자의 기본정보가 머지키 리스트에 존재하는지 검사한다.
     *
     * @param keyMap 머지키가 저장된 HashMap객체
     * @return 존재하면 true
     */
    private boolean existsBasicInfo(HashMap keyMap) {
        boolean exists = true;

        try {
            Integer idColumn = (Integer) keyMap.get(MessageKey.TO_ID);
            Integer nameColumn = (Integer) keyMap.get(MessageKey.TO_NAME);
            Integer emailColumn = (Integer) keyMap.get(MessageKey.TO_EMAIL);

            if (idColumn == null || nameColumn == null || emailColumn == null) {
                exists = false;
            }
        }
        catch (Exception ex) {
        	LOGGER.error(ex);
            exists = false;
        }
        return exists;
    }

    /**
     * 정기 메일일 경우 Neo_SubTask에 새로운 업무를 등록한다.
     *
     * @param connection 연결된 Work DB의 Connection
     */
    private void taskInsert(JdbcConnection connection) {
        HashMap taskMap = message.taskMap;
        int repeatType = intValue(taskMap.remove("NEO_TASK.SEND_REPEAT"));
        //System.out.println("taskNo: " + message.taskNo + "=> repeatType: " + repeatType);
        LOGGER.info("taskNo: " + message.taskNo + "=> repeatType: " + repeatType);
        
        if (repeatType > 0) {
            TaskInserter inserter = new TaskInserter(message);
            inserter.insert(connection);
        }
    }

}
