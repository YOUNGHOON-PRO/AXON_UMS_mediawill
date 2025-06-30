package messager.generator.content;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import messager.common.*;
import messager.common.util.*;
import messager.generator.config.ConfigLoader;
import messager.generator.repository.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import messager.dkim.SimpleJavaDKIMSign;

public class MultiPart extends Part {
	private static final Logger LOGGER = LogManager.getLogger(MultiPart.class.getName());
	
    /** boundary의 접두사 */
    private final static String BOUNDARY_PREFIX = "----=_NextPart_000_";

    /** 발송 정보 */
    private Message message;

    /** Mail Header 생성 */
    private MailHeader mailHeader;

    /** charset 명*/
    private String javaCharsetName;

    /** 컨텐츠 */
    private ContentPart mainPart;
    
    /** 컨텐츠 부분을 표현하는 객체2 */
    private ContentPart2 contentPart2;

    /** 컨텐츠 2*/
    private ContentPart2 mainPart2;

    
    private ArrayList contentPartList;

    private ArrayList attachFileList;

    private boolean isAttachFileWrite;

    private String boundary;

	public MultiPart(Message message, TemplateContent mainContent, ArrayList templateList, ArrayList fileList) {
        super(message);
        boundary = createBoundary(message.messageID);
        mailHeader = new MailHeader(message, boundary, 1);
        //기존의 mime charset과 java charset을 매핑 테이블로 처리 하던것을 이제는 사용안함
        //javaCharsetName = CharsetTable.javaCharsetName(message.charsetCode);
        javaCharsetName = message.charsetCode;
        mainPart = createMainPart(javaCharsetName, message.bodyEncodingCode, mainContent);
        contentPartList = createTemplatePartList(templateList, javaCharsetName);
        this.attachFileList = fileList;
    }
    
	public MultiPart(Message message, TemplateContent mainContent, ArrayList templateList, ArrayList fileList,
			TemplateContent2 mainContent2) {
	super(message);
	boundary = createBoundary(message.messageID);
	mailHeader = new MailHeader(message, boundary, 1);
	//기존의 mime charset과 java charset을 매핑 테이블로 처리 하던것을 이제는 사용안함
	//javaCharsetName = CharsetTable.javaCharsetName(message.charsetCode);
	javaCharsetName = message.charsetCode;
	mainPart = createMainPart(javaCharsetName, message.bodyEncodingCode, mainContent);
	mainPart2 = createMainPart(javaCharsetName, message.bodyEncodingCode, mainContent, mainContent2);
	contentPartList = createTemplatePartList(templateList, javaCharsetName);
	this.attachFileList = fileList;
	}
    
	public Address createContent(ReceiverInfo receiver, File contentFile, SendTo toUser, Message message)
        throws GeneratorException {
        Address address = new Address(toUser.email);
        StringBuffer buffer = new StringBuffer();
        StringBuffer buffer2 = new StringBuffer();
        mailHeader.create(receiver, buffer, toUser);
        buffer.append(lineSeparator);
		buffer.append(lineSeparator).append("--").append(boundary).append(lineSeparator);

        mainPart.create(receiver, buffer, message);
       // mainPart2.create(receiver, buffer, message);
        
        
        // 웹에이전트 보안 HTML
		if ("Y".equals(message.webagent_secuYn) && message.webagent_sourceUrl != null
				&& "HTML".equals(message.webagent_secuAttTyp)) {

        	String msg ="";  // 웹에이전트 URL 내용

        	
        	mainPart2.create(receiver, buffer2, message , toUser.id, toUser); //보안메일 체크 때문에 message 전달
			msg = buffer2.toString();
			//LOGGER.info("msg : "+ msg);
			
            String AAA ="";  // 보안메일 내용
            try {
    			AAA = readFile("./sample/output/"+ message.taskNo +"_"+toUser.id+"_screatfile.html", "UTF-8");
    		} catch (IOException e) {
    			// TODO Auto-generated catch block
				// e.printStackTrace();
				LOGGER.error(e);
    		}
			
	        String encodedString = Base64.getEncoder().encodeToString(AAA.getBytes());
	        //LOGGER.info("encodedString : "+ encodedString);
	        
	        buffer.append(lineSeparator);
	        
	        buffer.append("------=_NextPart_000_"+message.taskNo+"-"+message.subTaskNo);
	        buffer.append(lineSeparator);
			buffer.append("Content-Type: ").append("text/html").append(';').append(lineSeparator).append('\t')
					.append("name=\"").append("secretfile.html").append('\"').append(lineSeparator)
					.append("Content-Transfer-Encoding: base64").append(lineSeparator)
					.append("Content-Disposition: attachment;").append(lineSeparator).append('\t').append("filename=\"")
					.append("secretfile.html").append('\"').append(lineSeparator);
	        
			buffer.append(lineSeparator);
			// buffer.append(encodedString);
			// Base64를 76글자씩 폴딩처리
			Base64Util.foldingBase64(encodedString, buffer);
	        
	        buffer.append(lineSeparator);
	        buffer.append(lineSeparator);
			
	    // 웹에이전트 보안 PDF    
		} else if ("Y".equals(message.webagent_secuYn) && message.webagent_sourceUrl != null
				&& "PDF".equals(message.webagent_secuAttTyp)) {

        	String msg ="";  // 웹에이전트 URL 내용

        	
        	mainPart2.create(receiver, buffer2, message , toUser.id, toUser); //보안메일 체크 때문에 message 전달
			msg = buffer2.toString();
			//LOGGER.info("msg : "+ msg);
			
	        String pdfFILE= "./sample/output/"+ message.taskNo +"_"+toUser.id+"_screatfile.pdf";
	        String encodedString ="";
	        try {
				byte[] inFileBytes = Files.readAllBytes(Paths.get(pdfFILE)); 
	    		byte[] encoded = java.util.Base64.getEncoder().encode(inFileBytes);
	    	
	    		encodedString = new String(encoded);
	    		
			} catch (IOException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
				LOGGER.error(e);
			}
	        
	        buffer.append(lineSeparator);
	        
	        buffer.append("------=_NextPart_000_"+message.taskNo+"-"+message.subTaskNo);
	        buffer.append(lineSeparator);
			buffer.append("Content-Type: ").append("application/pdf").append(';').append(lineSeparator).append('\t')
					.append("name=\"").append("secretfile.pdf").append('\"').append(lineSeparator)
					.append("Content-Transfer-Encoding: base64").append(lineSeparator)
					.append("Content-Disposition: attachment;").append(lineSeparator).append('\t').append("filename=\"")
					.append("secretfile.pdf").append('\"').append(lineSeparator);
	        
			buffer.append(lineSeparator);
			// buffer.append(encodedString);
			// Base64를 76글자씩 폴딩처리
			Base64Util.foldingBase64(encodedString, buffer);
	        
	        buffer.append(lineSeparator);
	        buffer.append(lineSeparator);
			
	    // 웹에이전트 보안 EXCEL    
		} else if ("Y".equals(message.webagent_secuYn) && message.webagent_sourceUrl != null
				&& "EXCEL".equals(message.webagent_secuAttTyp)) {

        	String msg ="";  // 웹에이전트 URL 내용

        	mainPart2.create(receiver, buffer2, message , toUser.id, toUser); //보안메일 체크 때문에 message 전달
			msg = buffer2.toString();
			//LOGGER.info("msg : "+ msg);
			
			String excelFILE= "./sample/output/"+ message.taskNo +"_"+toUser.id+"_screatfile.xlsx";
	        String encodedString ="";
	        try {
				byte[] inFileBytes = Files.readAllBytes(Paths.get(excelFILE)); 
	    		byte[] encoded = java.util.Base64.getEncoder().encode(inFileBytes);
	    	
	    		encodedString = new String(encoded);
	    		
			} catch (IOException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
				LOGGER.error(e);
			}
        
	        buffer.append(lineSeparator);
	        
	        buffer.append("------=_NextPart_000_"+message.taskNo+"-"+message.subTaskNo);
	        buffer.append(lineSeparator);
			buffer.append("Content-Type: ").append("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
					.append(';').append(lineSeparator).append('\t').append("name=\"").append("secretfile.xlsx")
					.append('\"').append(lineSeparator).append("Content-Transfer-Encoding: base64")
					.append(lineSeparator).append("Content-Disposition: attachment;").append(lineSeparator).append('\t')
					.append("filename=\"").append("secretfile.xlsx").append('\"').append(lineSeparator);
	        
			buffer.append(lineSeparator);
	        buffer.append(encodedString);
		
	        buffer.append(lineSeparator);
	        buffer.append(lineSeparator);
        }

        if (contentPartList != null) {
            for (int i = 0; i < contentPartList.size(); i++) {
                ContentPart contentPart = (ContentPart) contentPartList.get(i);
				buffer.append(lineSeparator).append("--").append(boundary).append(lineSeparator);
                contentPart.create(receiver, buffer, message);
            }
        }
        if (!isAttachFileWrite) {
			buffer.append(lineSeparator).append("--").append(boundary).append("--").append(lineSeparator);
		}
		LOGGER.debug("헤더와 본문 MIME <<<\r\n{}>>>", buffer.toString());

		if (isAttachFileWrite) {
			String targetDomains = ConfigLoader.getProperty("DKIM.target.domain");
			if (targetDomains == null) {
				targetDomains = "";
			}
			targetDomains = targetDomains.trim().toLowerCase();
			// 메일 수신자의 도메인 추출
			String toDomain = toUser.email.split("@")[1].trim().toLowerCase();
			// 메일 발신자의 도메인 추출
			String fromDomain = message.fromEmail.split("@")[1].trim().toLowerCase();
			
			if (targetDomains.indexOf(toDomain) > -1) {
				LOGGER.info("DKIM 적용 대상 도메인 목록 {}, 수신자 도메인: {}", targetDomains, toDomain);
				/**
				 * 2024-02-02 keultae 일반 첨부 메일은 Generator에서 본문에는 본문의 MIME만 저장하고 attach 폴더에 첨부 파일
				 * MIME을 분리해서 저장 MailSender에서 본문 MIME 파일을 읽어서 먼저 전송하고 이어서 첨부 파일 MIME을 읽어서 전송함
				 * 
				 * DKIM-Signature 헤더를 생성하기 위해서는 본문 MIME과 첨부 파일 MIME을 합해서 DKIM-Signature 헤더를 생성하고
				 * Generator와 MailSender 기존 로직을 사용하기 위해서 DKIM-Signature 헤더만 본문 MIME 제일 앞에 추가 해서
				 * 본문 MIME 파일에 저장
				 */
				String headerBody = buffer.toString();
				StringBuffer completeMime = new StringBuffer();
				completeMime.append(headerBody);

				// lineSeparator를 추가하거나 뺴면 DKIM 에러 발생
				for (int afIndex = 0; afIndex < this.attachFileList.size(); afIndex++) {
					FileContent fc = (FileContent) this.attachFileList.get(afIndex);
					completeMime.append("--").append(boundary).append(lineSeparator);
					completeMime.append(fc.getContent());
				}
				completeMime.append(lineSeparator).append(lineSeparator).append("--").append(boundary).append("--")
						.append(lineSeparator);
//				LOGGER.debug("첨부 파일 추가한 일반 MIME<<<\r\n{}>>>", completeMime.toString());
				String dkimMime = SimpleJavaDKIMSign.addDKIMSignature(
						completeMime.toString(), 
//						ConfigLoader.getProperty("DKIM.domain"),
						fromDomain,
						ConfigLoader.getProperty("DKIM.selector"),
						ConfigLoader.getProperty("DKIM.private.key")
					);
//				LOGGER.debug("첨부 파일 있는 DKIM MIME<<<\r\n{}>>>", dkimMime);

				// DKIM-Siguature 헤더만 추출
				String dkimHeader = pickDKIMHeader(dkimMime);
//				LOGGER.debug("DKIM<<<\r\n{}>>>", dkimHeader);

				// buffer 초기화
				buffer = new StringBuffer();
				buffer.append(dkimHeader);
				buffer.append(headerBody);
//				LOGGER.debug("DKIM + 헤더와 본문 MIME<<<\r\n{}>>>", buffer.toString());
        }
		}

        writeContent(contentFile, buffer.toString(), javaCharsetName);
        
        // 웹에이전트 보안 HTML 삭제
		if ("Y".equals(message.webagent_secuYn) && message.webagent_sourceUrl != null
				&& "HTML".equals(message.webagent_secuAttTyp)) {
        	//-- 보안메일 파일 삭제 -------------------------------------------------
            String taskNo = (String) message.keyMap.get("TASK_NO");
        	String secuFile="./sample/output/"+ taskNo +"_"+toUser.id+"_screatfile.html";
    		
        	File secuFilePath = new File(secuFile);
    			if(secuFilePath.exists()) {
    				if(secuFilePath.delete()) {
    					//LOGGER.info("파일삭제 완료");
    				}else {
    					//LOGGER.info("파일삭제 실패");
    				}
    			}else {
    				//LOGGER.info("파일이 존재하지 않습니다.");
    			}
    	     //----------------------------------------------------------------
        }
        
        // 웹에이전트 보안 PDF 파일 삭제
        if("Y".equals(message.webagent_secuYn) && message.webagent_sourceUrl != null && "PDF".equals(message.webagent_secuAttTyp)) {
    	   //-- 보안메일 파일 삭제 -------------------------------------------------
            String taskNo = (String) message.keyMap.get("TASK_NO");
        	//String secuFile="./sample/output/"+ taskNo +"_"+toUser.id+"_screatfile.pdf";
        	String secuFile="/umsadmin/ems/axon-ems/engine/sample/output/"+ taskNo +"_"+toUser.id+"_screatfile.pdf";
    		
        	File secuFilePath = new File(secuFile);
    			if(secuFilePath.exists()) {
    				if(secuFilePath.delete()) {
    					//LOGGER.info("파일삭제 완료");
    				}else {
    					//LOGGER.info("파일삭제 실패");
    					
    				}
    			}else {
    				//LOGGER.info("파일이 존재하지 않습니다.");
    			}
    	     //----------------------------------------------------------------
        }
			
     // 웹에이전트 보안 EXCEL 파일 삭제
		if ("Y".equals(message.webagent_secuYn) && message.webagent_sourceUrl != null
				&& "EXCEL".equals(message.webagent_secuAttTyp)) {
    	   //-- 보안메일 파일 삭제 -------------------------------------------------
            String taskNo = (String) message.keyMap.get("TASK_NO");
        	String secuFile="./sample/output/"+ taskNo +"_"+toUser.id+"_screatfile.xlsx";
    		
        	File secuFilePath = new File(secuFile);
    			if(secuFilePath.exists()) {
    				if(secuFilePath.delete()) {
    					//LOGGER.info("파일삭제 완료");
    				}else {
    					//LOGGER.info("파일삭제 실패");
    				}
    			}else {
    				//LOGGER.info("파일이 존재하지 않습니다.");
    			}
    	     //----------------------------------------------------------------
        }
        
        return address;
    }

	private String pickDKIMHeader(String data) {
		StringBuffer dkimHeader = new StringBuffer();

		try (BufferedReader reader = new BufferedReader(new StringReader(data))) {
			String line = reader.readLine();
			if (line != null) {
				dkimHeader.append(line).append("\r\n");
			}
			while ((line = reader.readLine()) != null && (line.charAt(0) == ' ' || line.charAt(0) == '\t')) {
				dkimHeader.append(line).append("\r\n");
			}
//          LOGGER.debug("dkimHeader.toString(){}END", dkimHeader.toString());
		} catch (IOException e) {
			LOGGER.error("ERR", e);
		}
		return dkimHeader.toString();
	}

	public boolean writeAttachFileList(String unitName) throws Exception {
        if (isAttachFileWrite) {
            return isAttachFileWrite;
        }

        Exception ex = null;
        if (attachFileList != null && attachFileList.size() > 0) {
            UnitAttachFile unitAttachFile = null;
            try {
                unitAttachFile = new UnitAttachFile(unitName, boundary);
                for (int i = 0; i < attachFileList.size(); i++) {
					FileContent fileContent = (FileContent) attachFileList.get(i);
                    unitAttachFile.write(fileContent.getContent());
                }
                isAttachFileWrite = true;
			} catch (Exception ex1) {
				LOGGER.error(ex1);
                ex = ex1;
			} finally {
                unitAttachFile.close();
            }
        }
        if (ex != null) {
            throw ex;
        }
        return isAttachFileWrite;
    }

    private ContentPart createMainPart(String charsetName,
                                       String encCode,
                                       TemplateContent templateContent) {
        Encoder encoder = MimeTable.createEncoder(encCode, charsetName);
        return createContentPart(encoder, templateContent);
    }

    //보안메일 적용	
    private ContentPart2 createMainPart(String charsetName,
	            String encCode,
	            TemplateContent templateContent, TemplateContent2 templateContent2) {
	Encoder encoder = MimeTable.createEncoder(encCode, charsetName);
	return createContentPart(encoder, templateContent, templateContent2);
	}

    
    private ContentPart createContentPart(Encoder encoder,
                                          TemplateContent templateContent) {
        String header = templateContent.getHeader();
        Template template = templateContent.getTemplate();
        return new ContentPart(template, header, encoder);
    }

    //보안메일 적용
    private ContentPart2 createContentPart(Encoder encoder,
	            TemplateContent templateContent, TemplateContent2 templateContent2) {
	String header = templateContent.getHeader();
	Template template = templateContent.getTemplate();
	Template2 template2 = templateContent2.getTemplate2();
	return new ContentPart2(template2, header, encoder);
	}
    
    private String createBoundary(String string) {
        StringBuffer buffer = new StringBuffer(BOUNDARY_PREFIX);
        buffer.append(string);
        return buffer.toString();
    }

    private ArrayList createTemplatePartList(ArrayList list, String charsetName) {
        ArrayList partList = null;
        if (list != null && list.size() > 0) {
            //Encoder encoder = MimeTable.createEncoder(MimeTable.ENC_BASE64,	charsetName);
            //Encoder encoder = MimeTable.createEncoder(MimeTable.ENC_BASE64, charsetName);
            Encoder encoder = MimeTable.createEncoder(MimeTable.encType, charsetName);
            int size = list.size();
            partList = new ArrayList(size);

            for (int i = 0; i < size; i++) {
                TemplateContent templateContent = (TemplateContent) list.get(i);
                ContentPart contentPart = createContentPart(encoder,
                    templateContent);
                partList.add(contentPart);
            }
        }
        return partList;
    }
    
  	/**
  	 * vest 보안메일 readFile
  	 * @param fname
  	 * @param charset
  	 * @return
  	 * @throws IOException
  	 */
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
}
