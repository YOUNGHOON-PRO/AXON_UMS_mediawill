package messager.center.creator.parse;

import java.io.*;
import java.util.*;

import messager.common.*;

/**
 * 라인 단위로 템플릿을 분석한다 MergeKey를 구분 하는 문자열은 $:로 시작해서 :$로 끝난다..
 */
public class LineParser
{
    private Message message;

    private Template template;
    
    private Template2 template2;


    /**
     * LineParser 객체를 생성한다.
     *
     * @param keyMap 머지키가 정의 되어 있다.
     * @param mList 머지키와 일반 텍스트를 분리해서 MergeEelement객체로 저장한다.
     */
    public LineParser(Message message, Template template) {
        this.message = message;
        this.template = template;
    }
    
    public LineParser(Message message, Template2 template2) {
        this.message = message;
        this.template2 = template2;
    }

    /**
     * line를 MergeKey와 일반 텍스트 부분으로 분리한다. 분리된 스트링은 MergeElement객체에 저장하여
     * mergeList에 추가한다.
     *
     * @param line
     * @param norTextBuffer 이전 line의 가장 끝에 존재하는 텍스트 부분
     */
    public void parse(String line, StringBuffer normalTextBuffer)
        throws Exception {
        HashMap keyMap = message.keyMap;
        int len = line.length();
        if (len <= 0) {
            return;
        }
        //Character를 문자를 읽은 후 Reader에 되돌릴수 있게 하기 위해 PushbackReader객체 이용
        PushbackReader pbReader = new PushbackReader(new StringReader(line),len);  // line : $:NAME:$ 님 안녕하세요.  (메일제목)

        while (true) {
            int ch = pbReader.read();
            if (ch == -1) {
                //라인의 끝
                return;
            }
            else if (ch == '$') {
                //MergeKey 를 나타내는 문자
                int next_ch = pbReader.read();
                if (next_ch == -1) {
                    normalTextBuffer.append( (char) ch);
                    return;
                }
                else if (next_ch == ':') {
                    //
                    /////////////////////////////////////////////////
                    StringBuffer mergeTextBuffer = new StringBuffer();
                    int value = mergeKey(pbReader, mergeTextBuffer);
                    if (value == -1) {
                        normalTextBuffer.append( (char) ch);
                        normalTextBuffer.append( (char) next_ch);
                        normalTextBuffer.append(mergeTextBuffer.toString());
                        return;
                    }
                    else if (value == 0) {
                        normalTextBuffer.append( (char) ch);
                        normalTextBuffer.append( (char) next_ch);
                        normalTextBuffer.append(mergeTextBuffer.toString());
                    }
                    else {
                        String mergeKey = mergeTextBuffer.toString();
                        String upperText = mergeKey.toUpperCase();
                        /*********************************
                         * MARGE DEBUG 출력
                         * writed by 오범석
                         System.out.println(" LineParser.java : ");
                         System.out.println("upperText ==> " + upperText);
                         System.out.println("keyMap ==> " + keyMap);
                         */
                        Object obj = keyMap.get(upperText);
                        if (obj != null) {
                            if (obj instanceof String) {
                                String replaceText = (String) obj;
                                normalTextBuffer.append(replaceText);
                            }
                            else {
                                if (normalTextBuffer.length() > 0) {
                                    MergeElement element = new MergeElement(
                                        normalTextBuffer.toString());
                                    template.add(element);
                                    normalTextBuffer.setLength(0);
                                }
                                int type = MergeElement.MERGE_TYPE;
                                int index = ( (Integer) obj).intValue();
                                if (index == -1) {
                                    throw new Exception("Not Found Merge Key: " + mergeKey);
                                }

                                MergeElement mElement =
                                    new MergeElement(mergeKey, type, index);
                                template.add(mElement);
                            }
                        }
                        else {
                            normalTextBuffer.append("$:").append(
                                mergeTextBuffer.toString()).append(":$");
                        }
                    }
                }
                else {
                    normalTextBuffer.append( (char) ch);
                    pbReader.unread(next_ch);
                }
            }
            else if (ch == '^') {
                int next_ch = pbReader.read();
                if (next_ch == -1) {
                    normalTextBuffer.append( (char) ch);
                    return;
                }
                else if (next_ch == ':') {
                    HttpUrlParse urlParse = new HttpUrlParse(pbReader, keyMap);
                    Template urlTemplate = urlParse.getTemplate();
                    int result = urlParse.getResultCode();
                    if (result > 0) {
                        if (normalTextBuffer.length() > 0) {
                            MergeElement element = new MergeElement(
                                normalTextBuffer.toString());
                            template.add(element);
                            normalTextBuffer.setLength(0);
                        }

                        ArrayList agentList = message.agentList;
                        if (agentList == null) {
                            agentList = new ArrayList();
                            message.agentList = agentList;
                        }
                        int agentID = agentList.size();
                        String actionName = "WebAction";
                        HashMap parameters = new HashMap();
                        parameters.put("web.url", urlTemplate);
                        Agent agent = new Agent(agentID, actionName, parameters);
                        agentList.add(agent);
                        MergeElement element = new MergeElement(actionName,
                            agentID);
                        template.add(element);
                    }
                    else {
                        normalTextBuffer.append(ch).append(next_ch);
                        if (urlTemplate != null) {
                            MergeElement element = (MergeElement) urlTemplate
                                .get(0);
                            if (element.type == MergeElement.TEXT_TYPE) {
                                String text = element.text;
                                normalTextBuffer.append(text);
                                MergeElement nElement = new MergeElement(
                                    normalTextBuffer.toString());
                                template.add(nElement);
                            }
                            else {
                                MergeElement nElement = new MergeElement(
                                    normalTextBuffer.toString());
                                template.add(nElement);
                                normalTextBuffer.setLength(0);
                                template.add(element);
                            }

                            for (int i = 1; i < urlTemplate.size(); i++) {
                                element = (MergeElement) urlTemplate.get(i);
                                template.add(element);
                            }
                        }
                        if (result == -1) {
                            return;
                        }
                    }
                }
                else {
                    normalTextBuffer.append( (char) ch);
                    pbReader.unread(next_ch);
                }
            }
            else {
                normalTextBuffer.append( (char) ch);
            }

        }
    }

    
    
    /**
     * line를 MergeKey와 일반 텍스트 부분으로 분리한다. 분리된 스트링은 MergeElement객체에 저장하여
     * mergeList에 추가한다.
     *
     * @param line
     * @param norTextBuffer 이전 line의 가장 끝에 존재하는 텍스트 부분
     */
    public void parse2(String line, StringBuffer normalTextBuffer)
        throws Exception {
        HashMap keyMap = message.keyMap;
        int len = line.length();
        if (len <= 0) {
            return;
        }
        //Character를 문자를 읽은 후 Reader에 되돌릴수 있게 하기 위해 PushbackReader객체 이용
        PushbackReader pbReader = new PushbackReader(new StringReader(line),len);  // line : $:NAME:$ 님 안녕하세요.  (메일제목)

        while (true) {
            int ch = pbReader.read();
            if (ch == -1) {
                //라인의 끝
                return;
            }
            else if (ch == '$') {
                //MergeKey 를 나타내는 문자
                int next_ch = pbReader.read();
                if (next_ch == -1) {
                    normalTextBuffer.append( (char) ch);
                    return;
                }
                else if (next_ch == ':') {
                    //
                    /////////////////////////////////////////////////
                    StringBuffer mergeTextBuffer = new StringBuffer();
                    int value = mergeKey(pbReader, mergeTextBuffer);
                    if (value == -1) {
                        normalTextBuffer.append( (char) ch);
                        normalTextBuffer.append( (char) next_ch);
                        normalTextBuffer.append(mergeTextBuffer.toString());
                        return;
                    }
                    else if (value == 0) {
                        normalTextBuffer.append( (char) ch);
                        normalTextBuffer.append( (char) next_ch);
                        normalTextBuffer.append(mergeTextBuffer.toString());
                    }
                    else {
                        String mergeKey = mergeTextBuffer.toString();
                        String upperText = mergeKey.toUpperCase();
                        /*********************************
                         * MARGE DEBUG 출력
                         * writed by 오범석
                         System.out.println(" LineParser.java : ");
                         System.out.println("upperText ==> " + upperText);
                         System.out.println("keyMap ==> " + keyMap);
                         */
                        Object obj = keyMap.get(upperText);
                        if (obj != null) {
                            if (obj instanceof String) {
                                String replaceText = (String) obj;
                                normalTextBuffer.append(replaceText);
                            }
                            else {
                                if (normalTextBuffer.length() > 0) {
                                    MergeElement element = new MergeElement(
                                        normalTextBuffer.toString());
                                    template2.add(element);
                                    normalTextBuffer.setLength(0);
                                }
                                int type = MergeElement.MERGE_TYPE;
                                int index = ( (Integer) obj).intValue();
                                if (index == -1) {
                                    throw new Exception("Not Found Merge Key: " + mergeKey);
                                }

                                MergeElement mElement =
                                    new MergeElement(mergeKey, type, index);
                                template2.add(mElement);
                            }
                        }
                        else {
                            normalTextBuffer.append("$:").append(
                                mergeTextBuffer.toString()).append(":$");
                        }
                    }
                }
                else {
                    normalTextBuffer.append( (char) ch);
                    pbReader.unread(next_ch);
                }
            }
            else if (ch == '^') {
                int next_ch = pbReader.read();
                if (next_ch == -1) {
                    normalTextBuffer.append( (char) ch);
                    return;
                }
                else if (next_ch == ':') {
                    HttpUrlParse urlParse = new HttpUrlParse(pbReader, keyMap);
                    Template urlTemplate = urlParse.getTemplate();
                    int result = urlParse.getResultCode();
                    if (result > 0) {
                        if (normalTextBuffer.length() > 0) {
                            MergeElement element = new MergeElement(
                                normalTextBuffer.toString());
                            template2.add(element);
                            normalTextBuffer.setLength(0);
                        }

                        ArrayList agentList = message.agentList2;
                        if (agentList == null) {
                            agentList = new ArrayList();
                            message.agentList2 = agentList;
                        }
                        int agentID = agentList.size();
                        String actionName = "WebAction";
                        HashMap parameters = new HashMap();
                        parameters.put("web.url", urlTemplate);
                        Agent agent = new Agent(agentID, actionName, parameters);
                        agentList.add(agent);
                        MergeElement element = new MergeElement(actionName,
                            agentID);
                        template2.add(element);
                    }
                    else {
                        normalTextBuffer.append(ch).append(next_ch);
                        if (urlTemplate != null) {
                            MergeElement element = (MergeElement) urlTemplate
                                .get(0);
                            if (element.type == MergeElement.TEXT_TYPE) {
                                String text = element.text;
                                normalTextBuffer.append(text);
                                MergeElement nElement = new MergeElement(
                                    normalTextBuffer.toString());
                                template2.add(nElement);
                            }
                            else {
                                MergeElement nElement = new MergeElement(
                                    normalTextBuffer.toString());
                                template2.add(nElement);
                                normalTextBuffer.setLength(0);
                                template2.add(element);
                            }

                            for (int i = 1; i < urlTemplate.size(); i++) {
                                element = (MergeElement) urlTemplate.get(i);
                                template2.add(element);
                            }
                        }
                        if (result == -1) {
                            return;
                        }
                    }
                }
                else {
                    normalTextBuffer.append( (char) ch);
                    pbReader.unread(next_ch);
                }
            }
            else {
                normalTextBuffer.append( (char) ch);
            }

        }
    }
    
    
    /**
     * :$ 로 끝나는 텍스트(MergeKey)를 추출한다.
     *
     * @param pbReader
     * @param strBuffer
     *            텍스트를 저장한다.
     * @return 1 MergeKey, 0 일반 텍스트, -1 일반 텍스트로 pbReader의 끝
     * @excetion ParseException IOException 이 발생 할경우 throw된다.
     */
    private int mergeKey(PushbackReader pbReader, StringBuffer strBuffer)
        throws Exception {
        int retValue = 1;
        while (true) {
            int ch = pbReader.read();
            if (ch == -1) {
                retValue = -1;
                break;
            }
            else if (ch == ':') {
                int next_ch = pbReader.read();
                if (next_ch == -1) {
                    strBuffer.append( (char) ch);
                    retValue = -1;
                    break;
                }
                else if (next_ch == '^') {
                    strBuffer.append( (char) ch);
                    pbReader.unread(next_ch);
                    retValue = 0;
                    break;
                }
                else if (next_ch == '$') {
                    break;
                }
                else {
                    retValue = 0;
                    break;
                }
            }
            else if (ch == '$' || ch == '^') {
                pbReader.unread(ch);
                retValue = 0;
                break;
            }
            else if (ch == ' ' || ch == '\t' || ch == '\r' || ch == '\n') {
                strBuffer.append( (char) ch);
                retValue = 0;
                break;
            }
            else {
                strBuffer.append( (char) ch);
            }
        }
        return retValue;
    }
}
