package messager.mailsender.sendlog;

import java.io.*;
import java.sql.*;
import java.text.*;
import java.util.*;
import java.util.Date;
import java.util.regex.*;

import com.custinfo.safedata.*;
import messager.common.util.EncryptUtil;
import messager.mailsender.config.*;
import messager.mailsender.util.*;

public class Inserter_bk
{
    private String ClassName = "Inserter";

    /**************** DBHandle Values **************/
    private Connection myConn;
    private PreparedStatement pstmt = null;
    private PreparedStatement pstmt_test = null;
    private Statement stmt = null;
    private ResultSet result = null;
    private ResultSetMetaData rsmd = null;
    private static String strQuery;
    private static String strQuery_test;
    private static Hashtable DOMAIN_CODE;
    private static int insertedCount;

    /**
     * DB와 연결시도
     * @param dbDriver
     * @param dbUrl
     * @param user
     * @param passwd
     * @return ture : connected
     * @return false : connected fail..!
     */
    protected boolean makeConnection(String dbDriver, String dbUrl, String user,
                                     String passwd) {
        try {
            Class.forName(dbDriver);
            this.myConn = DriverManager.getConnection(dbUrl, user, passwd);
            System.out.println("SENDLOG INSERTER CONNECTION SUCCESS...");
            return true;
        }
        catch (Exception e) {
            System.err.println("connection Err");
            return false;
        }
    }

    protected void closeConnection() {
        try {
            myConn.close();
            System.out.println("SENDLOG INSERTER CONNECTION CLOSE ...");
        }
        catch (SQLException e) {
            LogWriter.writeException("Inserter", "closeConnection()",
                                     "closeConnection() 함수 체크", e);
        }
    }

    /**
     * DB로 부터 이메일에 해당하는 코드값을 불러온다.
     * @return true : DOMAIN_CODE 생성 완료
     * @return false : DOMAIN_CODE 생성 실패
     */
    public boolean getDomainCODE() {
        DOMAIN_CODE = new Hashtable();
        String SUB_CODE_NO = null;
        String CODE_NM = null;
        //String sqlStr = "select sub_code_no,code_nm from neo_code where code_no = 4";
        String sqlStr = "SELECT CD, CD_NM FROM NEO_CD WHERE CD_GRP = 'C003'";	//chk cskim 070905
        String columnName;

        int numOfRows;
        int numOfColumns;
        int rows = 0; // int cols = 0;
        try {
            stmt = myConn.createStatement();
            numOfRows = getRowCount(stmt, sqlStr);
            result = stmt.executeQuery(sqlStr);
            rsmd = result.getMetaData();
            numOfColumns = rsmd.getColumnCount();

            while (result.next()) {
                int cols = 0;
                while (cols < numOfColumns) {
                    columnName = rsmd.getColumnName(cols + 1);
                    if (columnName.equals("CD")) {
                        SUB_CODE_NO = result.getString(columnName);
                    }
                    else if (columnName.equals("CD_NM")) {
                        CODE_NM = result.getString(columnName);
                    }
                    cols++;
                }
                DOMAIN_CODE.put(CODE_NM, SUB_CODE_NO);
                rows++;
            }
            return true;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 로그를 입력할지 여부를 결정한다.
     * 현재 발송이 완료 되었는지를 확인 하기 위해 envelop에 파일 유무 확인
     * 
     * @param filePath : 파일 경로
     * @param fileName : 파일명
     * @return true : 발송 완료(인서트 작업 시작 가능)
     * @return false : 발송중 (인서트 작업 시작 할수 없음)
     */
    public boolean checkCondition(String filePath, String fileName) {
        File objectFile = null;
        try {
            objectFile = new File(filePath + "envelope/" + fileName);
            if (objectFile.exists()) {
                return false;
            }
            else {
                return true;
            }
        }
        catch (Exception e) {
            LogWriter.writeException("Inserter", "checkCondition()",
                                     objectFile.getAbsolutePath(), e);
            return false;
        }

    }

    /**
     * 로그 디렉토리에서 로그파일이 있는지를 검사 하고
     * 있다면 파일 리스트를 생성한다.
     * @return String [] : 파일 리스트
     */
    public String[] readFileName(String filePath)
        throws Exception {
        String[] fileList = null;
        fileList = new java.io.File(filePath + "sendlog/").list();
        return fileList;
    }

    /**
     * NEO_CODE의 테이블에 대한 쿼리 결과 받은 결과물의 행 수를 구한다.
     * @param stmt
     * @param query
     * @return  결과물의 행의 수
     */
    public int getRowCount(Statement stmt, String query) {
        try {
            result = stmt.executeQuery(query);
            int i = 0;
            while (result.next()) {
                i++;
            }
            return i;
        }
        catch (Exception e) {
            return -1;
        }
    }

    
    /**
     * 로그를 DB에 입력
     * /repository/transfer/sendlog/281-1^1 에 발송결과(성공/에러코드)를 DB에 기록한다
     * @param logFile : 로그파일명
     * @return true : 인서트 성공
     * @return false : 인서트 실패
     */
    public boolean insert(String logFile) {

        strQuery = new StringBuffer()
            .append("INSERT INTO NEO_SENDLOG(")
            .append("DEPT_NO")
            .append(",USER_ID")
            .append(",CAMP_TY")
            .append(",CAMP_NO")
            .append(",TASK_NO")
            .append(",SUB_TASK_NO")
            .append(",RETRY_CNT")
            .append(",SEND_DT")
            .append(",SEND_RCODE")
            .append(",RCODE_STEP1")
            .append(",RCODE_STEP2")
            .append(",RCODE_STEP3")
            .append(",SEND_MSG")
            .append(",DOMAIN_CD")
            .append(",CUST_DOMAIN")
            .append(",CUST_EM")
            .append(",CUST_NM")
            .append(",CUST_ID")
            .append(",YEAR")                
            .append(",MONTH")             
            .append(",DAY")                 
            .append(",HOUR")               
            .append(",TARGET_GRP_TY")
            .append(",BIZKEY)")
            .append(" VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)")
            .toString();
        		// chk cskim 070905
        strQuery_test = new StringBuffer()
            .append("INSERT INTO NEO_SENDTESTLOG(")
            .append("DEPT_NO")
            .append(",USER_ID")
            .append(",CAMP_TY")
            .append(",CAMP_NO")
            .append(",TASK_NO")
            .append(",SUB_TASK_NO")
            .append(",RETRY_CNT")
            .append(",SEND_DT")
            .append(",SEND_RCODE")
            .append(",RCODE_STEP1")
            .append(",RCODE_STEP2")
            .append(",RCODE_STEP3")
            .append(",SEND_MSG")
            .append(",DOMAIN_CD")
            .append(",CUST_EM")
            .append(",CUST_NM")
            .append(",CUST_ID")
            .append(",TARGET_GRP_TY")
            .append(",BIZKEY)")
            .append(" VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)")
            .toString();
		// chk cskim 070905

        try {
            this.pstmt = myConn.prepareStatement(strQuery);
            this.pstmt_test = myConn.prepareStatement(strQuery_test);
        }
        catch (SQLException e1) {
            e1.printStackTrace();
        }

        //DB 에러 처리 저장
        Vector dberrorVec = new Vector();

        try {
            //BufferedReader br = new BufferedReader(new FileReader(logFile));
            BufferedReader br = new BufferedReader(new java.io.InputStreamReader(new java.io.FileInputStream(logFile), "UTF-8"));
            String logStr = "";
            
            //복호화
    		String ALGORITHM = "PBEWithMD5AndDES";
    		String KEYSTRING = "ENDERSUMS";
    		EncryptUtil enc =  new EncryptUtil();
    		
    		CustInfoSafeData safeDbEnc = new CustInfoSafeData();
    		
            while ( (logStr = br.readLine()) != null) {
                try {
                    StringTokenizer st = new StringTokenizer(logStr, ""); //   => https://www.fileformat.info/info/unicode/char/0008/index.htm

                    boolean SENDTEST = new Boolean(st.nextToken()).booleanValue();

                    int ROWID = Integer.parseInt(st.nextToken());
                    int DEPT_NO = Integer.parseInt(st.nextToken());
                    String USER_ID = st.nextToken();
                    String CAMP_TY = st.nextToken();
                    int CAMP_NO = Integer.parseInt(st.nextToken());
                    int TASK_NO = Integer.parseInt(st.nextToken());
                    int SUB_TASK_NO = Integer.parseInt(st.nextToken());
                    int RETRY_CNT = Integer.parseInt(st.nextToken());

                    String SEND_DT = st.nextToken();

                    String YEAR = null;
                    String MONTH = null;
                    String DAY = null;
                    String HOUR = null;
                    if (SEND_DT.length() >= 10) {
                        YEAR = SEND_DT.substring(0, 4);
                        MONTH = SEND_DT.substring(4, 6);
                        DAY = SEND_DT.substring(6, 8);
                        HOUR = SEND_DT.substring(8, 10);
                    }

                    String SEND_RCODE = st.nextToken();
                    String RCODE_STEP1 = st.nextToken();
                    String RCODE_STEP2 = st.nextToken();
                    String RCODE_STEP3 = st.nextToken();
                    String SEND_MSG = st.nextToken();
                    if (SEND_MSG.length() > 250) {
                        SEND_MSG = SEND_MSG.substring(0, 250);
                    }
                    String CUST_EM = st.nextToken();
                    String CUST_EM2 = CUST_EM;
                    
                  //암호화
                    //CUST_EM = enc.getJasyptEncryptedString(ALGORITHM, KEYSTRING, CUST_EM);
                    CUST_EM = safeDbEnc.getEncrypt(CUST_EM, "NOT_RNNO");
                    
                    String DOMAIN_CD = replaceCode(CUST_EM2);
                    
                    String CUST_NM = st.nextToken();
                    String CUST_ID = st.nextToken();
                    String TARGET_GRP_TY = st.nextToken();
                    if(TARGET_GRP_TY.length() > 3) {
                        TARGET_GRP_TY = TARGET_GRP_TY.substring(0, 3);
                    }
                    String BIZKEY = st.nextToken();
                    
                    int nCount = -1;

                    //발송일때
                    if (!SENDTEST) {
                        pstmt.clearParameters();
                        pstmt.setInt(1, DEPT_NO);
                        pstmt.setString(2, USER_ID);
                        pstmt.setString(3, CAMP_TY);
                        pstmt.setInt(4, CAMP_NO);
                        pstmt.setInt(5, TASK_NO);
                        pstmt.setInt(6, SUB_TASK_NO);
                        pstmt.setInt(7, RETRY_CNT);
                        pstmt.setString(8, SEND_DT);
                        pstmt.setString(9, SEND_RCODE);
                        pstmt.setString(10, RCODE_STEP1);
                        pstmt.setString(11, RCODE_STEP2);
                        pstmt.setString(12, RCODE_STEP3);
                        pstmt.setString(13, SEND_MSG);
                        pstmt.setString(14, DOMAIN_CD);
                        pstmt.setString(15, splitDomain(CUST_EM2));
                        pstmt.setString(16, CUST_EM);
                        pstmt.setString(17, CUST_NM);
                        pstmt.setString(18, CUST_ID);
                        pstmt.setString(19, YEAR);
                        pstmt.setString(20, MONTH);
                        pstmt.setString(21, DAY);
                        pstmt.setString(22, HOUR);
                        pstmt.setString(23, TARGET_GRP_TY);
                        pstmt.setString(24, BIZKEY);

                        nCount = pstmt.executeUpdate();

                    }
                    else {
                        //테스트발송일경우
                        pstmt_test.clearParameters();
                        pstmt_test.setInt(1, DEPT_NO);
                        pstmt_test.setString(2, USER_ID);
                        pstmt_test.setString(3, CAMP_TY);
                        pstmt_test.setInt(4, CAMP_NO);
                        pstmt_test.setInt(5, TASK_NO);
                        pstmt_test.setInt(6, SUB_TASK_NO);
                        pstmt_test.setInt(7, RETRY_CNT);
                        pstmt_test.setString(8, SEND_DT);
                        pstmt_test.setString(9, SEND_RCODE);
                        pstmt_test.setString(10, RCODE_STEP1);
                        pstmt_test.setString(11, RCODE_STEP2);
                        pstmt_test.setString(12, RCODE_STEP3);
                        pstmt_test.setString(13, SEND_MSG);
                        pstmt_test.setString(14, DOMAIN_CD);
                        pstmt_test.setString(15, CUST_EM);
                        pstmt_test.setString(16, CUST_NM);
                        pstmt_test.setString(17, CUST_ID);
                        pstmt_test.setString(18, TARGET_GRP_TY);
                        pstmt_test.setString(19, BIZKEY);

                        nCount = pstmt_test.executeUpdate();
                    }

                    if (nCount != -1) {
                        insertedCount++;
                    }
                }
                catch (Exception x) {
                    System.out.println("INSERT NEO_SENDLOG : " + strQuery);
                    System.out.println("INSERT DATA : " + logStr);
                    x.printStackTrace();

                    int dbcode = 0;
                    if (x instanceof NoSuchElementException) {
                        LogWriter.writeException("Inserter", "insert()",
                                                 "항목중 비어 있는것이 있습니다.(" + logStr + ")", x);
                    }
                    else if (x instanceof SQLException) {
                        dbcode = ( (SQLException) x).getErrorCode();
                        LogWriter.writeException("Inserter", "insert()", " 에러 로그 확인", x);
                    }

                    StringTokenizer st = new StringTokenizer(logStr, "");
                    logStr = logStr + "^H^H" + dbcode;
                    dberrorVec.addElement(logStr);

                    continue;
                }
            } //end of while
            br.close();

            int dberrorSize = dberrorVec.size();
            if (dberrorSize > 0) {
                PrintWriter pw = null;
                try {
                    String workPath = ConfigLoader.TRANSFER_REPOSITORY_DIR;
                    File dirFile = new File(workPath, "sendloginsertfail");
                    if (!dirFile.exists()) {
                        if (!dirFile.mkdirs()) {
                            LogWriter.writeError("Inserter", "insert()",
                                                 "SENDLOG FAIL 로그 디렉토리 생성을 실패했습니다.", "repository/transfer 디렉토리 확인");
                        }
                    }
                    String errorfilename = new File(logFile).getName();
                    pw = new PrintWriter(
                        new FileWriter(new File(dirFile, errorfilename),
                                       true),
                        true);
                    for (int i = 0; i < dberrorSize; i++) {
                        String strData = (String) dberrorVec.elementAt(i);
                        pw.println(strData);
                    }
                }
                catch (Exception e) {
                    LogWriter.writeException("Inserter", "insert()",
                                             "입력 실패된 로그를 기록할수 없습니다.", e);
                }
                finally {
                    try {
                        pw.close();
                    }
                    catch (Exception e1) {}
                }
            }
            return true;
        }
        catch (Exception e) {
            LogWriter.writeException("Inserter", "insert()", "로그 파일을 읽는데 실패 했습니다.", e);
            return false;
        }
        finally {
            try {
                this.pstmt.close();
                this.pstmt_test.close();
            }
            catch (Exception e) {}
        }

    }
    
    /**
     * 수신확인 로그 적재
     */
    public void insert_AR_RESP(Connection conn) {
    	
    
    	PreparedStatement pstmt = null;
    	ResultSet result = null;
    	String open_day ="";
    	
    	 
		String strQuery1 = new StringBuffer()
				.append("SELECT NVL(MAX(OPEN_DAY),'00000000') AS var_open_day FROM NEO_AR_RESPLOG ").toString();
    	
		String strQuery2 = new StringBuffer()
				.append("DELETE FROM NEO_AR_RESPLOG WHERE OPEN_DAY >= ? ").toString();
		
		String strQuery3 = new StringBuffer()
				.append("INSERT INTO NEO_AR_RESPLOG (DEPT_NO,USER_ID,CAMP_TY,CAMP_NO,TASK_NO,SUB_TASK_NO,OPEN_DT,BLOCKED_YN,TARGET_GRP_TY,RESP_AR_AMT, OPEN_DAY ,DAYOFWEEK ) ")
				.append("SELECT A.DEPT_NO,A.USER_ID,A.CAMP_TY,A.CAMP_NO,A.TASK_NO,A.SUB_TASK_NO,A.OPEN_DT,A.BLOCKED_YN,A.TARGET_GRP_TY,SUM(RESP_AMT),MAX(SUBSTR(A.OPEN_DT ,1, 8)) , MAX(C.CD) ") 
				.append("FROM NEO_RESPLOG A ")
				.append("LEFT OUTER JOIN NEO_WEEKCD C  ON SUBSTR(A.OPEN_DT,1,8) = C.YMD ")
				.append("WHERE A.OPEN_DT >= ? ||'0000' ")
				.append("GROUP BY A.DEPT_NO,A.USER_ID,A.CAMP_TY,A.CAMP_NO,A.TASK_NO,A.SUB_TASK_NO,A.OPEN_DT,A.BLOCKED_YN,A.TARGET_GRP_TY ").toString();

        try {
        	pstmt = conn.prepareStatement(strQuery1);
        	result = pstmt.executeQuery();
            
            while(result.next()) {
            	open_day = result.getString("var_open_day");
            }
            //System.out.println("open_day : "  +open_day);
            
            result.close();
            pstmt.close();
            
        	pstmt = conn.prepareStatement(strQuery2);
        	pstmt.setString(1, open_day);
        	int rst = pstmt.executeUpdate();
                        
            pstmt.close();
            
        	pstmt = conn.prepareStatement(strQuery3);
        	pstmt.setString(1, open_day);
        	int rst1 = pstmt.executeUpdate();
        	//System.out.println("[AR_RESP] Delete 건수 : " + rst +", Insert 건수 : " + rst1);
                        
            pstmt.close();
 			
		} catch (SQLException e1) {
			e1.printStackTrace();
		}finally {
	        try{
	            if(result !=null)result.close();
	            if(pstmt !=null)pstmt.close();
	        }
	        catch(Exception e){}
		}
    }
    
    
    /**
     * 발송 로그 적재
     */
    public void insert_AR_SEND(Connection conn) {
    	
    	PreparedStatement pstmt = null;
    	ResultSet result = null;
    	String var_send_day ="";
    	
		String strQuery1 = new StringBuffer()
				.append("SELECT NVL(MAX(SEND_DAY),'00000000') AS var_send_day   FROM NEO_AR_SENDLOG ").toString();
    	
		String strQuery2 = new StringBuffer()
				.append("DELETE FROM NEO_AR_SENDLOG WHERE SEND_DAY >= ? ").toString();
		
		String strQuery3 = new StringBuffer()
				.append("INSERT INTO NEO_AR_SENDLOG (DEPT_NO,USER_ID,CAMP_TY,CAMP_NO,TASK_NO,SUB_TASK_NO,RETRY_CNT,SEND_DT, SEND_RCODE,RCODE_STEP1,RCODE_STEP2,RCODE_STEP3,DOMAIN_CD,TARGET_GRP_TY,SEND_AR_AMT, SEND_DAY , DAYOFWEEK ) ")  
				.append("SELECT   /*+ LEADING(B) USE_HASH(B A) INDEX(A IDX_NEO_SENDLOG_02) */ A.DEPT_NO ")
				.append("        ,A.USER_ID ")
				.append("        ,A.CAMP_TY ")
				.append("        ,A.CAMP_NO ")
				.append("        ,A.TASK_NO ")
				.append("        ,A.SUB_TASK_NO ") 
				.append("        ,A.RETRY_CNT ")
				.append("        ,A.SEND_DT ")
				.append("        ,A.SEND_RCODE ") 
				.append("        ,A.RCODE_STEP1 ")
				.append("        ,A.RCODE_STEP2 ")
				.append("        ,A.RCODE_STEP3 ")
				.append("        ,A.DOMAIN_CD  ")
				.append("        ,A.TARGET_GRP_TY ")
				.append("        ,SUM(SEND_AMT) ")
				.append("        ,MAX(SUBSTR(A.SEND_DT ,1, 8)) ")  
				.append("        ,MAX(C.CD) ")
				.append("FROM NEO_SENDLOG A ")
				.append("INNER JOIN   (SELECT T1.TASK_NO ") 
				.append("                 , T1.SUB_TASK_NO ")
				.append("                 , T1.CUST_ID ")
				.append("                 , MAX(T1.RETRY_CNT) AS MAX_RETRY_CNT ") 
				.append("               FROM (SELECT /*+ LEADING(B) USE_HASH(B A) INDEX(A IDX_NEO_SENDLOG_02) */ DISTINCT TASK_NO FROM NEO_SENDLOG WHERE SEND_DT >= ? || '0000' ) T2 ")  
				.append("              INNER JOIN NEO_SENDLOG T1  ON T2.TASK_NO = T1.TASK_NO  ")
				.append("              GROUP BY T1.TASK_NO, T1.SUB_TASK_NO, T1.CUST_ID) B ")
				.append("        ON  A.RETRY_CNT = B.MAX_RETRY_CNT AND A.TASK_NO = B.TASK_NO AND A.SUB_TASK_NO = B.SUB_TASK_NO AND A.CUST_ID = B.CUST_ID ") 
				.append("LEFT OUTER JOIN NEO_WEEKCD C  ON SUBSTR(A.SEND_DT,1,8) = C.YMD ")
				.append("WHERE A.SEND_DT >= ? || '0000' ")
				.append("GROUP BY A.DEPT_NO ")
				.append("        ,A.USER_ID ")
				.append("        ,A.CAMP_TY ")
				.append("        ,A.CAMP_NO ")
				.append("        ,A.TASK_NO ")
				.append("        ,A.SUB_TASK_NO ") 
				.append("        ,A.RETRY_CNT ")
				.append("        ,A.SEND_DT ")
				.append("        ,A.SEND_RCODE ") 
				.append("        ,A.RCODE_STEP1 ")
				.append("        ,A.RCODE_STEP2 ")
				.append("        ,A.RCODE_STEP3 ")
				.append("        ,A.DOMAIN_CD ")
				.append("        ,A.TARGET_GRP_TY ").toString();

        try {
        	pstmt = conn.prepareStatement(strQuery1);
        	result = pstmt.executeQuery();
            
            while(result.next()) {
            	var_send_day = result.getString("var_send_day");
            }
            result.close();
            pstmt.close();
    
            
        	pstmt = conn.prepareStatement(strQuery2);
        	pstmt.setString(1, var_send_day);
        	int rst = pstmt.executeUpdate();
                        
            pstmt.close();
            
        	pstmt = conn.prepareStatement(strQuery3);
        	pstmt.setString(1, var_send_day);
        	pstmt.setString(2, var_send_day);
        	int rst1 = pstmt.executeUpdate();
        	//System.out.println("[AR_SEND] Delete 건수 : " + rst +", Insert 건수 : " + rst1);
                        
            pstmt.close();
 			
		} catch (SQLException e1) {
			e1.printStackTrace();
		}finally {
	        try{
	            if(result !=null)result.close();
	            if(pstmt !=null)pstmt.close();
	        }
	        catch(Exception e){}
		}
    }

    
    
    
    /**
     * 발송도메인 로그 적재
     */
    public void insert_AR_DOMAIN(Connection conn) {
    	
    	PreparedStatement pstmt = null;
    	ResultSet result = null;
    	String var_send_day ="";
    	
    	 
		String strQuery1 = new StringBuffer()
				.append("SELECT NVL(MAX(SEND_DAY),'00000000') AS var_send_day FROM NEO_AR_DOMAINLOG ").toString();
    	
		String strQuery2 = new StringBuffer()
				.append("DELETE FROM NEO_AR_DOMAINLOG WHERE SEND_DAY >= ? ").toString();
		
		String strQuery3 = new StringBuffer()
				.append("INSERT INTO NEO_AR_DOMAINLOG (TASK_NO,SUB_TASK_NO,SEND_RCODE,RCODE_STEP1,RCODE_STEP2,RCODE_STEP3,CUST_DOMAIN,SEND_AR_AMT,SEND_DAY) ")				       
				.append("SELECT    T3.TASK_NO ")
				.append("        , T3.SUB_TASK_NO ") 
				.append("        , T3.SEND_RCODE ")
				.append("        , T3.RCODE_STEP1 ")
				.append("        , T3.RCODE_STEP2 ")
				.append("        , T3.RCODE_STEP3 ")
				.append("        , NVL(T2.CUST_DOMAIN , 'ETC') AS CUST_DOMAIN ") 
				.append("        , SUM(T3.SEND_AMT) AS SEND_AMT ")
				.append("        , MAX(T3.YEAR||T3.MONTH||T3.DAY) AS YMD ")  
				.append("  FROM (SELECT A.TASK_NO ")
				.append("             , A.SUB_TASK_NO ")
				.append("             , A.CUST_EM ")
				.append("             , A.CUST_ID ")
				.append("             , A.CUST_DOMAIN ")
				.append("             , MAX(RETRY_CNT) AS MAX_RETRY_CNT ") 
				.append("         FROM NEO_SENDLOG A ")
				.append("         WHERE A.SEND_DT >= ? ||'0000' ")  
				.append("         GROUP BY  A.TASK_NO ")
				.append("                 , A.SUB_TASK_NO ")  
				.append("                 , A.CUST_EM ")
				.append("                 , A.CUST_ID ")
				.append("                 , A.CUST_DOMAIN ) T1 ") 
				.append("  LEFT OUTER JOIN  (SELECT A.TASK_NO ")
				.append("                        , A.SUB_TASK_NO ") 
				.append("                        , A.CUST_DOMAIN ")
				.append("                        , ROW_NUMBER() OVER(PARTITION BY A.TASK_NO, A.SUB_TASK_NO, A.CUST_DOMAIN ORDER BY A.SEND_AMT DESC) AS ROW_SEQ ") 
				.append("                      FROM (SELECT TASK_NO ")
				.append("                                , SUB_TASK_NO ") 
				.append("                                , CUST_DOMAIN  ")
				.append("                                , SUM(SEND_AMT) AS SEND_AMT ")  
				.append("                              FROM NEO_SENDLOG ")
				.append("                             WHERE SEND_DT >= ? ||'0000' ")  
				.append("                               AND RETRY_CNT = 0 ")
				.append("                             GROUP BY TASK_NO, SUB_TASK_NO, CUST_DOMAIN  ) A ) T2 ")  
				.append("               ON T1.TASK_NO = T2.TASK_NO AND T1.SUB_TASK_NO = T2.SUB_TASK_NO AND T1.CUST_DOMAIN = T2.CUST_DOMAIN AND T2.ROW_SEQ <= 19 ") 
				.append("   INNER JOIN NEO_SENDLOG T3 ")
				.append("           ON T1.TASK_NO = T3.TASK_NO ") 
				.append("          AND T1.SUB_TASK_NO = T3.SUB_TASK_NO ") 
				.append("          AND T1.CUST_EM = T3.CUST_EM ")
				.append("          AND T1.CUST_ID = T3.CUST_ID ")
				.append("          AND T1.CUST_DOMAIN = T3. CUST_DOMAIN ") 
				.append("          AND T1.MAX_RETRY_CNT = T3.RETRY_CNT ")
				.append("GROUP BY  T3.TASK_NO ")
				.append("        , T3.SUB_TASK_NO ") 
				.append("        , T3.SEND_RCODE ")
				.append("        , T3.RCODE_STEP1 ")
				.append("        , T3.RCODE_STEP2 ")
				.append("        , T3.RCODE_STEP3 ")
				.append("	,  NVL(T2.CUST_DOMAIN , 'ETC') ").toString();

        try {
        	pstmt = conn.prepareStatement(strQuery1);
        	result = pstmt.executeQuery();
            
            while(result.next()) {
            	var_send_day = result.getString("var_send_day");
            }
            result.close();
            pstmt.close();
    
            
        	pstmt = conn.prepareStatement(strQuery2);
        	pstmt.setString(1, var_send_day);
        	int rst = pstmt.executeUpdate();
                        
            pstmt.close();
            
        	pstmt = conn.prepareStatement(strQuery3);
        	pstmt.setString(1, var_send_day);
        	pstmt.setString(2, var_send_day);
        	int rst1 = pstmt.executeUpdate();
        	//System.out.println("[AR_DOMAIN] Delete 건수 : " + rst +", Insert 건수 : " + rst1);
                        
            pstmt.close();
 			
		} catch (SQLException e1) {
			e1.printStackTrace();
		}finally {
	        try{
	            if(result !=null)result.close();
	            if(pstmt !=null)pstmt.close();
	        }
	        catch(Exception e){}
		}
    }
    

    /**
     * 수신링크 로그 적재
     */
    public void insert_AR_LINK(Connection conn) {
    	
    	PreparedStatement pstmt = null;
    	ResultSet result = null;
    	String click_day ="";
    	
		String strQuery1 = new StringBuffer()
				.append("SELECT NVL(MAX(CLICK_DAY),'00000000') AS var_click_day FROM NEO_AR_LINKLOG ").toString();
    	
		String strQuery2 = new StringBuffer()
				.append("DELETE FROM NEO_AR_LINKLOG WHERE CLICK_DAY >= ? ").toString();
		
		String strQuery3 = new StringBuffer()
				.append("INSERT INTO NEO_AR_LINKLOG (DEPT_NO, USER_ID, CAMP_TY, CAMP_NO, TASK_NO, SUB_TASK_NO, LINK_NO, CLICK_DT, TARGET_GRP_TY, CLICK_AR_AMT, VALID_AR_AMT , CLICK_DAY , DAYOFWEEK) ") 
				.append("SELECT A.DEPT_NO,A.USER_ID,A.CAMP_TY,A.CAMP_NO,A.TASK_NO,A.SUB_TASK_NO,A.LINK_NO,A.CLICK_DT,A.TARGET_GRP_TY,SUM(CLICK_AMT),SUM(VALID_AMT) , MAX(SUBSTR(CLICK_DT ,1, 8)) , MAX(C.CD) ") 
				.append("FROM NEO_LINKLOG A ")
				.append("LEFT OUTER JOIN (SELECT TASK_NO,SUB_TASK_NO,CUST_ID, MIN(LINK_NO) AS LINK_NO, 1 AS VALID_AMT ")
				.append("                   FROM NEO_LINKLOG ")
				.append("                  WHERE CLICK_DT >= ? || '0000' ")
				.append("                  GROUP BY TASK_NO,SUB_TASK_NO,CUST_ID) B  ")
				.append("             ON A.TASK_NO = B.TASK_NO AND A.SUB_TASK_NO = B.SUB_TASK_NO AND A.CUST_ID = B.CUST_ID AND A.LINK_NO = B.LINK_NO ")
				.append("LEFT OUTER JOIN NEO_WEEKCD C  ON SUBSTR(A.CLICK_DT,1,8) = C.YMD ")
				.append("WHERE  A.CLICK_DT >= ? || '0000'  ")
				.append("GROUP BY A.DEPT_NO,A.USER_ID,A.CAMP_TY,A.CAMP_NO,A.TASK_NO,A.SUB_TASK_NO,A.LINK_NO,A.CLICK_DT,A.TARGET_GRP_TY " ).toString();

        try {
        	pstmt = conn.prepareStatement(strQuery1);
        	result = pstmt.executeQuery();
            
            while(result.next()) {
            	click_day = result.getString("var_click_day");
            }
            //System.out.println("open_day : "  +open_day);
            
            result.close();
            pstmt.close();
    
            
        	pstmt = conn.prepareStatement(strQuery2);
        	pstmt.setString(1, click_day);
        	int rst = pstmt.executeUpdate();
                        
            pstmt.close();
            
        	pstmt = conn.prepareStatement(strQuery3);
        	pstmt.setString(1, click_day);
        	pstmt.setString(2, click_day);
        	int rst1 = pstmt.executeUpdate();
        	//System.out.println("[AR_LINK] Delete 건수 : " + rst +", Insert 건수 : " + rst1);
                        
            pstmt.close();
 			
		} catch (SQLException e1) {
			e1.printStackTrace();
		}finally {
	        try{
	            if(result !=null)result.close();
	            if(pstmt !=null)pstmt.close();
	        }
	        catch(Exception e){}
		}
    }
    
    
    /**
     * 도메인을 코드값으로 변경
     * @param domain : 대상 고객의 도메인
     * @return 해당 도메인에 맞는 코드값을 리턴한다.
     */
    public String replaceCode(String domain) {
        String code = "999";
        for (Enumeration e = DOMAIN_CODE.keys(); e.hasMoreElements(); ) {
            String idx = e.nextElement().toString();
            Pattern pattern = Pattern.compile(idx.toLowerCase());
            Matcher match = pattern.matcher(domain.toLowerCase());
            if (match.find()) {
                try {
                    //code = Integer.parseInt( (String) DOMAIN_CODE.get(idx));
                    code = (String) DOMAIN_CODE.get(idx);
                    break;
                }
                catch (Exception ex) {
                    LogWriter.writeException(ClassName, "replaceCode()",
                                             "도메인을 해당 코드로 치환하는데 실패 했습니다.", ex);
                }
            }
            else {
                code = "999";
            }
        }
        return code;
    }

    public String splitDomain(String eml) {
        String domain = "";
        if (eml != null) {
            domain = eml.substring(eml.lastIndexOf("@") + 1);
        }
        return domain;
    }

    public static void main(String[] args) {
        Inserter_bk ln = new Inserter_bk();
        ConfigLoader.load();
        String[] FileList;
        boolean domainCached = false;

        StringBuffer sb = new StringBuffer();

        String dbDriver = ConfigLoader.INSERT_DBDRIVER;
        String dbUrl = ConfigLoader.INSERT_DBURL;
        String user = ConfigLoader.INSERT_USER;
        String passwd = ConfigLoader.INSERT_PASSWD;
        String filePath;

        filePath = sb.append(ConfigLoader.TRANSFER_ROOT_DIR)
            .append(ConfigLoader.TRANSFER_REPOSITORY_DIR).toString();
        SimpleDateFormat sdt = new SimpleDateFormat("yyyy-MM-dd-hh-mm");
        

        while (true) {
            try {
            	System.out.println("sendlog 시작");
                FileList = ln.readFileName(filePath); // 파일 리스트를 구한다.
                //System.out.println("[ " + sdt.format(new Date()) + " ]");
                if (FileList.length > 0) {
                    if (!ln.makeConnection(dbDriver, dbUrl, user, passwd)) {
                        System.out.println(
                            "After Insert period, try to connect to database..");
                    }
                    if (!domainCached) {
                        if (ln.getDomainCODE()) {
                            System.out.println("Successfully get a Domain Code");
                            domainCached = true;
                        }
                    }

                    for (int i = 0; i < FileList.length; i++) {
                        String fileName = FileList[i];
                        boolean isCheck = ln.checkCondition(filePath, fileName);
                        if (isCheck) {
                            sb = new StringBuffer();
                            fileName = sb.append(filePath).append("sendlog/").append(fileName).toString();
                            System.out.println("Start log Insert [" + fileName + "]");
                            System.out.flush();
                            boolean isInsert = ln.insert(fileName);

                            if (isInsert) {
                                //if (ln.insert(fileName)) {
                                System.out.println(fileName + " is done[" + insertedCount +
                                    " lines]");
                                insertedCount = 0;
                                FileManager.deleteLogFile(fileName);
                            }
                        }
                    }
                    ln.closeConnection();
                }
                FileList = null;
                

                //통계분석 데이터 이관 작업 시작-------------------------------------------------------------------- 
                Inserter_bk inSert = new Inserter_bk();
            	Connection conn = null;

            	try {
                	Class.forName(dbDriver);
                	conn = DriverManager.getConnection(dbUrl, user, passwd);
                	conn.setAutoCommit(true);
                	
                	inSert.insert_AR_RESP(conn);
                	inSert.insert_AR_LINK(conn);
                	inSert.insert_AR_SEND(conn);
                	inSert.insert_AR_DOMAIN(conn);
                	
        		} catch (SQLException | ClassNotFoundException e) {
        			// TODO Auto-generated catch block
        			e.printStackTrace();
        		}finally {
        			conn.close();
        		}
            	//통계분석 데이터 이관 작업 끝--------------------------------------------------------------------
            	
            }
            catch (NullPointerException e) {
//				LogWriter.writeException("Inserter","main()","입력할 대상이 없습니다.",e);
            }
            catch (Exception ex) {
                LogWriter.writeException("Inserter", "main()", "에러내용 확인", ex);
            }

            try {
            	System.out.println("sendlog 슬립 " + ConfigLoader.INSERT_PERIOD * 1000);
                Thread.sleep(ConfigLoader.INSERT_PERIOD * 1000);
            }
            catch (InterruptedException e) {
                LogWriter.writeException("Inserter", "main()", "외부로 부터 인터럽트 신호를 받았습니다.",
                                         e);
            }
            
        }
        
        
    }

    public static void shutdown() {
        System.out.println("SendLog shutdown.");
        System.exit(0);
    }
    

    
}
