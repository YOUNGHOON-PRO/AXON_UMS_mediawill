package messager.mailsender.code;

import java.util.*;
import java.util.regex.*;

import messager.mailsender.config.*;
import messager.mailsender.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 서버로 부터 받은 응답 메세지를 체크하여 응답코드와 메세지 내용이
 * 정의된 에러코드표와  일치 하는가를 결정한다. 내용과 정의된 응답코드의 내용이
 * 다를때 응답코드를 변환하여 돌려 준다.
 * @param pUnit : 패턴 객체
 * @value pCode : 프로세스 단계 표시
 * @value sCode : 응답코드
 * @value rMessage : 응답메시지
 * @value return type : Integer (결과 코드 값)
 *
 * @return 1 : 이메일 생성 오류
 * @return 2 : 이메일 오류
 * @return 3 : 도메인 없음
 * @return 4 : 네트웍 에러
 * @return 5 : 트랜잭션 에러
 * @return 6 : 스팸
 * @return 7 : 메일 박스 용량 부족
 * @return 8 : 계정 없음
 * @return 9 : 그외 기타 에러
 */

public class PatternSearch
{
	
	private static final Logger LOGGER = LogManager.getLogger(PatternSearch.class.getName());
	
    /***************** Common Values *******************/
    private String rMessage; // 메일서버로 부터 받은 응답메시지
    private String patternFile; // 패턴 파일
    private String newKeywordFile; // 기존에 없는 새로운 패턴
    private String patternMessage; // 기존 패턴 파일에 수록되어 있는 패턴
    private String pCode; // 중분류 코드 - 발송 단계 표시
    private String sCode; // 소분류 코드 - 응답 코드
    private String return_Rcode; //	최종 반환 결과 코드

    /**
     * pUnit(패턴 객체)를 받아서 keyword 해쉬 테이블과 비교하여 발송결과
     * 코드를 결정한다.
     * @param pUnit : 패턴 객체
     * @return return_Rcode : 발송 결과 코드
     */
    public String filterMessage(PatternUnit pUnit) {
        pCode = pUnit.pCode;
        sCode = pUnit.sCode;
        rMessage = pUnit.response;
        String rCode = "000"; // 최종 결과 코드
        boolean checkMatch = false;

        for (Enumeration e = ConfigLoader.FILTER_KEY.keys(); e.hasMoreElements(); ) {
            String idx = e.nextElement().toString();
            Pattern pattern = Pattern.compile(idx.toLowerCase());
            Matcher match = pattern.matcher(rMessage.toLowerCase());
            if (match.find()) {
                try {
                    //rCode = Integer.parseInt((String)ConfigLoader.FILTER_KEY.get(idx));
                    rCode = (String) ConfigLoader.FILTER_KEY.get(idx);
                }
                catch (Exception ee) {
                	LOGGER.error(ee);
                    LogWriter.writeException("PatternSearch", "filterMessage()",
                                             "패턴 검사중 에러", ee);
                }
                checkMatch = true;
                return_Rcode = rCode;
                break;
            }
        }

        /* rCode가 0이 아닌 경우는 메일 서버로 부터 받은 메시지가 키워드 테이블과 일치하는
         * 부분이 있음을 나타 낸다.
         * rCode가 0인경우는 에러 로그 정의 표와 비교 하여 발송 결과 코드값을 결정 한다.
         * 예를 들어 pCode = 1,sCode = 421일때는 최종 결과 코드값은 6이 된다.
         */
        if (!checkMatch) {
            int tempCode = Integer.parseInt(pCode);
            switch (tempCode) {
                case 1:
                    if (sCode.equals("421")) {
                        return_Rcode = "007";
                    }
                    else {
                        return_Rcode = "005";
                    }
                    break;
                case 2:
                    if (sCode.equals("500") || sCode.equals("501") || sCode.equals("504")) {
                        return_Rcode = "006";
                    }
                    else if (sCode.equals("421")) {
                        return_Rcode = "007";
                    }
                    else {
                        return_Rcode = "005";
                    }
                    break;
                case 3:
                    if (sCode.equals("451") || sCode.equals("500") || sCode.equals("501")) {
                        return_Rcode = "006";
                    }
                    else if (sCode.equals("421")) {
                        return_Rcode = "007";
                    }
                    else if (sCode.equals("552") || sCode.equals("452")) {
                        return_Rcode = "008";
                    }
                    else {
                        return_Rcode = "005";
                    }
                    break;
                case 4:
                    if (sCode.equals("451") || sCode.equals("501") || sCode.equals("502") || sCode.equals("503")) {
                        return_Rcode = "006";
                    }
                    else if (sCode.equals("421")) {
                        return_Rcode = "007";
                    }
                    else if (sCode.equals("552") || sCode.equals("452")) {
                        return_Rcode = "008";
                    }
                    else if (sCode.equals("550") || sCode.equals("553") || sCode.equals("450")) {
                        return_Rcode = "009";
                    }
                    else {
                        return_Rcode = "005";
                    }
                    break;
                case 5:
                    if (sCode.equals("451") || sCode.equals("554") || sCode.equals("500") || sCode.equals("501") || sCode.equals("503")) {
                        return_Rcode = "005";
                    }
                    else if (sCode.equals("421")) {
                        return_Rcode = "007";
                    }
                    else {
                        return_Rcode = "005";
                    }
                    break;
                case 6:
                    if (sCode.equals("554") || sCode.equals("451")) {
                        return_Rcode = "006";
                    }
                    else if (sCode.equals("552") || sCode.equals("452")) {
                        return_Rcode = "008";
                    }
                    else {
                        return_Rcode = "005";
                    }
                    break;
                case 7:
                    if (sCode.equals("500") || sCode.equals("501") || sCode.equals("504")) {
                        return_Rcode = "006";
                    }
                    else if (sCode.equals("421")) {
                        return_Rcode = "007";
                    }
                    else {
                        return_Rcode = "005";
                    }
                default:
                    return_Rcode = "005";
                    break;
            }
            return return_Rcode;
        }
        
        if(return_Rcode ==null || "".equals(return_Rcode)) {
        	return return_Rcode = "005";
        }else {
        	return return_Rcode;
        }
        
    }
}