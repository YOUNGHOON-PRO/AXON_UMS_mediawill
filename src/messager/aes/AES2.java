package messager.aes;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;


public class AES2 {

    public static void main(String[] args)
    {
        try {
        	
        	String Key = "ums" ;
        	
        	System.out.println("��ȣȭ�� �� : " + Key);
        	System.out.println("");
        	/** 	
        	* ��ȣȭ ����
        	*/
        	String AESGAP = AES.encrypt(Key);
        	System.out.println("��ȣȭ �Ϸ�: " + AESGAP );
        	System.out.println("");
        	
        	
        	/** 	
        	* ��ȣȭ ����
        	*/ 
//        	String DESGAP = AES.decrypt("ebe4325c14625e59bf46fe901850a0eaa35c1109337896bbcf512e2c6cffd6f8041bdefe14a0b9cdeff09be240363069");
        	String DESGAP = AES.decrypt(AESGAP);
        	System.out.println("��ȣȭ �Ϸ�: " + DESGAP );
                        
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
  }
