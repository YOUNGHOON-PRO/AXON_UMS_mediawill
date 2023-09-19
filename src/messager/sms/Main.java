/*    */ package messager.sms;
/*    */ 
/*    */ import java.io.PrintStream;
/*    */ import messager.center.config.ConfigLoader;
/*    */ 
/*    */ public class Main
/*    */ {
/*    */   public static void main(String[] args)
/*    */   {
/*    */     try
/*    */     {
/* 19 */       ConfigLoader.load();
/* 20 */       new smsMigrateAgent().start();
/*    */     }
/*    */     catch (Exception ex) {
/* 23 */       ex.printStackTrace();
/* 24 */       System.exit(1);
/*    */     }
/*    */   }
/*    */ 
/*    */   public static void shutdown() {
/* 29 */     smsScheduler main = new smsScheduler();
/*    */ 
/* 31 */     smsScheduler.shutdown();
/*    */ 
/* 33 */     System.out.println("sms shutdown.");
/* 34 */     System.exit(0);
/*    */   }
/*    */ }