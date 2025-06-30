package messager.dkim;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.simplejavamail.utils.mail.dkim.Canonicalization;
import org.simplejavamail.utils.mail.dkim.DkimMessage;
import org.simplejavamail.utils.mail.dkim.DkimSigner;
import org.simplejavamail.utils.mail.dkim.SigningAlgorithm;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;

public class SimpleJavaDKIMSign {
	private static final Logger LOGGER = LogManager.getLogger(SimpleJavaDKIMSign.class);
	
	/**
	 * @param originEml DKIM 서명하고자 하는 EML 경로
	 * @param outputEml DKIM 서명 헤더를 붙인 새로운 EML 경로
	 */
	public void makeDkimSignMessage(Path originEml, Path outputEml) throws IOException, MessagingException, InvalidKeySpecException, NoSuchAlgorithmException {
		try (InputStream inputStream = Files.newInputStream(originEml);
			 OutputStream outputStream = Files.newOutputStream(outputEml)) {
			MimeMessage originMessage = new MimeMessage(null, inputStream);
			DkimMessage dkimMessage = getDkimMessage(originMessage);
			dkimMessage.writeTo(outputStream);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//check exists DKIM-Signature header
		try (InputStream inputStream = Files.newInputStream(outputEml)) {
			MimeMessage outputMessage = new MimeMessage(null, inputStream);
			String[] header = outputMessage.getHeader("DKIM-Signature");
			assert header.length > 0;
		}
	}
	
	public String makeDkimSignMessage(String originStr) throws IOException, MessagingException, InvalidKeySpecException, NoSuchAlgorithmException {
		String result = null;
		
		try (InputStream inputStream = new ByteArrayInputStream(originStr.getBytes());
				OutputStream outputStream = new ByteArrayOutputStream()) {
			MimeMessage originMessage = new MimeMessage(null, inputStream);
			DkimMessage dkimMessage = getDkimMessage(originMessage);
			dkimMessage.writeTo(outputStream);
			result = outputStream.toString();
			LOGGER.info("outputStream.toString(): |{}|", result);
		} catch (Exception e) {
			LOGGER.error("ERR", e);
		}
		
		//check exists DKIM-Signature header
//		try (InputStream inputStream = Files.newInputStream(outputEml)) {
//			MimeMessage outputMessage = new MimeMessage(null, inputStream);
//			String[] header = outputMessage.getHeader("DKIM-Signature");
//			assert header.length > 0;
//		}
		return null;
	}

	private DkimMessage getDkimMessage(MimeMessage message) throws InvalidKeySpecException, NoSuchAlgorithmException, IOException, MessagingException {
		final String domain = "enders.co.kr";
		final String selector = "enders";
//		ClassPathResource privateKeyResource = new ClassPathResource("keys/pkcs8.rsa.private");
//		RSAPrivateKey privateKey = RSAKeyLoader.loadPrivateKeyPKCS8(privateKeyResource.getFile().toPath());
		RSAPrivateKey privateKey = RSAKeyLoader.loadPrivateKeyPKCS8( Paths.get("/Users/chochongtae/enders/ws/ws_axon_ums_bulk/AXON_UMS_BULK/install/internal/conf/private.pem") );

		DkimSigner dkimSigner = new DkimSigner(domain, selector, privateKey);
//		dkimSigner.setHeaderCanonicalization(Canonicalization.SIMPLE);
//		dkimSigner.setBodyCanonicalization(Canonicalization.SIMPLE);
		
		dkimSigner.setHeaderCanonicalization(Canonicalization.RELAXED);
		dkimSigner.setBodyCanonicalization(Canonicalization.RELAXED);
		// From을 제외한 모든 헤더를 삭제
					dkimSigner.removeHeaderToSign("To");
					dkimSigner.removeHeaderToSign("Subject");
					dkimSigner.removeHeaderToSign("Content-Description");
					dkimSigner.removeHeaderToSign("Content-ID");
					dkimSigner.removeHeaderToSign("Content-Type");
					dkimSigner.removeHeaderToSign("Content-Transfer-Encoding");
					dkimSigner.removeHeaderToSign("Cc");
					dkimSigner.removeHeaderToSign("Date");
					dkimSigner.removeHeaderToSign("In-Reply-To");
					dkimSigner.removeHeaderToSign("List-Subscribe");
					dkimSigner.removeHeaderToSign("List-Post");
					dkimSigner.removeHeaderToSign("List-Owner");
					dkimSigner.removeHeaderToSign("List-Id");
					dkimSigner.removeHeaderToSign("List-Archive");
					dkimSigner.removeHeaderToSign("List-Help");
					dkimSigner.removeHeaderToSign("List-Unsubscribe");
					dkimSigner.removeHeaderToSign("MIME-Version");
					dkimSigner.removeHeaderToSign("Message-ID");
					dkimSigner.removeHeaderToSign("Resent-Sender");
					dkimSigner.removeHeaderToSign("Resent-Cc");
					dkimSigner.removeHeaderToSign("Resent-Date");
					dkimSigner.removeHeaderToSign("Resent-To");
					dkimSigner.removeHeaderToSign("Reply-To");
					dkimSigner.removeHeaderToSign("References");
					dkimSigner.removeHeaderToSign("Resent-Message-ID");
					dkimSigner.removeHeaderToSign("Resent-From");
					dkimSigner.removeHeaderToSign("Sender");
					// 필요한 헤더만 추가
					dkimSigner.addHeaderToSign("From");
					dkimSigner.addHeaderToSign("To");
					dkimSigner.addHeaderToSign("Subject");
					dkimSigner.addHeaderToSign("Date");
					dkimSigner.addHeaderToSign("Content-Type");
		dkimSigner.setSigningAlgorithm(SigningAlgorithm.SHA256_WITH_RSA);

		//지정된 도메인 및 지정된 selector에 대한 DNS 리소스 레코드가 올바르게 준비되었는지 확인하는 메서드다.
		//테스트를 위해서 실제로 DNS 구성을 하지 않았기 때문에 false로 지정한다.
		//리얼 환경에서는 아래 코드는 지정하지 않는 것이 좋다.
		dkimSigner.setCheckDomainKey(false);
		return new DkimMessage(message, dkimSigner);
	}
	
	//target/classes/sample.eml 파일에 대한 DKIM-Signature 헤더를 생성하여
	//DKIM-Signature 헤더가 삽입된 target/classes/sample-simple-javamail-dkim.eml 파일을 생성한다.
	Path simpleJavaMailSign() {
	    try {
	        SimpleJavaDKIMSign simpleJavaDKIMSign = new SimpleJavaDKIMSign();
//	        ClassPathResource resource = new ClassPathResource("sample.eml");
//	        Path originEmlPath = resource.getFile().toPath();
//	        Path outputEmlPath = originEmlPath.resolveSibling("sample-simple-javamail-dkim.eml");
//	        Path originEmlPath = Paths.get("/Users/chochongtae/enders/ws/ws_axon_ums_bulk/AXON_UMS_BULK/data/sample.eml");
//	        Path outputEmlPath = Paths.get("/Users/chochongtae/enders/ws/ws_axon_ums_bulk/AXON_UMS_BULK/data/sample-simple-javamail-dkim-SIMPLE.eml");
	        Path originEmlPath = Paths.get("/Users/chochongtae/enders/ws/ws_axon_ums_bulk/AXON_UMS_BULK/data/조종태-240202-2-첨부파일.eml");
	        Path outputEmlPath = Paths.get("/Users/chochongtae/enders/ws/ws_axon_ums_bulk/AXON_UMS_BULK/data/조종태-240202-2-첨부파일-simplejavamail-RELAXED.eml");
	        
	        simpleJavaDKIMSign.makeDkimSignMessage(originEmlPath, outputEmlPath);
	        return outputEmlPath;
	    } catch (Exception e) {
	    	e.printStackTrace();
	        return null;
	    }
	}
	
	
	Path signString() {
	    try {
	        SimpleJavaDKIMSign simpleJavaDKIMSign = new SimpleJavaDKIMSign();
	        String normal = "Reply-To: keultae@enders.co.kr\n"
	        		+ "From: =?euc-kr?B?QURNSU4=?= <keultae@enders.co.kr>\n"
	        		+ "To: =?euc-kr?B?sejB2Mjx?= <nothing5748@gmail.com>\n"
	        		+ "Subject: =?euc-kr?B?wbbBvsXCLTI0MDIwMS0z?=\n"
	        		+ "Date: Thu, 1 Feb 2024 15:57:02 +0900\n"
	        		+ "MIME-Version: 1.0\n"
	        		+ "Content-Type: text/html; charset=\"euc-kr\"\n"
	        		+ "Content-Transfer-Encoding: Base64\n"
	        		+ "Message-ID: LUm+Q0CYLS6ATiKYLUWYLEH0\n"
	        		+ "X-USER-ID: ch6zdBipe0TwKEm61\n"
	        		+ "X-USER-NM: 9shH8NVH8exr\n"
	        		+ "X-Mailer: AXON_v1.0\n"
	        		+ "\n"
	        		+ "PCEtLU5FT19fUkVTUE9OU0VfX1NUQVJULS0+PGltZyBzcmM9Imh0dHA6Ly8yMTEuNTUuNzUuMjQy\n"
	        		+ "OjkwOTAvZW1zL3Jlc3AvcmVzcG9uc2UuanNwPzIwMjQwMzAzMTU1MHwwMDB8bm90aGluZzU3NDh8\n"
	        		+ "MTg5fDF8MXxHUlN8MTR8MDAzIiB3aWR0aD0wIGhlaWdodD0wIGJvcmRlcj0wPjwhLS1ORU9fX1JF\n"
	        		+ "U1BPTlNFX19FTkQtLT48cD6+yLPnx8+8vL/kLjwvcD48cD4mbmJzcDs8L3A+PHA+RU1BSUw9bm90\n"
	        		+ "aGluZzU3NDhAZ21haWwuY29tPC9wPjxwPk5BTUU9sejB2MjxPC9wPjxwPklEPW5vdGhpbmc1NzQ4\n"
	        		+ "PC9wPjxwPkFHRT89MzA8L3A+PHA+R0VOREVSPz1GZW1hbGU8L3A+PHA+QklaPz1zZXJ2aWVjZTwv\n"
	        		+ "cD48cD4mbmJzcDs8L3A+PHA+sKi758fVtM+02S4/Jm5ic3A7PC9wPg0K\r\n";
	        
	        String output = addDKIMSignature(normal, "enders.co.kr", "enders", 
	        		"/Users/chochongtae/enders/ws/ws_axon_ums_bulk/AXON_UMS_BULK/install/internal/conf/private.pem");
	        return null;
	    } catch (Exception e) {
	    	e.printStackTrace();
	        return null;
	    }
	}
	
	
	public static String addDKIMSignature(String normal, String domain, String selector, String privateKeyFilename) {
		String result = null;
		
		try (InputStream inputStream = new ByteArrayInputStream(normal.getBytes());
				OutputStream outputStream = new ByteArrayOutputStream()) {
			MimeMessage originMessage = new MimeMessage(null, inputStream);
			
			RSAPrivateKey privateKey = RSAKeyLoader.loadPrivateKeyPKCS8( Paths.get(privateKeyFilename) );
			DkimSigner dkimSigner = new DkimSigner(domain, selector, privateKey);
			dkimSigner.setHeaderCanonicalization(Canonicalization.RELAXED);
			dkimSigner.setBodyCanonicalization(Canonicalization.RELAXED);
			// From을 제외한 모든 헤더를 삭제
			dkimSigner.removeHeaderToSign("To");
			dkimSigner.removeHeaderToSign("Subject");
			dkimSigner.removeHeaderToSign("Content-Description");
			dkimSigner.removeHeaderToSign("Content-ID");
			dkimSigner.removeHeaderToSign("Content-Type");
			dkimSigner.removeHeaderToSign("Content-Transfer-Encoding");
			dkimSigner.removeHeaderToSign("Cc");
			dkimSigner.removeHeaderToSign("Date");
			dkimSigner.removeHeaderToSign("In-Reply-To");
			dkimSigner.removeHeaderToSign("List-Subscribe");
			dkimSigner.removeHeaderToSign("List-Post");
			dkimSigner.removeHeaderToSign("List-Owner");
			dkimSigner.removeHeaderToSign("List-Id");
			dkimSigner.removeHeaderToSign("List-Archive");
			dkimSigner.removeHeaderToSign("List-Help");
			dkimSigner.removeHeaderToSign("List-Unsubscribe");
			dkimSigner.removeHeaderToSign("MIME-Version");
			dkimSigner.removeHeaderToSign("Message-ID");
			dkimSigner.removeHeaderToSign("Resent-Sender");
			dkimSigner.removeHeaderToSign("Resent-Cc");
			dkimSigner.removeHeaderToSign("Resent-Date");
			dkimSigner.removeHeaderToSign("Resent-To");
			dkimSigner.removeHeaderToSign("Reply-To");
			dkimSigner.removeHeaderToSign("References");
			dkimSigner.removeHeaderToSign("Resent-Message-ID");
			dkimSigner.removeHeaderToSign("Resent-From");
			dkimSigner.removeHeaderToSign("Sender");
			// 필요한 헤더만 추가
			dkimSigner.addHeaderToSign("From");
			dkimSigner.addHeaderToSign("To");
			dkimSigner.addHeaderToSign("Subject");
			dkimSigner.addHeaderToSign("Date");
			dkimSigner.addHeaderToSign("Content-Type");
			dkimSigner.setSigningAlgorithm(SigningAlgorithm.SHA256_WITH_RSA);

			//지정된 도메인 및 지정된 selector에 대한 DNS 리소스 레코드가 올바르게 준비되었는지 확인하는 메서드다.
			//테스트를 위해서 실제로 DNS 구성을 하지 않았기 때문에 false로 지정한다.
			//리얼 환경에서는 아래 코드는 지정하지 않는 것이 좋다.
			dkimSigner.setCheckDomainKey(false);
			DkimMessage dkimMessage = new DkimMessage(originMessage, dkimSigner);
			dkimMessage.writeTo(outputStream);
			result = outputStream.toString();
//			LOGGER.info("최종 MIME START|\r\n{}|END", result);
		} catch (Exception e) {
			LOGGER.error("ERR", e);
		}
		
		//check exists DKIM-Signature header
//		try (InputStream inputStream = Files.newInputStream(outputEml)) {
//			MimeMessage outputMessage = new MimeMessage(null, inputStream);
//			String[] header = outputMessage.getHeader("DKIM-Signature");
//			assert header.length > 0;
//		}
		
		return result;
	}
	
	public static void main(String[] args) {
		LOGGER.info("START");
		SimpleJavaDKIMSign simple = new SimpleJavaDKIMSign();
		simple.simpleJavaMailSign();
//		simple.signString();
		LOGGER.info("END");
		LOGGER.info("SigningAlgorithm.SHA256_WITH_RSA: {}", SigningAlgorithm.SHA256_WITH_RSA.getDkimNotation());
		LOGGER.info("SigningAlgorithm.SHA1_WITH_RSA: {}", SigningAlgorithm.SHA1_WITH_RSA.getDkimNotation());
	}
}
