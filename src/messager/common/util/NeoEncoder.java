package messager.common.util;

import java.io.*;

public class NeoEncoder
{
    private final static int PAD = '=';
    private static final int bytePerAtom = 3;
    private final static char BASE64[] = {
        'H', 'G', 'F', 'E', 'D', 'C', 'B',
        'A', 'P', 'O', 'N', 'M', 'L', 'K', 'J', 'I', 'X', 'W', 'V', 'U',
        'T', 'S', 'R', 'Q', 'f', 'e', 'd', 'c', 'b', 'a', 'Z', 'Y', 'm',
        'n', 'l', 'k', 'j', 'i', 'h', 'g', 'v', 'u', 't', 's', 'r', 'q',
        'p', 'o', '3', '2', '1', '0', 'z', 'y', 'x', 'w', '/', '+', '9',
        '8', '7', '6', '5', '4'};

    /**
     * NeoBase64Encoder 객체 생성
     *
     * @param javaCharsetName
     *            String를 byte배열로 변환시 사용
     */
    /**
     * 라인 구분문자를 추가하지 않고 인코딩을 실행한다.
     *
     * @param line
     *            인코딩 될 String
     * @return 인코딩된 결과(라인이 구분되어지지 않는다)
     */
    public static String encode(String text, String charsetName)
        throws UnsupportedEncodingException {
        StringBuffer outBuffer = new StringBuffer();
        byte[] data = null;
        if (charsetName == null) {
            data = text.getBytes();
        }
        else {
            data = text.getBytes(charsetName);
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

    public static String encode(String text) {
        String encodeStr = null;

        try {
            encodeStr = encode(text, null);
        }
        catch (Exception ex) {
        }
        return encodeStr;
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
    private static void encodeAtom(byte[] data, int off, int len,
                                   StringBuffer outBuffer) {
        byte b1, b2, b3;
        int i = off;
        switch (len) {
            case 1:
                b1 = data[i];
                outBuffer.append(BASE64[ (b1 >>> 2) & 0x3F]);
                outBuffer.append(BASE64[ ( (b1 << 4) & 0x30)]);
                outBuffer.append(PAD);
                outBuffer.append(PAD);
                break;
            case 2:
                b1 = data[i++];
                b2 = data[i];
                outBuffer.append(BASE64[ (b1 >>> 2) & 0x3F]);
                outBuffer
                    .append(BASE64[ ( (b1 << 4) & 0x30) | ( (b2 >>> 4) & 0xF)]);
                outBuffer.append(BASE64[ ( (b2 << 2) & 0x3C)]);
                outBuffer.append(PAD);
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
