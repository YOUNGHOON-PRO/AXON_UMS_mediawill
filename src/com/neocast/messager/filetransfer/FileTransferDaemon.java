package com.neocast.messager.filetransfer;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FileTransferDaemon
  implements Daemon
{
	private static final Logger LOGGER = LogManager.getLogger(FileTransferDaemon.class.getName());
	
  private Main main;

  public void init(DaemonContext paramDaemonContext)
    throws Exception
  {
    println("FileTransferDaemon instance: init()");
  }

  public void start() {
    println("FileTransferDaemon instance: start(): in");

    this.main = new Main();
    Main.main(new String[1]);

    println("FileTransferDaemon instance: start(): out");
  }

  public void stop() throws Exception {
    println("FileTransferDaemon instance: stop(): in");

    Main.shutdown();

    println("FileTransferDaemon instance: stop(): out");
  }

  public void destroy() {
    println("FileTransferDaemon instance: destroy(): in");

    println("FileTransferDaemon instance: destroy(): out");
  }

  private String getCurrentTime() {
    SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US);

    return localSimpleDateFormat.format(new Date());
  }

  private void println(String paramString) {
    //System.out.println(getCurrentTime() + " : " + paramString);
	  LOGGER.info(getCurrentTime() + " : " + paramString);
  }
}