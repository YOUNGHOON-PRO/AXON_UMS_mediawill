package messager.common;

import java.io.UnsupportedEncodingException;

import messager.common.util.MailHeaderEncoder;

import messager.common.util.MimeTable;

import messager.common.util.Base64Encoder;

/**
 * 머지가 필요하지 않은 첨부파일을 표현한다. <br>
 * 대상자의 정보가 필요하지 않으므로 Center에서 Message의 정보를 가져올 때 
 * 컨텐츠의 헤더를 생성하고 데이타를 base64로 인코딩 한다.
 */
public class FileContent implements java.io.Serializable {
	/** 라인 구분 String */
	private final static String lineSeparator = "\r\n";

	/** 첨부파일의 컨텐츠 헤더와 base64로 인코딩된 body를 포함한다.*/
	private String content;

	/**
	 * AttachFile객체를 생성한다.
	 * 
	 * @param data byte배열로 저장된 첨부파일
	 * @param attachName 첨부파일의 파일명
	 * @param headerEncoder 컨텐츠 헤더를 인코딩를 실행할 객체
	 */
	public FileContent(byte[] data, String name, MailHeaderEncoder headerEncoder)
			throws UnsupportedEncodingException {
		content = create(name, data, headerEncoder);
	}

	/** 
	 * 첨부파일의 컨텐츠를 생성한다
	 * 
	 * @param name 첨부파일의 파일명
	 * @param data 첨부파일의 내용
	 * @param headerEncoder 컨텐츠의 헤더를 인코딩할때 실행될 객체
	 * @return 컨텐츠의 헤더와 첨부파일의 내용을 base64로 인코딩된 결과를 포함한 String 객체
	 * @throws UnsupportedEncodingException 지원되지 않는 charset일 경우
	 */
	private String create(String name, byte[] data,
			MailHeaderEncoder headerEncoder)
			throws UnsupportedEncodingException {
		StringBuffer buffer = new StringBuffer();
		
		//컨텐츠의 헤더 생성
		createHeader(name, headerEncoder, buffer);
		buffer.append(lineSeparator); //헤더와 바디의 구분
		//첨부파일의 내용을 Base64로 인코딩
		Base64Encoder encoder = new Base64Encoder();
		buffer.append(encoder.encode(data)); //데이타를 인코딩해서 추가
		return buffer.toString();
	}

	/**
	 * 컨텐츠의 헤더를 생성한다.
	 * 
	 * @param fileName 첨부파일의 파일명
	 * @param headerEncoder 컨텐츠 헤더를 인코딩 한다.
	 * @return String 컨텐츠 헤더
	 */
	private void createHeader(String fileName, MailHeaderEncoder headerEncoder,
			StringBuffer buffer) throws UnsupportedEncodingException {
		String encAttachName = headerEncoder.encodeText(fileName);
		String contentType = MimeTable.getContentType(fileName);
		buffer.append("Content-Type: ").append(contentType).append(';').append(
				lineSeparator).append('\t').append("name=\"").append(
				encAttachName).append('\"').append(lineSeparator).append(
				"Content-Transfer-Encoding: base64").append(lineSeparator)
				.append("Content-Disposition: attachment;").append(
						lineSeparator).append('\t').append("filename=\"")
				.append(encAttachName).append('\"').append(lineSeparator);
	}

	/**
	 * 첨부파일의 컨텐츠를 얻는다.
	 * 
	 * @return 첨부파일의 컨텐츠
	 */
	public String getContent() {
		return content;
	}
}