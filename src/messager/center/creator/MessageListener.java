package messager.center.creator;

import java.sql.*;
import java.text.*;
import java.util.*;

import messager.center.config.*;
import messager.center.db.*;
import messager.center.repository.*;
import messager.common.*;
import messager.common.util.*;
import com.custinfo.safedata.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 이 클래스는 Mail Queue 테이블에서 발송 예약된 메시지를 검색하고 Mail Queue 테이블에서 검색된 메시지를 삭제하고
 * MessageHandler 실행한다.
 */
public class MessageListener
    extends Thread
{
	private static final Logger LOGGER = LogManager.getLogger(MessageListener.class.getName());

	//DBType 추가
    private static String insert_dbtype;
	
    /**
     * 원래 코드에 대한 주석
     * [Task에 대한 데이타 검색중인 상태 (NEO_SUBTASK.WORK_STATUS 필드) 를 나타내기 위해 임시로 사용된다.]
     * 하지만 바로 발송중으로 처리하도록 수정한다.
     * writed by 오범석
     * private static final String RUN_STATUS = "099";
     * */
    private static final String RUN_STATUS = "002";

    /** Task의 발송 예약 승인 상태를 나타낸다. */
    private static final String INIT_STATUS = "001";

    /** 발송 예약 날짜 비교를 위한 현재 시간의 포맷 */
    private static final String TIME_FORMAT = "yyyyMMddHHmm";

    /** 검색할 컬럼 리스트 */
    private static final Column[] columns = {
        new Column(null, 0),
        new Column("NEO_SUBTASK.SUB_TASK_NO", Column.INTEGER),
        new Column("NEO_SUBTASK.TASK_NO", Column.INTEGER),
        new Column("NEO_SUBTASK.SEND_DT", Column.STRING),
        new Column("NEO_SUBTASK.RESP_END_DT", Column.STRING),
        new Column("NEO_SUBTASK.SEND_TEST_YN", Column.STRING),
        new Column("NEO_SUBTASK.SEND_TEST_EM", Column.STRING),
        new Column("NEO_SUBTASK.SEND_TEST_CNT", Column.INTEGER),
        new Column("NEO_SUBTASK.SEND_IP", Column.STRING),
        new Column("NEO_SUBTASK.RTY_TYP", Column.STRING),
        new Column("NEO_SUBTASK.RTY_TASK_NO", Column.STRING),
        new Column("NEO_SUBTASK.RTY_SUB_TASK_NO", Column.STRING),
        new Column("NEO_SUBTASK.RTY_CODE", Column.STRING),
        new Column("NEO_SUBTASK.TEST_SEND_ID", Column.STRING),
        new Column("NEO_WEBAGENT.ATT_NO", Column.STRING),
        new Column("NEO_WEBAGENT.SOURCE_URL", Column.STRING),
        new Column("NEO_WEBAGENT.SECU_ATT_YN", Column.STRING),
        new Column("NEO_WEBAGENT.SECU_ATT_TYP", Column.STRING),
        new Column("NEO_MAILMKT_CHK.MAIL_MKT_GB", Column.STRING)
        };
     
    private static String fetchSQL;

    static {
    	
        StringBuffer buffer = new StringBuffer();

        buffer.append("SELECT ").append(columns[1].name);
        for (int i = 2; i < columns.length; i++) {
            buffer.append(", ").append(columns[i].name);
        }

    	if("MYSQL".equals(insert_dbtype)) {
		  buffer.append(" FROM NEO_SUBTASK ");
  	      buffer.append(" INNER JOIN NEO_TASK ON NEO_SUBTASK.TASK_NO = NEO_TASK.TASK_NO ");
  	      buffer.append(" LEFT OUTER JOIN NEO_WEBAGENT ON NEO_TASK.TASK_NO = NEO_WEBAGENT.TASK_NO ");
  	      buffer.append(" LEFT OUTER JOIN NEO_MAILMKT_CHK ON NEO_TASK.TASK_NO = NEO_MAILMKT_CHK.TASK_NO ");
  	      buffer.append("	WHERE NEO_SUBTASK.WORK_STATUS = ? ")
  	            .append("AND NEO_SUBTASK.TASK_NO = NEO_TASK.TASK_NO ")
  	            .append("AND NEO_TASK.STATUS = '000' ")
  	            .append("AND NEO_SUBTASK.SEND_DT <= ? ")
  	            .append("AND NEO_SUBTASK.CHANNEL = '000'");	
  	        
  	      	fetchSQL = buffer.toString();
    		
    	} else if("MARIA".equals(insert_dbtype)) {
		  buffer.append(" FROM NEO_SUBTASK ");
  	      buffer.append(" INNER JOIN NEO_TASK ON NEO_SUBTASK.TASK_NO = NEO_TASK.TASK_NO ");
  	      buffer.append(" LEFT OUTER JOIN NEO_WEBAGENT ON NEO_TASK.TASK_NO = NEO_WEBAGENT.TASK_NO ");
  	      buffer.append(" LEFT OUTER JOIN NEO_MAILMKT_CHK ON NEO_TASK.TASK_NO = NEO_MAILMKT_CHK.TASK_NO ");
  	      buffer.append("	WHERE NEO_SUBTASK.WORK_STATUS = ? ")
  	            .append("AND NEO_SUBTASK.TASK_NO = NEO_TASK.TASK_NO ")
  	            .append("AND NEO_TASK.STATUS = '000' ")
  	            .append("AND NEO_SUBTASK.SEND_DT <= ? ")
  	            .append("AND NEO_SUBTASK.CHANNEL = '000'");	
  	        
  	      	fetchSQL = buffer.toString();
    		
    	} else {
		 buffer.append(" FROM NEO_SUBTASK ");
	     buffer.append(" INNER JOIN NEO_TASK ON NEO_SUBTASK.TASK_NO = NEO_TASK.TASK_NO ");
	     buffer.append(" LEFT OUTER JOIN NEO_WEBAGENT ON NEO_TASK.TASK_NO = NEO_WEBAGENT.TASK_NO ");
	     buffer.append(" LEFT OUTER JOIN NEO_MAILMKT_CHK ON NEO_TASK.TASK_NO = NEO_MAILMKT_CHK.TASK_NO ");
	     buffer.append("	WHERE NEO_SUBTASK.WORK_STATUS = ? ")
	           .append("AND NEO_SUBTASK.TASK_NO = NEO_TASK.TASK_NO ")
	           .append("AND NEO_TASK.STATUS = '000' ")
	           .append("AND NEO_SUBTASK.SEND_DT <= ? ")
	           .append("AND NEO_SUBTASK.CHANNEL = '000'");	
    	        
    	   fetchSQL = buffer.toString();
    	}
     
    }

    /** 검색중인 TASK의 작업 상태로 업데이트 문
     * 분석시 메세지단위의 발송을 시작하는 시간을 넣어주도록 한다.
     * 송진우 2004.11.09
     */
    private final static String TASK_UPDATE_SQL
        = "UPDATE NEO_SUBTASK SET WORK_STATUS = ?, SEND_DT = ? WHERE TASK_NO = ? AND SUB_TASK_NO = ?";
    	// chk cskim 07.09.04

    /** Message Creator Thread Group Name */
    private final static String GROUP_NAME = "message_creator";

    /** Message Creator Thread Group */
    private static ThreadGroup threadGroup;

    /**
     * MessageListener의 객체 <br>
     * 하나의 인스턴스만 생성되어야 한다.
     */
    private static MessageListener instance;

    /** MessageListener의 객체를 생성하고 Thread를 실행한다. */
    public static void execute() {
        synchronized (MessageListener.class) {
            if (threadGroup == null) {
                threadGroup = new ThreadGroup(GROUP_NAME);
            }
            if (instance == null) {
                try {
                    instance = new MessageListener();
                }
                catch (Exception ex) {
                	LOGGER.error(ex);
                    //ex.printStackTrace();
                    if (ex instanceof ClassNotFoundException) {
                        System.exit(1);
                    }
                }
            }
            if (!instance.isAlive()) {
                instance.start();
            }
        }
    }

    /** 예약 승인된 Task 검색 주기 */
    private long fetchPeriod;

    /** 검색후 Connection 닫기(true: Connection close, false: Connection 유지) */
    private boolean closeFlag;

    /** 1회 검색 후 가져올수 있는 Task 수 */
    private int maxFetchSize;

    /** 발송 예약 시간 비교을 위한 현재 시간의 Formatter */
    private SimpleDateFormat timeFormatter;

    /** NEO_SUBTASK 테이블 검색을 위한 WORK DB의 컨넥션 */
    private JdbcConnection connection;

    private MessageMap messageMap;
    

    /**
     * NEO_SUBTASK 테이블에서 예약 승인된 TASK 검색을 위한 MessageListener 객체를 생성한다.
     */
    private MessageListener()
        throws Exception {
        fetchPeriod = ConfigLoader.getInt("message.fetch.period", 10) * 1000;
        closeFlag = ConfigLoader.getBool("database.connection.close", false);
        maxFetchSize = ConfigLoader.getInt("message.fetch.size", 1);
        timeFormatter = new SimpleDateFormat(TIME_FORMAT);
        connection = JdbcConnection.getWorkConnection();
        messageMap = MessageMap.getInstance();
        insert_dbtype = ConfigLoader.getProperty("db.dbType");
    }

    /**
     * 지정된 시간 만큼 Sleep
     */
    private void sleepListener() {
        try {
            sleep(fetchPeriod);
        }
        catch (InterruptedException ex) {
        	LOGGER.error(ex);
        }
    }

    /**
     * NEO_SUBTASK 테이블에서 예약 승인된 Task를 가져온다.
     *
     * @param size
     *            NEO_SUBTASK테이블에 한번 검색후 가져올 TASK의 수
     * @return ArrayList Task를 HashMap의 형태로 가져온 후 ArrayList에 저장하여 리턴
     * @exception SQLException
     */
    private ArrayList fetchTask(int size)
        throws SQLException {
        PreparedStatement pstmt = null;
        ResultSet rset = null;
        Exception exception = null;
        java.util.Date currentTime = new java.util.Date();

        //현재시간의 포맷팅된 String(yyyyMMddHHmm)얻기
        String curTime = timeFormatter.format(new java.util.Date());
        ArrayList list = new ArrayList(size);
        String status = INIT_STATUS;
        try {
        	
//        	System.out.println("fetchSQL : " +fetchSQL );
//        	System.out.println("status : " +status );   //001
//        	System.out.println("curTime : " +curTime ); //202110022241
        	
            //PreparedStatement open
            pstmt = connection.prepareStatement(fetchSQL);
            pstmt.setString(1, status);
            pstmt.setString(2, curTime);
            rset = pstmt.executeQuery();
            while (rset.next()) {
                HashMap taskMap = new HashMap();
                HashMap keyMap = new HashMap();
                String keyName;
                for (int i = 1; i < columns.length; i++) {
                    Column column = columns[i];
                    Object obj = null;
                    switch (column.type) {
                        case Column.INTEGER:
                            obj = new Integer(rset.getInt(i));
                            break;
                        case Column.STRING: {
                            String str = rset.getString(i);
                            if (str != null && str.length() > 0) {
                                obj = str.trim();
                            }
                        }
                        break;
                        case Column.STRING_INTEGER: {
                            int ivalue = 0;

                            String str = rset.getString(i);
                            if (str != null && str.length() > 0) {
                                try {
                                    ivalue = Integer.parseInt(str.trim());
                                    obj = new Integer(ivalue);
                                }
                                catch (NumberFormatException ex) {
                                	LOGGER.error(ex);
                                }
                            }
                        }
                        break;
                        case Column.CHARACTER: {
                            char ch = 0;

                            String str = rset.getString(i);
                            if (str != null && str.length() > 0) {
                                ch = str.charAt(0);
                                obj = new Character(ch);
                            }
                        }
                        break;
                    }
                    keyName = MessageKey.getName(column.name);
                    if (keyName != null) {
                        if (obj != null) {
                            keyMap.put(keyName, obj.toString());
                        }
                        else {
                            keyMap.put(keyName, new Integer( -1));
                        }
                    }
                    if (obj != null) {
                        taskMap.put(column.name, obj);
                    }
                }
                int taskNo =
                    ( (Integer) taskMap.get("NEO_SUBTASK.TASK_NO")).intValue();
                int subTaskNo = ( (Integer) taskMap.get("NEO_SUBTASK.SUB_TASK_NO")).intValue();
                String sendTest = (String) taskMap.remove("NEO_SUBTASK.SEND_TEST_YN");
                
                String webagent_attNo = (String) taskMap.remove("NEO_WEBAGENT.ATT_NO");
                String webagent_sourceUrl = (String) taskMap.remove("NEO_WEBAGENT.SOURCE_URL");
                webagent_sourceUrl = "^:"+webagent_sourceUrl+":^";
                String webagent_secuYn = (String) taskMap.remove("NEO_WEBAGENT.SECU_ATT_YN");
                String webagent_secuAttTyp = (String) taskMap.remove("NEO_WEBAGENT.SECU_ATT_TYP");

                String mail_mkt_gb = (String) taskMap.remove("NEO_MAILMKT_CHK.MAIL_MKT_GB");
                
                String rty_typ = (String) taskMap.remove("NEO_SUBTASK.RTY_TYP");
                String rty_task_no = (String) taskMap.remove("NEO_SUBTASK.RTY_TASK_NO");
                String rty_sub_task_no = (String) taskMap.remove("NEO_SUBTASK.RTY_SUB_TASK_NO");
                String rty_code = (String) taskMap.remove("NEO_SUBTASK.RTY_CODE");
                String test_send_id = (String) taskMap.remove("NEO_SUBTASK.TEST_SEND_ID");
                
                String messageID = messageMap.createMessageID(taskNo, subTaskNo);
                Message message = new Message(messageID);
                list.add(message); //객체를 list에 add

                //message객체의 값을 채운다.
                message.taskNo = taskNo;
                message.subTaskNo = subTaskNo;
                message.taskMap = taskMap;
                message.keyMap = keyMap;
                
                
                //message객체의 보안메일 webagent 정보를 채운다.
                if(webagent_attNo != null || webagent_attNo != "") {
                	message.webagent_attNo = webagent_attNo;
                }
                if(webagent_sourceUrl != null || webagent_sourceUrl != "") {
                	message.webagent_sourceUrl = webagent_sourceUrl;
                }
                if(webagent_secuYn != null || webagent_secuYn != "") {
                	message.webagent_secuYn = webagent_secuYn;
                }
                if(webagent_secuAttTyp != null || webagent_secuAttTyp != "") {
                	message.webagent_secuAttTyp = webagent_secuAttTyp;
                }
                if(mail_mkt_gb != null || mail_mkt_gb != "") {
                	message.mail_mkt_gb = mail_mkt_gb;
                }
                
                
                if(rty_typ != null || rty_typ != "") {
                	message.rty_typ  = rty_typ ;
                }
                if(rty_task_no != null || rty_task_no != "") {
                	message.rty_task_no   = rty_task_no  ;
                }
                if(rty_sub_task_no != null || rty_sub_task_no != "") {
                	message.rty_sub_task_no   = rty_sub_task_no  ;
                }
                if(rty_code != null || rty_code != "") {
                	message.rty_code   = rty_code  ;
                }
                if(test_send_id != null || test_send_id != "") {
                	message.test_send_id   = test_send_id  ;
                }
                
                
                if (sendTest.equals("Y")) { //값이 Y일때 테스트 발송
                	
                	//복호화
            		String ALGORITHM = "PBEWithMD5AndDES";
            		String KEYSTRING = "ENDERSUMS";
            		EncryptUtil enc =  new EncryptUtil();
            		
            		CustInfoSafeData CustInfo = new CustInfoSafeData();
            		
                    message.isTest = true;
                    String toList = (String) taskMap.remove("NEO_SUBTASK.SEND_TEST_EM");
                    
                    //복호화
                    //toList = CustInfo.getDecrypt(toList, "NOT_RNNO");
                    
                    if (toList == null || toList.length() == 0) {
                        message.errorCode = ErrorCode.NOT_FOUND_TEST_EMAIL_FIELD;
                        continue;
                    }
                    StringTokenizer st = new StringTokenizer(toList, ",; \t\r\n");
                    int count = st.countTokens();

                    if (count == 0) {
                        message.errorCode = ErrorCode.NOT_FOUND_TEST_EMAIL_FIELD;
                        continue;
                    }

                    String[] address = new String[count];
                    for (int i = 0; i < count; i++) {
                        address[i] = st.nextToken();
                    }
                    
                    message.testTo = address;

                    message.send_test_cnt = ( (Integer) taskMap.remove("NEO_SUBTASK.SEND_TEST_CNT")).intValue();
                    if (message.send_test_cnt == 0) {
                        message.send_test_cnt = 1; //address.length;
                    }
                }
                else {
                    message.retryEndTime
                        = retryEndTime( (String) taskMap.get("NEO_SUBTASK.RESP_END_DT"));
                }

                if (list.size() >= size) {
                    break;
                }
            }
        }
        catch (Exception ex) {
            //ex.printStackTrace();
        	LOGGER.error(ex);
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
            }

            if (pstmt != null) {
                try {
                    pstmt.close();
                }
                catch (SQLException ex) {
                	LOGGER.error(ex);
                }
            }
        }
        if (exception != null) {
            if (exception instanceof SQLException) {
                throw (SQLException) exception;
            }
            else {
                throw (RuntimeException) exception;
            }
        }
        return list;
    }

    /**
     * Message의 발송 정보와 대상자의 정보를 검색하는 작업을 실행한다.
     * 실행된 Message는 Neo_SubTask테이블의 Work_Status 필드를 "002"로 업데이트 한다.
     *
     * @param list Neo_SubTask에서 검색된 Message객체 리스트
     * @param SQLException Work DB에 업데이트 중 에러가 발생할 경우
     */
    private void runTask(ArrayList list)
        throws SQLException {
        Exception exception = null;
        PreparedStatement pstmt = null;
        String curTime = timeFormatter.format(new java.util.Date());
        try {
            while (list.size() > 0) {
                if (pstmt == null) {
                    pstmt = connection.prepareStatement(TASK_UPDATE_SQL);
                }
                Message message = (Message) list.remove(0);
                int taskNo = message.taskNo;
                int subTaskNo = message.subTaskNo;
                String errorCode = message.errorCode;

                String statusCode = RUN_STATUS;
                if (errorCode != null) {
                    statusCode = errorCode;
                }
                pstmt.setString(1, statusCode);
                pstmt.setString(2, curTime);
                pstmt.setInt(3, taskNo);
                pstmt.setInt(4, subTaskNo);
                if (pstmt.executeUpdate() > 0 && errorCode == null) {
                    MessageCreator msgCreator
                        = new MessageCreator(threadGroup, message);
                    msgCreator.start();
                }
            }
        }
        catch (Exception ex) {
        	LOGGER.error(ex);
            exception = ex;
        }
        finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                }
                catch (SQLException ex) {
                	LOGGER.error(ex);
                }
            }
        }

        if (exception != null) {
            if (exception instanceof SQLException) {
                throw (SQLException) exception;
            }
            else {
                throw (RuntimeException) exception;
            }
        }
    }

    /**
     * run
     */
    public void run() {
        while (true) {
            boolean dbClose = closeFlag;
            try {
                //DB에서 새로운 Task을 가져올 개수 얻는다.
                int fetchSize = maxFetchSize - threadGroup.activeCount();

                if (fetchSize > 0) {
                    connection.openConnection();
                    ArrayList list = fetchTask(fetchSize);
                    if (list.size() > 0) {
                        runTask(list);
                    }
                }
            }
            catch (SQLException ex) {
            	LOGGER.error(ex);
                //ex.printStackTrace();
                dbClose = true;
            }
            catch (Exception ex) {
            	LOGGER.error(ex);
                //ex.printStackTrace();
                dbClose = true;
            }
            if (dbClose) {
                connection.close();
            }
            sleepListener();
        }
    }

    /**
     * 재발송 완료 시간을 long 형으로 얻는다.
     * 재발송 완료시간은 NEO_SUBTASK.RESP_END_DT보다 하루 작은 값으로 지정한다.
     * @param endTime
     * @return long으로 변환된 시간
     */
    private long retryEndTime(String endTime) {
        long time = 0;
        try {
            java.util.Date dateTime = timeFormatter.parse(endTime);
            time = dateTime.getTime() - (60 * 60 * 24);
        }
        catch (Exception ex) {LOGGER.error(ex);}
        return time;
    }

}
