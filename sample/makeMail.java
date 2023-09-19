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
import com.yettiesoft.javarose.SGException;


public class makeMail {
	private static int		VM_FLAG_TEXTMSG				= 0x0001;
	private static int		VM_FLAG_STD_PDF				= 0x0002;
	private static int		VM_FLAG_STD_ZIP				= 0x0004;
	private static int		VM_FLAG_TEXTMSG_TEMPLATE	= 0x0008;
	private static int		VM_FLAG_PDF_TEMPLATE		= 0x0010;
	private static int		VM_FLAG_BIN_TEMPLATE		= 0x0020;
	private static int		VM_FLAG_BIN_MSG_TEMPLATE		= 0x0040;
	private static int		VM_FLAG_MBIN_MSG_TEMPLATE		= 0x0080;
	private static int		VM_FLAG_OFFICE_TEMPLATE		= 0x0100;
	
	public static void main(String[] args) {
		int			aFlag = 0;
		String		aKeyParam = "123456";
		
		//String 		contentFilePath = "./template/ktemail.html";
		String 		contentFilePath = "./template/uplusemail.html";
		String		contentFileEncoding = "utf-8";		
		/************************************************************************************************/
		/* STEP.1 check args */
		if (args.length == 0) {
			aFlag	|= VM_FLAG_TEXTMSG;
			aFlag	|= VM_FLAG_STD_PDF;
			aFlag	|= VM_FLAG_STD_ZIP;
			aFlag	|= VM_FLAG_TEXTMSG_TEMPLATE;
			aFlag	|= VM_FLAG_PDF_TEMPLATE;
			aFlag	|= VM_FLAG_BIN_TEMPLATE;
			aFlag	|= VM_FLAG_BIN_MSG_TEMPLATE;
			aFlag	|= VM_FLAG_MBIN_MSG_TEMPLATE;
			aFlag	|= VM_FLAG_OFFICE_TEMPLATE;
			
		}
		else {
			int	i = 0;
			while (i < args.length) {
				switch (Integer.parseInt(args[i])) {
				case 1:
					aFlag	|= VM_FLAG_TEXTMSG;
					break;
				case 2:
					aFlag	|= VM_FLAG_STD_PDF;
					break;
				case 3:
					aFlag	|= VM_FLAG_STD_ZIP;
					break;
				case 4:
					aFlag	|= VM_FLAG_TEXTMSG_TEMPLATE;
					break;
				case 5:
					aFlag	|= VM_FLAG_PDF_TEMPLATE;
					break;
				case 6:
					aFlag	|= VM_FLAG_BIN_TEMPLATE;
					break;
				case 7:
					aFlag	|= VM_FLAG_BIN_MSG_TEMPLATE;
					break;
				case 8:
					aFlag	|= VM_FLAG_MBIN_MSG_TEMPLATE;
					break;
				case 9:
					aFlag	|= VM_FLAG_OFFICE_TEMPLATE;
					break;
					
				}
				i++;
			}
		}
				
		
		
		VMCipherImpl aCipherInterface = null;
		
		// 설정파일 별도로 사용하는 경우
		/*
		String configPath = "./properties/vestmail.properties";
		aCipherInterface = new VMCipherImpl(configPath);
		// 설정 파일 동적으로 변경하는 경우
		aCipherInterface.setConfig(configPath);
		*/
		try {
			aCipherInterface = new VMCipherImpl();

			/**
			 * STEP.1 일반 HTML 암호화.
			 * 일반 HTML 암호화는 설정에 미리 정의된 템플릿과 인코딩셋을 이용하는 방법으로
			 * 파일의 위치를 직접 입력하거나 파일 스트림을 넘겨주는 방법이 존재한다.
			 */
			if ( (aFlag & VM_FLAG_TEXTMSG) != 0) {
				/* 파일의 위치를 인자로 넘겨주는 경우 */
				
				String encMail = aCipherInterface.makeMailFile(aKeyParam, contentFilePath);
				try {
					saveFile("./sample/output/htmlfile_enc.html", encMail.getBytes(), contentFileEncoding);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				/* 파일 스트림을 미리 읽고 넘겨주는 경우 */
				String plainMail;
				try {
					plainMail = VMUtil.readFile (contentFilePath, contentFileEncoding);
					encMail = aCipherInterface.makeMailContent (aKeyParam, plainMail);
					saveFile("./sample/output/htmlctx_enc.html", encMail.getBytes(), contentFileEncoding);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			/**
			 * STEP.2 템플릿을 이용한 HTML 암호화
			 * 일반 HTML 암호화와 동일한 기능을 제공하며 템플릿을 동적으로 입력할 수 있다.
			 * 템플릿 역시 파일의 위치 및 스트림을 직접 입력할 수 있다.
			 */
			if ( (aFlag & VM_FLAG_TEXTMSG_TEMPLATE) != 0) {
				/* 하나의 템플릿만을 입력받는 방법 */
				String encMail = aCipherInterface.makeMailFileWithTemplate (aKeyParam,
																			contentFilePath,
																			"./template/htmlcipher/template.html");
				try {
					saveFile("./sample/output/template_file_type_enc.html", encMail.getBytes(), contentFileEncoding);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				/* 템플릿을 head, body, css를 분리해서 외부로부터 입력받는 방법. */
				/*
				encMail = aCipherInterface.makeMailFileWithTemplate (aKeyParam,
																	 contentFilePath,
																	 null, 
																	 "./template/htmlcipher/template_head.html",
																	 "./template/htmlcipher/template_body.html",
																	 null);
				try {
					saveFile("./sample/output/template_file_type2_enc.html", encMail.getBytes(), contentFileEncoding);
				} catch (IOException e) {
					e.printStackTrace();
				}
				*/
				/* 파일 스트림으로 입력받는 방법 */
				String plainMail, templateType1, templateType2Head, templateType2Body;
				try {
					plainMail			= readFile (contentFilePath, contentFileEncoding);
					templateType1		= readFile ("./template/htmlcipher/template.html", contentFileEncoding);
					templateType2Head	= readFile ("./template/htmlcipher/template_head.html", contentFileEncoding);
					templateType2Body	= readFile ("./template/htmlcipher/template_body.html", contentFileEncoding);

					encMail = aCipherInterface.makeMailContentWithTemplate (aKeyParam, plainMail, templateType1);
					saveFile ("./sample/output/template_ctx_type_enc.html", encMail.getBytes(), contentFileEncoding);
					/*
					encMail = aCipherInterface.makeMailContentWithTemplate (aKeyParam, plainMail, null, templateType2Head, templateType2Body, null);
					saveFile ("./sample/output/template_ctx_type2_enc.html", encMail.getBytes(), contentFileEncoding);
					*/
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			/**
			 * STEP.3 표준 PDF 암호화
			 * 표준 PDF 암호화를 이용한 PDF 표준 암호화 기능을 제공한다.
			 * PDF 파일을 배포할때 사용할 수 있다.
			 * 무작위 대입공격에 취약할 수 있다.
		 	 * @param aKeyParam: 암호화 PDF 파일을 위한 비밀번호
			 * @param aPath: 암호화 PDF 파일의 경로

			 */
			if ( (aFlag & VM_FLAG_STD_PDF) != 0) {
				//long beforeTime = System.currentTimeMillis();
				byte[] encPDF = aCipherInterface.makeEncryptedPDF(aKeyParam, "./template/test.pdf");
				//long afterTime = System.currentTimeMillis(); 
				//long secDiffTime = (afterTime - beforeTime);
				//System.out.println("makeEncryptedPDF 시간차이(ms) : "+secDiffTime);
				try {
					saveFile("./sample/output/standard_pdfenc.pdf", encPDF);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				//beforeTime = System.currentTimeMillis(); 
				encPDF = aCipherInterface.makeEncryptedPDFAES(aKeyParam, "./template/test.pdf");
				//afterTime = System.currentTimeMillis(); 
				//secDiffTime = (afterTime - beforeTime);
				//System.out.println("makeEncryptedPDFAES 시간차이(ms) : "+secDiffTime);
				
				try {
					saveFile("./sample/output/standard_pdfenc_aes.pdf", encPDF);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				
			}

			/**
			 * STEP.4 표준 ZIP 암호화
			 * 표준 ZIP 암호화 기능을 제공한다.
			 * PDF를 제외한 타 바이너리(doc, xls, hwp와 같은) 파일을 암호화할때 사용할 수 있다.
			 * 무작위 대입공격에 취약할 수 있다.
			 */
			if ( (aFlag & VM_FLAG_STD_ZIP) != 0) {
				byte[] encZIP = aCipherInterface.makeEncryptedZip(aKeyParam, "./template/policy.doc");
				try {
					saveFile("./sample/output/standard_zipenc.zip", encZIP);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			/**
			* STEP.5 암호화 pdf 파일을 생성한다.
			* STEP.3과 다른 점은 pdf.js을 이용한 암호화 기능을 제공한다.
			*/
			/**
			* STEP.5-1 파일 경로를 입력받는 경우
			* @param aKeyParam: 보안메일 생성을 위한 비밀번호
			* @param aFilePath: 보안이 필요한 pdf 파일의 경로
			* @param aTemplatePath: 보안 메일을 구성하는데 필요한 템플릿 파일의 경로
			* @param file_encoding: char file_encoding
			*/

			if ( (aFlag & VM_FLAG_PDF_TEMPLATE) != 0) {
				String encBin = aCipherInterface.makeAttachFileWithTemplate (aKeyParam,
																			 "./template/policy.pdf",
																			 "./template/bincipher_pdfjs/template.html",
																			 "utf-8");
				try {
					saveFile("./sample/output/pdfjs_enc.html", encBin.getBytes("utf-8"));
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				String	aFileName = "plain.pdf";
				byte[]	aBin;
				String	aTemplate;
				
				try {
					aTemplate = readFile ("./template/bincipher_pdfjs/template.html", "utf-8");
					aBin = readBinaryFile ("./template/policy.pdf");
					encBin = aCipherInterface.makeAttachFileWithTemplate(aKeyParam, aFileName, aBin, aTemplate);
					saveFile("./sample/output/pdfjs_ctx_enc.html", encBin.getBytes("utf-8"));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
//			/**
//			* STEP.6 암호화 파일 HTML을 생성한다.
//			*/
//			/**
//			* STEP.6-1 파일 경로를 입력받는 경우
//			* @param aKeyParam: 보안메일 생성을 위한 비밀번호
//			* @param aFilePath: 보안이 필요한 파일의 경로
//			* @param aTemplatePath: 보안 메일을 구성하는데 필요한 템플릿 파일의 경로
//			*/
//			if ( (aFlag & VM_FLAG_BIN_TEMPLATE) != 0) {
//				String encBin = aCipherInterface.makeAttachFileWithTemplate (aKeyParam,
//																			 "./template/policy.pdf",
//																			 "./template/htmlcipher/template.html");
//				try {
//					saveFile("./sample/output/bin_enc.html", encBin.getBytes(), contentFileEncoding);
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//
//				/**
//				* STEP.6-2 파일 스트림를 입력받는 경우
//				* @param aKeyParam: 보안메일 생성을 위한 비밀번호
//				* @param aFileName: 복호화 시 저장할 파일명
//				* @param aBin: 암호화할 파일
//				* @param aTemplate: 보안 메일을 구성하는데 필요한 템플릿
//				*/
//
//				String	aFileName = "plain.pdf";
//				byte[]	aBin;
//				String	aTemplate;
//				
//				try {
//					aTemplate = readFile ("./template/htmlcipher/template.html", contentFileEncoding);
//					aBin = readBinaryFile ("./template/policy.pdf");
//
//					encBin = aCipherInterface.makeAttachFileWithTemplate(aKeyParam, aFileName, aBin, aTemplate);
//					saveFile("./sample/output/bin_ctx_enc.html", encBin.getBytes(), contentFileEncoding);
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}

			/**
			 * STEP.7 템플릿을 이용한 HTML + 첨부파일 암호화
			 * 일반 HTML 암호화와 동일한 기능을 제공하며 템플릿을 동적으로 입력할 수 있다.
			 * 템플릿 역시 파일의 위치 및 스트림을 직접 입력할 수 있다.
			 * 첨부파일의 위치 및 파일명 만 입력이 가능하다.
			 * @param aKeyParam: 보안메일 생성을 위한 비밀번호
			 * @param plainMail: 보안이 필요한 html 파일 내용
			 * @param aFilePath: 암호화 할 파일의 경로
			 * @param file_encoding: char file_encoding
			 * @Param fileName : 첨부파일 이름, ""시 aFilePath 의 파일명을 따름
			 * @Param aBin : 첨부파일 binary, aFilePath와 함께 사용 불가
			 */
			if ( (aFlag & VM_FLAG_BIN_MSG_TEMPLATE) != 0) {
				
				String	aFileName = "./template/policy.pdf";
				byte[]	aBin;
				String	aTemplate = "./template/htmlcipher/template.html";
				String  encMail;
								
				try {
					String plainMail = readFile (contentFilePath, contentFileEncoding);
					encMail = aCipherInterface.makeMailContentAttachFile(aKeyParam, plainMail, aFileName );
					saveFile("./sample/output/attach_ctx_enc1.html", encMail.getBytes(), contentFileEncoding);
				}catch (IOException e) {
					e.printStackTrace();
				}
				

				/**
				 * STEP.7-1 템플릿을 이용한 HTML + 첨부파일 암호화
				 * 일반 HTML 암호화와 동일한 기능을 제공하며 템플릿을 동적으로 입력할 수 있다.
				 * 템플릿 역시 파일의 위치 및 스트림을 직접 입력할 수 있다.
				 * 첨부파일의 위치 및 파일명 만 입력이 가능하다.
				 * @param aKeyParam: 보안메일 생성을 위한 비밀번호
				 * @param plainMail: 보안이 필요한 html 파일 내용
				 * @param aFilePath: 암호화 할 파일의 경로
				 * @param file_encoding: char file_encoding
				 **/
				try {
					String plainMail = readFile (contentFilePath, contentFileEncoding);
					encMail = aCipherInterface.makeMailContentAttachFile(aKeyParam, plainMail, aFileName , contentFileEncoding);
					saveFile("./sample/output/attach_ctx_enc2.html", encMail.getBytes(), contentFileEncoding);
				}catch (IOException e) {
					e.printStackTrace();
				}
				
				/**
				* STEP.7-2 켐플릿을 이용한 HTML + 첨부파일 암호화
				* @param aKeyParam: 보안메일 생성을 위한 비밀번호
			 	* @param aFileNames: 암호화 할 파일의 경로 
			 	* @param plainMail: 보안이 필요한 html 파일 내용
				* @param aTemplate: 보안 메일을 구성하는데 필요한 템플릿 파일 경로
				*/
				
				try {
					String plainMail = readFile (contentFilePath, contentFileEncoding);
					aTemplate = "./template/htmlcipher/template.html";
					
					encMail = aCipherInterface.makeMailContentAttachFileWithTemplate(aKeyParam, aFileName, plainMail, aTemplate);
					saveFile("./sample/output/attach_ctx_enc3.html", encMail.getBytes(), contentFileEncoding);

				}catch (IOException e) {
					e.printStackTrace();
				}
				
				/**
				* STEP.7-3 켐플릿을 이용한 HTML + 첨부파일 암호화
				* @param aKeyParam: 보안메일 생성을 위한 비밀번호
			 	* @param aFileNames: 암호화 할 파일의 경로 
			 	* @param plainMail: 보안이 필요한 html 파일 내용
				* @param aTemplate: 보안 메일을 구성하는데 필요한 템플릿 파일 스트링
				*/
				
				try {
					String plainMail = readFile (contentFilePath, contentFileEncoding);
					aTemplate = readFile ("./template/htmlcipher/template.html", contentFileEncoding);
					
					encMail = aCipherInterface.makeMailContentAttachFileWithTemplateS(aKeyParam, aFileName, plainMail, aTemplate);
					saveFile("./sample/output/attach_ctx_enc4.html", encMail.getBytes(), contentFileEncoding);

				}catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			/**
			 * STEP.8 이용한 HTML + (멀티)첨부파일 암호화
			 * 일반 HTML 암호화와 동일한 기능을 제공하며 템플릿을 동적으로 입력할 수 있다.
			 * 템플릿 역시 파일의 위치 및 스트림을 직접 입력할 수 있다.
			 * 첨부파일의 위치 및 파일명 만 입력이 가능하다.
			 * @param aKeyParam: 보안메일 생성을 위한 비밀번호
			 * @param plainMail: 보안이 필요한 html 파일 내용
			 * @param aFileNames: 암호화 할 파일의 경로 ( String array )
			 * @param file_encoding: char file_encoding
			 */
			if ( (aFlag & VM_FLAG_MBIN_MSG_TEMPLATE) != 0) {
				
				
				String[]	aFileName = {"./template/policy.pdf", "./template/policy.doc", "./template/png.png", "./template/jpg.jpg", "./template/gif.gif"};
				String[]	aFileName2 = {"policy2.pdf", "policy2.doc"};
				byte[][]	aBin;
				String	aTemplate;
				String  encMail;
								
				try {
					String plainMail = readFile (contentFilePath, contentFileEncoding);
					encMail = aCipherInterface.makeMailContentMultiAttachFile(aKeyParam, plainMail, aFileName );
					saveFile("./sample/output/attach_ctx_enc_multi1.html", encMail.getBytes(), contentFileEncoding);
				}catch (IOException e) {
					e.printStackTrace();
				}
				
				try {
					String plainMail = readFile (contentFilePath, contentFileEncoding);
					encMail = aCipherInterface.makeMailContentMultiAttachFile(aKeyParam, plainMail, aFileName, contentFileEncoding);
					saveFile("./sample/output/attach_ctx_enc_multi2.html", encMail.getBytes(), contentFileEncoding);

				}catch (IOException e) {
					e.printStackTrace();
				}
				
				/**
				* STEP.8-2 켐플릿을 이용한 HTML + (멀티)첨부파일 암호화
				* @param aKeyParam: 보안메일 생성을 위한 비밀번호
			 	* @param aFileNames: 암호화 할 파일의 경로 ( String array )
			 	* @param plainMail: 보안이 필요한 html 파일 내용
				* @param aTemplate: 보안 메일을 구성하는데 필요한 템플릿 파일 경로
				*/
				
				try {
					String plainMail = readFile (contentFilePath, contentFileEncoding);
					aTemplate = "./template/htmlcipher/template.html";
					
					encMail = aCipherInterface.makeMailContentMultiAttachFileWithTemplate(aKeyParam, aFileName, plainMail, aTemplate);
					saveFile("./sample/output/attach_ctx_enc_multi3.html", encMail.getBytes(), contentFileEncoding);

				}catch (IOException e) {
					e.printStackTrace();
				}
				
				/**
				* STEP.8-3 켐플릿을 이용한 HTML + (멀티)첨부파일 암호화
				* @param aKeyParam: 보안메일 생성을 위한 비밀번호
			 	* @param aFileNames: 암호화 할 파일의 경로 ( String array )
			 	* @param plainMail: 보안이 필요한 html 파일 내용
				* @param aTemplate: 보안 메일을 구성하는데 필요한 템플릿 파일 스트링
				*/
				
				try {
					String plainMail = readFile (contentFilePath, contentFileEncoding);
					aTemplate = readFile ("./template/htmlcipher/template.html", contentFileEncoding);
					
					encMail = aCipherInterface.makeMailContentMultiAttachFileWithTemplateS(aKeyParam, aFileName, plainMail, aTemplate);
					saveFile("./sample/output/attach_ctx_enc_multi4.html", encMail.getBytes(), contentFileEncoding);
				}catch (IOException e) {
					e.printStackTrace();
				}
				
			}
			
			/**
			* STEP.9 OFFICE 파일을 암호화 한다
			* office 97 이상의 문서를 암호화 한다
			* @param aKeyParam: 보안메일 생성을 위한 비밀번호
			* @param aFileName: 복호화 시 저장할 파일명
			* @param aBin: 암호화할 파일
			* @param aTemplate: 보안 메일을 구성하는데 필요한 템플릿
			*/
			if((aFlag & VM_FLAG_OFFICE_TEMPLATE)!= 0) {
				
				byte[]	aBin;
				try {
					aBin = readBinaryFile ("./template/policy.docx");

					byte[] encBin = aCipherInterface.makeEncryptedOffice(aKeyParam, aBin );
					saveFile("./sample/output/office_enc.docx", encBin);
				} catch (IOException e) {
					e.printStackTrace();
				}
				/*
				 * office 97 이하 파일을 암호화 한다
				 * type 1 : doc 문서
				 * type 2 : xls 문서
				 * type 3 : ppt 문서 
				/*
				try {
					aBin = readBinaryFile ("./template/policy.doc");

					byte[] encBin = aCipherInterface.makeEncryptedOffice(aKeyParam, aBin, 1);
					saveFile("./sample/output/office_enc.doc", encBin);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				try {
					aBin = readBinaryFile ("./template/test.xls");

					byte[] encBin = aCipherInterface.makeEncryptedOffice(aKeyParam, aBin, 2);
					saveFile("./sample/output/office_enc.xls", encBin);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				try {
					aBin = readBinaryFile ("./template/test.ppt");

					byte[] encBin = aCipherInterface.makeEncryptedOffice(aKeyParam, aBin, 3);
					saveFile("./sample/output/office_enc.ppt", encBin);
				} catch (IOException e) {
					e.printStackTrace();
				}
				*/
			}
		} catch (SGException e) {
			e.printStackTrace();
		}

		/************************************************************************************************/
	}
	
	public static void saveFile(String fname, byte[] content) throws IOException {
    	File aEncFile = new File(fname);
		FileOutputStream os = new FileOutputStream(aEncFile);
		os.write(content);
		os.close();	
	}
	
	public static void saveFile(String fname, byte[] content, String charset) throws IOException {
    	File aEncFile = new File(fname);
    	aEncFile.createNewFile();
    	
    	BufferedWriter output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(aEncFile.getPath()), charset));
		output.write(new String(content));	
		output.close();
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
	
	public static byte[] readBinaryFile (String name) throws IOException {
		return readBinaryFile (new File(name));
	}
	
	public static byte[] readBinaryFile (File f) throws IOException {
		int fsize = (int) f.length();
		byte[] tmp = new byte[fsize];
		FileInputStream fi = new FileInputStream(f);
		fi.read(tmp);
		if (fi != null)
			fi.close();
		return tmp;
	}
}
