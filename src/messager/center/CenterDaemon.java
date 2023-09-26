package messager.center;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CenterDaemon
    implements Daemon
{
	
	private static final Logger LOGGER = LogManager.getLogger(CenterDaemon.class.getName());
	
    private Main main;

    public CenterDaemon() {
    }

    public void init(DaemonContext context)
        throws Exception {
        println("CenterDaemon instance: init()");
    }

    public void start() {
        println("CenterDaemon instance: start(): in");

        main = new Main();
        main.main(new String[1]);

        println("CenterDaemon instance: start(): out");
    }

    public void stop()
        throws Exception {
        println("CenterDaemon instance: stop(): in");

        main.shutdown();

        println("CenterDaemon instance: stop(): out");
    }

    public void destroy() {
        println("CenterDaemon instance: destroy(): in");

        println("CenterDaemon instance: destroy(): out");
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
