package messager.common.util;

import java.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
/**
 * charset resource 파일을 읽어서 charset code를 java의 charset이나 mime charset으로 변환한다.
 */
public class CharsetTable
{
	
	private static final Logger LOGGER = LogManager.getLogger(CharsetTable.class.getName());
	
    //resource 파일 경로(messager/common/resource/charset.properties파일)
    private final static String CHARSET_RESOURCE = "messager.common.resource.charset";

    //정의 되지 않은 charset code 사용시 사용되는 charset
    private final static String DEFAULT_CHARSET = "euc-kr";

    //resource 파일을 읽어올 ResourceBundle객체
    private static ResourceBundle resources;

    //mime charset를 java의 charset으로 변환할 테이블
    private static HashMap mime2java;

    /**
     * resource 파일 로드
     */
    private static void load() {
        try {
            resources = ResourceBundle.getBundle(CHARSET_RESOURCE);
        }
        catch (MissingResourceException ex) {
        	LOGGER.error(ex);
            //ex.printStackTrace();
            System.exit(1);
        }
    }

    static {
        load();
        //charset변환 테이블의 데이타 설정
        mime2java = new HashMap(10);
        mime2java.put("iso-2022-cn", "ISO2022CN");
        mime2java.put("iso-2022-kr", "ISO2022KR");
        mime2java.put("utf-8", "UTF8");
        mime2java.put("utf8", "UTF8");
        mime2java.put("ja_jp.iso2022-7", "ISO2022JP");
        mime2java.put("ja_jp.eucjp", "EUCJIS");
        mime2java.put("euc-kr", "KSC5601");
        mime2java.put("euckr", "KSC5601");
        mime2java.put("us-ascii", "ISO-8859-1");
        mime2java.put("x-us-ascii", "ISO-8859-1");
    }

    /**
     * charset code로 resource 파일을 참조해서 mime charset를 얻는다.
     *
     * @param charsetCode
     *            charset code
     * @return mime charset name
     */
    public static String mimeCharsetName(int charsetCode) {
        String key = Integer.toString(charsetCode);
        String charset = resources.getString(key);
        if (charset == null) {
            return DEFAULT_CHARSET;
        }
        else {
            return charset;
        }
    }

    /**
     * charset code로 java의 charset를 얻는다.
     *
     * @param charsetCode
     *            charset code
     * @return java의 charset name
     */
    public static String javaCharsetName(int charsetCode) {
        //mime charset
        String mimecharset = mimeCharsetName(charsetCode);
        //java의 charset(charset 변환 테이블 참조)
        String javacharset = (String) mime2java.get(mimecharset);
        if (javacharset == null) {
            //mime charset과 java charset이 같다.
            javacharset = mimecharset;
        }
        return javacharset;
    }
}
