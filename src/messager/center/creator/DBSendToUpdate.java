package messager.center.creator;

import java.sql.*;
import java.util.*;

import messager.center.db.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

class DBSendToUpdate
{
	private static final Logger LOGGER = LogManager.getLogger(DBSendToUpdate.class.getName());
	
    private JdbcConnection connection;
    private PreparedStatement pstmt;

    public DBSendToUpdate(Properties props, String tableName)
        throws Exception {
        Exception exception = null;

        try {
            String sql = createSQL(tableName);
            connection = JdbcConnection.getInstance(props);
            pstmt = connection.prepareStatement(sql);
        }
        catch (Exception ex) {
        	LOGGER.error(ex);
            exception = ex;
            ex.printStackTrace();
        }

        if (exception != null) {
            close();
            throw exception;
        }
    }

    private String createSQL(String tableName) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("UPDATE ").append(tableName).append(' ')
            .append("SET SEND_FG = '1' WHERE SEQ_NO = ?");	//chk cskim 07.09.04
        return buffer.toString();
    }

    public boolean update(String id) {
        boolean success = false;
        try {
            pstmt.setString(1, id);
            if (pstmt.executeUpdate() > 0) {
                success = true;
            }
        }
        catch (Exception ex) {
        	LOGGER.error(ex);
            //ex.printStackTrace();
        }
        return success;
    }

    public void close() {
        if (pstmt != null) {
            try {
                pstmt.close();
            }
            catch (SQLException ex) {LOGGER.error(ex);}
            pstmt = null;
        }

        if (connection != null) {
            connection.close();
            connection = null;
        }
    }
}
