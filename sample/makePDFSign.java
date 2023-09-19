import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import com.yettiesoft.vestmail.VMCipherImpl;
import com.yettiesoft.vestmail.VMUtil;
import com.yettiesoft.vestmail.pdf.CreateSignature;
import com.yettiesoft.vestmail.pdf.VMPdfImpl;
import com.yettiesoft.javarose.SGException;


public class makePDFSign {
	
	
	public static void main(String[] args) {
		
		// 인증서 유효성 검사를 하지 않을 경우 false
		CreateSignature.validCheck = false;
		
		String pfxFile = "./template/koscom-test.pfx";
		String pin = "12qwaszx23!";
		String pdfFile = "./template/policy.pdf";
		String outFile = "./sample/output/policy_signed.pdf";

		String name = "예티소프트"; 
		String reaeson = "전자서명 테스트 용";
		
		VMPdfImpl pdfSign = null;
		try{
			pdfSign = new VMPdfImpl();
			
			// sample 1
	//		KeyStore keystore = KeyStore.getInstance("PKCS12");
	//		keystore.load(new FileInputStream(pfxFile), pin.toCharArray());
	//		File inFile = new File(pdfFile);
	//      PDDocument doc = PDDocument.load(inFile);
	//		byte[] out = pdfSign.makePDFSignature(keystore, pin, doc, "", "");
			
			// sample 2
			byte[] out = pdfSign.makePDFSignature(pfxFile, pin, pdfFile, name, reaeson);
			
			// sample 3
//			byte[] bPfx = VMUtil.readBinaryFile(pfxFile);
//			byte[] bPdf = VMUtil.readBinaryFile(pdfFile);
//			byte[] out = pdfSign.makePDFSignature(bPfx, pin, bPdf);
	
			VMUtil.saveFile(outFile, out);
		} catch(SGException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}
