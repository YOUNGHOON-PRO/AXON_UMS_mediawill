package messager.center.result;

import java.sql.*;
import java.text.*;

import messager.center.db.*;

/**
 * 발송 완료된 메세지의 발송 완료 시간과 작업 상태를 발송 완료로 처리한다.
 * 발송 완료 시간과 작업 상태를 Neo_SubTask에 업데이트 한다.
 * @author Administrator
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class EndTimeManager
{
    /** 발송 완료 시간의 포맷 */
    private final static String TIME_FORMAT = "yyyyMMddHHmm";

    /** 업데트 */
    private final static String UPDATE_SQL = "UPDATE NEO_SUBTASK SET END_DT = ?, WORK_STATUS = '003' WHERE TASK_NO = ? AND SUB_TASK_NO = ? ";
    	//chk cskim 070905
    /** Connection */
    private JdbcConnection connection;

    /** PreparedStatement */
    private PreparedStatement pstmt;

    /**
     *  객체 생성
     * @throws Exception
     * */
    public EndTimeManager()
        throws Exception {
        connection = JdbcConnection.getWorkConnection();
    }

    /**
     * DB에 새로운 Connection를 맺고 PreparedStatement 객체를 생성한다.
     *
     * @throws Exception SQLException
     */
    public void open()
        throws Exception {
        connection.newConnection();
        pstmt = connection.prepareStatement(UPDATE_SQL);
    }

    /**
     * PreparedStatement 객체와 Connection 객체를 닫는다.
     */
    public void close() {
        if (pstmt != null) {
            try {
                pstmt.close();
            }
            catch (Exception ex) {
            }
            pstmt = null;
        }

        connection.close();
    }

    /**
     * 생성된 PreparedStatement 객체를 이용하여 업데이트를 실행한다.
     *
     * @param taskNo 업무번호
     * @param subTaskNo 보조 업무번호
     * @param endTime 발송 완료 일시를 나타내는 long (System.currentTimeMillis())
     * @return 업데이트가 성공하면 true
     * @throws Exception SQLException
     */
    public boolean update(int taskNo, int subTaskNo, long endTime)
        throws Exception {
        if (pstmt != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(TIME_FORMAT);
            String time = dateFormat.format(new java.util.Date(endTime));
            pstmt.setString(1, time);
            pstmt.setInt(2, taskNo);
            pstmt.setInt(3, subTaskNo);
            if (pstmt.executeUpdate() > 0) {
                return true;
            }
        }
        return false;
    }
}
