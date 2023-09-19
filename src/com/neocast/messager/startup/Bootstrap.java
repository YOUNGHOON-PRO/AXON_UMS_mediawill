package com.neocast.messager.startup;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Bootstrap
{
  private Logger logger = Logger.getLogger("com.neocast.messager.startup.Bootstrap");

  public Bootstrap() {
    this.logger.setLevel(Level.FINE);
    try {
      File localFile = new File("./Bootlog");
      localFile.mkdirs();
      FileHandler localFileHandler = new FileHandler(localFile.getAbsolutePath() + "/Bootstrap.log");
      localFileHandler.setFormatter(new SimpleFormatter());
      this.logger.addHandler(localFileHandler);
      this.logger.setUseParentHandlers(false);
    }
    catch (IOException localIOException) {
      localIOException.printStackTrace();
    }
  }

  public void doFileTransferStart() throws IOException
  {
    this.logger.fine("Bootstrap instance: doFileTransferStart() : 진입");
    try
    {
      NeoDaemonLoader localNeoDaemonLoader = new NeoDaemonLoader("com.neocast.messager.filetransfer.FileTransferDaemon", Integer.parseInt(System.getProperty("FileTransfer.shutdown", "2105")));

      localNeoDaemonLoader.start();
    }
    catch (Exception localException) {
      this.logger.log(Level.SEVERE, "Bootstrap instance: doFileTransferStart()", localException);
    }

    this.logger.fine("Bootstrap instance: doFileTransferStart() : 종료");
  }

  public void doFileTransferStop() throws IOException {
    this.logger.fine("Bootstrap instance: doFileTransferStop() : 진입");

    Socket localSocket = null;
    PrintWriter localPrintWriter = null;
    try {
      localSocket = new Socket("localhost", Integer.parseInt(System.getProperty("FileTransfer.shutdown", "2105")));
      localPrintWriter = new PrintWriter(new OutputStreamWriter(localSocket.getOutputStream()));
      localPrintWriter.println("com.neocast.messager.filetransfer.FileTransferDaemon");
      localPrintWriter.flush();
    }
    catch (IOException localIOException) {
      this.logger.log(Level.SEVERE, "Bootstrap instance: doFileTransferStop()", localIOException);
    }
    finally {
      if (localPrintWriter != null) localPrintWriter.close();
      if (localSocket != null) localSocket.close();
    }

    this.logger.fine("Bootstrap instance: doFileTransferStop() : 종료");
  }

  public static void main(String[] paramArrayOfString)
    throws Throwable
  {
    try
    {
      if (paramArrayOfString.length > 0)
      {
        Bootstrap localBootstrap;
        if (paramArrayOfString[0].equals("FileTransfer_start")) {
          localBootstrap = new Bootstrap();
          localBootstrap.doFileTransferStart();
        } else if (paramArrayOfString[0].equals("FileTransfer_stop")) {
          localBootstrap = new Bootstrap();
          localBootstrap.doFileTransferStop();
        } else {
          System.out.println("Invalid Paramter: " + paramArrayOfString[0]);
          System.exit(1);
        }
      } else {
        System.out.println("Parameter not found!!");
        System.exit(2);
      }
    } catch (Throwable localThrowable) {
      localThrowable.printStackTrace(System.out);
    }
  }
}