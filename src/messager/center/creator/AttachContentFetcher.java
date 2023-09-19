/*
 * @(#)AttachFileFetcher.java Copyright 2003 Neocast, Inc. All rights reserved.
 */
package messager.center.creator;

import java.io.*;
import java.sql.*;
import java.util.*;

import messager.center.creator.parse.*;
import messager.center.db.*;
import messager.common.*;
import messager.common.util.*;
import messager.center.config.ConfigLoader;
import synap.next.JFilterUtil;
import synap.next.ParttenCheckUtil;

/**
 * 첨부 파일이 포함 되었을 경우 AttachFile 테이블에서 AttachFile 리스트를 검색하여 FileRequester를 이용해 첨부
 * 파일의 Stream를 얻고 Template 형태의 첨부 파일일 경우 Template 객체를 생성하고 일반 첨부 파일일 경우
 * AttachFile 객체를 생성한다.
 */
class AttachContentFetcher
{
    // AttachFile 테이블 검색 SQL
    private static final String searchSQL = "SELECT ATT_NM, ATT_FL_PATH, ATT_FL_TY, ENCRYPT_YN, "
        + "ENCRYPT_KEY, ENCRYPT_TY " + "FROM NEO_ATTACH WHERE TASK_NO = ?"; // chk cskim 070904

    private ArrayList list;

    private ArrayList fileList;

    private ArrayList templateList;

    private String charsetCode;

//	private String javaCharset;

    private MailHeaderEncoder headerEncoder;
    
    private static String persoanl_pass="";
    private static String persoanl_yn ="";
    private static String persoanl_attach_pass ="";

    private Message message;
    
    public AttachContentFetcher(JdbcConnection connection,
                                int taskNo,
                                int size,
                                String charsetCode,
                                String headerEncCode,
                                Message message)
        throws FetchException {
        this.charsetCode = charsetCode;
        //this.javaCharset = CharsetTable.javaCharsetName(charsetCode);
        list = searchFile(connection, taskNo, size, message);
        headerEncoder = new MailHeaderEncoder(charsetCode, headerEncCode);
        this.message = message;
    }

    /**
     * 첨부 파일 리스트를 NEO_ATTACH 테이블에서 검색하여 ArrayList에 저장하여 리턴한다.
     *
     * @param connection
     *            Work DB Connection
     * @param taskNo
     *            캠페인 업무의 업무 번호
     * @return ArrayList NEO_ATTACH 테이블에서 검색된 결과를 저장한 Entry객체의 리스트
     * @exception CreatorException
     *                NEO_ATTACH 테이블 검색중 에러 발생 할 경우
     */
    private ArrayList searchFile(JdbcConnection connection, int taskNo, int size, Message message)
        throws FetchException {
        PreparedStatement pstmt = null;
        ResultSet rset = null;
        FetchException exception = null;
        ArrayList list = null;
        try {
            pstmt = connection.prepareStatement(searchSQL);
            pstmt.setInt(1, taskNo);
            rset = pstmt.executeQuery();
            
            String allEncAttahStr ="";
            
            while (rset.next()) {
                if (list == null) {
                    list = new ArrayList();
                }
                Entry entry = new Entry();

                // 첨부 파일 명
                entry.name = connection.fromDB(rset.getString(1));

                //요청 file 경로
                entry.path = connection.fromDB(rset.getString(2));

                // file type (1일 경우 일반 AttachFile, 2일 경우 템플릿형태)
                entry.type = rset.getString(3);
                String encrypt = rset.getString(4); //암호화 여부 (0: 암호화 하지 않음, 1:
                // 암호화)
                
                //---------------------------------------------------------------------------
           		//첨부파일 개인정보 체크 start
           		//---------------------------------------------------------------------------
                persoanl_yn = ConfigLoader.getString("PERSONAL_YN", "N");
                persoanl_pass = ConfigLoader.getString("PERSONAL_PASS", "");
                persoanl_attach_pass = ConfigLoader.getString("PERSONAL_ATTACH_PATH", "");
                
                
                String attachfull_path =persoanl_attach_pass+entry.path;
                //String attachfull_path ="/App/mailapp/AXON_UMS/front/upload/attach/한글테스트.pdf";
                
                //config체크 방식
            	//if("Y".equals(persoanl_yn)) {
            		
        		//DB체크 방식
                if("Y".equals(message.attach_file_chk_yn)) {            		
            		JFilterUtil jFilterUtil = new JFilterUtil(attachfull_path);
                	
                	String[] passedNumbers = persoanl_pass.split(",");  // center.properties에 PERSONAL_PASS 값을 가져옴
                    
                    List<String> passedList = new ArrayList<>();
                    for (int z = 0; z < passedNumbers.length; z++) {
                    	passedList.add(passedNumbers[z]);
                    	//System.out.println("phons : "+passedNumbers[i]);
                    }
            		
                    //휴대폰번호 체크
            		boolean CellCK = false;
                    //JFilterUtil jFilterUtil = new JFilterUtil(srcFile);
            		CellCK = jFilterUtil.hasCellPhoneNumber(passedList);
            		//System.out.println("첨부파일 CellCK : " + CellCK);
                   
            		//전화번호 체크		
            		boolean TellCK = false;
            		//TellCK = jFilterUtil.hasTelePhoneNumber(passedList);
            		//System.out.println("첨부파일 TellCK : " + TellCK);
            		
                    //주민번호 체크
            		boolean PersonalCK = false;
            		PersonalCK = jFilterUtil.hasPersonalId();
            		//System.out.println("첨부파일 PersonalCK : " + PersonalCK);
            		
            		//이메일 체크
             		boolean EmailCK = false;
             		EmailCK = jFilterUtil.hasEamil(passedList);
             		//System.out.println("첨부파일 EmailCK : " + EmailCK);             		
            		
        			if((CellCK) || (TellCK) || (PersonalCK) || (EmailCK)) { 
        				allEncAttahStr =  "첨부보안오류";
        				
//        				break;
        			}
            	}
 
            	//---------------------------------------------------------------------------
                
                if (encrypt != null && encrypt.length() > 0
                    && encrypt.charAt(0) == '1') {
                    entry.isEncrypt = true;
                    entry.encryptKey = rset.getString(5);
                    entry.encryptType = rset.getString(6);
                }
                list.add(entry);
            }
            if (list == null || list.size() != size) {
                int count = 0;

                if (list != null) {
                    count = list.size();
                }
                String detail = "[" + Integer.toString(taskNo)
                    + "] NEO_ATTACH size unmatched: " + size + ":" + count;
                exception = new FetchException(detail,
                                               ErrorCode.ATTACH_FILE_SEARCH_FAIL);
            }
            else if("첨부보안오류".equals(allEncAttahStr)) {
        		exception = new FetchException("첨부보안오류",
                        ErrorCode.PERSONAL_ATTACH_ERROR);	
        		
        	}
            
        }
        catch (Exception ex) {
            String detail = "[" + Integer.toString(taskNo) + "] Exception: "
                + ex.getMessage();
            exception = new FetchException(ex.getMessage(),
                                           ErrorCode.ATTACH_FILE_SEARCH_FAIL);
        }
        finally {
            if (rset != null) {
                try {
                    rset.close();
                }
                catch (SQLException ex) {
                }
            }
            if (pstmt != null) {
                try {
                    pstmt.close();
                }
                catch (SQLException ex) {
                }
            }
        }
        if (exception != null) {
            throw exception;
        }
        return list;
    }

    /**
     * FileRequester로 AttachFile에 대한 Stream를 얻고 템플릿 형태일 경우 TemplateParser를 이용해
     * Template 객체를 생성하고 일반 형태일 경우 AttachFile객체를 생성한다.
     */
    public void fetch(FileRequester fileRequester, TemplateParser parser)
        throws FetchException {
        FetchException ex = null;
        String path = null;
        try {
            while (list != null && list.size() > 0) {
                char fileType = 1;
                Entry entry = (Entry) list.remove(0);
                path = entry.path;
                String type = entry.type; //1: 일반 첨부 2: 머지첨부
                String name = entry.name;
                if (type != null && type.length() > 0) {
                    fileType = type.charAt(0);
                }
                if (path != null) {
                    InputStream stream = fileRequester.request(path);
                    if (stream != null) {
                        switch (fileType) {
                            case '2': //템플릿 타입(머지 첨부)
                                Template template = parse(stream, parser);
                                TemplateContent templateContent = new TemplateContent(
                                    template, name, headerEncoder);
                                if (templateList == null) {
                                    templateList = new ArrayList();
                                }
                                templateList.add(templateContent);
                                break;
                            default:
                                FileContent fileContent = createFileContent(
                                    stream, name);
                                if (fileList == null) {
                                    fileList = new ArrayList();
                                }
                                fileList.add(fileContent);
                                break;
                        }
                    }
                }
            }
        }
        catch (Exception exception) {
            String detail = path + " " + exception.getMessage();
            throw new FetchException(detail, ErrorCode.ATTACH_FILE_FETCH_FAIL);
        }
    }

    public ArrayList getTemplateList() {
        return templateList;
    }

    public ArrayList getFileContentList() {
        return fileList;
    }

    /**
     * 템플릿 type의 AttachFile를 TemplateParser로 분석하여 MergeElement list를 리턴한다.
     *
     * @param stream
     *            InputStream
     * @param javaCharsetName
     *            컨텐츠의 Charset
     * @param templateParser
     *            템플릿을 분석
     * @return Template 머지 리스트를 리스트로 저장한 객체
     * @exception CreatorException
     */
    private Template parse(InputStream stream, TemplateParser parser)
        throws Exception {
        Reader reader = null;
        Template aTemplate = null;
        Exception exception = null;
        try {
            //reader = new InputStreamReader(stream, javaCharset);
            reader = new InputStreamReader(stream, charsetCode);
            aTemplate = parser.parse(reader);
        }
        catch (Exception ex) {
            exception = ex;
        }
        finally {
            if (reader != null) {
                try {
                    reader.close();
                }
                catch (IOException ex) {
                }
                reader = null;
            }
        }
        if (exception != null) {
            throw exception;
        }

        return aTemplate;
    }

    /**
     * AttachFile를 읽어서 AttachFile 객체를 생성한다.
     *
     * @param stream
     *            AttachFile을 읽을 InputStream
     * @param name
     *            AttachFile의 파일 Name
     * @param mailHeaderEncoder
     *            AttachFile Part의 Content헤더를 구성할때 인코딩을 실행한다.
     * @return AttachFile 객체
     */
    private FileContent createFileContent(InputStream stream, String name)
        throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
        byte[] buf = new byte[1024];
        int rc;
        while ( (rc = stream.read(buf, 0, 1024)) != -1) {
            baos.write(buf, 0, rc);
        }
        stream.close();

        stream = null;
        return new FileContent(baos.toByteArray(), name, headerEncoder);
    }

    /**
     * NEO_ATTACH Table에서 검색된 결과를 저장
     */
    class Entry
    {

        /** 파일 이름 */
        String name;

        /** 파일 경로 */
        String path;

        /** 파일 타입 1: 일반 첨부, 2: 머지 첨부 */
        String type;

        /** 암호화 여부 false: 암호화 하지 않음, true: 암호화 */
        boolean isEncrypt;

        /** 암호키 */
        String encryptKey;

        /** 암호화 방식 */
        String encryptType;
    }
}
