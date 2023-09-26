package messager.center.creator;

import java.sql.*;
import java.util.*;

import messager.center.config.ConfigLoader;

// import com.activemailkorea.decode.*;

import messager.center.db.*;
import messager.common.*;
import com.custinfo.safedata.*;
import messager.common.util.EncryptUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
/**
 * 이 클래스는 WorkDB에서 대상자를 추출할 DB의 접속정보를 가져와서 대상자가 저장된 DB에 접속하여 대상자를 추출한다.
 */
public class DBSendToFetcher
    extends SendToFetcher
{
	
	private static final Logger LOGGER = LogManager.getLogger(DBSendToFetcher.class.getName());
	
    // 대상자를 추출할 DB의 접속 정보를 검색하는 SQL
    private final static String CON_INFO_SQL = " SELECT A.DB_TY, A.DB_DRIVER, A.DB_URL, A.LOGIN_ID,						"
						+" 	A.LOGIN_PWD, A.DB_CHAR_SET , B.CD_NM AS DB_CHAR_SET_NM		"
				    	+" FROM NEO_DBCONN A, NEO_CD B											"
				    	+"	WHERE A.DB_CONN_NO = ?																	"
				    	+"	AND B.CD_GRP = 'C032'																		"
				    	+"	AND B.USE_YN = 'Y'																				"
				    	+"	AND B.UILANG = '000'																			"
				    	+"	AND B.CD = A.DB_CHAR_SET																";
				    	// chk cskim 070904

    /** DB 접속 정보를 저장한다. */
    private Properties props;

    /** 대상자 추출 쿼리문 */
    private String fetchSQL;

    /**
     * Database에서 대상자를 추출하는 DBSendToFetcher객체를 생성한다.
     *
     * @param messageID
     * @param con
     *            WorkDB의 Connection
     * @param connectionNo
     *            회원DB의 Connection 정보를 검색에 이용
     * @param sql
     *            대상자 추출 쿼리문
     * @param charsetName
     *            컨텐츠의 charset
     */
    public DBSendToFetcher(Message message, JdbcConnection con)
        throws FetchException {
        super(message);
        init(con);
        
    }

    private String createFetchSQL()
        throws FetchException {
        HashMap taskMap = message.taskMap;
        String query = (String) taskMap.remove("NEO_SEGMENT.QUERY");
        return query;
    }

    private void init(JdbcConnection workConnection)
        throws FetchException {
        HashMap taskMap = message.taskMap;
        try {
            int connectionNo = ( (Integer) taskMap
                                .remove("NEO_SEGMENT.DB_CONN_NO")).intValue();

            fetchSQL = createFetchSQL();
            // 회원DB의 접속 정보 검색
            props = getDBConInfo(workConnection, connectionNo);

        }
        catch (Exception ex) {
        	LOGGER.error(ex);
            String detail = "[" + message.messageID
                + "] database connection info search fail: "
                + ex.getMessage();
            throw new FetchException(detail, ErrorCode.MEMBER_DB_SEARCH_FAIL);
        }
    }

    /**
     * ResultSet, Statement close 하고 새로 Connection를 맺었을 경우(WorkDB가 아닐경우)
     * Connection 닫는다.
     */
    protected void close() {
    }

    /**
     * 회원 DB 접속 정보 검색한다.
     *
     * @param workConnection
     *            WorkDB에 접속된 Connection
     * @param connNo
     *            회원DB의 Connection No
     * @return 회원DB의 접속정보를 담은 Properties 객체
     */
    private Properties getDBConInfo(JdbcConnection workConnection, int connNo)
        throws SQLException {
        Properties props = new Properties();
        PreparedStatement pstmt = null;
        ResultSet rset = null;
        Exception ex = null;

        //복호화
		String ALGORITHM = "PBEWithMD5AndDES";
		String KEYSTRING = "ENDERSUMS";
		//EncryptUtil enc =  new EncryptUtil();
		CustInfoSafeData CustInfo = new CustInfoSafeData();
        
        try {
            pstmt = workConnection.prepareStatement(CON_INFO_SQL);
            pstmt.setInt(1, connNo);
            rset = pstmt.executeQuery();
            if (rset.next()) {
                String type = rset.getString(1);
                if (type != null) {
                    props.setProperty("db.type", type);
                }
                String driver = rset.getString(2);
                if (driver != null) {
                    props.setProperty("jdbc.driver.name", driver);
                }
                String url = rset.getString(3);
                if (url != null) {
                    props.setProperty("jdbc.url", url);
                }
                String user = rset.getString(4);
                if (user != null) {
                    props.setProperty("user", user);
                }
                String password = rset.getString(5);
                if (password == null) {
                    password = "";
                }
                
                //복호화
                //String db_password = enc.getJasyptDecryptedString(ALGORITHM, KEYSTRING, password);
                String db_password = CustInfo.getDecrypt(password, KEYSTRING);
                
                props.setProperty("password", db_password);

                String db_char_set = rset.getString(6);
                if (db_char_set == null) {
                    db_char_set = "000"; //일반으로 설정
                }
                props.setProperty("db_char_set", db_char_set);

                String db_char_set_nm = rset.getString(7);
                if (db_char_set_nm == null) {
                    db_char_set_nm = "";
                }
                props.setProperty("db_char_set_nm", db_char_set_nm);
            }
        }
        catch (Exception exception) {
        	LOGGER.error(exception);
            ex = exception;
        }
        finally {
            if (rset != null) {
                try {
                    rset.close();
                }
                catch (SQLException ex2) {
                	LOGGER.error(ex2);
                }
            }
            if (pstmt != null) {
                try {
                    pstmt.close();
                }
                catch (SQLException ex2) {
                	LOGGER.error(ex2);
                }
            }
        }

        if (ex != null) {
            if (ex instanceof SQLException) {
                throw (SQLException) ex;
            }
            else {
                throw (RuntimeException) ex;
            }
        }

        return props;
    }

    /**
     * 대상자들을 읽어와서 UnitInfo 객체 생성
     *
     * @param repository
     *            Unit을 저장한다.
     */
    public void fetch()
        throws FetchException {
    	
        
        Exception exception = null;
        String messageID = message.messageID;
        LegacyConnection connection = null;
        Statement stmt = null;
        ResultSet rs = null;
        Connection conn = null;
        
        try {
        	
            connection = LegacyConnection.getInstance(props);
            conn = connection.getConnection();

            stmt = connection.createStatement();
            String sql = connection.toDB(fetchSQL);
            rs = stmt.executeQuery(sql);
            
            
            
            
            /*===========================================================================================
             * 대상자가 없을경우 프로퍼티의 특정시간만큼 웨이팅 및 루프
             * cnenter.properties
             * RETRY_CNT : 재시도 횟수
             * RETRY_TERM : 60분 MAX (1000 = 1초, 1800000 = 30분, 3600000 )
             *===========================================================================================*/

            ConfigLoader.load();
            int RETRY_CNT = Integer.parseInt(ConfigLoader.getString("RETRY_CNT", "5"));
            
            if(RETRY_CNT >5) {
            	RETRY_CNT=5;
            }
            
            int RETRY_TERM = Integer.parseInt(ConfigLoader.getString("RETRY_TERM", "5000"));
            
            //60분 MAX (1000 = 1초, 1800000 = 30분, 3600000 )
            if(RETRY_TERM >3600000) {
            	RETRY_TERM=3600000;
            }
            
            //정기메일인지 확인 (정기메일일 경우 null 이 아님)
            String SEND_TERM_LOOP_TY = (String) message.taskMap.remove("NEO_TASK.SEND_TERM_LOOP_TY");

            /* SEND_TERM_LOOP_TY
             *	000 : 분
	            001 : 시
	            002 : 일
	            003 : 주
	            004 : 월
	            005 : 년  */
            
            //정기메일 일 경우 
            if(SEND_TERM_LOOP_TY != null) {
            	
            	//정기메일중에 시,일,주,월,년 일경우만 대기 로직 수행
            	if(SEND_TERM_LOOP_TY.equals("002") || SEND_TERM_LOOP_TY.equals("003")|| SEND_TERM_LOOP_TY.equals("004")|| SEND_TERM_LOOP_TY.equals("005")) {
            		int a=1;
                    if(rs.next()) { //대상자가 있을 경우 
                    	rs = stmt.executeQuery(sql);
                    }else { // 대상자가 없을 경우 
                    	//System.out.println("============ 정기메일 Retry process start ============ ");
                    	//System.out.println(RETRY_TERM+"밀리세컨드초 간격으로 "+RETRY_CNT+"회 재 발송 시도 (task_no:"+message.taskNo+")");
                    	
                    	LOGGER.info("============ 정기메일 Retry process start ============ ");
                    	LOGGER.info(RETRY_TERM+"밀리세컨드초 간격으로 "+RETRY_CNT+"회 재 발송 시도 (task_no:"+message.taskNo+")");
                    	while(a<=RETRY_CNT) { 
                    		//System.out.println(a+"회 시작..");
                    		LOGGER.info(a+"회 시작..");
                    		
                    		Thread.sleep(RETRY_TERM);
                    		sql = connection.toDB(fetchSQL);
                        	rs = stmt.executeQuery(sql);
                        	a++;
                        	if(rs.next()){ //대상자가 다시 생겼을 경우
                        		//System.out.println("대상자 생성 확인");
                        		LOGGER.info("대상자 생성 확인");
                        		rs = stmt.executeQuery(sql);
                        		break;
                        	}
                    	}
                    	//System.out.println("============ 정기메일 Retry process end ============ ");
                    	LOGGER.info("============ 정기메일 Retry process end ============ ");
                    }
                }else {
                	//정기메일 이지만 분,시 일경우 제외 대상
                }

            //단기메일 일 경우 대기로직 수행           	
            }else { 
                int a=1;
                if(rs.next()) { //대상자가 있을 경우 
                	rs = stmt.executeQuery(sql);
                }else { // 대상자가 없을 경우
                	//System.out.println("============ 단기메일 Retry process start ============ ");
                	//System.out.println(RETRY_TERM+"밀리세컨드초 간격으로 "+RETRY_CNT+"회 재 발송 시도 (task_no:"+message.taskNo+")");
                	
                	LOGGER.info("============ 단기메일 Retry process start ============ ");
                	LOGGER.info(RETRY_TERM+"밀리세컨드초 간격으로 "+RETRY_CNT+"회 재 발송 시도 (task_no:"+message.taskNo+")");
                	while(a<=RETRY_CNT) { 
                		//System.out.println(a+"회 시작..");
                		LOGGER.info(a+"회 시작..");
                		
                		Thread.sleep(RETRY_TERM);
                		sql = connection.toDB(fetchSQL);
                    	rs = stmt.executeQuery(sql);
                    	a++;
                    	if(rs.next()){ //대상자가 다시 생겼을 경우
                    		//System.out.println("대상자 생성 확인");
                    		LOGGER.info("대상자 생성 확인");
                    		rs = stmt.executeQuery(sql);
                    		break;
                    	}
                	}
                	//System.out.println("============ 단기메일 Retry process end ============ ");
                	LOGGER.info("============ 단기메일 Retry process end ============ ");
                }
            }

            //String 데이타 Type일 경우 DB Charset에서 메세지 Charset으로 변환
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();
            
            
            /*------------------------------------------------------------------------
          		암호화된 대상자컬럼들을  복호화 하기 위한 사전 준비
         	------------------------------------------------------------------------*/
	        
	        //암호화 대상 center.properties에서 가져오기 
	        String dec_merge = ConfigLoader.getString("DEC_MERGE", "");
	        //구분자 변경
	        dec_merge = dec_merge.replace(";",",");
	        
			HashMap<Integer, String> merge_map= new HashMap<Integer, String>();
			//머지 항목 갯수
			int merge_cnt=1;
			String merge_value="";
			for(int z=1; z <=columnCount; z++) {
				merge_value = rsmd.getColumnName(z);
				 if(dec_merge.contains(merge_value)){
					 merge_map.put(merge_cnt, merge_value);
				 }	
				 merge_cnt++;
			}
			/*------------------------------------------------------------------------*/
            
            
            /*-------------------------------------------------------------------------*/
            //마케팅 수신동의 체크를 위한 ID 위치값 발취 (ID 값이 몇번째 컬럼인지 찾음)            
            int ID_i =0;
            if(columnCount != 0) {
            	for(int z=1; z <=columnCount; z++) {
            		//System.out.println("필드값 : " + rsmd.getColumnName(z));
            		//if("ID".equals(rsmd.getColumnName(z).toUpperCase())) { //컬러명
            		if("ID".equals(rsmd.getColumnLabel(z).toUpperCase())) {  //알리아스명
                		ID_i = z;
                	}
            	}
            }
            //System.out.println("ID값은 : " + ID_i );
            /*-------------------------------------------------------------------------*/
            
            /*-------------------------------------------------------------------------*/
            //EMAIL 복호화를 위한 EMAIL 위치값 발취 (EMAIL 값이 몇번째 컬럼인지 찾음)            
//            int EMAIL_i =0;
//            if(columnCount != 0) {
//            	for(int z=1; z <=columnCount; z++) {
//            		//System.out.println("필드값 : " + rsmd.getColumnName(z));
//            		if("EMAIL".equals(rsmd.getColumnName(z))) {
//            			EMAIL_i = z;
//                	}
//            	}
//            }
            //System.out.println("EMAIL값은 : " + EMAIL_i );
            /*-------------------------------------------------------------------------*/
            
            if (columnCount < message.keySize) {
                String detail = "[" + messageID + "] unmatched columns";
                throw new FetchException(detail,
                                         ErrorCode.UNMATCHED_MEMBER_COLUMNS);
            }
            BitSet bitSet = new BitSet(columnCount + 1);

            for (int i = 1; i <= columnCount; i++) {
                int type = rsmd.getColumnType(i);

                switch (type) {
                    case Types.CHAR:
                    case Types.VARCHAR:
                        bitSet.set(i);
                        break;
                }
            }

            // unitID는 1부터 시작
            int unitID = 1;
            //UnitInfo 객체 생성
            UnitInfo unit = new UnitInfo(messageID, unitID++);

//            String isDecode = message.decode;

            //복호화
    		String ALGORITHM = "PBEWithMD5AndDES";
    		String KEYSTRING = "ENDERSUMS";
    		EncryptUtil enc =  new EncryptUtil();
    		
    		CustInfoSafeData safeDbEnc = new CustInfoSafeData();
            
            /***************************************************************
             * 테스트 발송 여부를 확인하여 테스트 발송대상자 수를 얻어온다. writed by 오범석
             ***************************************************************/
            if (message.isTest && "000".equals(message.rty_typ)) { //파일방식 테스트 발송
                int send_test_cnt = message.send_test_cnt;
                for (int j = 0; j < send_test_cnt; j++) {
                    if (rs.next()) {
                        receiverCount++; // 수신 대상자 수 증가
                        // 대상자 머지 정보를 담을 배열
                        String[] columns = new String[columnCount + 1];

                        for (int i = 1; i <= columnCount; i++) {
                                String string = rs.getString(i); // String Type이 아닌 경우
                                if (bitSet.get(i)) { // String 일 경우 Charset에 따라 변환 한다.
                                    
                                	//암호화 (이메일 위치값이 같으면)
//                                	if(i == EMAIL_i) {
//                                		//string = enc.getJasyptDecryptedString(ALGORITHM, KEYSTRING, string);
//                                		string = safeDbEnc.getDecrypt(string, "NOT_RNNO");
//                                		
//                                		string = connection.fromDB(string);
//                                	}else {
//                                		string = connection.fromDB(string);
//                                	}
                                	
                                    if(merge_map.size()!=0) {
            	                    		if(merge_map.get(i)!=null) {
            	                    			string = safeDbEnc.getDecrypt(string, "NOT_RNNO"); //이메일 복호화
            	                    	}
            	                    }
                                	
                                }
                                columns[i] = string;
                        }

                        ReceiverInfo receiverInfo = new ReceiverInfo(columns);
                        unit.add(receiverInfo);
                    }
                    else {
                        break; 
                    }
                } 

                // UnitInfo에 대상자 수가 지정된 수만큼 되었을 경우 UnitInfo를 FileSystem에 저장 후
                writeUnit(unit);
            }
            else if(message.isTest && "002".equals(message.rty_typ)){ //DB방식 테스트 발송
            	
            	String[] columns =null;
            	HashMap<String, String[]> streamMap = new HashMap<String, String[]>();
            	String sqlWhere = "";
            	
                while (rs.next()) {
                    // 대상자 머지 정보를 담을 배열
                    columns = new String[columnCount + 1];
                    for (int i = 1; i <= columnCount; i++) {
                            String string = rs.getString(i); // String Type이 아닌 경우
                            if (bitSet.get(i)) { // String 일 경우 Charset에 따라 변환 한다.
                                if(merge_map.size()!=0) {
        	                    		if(merge_map.get(i)!=null) {
        	                    			string = safeDbEnc.getDecrypt(string, "NOT_RNNO"); //이메일 복호화
        	                    	}
        	                    }
                            }
                            columns[i] = string;
                    } 
               
                    /* TODO
                	 *  1. column의 모든 정보를 저장한다. ( String ) 
                	 *  2. 데이터에서 ID 만 추출해서 SQL 을 준비한다. 
                	 */
                	streamMap.put(columns[ID_i], columns);
        			sqlWhere += "'" + columns[ID_i] + "' ,"; 

                }
                sqlWhere += "'Q'";
                
        		/*
            	 * TODO 
            	 * 1. DB SQL Call
            	 * 2. 결과값을 Hashmap 형태로 보관한다. (= resultMap) 
            	 */
            	HashMap<String, String> resultMap = null;
        		
        		if(message.mail_mkt_gb != null) {
        			MktCheck mktCheck = new MktCheck();
        			resultMap = mktCheck.MktCheckWhere(message.mail_mkt_gb,sqlWhere);
        		}
        		
        		/*  TODO 
            	 *  1. 기존 Reader 에서 비교하던 부분을  Hashmap(= StreamMap ) 에서 꺼내서 사용한다.
            	 *  2. DB 에서 마케팅 동의자에 대한 값이 있는 경우만 처리한다.  
            	 */
        		Set<String> set = streamMap.keySet();
        		
        		Iterator<String> it = set.iterator();
        		
        		while(it.hasNext()) { 
        			String id = it.next();
        			String[] fileds = streamMap.get(id);
        			
        			ReceiverInfo receiverInfo = new ReceiverInfo(fileds);
        			

        			if (message.mail_mkt_gb == null) {
                    	//System.out.println("마케팅 수신동의 미 체크 ");
                    	unit.add(receiverInfo);
                    	receiverCount++;
                    } else { 
                    	//-------------------------------------------------------------------------
                    	//마케팅 수신동의 체크
                    	//System.out.println("마케팅 수신동의  체크 ");
                    	
                    	String mktYn = resultMap.get(id);
                    	// 마케팅 미동의자에 대한 처리 
                    	if ( null == mktYn || "".equals(mktYn)) { 
                    		//continue;

                    		//미동의자 수집
                    		message.mkttList.add(id);
                    		message.mktIdIdx=ID_i;
                    	} 
                        unit.add(receiverInfo);
                        receiverCount++;
                       //-------------------------------------------------------------------------
                    }
        			// UnitInfo에 대상자 수가 지정된 수만큼 되었을 경우 UnitInfo를 FileSystem에 저장
                    // 후
                    // 새로운 UnitInfo 객체를 생성한다.
                    if (unit.size() >= receiversPerUnit) {
                        writeUnit(unit);
                        unit = new UnitInfo(messageID, unitID++);
                    }
        		}
                // UnitInfo에 저장된 대상자가 존재할 경우(마지막)
                if (unit.size() > 0) {
                    writeUnit(unit);
                }
            } 
            else {
            	
            	String[] columns =null;
            	HashMap<String, String[]> streamMap = new HashMap<String, String[]>();
            	String sqlWhere = "";
            	
                while (rs.next()) {
                    // 대상자 머지 정보를 담을 배열
                    columns = new String[columnCount + 1];
                    for (int i = 1; i <= columnCount; i++) {
                            String string = rs.getString(i); // String Type이 아닌 경우
                            if (bitSet.get(i)) { // String 일 경우 Charset에 따라 변환 한다.
                                
                            	//암호화 (이메일 위치값이 같으면)
//                            	if(i == EMAIL_i) {
//                            		//string = enc.getJasyptDecryptedString(ALGORITHM, KEYSTRING, string);
//                            		string = safeDbEnc.getDecrypt(string, "NOT_RNNO");
//                            		string = connection.fromDB(string);
//                            	}else {
//                            		string = connection.fromDB(string);
//                            	}
                            	
                                if(merge_map.size()!=0) {
        	                    		if(merge_map.get(i)!=null) {
        	                    			string = safeDbEnc.getDecrypt(string, "NOT_RNNO"); //이메일 복호화
        	                    	}
        	                    }
                            	
                            	
                            }
                            columns[i] = string;
//                        } 

                    } 

               
                    /* TODO
                	 *  1. column의 모든 정보를 저장한다. ( String ) 
                	 *  2. 데이터에서 ID 만 추출해서 SQL 을 준비한다. 
                	 */
                	streamMap.put(columns[ID_i], columns);
        			sqlWhere += "'" + columns[ID_i] + "' ,"; 

                }
                sqlWhere += "'Q'";
                
                
        		/*
            	 * TODO 
            	 * 1. DB SQL Call
            	 * 2. 결과값을 Hashmap 형태로 보관한다. (= resultMap) 
            	 */
            	HashMap<String, String> resultMap = null;
        		
        		if(message.mail_mkt_gb != null) {
        			MktCheck mktCheck = new MktCheck();
        			resultMap = mktCheck.MktCheckWhere(message.mail_mkt_gb,sqlWhere);
        		}
        		
        		/*  TODO 
            	 *  1. 기존 Reader 에서 비교하던 부분을  Hashmap(= StreamMap ) 에서 꺼내서 사용한다.
            	 *  2. DB 에서 마케팅 동의자에 대한 값이 있는 경우만 처리한다.  
            	 */
        		Set<String> set = streamMap.keySet();
        		
        		Iterator<String> it = set.iterator();
        		
        		while(it.hasNext()) { 
        			String id = it.next();
        			String[] fileds = streamMap.get(id);
        			
        			ReceiverInfo receiverInfo = new ReceiverInfo(fileds);
        			

        			if (message.mail_mkt_gb == null) {
                    	//System.out.println("마케팅 수신동의 미 체크 ");
                    	unit.add(receiverInfo);
                    	receiverCount++;
                    } else { 
                    	//-------------------------------------------------------------------------
                    	//마케팅 수신동의 체크
                    	//System.out.println("마케팅 수신동의  체크 ");
                    	
                    	String mktYn = resultMap.get(id);
                    	// 마케팅 미동의자에 대한 처리 
                    	if ( null == mktYn || "".equals(mktYn)) { 
                    		//continue;

                    		//미동의자 수집
                    		message.mkttList.add(id);
                    		message.mktIdIdx=ID_i;
                    	} 
                        unit.add(receiverInfo);
                        receiverCount++;
                       //-------------------------------------------------------------------------
                    }
        			// UnitInfo에 대상자 수가 지정된 수만큼 되었을 경우 UnitInfo를 FileSystem에 저장
                    // 후
                    // 새로운 UnitInfo 객체를 생성한다.
                    if (unit.size() >= receiversPerUnit) {
                        writeUnit(unit);
                        unit = new UnitInfo(messageID, unitID++);
                    }

        		}
        		
    			
                
                // UnitInfo에 저장된 대상자가 존재할 경우(마지막)
                if (unit.size() > 0) {

                    writeUnit(unit);

                }
            } // if(message.isTest) END ..........
        }
        catch (Exception ex) {
        	LOGGER.error(ex);
            exception = ex;
        }
        finally {
            if (rs != null) {
                try {
                    rs.close();
                }
                catch (SQLException ex) {
                	LOGGER.error(ex);
                }
                rs = null;
            }
            if (stmt != null) {
                try {
                    stmt.close();
                }
                catch (SQLException ex) {
                	LOGGER.error(ex);
                }
                stmt = null;
            }

            if (connection != null) {
                connection.close();
                connection = null;
            }
            if (conn != null) {
                try {
                    conn.close();
                }
                catch (SQLException e) {
                	LOGGER.error(e);
                    //e.printStackTrace();
                }
                conn = null;
            }
            /*
             * if (sendToUpdate != null) { sendToUpdate.close(); }
             */
        }

        if (exception != null) {
            //System.out.println("exception 발생 ==> " + exception.toString());
        	LOGGER.info("exception 발생 ==> " + exception.toString());

            if (exception instanceof FetchException) {
                throw (FetchException) exception;
            }
            else if (exception instanceof ClassNotFoundException) {
                String jdbcDriver = props.getProperty("jdbc.driver.name");
                String detail = "[" + messageID + "] JDBC Driver Not found : "
                    + jdbcDriver;
                throw new FetchException(detail, ErrorCode.INVALID_DB_INFO);
            }
            else if (exception instanceof SQLException) {
                String detail = "[" + messageID + "] " + exception.getMessage();
                throw new FetchException(detail,
                                         ErrorCode.MEMBER_DB_SQL_EXCEPTION);
            }
            else {
                String detail = "[" + messageID + "] " + exception.getMessage();
                throw new FetchException(detail, ErrorCode.MEMBER_FETCH_FAIL);
            }
        }
    }
}
