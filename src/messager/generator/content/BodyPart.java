package messager.generator.content;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

import messager.common.*;
import messager.common.util.*;

/**
 * 첨부 파일이 포함되지 않은 단일 컨텐츠를 갖는 메일의 컨텐츠를 생성한다.
 *
 * @author Administrator TODO To change the template for this generated type
 *         comment go to Window - Preferences - Java - Code Style - Code
 *         Templates
 */
public class BodyPart
    extends Part
{
    /** Mail 헤더를 생성하기 위한 객체 */
    private MailHeader mailHeader;

    /** 컨텐츠 부분을 표현하는 객체 */
    private ContentPart contentPart;

    /** 컨텐츠 부분을 표현하는 객체2 */
    private ContentPart2 contentPart2;
    
    /** java에서 표현되는 charset */
    private String javaCharsetName;

    /** 컨텐츠의 인코딩을 실행하는 객체 */
    private Encoder encoder;

    private String boundary;
    
    /**
     * BodyPart 객체를 생성한다.
     *
     * @param message
     * @param templateContent
     */
    public BodyPart(Message message, TemplateContent templateContent) {
        super(message);
        this.message = message;
        String header = templateContent.getHeader();
        Template template = templateContent.getTemplate();
        //코드표에서 java의 charset을 얻는다.
        //코드표대신 TASK의 CHARSET코드의 CHARSET이름을 직접 사용한다.
        //javaCharsetName = CharsetTable.javaCharsetName(message.charsetCode);
        javaCharsetName = message.charsetCode;
        encoder = MimeTable.createEncoder(message.bodyEncodingCode, javaCharsetName);

        // 웹에이전트 보안 HTML
        if("Y".equals(message.webagent_secuYn) && message.webagent_sourceUrl != null && "HTML".equals(message.webagent_secuAttTyp)) {
        	mailHeader = new MailHeader(message, header, 1);
        
    	// 웹에이전트 보안 PDF
        }else if("Y".equals(message.webagent_secuYn) && message.webagent_sourceUrl != null && "PDF".equals(message.webagent_secuAttTyp)) {
        	mailHeader = new MailHeader(message, header, 1);
        
    	// 웹에이전트 보안 EXCEL
        }else if("Y".equals(message.webagent_secuYn) && message.webagent_sourceUrl != null && "EXCEL".equals(message.webagent_secuAttTyp)) {
        	mailHeader = new MailHeader(message, header, 1);
        
        }else {
        	mailHeader = new MailHeader(message, header, 0);
        }
        
        contentPart = new ContentPart(template, encoder);
        //대상자 정보 (ReceiverInfo객체)에서 email address를 얻어내기위한 Column Number */
    }

    /**
     * BodyPart2 객체를 생성한다.
     *
     * @param message
     * @param templateContent
     */
    public BodyPart(Message message, TemplateContent templateContent, TemplateContent2 templateContent2) {
        super(message);
        this.message = message;
        String header = templateContent.getHeader();
        Template template = templateContent.getTemplate();
        Template2 template2 = templateContent2.getTemplate2();
        //코드표에서 java의 charset을 얻는다.
        //코드표대신 TASK의 CHARSET코드의 CHARSET이름을 직접 사용한다.
        //javaCharsetName = CharsetTable.javaCharsetName(message.charsetCode);
        javaCharsetName = message.charsetCode;
        encoder = MimeTable.createEncoder(message.bodyEncodingCode, javaCharsetName);

        boundary = createBoundary(message.messageID);
        
        // 웹에이전트 보안 HTML
        if("Y".equals(message.webagent_secuYn) && message.webagent_sourceUrl != null && "HTML".equals(message.webagent_secuAttTyp)) {
        	mailHeader = new MailHeader(message, boundary, 1);
        
    	// 웹에이전트 보안 PDF
        }else if("Y".equals(message.webagent_secuYn) && message.webagent_sourceUrl != null && "PDF".equals(message.webagent_secuAttTyp)) {
        	mailHeader = new MailHeader(message, boundary, 1);
        
    	// 웹에이전트 보안 EXCEL
        }else if("Y".equals(message.webagent_secuYn) && message.webagent_sourceUrl != null && "EXCEL".equals(message.webagent_secuAttTyp)) {
        	mailHeader = new MailHeader(message, boundary, 1);
        
        }else {
        	mailHeader = new MailHeader(message, header, 0);
        }
        
        contentPart = new ContentPart(template, encoder);
        contentPart2 = new ContentPart2(template2, encoder);
        //대상자 정보 (ReceiverInfo객체)에서 email address를 얻어내기위한 Column Number */
    }
    
    /**
     * 대상자의 메일 컨텐츠를 생성후 파일에 저장한다.
     *
     * @param receiver
     *            대상자의 정보를 지닌 ReceiverInfo객체
     * @param contentFile
     *            대상자의 컨텐츠를 저장할 File 객체
     * @param toUser
     *            대상자의 발송 정보(userID, userEmail)를 저장할 객체
     * @exception GeneratorException
     *                대상자의 email address가 잘못 되었거나 <br>
     *                컨테츠 생성시 에러가 발생할 경우 <br>
     *                생성된 컨텐츠를 저장하지 못하는 경우
     */
    public Address createContent(ReceiverInfo receiver,
                                 File contentFile,
                                 SendTo toUser, Message message)
        throws GeneratorException {

        Address address = new Address(toUser.email);
        StringBuffer buffer = new StringBuffer();
        StringBuffer buffer2 = new StringBuffer();
        //헤더를 생성한다.
        mailHeader.create(receiver, buffer, toUser);
        //buffer.append(lineSeparator);
        
        String boundary;
        boundary = createBoundary(message.messageID);
        
        // 웹에이전트 보안 HTML
        if("Y".equals(message.webagent_secuYn) && message.webagent_sourceUrl != null && "HTML".equals(message.webagent_secuAttTyp)) {
            buffer.append(lineSeparator);
            buffer.append(lineSeparator).append("--").append(boundary).append(
                lineSeparator);
            buffer.append("Content-Type: text/html; charset=\"euc-kr\"");
            buffer.append(lineSeparator);
            //buffer.append("Content-Transfer-Encoding: 8bit");
            buffer.append("Content-Transfer-Encoding: ");buffer.append(message.bodyEncodingCode);
            buffer.append(lineSeparator);
        
        // 웹에이전트 보안 PDF
        }else if("Y".equals(message.webagent_secuYn) && message.webagent_sourceUrl != null && "PDF".equals(message.webagent_secuAttTyp)) {
            buffer.append(lineSeparator);
            buffer.append(lineSeparator).append("--").append(boundary).append(
                lineSeparator);
            buffer.append("Content-Type: text/html; charset=\"euc-kr\"");
            buffer.append(lineSeparator);
            //buffer.append("Content-Transfer-Encoding: 8bit");
            buffer.append("Content-Transfer-Encoding: ");buffer.append(message.bodyEncodingCode);
            buffer.append(lineSeparator);
        
        // 웹에이전트 보안 EXCEL
        }else if("Y".equals(message.webagent_secuYn) && message.webagent_sourceUrl != null && "EXCEL".equals(message.webagent_secuAttTyp)) {
            buffer.append(lineSeparator);
            buffer.append(lineSeparator).append("--").append(boundary).append(
                lineSeparator);
            buffer.append("Content-Type: text/html; charset=\"euc-kr\"");
            buffer.append(lineSeparator);
            //buffer.append("Content-Transfer-Encoding: 8bit");
            buffer.append("Content-Transfer-Encoding: ");buffer.append(message.bodyEncodingCode);
            buffer.append(lineSeparator);
        }

        //컨텐츠를 생성한다.
        contentPart.create(receiver, buffer, message); 
        
        // 웹에이전트 보안 HTML
        if("Y".equals(message.webagent_secuYn) && message.webagent_sourceUrl != null && "HTML".equals(message.webagent_secuAttTyp)) {
        	String msg= "";	

			contentPart2.create(receiver, buffer2, message , toUser.id, toUser); //보안메일 체크 때문에 message 전달
			msg = buffer2.toString();
			//System.out.println("msg : "+ msg);
			
	        String AAA= "";
	        try {
				AAA = readFile("./sample/output/"+ message.taskNo +"_"+toUser.id+"_screatfile.html", "UTF-8");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

        String encodedString = Base64.getEncoder().encodeToString(AAA.getBytes());
        //System.out.println("encodedString : "+ encodedString);
        
        buffer.append(lineSeparator);
        
        buffer.append("------=_NextPart_000_"+message.taskNo+"-"+message.subTaskNo);
        buffer.append(lineSeparator);
		buffer.append("Content-Type: ").append("text/html").append(';').append(
				lineSeparator).append('\t').append("name=\"").append(
				"secretfile.html").append('\"').append(lineSeparator).append(
				"Content-Transfer-Encoding: base64").append(lineSeparator)
				.append("Content-Disposition: attachment;").append(
						lineSeparator).append('\t').append("filename=\"")
				.append("secretfile.html").append('\"').append(lineSeparator);
        
		buffer.append(lineSeparator);
        buffer.append(encodedString);
        
        // 웹에이전트 보안 PDF
        }else if("Y".equals(message.webagent_secuYn) && message.webagent_sourceUrl != null && "PDF".equals(message.webagent_secuAttTyp)) {
        	String msg= "";	

			contentPart2.create(receiver, buffer2, message , toUser.id, toUser); //보안메일 체크 때문에 message 전달
			msg = buffer2.toString();
			//System.out.println("msg : "+ msg);
			
	        String pdfFILE= "./sample/output/"+ message.taskNo +"_"+toUser.id+"_screatfile.pdf";
	        String encodedString ="";
	        try {
				byte[] inFileBytes = Files.readAllBytes(Paths.get(pdfFILE)); 
	    		byte[] encoded = java.util.Base64.getEncoder().encode(inFileBytes);
	    	
	    		encodedString = new String(encoded);
	    		
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        
        buffer.append(lineSeparator);
        
        buffer.append("------=_NextPart_000_"+message.taskNo+"-"+message.subTaskNo);
        buffer.append(lineSeparator);
		buffer.append("Content-Type: ").append("application/pdf").append(';').append(
				lineSeparator).append('\t').append("name=\"").append(
				"secretfile.pdf").append('\"').append(lineSeparator).append(
				"Content-Transfer-Encoding: base64").append(lineSeparator)
				.append("Content-Disposition: attachment;").append(
						lineSeparator).append('\t').append("filename=\"")
				.append("secretfile.pdf").append('\"').append(lineSeparator);
        
		buffer.append(lineSeparator);
        buffer.append(encodedString);
        
        // 웹에이전트 보안 EXCEL
        }else if("Y".equals(message.webagent_secuYn) && message.webagent_sourceUrl != null && "EXCEL".equals(message.webagent_secuAttTyp)) {
        	String msg= "";	

			contentPart2.create(receiver, buffer2, message , toUser.id, toUser); //보안메일 체크 때문에 message 전달
			msg = buffer2.toString();
			//System.out.println("msg : "+ msg);
			
			String excelFILE= "./sample/output/"+ message.taskNo +"_"+toUser.id+"_screatfile.xlsx";
	        String encodedString ="";
	        try {
				byte[] inFileBytes = Files.readAllBytes(Paths.get(excelFILE)); 
	    		byte[] encoded = java.util.Base64.getEncoder().encode(inFileBytes);
	    	
	    		encodedString = new String(encoded);
	    		
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        
        buffer.append(lineSeparator);
        
        buffer.append("------=_NextPart_000_"+message.taskNo+"-"+message.subTaskNo);
        buffer.append(lineSeparator);
		buffer.append("Content-Type: ").append("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet").append(';').append(
				lineSeparator).append('\t').append("name=\"").append(
				"secretfile.xlsx").append('\"').append(lineSeparator).append(
				"Content-Transfer-Encoding: base64").append(lineSeparator)
				.append("Content-Disposition: attachment;").append(
						lineSeparator).append('\t').append("filename=\"")
				.append("secretfile.xlsx").append('\"').append(lineSeparator);
        
		buffer.append(lineSeparator);
        buffer.append(encodedString);
        
        }
		
        //파일에 write
        /*
         * contentFile 			: .\repository\transfer\content\313-1^1\0.mcf
         * buffer.toString() 	:  Reply-To: AXon@enders.co.kr
									From: =?euc-kr?B?QURNSU4=?= <AXon@enders.co.kr>
									To: =?euc-kr?B?sei8+LTr?= <hun1110@enders.co.kr>
									Subject: =?euc-kr?B?dGVzdA==?=
									Date: Fri, 20 Aug 2021 14:14:43 +0900
									MIME-Version: 1.0
									Content-Type: text/html; charset="euc-kr"
									Content-Transfer-Encoding: 8bit
									X-MESSAGE-ID: L0D0Q0CYLS6GWDyOUi72Q0H3L36161
									X-USER-ID: aBS0aED61
									X-USER-NM: 9shH8Plb9/1H
									X-Mailer: Postware@MasterPromotion_v3.0
									
									test<p>&nbsp;</p><!--NEO__RESPONSE__START--><img src='http://127.0.0.1:8080/resp/response.jsp?202109011354&&000&&test1&&313&&1&&1&&ADMIN&&003&&1&&$:TARGET_GRP_TY:$' width=0 height=0 border=0><!--NEO__RESPONSE__END-->
          javaCharsetName 		: euc-kr	 						
         */
        
        writeContent(contentFile, buffer.toString(), javaCharsetName);
        
        // 웹에이전트 보안 HTML 파일 삭제
        if("Y".equals(message.webagent_secuYn) && message.webagent_sourceUrl != null && "HTML".equals(message.webagent_secuAttTyp)) {
    	   //-- 보안메일 파일 삭제 -------------------------------------------------
            String taskNo = (String) message.keyMap.get("TASK_NO");
        	String secuFile="./sample/output/"+ taskNo +"_"+toUser.id+"_screatfile.html";
    		
        	File secuFilePath = new File(secuFile);
    			if(secuFilePath.exists()) {
    				if(secuFilePath.delete()) {
    					//System.out.println("파일삭제 완료");
    				}else {
    					//System.out.println("파일삭제 실패");
    				}
    			}else {
    				//System.out.println("파일이 존재하지 않습니다.");
    			}
    	     //----------------------------------------------------------------
        }
        
     // 웹에이전트 보안 PDF 파일 삭제
        if("Y".equals(message.webagent_secuYn) && message.webagent_sourceUrl != null && "PDF".equals(message.webagent_secuAttTyp)) {
    	   //-- 보안메일 파일 삭제 -------------------------------------------------
            String taskNo = (String) message.keyMap.get("TASK_NO");
        	String secuFile="./sample/output/"+ taskNo +"_"+toUser.id+"_screatfile.pdf";
    		
        	File secuFilePath = new File(secuFile);
    			if(secuFilePath.exists()) {
    				if(secuFilePath.delete()) {
    					//System.out.println("파일삭제 완료");
    				}else {
    					//System.out.println("파일삭제 실패");
    				}
    			}else {
    				//System.out.println("파일이 존재하지 않습니다.");
    			}
    	     //----------------------------------------------------------------
        }
     
     // 웹에이전트 보안 EXCEL 파일 삭제
        if("Y".equals(message.webagent_secuYn) && message.webagent_sourceUrl != null && "EXCEL".equals(message.webagent_secuAttTyp)) {
    	   //-- 보안메일 파일 삭제 -------------------------------------------------
            String taskNo = (String) message.keyMap.get("TASK_NO");
        	String secuFile="./sample/output/"+ taskNo +"_"+toUser.id+"_screatfile.xlsx";
    		
        	File secuFilePath = new File(secuFile);
    			if(secuFilePath.exists()) {
    				if(secuFilePath.delete()) {
    					//System.out.println("파일삭제 완료");
    				}else {
    					//System.out.println("파일삭제 실패");
    				}
    			}else {
    				//System.out.println("파일이 존재하지 않습니다.");
    			}
    	     //----------------------------------------------------------------
        }
        
        return address;
    }
    
    
    private String createBoundary(String string) {
        StringBuffer buffer = new StringBuffer("----=_NextPart_000_");
        buffer.append(string);
        return buffer.toString();
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
