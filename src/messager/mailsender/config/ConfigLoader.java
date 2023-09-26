package messager.mailsender.config;

import java.io.*;
import java.net.*;
import java.util.*;

import com.custinfo.safedata.CustInfoSafeData;

import messager.center.creator.FetchException;
import messager.common.util.EncryptUtil;
import messager.mailsender.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * 환경 설정 변수 클래스
 * @value BLOCK_SESSION					: 도메인별 연결 세션수를 보관하는 해쉬 테이블
 * @value DOMAIN_CD						: 도메인을 숫자로 정의한 해쉬 테이블
 * @value FILTER_KEY					: 응답 메시지를 분석하기 위한 패턴기를 보관하는 해쉬 테이블
 * @value TRANSFER_ROOT_DIR				: Mail Transfer의 루트 디렉토리 경로
 * @value TRANSFER_REPOSITORY_DIR		: Mail Transfer의 repository 디렉토리 경로
 * @value MAILSENDER_ID					: Mail Transfer의 고유 아이디
 * @value GENERATOR_PORT				: Mail Generator과 통신하는 포트
 * @value SEND_DOMAIN					: Mail Transfer의 도메인
 * @value SEND_BUFFER					: 소켓의 보내는 버퍼 사이즈
 * @value READ_BUFFER					: 소켓의 받는 버퍼 사이즈
 * @value NAME_SERVER					: DNS 주소
 * @value MESSAGE_SENDER_AGENT			: 발송 Thread 수
 * @value MESSAGE_DOMAIN_CONNECT_LIMIT	: 도메인별 연결 제한 값
 * @value RETRY_SENDER_AGENT			: Retry시 생성되는 Agent 수
 * @value RETRY_WAIT_TIME				: 재발송 대기 시간
 * @value LOG_PATH						: Mail Transfer의 내부 에러 로그 경로
 * @value DELETE_PERIOD					: 내부 에러 로그 삭제 기간
 * @value DATE_FORMAT					: 로그에 기록할 날짜 형식
 * @value sepKey						: 로그 파일에서 각각의 변수를 구분 문자
 * @value senderInitTime				: sender.properties 파일의 최초 변경 시간
 * @value sessionInitTime				: Session.properties 파일의 최초 변경 시간
 * @value filterInitTime				: filter.properties 파일의 최초 변경 시간
 */
public class ConfigLoader
    extends Thread
{
	
	private static final Logger LOGGER = LogManager.getLogger(ConfigLoader.class.getName());
	
    /**************** Inner Function Values **************/
    //< BLOCK_SESSION>
    public static Hashtable BLOCK_SESSION = new Hashtable();
    public static Hashtable DOMAIN_CD = new Hashtable();
    public static Hashtable FILTER_KEY = new Hashtable();
    public static Hashtable DOMAIN_CONNECT_LIMIT = new Hashtable();

    /***************** Common Values *******************/

    public static String TRANSFER_ROOT_DIR;
    public static String TRANSFER_REPOSITORY_DIR;
    public static String MAILSENDER_ID;
    public static String GENERATOR_PORT;
    public static String GENERATOR_IP;
    public static String CENTER_SENDLOG_PORT;
    public static String SEND_DOMAIN;
    public static byte[] SEND_IP = new byte[4];
    public static InetAddress sendIP;

    public static int SEND_BUFFER;
    public static int READ_BUFFER;

    public static String NAME_SERVER;

    public static int MESSAGE_SENDER_AGENT; // Sending the number of agent;

    public static int RETRY_SENDER_AGENT; // Retry시 생성되는 Agent 수
    public static int RETRY_WAIT_TIME; // 재발송 대기 시간

    public static String DELETE_PERIOD;

    public static String INSERT_DBURL;
    public static String INSERT_DBDRIVER;
    public static String INSERT_USER;
    public static String INSERT_PASSWD;
    public static String INSERT_PATH;
    public static int INSERT_PERIOD;
    
    //DBType 추가
    public static String INSERT_DBTYPE;

    //	< BLOCK_SESSION>
    private static String sepKey = "|";
    private static long senderInitTime;
    private static long sessionInitTime;
    private static long filterInitTime;

    /*
     * 환경 설정 파일이 변경이 있을경우 이를 실시간으로 새로 로딩한다.
     * configFile : 기본 환경 설정 파일
     * spamFile : 도메인별 세션 설정 파일
     * keywordFile : 발송 결과 코드 키워드 설정 파일
     * @see java.lang.Runnable#run()
     */
    public void run() {
        StringBuffer sb = new StringBuffer();
        long initTime;

        File configFile = new File(sb.append(TRANSFER_ROOT_DIR).append("conf/sender.properties").toString());
        senderInitTime = configFile.lastModified(); // default.conf 최종 수정 시간

        sb = new StringBuffer();
        File sessionFile = new File(sb.append(TRANSFER_ROOT_DIR).append("conf/session.properties").toString());
        sessionInitTime = sessionFile.lastModified(); // spam.conf 최종 수정 시간

        sb = new StringBuffer();
        File filterFile = new File(sb.append(TRANSFER_ROOT_DIR).append("conf/filter.properties").toString());
        filterInitTime = filterFile.lastModified(); // keyword.conf 최종 수정 시간

        while (true) {
            try {
                Thread.sleep(600000); // 10분마다 체크
            }
            catch (InterruptedException e) {
            	LOGGER.error(e);
                e.printStackTrace();
            }

            initTime = configFile.lastModified(); // sender.properties 최근 수정 시간
            if (senderInitTime < initTime) {
                //System.out.println("sender.properties 파일을 다시 읽어 들입니다.");
            	LOGGER.info("sender.properties 파일을 다시 읽어 들입니다.");
                load();
                senderInitTime = initTime;
            }

            initTime = sessionFile.lastModified(); // session.properties 최근 수정 시간
            if (sessionInitTime < initTime) {
                //System.out.println("session.properties 파일을 다시 읽어 들입니다.");
            	LOGGER.info("session.properties 파일을 다시 읽어 들입니다.");
                loadSession();
                sessionInitTime = initTime;
            }

            initTime = filterFile.lastModified(); // filter.properties 최근 수정 시간
            if (filterInitTime < initTime) {
                //System.out.println("filter.properties 파일을 다시 읽어 들입니다.");
            	LOGGER.info("filter.properties 파일을 다시 읽어 들입니다.");
                loadMessageFilter();
                filterInitTime = initTime;
            }
        }
    }

    /**
     * Load config file named sender.properties
     */
    public static void load() {

        Properties pro = new Properties();
        InputStream is = null;
        try {
            is = new FileInputStream("conf" + File.separator + "sender.properties");
            pro.load(is);
        }
        catch (FileNotFoundException e) {
        	LOGGER.error("sender.properties 를 찾을수 없습니다. : " + e);
            LogWriter.writeException("ConfigLoader", "load()", "sender.properties 를 찾을수 없습니다.", e);
        }
        catch (IOException e) {
        	LOGGER.error("sender.properties 를 읽는데 문제가 발생하였습니다. : " + e);
            LogWriter.writeException("ConfigLoader", "load()", "sender.properties 를 읽는데 문제가 발생하였습니다.", e);
        }
        finally {
            if (is != null) {
                try {
                    is.close();
                }
                catch (Exception e) {LOGGER.error(e);}
            }
        }

        Properties pro_1 = new Properties();
        try {
            is = new FileInputStream("conf" + File.separator + "database.properties");
            pro_1.load(is);
        }
        catch (FileNotFoundException e) {
        	LOGGER.error("database.properties를 찾을수 없습니다. : " + e);
            LogWriter.writeException("ConfigLoader", "load()", "database.properties를 찾을수 없습니다.", e);
        }
        catch (IOException e) {
        	LOGGER.error("database.properties를 읽는데 문제가 발생하였습니다. : " + e);
            LogWriter.writeException("ConfigLoader", "load()", "database.properties를 읽는데 문제가 발생하였습니다.", e);
        }
        finally {
            try {
                is.close();
            }
            catch (Exception e) {LOGGER.error(e);}
        }

        
        //복호화
		String ALGORITHM = "PBEWithMD5AndDES";
		String KEYSTRING = "ENDERSUMS";
		//EncryptUtil enc =  new EncryptUtil();
		CustInfoSafeData CustInfo = new CustInfoSafeData();
        
        TRANSFER_ROOT_DIR = (String) pro.get("TRANSFER.ROOT.DIR");
        TRANSFER_REPOSITORY_DIR = (String) pro.get("TRANSFER.REPOSITORY.DIR");

        //< MailSender ID >
        MAILSENDER_ID = (String) System.getProperty("sender.id");
        GENERATOR_PORT = (String) pro.get("GENERATOR.PORT");
        GENERATOR_IP = (String) pro.get("GENERATOR.IP");
        CENTER_SENDLOG_PORT = (String) pro.get("CENTER.SENDLOG.PORT"); //사용 여부 ????
        SEND_DOMAIN = (String) pro.get("SEND.DOMAIN");

        SEND_BUFFER = Integer.parseInt( (String) pro.get("SEND.BUFFER"));
        READ_BUFFER = Integer.parseInt( (String) pro.get("READ.BUFFER"));

        NAME_SERVER = (String) pro.get("NAME.SERVER");

        MESSAGE_SENDER_AGENT = Integer.parseInt( (String) pro.get("MESSAGE.SENDER.AGENT"));

        RETRY_SENDER_AGENT = Integer.parseInt( (String) pro.get("RETRY.SENDER.AGENT"));
        RETRY_WAIT_TIME = Integer.parseInt( (String) pro.get("RETRY.WAIT.TIME"));

        DELETE_PERIOD = (String) pro.get("DELETE.PERIOD");

        INSERT_DBURL = (String) pro_1.get("jdbc.url");
        INSERT_DBDRIVER = (String) pro_1.get("jdbc.driver.name");
        INSERT_USER = (String) pro_1.get("db.user");
        
        //DBType 추가
        INSERT_DBTYPE= (String) pro_1.get("db.dbType");
        
        //복호화
        if("Y".equals((String) pro_1.get("db.password.yn"))) {
        	String db_password;
			try {
				db_password = CustInfo.getDecrypt((String) pro_1.get("db.password"), KEYSTRING);
				INSERT_PASSWD = db_password;
			} catch (Exception e) {
				LOGGER.error(e);
				// TODO Auto-generated catch block
				//e.printStackTrace();
				
			}
        }else {
        	INSERT_PASSWD = (String) pro_1.get("db.password");	
        }
        
        INSERT_PERIOD = Integer.parseInt( (String) pro.get("INSERT.PERIOD"));
        
        
        StringTokenizer st = new StringTokenizer( ( (String) pro.get("SEND.IP")), ".");
        SEND_IP[0] = (byte) Integer.parseInt(st.nextToken());
        SEND_IP[1] = (byte) Integer.parseInt(st.nextToken());
        SEND_IP[2] = (byte) Integer.parseInt(st.nextToken());
        SEND_IP[3] = (byte) Integer.parseInt(st.nextToken());

        //< BLOCK SESSION >
        loadSession();

        //< FILTER MESSAGE >
        loadMessageFilter();

        try {
            sendIP = InetAddress.getByAddress(ConfigLoader.SEND_IP);
            //System.out.println("MailSender'IP is binded with " + sendIP.getHostAddress());
            LOGGER.info("MailSender'IP is binded with \" + sendIP.getHostAddress()");
        }
        catch (UnknownHostException e1) {
        	LOGGER.error("sender.properties 의 SEND.IP를 확인 하세요 : " + e1);
            LogWriter.writeException("ConfigLoader", "load()", "sender.properties 의 SEND.IP를 확인 하세요", e1);
        }
    }

    /**
     * Load spam policy config file
     * 도메인별 세션수를 설정할수 있다.
     */
    public static void loadSession() {
        StringBuffer sb = new StringBuffer();
        String sessionFile = sb.append(ConfigLoader.TRANSFER_ROOT_DIR).append("conf/session.properties").toString();
        String tempLine = null;

        try {
            BufferedReader br = new BufferedReader(new FileReader(sessionFile));
            while ( (tempLine = br.readLine()) != null) {
                int pos = tempLine.indexOf(sepKey);
                /**
                 * 도메인별 세션 수를 구한다..
                 * Domain | session
                 */
                String spamDomain = tempLine.substring(0, pos).trim();
                String session = tempLine.substring(pos + 1);
                BLOCK_SESSION.put(spamDomain, session);
            }
            br.close();
            //System.out.println("Successfully loading Session.properties(" + BLOCK_SESSION.size() + " elements)");
            LOGGER.info("Successfully loading Session.properties(" + BLOCK_SESSION.size() + " elements)");
        }
        catch (Exception e) {
        	LOGGER.error("sessuib,properties파일 로딩시 에러 : " + e);
            LogWriter.writeException("ConfigLoader", "loadSpam()", "sessuib,properties파일 로딩시 에러 ", e);
        }
    }

    /**
     * Load keyword filter file
     * 메일 서버로 부터 받는 응답메시지 유형 파일
     */
    public static void loadMessageFilter() {
        StringBuffer sb = new StringBuffer();
        String patternFile = sb.append(ConfigLoader.TRANSFER_ROOT_DIR).append("conf/filter.properties").toString();
        String patternMessage = null;
        try {
            BufferedReader br = new BufferedReader(new FileReader(patternFile));
            while ( (patternMessage = br.readLine()) != null) {
                int pos = patternMessage.indexOf(sepKey);
                /**
                 * 에러 메세지 내용과 해당 코드 값을 읽어 온다.
                 * Error Keyword | code value
                 */
                String patM = patternMessage.substring(0, pos).trim();
                String patC = patternMessage.substring(pos + 1);
                if (patM != null && patC != null) {
                    FILTER_KEY.put(patM, patC);
                }
            }
            br.close();
            //System.out.println("Successfully loading filter.properties(" + FILTER_KEY.size() + " elements)");
            LOGGER.info("Successfully loading filter.properties(" + FILTER_KEY.size() + " elements)");
        }
        catch (Exception e) {
        	LOGGER.error("filter.properties 파일 로딩시 에러 : ", e);
            LogWriter.writeException("ConfigLoader", "loadMessageFilter()", "filter.properties 파일 로딩시 에러", e);
        }
    }

    public static Hashtable cachingDomain() {
        String cachePath = new StringBuffer(ConfigLoader.TRANSFER_ROOT_DIR)
            .append("conf/cache/").toString();
        Hashtable mxRecordTable = new Hashtable();
        try {
            String[] cachingList = new java.io.File(cachePath).list();
            if (cachingList.length > 0) {
                for (int k = 0; k < cachingList.length; k++) {

                    Vector MXRcord = new Vector();
                    String filePath = cachePath + cachingList[k];
                    String ipLine = null;
                    BufferedReader br = new BufferedReader(new FileReader(filePath));
                    while ( (ipLine = br.readLine()) != null) {
                        MXRcord.add(ipLine.trim());
                    }
                    br.close();
                    mxRecordTable.put(cachingList[k], MXRcord);
                    //System.out.println(cachingList[k] + " is cached -> " + MXRcord.firstElement());
                    LOGGER.info(cachingList[k] + " is cached -> " + MXRcord.firstElement());
                }
            }
            else {
            	LOGGER.info("캐쉬할 도메인이 없습니다.");
                LogWriter.writeError("ConfigLoader", "cachiingDomain()", "캐쉬할 도메인이 없습니다.", "");
            }
        }
        catch (Exception e) {
        	LOGGER.error("도메인을 캐쉬하는데 실패 : " + e);
            LogWriter.writeException("ConfigLoader", "chchingDomain()", "도메인을 캐쉬하는데 실패 ", e);
        }
        return mxRecordTable;
    }
}
