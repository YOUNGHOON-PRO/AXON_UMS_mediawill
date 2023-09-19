import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.yettiesoft.javarose.SGException;
import com.yettiesoft.javarose.crypto.cipher.SGCipher;
import com.yettiesoft.javarose.crypto.digests.SGDigest;
import com.yettiesoft.javarose.util.encoders.SGBase64;
import com.yettiesoft.sg.facade.csp.CipherParam;
import com.yettiesoft.sg.facade.csp.HashParam;
import com.yettiesoft.sg.facade.csp.SGCryptoException;
import com.yettiesoft.sg.facade.csp.SGCryptoServiceProvider;
import com.yettiesoft.vestmail.VMCipherConfig;
import com.yettiesoft.vestmail.VMCipherImpl;
import com.yettiesoft.vestmail.VMCipherLib;
import com.yettiesoft.vestmail.VMCipherResource;
import com.yettiesoft.vestmail.VMConstants;
import com.yettiesoft.vestmail.crypto.VMCipher;
import com.yettiesoft.vestmail.crypto.VMCipherNative;
import com.yettiesoft.vestmail.crypto.VMCryptoFactory;


public class decryptMail {
	
	
	public static void main(String[] args) throws SGException {
		
		VMCipherImpl aCipherInterface = null;
		
		try {
			aCipherInterface = new VMCipherImpl();
			
			try{
				/*
				String keyParam = "830212";		
				String plainMail;				
				String decryptMail;				
				plainMail = readFile ("./template/sam_err.html", "UTF-8");
				decryptMail = aCipherInterface.decryptMailContent(keyParam, plainMail);
				saveFile("./template/decFile.html", decryptMail.getBytes());
				*/
				
				// 패스워드 감지 테스트
				
				String plainMail;				
				String decryptMail;	
				//plainMail = readFile ("./template/BC.html", "utf-8");
				//plainMail = readFile ("./template/dg.html", "utf-8");
				//plainMail = readFile ("./template/bc780415.html", "utf-8");
				plainMail = readFile ("./template/skt2.html", "utf-8");
				
				try{
					decryptMail = aCipherInterface.decryptMailContent("111111", plainMail);
					saveFile("./template/dec/decFile_skt2.html", decryptMail.getBytes());	
				} catch(SGException e) {
					e.printStackTrace();
				}
				
				
				/*
				for(int i = 10; i < 100; i ++) {
					String si = Integer.toString(i);
					for(int j = 1; j <13 ; j++) {
						String sj = Integer.toString(j);
						if( j < 10) 
							sj = "0" + sj;
						for(int k = 1; k < 32; k++){
							String sk = Integer.toString(k);
							if( k < 10) 
								sk = "0" + sk;
							
							try{
								System.out.println("decript value : " + si + sj + sk);
								decryptMail = aCipherInterface.decryptMailContent(si + sj + sk, plainMail);
								System.out.println("decript value : " + si + sj + sk);
								saveFile("./template/dec/decFile" + si + sj + sk  +".html", decryptMail.getBytes());
								break;
								}catch(SGException e) {
								}
						}
					}
				}
				*/
				
				
				/*
				for(int i = 10; i < 50; i ++) {
					String si = Integer.toString(i);
					if( i < 10) 
						si = "0" + si;
					for(int j = 0; j <100 ; j++) {
						String sj = Integer.toString(j);
						if( j < 10) 
							sj = "0" + sj;
						for(int k = 0; k < 100; k++){
							String sk = Integer.toString(k);
							if( k < 10) 
								sk = "0" + sk;
							for(int l = 0; l < 100; l++){
								String sl = Integer.toString(l);
								if( l < 10) 
									sl = "0" + sl;
								for(int m = 0; m < 100; m++){
									String sm = Integer.toString(m);
									if( m < 10) 
										sm = "0" + sm;
							
									try{
										System.out.println("decript value : " + si + sj + sk + sl + sm);
										decryptMail = aCipherInterface.decryptMailContent(si + sj + sk + sl + sm, plainMail);
										System.out.println("decript value : " + si + sj + sk + sl + sm);
										saveFile("./template/dec/decFile" + si + sj + sk + sl + sm +".html", decryptMail.getBytes());
										break;
									}catch(SGException e) {}
								}
							}
						}
					}
				}
				*/
				/*
				for(int i = 1; i < 3; i ++) {
					String si = Integer.toString(i);
					for(int j = 0; j <100 ; j++) {
						String sj = Integer.toString(j);
						if( j < 10) 
							sj = "0" + sj;
						for(int k = 0; k < 100; k++){
							String sk = Integer.toString(k);
							if( k < 10) 
								sk = "0" + sk;
							for(int l = 0; l < 100; l++){
								String sl = Integer.toString(l);
								if( l < 10) 
									sl = "0" + sl;
							
								try{
									System.out.println("decript value : " + si + sj + sk + sl);
									decryptMail = aCipherInterface.decryptMailContent(si + sj + sk + sl, plainMail);
									System.out.println("decript value : " + si + sj + sk + sl);
									saveFile("./template/dec/decFile" + si + sj + sk + sl+".html", decryptMail.getBytes());
									break;
								}catch(SGException e) {}
							}
						}
					}
				}
				*/
				
				
			}catch (IOException e) {
				e.printStackTrace();
			}
		} catch (SGException e) {
			e.printStackTrace();
		}
	}
	
	public static String readFile(String fname, String charset) throws IOException {
		StringBuffer sb = new StringBuffer();
		BufferedReader br = null;
		br = new BufferedReader(new InputStreamReader(new FileInputStream(fname), charset));
		String sread = null;
		while((sread = br.readLine())!=null) {					
			sb.append(sread).append("\n");
		}
		br.close();
		return sb.toString();
	}
	
	public static void saveFile(String fname, byte[] content) throws IOException {
    	File aEncFile = new File(fname);
		FileOutputStream os = new FileOutputStream(aEncFile);
		os.write(content);
		os.close();	
	}
}