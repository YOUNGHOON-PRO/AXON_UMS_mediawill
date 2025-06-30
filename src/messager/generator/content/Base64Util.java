package messager.generator.content;

public class Base64Util {
	
	public static void foldingBase64(String encodedString, StringBuffer buffer) {
		int totalLen = encodedString.length();
		int foldingLen = 76;
		int foldingCount = totalLen / foldingLen;
		int other = totalLen % foldingLen;
		String line = null;
		
		for(int i = 0; i < foldingCount; i++) {
			line = encodedString.substring(i * foldingLen, i * foldingLen + foldingLen);
			buffer.append(line).append("\r\n");
		}
		if(other > 0) {
			line = encodedString.substring(foldingCount * foldingLen);
			buffer.append(line).append("\r\n");
		}
	}
}
