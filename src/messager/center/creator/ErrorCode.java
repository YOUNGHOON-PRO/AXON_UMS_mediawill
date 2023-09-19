package messager.center.creator;

/**
 * Message 생성시 에러 코드
 */
public interface ErrorCode
{
    public final static String UNSUPPORTED_CHARSET = "031";
    public final static String ENCODING_FAIL = "032";
    public final static String NOT_FOUND_TASK = "033";
    public final static String SQL_EXCEPTION = "034";
    public final static String EXCEPTION = "035";
    public final static String REPOSITORY_EXCEPTION = "036";
    public final static String INVALID_MERGE_KEY = "041";
    public final static String INVALID_SUBJECT = "042";
    public final static String INVALID_NM_MERGE = "043";
    public final static String INVALID_RETURN_PATH = "044";
    public final static String INVALID_REPLY_TO = "045";
    public final static String INVALID_FROM_NAME = "046";
    public final static String INVALID_FROM_EMAIL = "047";
    public final static String INVALID_CONTENT_PATH = "048";
    public final static String INVALID_SEGMENT = "049";
    public final static String INVALID_ADDRESS_FILE_PATH = "050";
    public final static String NOT_EXISTS_TO_BASIC_INFO = "051";
    public final static String NOT_FOUND_TEST_EMAIL_FIELD = "052";
    public final static String ATTACH_FILE_SEARCH_FAIL = "061";
    public final static String MEMBER_DB_SEARCH_FAIL = "062";
    public final static String FILE_CONNECT_FAIL = "071";
    public final static String CONTENT_FETCH_FAIL = "072";
    public final static String ATTACH_FILE_FETCH_FAIL = "073";
    public final static String ADDRESS_FILE_FETCH_FAIL = "074";
    public final static String MEMBER_FETCH_FAIL = "076";
    public final static String INVALID_DB_INFO = "077";
    public final static String MEMBER_DB_SQL_EXCEPTION = "078";
    public final static String UNMATCHED_MEMBER_COLUMNS = "079";
    public final static String NOT_EXISTS_MEMBER = "081";
    public final static String MTK_CK_ERROR = "082";
    public final static String PERSONAL_ATTACH_ERROR = "084";
    public final static String DEC_ERROR = "085"; //
    
}