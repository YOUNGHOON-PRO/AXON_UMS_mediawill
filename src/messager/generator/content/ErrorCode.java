package messager.generator.content;

/**
 * 생성시 발생하는 에러 코드 GeneratorException의 errorCode의 값이다.
 */
public final class ErrorCode
{
    public String code;

    public String code1;

    public String code2;

    public String code3;

    public ErrorCode(String code0, String code1, String code2, String code3) {
        this.code = code0;
        this.code1 = code1;
        this.code2 = code2;
        this.code3 = code3;
    }

    public final static ErrorCode NO_BASIC_INFO_RECEIVER
        = new ErrorCode("001", "001", "001", "002");

    public final static ErrorCode NO_RECEIVER_CHANNEL_VALUE
        = new ErrorCode("001", "001", "001", "004");

    public final static ErrorCode CLASS_CAST_ERROR
        = new ErrorCode("001", "001", "001", "005");

    public final static ErrorCode CONTENT_INVALID
        = new ErrorCode("001", "001", "001", "006");

    public final static ErrorCode WEB_ERROR
        = new ErrorCode("001", "001", "002", "001");

    public final static ErrorCode MALFORM_URL
        = new ErrorCode("001", "001", "002", "002");

    public final static ErrorCode HTTP_RESPONSE_ERROR
        = new ErrorCode("001", "001", "002", "003");

    public final static ErrorCode WEB_CONTENT_SCRIPT_ERROR
        = new ErrorCode("001", "001", "002", "004");

    public final static ErrorCode WEB_CONTENT_INVALID_ERROR
        = new ErrorCode("001", "001", "002", "005");

    public final static ErrorCode MERGE_ERROR
        = new ErrorCode("001", "001", "004", "001");

    public final static ErrorCode ENCODING_ERROR
        = new ErrorCode("001", "001", "004", "004");

    public final static ErrorCode UNKNOWN_CHANNEL
        = new ErrorCode("001", "001", "004", "005");

    public final static ErrorCode AGENT_INIT_FAIL
        = new ErrorCode("001", "001", "004", "006");

    public final static ErrorCode UNKNOWN_ERROR
        = new ErrorCode("001", "001", "001", "001");
    
    public final static ErrorCode SECUREFILE_ERROR
    = new ErrorCode("010", "001", "001", "001");
    
    public final static ErrorCode PERSONAL_SUBJECT_ERROR
    = new ErrorCode("011", "001", "001", "001");
    
    public final static ErrorCode PERSONAL_BODY_ERROR
    = new ErrorCode("011", "001", "001", "002");
    
}
