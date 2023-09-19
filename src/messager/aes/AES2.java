package messager.aes;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;


public class AES2 {

    public static void main(String[] args)
    {
        try {
        	
        	String Key = "ums" ;
        	
        	System.out.println("암호화할 값 : " + Key);
        	System.out.println("");
        	/** 	
        	* 암호화 시작
        	*/
        	String AESGAP = AES.encrypt(Key);
        	System.out.println("암호화 완료: " + AESGAP );
        	System.out.println("");
        	
        	
        	/** 	
        	* 복호화 시작
        	*/ 
//        	String DESGAP = AES.decrypt("ebe4325c14625e59bf46fe901850a0eaa35c1109337896bbcf512e2c6cffd6f8041bdefe14a0b9cdeff09be240363069");
        	String DESGAP = AES.decrypt(AESGAP);
        	System.out.println("복호화 완료: " + DESGAP );
                        
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
  }
