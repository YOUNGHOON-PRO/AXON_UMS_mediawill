package messager.center.creator;

import java.io.*;

import messager.center.creator.parse.*;
import messager.common.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
/**
 * 이 클래스는 Mail Template FilePath로 FileRequest를 이용해서 Stream를 얻고 Stream를
 * TemplateParser로 분석해서 Template 객체를 생성한다.
 */

class ContentFetcher
{
	
	private static final Logger LOGGER = LogManager.getLogger(ContentFetcher.class.getName());
	
    /** template 파일 경로 (요청 경로) */
    private String path;

    private String contentTypeCode;

    /** Message의 Charset 코드 */
    private String charsetCode;

    /** Encoding code (Base64 : 2, 8bit : 0) */
    private String bodyEncCode;

    /**
     * Template객체를 생성하는 TemplateFetcher 객체를 생성한다.
     *
     * @param templatePath
     *            템플릿의 파일 경로
     * @param aCharsetCode
     *            Message의 컨텐츠 charset code
     * @param bodyEncCode
     *            Message의 컨텐츠 encoding code
     */
    public ContentFetcher(String path,
                          String contentTypeCode,
                          String charsetCode,
                          String bodyEncCode) {
        this.path = path;
        this.charsetCode = charsetCode;
        this.bodyEncCode = bodyEncCode;
        this.contentTypeCode = contentTypeCode;
    }

    /**
     * template 에 대한 파일 경로로 stream(byte배열) 요청
     *
     * @param fileRequester
     *            템플릿의 파일 경로로 템플릿 요청
     * @param parser
     *            템플릿에서 MergeKey를 추출한다.
     * @return 템플릿에 해당하는 Template 객체
     * @exception CreatorException
     */
    public TemplateContent fetch(FileRequester fileRequester,
                                 TemplateParser parser)
        throws Exception {
        TemplateContent templateContent = null;
        Exception exception = null;

        // charset Code를 java의 Charset으로 변환
        //String javaCharsetName = CharsetTable.javaCharsetName(charsetCode);

        Reader reader = null;

        try {
            //Stream 요청
            InputStream stream = fileRequester.request(path);
            if (stream != null) {
                // Reader 객체 생성
                //reader = new InputStreamReader(stream, javaCharsetName);
                //reader = new InputStreamReader(stream, charsetCode);
                reader = new InputStreamReader(stream, "UTF-8");

                // MergeElement (mergeKey와 텍스트로 구분하는데, 채울수 있는 mergekey는 치환, 예로 고객정보는 없어서 치환 X) List 객체 생성
                Template template = parser.parse(reader);

                // Template 객체 생성
                templateContent = new TemplateContent(template,
                    contentTypeCode, charsetCode, bodyEncCode);
            }
        }
        catch (Exception ex) {
        	LOGGER.error(ex);
            exception = ex;
        }
        finally {
            if (reader != null) {
                try {
                    // Reader 객체 close
                    // FileRequester의 컨넥션은 닫혀지지 않는다.
                    reader.close();
                }
                catch (IOException ex) {
                	LOGGER.error(ex);
                }
            }
        }

        if (exception != null) {
            throw exception;
        }

        return templateContent;
    }
    
    
    /**
     * template 에 대한 파일 경로로 stream(byte배열) 요청
     *
     * @param fileRequester
     *            템플릿의 파일 경로로 템플릿 요청
     * @param parser
     *            템플릿에서 MergeKey를 추출한다.
     * @return 템플릿에 해당하는 Template 객체
     * @exception CreatorException
     */
    public TemplateContent2 fetch2(FileRequester fileRequester,
                                 TemplateParser parser, Message message)
        throws Exception {
        TemplateContent2 templateContent2 = null;
        Exception exception = null;

        // charset Code를 java의 Charset으로 변환
        //String javaCharsetName = CharsetTable.javaCharsetName(charsetCode);

        Reader reader = null;

        try {
            //Stream 요청
//            InputStream stream = fileRequester.request(path);
            
            String msg = (String)message.webagent_sourceUrl;
            
//            if (stream != null) {
                // Reader 객체 생성
                //reader = new InputStreamReader(stream, javaCharsetName);
                //reader = new InputStreamReader(stream, charsetCode);
//                reader = new InputStreamReader(stream, "UTF-8");

                // MergeElement (mergeKey와 텍스트로 구분하는데, 채울수 있는 mergekey는 치환, 예로 고객정보는 없어서 치환 X) List 객체 생성
                Template2 template2 = parser.parse2(msg);

                // Template 객체 생성
                templateContent2 = new TemplateContent2(template2,
                    contentTypeCode, charsetCode, bodyEncCode);
//            }
        }
        catch (Exception ex) {
        	LOGGER.error(ex);
            exception = ex;
        }
        finally {
            if (reader != null) {
                try {
                    // Reader 객체 close
                    // FileRequester의 컨넥션은 닫혀지지 않는다.
                    reader.close();
                }
                catch (IOException ex) {
                	LOGGER.error(ex);
                }
            }
        }

        if (exception != null) {
            throw exception;
        }

        return templateContent2;
    }
}
