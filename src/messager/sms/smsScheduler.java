/*    */ package messager.sms;
/*    */ 
/*    */ import java.io.PrintStream;
/*    */ import messager.center.config.ConfigLoader;
/*    */ 
/*    */ public class smsScheduler
/*    */ {
/*    */   public static void main(String[] args)
/*    */   {
/* 20 */     ConfigLoader.load();
/* 21 */     new smsMigrateAgent().start(); }
/*    */ 
/*    */   public static void shutdown() {
/* 24 */     System.out.println("Sms shutdown.");
/* 25 */     System.exit(0);
/*    */   }
/*    */ }