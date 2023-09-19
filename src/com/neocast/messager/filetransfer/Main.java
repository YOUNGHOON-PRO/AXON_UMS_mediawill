package com.neocast.messager.filetransfer;

import java.io.PrintStream;

public class Main
{
  public static void main(String[] paramArrayOfString)
  {
    FileTransfer localFileTransfer = new FileTransfer();
    localFileTransfer.start();
  }

  public static void shutdown() {
    System.out.println("FileTransfer shutdown.");
    System.exit(0);
  }
}