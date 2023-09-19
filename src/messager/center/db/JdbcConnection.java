package messager.center.db;

import java.io.*;
import java.sql.*;
import java.util.*;

import com.custinfo.safedata.CustInfoSafeData;

import messager.center.config.*;
import messager.center.creator.FetchException;
import messager.common.util.EncryptUtil;

/**
 * JDBC의 Connection 객체 Wrapper 클래스로 DB의 charset과 컨텐츠의 charset으로 String를 변환를
 * 제공한다..
 */
public class JdbcConnection
{
    //WorkDB의 접속 정보가 들어있는 Properties 객체
    private static Properties workDBProperties;

    /**
     * WorkDB에 대한 JdbcConnection instance 를 얻는다.
     *
     * @return JdbcConnection Connection객체를 감싼다.
     * @throws FetchException 
     */
    public static JdbcConnection getWorkConnection()
        throws ClassNotFoundException, SQLException {

        synchronized (JdbcConnection.class) {
            if (workDBProperties == null) {
                //workDB의 접속 정보를 가져온다.
                workDBProperties = ConfigLoader.getDBProperties();
            }
        }
        
        //복호화
		String ALGORITHM = "PBEWithMD5AndDES";
		String KEYSTRING = "ENDERSUMS";
		//EncryptUtil enc =  new EncryptUtil();
		CustInfoSafeData CustInfo = new CustInfoSafeData();
		
        //db의 charset
        String dbCharsetName = (String) workDBProperties.get("db.charset");

        // 컨텐츠의 charset
        String userCharsetName = (String) workDBProperties.get("user.charset");

        //JDBC Driver
        String driverName = (String) workDBProperties.get("jdbc.driver.name");

        // JDBC URL
        String url = (String) workDBProperties.get("jdbc.url");

        // DB의 User Name
        String user = (String) workDBProperties.get("db.user");

        // DB의 User Password
        String password ="";
        if("Y".equals(workDBProperties.getProperty("db.password.yn"))) {
        	String db_password;
			try {
				db_password = CustInfo.getDecrypt(workDBProperties.getProperty("db.password"), KEYSTRING);
				password = db_password;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	
        }else {
        	password = (String) workDBProperties.get("db.password");	
        }

        Properties props = new Properties();

        if (user != null) {
            props.put("user", user);
        }

        if (password == null) {
            //password가 null이면 빈 문자열로 설정된다.
            password = "";
        }
        props.put("password", password);
        //JDBC Driver 로드
        Class.forName(driverName);
        //JdbcConnection 객체를 생성한다.
        return new JdbcConnection(url, dbCharsetName, userCharsetName, props);
    }

    /**
     * JdbcConnection Instance 를 얻는다.
     *
     * @param props
     *            DB의 접속 정보가 저장된 Properties 객체
     * @param Connection
     *            객체를 포함하는 JdbcConnection 객체
     * @exception ClassNotFoundExcetion,
     *                SQLException
     */
    public static JdbcConnection getInstance(Properties props)
        throws ClassNotFoundException, SQLException {
        Properties dbProps = (Properties) props.clone();
        String dbCharsetName = (String) dbProps.remove("db.charset");
        String userCharsetName = (String) dbProps.remove("user.charset");
        String driverName = (String) dbProps.remove("jdbc.driver.name");
        String url = (String) dbProps.remove("jdbc.url");
        //JDBC driver 로드
        Class.forName(driverName);
        //JdbcConnection객체 생성
        JdbcConnection connection
            = new JdbcConnection(url, dbCharsetName, userCharsetName, dbProps);
        // DB에 접속한다.
        connection.newConnection();
        return connection;
    }

    private String jdbcUrl; //JDBC의 URL

    private String dbCharsetName; //DB의 charset

    private String userCharsetName; //컨텐츠의 charset

    private Properties properties; //DB의 기타 접속정보(user, password)

    private Connection connection; //Connection객체

    private boolean decoding; //DB의 charset과 컨텐츠의 charset이 다를 경우 true

    /**
     * JdbcConnection 객체를 생성한다.
     *
     * @param url
     *            JDBC의 URL
     * @param dbCharset
     *            DB의 charset
     * @param userCharset
     *            컨텐츠의 charset
     * @param props
     *            기타 접속 정보(user, password)
     */
    private JdbcConnection(String url, String dbCharset, String userCharset,
                           Properties props)
        throws SQLException {
        jdbcUrl = url;
        dbCharsetName = dbCharset;
        userCharsetName = userCharset;
        properties = props;

        if (dbCharsetName != null && userCharsetName != null
            && !dbCharsetName.equals(userCharset)) {
            //DB의 charset과 컨텐츠의 charset의 설정되어 있고 charset이 다를 경우 decoding는 true로
            // 설정된다.
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
            dbstr = new String(str.getBytes(userCharsetName), dbCharsetName);
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
            userstr = new String(str.getBytes(dbCharsetName), userCharsetName);
        }
        return userstr;
    }

    /**
     * DB의 charset 를 얻는다.
     *
     * @return DB의 charset
     */
    public String getDBCharset() {
        return dbCharsetName;
    }

    /**
     * 컨텐츠의 charset를 얻는다.
     *
     * @return 컨텐츠의 charset
     */
    public String getUserCharset() {
        return userCharsetName;
    }

    /**
     * 컨텐츠의 charset를 설정하고 charset에 의한 문자열의 디코딩을 할지 결정한다.
     *
     * @param charsetName
     *            컨텐츠의 charset
     */
    public void setUserCharset(String charsetName) {
        userCharsetName = charsetName;
        if (dbCharsetName != null && !charsetName.equals(dbCharsetName)) {
            decoding = true;
        }
        else {
            decoding = false;
        }
    }
}
