package messager.mailsender.code;

public interface ErrorCode
{
    /**************< (L) Local >**************/
    public static final String SUCCESS = "0000"; // 내부 프로세스 function의  결과 성공 여부
    // FileManager.java
    // (M) Make Name File, Delete Obj File
    public static final String ALREADYEXIST = "1001";
    public static final String MAKEFAIL = "1002";
    public static final String EXCEPT = "1003";
    public static final String DELFAIL = "1004";
    public static final String FILENOTFOUND = "1005";
    // (O)UnitName -> UnitEnvelope
    public static final String OBJSTREAMEXCEPTION = "2001";

    /*********< SOFTWARE BOUNDARY >************/
    // 대분류
    public static final String SOFTERROR = "001";

    // 중분류
    public static final String STP_SYNTAX = "001";
    // 소분류
    public static final String STS_WRONGEMAIL = "102";
    public static final String STS_NOTEXISTCONTENT = "106";
    // 결과코드
    public static final String STR_EMAILEROR = "002";
    public static final String STR_NOTEXISTCONTENT = "001";

    //중분류
    public static final String STP_DOMAIN = "005";
    // 소분류
    public static final String STS_UNKNOWNHOST = "001";
    // 결과코드
    public static final String STR_DOMAINERROR = "003";

    // 중분류
    public static final String STP_NETWORK = "006";
    // 소분류
    public static final String STS_SocketException = "003";
    public static final String STS_BindException = "003";
    public static final String STS_NoRouteToHostException = "002";
    public static final String STS_ConnectException = "001";
    public static final String STS_ProtocolException = "006";
    public static final String STS_MalformedURLException = "004";
    public static final String STS_UnknownServiceException = "006";
    public static final String STS_SockTimeoutException = "005";
    public static final String STS_NetworkETC = "999";
    //결과코드
    public static final String STR_NETWORKERROR = "004";

    /*********< HARDWARE BOUNDARY >************/
    // 대분류
    public static final String HARDERROR = "002";

    // 중분류
    public static final String HDP_CONNECT = "001";
    public static final String HDP_HELO = "002";
    public static final String HDP_MAILFROM = "003";
    public static final String HDP_RCPTTO = "004";
    public static final String HDP_DATA = "005";
    public static final String HDP_DOT = "006";
    public static final String HDP_RSET = "007";

    // 소분류
    public static final String HDS_NETWORKERROR = "999"; // 임의로 만든 응답코드 ETC ERROR

    // 결과코드
    public static final String HDR_NETWORK = "005";
    public static final String HDR_TRANSACTION = "006";
    public static final String HDR_SPAM = "007";
    public static final String HDR_MAILBOX = "008";
    public static final String HDR_UNKNOWNACCOUNT = "009";
}
