package com.neocast.messager.filetransfer;

import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 파일트랜스퍼 서버
 * 요청자가 접속할 수 있도록 서버소켓을 생성
 * @author younghoon
 *
 */
class FileTransfer extends Thread
{
  private static int port;

  public void run()
  {
    ServerSocket localServerSocket = null;
    try
    {
     //port=10002;
      localServerSocket = new ServerSocket(port);
      
      while(true) { 

	      Socket localSocket = localServerSocket.accept();
	      FileTransferClient localFileTransferClient = new FileTransferClient(localSocket);
	      localFileTransferClient.start();
      }
    }
    catch (Exception localException) {
    	localException.printStackTrace();
      
    }
  }

  static
  {
    try
    {
      //String str = System.getProperty("listen.port");
    String str ="10002";
      port = Integer.parseInt(str);
    } catch (Exception localException) {
      System.err.println("not found : listen.port");
      localException.printStackTrace();
      System.exit(1);
    }
  }
}