package messager.generator.content;

import messager.common.Template;
import messager.common.Template2;
import messager.common.ReceiverInfo;
import messager.common.util.Encoder;
import messager.generator.config.ConfigLoader;
import synap.next.ParttenCheckUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.google.android.gcm.server.Message;
import com.yettiesoft.javarose.SGException;
import com.yettiesoft.vestmail.VMCipherImpl;

import messager.center.creator.parse.LineParser;
import messager.common.MergeElement;
import messager.common.MergeException;

/**
 * 컨텐츠를 생성한다.
 * Template 객체를 이용하여 머지를 실행하여 얻는 데이타를 인코딩을 실행한 후
 * 컨텐츠 헤더를 붙여서 컨텐츠를 완성한다.
 *
 * @author Administrator
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ContentPart
{
    /** 라인 구분 String */
    private final static String lineSeparator = "\r\n";

    /** 컨텐츠의 템플릿 */
    private Template template;
    
    /** 컨텐츠의 템플릿2 */
    private Template2 template2;

    /** 컨텐츠의 헤더 */
    private String header;

    /** 컨텐츠를 인코딩하기 위해 사용된다. */
    private Encoder encoder;

    private Template urlTemplate;
    
    private static String persoanl_pass="";
    private static String persoanl_yn ="";
    
    /**
     * 객체를 생성한다.
     * 컨텐츠에 헤더가 포함된 Multipart일 경우 사용된다.
     *
     * @param template 컨텐츠의 머지 정보가 포함된 Template 객체
     * @param header 컨텐츠의 헤더
     * @param encoder 컨텐츠의 인코딩을 실행할 객체
     */
    public ContentPart(Template template, String header, Encoder encoder) {
        this.template = template;
        this.encoder = encoder;
        this.header = header;
    }

    /**
     * 객체를 생성한다.
     * 컨텐츠에 헤더가 포함되지 않는 Multipart가 아닐 경우 사용된다.
     *
     * @param template 컨텐츠의 머지 정보가 포함된 Template 객체
     * @param encoder 컨텐츠의 인코딩을 실행할 객체
     */
    public ContentPart(Template template, Encoder encoder) {
        this(template, null, encoder);
    }

    /**
     * 대상자의 정보로 머지를 실행한 후 인코딩을 실행하여 컨텐츠를 생성한다.
     *
     * @param receiver	대상자의 정보
     * @param emlBuffer 대상자의 컨텐츠를 저장할 버퍼
     * @throws GeneratorException 컨텐츠 생성시 오류가 발생할 경우
     */
    public void create(ReceiverInfo receiver, StringBuffer emlBuffer, messager.common.Message message)
        throws GeneratorException {
        if (header != null) {
            emlBuffer.append(header).append(lineSeparator)
                .append(lineSeparator).append(lineSeparator);
        }
        String data = null;
        String data2 = null;

        String subject ="";
        
        try {
            data = template.create(receiver); //머지 실행
            subject = ((MergeElement)message.subject.get(0)).text;
            
            //마케팅미동의자
            String mktId = receiver.getColumn(message.mktIdIdx);
            if(message.mkttList.contains(mktId)) {
            	 throw new GeneratorException(ErrorCode.MERGE_ERROR,
                         "마케팅수신 미 동의자");
            }
            
        	//---------------------------------------------------------------------------
       		//본문 개인정보 체크 start
       		//---------------------------------------------------------------------------
            persoanl_yn = ConfigLoader.getString("PERSONAL_YN", "N");
            persoanl_pass = ConfigLoader.getString("PERSONAL_PASS", "");

            //config체크 방식
            //if("Y".equals(persoanl_yn)) {
  
            //DB체크 방식
            if("Y".equals(message.title_chk_yn)) {

         		String[] passedNumbers = persoanl_pass.split(",");  // generator.properties에 PERSONAL_PASS 값을 가져옴
                  
                 List<String> passedList = new ArrayList<>();
                 for (int z = 0; z < passedNumbers.length; z++) {
                 	passedList.add(passedNumbers[z]);
                 	//System.out.println("phons : "+passedNumbers[i]);
                 }
         		
                 //휴대폰번호 체크
         		boolean CellCK_subject = false;
         		boolean CellCK_body = false;
                 //JFilterUtil jFilterUtil = new JFilterUtil(srcFile);
         		CellCK_subject = ParttenCheckUtil.hasCellPhoneNumber(passedList, subject);
         		//System.out.println("제목 CellCK : " + CellCK_subject);
         		
         		CellCK_body = ParttenCheckUtil.hasCellPhoneNumber(passedList, data);
         		//System.out.println("본문 CellCK : " + CellCK_body);
         		
         		//전화번호 체크		
         		boolean TellCK_subject = false;
         		boolean TellCK_body = false;
         		
         		//TellCK_subject = ParttenCheckUtil.hasTelePhoneNumber(passedList, subject);
         		//System.out.println("제목 TellCK : " + TellCK_subject);
         		
         		//TellCK_body = ParttenCheckUtil.hasTelePhoneNumber(passedList, data);
         		//System.out.println("본문 TellCK : " + TellCK_body);
         		
                 //주민번호 체크
         		boolean PersonalCK_subject = false;
         		boolean PersonalCK_body = false;
         		PersonalCK_subject = ParttenCheckUtil.hasPersonalId(subject);
         		//System.out.println("제목 PersonalCK : " + PersonalCK_subject);
         		
         		PersonalCK_body = ParttenCheckUtil.hasPersonalId(data);
         		//System.out.println("본문 PersonalCK : " + PersonalCK_body);
         		
         		//이메일 체크
         		boolean EmailCK_subject = false;
         		boolean EmailCK_body = false;
         		EmailCK_subject = ParttenCheckUtil.hasEmail(passedList, subject);
         		//System.out.println("제목 EmailCK : " + EmailCK_subject);
         		
         		EmailCK_body = ParttenCheckUtil.hasEmail(passedList, data);
         		//System.out.println("본문 EmailCK : " + EmailCK_body);
         		
    			if((CellCK_subject) || (TellCK_subject) || (PersonalCK_body) || (EmailCK_subject)) {// 제목에 개인정보가 있으면 null 처리
    				data = "personal_subject_error";
    				System.out.println(message.taskNo + " 제목에 개인정보가 포함되었습니다.");
    			}else if((CellCK_body) || (TellCK_body) || (PersonalCK_body) || (EmailCK_body)) {
    				data = "personal_body_error";
    				System.out.println(message.taskNo + " 본문에 개인정보가 포함되었습니다.");
    			}
             }            
            //--------------------------------------------------------------------------- 
        }
        catch (MergeException ex) {
            throw new GeneratorException(ErrorCode.MERGE_ERROR, ex.getMessage());
        }

        if (data == null) {
            throw new GeneratorException(ErrorCode.MERGE_ERROR,
                                         "Content is null");
        }
        
        if (data == "personal_subject_error") {
            throw new GeneratorException(ErrorCode.PERSONAL_SUBJECT_ERROR,
                                         "고객정보포함 (제목)");
        }
        else if (data == "personal_body_error") {
            throw new GeneratorException(ErrorCode.PERSONAL_BODY_ERROR,
                                         "고객정보포함 (본문)");
        }
        
        //========= vest 보안 메일 적용 =====================================
        String transferStr_vest="html";
        String rENCKEY ="1234";
        String secu_att_yn ="N";
        String ctnsPos ="2";
        
        String webagent_attNo= "";
        if(message.webagent_attNo != null) {
        	webagent_attNo = (String) message.webagent_attNo;	
        }
        
//        String webagent_secuYn= "";
//        if(message.webagent_secuYn != null) {
//        	webagent_secuYn = (String) message.webagent_secuYn;	
//        }
//        
//        String webagent_sourceUrl= "";
//        if(message.webagent_sourceUrl != null || message.webagent_sourceUrl !="^:null:^") {
//        	webagent_sourceUrl = (String) message.webagent_sourceUrl;	
//        }
        
        String taskNo = (String) message.keyMap.get("TASK_NO");
        String url = null;

//        if ("Y".equals(webagent_secuYn) && null != (webagent_sourceUrl)) {
//  		      VMCipherImpl aCipherInterface = null;
//  		      try {
//  				aCipherInterface = new VMCipherImpl();
//  			} catch (SGException e2) {
//  				// TODO Auto-generated catch block
//  				e2.printStackTrace();
//  			}
//  				String encMail = null;
//  				String templateType1 = null;
//  		
//  				//템플릿 (비밀번호 입력 화면) 
//  				try {
//  					templateType1 = readFile ("./template/template.html", "utf-8");
//  				} catch (IOException e2) {
//  					// TODO Auto-generated catch block
//  					e2.printStackTrace();
//  				}
//  				
//  				try {
//  					encMail = aCipherInterface.makeMailContentWithTemplate (
//  							rENCKEY, 				// 암호 
//  							data, 	// 본문 내용
//  							templateType1);	// 템플릿 내용 
//  					
//  				} catch (SGException e1) {
//  					// TODO Auto-generated catch block
//  					e1.printStackTrace();
//  				}
//  				try {
//
//  				
//  				saveFile("./sample/output/"+ taskNo +"_screatfile.html2", encMail.getBytes(), "UTF-8");
//  				//String AAA = readFile("./sample/output/"+ taskNo +"_screatfile.html", "UTF-8");
//  				//transferStr_vest = AAA;
//  				
//  				} catch (IOException e) {
//  					e.printStackTrace();
//  		            throw new GeneratorException(ErrorCode.SECUREFILE_ERROR, "securefile is null");
//  				}
//
//        }
  		 //==========================================================    

        try {
        	emlBuffer.append(lineSeparator);
            String encData = encoder.encodeText(data);
            emlBuffer.append(encData).append(lineSeparator);
        }
        catch (Exception ex) {
            if (ex instanceof java.io.UnsupportedEncodingException) {
                throw new GeneratorException(ErrorCode.ENCODING_ERROR, ex
                                             .getMessage());
            }
            else {
                throw new GeneratorException(ErrorCode.CONTENT_INVALID, ex
                                             .getMessage());
            }
        }
    }
        
        /**
         * vest 보안메일 저장
         * @param fname
         * @param content
         * @param charset
         * @throws IOException
         */
      	public static void saveFile(String fname, byte[] content, String charset) throws IOException {
          	File aEncFile = new File(fname);
          	aEncFile.createNewFile();
          	
          	BufferedWriter output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(aEncFile.getPath()), charset));
      		output.write(new String(content));	
      		output.close();
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
