package messager.generator;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;

public class GeneratorDaemon
    implements Daemon
{
    private Main main;

    public GeneratorDaemon() {
    }

    public void init(DaemonContext context)
        throws Exception {
        println("GeneratorDaemon instance: init()");
    }

    public void start() {
        println("GeneratorDaemon instance: start(): in");

        try {
            main = new Main();
            main.main(new String[1]);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        println("GeneratorDaemon instance: start(): out");
    }

    public void stop()
        throws Exception {
        println("GeneratorDaemon instance: stop(): in");

        main.shutdown();

        println("GeneratorDaemon instance: stop(): out");
    }

    public void destroy() {
        println("GeneratorDaemon instance: destroy(): in");

        println("GeneratorDaemon instance: destroy(): out");
    }

    private String getCurrentTime() {
        java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat(
            "yyyy/MM/dd HH:mm:ss", java.util.Locale.US);
        return fmt.format(new java.util.Date());
    }

    private void println(String msg) {
        System.out.println(getCurrentTime() + " : " + msg);
    }
}
