package messager.center.creator;

import java.sql.*;

import messager.center.db.*;

public class TaskStatusManager
{
    private final static String NOT_ERROR_CODE = "002";

    private final static String UPDATE_SQL = "UPDATE NEO_SUBTASK SET WORK_STATUS = ? WHERE TASK_NO = ? AND SUB_TASK_NO = ?";
    	//chk cskim 07.09.05
    public static boolean update(JdbcConnection connection, int taskNo,
                                 int subTaskNo, String statusCode)
        throws SQLException {
        String status = NOT_ERROR_CODE;
        boolean success = false;
        if (statusCode != null) {
            status = statusCode;
        }
        PreparedStatement pstmt = null;
        Exception exception = null;

        try {
            pstmt = connection.prepareStatement(UPDATE_SQL);
            pstmt.setString(1, status);
            pstmt.setInt(2, taskNo);
            pstmt.setInt(3, subTaskNo);
            if (pstmt.executeUpdate() > 0) {
                success = true;
            }
        }
        catch (Exception ex) {
            exception = ex;
        }
        finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                }
                catch (Exception ex) {
                }
                pstmt = null;
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
        return success;
    }
}