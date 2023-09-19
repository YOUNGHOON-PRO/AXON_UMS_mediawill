package messager.common;

import java.io.UnsupportedEncodingException;

import messager.common.util.MailHeaderEncoder;

import messager.common.util.CharsetTable;

import messager.common.util.MimeTable;

/**
 * 템플릿을 저장한 클래스로서 템플릿을 분석해서 저장한다. 컨텐츠 헤더와 MergeElement와 Template이 저장된 ArrayList로
 * 구성된다. ArrayList 객체에 저장된 Template은 WEBAGENT의 URL를 관리하기 위한것이고 컨텐츠 헤더가 null이다.
 * 컨텐츠 헤더는 메일 제목과 같은 경우는 null로 채워진다.
 */
public class TemplateContent2 implements java.io.Serializable {

	private final static String lineSeparator = "\r\n";

	private final static String defaultContentType = "text/html";

	private String contentHeader;

	private Template template;
	
	private Template2 template2;

	private boolean isEncrypt;

	private String encryptKey;

	private String encryptType;

	/**
	 * 컨텐츠의 헤더가 존재하지 않은 Template 객체를 생성한다. 제목이나 WEBAGENT의 URL 를 위한 Template
	 *
	 * @param list
	 *            템플릿을 분석해서 저장한 ArrayList
	 * @param aType
	 *            템플릿의 타입 (WEBAGENT_TYPE, NORMAL_TYPE)
	 */
	public TemplateContent2(Template2 template2) {
          //기본은 한국어, 8bit 로 한다.
          this(template2, null, "EUC-KR", "8bit");
	}

	public TemplateContent2(Template2 template2, String charsetCode, String encCode) {

		this(template2, null, charsetCode, encCode);

	}

	public TemplateContent2(Template2 template2, String contentTypeCode,
			String charsetCode, String encCode) {

		contentHeader = createHeader(charsetCode, encCode, contentTypeCode);

		this.template2 = template2;

	}

	/**
	 * 첨부파일이 아닌 컨텐츠의 헤더가 존재하는 Template 객체를 생성한다.
	 *
	 * @param list
	 *            템플릿을 분석해 저장한 ArrayList
	 * @param fileName
	 *            템플릿의 File명(Content-Type를 얻는데 사용된다)
	 * @param aCharsetCode
	 *            컨텐츠의 charset code
	 * @param aEncCode
	 *            encoding code (base64[1]와 8bit)
	 */
/*
	public TemplateContent(Template template, String fileName,
			int aCharsetCode, int aEncCode) {

		this.template = template;

		contentHeader = createHeader(aCharsetCode, aEncCode, fileName);

	}
*/

	/**
	 * 첨부파일인 템플릿에 대한 Template객체를 생성한다.
	 *
	 * @param list
	 *            템플릿을 분석해 저장한 ArrayList
	 * @param fileName
	 *            템플릿의 파일명으로 Content-Type과 attachName필드로 이용된다.
	 * @param aCharsetCode
	 *            컨텐츠의 charset를 나타내는 코드(CharsetTable에서 charset를 얻는다.)
	 * @param headerEncoder
	 *            컨텐츠이 헤더를 인코딩 하는데 사용된다.
	 */
	public TemplateContent2(Template template, String fileName, MailHeaderEncoder headerEncoder)
			throws UnsupportedEncodingException {

		this.template = template;

		contentHeader = createHeader(headerEncoder, fileName);

	}

	/**
	 * 첨부파일이 아닌 경우 컨텐츠의 헤더를 생성한다.
	 *
	 * @param charsetCode
	 *            컨텐츠의 charset를 나타내는 코드
	 * @param encCode
	 * @param name
	 *            파일명
	 * @return 컨텐츠의 헤더
	 */
	private String createHeader(String charsetCode, String encCode, String name) {

		StringBuffer buffer = new StringBuffer();

		//String mimeCharsetName = CharsetTable.mimeCharsetName(charsetCode); //mime
																			// charset
		//String encodeName = MimeTable.encodeName(encCode); //encode (base64,
														   // 8bit)

		String contentType = null;

                if(name.equals("HTML")) {
                  contentType = defaultContentType;
                } else if(name.equals("TEXT")) {
                  contentType = "text/plain";
                } else {
                  contentType = defaultContentType;
                }

		// header 생성
		//buffer.append("Content-Type: ").append(contentType).append("; charset=\"").append(mimeCharsetName).append('\"').append(lineSeparator).append("Content-Transfer-Encoding: ").append(encodeName);
                buffer.append("Content-Type: ").append(contentType).append("; charset=\"").append(charsetCode).append('\"').append(lineSeparator).append("Content-Transfer-Encoding: ").append(encCode);
		return buffer.toString();

	}

        /***********************************************
         //구버전의 contentTypeCode 숫자 일때 사용 한 메소드
         //지금은 사용을 안함.
         //writed by 오범석
         ***********************************************/
	private String createHeader(String charsetCode, String encCode,
			int contentTypeCode) {

		StringBuffer buffer = new StringBuffer();

		//String mimeCharsetName = CharsetTable.mimeCharsetName(charsetCode); //mime
																			// charset
		//String encodeName = MimeTable.encodeName(encCode); //encode (base64,
														   // 8bit)
		String contentType = null;

		if (contentTypeCode == 0) {

			contentType = "text/plain";

		} else {

			contentType = defaultContentType;

		}

		//buffer.append("Content-Type: ").append(contentType).append("; charset=\"").append(mimeCharsetName).append('\"').append(lineSeparator).append("Content-Transfer-Encoding: ").append(encodeName);
                buffer.append("Content-Type: ").append(contentType).append("; charset=\"").append(charsetCode).append('\"').append(lineSeparator).append("Content-Transfer-Encoding: ").append(encCode);

		return buffer.toString();

	}

	/**
	 * 첨부 파일의 컨텐츠 헤더를 생성한다.
	 *
	 * @param headerEncoder
	 *            헤더 인코딩
	 * @param name
	 *            파일명
	 * @return 컨텐츠의 헤더
	 */
	private String createHeader(MailHeaderEncoder headerEncoder, String name)
			throws UnsupportedEncodingException {

		String eName = headerEncoder.encodeText(name); //fileName encode
		String contentType = MimeTable.getContentType(name); //Content-Type

		StringBuffer buffer = new StringBuffer();

		//헤더 생성
		buffer.append("Content-Type: ").append(contentType).append(';').append(
				lineSeparator).append('\t').append("name=\"").append(eName)
				.append('\"').append(lineSeparator).append(
						"Content-Transfer-Encoding: base64\r\n").append(
						"Content-Disposition: attachment;\r\n").append('\t')
				.append("filename=\"").append(eName).append('\"').append(
						lineSeparator);

		return buffer.toString();

	}

	/**
	 * 컨텐츠의 헤더를 얻는다.
	 *
	 * @return 컨텐츠 헤더
	 */
	public String getHeader() {

		return contentHeader;

	}

	public Template2 getTemplate2() {

		return template2;

	}

}

