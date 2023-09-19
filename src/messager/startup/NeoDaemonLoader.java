package messager.startup;

import java.io.*;
import java.net.*;
import java.util.logging.*;

import org.apache.commons.daemon.support.*;

public class NeoDaemonLoader
{
    private DaemonLoader loader;
    private String daemonClass;
    private Logger logger;
    private ShutdownListener downListener;

    public NeoDaemonLoader(String daemonClass, int port)
        throws IOException {
        this.daemonClass = daemonClass;
        downListener = new ShutdownListener(daemonClass, port);
        logger = Logger.getLogger("messager.startup.Bootstrap");
    }

    public void start() {

        Thread listenerThread = new Thread(downListener);
        listenerThread.start();

        boolean successStart;

        successStart = loader.load(daemonClass, null);
        if (successStart) {
            successStart = loader.start();
        }
        if (!successStart) {
            throw new RuntimeException("NeoDaemonLoader failed to Start : " + daemonClass);
        }
    }

    public void stop() {

        boolean successStop;

        logger.fine("stop start : " + loader);
        successStop = loader.stop();
        logger.fine("successStop:" + successStop);
        if (successStop) {
            successStop = loader.destroy();
        }

        if (!successStop) {
            throw new RuntimeException("NeoDaemonLoader failed to stop : " + daemonClass);
        }
    }

    private class ShutdownListener
        implements Runnable
    {

        private ServerSocket shutdownSocket;
        private boolean stopping;
        private String stopMsg;

        public ShutdownListener(String stopMsg, int port)
            throws IOException {
            shutdownSocket = new ServerSocket(port);
            this.stopMsg = stopMsg;
        }

        public void run() {
            logger.fine("ShutdownListener instance: run() : 진입");
            Socket socket = null;
            BufferedReader br = null;
            try {
                while (!stopping) {
                    socket = shutdownSocket.accept();
                    try {
                        br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        if (br.readLine().equals(stopMsg)) {
                            stopping = true;
                        }
                    }
                    catch (IOException e) {
                        logger.log(Level.SEVERE, "ShutdownListener instance: run()", e);
                    }
                    finally {
                        if (br != null) {
                            try {
                                br.close();
                            }
                            catch (IOException e) {}
                        }
                        if (socket != null) {
                            try {
                                socket.close();
                            }
                            catch (IOException ex) {}
                        }
                    }
                }
            }
            catch (IOException ex) {
                logger.log(Level.SEVERE, "ShutdownListener instance: run()", ex);
            }
            finally {
                if (shutdownSocket != null) {
                    try {
                        shutdownSocket.close();
                    }
                    catch (IOException ex) {}
                }
            }
            NeoDaemonLoader.this.stop();
            logger.fine("ShutdownListener instance: run() : 종료");
        }
    }

}
