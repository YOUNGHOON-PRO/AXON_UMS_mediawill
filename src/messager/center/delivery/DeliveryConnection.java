package messager.center.delivery;

import java.io.*;
import java.net.*;

/**
 * Generator의 요청에 의해 UnitInfo객체와 MessageInfo객체를 전달 하기 위한 Socket 를 생성하고 입출력을 버퍼링된
 * 스트림으로 처리한다.
 */
public class DeliveryConnection
{
    private Socket socket;
    private InputStream in;
    private OutputStream out;

    /**
     * DeliveryConnection 객체를 생성
     *
     * @param aSocket
     *            Generator와 접속된 Socket객체
     */
    public DeliveryConnection(Socket aSocket) {
        socket = aSocket;
    }

    /**
     * Socket를 위한 InputStream를 버퍼링된 BufferedInputStream으로 얻는다.
     *
     * @return Socket에 bytes를 읽기 위한 Input Stream
     */
    public InputStream getInputStream()
        throws IOException {
        if (in == null) {
            in = new BufferedInputStream(socket.getInputStream());
        }
        return in;
    }

    /**
     * stream 를 write하기 위한 OutputStream를 버퍼링된 BufferedOutputStream으로 얻는다.
     *
     * @return Socket에 bytes를 쓰기위한 output Stream
     */
    public OutputStream getOutputStream()
        throws IOException {
        if (out == null) {
            out = new BufferedOutputStream(socket.getOutputStream());
        }
        return out;
    }

    /**
     * connection close(Socket close)
     */
    public void close() {
        if (in != null) {
            try {
                in.close();
            }
            catch (IOException ex) {
            }
            in = null;
        }

        if (out != null) {
            try {
                out.close();
            }
            catch (IOException ex) {
            }
            out = null;
        }

        if (socket != null) {
            try {
                socket.close();
            }
            catch (IOException ex) {
            }
            socket = null;
        }
    }

    /**
     * 지정된 길이를 bytes를 읽는다.
     *
     * @param buf
     *            bytes를 읽어서 저장할 byte 배열
     * @param offset
     *            저장할 시작위치
     * @param len
     *            읽을 byte의 길이
     * @return 읽은 byte의 길이
     * @exception IOException
     */
    public int readbytes(byte[] buf, int offset, int len)
        throws IOException {
        InputStream stream = getInputStream();
        int i = offset;
        int count = 0;
        int c;
        for (; count < len; count++) {
            c = stream.read();
            if (c == -1) {
                return -1;
            }
            buf[i++] = (byte) c;
        }
        if (count != len) {
            throw new IOException("stream read count unmatched");
        }
        return count;
    }

    /**
     * 지정된 바이트 배열의 offsetf 로부터 시작되는 len 바이트를, 버퍼링 된 출력 스트림에 기입합니다.
     *
     * @param buf
     *            데이터
     * @param offset
     *            데이터의 시작 offset
     * @param write되는
     *            바이트 수
     * @exception IOException
     *                입출력 에러가 발생하였을 경우
     */
    public void sendbytes(byte[] buf, int offset, int len)
        throws IOException {
        OutputStream stream = getOutputStream();
        stream.write(buf, offset, len);
    }

    /**
     * 버퍼링 된 출력 스트림을 플래시 한다. 이 처리에 의해, 버퍼의 내용은 모두 기본이 되는 출력 스트림에 기입해집니다.
     *
     * @exception IOException
     *                입출력 에러가 발생했을 경우
     */
    public void flush()
        throws IOException {
        OutputStream stream = getOutputStream();
        stream.flush();
    }

    public String getAddress() {
        InetAddress address = socket.getInetAddress();
        return address.getHostAddress();
    }
}
