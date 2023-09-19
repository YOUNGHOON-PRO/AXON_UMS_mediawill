/*    */ package messager.sms;
/*    */ 
/*    */ import java.io.PrintStream;
/*    */ import java.text.SimpleDateFormat;
/*    */ import java.util.Date;
/*    */ import java.util.Locale;
/*    */ import org.apache.commons.daemon.Daemon;
/*    */ import org.apache.commons.daemon.DaemonContext;
/*    */ 
/*    */ public class SmsDaemon
/*    */   implements Daemon
/*    */ {
/*    */   private Main main;
/*    */ 
/*    */   public void init(DaemonContext context)
/*    */     throws Exception
/*    */   {
/* 16 */     println("smsDaemon instance: init()");
/*    */   }
/*    */ 
/*    */   public void start() {
/* 20 */     println("smsDaemon instance: start(): in");
/*    */ 
/* 22 */     this.main = new Main();
/* 23 */     Main.main(new String[1]);
/*    */ 
/* 25 */     println("smsDaemon instance: start(): out");
/*    */   }
/*    */ 
/*    */   public void stop() throws Exception
/*    */   {
/* 30 */     println("smsDaemon instance: stop(): in");
/*    */ 
/* 32 */     Main.shutdown();
/*    */ 
/* 34 */     println("smsDaemon instance: stop(): out");
/*    */   }
/*    */ 
/*    */   public void destroy() {
/* 38 */     println("smsDaemon instance: destroy(): in");
/*    */ 
/* 40 */     println("smsDaemon instance: destroy(): out");
/*    */   }
/*    */ 
/*    */   private String getCurrentTime() {
/* 44 */     SimpleDateFormat fmt = new SimpleDateFormat(
/* 45 */       "yyyy/MM/dd HH:mm:ss", Locale.US);
/* 46 */     return fmt.format(new Date());
/*    */   }
/*    */ 
/*    */   private void println(String msg) {
/* 50 */     System.out.println(getCurrentTime() + " : " + msg);
/*    */   }
/*    */ }