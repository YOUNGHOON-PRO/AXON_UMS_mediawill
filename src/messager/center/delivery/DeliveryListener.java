package messager.center.delivery;

import java.io.*;
import java.net.*;

import messager.center.config.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * UnitInfo객체나 MessageInfo객체를 Generator에 전달 하기 위한 ServerSocket를 생성하고 Generator의
 * 접속을 대기한다. 접속이 이루어지면 DeliveryAgent 쓰레드를 실행한다.
 */
public class DeliveryListener
    extends Thread
{
	private static final Logger LOGGER = LogManager.getLogger(DeliveryListener.class.getName());
	
    private final static int backlog = 10;
    private static int port;
    private static String localHost;

    static {
        localHost = ConfigLoader.getString("local.host", null);
        port = ConfigLoader.getInt("delivery.listen.port", 2004);
    }

    private static DeliveryListener listener;

    /**
     * DeliveryListener의 쓰레드를 실행한다.
     */
    public static void execute()
        throws IOException {
        synchronized (DeliveryListener.class) {
            if (listener == null) {
                listener = new DeliveryListener();
            }
            if (!listener.isAlive()) {
                listener.start();
            }
        }
    }

    private ServerSocket server;

    /**
     * DeliveryServer 객체를 생성한다 Generator로 부터 접속을 받는 ServerSocket를 생성한다. Listen
     * Port 번호는 설정 파일의 delivery.listen.port로 설정되고, 디폴트는 2004가 사용된다. 특정 Address로만
     * Listen 할 경우 설정 파일에 local.host에 호스트 네임또는 IP를 지정하면 되고 backlog는 10이 디폴트로
     * 사용된다.
     *
     * @throw IOException ServerSocket를 생성 할 수 없을 경우나 지정된 잘못된 LocalHost를 지정하였을
     *        경우 발생한다.
     */
    private DeliveryListener()
        throws IOException {
        if (localHost == null) {
            server = new ServerSocket(port);
        }
        else {
            InetAddress inetAddress = InetAddress.getByName(localHost);
            server = new ServerSocket(port, backlog, inetAddress);
        }
    }

    /**
     * Generator로 부터 접속을 받아 들이고 DeliveryAgent의 쓰레드를 생성하는 쓰레드를 실행한다.
     */
    public void run() {
        while (true) {
            try {
                Socket socket = server.accept();
                DeliveryAgent agent = new DeliveryAgent(socket);
                agent.start();
            }
            catch (IOException ex) {
            	LOGGER.error(ex);
                //System.err.println(ex.getMessage());
            }
        }
    }
}
