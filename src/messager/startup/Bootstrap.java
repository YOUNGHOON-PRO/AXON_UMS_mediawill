package messager.startup;

/**
 * Daemon 을 구동시키는 작업을 한다.
 * 모듈별로 작업종료를 위한 ShutdownListener 의 포트를 다르게 하여 구동한다.
 */

import java.io.*;
import java.net.*;
import java.util.logging.*;

public class Bootstrap
{
    private Logger logger = Logger.getLogger("messager.startup.Bootstrap");

    public Bootstrap() {
        logger.setLevel(Level.FINE);
        try {
            File logDir = new File("./Bootlog");
            logDir.mkdirs();
            FileHandler handler = new FileHandler(logDir.getAbsolutePath() + "/Bootstrap.log");
            handler.setFormatter(new SimpleFormatter());
            logger.addHandler(handler);
            logger.setUseParentHandlers(false);
        }
        catch (IOException ex) {
            ex.printStackTrace();
            // FileHandler 생성 실패
        }
    }

    public void doCenterStart()
        throws IOException {
        logger.fine("Bootstrap instance: doCenterStart() : 진입");

        try {
            NeoDaemonLoader cd
                = new NeoDaemonLoader("messager.center.CenterDaemon", Integer.parseInt(System.getProperty("center.shutdown", "2101")));
            cd.start();
        }
        catch (Exception ex) {
            logger.log(Level.SEVERE, "Bootstrap instance: doCenterStart()", ex);
        }

        logger.fine("Bootstrap instance: doCenterStart() : 종료");
    }

    public void doCenterStop()
        throws IOException {
        logger.fine("Bootstrap instance: doCenterStop() : 진입");

        Socket socket = null;
        PrintWriter pw = null;
        try {
            socket = new Socket("localhost", Integer.parseInt(System.getProperty("center.shutdown", "2101")));
            pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            pw.println("messager.center.CenterDaemon");
            pw.flush();
        }
        catch (IOException ex) {
            logger.log(Level.SEVERE, "Bootstrap instance: doCenterStop()", ex);
        }
        finally {
            if (pw != null) {
                pw.close();
            }
            if (socket != null) {
                socket.close();
            }
        }

        logger.fine("Bootstrap instance: doCenterStop() : 종료");
    }

    public void doGeneratorStart()
        throws IOException {
        logger.fine("Bootstrap instance: doGeneratorStart() : 진입");

        try {
            NeoDaemonLoader gd = new NeoDaemonLoader(
                "messager.generator.GeneratorDaemon", Integer.parseInt(System.getProperty("generator.shutdown", "2102")));
            gd.start();
        }
        catch (Exception ex) {
            logger.log(Level.SEVERE, "Bootstrap instance: doGeneratorStart()", ex);
        }

        logger.fine("Bootstrap instance: doGeneratorStart() : 종료");
    }

    public void doGeneratorStop()
        throws IOException {
        logger.fine("Bootstrap instance: doGeneratorStop() : 진입");

        Socket socket = null;
        PrintWriter pw = null;
        try {
            socket = new Socket("localhost", Integer.parseInt(System.getProperty("generator.shutdown", "2102")));
            pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            pw.println("messager.generator.GeneratorDaemon");
            pw.flush();
        }
        catch (IOException ex) {
            logger.log(Level.SEVERE, "Bootstrap instance: doGeneratorStop()", ex);
        }
        finally {
            if (pw != null) {
                pw.close();
            }
            if (socket != null) {
                socket.close();
            }
        }

        logger.fine("Bootstrap instance: doGeneratorStop() : 종료");
    }

    public void doMailSenderStart()
        throws IOException {
        logger.fine("Bootstrap instance: doMailSenderStart() : 진입");

        try {
            NeoDaemonLoader md = new NeoDaemonLoader(
                "messager.mailsender.MailSenderDaemon", Integer.parseInt(System.getProperty("mailsender.shutdown", "2103")));
            md.start();
        }
        catch (Exception ex) {
            logger.log(Level.SEVERE, "Bootstrap instance: doMailSenderStart()", ex);
        }

        logger.fine("Bootstrap instance: doMailSenderStart() : 종료");
    }

    public void doMailSenderStop()
        throws IOException {
        logger.fine("Bootstrap instance: doMailSenderStop() : 진입");

        Socket socket = null;
        PrintWriter pw = null;
        try {
            socket = new Socket("localhost", Integer.parseInt(System.getProperty("mailsender.shutdown", "2103")));
            pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            pw.println("messager.mailsender.MailSenderDaemon");
            pw.flush();
        }
        catch (IOException ex) {
            logger.log(Level.SEVERE, "Bootstrap instance: doMailSenderStop()", ex);
        }
        finally {
            if (pw != null) {
                pw.close();
            }
            if (socket != null) {
                socket.close();
            }
        }

        logger.fine("Bootstrap instance: doMailSenderStop() : 종료");
    }

    public void doSendLogStart()
        throws IOException {
        logger.fine("Bootstrap instance: doMailSenderStart() : 진입");

        try {
            NeoDaemonLoader sd = new NeoDaemonLoader(
                "messager.mailsender.sendlog.SendLogDaemon", Integer.parseInt(System.getProperty("sendlog.shutdown", "2104")));
            sd.start();
        }
        catch (Exception ex) {
            logger.log(Level.SEVERE, "Bootstrap instance: doSendLogStart()", ex);
        }

        logger.fine("Bootstrap instance: doSendLogStart() : 종료");
    }

    public void doSendLogStop()
        throws IOException {
        logger.fine("Bootstrap instance: doSendLogStop() : 진입");

        Socket socket = null;
        PrintWriter pw = null;
        try {
            socket = new Socket("localhost", Integer.parseInt(System.getProperty("sendlog.shutdown", "2104")));
            pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            pw.println("messager.mailsender.sendlog.SendLogDaemon");
            pw.flush();
        }
        catch (IOException ex) {
            logger.log(Level.SEVERE, "Bootstrap instance: doSendLogStop()", ex);
        }
        finally {
            if (pw != null) {
                pw.close();
            }
            if (socket != null) {
                socket.close();
            }
        }

        logger.fine("Bootstrap instance: doSendLogStop() : 종료");
    }

    /**
     * Daemon startup of Neocast@Messager v3.0 for The service of windows system
     * @param args String[]
     * @throws Throwable
     */
    public static void main(String[] args)throws Throwable {
    	

    	args = new String[1];
    	args[0]="all_stop";	
    	

        try {
            if (args.length > 0) {
                if (args[0].equals("center_start")) {
                    Bootstrap boot = new Bootstrap();
                    boot.doCenterStart();
                }
                else if (args[0].equals("center_stop")) {
                    Bootstrap boot = new Bootstrap();
                    boot.doCenterStop();
                }
                else if (args[0].equals("generator_start")) {
                    Bootstrap boot = new Bootstrap();
                    boot.doGeneratorStart();
                }
                else if (args[0].equals("generator_stop")) {
                    Bootstrap boot = new Bootstrap();
                    boot.doGeneratorStop();
                }
                else if (args[0].equals("mailsender_start")) {
                    Bootstrap boot = new Bootstrap();
                    boot.doMailSenderStart();
                }
                else if (args[0].equals("mailsender_stop")) {
                    Bootstrap boot = new Bootstrap();
                    boot.doMailSenderStop();
                }
                else if (args[0].equals("sendlog_start")) {
                    Bootstrap boot = new Bootstrap();
                    boot.doSendLogStart();
                }
                else if (args[0].equals("sendlog_stop")) {
                    Bootstrap boot = new Bootstrap();
                    boot.doSendLogStop();
                }else if (args[0].equals("all_stop")) {
                	Bootstrap boot = new Bootstrap();
                	boot.doMailSenderStop();
                	boot.doSendLogStop();
                	boot.doGeneratorStop();
                	boot.doCenterStop();
                }else if (args[0].equals("all_start")) {
                	Bootstrap boot = new Bootstrap();
                	boot.doMailSenderStart();
                	boot.doSendLogStart();
                	boot.doGeneratorStart();
                	boot.doCenterStart();
                }
                else {
                    System.out.println("Invalid Paramter: " + args[0]);
                    System.exit(1);
                }
            }
            else {
                System.out.println("Parameter not found!!");
                System.exit(2);
            }
        }
        catch (Throwable e) {
            e.printStackTrace(System.out);
        }
    }
}
