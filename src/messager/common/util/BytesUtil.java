package messager.common.util;

/**
 * Number 형(short, int, long) 데이타와 big endian형식으로 byte 배열를 상호 변환 하는 클래스 java에서
 * big endian 형식의 메모리 구조를 사용된다.
 */
public final class BytesUtil
{
    /**
     * int형 데이타를 byte배열로 변환한다.
     *
     * @param n
     *            변환될 int형 데이타
     * @param buf
     *            변환된 결과를 저장할 byte배열
     * @param offset
     *            byte배열의 변환된 결과가 저장될 시작 위치
     */
    public static void int2bytes(int n, byte[] buf, int offset) {
        int i = offset;
        buf[i++] = (byte) ( (n >>> 24) & 0xFF);
        buf[i++] = (byte) ( (n >>> 16) & 0xFF);
        buf[i++] = (byte) ( (n >>> 8) & 0xFF);
        buf[i] = (byte) ( (n >>> 0) & 0xFF);
    }

    /**
     * long 형 데이타를 byte배열로 변환
     *
     * @param n
     *            long형 데이타
     * @param buf
     *            byte배열
     * @param offset
     *            byte배열의 변환된 결과가 저장될 시작 위치
     */
    public static void long2bytes(long n, byte[] buf, int offset) {
        int i = offset;
        buf[i++] = (byte) ( (n >>> 56) & 0xFF);
        buf[i++] = (byte) ( (n >>> 48) & 0xFF);
        buf[i++] = (byte) ( (n >>> 40) & 0xFF);
        buf[i++] = (byte) ( (n >>> 32) & 0xFF);
        buf[i++] = (byte) ( (n >>> 24) & 0xFF);
        buf[i++] = (byte) ( (n >>> 16) & 0xFF);
        buf[i++] = (byte) ( (n >>> 8) & 0xFF);
        buf[i] = (byte) ( (n >>> 0) & 0xFF);
    }

    /**
     * short형 데이타를 byte배열로 변환
     *
     * @param n
     *            short형 데이타
     * @param buf
     *            byte배열
     * @param offset
     *            byte배열의 변환된 결과가 저장될 시작 위치
     */
    public static void short2bytes(short n, byte[] buf, int offset) {
        int i = offset;
        buf[i++] = (byte) ( (n >>> 8) & 0xFF);
        buf[i] = (byte) ( (n >>> 0) & 0xFF);
    }

    /**
     * byte배열을 int형으로 변환
     *
     * @param buf
     *            byte배열
     * @param offset
     *            byte배열에서 변환될 시작 위치
     * @return 변환된 int형 데이타
     */
    public static int bytes2int(byte[] buf, int offset) {
        int i = offset;
        int n = 0;
        n |= (int) buf[i++] << 24;
        n |= ( (int) buf[i++] & 0xFF) << 16;
        n |= ( (int) buf[i++] & 0xFF) << 8;
        n |= ( (int) buf[i++] & 0xFF) << 0;
        return n;
    }

    /**
     * byte배열을 long형으로 변환
     *
     * @param buf
     *            byte배열
     * @param offset
     *            byte배열에서 변환될 시작 위치
     * @return 변환된 long형 데이타
     */
    public static long bytes2long(byte[] buf, int offset) {
        int i = offset;
        long n = 0;
        n |= (long) buf[i++] << 56;
        n |= ( (long) buf[i++] & 0xFF) << 48;
        n |= ( (long) buf[i++] & 0xFF) << 40;
        n |= ( (long) buf[i++] & 0xFF) << 32;
        n |= ( (long) buf[i++] & 0xFF) << 24;
        n |= ( (long) buf[i++] & 0xFF) << 16;
        n |= ( (long) buf[i++] & 0xFF) << 8;
        n |= ( (long) buf[i++] & 0xFF) << 0;
        return n;
    }

    /**
     * byte배열을 short형으로 변환
     *
     * @param buf
     *            byte배열
     * @param offset
     *            byte배열에서 변환될 시작 위치
     * @return 변환된 short형 데이타
     */
    public static short bytes2short(byte[] buf, int offset) {
        int i = offset;
        short n = 0;

        n |= (short) buf[i++] << 8;
        n |= ( (short) buf[i++] & 0xFF) << 0;
        return n;
    }

}
