package messager.common.util;

import java.io.*;

/**
 * Mail Header 필드이 인코딩을 실행한다. 8bit로 지정되었을 경우 변환없이 리턴하고 base64(type 1)로 지정되었을 경우
 * base64로 인코딩 된다.
 */
public class MailHeaderEncoder
{
    //���δ� ���� ��
    private static final int charPerLine = 20;

    //���� ����
    private static final String lineSeparator = "\r\n";

    //mime charset
    //private String mimeCharset;

    //java charset
    //private String javaCharset;

    private String charsetType;

    //encode type(1: base64 ��Ÿ: 8bit)
    private String encType;

    //����� ���ڵ� �Ǿ��� ��� ���� ���� ���λ�
    private String prefixStr;

    //encode�� ������ encoder
    private Encoder encoder;

    /**
     * MailHeaderEncoder 객체를 생성한다.
     *
     * @param charsetType
     *            charset code
     * @param encType
     *            encode code
     */
    public MailHeaderEncoder(String charsetType, String encType) {
        //mime charset
        //mimeCharset = CharsetTable.mimeCharsetName(charsetType);

        //java charset
        //javaCharset = CharsetTable.javaCharsetName(charsetType);
        this.charsetType = charsetType;
        this.encType = encType;
        //Encoder 객체 생성
        /*
                        switch (encType) {
           case MimeTable.ENC_BASE64:
            encoder = new Base64Encoder(charsetType);
            break;
           default:
            encoder = new Encoder(charsetType);
            break;
          }
         */
        //if (encType.equals(MimeTable.ENC_BASE64)) {
        if (encType.equals(MimeTable.encType)) {
            encoder = new Base64Encoder(charsetType);
        }
        else {
            encoder = new Encoder(charsetType);
        }
        //encoding된 텍스트의 접두사로 사용될 String
        prefixStr = createPrefixString();
    }

    /**
     * encodeing된 텍스트의 접두사로 사용될 String를 생성한다. mime charset 과 Encode type이 포함된다.
     *
     * @return 인코딩 된 텍스트의 접두사
     */
    private String createPrefixString() {
        StringBuffer sbuffer = new StringBuffer();
        if (encoder instanceof Base64Encoder) {
            //sbuffer.append("=?").append(mimeCharset).append('?');
            sbuffer.append("=?").append(charsetType).append('?');
            sbuffer.append('B').append('?');
        }
        /*
         * else if (encoder instanceof QPEncoder) {
         * sbuffer.append(charset).append("=?").append(mimeCharset).append('?');
         * strbuffer.append('Q').append('?'); }
         */

        return sbuffer.toString();
    }

    /**
     * string를 Encoder객체로 인코딩 한다.
     *
     * @param str
     *            인코딩될 String
     * @return 인코딩된 String
     * @excpetion UnsupportedEncodingException
     */
    public String encodeText(String str)
        throws UnsupportedEncodingException {
        StringBuffer strbuf = new StringBuffer();
        if (encoder instanceof Base64Encoder) {
            encodeText(str, strbuf);
        }
        /*
         * else if (encoder instanceof QPEncoder) { encode(str, strbuf); }
         */
        else {
            strbuf.append(str);
        }
        return strbuf.toString();
    }

    /**
     * string를 인코딩 해서 StringBuffer에 결과를 채운다. 길이가 긴 string를 정해진 길이로 인코딩 해서 라인 구분
     * 문자를 채운다.
     *
     * @param str
     *            인코딩할 string
     * @param strbuf
     *            인코딩 된 결과를 저장할 StringBuffer 객체
     * @excpetion UnsupportedEncodingException
     */
    public void encodeText(String str, StringBuffer strbuf)
        throws UnsupportedEncodingException {
        int len = str.length();
        int offset = 0;
        int end = charPerLine;

        end = (end < len) ? end : len;
        encodeLine(str.substring(offset, end), strbuf);
        offset = end;
        end += charPerLine;

        while (offset < len) {
            strbuf.append(lineSeparator).append('\t');
            end = (end < len) ? end : len;
            encodeLine(str.substring(offset, end), strbuf);
            offset = end;
            end = offset + charPerLine;
        }
    }

    /**
     * 한 라인에 해당 하는 string를 인코딩 하고 접두사와 접미사를 추가한다.
     *
     * @param line
     *            인코딩을 실행할 string
     * @param strbuf
     *            인코딩된 결과를 저장할 StringBuffer객체
     * @excpetion UnsupportedEncodingException
     */
    private void encodeLine(String line, StringBuffer strbuf)
        throws UnsupportedEncodingException {
        strbuf.append(prefixStr);
        strbuf.append(encoder.encodeLine(line));
        strbuf.append("?=");
    }
}
