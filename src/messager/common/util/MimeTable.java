package messager.common.util;

import java.io.*;
import java.util.*;

/**
 * conf/content-types.properties 파일을 읽어서 파일명의 확장자로 Content-Type을 얻는다.
 * content-types.properties파일은 Content-Type: 파일확장자1, 파일 확장자2....의 형식 encode
 * Code로 encode Name를 변환한다.
 */
public class MimeTable
{
    // None
    //public static final String ENC_NONE = "002";

    // 7bit
    //public static final String ENC_7BIT = "001";

    // 8bit
    //public static final String ENC_8BIT = "002";

    //QP
    //public static final String ENC_QP = "003";

    // base64
    //public static final String ENC_BASE64 = "004";

    public static String encType;

    public static Encoder createEncoder(String type, String charsetName) {
        Encoder encoder = null;
        encType = type;
        //if (type.equals(ENC_BASE64)) {
        if (type.equals("Base64")) {
            encoder = new Base64Encoder(charsetName);
        }
        else {
            encoder = new Encoder(charsetName);
        }
        return encoder;
    }

    /**
     * Encode Name을 얻는다.
     *
     * @param encCode
     * @return Encode Name
     */
    public static String encodeName(String encCode) {
        /*
            String encName = null;
            switch (encCode) {
              case ENC_BASE64:
                encName = "base64";
                break;
              default:
                encName = "8bit";
                break;
            }
            return encName;
         */
        return encType;
    }

    private static final String UNKNOWN_TYPE = "unknown/unknown";

    private static final String DEFAULT_TYPE = "application/octet-stream";

    // mimetype ��ȯ ���̺�
    private static HashMap mimeMap;

    static {
        load();
    }

    /**
     * Properties 파일(conf/content-types.properties)를 읽는다.
     */
    private static void load() {
        if (mimeMap != null) {
            return;
        }

        File file = new File(System.getProperty("user.dir") +
                             File.separator + "conf" +
                             File.separator + "content-types.properties");
        mimeMap = new HashMap();
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(file));
            String line;
            while ( (line = in.readLine()) != null) {
                line = line.trim();
                if ( (line.length() == 0) || (line.charAt(0) == '#')) {
                	 //비워있는 라인이나 첫문자가 '#'인 라인은 주석 처리
                    line = null;
                    continue;
                }
                //mime type과 확장자 리스트의 구분문자는 ':'
                int index = line.indexOf(':');
                if (index > 0) {
                    String type = line.substring(0, index++).trim();
                    String elements = line.substring(index);
                    StringTokenizer st = new StringTokenizer(elements, ", ");
                    while (st.hasMoreTokens()) {
                        String extension = st.nextToken().toLowerCase();
                        mimeMap.put(extension, type);
                    }
                    st = null;
                }
                line = null;
            }
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
        finally {
            if (in != null) {
                try {
                    in.close();
                }
                catch (IOException ex) {
                }
            }
        }
    }

    /**
     * fileName의 확장자로 Content-Type를 얻는다.
     *
     * @param fileName
     * @return fileName의 MimeType
     */
    public static String getContentType(String fileName) {
        String contentType = null;
        int i = fileName.lastIndexOf('.');
        if (i != -1) {
            String ext = fileName.substring(i).toLowerCase();
            contentType = (String) mimeMap.get(ext);
            if (contentType == null) {
                contentType = UNKNOWN_TYPE;
            }
        }
        else {
            contentType = DEFAULT_TYPE;
        }

        return contentType;
    }
}
