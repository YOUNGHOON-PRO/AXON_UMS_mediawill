package messager.mailsender;

import org.apache.commons.daemon.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MailSenderDaemon
    implements Daemon
{
	private static final Logger LOGGER = LogManager.getLogger(MailSenderDaemon.class.getName());
	
    private MailSender mailsender;

    public MailSenderDaemon() {
    }

    public void init(DaemonContext context)
        throws Exception {
        println("MailSenderDaemon instance: init()");
    }

    public void start() {
        println("MailSenderDaemon instance: start(): in");

        mailsender = new MailSender();
        mailsender.main(new String[1]);

        println("MailSenderDaemon instance: start(): out");
    }

    public void stop()
        throws Exception {
        println("MailSenderDaemon instance: stop(): in");

        mailsender.shutdown();

        println("MailSenderDaemon instance: stop(): out");
    }

    public void destroy() {
        println("MailSenderDaemon instance: destroy(): in");

        println("MailSenderDaemon instance: destroy(): out");
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
