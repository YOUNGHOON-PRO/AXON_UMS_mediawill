package messager.mailsender.code;

/**
 * 패턴을 찾기 위한 메시지의 객체를 형성한다.
 * @param pCode : 중분류 코드
 * @param sCode : 응답코드
 * @param response : 응답 메시지
 */
public class PatternUnit
{
    public String response;
    public String pCode;
    public String sCode;

    public PatternUnit(String aPcode, String aScode, String aResponse) {
        pCode = aPcode;
        sCode = aScode;
        response = aResponse;
    }
}
