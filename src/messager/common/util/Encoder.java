package messager.common.util;

import java.io.UnsupportedEncodingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
/**
 * 컨텐츠 인코드를 하는 클래스의 수퍼 클래스로 8bit로 인코딩 한다. 특정 인코딩을 사용하려면 확장 클래스에서 구현해야 한다.
 */
public class Encoder
{
	private static final Logger LOGGER = LogManager.getLogger(Encoder.class.getName());
	
    protected String javaCharsetName;
    /**
     * Encoder 객체를 생성한다.
     *
     * @param aJavaCharsetName
     *            charset
     */
    public Encoder(String aJavaCharsetName) {
        javaCharsetName = aJavaCharsetName;
    }

    /**
     * Encoder 객체를 생성한다.
     */
    public Encoder() {
    }

    /**
     * 텍스트를 인코딩 한다(8bit로 인코딩 되므로 변환 처리를 하지 않는다.) 특정 인코딩을 실행할려면 확장 클래스에서 구현해야한다.
     *
     * @param plainText
     *            인코딩 될 텍스트
     * @return 8bit로 인코딩 된 텍스트
     */
    public String encodeText(String plainText)
        throws UnsupportedEncodingException {
        return plainText;
    }

    /**
     * 한 라인에 대한 인코딩을 실행한다. 특정 인코딩을 실행할려면 확장 클래스에서 구현해야 한다.
     *
     * @param plainText
     *            인코딩될 텍스트
     * @param 8bit로
     *            인코딩 된 텍스트
     */
    public String encodeLine(String plainText)
        throws UnsupportedEncodingException {
        return plainText;
    }

    /**
     * byte[] 배열을 8bit로 인코딩한다. 특정 인코딩을 사용하려면 확장 클래스에서 구현해야 한다.
     *
     * @param bytes
     *            인코딩 될 byte배열
     * @return 8bit로 인코딩된 텍스트, bytes를 charset으로 String 객체를 생성하지 못할 경우 null를
     *         리턴한다.
     */
    public String encode(byte[] bytes) {
        String str = null;
        try {
            if (javaCharsetName != null) {
                str = new String(bytes, javaCharsetName);
            }
        }
        catch (UnsupportedEncodingException ex) {
        	LOGGER.error(ex);
        }
        return str;
    }
}
