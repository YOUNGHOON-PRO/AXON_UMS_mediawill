package messager.common.util;

import java.io.*;

/**
 * Base64 인코딩 실행하는 클래스
 */
public class Base64Encoder
    extends Encoder
{
    private final static int PAD = '=';
    private static final int bytePerAtom = 3;
    private static final int bytePerLine = bytePerAtom * 19;
    private static final String lineSeparator = "\r\n";

    private final static char BASE64[] = {
        'A', 'B', 'C', 'D', 'E', 'F', 'G',
        'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
        'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g',
        'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
        'u', 'v',
        'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8',
        '9', '+', '/'};

    /**
     * Base64Encoder 객체 생성
     *
     * @param javaCharsetName
     *            String를 byte배열로 변환시 사용
     */
    public Base64Encoder(String javaCharsetName) {
        super(javaCharsetName);
    }

    public Base64Encoder() {
        super();
    }

    /**
     * 텍스트를 Base64로 인코딩한다. 인코딩의 결과는 정해진 길이(76문자) 만큼 한 라인을 채워진다.
     *
     * @param plainText
     *            인코딩될 텍스트
     * @return 인코딩 된 결과(라인이 구분되어진다.)
     */
    public String encodeText(String plainText)
        throws UnsupportedEncodingException {
        byte[] bytes = null;

        //String를 byte배열로 변환
        if (javaCharsetName != null) {
            bytes = plainText.getBytes(javaCharsetName);
        }
        else {
            bytes = plainText.getBytes();
        }
        return encode(bytes);
    }

    /**
     * 라인 구분문자를 추가하지 않고 인코딩을 실행한다.
     *
     * @param line
     *            인코딩 될 String
     * @return 인코딩된 결과(라인이 구분되어지지 않는다)
     */
    public String encodeLine(String line)
        throws UnsupportedEncodingException {
        StringBuffer outBuffer = new StringBuffer();
        byte[] data = null;
        if (javaCharsetName == null) {
            data = line.getBytes();
        }
        else {
            data = line.getBytes(javaCharsetName);
        }

        int size = data.length;
        int offset = 0;
        int atomsize;
        int count = 0;

        while (size > 0) {
            atomsize = ( (size > bytePerAtom) ? bytePerAtom : size);
            encodeAtom(data, offset, atomsize, outBuffer);
            offset += atomsize;
            size -= atomsize;
            count += atomsize;
        }
        return outBuffer.toString();
    }

    /**
     * byte 배열로 인코딩을 실행하고 지정된 길이 만큼 line를 채워진다.
     *
     * @param data
     *            인코딩할 데이타 (byte배열)
     * @return 인코딩된 결과
     */
    public String encode(byte[] data) {
        StringBuffer outBuffer = new StringBuffer();
        int size = data.length;
        int offset = 0;
        int atomsize;
        int count = 0;

        while (size > 0) {
            atomsize = ( (size > bytePerAtom) ? bytePerAtom : size);
            encodeAtom(data, offset, atomsize, outBuffer);
            offset += atomsize;
            size -= atomsize;
            count += atomsize;

            if (count == bytePerLine) {
                outBuffer.append(lineSeparator);
                count = 0;
            }
        }

        outBuffer.append(lineSeparator);
        return outBuffer.toString();
    }

    /**
     * 3byte의 byte배열을 인코딩하여 4문자로 만들어서 StringBuffer에 채운다.
     *
     * @param data
     *            인코딩될 데이타
     * @param off
     *            인코딩을 시작할 위치
     * @param len
     *            인코딩을 실행할 데이타 길이
     * @param outBuffer
     *            인코딩 된 결과를 저장할 StringBuffer객체
     */
    private void encodeAtom(byte[] data, int off, int len,
                            StringBuffer outBuffer) {
        byte b1, b2, b3;
        int i = off;
        switch (len) {
            case 1:
                b1 = data[i];
                outBuffer.append(BASE64[ (b1 >>> 2) & 0x3F]);
                outBuffer.append(BASE64[ ( (b1 << 4) & 0x30)]);
                outBuffer.append('=');
                outBuffer.append('=');
                break;
            case 2:
                b1 = data[i++];
                b2 = data[i];
                outBuffer.append(BASE64[ (b1 >>> 2) & 0x3F]);
                outBuffer
                    .append(BASE64[ ( (b1 << 4) & 0x30) | ( (b2 >>> 4) & 0xF)]);
                outBuffer.append(BASE64[ ( (b2 << 2) & 0x3C)]);
                outBuffer.append('=');
                break;
            case 3:
                b1 = data[i++];
                b2 = data[i++];
                b3 = data[i];
                outBuffer.append(BASE64[ (b1 >>> 2) & 0x3F]);
                outBuffer
                    .append(BASE64[ ( (b1 << 4) & 0x30) | ( (b2 >>> 4) & 0xF)]);
                outBuffer
                    .append(BASE64[ ( (b2 << 2) & 0x3C) | ( (b3 >>> 6) & 0x3)]);
                outBuffer.append(BASE64[b3 & 0x3F]);
                break;
        }
    }
}
