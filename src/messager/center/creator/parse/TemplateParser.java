/*
 * Copyright 2003 Neocast, Inc All rights reserved. Neocast Engine terms.
 */
package messager.center.creator.parse;

/**
 * 이 클래스는 템플릿을 분석해서 머지부분과 일반 텍스트 부분을 구분해서 ArrayList에 리스트화 해서 저장한다.
 * <p>
 * 머지키에 대한 데이타가 Message에 대해서 유일할 때에는 실제 값을 넣는다.
 *
 * @author Park MinChan
 * @version 2003/10/16
 */
import java.io.*;
import java.util.*;

import messager.common.*;

public class TemplateParser
{
    private final static String lineSeparator = "\r\n";

    private final static int PUSHBACK_CHAR_SIZE = 1024;

    private Message message;

    /**
     * 생성자는 고정된 머지키와 데이타를 HashMap에 지정되고 ArrayList에 순서화된 검색필드들을 저장한다.
     */
    public TemplateParser(Message message) {
        this.message = message;
    }

    /**
     * reader에서 템플릿을 읽어서 한라인씩 머지키 부분과 일반 텍스트 부분를 분리한다. 여러라인으로 이루어진 컨텐츠를 분석하기 위해
     * 사용된다.
     *
     * @param reader
     */
    public Template parse(Reader reader)
        throws Exception {
        HashMap keyMap = message.keyMap;
        BufferedReader in = null;
        if (reader == null) {
            throw new NullPointerException();
        }

        //BufferedReader로 변환한다.
        if (reader instanceof BufferedReader) {
            in = (BufferedReader) reader;
        }
        else {
            in = new BufferedReader(reader);
        }

        //분석된 결과를 저장할 ArrayList객체
        Template template = new Template();
        try {
            String line;
            //한라인씩 분석하기 위해서 LineParser 객체 선언
            LineParser lineParser = new LineParser(message, template);

            //MergeKey가 아닌 텍스트를 저장하기 위한 StringBuffer 객체
            StringBuffer normalTextBuffer = new StringBuffer();
            // System.out.println("======");
            while ( (line = in.readLine()) != null) {
                // System.out.println(line);
                //한라인씩 읽어서 분석한다.
                lineParser.parse(line, normalTextBuffer);
                //lineParser.parse(new String(line.getBytes(),"utf-8"), normalTextBuffer);

                //line의 끝
                normalTextBuffer.append(lineSeparator);
            }
            if (normalTextBuffer.length() > 0) {
                //템플릿의 끝 부분에 MergeKey가 아닌 텍스트 확인
                MergeElement element = new MergeElement(normalTextBuffer
                    .toString());
                template.add(element);
            }
            //System.out.println(normalTextBuffer.toString());
            // System.out.println("======");
        }
        catch (Exception ex) {
            throw ex;
        }
        return template;
    }

    /**
     * 메일 제목과 같은 라인으로 처리 되지 않는 (라인 구분String이 포함되지 않는) 텍스트를 분석한다. 끝에 라인 구분문자를 넣지
     * 않는다.
     *
     * @param text
     *            머지키포함 될 수 있는 텍스트
     */
    public Template parse(String text)
        throws Exception {
        Template template = new Template();
        LineParser lineParser = new LineParser(message, template);
        StringBuffer normalTextBuffer = new StringBuffer();
        lineParser.parse(text, normalTextBuffer);
        if (normalTextBuffer.length() > 0) {
            MergeElement element = new MergeElement(normalTextBuffer.toString());
            template.add(element);
        }
        return template;
    }
    
    
    /**
     * 메일 제목과 같은 라인으로 처리 되지 않는 (라인 구분String이 포함되지 않는) 텍스트를 분석한다. 끝에 라인 구분문자를 넣지
     * 않는다.
     *
     * @param text
     *            머지키포함 될 수 있는 텍스트
     */
    public Template2 parse2(String text)
        throws Exception {
        Template2 template2 = new Template2();
        LineParser lineParser = new LineParser(message, template2);
        StringBuffer normalTextBuffer = new StringBuffer();
        lineParser.parse2(text, normalTextBuffer);
        if (normalTextBuffer.length() > 0) {
            MergeElement element = new MergeElement(normalTextBuffer.toString());
            template2.add(element);
        }
        return template2;
    }
    
}
