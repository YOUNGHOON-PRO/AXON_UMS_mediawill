package messager.center.control;

import java.net.*;

import messager.center.config.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
public class ControlListener
    extends Thread
{
	
	private static final Logger LOGGER = LogManager.getLogger(ControlListener.class.getName());
	
    private ServerSocket server;

    public ControlListener()
        throws Exception {

        int port = ConfigLoader.getInt("control.listen.port", -1);
        String host = ConfigLoader.getString("control.host.name", null);
        try {
            if (host != null) {
                InetAddress address = InetAddress.getByName(host);
                server = new ServerSocket(port, 2, address);
            }
            else {
                server = new ServerSocket(port);
            }
        }
        catch (Exception e) {
        	LOGGER.error(e);
            StringBuffer err = new StringBuffer();
            err.append("ControlListener() - ")
                .append("host : ").append(host).append(" - ")
                .append("port : ").append(port).append(" - ")
                .append(e.getMessage());
            throw new Exception(err.toString());
        }
    }

    public void run() {
        while (true) {
            try {
                Socket socket = server.accept();
                MessageController controller = new MessageController(socket);
                controller.start();
            }
            catch (Exception ex) {LOGGER.error(ex);}
        }
    }
}
