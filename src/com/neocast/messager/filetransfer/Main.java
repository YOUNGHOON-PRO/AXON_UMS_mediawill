package com.neocast.messager.filetransfer;

import java.io.PrintStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main
{
	private static final Logger LOGGER = LogManager.getLogger(Main.class.getName());
	
  public static void main(String[] paramArrayOfString)
  {
    FileTransfer localFileTransfer = new FileTransfer();
    localFileTransfer.start();
  }

  public static void shutdown() {
	  LOGGER.info("FileTransfer shutdown.");
	  //System.out.println("FileTransfer shutdown.");
    System.exit(0);
    
  }
}