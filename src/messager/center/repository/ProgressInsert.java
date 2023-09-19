package messager.center.repository;

import java.sql.*;
import java.util.*;

import com.custinfo.safedata.CustInfoSafeData;

import messager.center.config.*;
import messager.center.creator.FetchException;
import messager.common.util.EncryptUtil;

public class ProgressInsert
    extends Thread
{
    private String MNAME = "ProgressInsert";

    /**************** DBHandle Values **************/
    private Connection myConn = null;
    private PreparedStatement pstmt_send_progress = null;
    private String strSendProgressQuery = null;

    private String dbDriver = null;
    private String dbUrl = null;
    private String user = null;
    private String passwd = null;

    private static Vector progVec = null;

    /**
     * 생성자 구현
     */
    public ProgressInsert() {
    	
        //복호화
		String ALGORITHM = "PBEWithMD5AndDES";
		String KEYSTRING = "ENDERSUMS";
		//EncryptUtil enc =  new EncryptUtil();
		CustInfoSafeData CustInfo = new CustInfoSafeData();
    	
        Properties props = ConfigLoader.getDBProperties();
        this.dbDriver = (String) props.get("jdbc.driver.name");
        this.dbUrl = (String) props.get("jdbc.url");
        this.user = (String) props.get("db.user");
        
        //복호화
        if("Y".equals(props.get("db.password.yn"))) {
        	String db_password;
			try {
				db_password = CustInfo.getDecrypt((String) props.get("db.password"), KEYSTRING);
				this.passwd = db_password;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }else {
        	   this.passwd = (String) props.get("db.password");

        }
     
        //저장소 초기화
        progVec = new Vector();

        //쿼리문 초기화
        strSendProgressQuery = new StringBuffer()
            .append("UPDATE NEO_SEND_PROGRESS SET ")
            .append("CUR_SEND_CNT = CUR_SEND_CNT+?, ")
            .append("SUC_SEND_CNT = SUC_SEND_CNT+?, ")
            .append("FAIL_SEND_CNT = FAIL_SEND_CNT+?")
            .append(" WHERE TASK_NO = ?")
            .append("   AND SUB_TASK_NO = ?")	// chk cskim 07.09.05
            .toString();
    }

    /**
     * DB와 연결시도
     * @return ture : connected
     * @return false : connected fail..!
     */
    protected boolean makeConnection() {
        try {
            Class.forName(dbDriver);
            this.myConn = DriverManager.getConnection(dbUrl, user, passwd);
            this.pstmt_send_progress = myConn.prepareStatement(strSendProgressQuery);
            System.out.println("SEND PROGRESS UPDATE DB CONNECTION SUCCESS ...");
            return true;
        }
        catch (Exception e) {
            System.out.println("SEND PROGRESS UPDATE DB CONNECTION FAIL ..." + e.toString());
            return false;
        }
    }

    protected void closeConnection() {
        try {
            myConn.close();
        }
        catch (SQLException e) {
            System.out.println("SEND PROGRESS DB CONNECTION CLOSE FAIL ..." + e.toString());
        }
    }

    /**
     * 발송 진행 사항 정보 저장
     */
    public static void setData(int[] data) {
        synchronized (progVec) {
            progVec.addElement(data);
        }
    }

    /**
     * 발송 진행 사항 정보 추출
     */
    public static Vector getData() {
        synchronized (progVec) {
            Vector dataVec = new Vector();
            for (int i = 0; i < progVec.size(); i++) {
                dataVec.addElement(progVec.remove(i));
            }
            return dataVec;
        }
    }

    /**
     * 발송 진행 사항 DB UPDATE
     */
    public void update() {

        Vector dataVec = getData();

        int[] temp = null;
        for (int i = 0; i < dataVec.size(); i++) {

            try {

                temp = (int[]) dataVec.elementAt(i);

                int task_no = temp[0];
                int sub_task_no = temp[1];
                int cur_send_cnt = temp[2];
                int suc_send_cnt = temp[3];
                int fail_send_cnt = temp[4];

                System.out.println("====== K-Y-H =====");
                System.out.println("task_no : "+task_no);
                System.out.println("cur_send_cnt : "+cur_send_cnt);
                System.out.println("suc_send_cnt : "+suc_send_cnt);
                System.out.println("fail_send_cnt : "+fail_send_cnt);
                System.out.println("====== K-Y-H =====");
                
                //발송 진행률 업데이트
                pstmt_send_progress.setInt(1, cur_send_cnt); //현재 발송수
                pstmt_send_progress.setInt(2, suc_send_cnt); //현재 성공수
                pstmt_send_progress.setInt(3, fail_send_cnt); //현재 실패수
                pstmt_send_progress.setInt(4, task_no); //TASK_NO
                pstmt_send_progress.setInt(5, sub_task_no); //SUB_TASK_NO
                pstmt_send_progress.executeUpdate();

            }
            catch (Exception x) {
                System.out.println("SEND PROGRESS UPDATE FAIL ..." + x.toString());
            }

        }
    }

    public void run() {

        if (!makeConnection()) {
            return;
        }

        while (true) {
            try {

                //DB CONNECTION 이 정상인지 확인한다.
                if (myConn.isClosed()) {
                    System.out.println("SEND PROGRESS DB CONNECTION CLOSED, DB CONNECTION CONNECT TRY ..... ");
                    //DB CONNECTION 재설정
                    makeConnection();
                }

                //진행 사항 UPDATE
                update();

            }
            catch (NullPointerException e) {
                System.out.println("SEND PROGRESS UPDATE FAIL ..... " + e.toString());
            }
            catch (Exception ex) {
                System.out.println("SEND PROGRESS UPDATE FAIL ..... " + ex.toString());
            }

            try {
                Thread.sleep(1000);
            }
            catch (InterruptedException e) {
                System.out.println("SEND PROGRESS THREAD SLEEP FAIL ..... " + e.toString());
            }
        }
    }
}
