package messager.center.creator.parse;

import java.io.*;
import java.util.*;

import messager.common.*;

/**
 * WebAgent의 Url 부분을 분석하고 MergeKey를 추출한다. 시작과 끝은 $^로 시작해서 ^$로 끝난다.
 */
class HttpUrlParse
{
    /**
     * character를 읽어올 reader <br>
     * 읽어온 character를 되돌리기 위해서 PushbackReader를 사용한다.
     */
    private PushbackReader pbReader;

    /**
     * url를 머지키와 일반 텍스트를 구분하여 저장한다.
     */
    private Template template;

    /**
     * MergeKey를 정의해 놓았다. <br>
     * $:와 :$사이의 String이 Merge키 여부를 확인하기 위해
     */
    private HashMap mergeKeyMap;

    /**
     * WebAgent의 URL 분석 코드 <br>
     * -1: reader의 끝 <br>
     * 0: 일반 텍스트 <br>
     * 1: WebAgent URL
     */
    private int resultCode;

    /**
     * WebAgnet URL 를 분석하기위하여 객체 생성
     *
     * @param pushbackreader
     *            character를 읽어올 Reader객체 (읽어온 문자들을 Reader에 되돌릴수 있다)
     * @param keyMap
     *            MergeKey가 저장되어 있다
     * @exception Exception
     *                Reader로 문자를 읽을 때 오류가 발생 할 경우
     */
    public HttpUrlParse(PushbackReader pushbackreader, HashMap keyMap)
        throws Exception {
        pbReader = pushbackreader;
        mergeKeyMap = keyMap;
        parse();
    }

    /**
     * MergeKey와 일반 텍스트를 MergeElement객체저장후 ArrayList로 리턴
     */
    public Template getTemplate() {
        return template;
    }

    /**
     * WebAgent의 URL 분석한 후 성공 실패 유무 리턴
     *
     * @return resultCode 성공이면 1이상의 값
     */
    public int getResultCode() {
        return resultCode;
    }

    /**
     * WebAgent Url 부분을 분석하여 MergeKey부분과 일반 텍스트 부분을 MergeElemenet객체에 저장한 후
     * Template에 저장한다.
     *
     * @exception Exception
     *                입출력 에러가 발생 할 경우
     */
    private void parse()
        throws Exception {
        template = new Template();
        StringBuffer normalTextBuffer = new StringBuffer();
        int ch, nextch;

        while (true) {
            ch = pbReader.read();
            if (ch == -1) {
                resultCode = -1;
                break;
            }
            else if (ch == '$') {
                nextch = pbReader.read();
                if (nextch == -1) {
                    normalTextBuffer.append( (char) ch);
                    resultCode = -1;
                    return;
                }
                else if (nextch == ':') {
                    StringBuffer mergeTextBuffer = new StringBuffer();
                    //MergeKey 분석, MergeKey는 mergeTextBuffer에 저장
                    int value = mergeKey(pbReader, mergeTextBuffer);
                    if (value == -1) {
                        normalTextBuffer.append( (char) ch);
                        normalTextBuffer.append( (char) nextch);
                        normalTextBuffer.append(mergeTextBuffer.toString());
                    }
                    else {
                        //$:MergeKey:$인 경우
                        String mergeKey = mergeTextBuffer.toString();
                        String upperText = mergeKey.toUpperCase();
                        //MergeKey가 저장된 곳에서 MergeKey의 정보(Integer, String)를 얻는다.
                        Object obj = mergeKeyMap.get(upperText);
                        if (obj != null) {
                            if (obj instanceof String) {
                                //MergeKey의 정보가 String객체일 경우 대상자 의존적인 데이타가 아니므로
                                // 실제 데이타로 변경한다.
                                String replaceText = (String) obj;
                                //변경된 텍스트를 일반 텍스트로 처리한다.
                                normalTextBuffer.append(replaceText);
                            }
                            else {
                                //MergeKey의 정보가 Integer 일경우 대상자 의존적인 MergeKey
                                if (normalTextBuffer.length() > 0) {
                                    //이전 일반 텍스트를 MergeElement객체에 담아서 list에 추가
                                    MergeElement element = new MergeElement(
                                        normalTextBuffer.toString());
                                    template.add(element);
                                    normalTextBuffer.setLength(0);
                                }
                                //MergeElement의 type 설정
                                int type = MergeElement.MERGE_TYPE;
                                //MergeKey의 필드의 인덱스 설정(커리문이나 파일의 저장된 순서)
                                int index = ( (Integer) obj).intValue();
                                if (index == -1) {
                                    throw new Exception("Not Found MergeKey: " + mergeKey);
                                }
                                //MergeElement객체를 생성해서 list에 추가
                                MergeElement element = new MergeElement(
                                    mergeKey, type, index);
                                template.add(element);
                            }
                        }
                        else {
                            //MergeKey의 정보가 들어있지 않을 경우 MergeKey가 아니다.
                            normalTextBuffer.append("$:").append(
                                mergeTextBuffer.toString()).append(":$");
                        }
                    }
                }
                else {
                    //'$'문자 다음 문자가 ':'가 아닐 경우 '$'문자는 일반 텍스트에 저장하고 다음 문자는 다시
                    // 비교한다.
                    normalTextBuffer.append(ch);
                    pbReader.unread(nextch);
                }
            }
            else if (ch == ':') { //WebAgent의 끝을 나타내는 스트링(":^")중 처음 문자
                nextch = pbReader.read();

                if (nextch == -1) { //reader의 끝
                    normalTextBuffer.append( (char) ch);
                    resultCode = -1;
                    break;
                }
                else if (nextch == '^') { //WebAgent의 끝....
                    resultCode = 1;
                    break;
                }
                else {
                    normalTextBuffer.append( (char) ch);
                    pbReader.unread(nextch);
                }
            }
            else {
                normalTextBuffer.append( (char) ch);
            }
        }

        if (normalTextBuffer.length() > 0) {
            MergeElement element = new MergeElement(normalTextBuffer.toString());
            template.add(element); //일반 텍스트 부분을 list에 추가한다.
        }
    }

    /**
     * MergeKey를 추출한다.
     *
     * @param pbReader
     * @param strBuffer
     *            추출된 머지키를 저장한다. 일반 텍스트 일경우도 저장된다.
     * @return MergeKey이면 1, MergeKey가 아니면 0, reader의 끝이면 -1
     */
    private int mergeKey(PushbackReader pbReader, StringBuffer strBuffer)
        throws Exception {
        int retValue = 0;
        int ch, nextch;

        while (true) {
            ch = pbReader.read();

            if (ch == -1) {
                retValue = -1;
                break;
            }
            else if (ch == ':') { //MergeKey의 끝(":$")
                nextch = pbReader.read();
                if (nextch == -1) {
                    strBuffer.append( (char) ch);
                    break;
                }
                else if (nextch == '$') { //MergeKey의 끝
                    retValue = 1;
                    break;
                }
                else if (nextch == '^') { //WebAgent의 끝이므로 ':'와 '^'문자는
                    // reader에 되돌린다.
                    pbReader.unread(nextch);
                    pbReader.unread(ch);
                    break;
                }
                else { //':'문자 뒤에 '$'문자가 나오지 않을 경우
                    pbReader.unread(nextch);
                    pbReader.unread(ch);
                }
            }
            else if (ch == '$' || ch == '^') { //'$'문자또는 '^'문자가 나올경우 다시 처리 하기
                // 위해서 문자를 reader에 되돌린다.
                pbReader.unread(ch);
                break;
            }
            else if (ch == ' ' || ch == '\t' || ch == '\r' || ch == '\n') { //MergeKey에는
                // 공백문자가
                // 포함되지
                // 않는다.
                strBuffer.append( (char) ch);
                break;
            }
            else {
                strBuffer.append( (char) ch);
            }
        }
        return retValue;
    }
}
