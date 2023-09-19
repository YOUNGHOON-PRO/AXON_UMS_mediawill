package messager.generator.content;

/**
 * 컨텐츠 생성시 Error가 발생하여 컨텐츠가 생성되지 않을 시 GeneratorException이 throw 된다.
 */
public class GeneratorException
    extends Exception
{
    protected ErrorCode errorCode;

    public GeneratorException(ErrorCode code) {
        super();
        errorCode = code;
    }

    public GeneratorException(ErrorCode code, String message) {
        super(message);
        errorCode = code;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
