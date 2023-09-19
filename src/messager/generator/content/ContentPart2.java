package messager.generator.content;

import messager.common.Template;
import messager.common.Template2;
import messager.common.ReceiverInfo;
import messager.common.SendTo;
import messager.common.util.Encoder;
import messager.generator.config.ConfigLoader;
import synap.next.ParttenCheckUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.enders.excelconverter.HtmlToExcelConverter;
import com.google.android.gcm.server.Message;
import com.yettiesoft.javarose.SGException;
import com.yettiesoft.vestmail.VMCipherImpl;

import messager.center.creator.parse.LineParser;
import messager.common.MergeException;

import com.pdf.convert.HtmlToPdf;

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
public class ContentPart2
{
    /** 라인 구분 String */
    private final static String lineSeparator = "\r\n";

    /** 컨텐츠의 템플릿 */
    private Template template;
    
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
    public ContentPart2(Template template, String header, Encoder encoder) {
        this.template = template;
        this.encoder = encoder;
        this.header = header;
    }
    
    /**
     * 객체를 생성한다.
     * 컨텐츠에 헤더가 포함된 Multipart일 경우 사용된다.
     *
     * @param template2 컨텐츠의 머지 정보가 포함된 Template 객체
     * @param header 컨텐츠의 헤더
     * @param encoder 컨텐츠의 인코딩을 실행할 객체
     */
    public ContentPart2(Template2 template2, String header, Encoder encoder) {
        this.template2 = template2;
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
    public ContentPart2(Template template2, Encoder encoder) {
        this(template2, null, encoder);
    }
    
    /**
     * 객체를 생성한다.
     * 컨텐츠에 헤더가 포함되지 않는 Multipart가 아닐 경우 사용된다.
     *
     * @param template2 컨텐츠의 머지 정보가 포함된 Template 객체
     * @param encoder 컨텐츠의 인코딩을 실행할 객체
     */
    public ContentPart2(Template2 template2, Encoder encoder) {
        this(template2, null, encoder);
    }

    /**
     * 대상자의 정보로 머지를 실행한 후 인코딩을 실행하여 컨텐츠를 생성한다.
     *
     * @param receiver	대상자의 정보
     * @param emlBuffer 대상자의 컨텐츠를 저장할 버퍼
     * @throws GeneratorException 컨텐츠 생성시 오류가 발생할 경우
     */
    public void create(ReceiverInfo receiver, StringBuffer emlBuffer, messager.common.Message message, String userID, SendTo toUser)
        throws GeneratorException {
        if (header != null) {
            emlBuffer.append(header).append(lineSeparator)
                .append(lineSeparator);
        }
        String data = null;
        String data2 = null;
        
        try {
            data = template2.create(receiver); //머지 실행

        }
        catch (MergeException ex) {
            throw new GeneratorException(ErrorCode.MERGE_ERROR, ex.getMessage());
        }

        if (data == null) {
            throw new GeneratorException(ErrorCode.MERGE_ERROR,
                                         "Content is null");
        }
        
        //========= vest 보안 메일 적용 =====================================
        String transferStr_vest="html";
        //String rENCKEY ="1234";
        String rENCKEY = toUser.encKey;
        
        //보안메일 암호키값이 없으면 에러 처리
        if(rENCKEY =="NoENCKEY") {
        	  throw new GeneratorException(ErrorCode.SECUREFILE_ERROR, "보안메일생성오류 (ENCKEY 누락)");
        }
        
        String secu_att_yn ="N";
        String ctnsPos ="2";
        String webagent_attNo = (String) message.webagent_attNo;
        String webagent_secuYn = (String) message.webagent_secuYn;
        String webagent_sourceUrl = (String) message.webagent_sourceUrl;
        String webagent_secuAttTyp = (String) message.webagent_secuAttTyp;
        
        String taskNo = (String) message.keyMap.get("TASK_NO");
        String url = null;
        
        // 웹에이전트 보안 HTML
        if ("Y".equals(webagent_secuYn) && null != (webagent_sourceUrl) && "HTML".equals(webagent_secuAttTyp)) {
  		      VMCipherImpl aCipherInterface = null;
  		      try {
  				aCipherInterface = new VMCipherImpl();
  			} catch (SGException e2) {
  				// TODO Auto-generated catch block
  				e2.printStackTrace();
  			}
  				String encMail = null;
  				String templateType1 = null;
  		
  				//템플릿 (비밀번호 입력 화면) 
  				try {
  					templateType1 = readFile ("./template/template.html", "utf-8");
  				} catch (IOException e2) {
  					// TODO Auto-generated catch block
  					e2.printStackTrace();
  				}
  				
  				try {
  					encMail = aCipherInterface.makeMailContentWithTemplate (
  							rENCKEY, 				// 암호 
  							data, 	// 본문 내용
  							templateType1);	// 템플릿 내용 
  					
  				} catch (SGException e1) {
  					// TODO Auto-generated catch block
  					e1.printStackTrace();
  				}
  				try {

  				saveFile("./sample/output/"+ taskNo +"_"+userID+"_screatfile.html", encMail.getBytes(), "UTF-8");
  				//String AAA = readFile("./sample/output/"+ taskNo +"_screatfile.html", "UTF-8");
  				//transferStr_vest = AAA;

  				} catch (IOException e) {
  					e.printStackTrace();
		            throw new GeneratorException(ErrorCode.SECUREFILE_ERROR, "securefile is null");
  				}

  		// 웹에이전트 보안 PDF
        }else if ("Y".equals(webagent_secuYn) && null != (webagent_sourceUrl) && "PDF".equals(webagent_secuAttTyp)) {
        	
        	//HtmlToPdf convert = new HtmlToPdf("./conf/chhtmltopdf.properties");
        	HtmlToPdf convert = new HtmlToPdf("./conf/generator.properties");
    		
    		convert.setOrientation("Portrait");			//Portrait:세로  , Landscape:가로
    		convert.setMarginTop(10);					//페이지 프레임 상단 10 여백
    		convert.setMarginBottom(10);				//페이지 프레임 하단 10 여백
    		convert.setMarginLeft(10);					//페이지 프레임 왼쪽 10 여백
    		convert.setMarginRight(10);					//페이지 프레임 오른쪽 10 여백
        	
        	String pdfFile = "./sample/output/"+ taskNo +"_"+userID+"_screatfile.pdf";	
        	try {
        		convert.htmlContentToEncryptPdf(data, pdfFile, rENCKEY);
        	}catch (Exception e) {
				e.printStackTrace();
	            throw new GeneratorException(ErrorCode.SECUREFILE_ERROR, "securefile is null");
			}

      // 웹에이전트 보안 EXCEL
      }else if ("Y".equals(webagent_secuYn) && null != (webagent_sourceUrl) && "EXCEL".equals(webagent_secuAttTyp)) {
	     
          String fileName = taskNo +"_"+userID+"_screatfile";
          try {
        	  HtmlToExcelConverter excelConverter = new HtmlToExcelConverter(data);
              excelConverter.getWorkBook(fileName, "./sample/output/", rENCKEY);
          }catch (Exception e) {
			// TODO: handle exception
        	  System.out.println("");
        	  e.printStackTrace();
        	  throw new GeneratorException(ErrorCode.SECUREFILE_ERROR, "excel 변환 오류");
          }

      	}

        try {
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
        
    	//EXCEL 변환 테스트 =================================================================================
    	public static void main(String args[]) throws Exception  {
    		
 		   String srcFile = "./sample/input/KJUNI_balancestate.html";
 	        
 	        String htmlData = "";
 	         
 	        try
 	        {
 	            htmlData = getHtmlString(srcFile);
 	        }
 	        catch (IOException e1)
 	        {
 	            // TODO Auto-generated catch block
 	            e1.printStackTrace();
 	        }
 	        
 	        HtmlToExcelConverter excelConverter = new HtmlToExcelConverter(htmlData);
 	        
 	        Date now = new Date();
 	        String currDate = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(now);
 	        String fileName = "SampleExcel_" + currDate;
 	        String enc ="123456";
 	        
 	        excelConverter.getWorkBook(fileName, "./sample/output", enc);
 	        System.out.println("#### Success ####");
 	        System.out.println("파일위치 :  ./sample/output"+fileName);
 	        System.out.println("비밀번호 : "+enc);
 	      
 	   
 	    }

 	    public static String getHtmlString(String filePath) throws IOException 
 	    {
 	        String rst = "";
 	        BufferedReader reader = null;
 	        
 	        try
 	        {
 	            String str;
 	            reader = new BufferedReader(new FileReader(filePath));            
 	            while ((str = reader.readLine()) != null) {
 	                rst += str;
 	            }
 	        }
 	        catch (IOException e)
 	        {
 	            // TODO Auto-generated catch block
 	            e.printStackTrace();
 	        }        
 	        finally {
 	            reader.close();
 	        }
 	        return rst;
 	    }
    	//EXCEL 변환 테스트 =================================================================================
}
