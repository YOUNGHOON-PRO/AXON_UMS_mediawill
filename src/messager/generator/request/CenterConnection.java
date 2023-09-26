package messager.generator.request;

import java.io.*;
import java.net.*;

import messager.generator.config.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * 이 클래스는 UnitInfo객체와 MessageInfo객체를 전송 받기 위한 MessageCenter와의 접속을 위한 클래스이다.
 * MessageCenter와 접속된 Socket을 통한 stream를 제공한다. MessageCenter의 delivery 패키지 참조
 */
class CenterConnection
{
	private static final Logger LOGGER = LogManager.getLogger(CenterConnection.class.getName());
	
    //MessageCenter의 host
    private static String centerHost;

    //MessageCenter의 port
    private static int centerPort;

    //LocalHost의 host Name(IPAddress)
    private static String localHost;

    static {

        centerHost = ConfigLoader.getString("center.host", null);

        centerPort = ConfigLoader.getInt("request.connect.port", -1);

        if (centerHost == null || centerPort == -1) {

            System.err.println("center.host : " + centerHost);

            System.err.println("request.connect.port : " + centerPort);

            System.exit(1);

        }

        localHost = ConfigLoader.getString("local.host", null);

    }

    private Socket socket;

    private InputStream in;

    private OutputStream out;

    /**
     * Socket Open하여 객체를 얻는다.
     */
    public CenterConnection()
        throws UnknownHostException, IOException {

        if (localHost == null) {

            //localHost가 지정되어 있지 않을 경우
            socket = new Socket(centerHost, centerPort);

        }
        else {

            //localHost가 지정되어 있을 경우(두개이상의 Network에 연결되어 있을 경우)
            InetAddress centerInetAddr = InetAddress.getByName(centerHost);

            InetAddress localInetAddr = InetAddress.getByName(localHost);

            socket = new Socket(centerInetAddr, centerPort, localInetAddr, 0);

        }

    }

    /**
     * socket에서 데이타를 읽기 위한 InputStream를 얻는다.
     */
    public InputStream getInputStream()
        throws IOException {

        if (in == null) {

            in = new BufferedInputStream(socket.getInputStream());

        }

        return in;

    }

    /**
     * Socket에 데이타를 쓰기 위한 OutputStream를 얻는다.
     */
    public OutputStream getOutputStream()
        throws IOException {

        if (out == null) {

            out = new BufferedOutputStream(socket.getOutputStream());

        }

        return out;

    }

    /**
     * Socket를 닫는다.
     */
    public void close() {

        if (in != null) {

            try {

                in.close();

            }
            catch (IOException ex) {
            	LOGGER.error(ex);
            }

            in = null;

        }

        if (out != null) {

            try {

                out.close();

            }
            catch (IOException ex) {
            	LOGGER.error(ex);
            }

            out = null;

        }

        if (socket != null) {

            try {

                socket.close();

            }
            catch (IOException ex) {
            	LOGGER.error(ex);
            }

            socket = null;

        }

    }

    /**
     * 지정된 길이의 data를 읽는다.
     *
     * @param buf
     *            데이타를 읽어서 채울 버퍼
     * @param offset
     *            데이타를 채울 buf의 시작위치
     * @param len
     *            읽을 데이타의 길이
     * @return 읽은 데이타의 길이, -1(stream의 끝)을 읽었을 경우 -1를 리턴
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

        return count;

    }

    /**
     * 지정된 길이의 byte[]를 socket으로 보낸다.
     *
     * @param buf
     *            보낼 데이타가 들어있는 버퍼
     * @param offset
     *            보낼 데이타의 시작 위치
     * @param len
     *            보낼 데이타의 길이
     */
    public void sendbytes(byte[] buf, int offset, int len)
        throws IOException {

        OutputStream stream = getOutputStream();

        stream.write(buf, offset, len);

    }

    /**
     * Socket버퍼에 있는 보낼 데이타를 모두 보내고 버퍼를 비운다.
     */
    public void flush()
        throws IOException {

        OutputStream stream = getOutputStream();

        stream.flush();

    }

}
