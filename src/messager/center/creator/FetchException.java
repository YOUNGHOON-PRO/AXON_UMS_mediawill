package messager.center.creator;

/**
 * Message 생성시 Exception이 발생되면 Throw된다.
 */
public class FetchException
    extends Exception
{
    //errorCode
    protected String errorCode = "090";

    /**
     * Error Message와 Error Code로 Exception 객체 생성
     */
    public FetchException(String str, String code) {
        super(str);
        errorCode = code;
    }

    /**
     * ErrorCode 를 얻는다.
     */
    public String getErrorCode() {
        return errorCode;
    }
}
