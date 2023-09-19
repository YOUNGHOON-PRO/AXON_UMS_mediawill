package messager.center.creator;

import java.io.*;

/**
 * InputStream에서 지정된 길이만큼만 처리한다. FileRequester에서 한개의 컨넥션으로 여러 파일을 요청하기 위하여 사용된다.
 */
public class ResponseStream
    extends InputStream
{
    //stream의 길이
    private int count;
    //데이타를 읽어올 InputStream
    private InputStream in;
    //stream의 끝인가 체크
    private boolean isClosed = false;
    //현재 읽은 위치
    private int pos;

    /**
     * ResponseStream 객체 생성
     *
     * @param stream
     *            데이타를 읽어 올 InputStream 객체
     * @param size
     *            읽어올 데이타의 길이
     */
    public ResponseStream(InputStream stream, int size) {
        in = stream;
        count = size;
        pos = 0;
    }

    /**
     * 1 byte를 읽는다.
     *
     * @return 읽은 데이타의 byte, stream의 끝일 경우 -1
     */
    public synchronized int read()
        throws IOException {
        ensureOpen();
        if (pos < count) {
            pos++;
            return in.read();
        }

        return -1;
    }

    /**
     * 지정된 byte배열로 주어진 위치에서 주어진 길이만큼 InputStream에서 읽는다.
     *
     * @param b
     *            지정된 byte배열
     * @param offset
     *            byte배열에 저장할 시작위치
     * @param len
     *            읽을 byte의 최대 길이
     * @return 읽은 byte의 길이
     */
    public synchronized int read(byte b[], int offset, int len)
        throws IOException {
        ensureOpen();
        if (b == null) {
            throw new NullPointerException();
        }
        else if ( (offset < 0) || (offset > b.length) || (len < 0)
                 || ( (offset + len) > b.length) || ( (offset + len) < 0)) {
            throw new IndexOutOfBoundsException();
        }

        if (pos >= count) {
            return -1;
        }

        if (pos + len > count) {
            len = count - pos;
        }

        if (len <= 0) {
            return 0;
        }

        int rc = in.read(b, offset, len);
        pos += rc;
        return rc;
    }

    public synchronized long skip(long n) {
        return 0;
    }

    public synchronized int available()
        throws IOException {
        ensureOpen();
        return in.available();
    }

    /**
     * stream이 닫혀있나 확인한다.
     *
     * @exception IOException
     */
    private void ensureOpen()
        throws IOException {
        if (in == null || isClosed) {
            throw new IOException("Stream closed");
        }
    }

    /**
     * stream 을 close한다.
     */
    public void close() {
        isClosed = true;
    }
}