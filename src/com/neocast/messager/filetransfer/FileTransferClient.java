package com.neocast.messager.filetransfer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * 요청자의 파일을 전송하기 위한 파일트랜스퍼 클라이언트
 * 첫번째로 보내는 데이터는 사이즈를 전송하고  두번째로 파일을 전송하다
 * @author younghoon
 */

class FileTransferClient extends Thread
{
  private static final Logger LOGGER = LogManager.getLogger(FileTransferClient.class.getName());
	
  private static String rootPath;
  private Socket socket;
  private InputStream in;
  private OutputStream out;
  private static final int bufferSize = 1024;

  /**
   * 요청자의 파일을 전달하는 생성자 메소드
   * @param paramSocket 연결된 소켓스트림
   */
  public FileTransferClient(Socket paramSocket)
  {
    this.socket = paramSocket;
  }

  public void run()
  {
    try {
      this.in = this.socket.getInputStream();
      this.out = this.socket.getOutputStream();
      while (true)
      {
    	  String str =null;
        try {
        	str = readRequest();
		} catch (Exception e) {
			 LOGGER.error(e);
		}
        
        if (str == null) {
          break;
        }
        //System.out.println("request: " + str);
        LOGGER.info("request: " + str);
        response(str);
      }
    } catch (Exception localException) {
      //localException.printStackTrace();
      LOGGER.error(localException);
    } finally {
      closeSocket();
    }
  }

  private void closeSocket() {
    if (this.in != null) {
      try {
        this.in.close(); } catch (IOException localIOException1) {
        	LOGGER.error(localIOException1);
      }
      this.in = null;
    }

    if (this.out != null) {
      try {
        this.out.close(); } catch (IOException localIOException2) {
        	LOGGER.error(localIOException2);
      }
      this.out = null;
    }

    if (this.socket == null) return;
    try {
      this.socket.close(); } catch (IOException localIOException3) {
    	  LOGGER.error(localIOException3);
    }
    this.socket = null;
  }

  private int readbytes(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws IOException
  {
    int i = 0;

    if (paramInt1 + paramInt2 > paramArrayOfByte.length) {
      throw new IndexOutOfBoundsException((paramInt1 + paramInt2) + ":" + paramArrayOfByte.length);
    }

    while (i < paramInt2) {
    	// TODO Transfer - inputstream 을 읽는데 이해가 잘 안되네....
      int j = this.in.read(paramArrayOfByte, paramInt1, paramInt2);
      //System.out.println("read count: " + j);
      LOGGER.info("read count: " + j);
      
      if (j == -1) {
        return -1;
      }
      i += j;
      paramInt1 += j;
      paramInt2 -= j;
    }
    return i;
  }

  private int readsize() throws IOException {
    byte[] arrayOfByte = new byte[4];

    int i = readbytes(arrayOfByte, 0, 4);
    if (i != 4) {
      throw new IOException();
    }

    for (int j = 0; j < 4; ++j) {
     // System.out.println("byte: " + arrayOfByte[j]);
    	LOGGER.info("byte: " + arrayOfByte[j]);
    }
    
    // TODO Transfer - << shift 연산자를 이용하는데 이해가 되지 않는다...
    return (arrayOfByte[0] << 24 | (arrayOfByte[1] & 0xFF) << 16 | (arrayOfByte[2] & 0xFF) << 8 | (arrayOfByte[3] & 0xFF) << 0);
  }

  /**
   * 파일의 사이즈를 전달
   * @param paramInt 파일사이즈
   * @throws IOException
   */
  private void sendsize(int paramInt)
    throws IOException
  {
    byte[] arrayOfByte = new byte[4];

    arrayOfByte[0] = (byte)(paramInt >>> 24);
    arrayOfByte[1] = (byte)(paramInt >>> 16);
    arrayOfByte[2] = (byte)(paramInt >>> 8);
    arrayOfByte[3] = (byte)(paramInt >>> 0);

    this.out.write(arrayOfByte, 0, 4);
    this.out.flush();
  }

  
  /**
   * 요청자의 파일 경로를 담는다
   * @return
   * @throws IOException
   */
  private String readRequest() throws IOException {
    int i = readsize();

    if (i > 0) {
      byte[] arrayOfByte = new byte[i];
      int j = readbytes(arrayOfByte, 0, i);
      if (j == -1) {
        return null;
      }

      if (j != i) {
        throw new IOException();
      }
      return new String(arrayOfByte,"UTF-8");
    }
    return null;
  }

  
  /**
   * 요청자에게 파일 정보를 전달한다.
   * @param paramString 파일경로
   * @throws IOException
   */
  private void response(String paramString) throws IOException {
    int i = 0;
    File localFile = null;

    if (rootPath != null)
      localFile = new File(rootPath, paramString);
    else {
      localFile = new File(paramString);
    }
    //System.out.println("requestFile: " + localFile);
    if (localFile.exists()) {
      i = (int)localFile.length();
      //System.out.println("requestFile size: " + i);
    }
    
    //요청자에게 파일의 사이즈를 전송
    sendsize(i);
    
    if (i > 0) {
      //요청자에게 파일을 전송	
      sendFile(localFile);
    }

    this.out.flush();
  }

  /**
   * 실제 파일을 전송
   * @param paramFile 실제파일
   * @throws IOException
   */
  private void sendFile(File paramFile)
    throws IOException
  {
    BufferedInputStream localBufferedInputStream = null;
    Object localObject1 = null;
    try
    {
      localBufferedInputStream = new BufferedInputStream(new FileInputStream(paramFile));

      byte[] arrayOfByte = new byte[1024];
      int i;
      while ((i = localBufferedInputStream.read(arrayOfByte, 0, 1024)) != -1)
      {
       
        this.out.write(arrayOfByte, 0, i);
        this.out.flush();
      }
    } catch (Exception localException) {
    	LOGGER.error(localException);
      localObject1 = localException;
    } finally {
      if (localBufferedInputStream != null)
        try {
          localBufferedInputStream.close();
        }
        catch (IOException localIOException) {
        	LOGGER.error(localIOException);
        }
    }
    if (localObject1 != null) {
      if (localObject1 instanceof IOException) {
        throw ((IOException)localObject1);
      }
      throw ((RuntimeException)localObject1);
    }
  }

  static
  {
    String str1 = System.getProperty("root.dir");
	  //String str1 = "E:\\mpv3\\AXON_UMS\\front\\upload";
    if (str1 != null) {
      String str2 = str1.trim();
      if (str2.length() > 0)
        rootPath = str2;
    }
  }
}
