package messager.dkim;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

public class RSAKeyLoader {
	//PKCS1 private key를 loading 하기 위해서 BouncyCastle 라이브러리 사용
	//java는 PKCS1을 지원하지 않는다.
	//https://www.baeldung.com/java-read-pem-file-keys#2-read-private-key
	public static RSAPrivateKey loadPrivateKeyPKCS1(Path keyPath) throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
		Security.addProvider(new BouncyCastleProvider());
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		try (FileReader keyReader = new FileReader(keyPath.toFile());
			 PemReader pemReader = new PemReader(keyReader)) {
			PemObject pemObject = pemReader.readPemObject();
			byte[] content = pemObject.getContent();
			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(content);
			return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
		}
	}

	//PKCS8 private key를 loading
	public static RSAPrivateKey loadPrivateKeyPKCS8(Path keyPath) throws InvalidKeySpecException, NoSuchAlgorithmException, IOException {
//		String privateKeyData = Files.readString(keyPath, Charset.defaultCharset());
		
//		byte[] encodedBytes = Files.readAllBytes(Paths.get(filename));
		byte[] encodedBytes = Files.readAllBytes(keyPath);
		String privateKeyData = new String(encodedBytes, StandardCharsets.UTF_8);
		
		String privateKeyPEM = privateKeyData
			.replace("-----BEGIN PRIVATE KEY-----", "")
			.replace("-----END PRIVATE KEY-----", "");
		byte[] encoded = Base64.decodeBase64(privateKeyPEM);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
		return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
	}
}
