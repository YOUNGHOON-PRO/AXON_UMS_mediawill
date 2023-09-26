package messager.center.db;

import java.io.*;
import java.sql.*;
import java.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * JDBC의 Connection 객체 Wrapper 클래스로 DB의 charset과 컨텐츠의 charset으로 String를 변환를
 * 제공한다..
 */
public class LegacyConnection
{
	
	private static final Logger LOGGER = LogManager.getLogger(LegacyConnection.class.getName());
	
    /**
     * LegacyConnection Instance 를 얻는다.
     *
     * @param props
     *            DB의 접속 정보가 저장된 Properties 객체
     * @param Connection
     *            객체를 포함하는 LegacyConnection 객체
     * @exception ClassNotFoundExcetion,
     *                SQLException
     */
    public static LegacyConnection getInstance(Properties props)
        throws ClassNotFoundException, SQLException {
        Properties dbProps = (Properties) props.clone();
        String db_char_set = (String) dbProps.remove("db_char_set");
        String db_char_set_nm = (String) dbProps.remove("db_char_set_nm");
        String driverName = (String) dbProps.remove("jdbc.driver.name");
        String url = (String) dbProps.remove("jdbc.url");
        //JDBC driver 로드
        Class.forName(driverName);

        //LegacyConnection객체 생성
        LegacyConnection connection
            = new LegacyConnection(url, db_char_set, db_char_set_nm, dbProps);

        // DB에 접속한다.
        connection.newConnection();
        return connection;
    }

    private String jdbcUrl; //JDBC의 URL

    private Properties properties; //DB의 기타 접속정보(user, password)

    private Connection connection; //Connection객체

    private boolean decoding; //DB의 charset과 컨텐츠의 charset이 다를 경우 true

    private String db_char_set;
    private String db_char_set_nm;

    /**
     * LegacyConnection 객체를 생성한다.
     *
     * @param url
     *            JDBC의 URL
     * @param db_char_set
     *            DB의 charset 코드
     * @param db_char_set_nm
     *            DB의 charset
     * @param props
     *            기타 접속 정보(user, password)
     */
    private LegacyConnection(String url, String db_char_set, String db_char_set_nm,
                             Properties props)
        throws SQLException {
        jdbcUrl = url;
        this.db_char_set = db_char_set;
        this.db_char_set_nm = db_char_set_nm;
        properties = props;
        if (db_char_set != null && db_char_set_nm != null
            && !db_char_set.equals("000")) { //인코딩 대상
            decoding = true;
        }
    }

    /**
     * DB에 새로운 새로운 접속을 한다 <br>
     * 이미 연결된 상태이면 기존 접속은 닫는다.
     */
    public void newConnection()
        throws SQLException {
        close(); //이미 생성된 Connection 객체를 닫는다.
        //새로운 Connection 객체를 생성
        connection = DriverManager.getConnection(jdbcUrl, properties);
    }

    /**
     * Connection 이 이루어 지지 않았을 경우 새로운 접속을 한다.
     */
    public void openConnection()
        throws SQLException {
        if (connection == null) {
            connection = DriverManager.getConnection(jdbcUrl, properties);
        }
    }

    /**
     * Connection객체를 얻는다.
     *
     * @return Connection
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * PreparedStatement 객체를 생성한다.
     */
    public PreparedStatement prepareStatement(String sql)
        throws SQLException {
        if (connection == null) {
            throw new NullPointerException();
        }
        return connection.prepareStatement(sql);
    }

    /**
     * Statement 객체를 얻는다.
     */
    public Statement createStatement()
        throws SQLException {
        if (connection == null) {
            throw new NullPointerException();
        }
        return connection.createStatement();
    }

    /**
     * auto commit 모드 설정
     */
    public void setAutoCommit(boolean autoCommit)
        throws SQLException {
        if (connection == null) {
            throw new NullPointerException();
        }
        connection.setAutoCommit(autoCommit);
    }

    /**
     * rollback
     */
    public void rollback()
        throws SQLException {
        if (connection == null) {
            throw new NullPointerException();
        }
        connection.rollback();
    }

    /**
     * commit
     */
    public void commit()
        throws SQLException {
        if (connection == null) {
            throw new NullPointerException();
        }
        connection.commit();
    }

    /**
     * connection close
     */
    public void close() {
        if (connection != null) {
            try {
                connection.close();
            }
            catch (Exception ex) {
            	LOGGER.error(ex);
            }
            connection = null;
        }
    }

    /**
     * DB에 저장될 String으로 변환한다.
     *
     * @param str
     *            DB에 저장될 String 객체로 컨텐츠의 charset으로 되어있다.
     * @return DB의 charset으로 변환된 String객체
     */
    public String toDB(String str)
        throws UnsupportedEncodingException {
        String dbstr = str;
        if (decoding) {
            // decoding가 true일 경우만 실행된다.
            //charset으로 DB에 저장될 String 객체 생성한다.
            dbstr = new String(str.getBytes("ISO-8859-1"), db_char_set_nm);
        }
        return dbstr;
    }

    /**
     * DB에서 읽어온 String 객체를 컨텐츠 에 사용될 String객체로 변환한다.
     *
     * @param DB에서
     *            읽어온 String
     * @return 컨텐츠 charset으로 변환된 String
     */
    public String fromDB(String str)
        throws UnsupportedEncodingException {
        String userstr = str;
        if (decoding) {
            userstr = new String(str.getBytes("ISO-8859-1"), db_char_set_nm);
        }
        return userstr;
    }

    /**
     * DB의 charset 를 얻는다.
     *
     * @return DB의 charset
     */
    public String getDBCharset() {
        return db_char_set_nm;
    }
}
