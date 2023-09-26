package messager.center.result;

import java.io.*;
import java.net.*;

import messager.center.config.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * ServerSocket를 생성하고 Genreator로 부턴 접속을 대기하다가 접속이 이루어지면
 * ResultReceiver 쓰레드를 생성하여 발송결과를 전달 받는다.
 *
 * @author Administrator
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ResultListener
    extends Thread
{
	private static final Logger LOGGER = LogManager.getLogger(ResultListener.class.getName());
	
    /** 접속을 처리할 ServerSocket의 포트번호 */
    private static int port;

    static {
        port = ConfigLoader.getInt("result.listen.port", -1);
    }

    /** 접속을 처리할 소켓 */
    private ServerSocket server;

    /** 객체를 생성한다.
     * @throws IOException
     */
    public ResultListener()
        throws IOException {
        server = new ServerSocket(port);
    }

    /** 쓰레드를 실행한다 */
    public void run() {
        try {
            while (true) {
                Socket socket = server.accept();
                ResultReceiver receiver = new ResultReceiver(socket);
                receiver.start();
            }
        }
        catch (IOException ex) {
        	LOGGER.error(ex);
            //ex.printStackTrace();
        }
    }
}
