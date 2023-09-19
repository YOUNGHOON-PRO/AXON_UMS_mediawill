package com.neocast.messager.startup;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.logging.Logger;
import org.apache.commons.daemon.support.DaemonLoader;

public class NeoDaemonLoader
{
  private DaemonLoader loader;
  private String daemonClass;
  private Logger logger;
  private ShutdownListener downListener;

  public NeoDaemonLoader(String paramString, int paramInt)
    throws IOException
  {
    this.daemonClass = paramString;
    this.downListener = new ShutdownListener(paramString, paramInt);
    this.logger = Logger.getLogger("com.neocast.messager.startup.Bootstrap");
  }

  public void start()
  {
    Thread localThread = new Thread(this.downListener);
    localThread.start();

    boolean bool = DaemonLoader.load(this.daemonClass, null);
    if (bool) {
      bool = DaemonLoader.start();
    }
    if (!(bool))
      throw new RuntimeException("NeoDaemonLoader failed to Start : " + this.daemonClass);
  }

  public void stop()
  {
    this.logger.fine("stop start : " + this.loader);
    boolean bool = DaemonLoader.stop();
    this.logger.fine("successStop:" + bool);
    if (bool) {
      bool = DaemonLoader.destroy();
    }

    if (!(bool))
      throw new RuntimeException("NeoDaemonLoader failed to stop : " + this.daemonClass);
  }

  private class ShutdownListener implements Runnable
  {
    private ServerSocket shutdownSocket;
    private boolean stopping;
    private String stopMsg;

    public ShutdownListener(String paramString, int paramInt) throws IOException
    {
      this.shutdownSocket = new ServerSocket(paramInt);
      this.stopMsg = paramString;
    }

    // ERROR //
    public void run()
    {
      // Byte code:
      //   0: aload_0
      //   1: getfield 2	com/neocast/messager/startup/NeoDaemonLoader$ShutdownListener:this$0	Lcom/neocast/messager/startup/NeoDaemonLoader;
      //   4: invokestatic 7	com/neocast/messager/startup/NeoDaemonLoader:access$000	(Lcom/neocast/messager/startup/NeoDaemonLoader;)Ljava/util/logging/Logger;
      //   7: ldc 8
      //   9: invokevirtual 9	java/util/logging/Logger:fine	(Ljava/lang/String;)V
      //   12: aconst_null
      //   13: astore_1
      //   14: aconst_null
      //   15: astore_2
      //   16: aload_0
      //   17: getfield 10	com/neocast/messager/startup/NeoDaemonLoader$ShutdownListener:stopping	Z
      //   20: ifne +116 -> 136
      //   23: aload_0
      //   24: getfield 5	com/neocast/messager/startup/NeoDaemonLoader$ShutdownListener:shutdownSocket	Ljava/net/ServerSocket;
      //   27: invokevirtual 11	java/net/ServerSocket:accept	()Ljava/net/Socket;
      //   30: astore_1
      //   31: new 12	java/io/BufferedReader
      //   34: dup
      //   35: new 13	java/io/InputStreamReader
      //   38: dup
      //   39: aload_1
      //   40: invokevirtual 14	java/net/Socket:getInputStream	()Ljava/io/InputStream;
      //   43: invokespecial 15	java/io/InputStreamReader:<init>	(Ljava/io/InputStream;)V
      //   46: invokespecial 16	java/io/BufferedReader:<init>	(Ljava/io/Reader;)V
      //   49: astore_2
      //   50: aload_2
      //   51: invokevirtual 17	java/io/BufferedReader:readLine	()Ljava/lang/String;
      //   54: aload_0
      //   55: getfield 6	com/neocast/messager/startup/NeoDaemonLoader$ShutdownListener:stopMsg	Ljava/lang/String;
      //   58: invokevirtual 18	java/lang/String:equals	(Ljava/lang/Object;)Z
      //   61: ifeq +8 -> 69
      //   64: aload_0
      //   65: iconst_1
      //   66: putfield 10	com/neocast/messager/startup/NeoDaemonLoader$ShutdownListener:stopping	Z
      //   69: jsr +37 -> 106
      //   72: goto -56 -> 16
      //   75: astore_3
      //   76: aload_0
      //   77: getfield 2	com/neocast/messager/startup/NeoDaemonLoader$ShutdownListener:this$0	Lcom/neocast/messager/startup/NeoDaemonLoader;
      //   80: invokestatic 7	com/neocast/messager/startup/NeoDaemonLoader:access$000	(Lcom/neocast/messager/startup/NeoDaemonLoader;)Ljava/util/logging/Logger;
      //   83: getstatic 20	java/util/logging/Level:SEVERE	Ljava/util/logging/Level;
      //   86: ldc 21
      //   88: aload_3
      //   89: invokevirtual 22	java/util/logging/Logger:log	(Ljava/util/logging/Level;Ljava/lang/String;Ljava/lang/Throwable;)V
      //   92: jsr +14 -> 106
      //   95: goto -79 -> 16
      //   98: astore 4
      //   100: jsr +6 -> 106
      //   103: aload 4
      //   105: athrow
      //   106: astore 5
      //   108: aload_2
      //   109: ifnull +12 -> 121
      //   112: aload_2
      //   113: invokevirtual 23	java/io/BufferedReader:close	()V
      //   116: goto +5 -> 121
      //   119: astore 6
      //   121: aload_1
      //   122: ifnull +12 -> 134
      //   125: aload_1
      //   126: invokevirtual 24	java/net/Socket:close	()V
      //   129: goto +5 -> 134
      //   132: astore 6
      //   134: ret 5
      //   136: jsr +37 -> 173
      //   139: goto +57 -> 196
      //   142: astore_3
      //   143: aload_0
      //   144: getfield 2	com/neocast/messager/startup/NeoDaemonLoader$ShutdownListener:this$0	Lcom/neocast/messager/startup/NeoDaemonLoader;
      //   147: invokestatic 7	com/neocast/messager/startup/NeoDaemonLoader:access$000	(Lcom/neocast/messager/startup/NeoDaemonLoader;)Ljava/util/logging/Logger;
      //   150: getstatic 20	java/util/logging/Level:SEVERE	Ljava/util/logging/Level;
      //   153: ldc 21
      //   155: aload_3
      //   156: invokevirtual 22	java/util/logging/Logger:log	(Ljava/util/logging/Level;Ljava/lang/String;Ljava/lang/Throwable;)V
      //   159: jsr +14 -> 173
      //   162: goto +34 -> 196
      //   165: astore 7
      //   167: jsr +6 -> 173
      //   170: aload 7
      //   172: athrow
      //   173: astore 8
      //   175: aload_0
      //   176: getfield 5	com/neocast/messager/startup/NeoDaemonLoader$ShutdownListener:shutdownSocket	Ljava/net/ServerSocket;
      //   179: ifnull +15 -> 194
      //   182: aload_0
      //   183: getfield 5	com/neocast/messager/startup/NeoDaemonLoader$ShutdownListener:shutdownSocket	Ljava/net/ServerSocket;
      //   186: invokevirtual 25	java/net/ServerSocket:close	()V
      //   189: goto +5 -> 194
      //   192: astore 9
      //   194: ret 8
      //   196: aload_0
      //   197: getfield 2	com/neocast/messager/startup/NeoDaemonLoader$ShutdownListener:this$0	Lcom/neocast/messager/startup/NeoDaemonLoader;
      //   200: invokevirtual 26	com/neocast/messager/startup/NeoDaemonLoader:stop	()V
      //   203: aload_0
      //   204: getfield 2	com/neocast/messager/startup/NeoDaemonLoader$ShutdownListener:this$0	Lcom/neocast/messager/startup/NeoDaemonLoader;
      //   207: invokestatic 7	com/neocast/messager/startup/NeoDaemonLoader:access$000	(Lcom/neocast/messager/startup/NeoDaemonLoader;)Ljava/util/logging/Logger;
      //   210: ldc 27
      //   212: invokevirtual 9	java/util/logging/Logger:fine	(Ljava/lang/String;)V
      //   215: return
      //
      // Exception table:
      //   from	to	target	type
      //   31	69	75	IOException
      //   31	72	98	finally
      //   75	95	98	finally
      //   98	103	98	finally
      //   112	116	119	IOException
      //   125	129	132	IOException
      //   16	136	142	IOException
      //   16	139	165	finally
      //   142	162	165	finally
      //   165	170	165	finally
      //   182	189	192	IOException
    }
  }
}