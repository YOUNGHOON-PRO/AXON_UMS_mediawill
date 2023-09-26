package messager.mailsender.sendlog;

import org.apache.commons.daemon.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SendLogDaemon
    implements Daemon
{
	private static final Logger LOGGER = LogManager.getLogger(SendLogDaemon.class.getName());
    private Inserter inserter;

    public SendLogDaemon() {
    }

    public void init(DaemonContext context)
        throws Exception {
        println("SendLogDaemon instance: init()");
    }

    public void start() {
        println("SendLogDaemon instance: start(): in");

        inserter = new Inserter();
        inserter.main(new String[1]);

        println("SendLogDaemon instance: start(): out");
    }

    public void stop()
        throws Exception {
        println("SendLogDaemon instance: stop(): in");

        inserter.shutdown();

        println("SendLogDaemon instance: stop(): out");
    }

    public void destroy() {
        println("SendLogDaemon instance: destroy(): in");

        println("SendLogDaemon instance: destroy(): out");
    }

    private String getCurrentTime() {
        java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat(
            "yyyy/MM/dd HH:mm:ss", java.util.Locale.US);
        return fmt.format(new java.util.Date());
    }

    private void println(String msg) {
        //System.out.println(getCurrentTime() + " : " + msg);
    	LOGGER.info(getCurrentTime() + " : " + msg);
    	
    }

}
