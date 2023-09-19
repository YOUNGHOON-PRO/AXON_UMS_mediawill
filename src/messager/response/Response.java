/*
* 클래스명: DBRecorder.java
* 버전정보: JDK 1.4.1
* 요약설명: 설정 파일
* 작성일자: 2003-04-04 하광범
 */

package messager.response;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import messager.response.util.utilLogWriter;

import java.text.*;

import messager.response.JdbcConnection;
import messager.response.Config_File_Receiver;
import messager.response.LogWriter;
//import com.util.NeoQueue_DirManager;

public class Response extends Thread
{
	
	//수신확인테이블 검색용쿼리
	//public static String RES_SEARCH_QUERY = "SELECT CUST_ID FROM NEO_RESPLOG WHERE TASK_NO=? and CUST_ID=?";
	//수신확인테이블 입력용쿼리
	
	
	public static String RES_INSERT_QUERY = " INSERT INTO NEO_RESPLOG						" + 
 										  "		  (DEPT_NO, CAMP_TY, CAMP_NO,				" +
										  "		   TASK_NO, SUB_TASK_NO, CUST_ID,			" +
										  "		   USER_ID, OPEN_DT, 						" +
										  "		   RESP_AMT,  TARGET_GRP_TY)				" +
										  "	VALUES(?,?,?,?,?,?,?,?,?,?)						" ;	
	
	//수신확인통계테이블 검색용쿼리
	//public static String RES_STATIC_SEARCH_QUERY = "SELECT COUNT(*) FROM NEO_RESPLOG WHERE TASK_NO=? and CUST_ID=? ";
	
	//수신확인통계테이블 수정용쿼리
	
	Config_File_Receiver config_File_Receiver;
	LogWriter logWriter;

	Vector vLogFileList;
	Vector vRollbackLogFileList;

	String startTime;

	public Response()
	{
		Config_File_Receiver CFR = new Config_File_Receiver();
		config_File_Receiver = CFR.getInstance();
		logWriter = new LogWriter();
		start();
	}

	public void run()
	{
		String RESPONSE_CONFIRM_FULL_PATH = "";
		String BACK_EXT = ".bak";
		
		while( true )
		{
			try
			{
				RESPONSE_CONFIRM_FULL_PATH = config_File_Receiver.RESPONSE_CONFIRM_FULL_PATH;
				
				File rcLog = new File(RESPONSE_CONFIRM_FULL_PATH);
				
				if(rcLog.exists()) {
					insertToDBFromResponseConfirmLog(rcLog, RESPONSE_CONFIRM_FULL_PATH , BACK_EXT);	
				}
				
				sleep(config_File_Receiver.RESPONSE_INSERT_PERIOD*1000*60);
				//sleep(config_File_Receiver.RESPONSE_INSERT_PERIOD*1000*3);
				//sleep(1000*60*5);
				
			}
			catch(Exception e) {
				logWriter.logWrite("Response", "run()", e);
			}
		}
	}
	
	//수신로그파일의 내용을 DB에 넣어준다.
	public static boolean insertToDBFromResponseConfirmLog(File rcLog, String RESPONSE_CONFIRM_FULL_PATH, String BACK_EXT)
	{
		
		SimpleDateFormat format1 = new SimpleDateFormat ( "yyyy-MM-dd HH:mm:ss");
		Date time = new Date();
		String time1 = format1.format(time);
		
		
		File reRcLog = new File(RESPONSE_CONFIRM_FULL_PATH+BACK_EXT);
		
		//수신로그파일의 이름을 바꿔준다.
		if(!(rcLog.renameTo(reRcLog)))
		{
			//그전에 남아있는 쓰레기가 있는 모양이니 지워버린다.
			if(reRcLog.exists()) {
				reRcLog.delete();
			}
//			System.out.println("전에있던 쓰레기 데이타를 지워버린다");
		}
		
		//수신로그의 파일을 읽어들인다.
		
		String tempStr= "";
		StringTokenizer st = null;
		
		String openDate = "";
		String temp = "";
		String campNo = "";
		String custId = "";
		String taskNo = "";
		String subtaskNo = "";
		String deptNo = "";
		String userId = "";
		String campType = "";
		String targetGrp = "";
		
		ArrayList mIDList = new ArrayList();
		Connection con_work =null;
		 JdbcConnection jc = null;
		 
		try {
			jc = JdbcConnection.getWorkConnection();
			 jc.newConnection();
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
         con_work = jc.getConnection();
         
         try {
			con_work.setAutoCommit(false);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
         
		//Connection con_work = DBConnection.getConnection();
		ResultSet rs =null;
		PreparedStatement pstmt = null;
		
		boolean return_value = false;
		
		BufferedReader br = null;
		
		try
		{
			if(reRcLog.exists()) {
				br = new BufferedReader(new FileReader(reRcLog));
				
				pstmt = con_work.prepareStatement(RES_INSERT_QUERY);
				
				while((tempStr = br.readLine())!=null)
				{
					
					if(tempStr.contains("``null") || tempStr.contains("null``") ) {
						
					}else {
						
							st = new StringTokenizer(tempStr,"``");

							//10개  (202111191535``000``test20``317``1``1``ADMIN``003``1``000)
							if(st.countTokens()==10) {
								while(st.hasMoreTokens())
								{
									openDate = st.nextToken();
									temp = st.nextToken();
									custId = st.nextToken();
									taskNo = st.nextToken();
									subtaskNo = st.nextToken();
									deptNo = st.nextToken();
									userId = st.nextToken();
									campNo = st.nextToken();
									campType = st.nextToken();
									targetGrp = st.nextToken();
								}
								
								if(("$:DEPT_NO:$").equals(deptNo) ||("$:dept_no:$").equals(deptNo)) {
									//System.out.println("비정상 데이터");
				
								}else {
									pstmt.clearParameters();
									pstmt.setInt(1, Integer.parseInt(deptNo) );
									pstmt.setString(2, campType );
									pstmt.setInt(3, Integer.parseInt(campNo) );
									pstmt.setInt(4, Integer.parseInt(taskNo)  );
									pstmt.setInt(5, Integer.parseInt(subtaskNo) );
									pstmt.setString(6, custId );
									pstmt.setString(7, userId );
									pstmt.setString(8, openDate );
									pstmt.setInt(9, 1 );
									pstmt.setString(10, "000"  );
									try {
										pstmt.executeUpdate();
										System.out.println("수신확인 : "+ taskNo + "  "+custId + "  "+ openDate);
									} catch (Exception e) {
										// TODO: handle exception
										//System.out.println("중복수신 : "+e.getMessage());
								}
									
										
									
									//수신확인 이력 관리 (respose 폴더에 2021_09_27.log 파일 생성)
									utilLogWriter.setLogFormat("Response", "수신확인 완료", "custId:"+custId, "taskNo:"+taskNo);
								}
							}
						}
				}
				
				if(br!=null) br.close();	
				//수신로그를 지워준다.
				if(reRcLog.exists()) {
					reRcLog.delete();
				}
							
				con_work.commit();
				return_value = true;
			}
			
		}catch(Exception e)
		{
			e.printStackTrace();
			//에러 로그에 남겨준다.
//			ErrorLogGenerator.setErrorLogFormat("LogFileManager", ReserveStatusCode.SQL_ERROR_TYPE,ReserveStatusCode.RESPONSE_LOG_FAIL_COMMENT,mID);
			return_value = false;
		}finally
		{
			try
			{
				if(br != null) br.close();
				if(rs != null) rs.close();
				if(pstmt != null) pstmt.close();
				if(con_work !=null) con_work.close();
			}catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		return return_value;
	}



	public static void main(String[] args) {
		System.out.println("Response Strart..");
		new Response();
	}


}
