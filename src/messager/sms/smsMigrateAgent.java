package messager.sms;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.UnsupportedEncodingException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;
import java.util.Vector;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import messager.aes.*;
import messager.center.config.ConfigLoader;
import messager.center.creator.FileRequester;
import messager.center.db.JdbcConnection;
import messager.common.util.CSVTokenizer;

/**
 * <p>Title: @Master promotion 3.0</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2007 i-Spirit</p>
 * <p>Company: (주)아이스피릿</p>
 * @author 이명재
 * @version 1.0
 */

public class smsMigrateAgent
    extends Thread
{
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm", Locale.US);
    private SimpleDateFormat sdfCmid = new SimpleDateFormat("yyyyMMdd", Locale.US);
    private FileRequester fr = null;
    
    String pattern="###";
    DecimalFormat myFormatter = new DecimalFormat(pattern);
//	private String token;
    
    
    public smsMigrateAgent() {
    	System.out.println("smsMigrateAgent Start...");
    }

    public void run() {
        while (true) {
            migrate();

            try {
                sleep(60 * 500);
            }
            catch (Exception e) {
            }
        }
    }

    private String replace(String str, Hashtable replaceList) {
        String temp;

        Enumeration e = replaceList.keys();

        while (e.hasMoreElements()) {
            temp = (String) e.nextElement();
            str = replace(str, temp, (String) replaceList.get(temp));
        }
        return str;
    }

    private String replace(String str, String oldWord, String newWord) {
        int index = 0, oldLength = oldWord.length(), newLength = newWord.length();

        while (true) {
            if ( (index = str.indexOf(oldWord)) != -1) {
                str = str.substring(0, index) + newWord + str.substring(index + oldLength);
            }
            else {
                break;
            }
        }
        return str;
    }

    private Properties getDBInfo(String dbconno, Connection con) {
        Properties prop = null;
        String selectQuery = "SELECT A.DB_DRIVER, A.DB_URL, A.LOGIN_ID, A.LOGIN_PWD, B.CD_NM " +
            "FROM NEO_DBCONN A, NEO_CD B " +
            "WHERE B.CD_GRP = 'C022' AND B.UILANG = '000' AND A.DB_CHAR_SET = B.CD AND A.DB_CONN_NO = ?";
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = con.prepareStatement(selectQuery);
            ps.setInt(1, Integer.parseInt(dbconno));
            rs = ps.executeQuery();
            if (rs.next()) {
                prop = new Properties();
                prop.setProperty("jdbc.driver.name", rs.getString("DB_DRIVER"));
                System.out.print("777777");
                prop.setProperty("jdbc.url", rs.getString("DB_URL"));
                prop.setProperty("user", rs.getString("LOGIN_ID"));
                prop.setProperty("password", rs.getString("LOGIN_PWD"));
            }
        }
        catch (Exception e) {
            prop = null;
            System.err.println(e.getMessage());
            e.printStackTrace(System.err);
        }
        finally {
            if (rs != null) {
                try {
                    rs.close();
                    rs = null;
                }
                catch (Exception e) {}
            }
            if (ps != null) {
                try {
                    ps.close();
                    ps = null;
                }
                catch (Exception e) {}
            }
        }

        return prop;
    }

    private int getMID_MSSQL(Connection con) {
        String sql = "{call GETSEQ(?, ?)}";
        CallableStatement cs = null;
        int mid = -1;

        try {
            cs = con.prepareCall(sql);
            cs.setString(1, "SMSSEQ");
            cs.registerOutParameter(2, java.sql.Types.BIGINT);
            cs.execute();
            mid = cs.getInt(2);
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace(System.err);
        }
        finally {
            try {
                if (cs != null) {
                    cs.close();
                    cs = null;
                }
            }
            catch (Exception e) {}
        }
        return mid;
    }

    private void updateStatus(String msgid, String keygen, Connection con) {
        PreparedStatement ps = null;
        String updateQuery = "UPDATE NEO_SMS SET STATUS = 2 WHERE MSGID = ? AND KEYGEN = ?";

        try {
            ps = con.prepareStatement(updateQuery);
            ps.setString(1, msgid);
            ps.setString(2, keygen);
            ps.executeUpdate();
//            System.out.println("<<<====== update end =====>>>");
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace(System.err);
        }
        finally {
            if (ps != null) {
                try {
                    ps.close();
                    ps = null;
                }
                catch (Exception e) {}
            }
        }
    }

    private void doMmsWork(Hashtable ht,Hashtable htSMS, Connection con, String gubun){
//    	System.out.println("&&&&&&&&&&  mms insert  &&&&&&&&&&");
        JdbcConnection jc = null;
        Connection smsCon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Statement stmt = null;
        
        Properties dbProps = ConfigLoader.getDBProperties();
        Properties dbProps2 = new Properties();

        String mmsSeq = null;
        
        // SMS 데이터베이스에 연결하기 위한 정보를 추출.
        dbProps2.setProperty("jdbc.driver.name", dbProps.getProperty("sms.jdbc.driver.name"));
        dbProps2.setProperty("jdbc.url", dbProps.getProperty("sms.jdbc.url"));
        dbProps2.setProperty("user", dbProps.getProperty("sms.db.user"));
        dbProps2.setProperty("password", dbProps.getProperty("sms.db.password"));

        //String insertQuery = "INSERT INTO UMS_DATA(MSGID, DUMMY2, DEST_MIN, CALL_BACK, SMS_MSG, COLLEGE_NO, O_CODE, P_CODE, " +
        //    "STATUS, CUST_NM) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 100, ?)";
        String insertQuery = "";
//        System.out.println("<<======== MMS Gubun =========>>> ");
//        System.out.println("MMS Gubun : " + gubun);
        	
        	insertQuery = "	SET NOCOUNT ON;INSERT INTO neo_mmsqueue   " +                                                                
							        	"          (file_cnt        " +                                                              
							        	"          ,build_yn        " +                                                              
							        	"          ,mms_body        " +                                                              
							        	"          ,mms_subject     " +                                                              
							        	"          ,file_type1      " +                                                              
							        	"          ,file_type2      " +                                                              
							        	"          ,file_type3      " +                                                              
							        	"          ,file_type4      " +                                                              
							        	"          ,file_type5      " +                                                              
							        	"          ,file_name1      " +                                                              
							        	"          ,file_name2      " +                                                              
							        	"          ,file_name3      " +                                                              
							        	"          ,file_name4      " +                                                              
							        	"          ,file_name5      " +                                                              
							        	"          ,service_dep1    " +                                                              
							        	"          ,service_dep2    " +                                                              
							        	"          ,service_dep3    " +                                                              
							        	"          ,service_dep4    " +                                                              
							        	"          ,service_dep5    " +                                                              
							        	"          ,skn_file_name)  " +                                                              
//							        	"    VALUES(?,'',?,?,?,'','','','',?,'','','','','','','','','','All') select @@identity as " + "seq" ;   
        	"    VALUES(?,null,?,?,?,null,null,null,null,?,null,null,null,null,'ALL',null,null,null,null,null) select @@identity as " + "seq" ;   
        	
            try {
            	 jc = JdbcConnection.getInstance(dbProps2);
                 smsCon = jc.getConnection(); 
                 
                 String createType = (String) ht.get("CREATE_TY");
                 String cmid;
                 String data = "", contents = "", subJect = "";
	             
	             
	             //System.out.println("==>>> contents mms ===>> " + contents);
//	             System.out.println("==>>> contents mms file path ===>> " + ht.get("FILE_PATH"));
//	             System.out.println("==>>> contents mms SEG_FL_PATH ===>> " + ht.get("SEG_FL_PATH"));
                     
                String fileCnt = null;
                if(gubun.equals("M")){
                	fileCnt = "2";
                }else{
                	fileCnt = "0";
                }
//                System.out.println("mms insert");
//                System.out.println("mms insert + fileCnt : " + fileCnt);
                int count = 1;
                
                if ("003".equals(createType)) { // 파일그룹
                    // merge

                    // 주소록파일을 로컬에서 직접 읽는다. (원래는 FileRequester를 통해 가져와야 하는데..)
                    String segPath = (String) ht.get("SEG_FL_PATH");
                    String filePath = ConfigLoader.getProperty("upload.dir") + segPath;
                    
//                    System.out.println("==>> 003  filePath ==>> " + filePath);
                    //System.out.println("address filePath = " + filePath);
                    LineNumberReader lnr = null;
                    // 이소라 추가
                    BufferedReader br = null;
                    try {
                        if(filePath.toLowerCase().endsWith(".xls")) {
                        	System.out.println("xls 파일..");

                        } else {
                        	System.out.println("mms cvs,txt 파일..");
                        	
                        	// 이소라 - 한글 깨짐 으로 인해 변환 추가
                        	br = new BufferedReader( new InputStreamReader (new FileInputStream(filePath), "UTF-8"));
                        	boolean first = true;
                            //boolean first = true;
                            String[] key = null;
                            String phone = "";
                            int phoneIndex = -1;

                            while ( (data = br.readLine()) != null) {
//                            	
//    System.out.println(" ===>> 00000한글 변환이 되는가 ???" + data);
                                if (data.trim().length() == 0) {
                                    continue;
                                }

                                CSVTokenizer ct = new CSVTokenizer(data);
                                int index = 0, nameIndex = -1;

//                                System.out.println("===>>>  data ct  <<==== " + ct);
//                                System.out.println("===>>>  data ct toString <<==== " + ct.toString());
                                
                                // 첫번째 라인은 무조건 필드명으로 인식시킨다.(동작사양)
                                if (first) {
                                    key = new String[ct.countTokens()];
//                                    System.out.println("==>> key ==>> " + key);
                                    while (ct.hasMoreTokens()) {
                                        String token = ct.nextToken();
//                                        System.out.println(">>>> token >>>>>>>>>>>>>>" + token);
                                        if (token.equalsIgnoreCase("phone")) {
                                        	
                                            phoneIndex = index; // 전화번호 필드 인덱스를 가져온다.
//                                            System.out.println("phoneIndex : "+ phoneIndex);
                                            
                                        }
//                                        else if(token.equalsIgnoreCase("name")) {
                                        else if(token.equalsIgnoreCase("name")) {
                                            nameIndex = index;
//                                            System.out.println("nameIndex : " + nameIndex);
                                        }
                                        key[index++] = token;
                                    }
//                                    System.out.println("===>>> token END >>> ");
                                    first = false;
                                    ct = null;
                                    continue;
                                }
                                
                                Hashtable htMerge = new Hashtable();
                                while (ct.hasMoreTokens()) {
                                	htMerge.put("$:" + key[index++] + ":$", ct.nextToken());
                                }
//                                contents = replace( (String) ht.get("SMS_MESSAGE"), htMerge1);
                                contents = replace( (String) ht.get("TRAN_MSG"), htMerge);
                                subJect = replace( (String) ht.get("SMS_NAME"), htMerge);
                                
//                                System.out.println("==>>> contents ===>> " + contents);
                                
                                phone = (String) htMerge.get("$:" + key[phoneIndex] + ":$");
                                String name = (nameIndex == -1) ? " " : (String) htMerge.get("$:" + key[nameIndex] + ":$");
                                
                                if (phone == null || phone.trim().length() == 0) {
                                	htMerge = null;
                                    ct = null;
                                    continue;
                                }
                                
                                phone = replace(phone, ")", "-");
                                phone = replace(phone, " ", "");
                                phone = replace(phone, "(", "");

                                cmid = (String)sdf.format(new java.util.Date()) + String.valueOf(myFormatter.format(count++));
                                
                                
//                                ps.clearParameters();
                                ps = smsCon.prepareStatement(insertQuery);
                                ps.setString(1, fileCnt);
                                ps.setString(2, contents);
//                                ps.setString(3, (String) ht.get("SMS_NAME"));
                                ps.setString(3, subJect);
                                if(gubun.equals("M")){
                                	ps.setString(4, (String) ht.get("TYPE1"));
                                	ps.setString(5, (String) ht.get("FILE_PATH"));
                                }else{
                                	ps.setString(4, null);
                                	ps.setString(5, null);
                                }
//                                                            
                                
//                                ps.executeUpdate();
    							rs = ps.executeQuery();

                                htMerge = null;
                                ct = null;
                                
                                while(rs.next()){	
                                	mmsSeq = String.valueOf(rs.getInt("seq"));
                                }
                                
//                                System.out.println("mms phone ==>> " + phone);
                                
                                htSMS.put("phone", phone);
                                doWork(htSMS, con, gubun, mmsSeq);                                
                            }
                        }
                    }catch (Exception e) {
                        System.err.println(e.getMessage());
                        e.printStackTrace(System.err);
                    }
                    finally {
                    }
                }else{
                	Hashtable htMerge = new Hashtable();	             
   	             	contents = replace( (String) ht.get("TRAN_MSG"), htMerge);
                	
                    ps = smsCon.prepareStatement(insertQuery);
                    ps.setString(1, fileCnt);
                    ps.setString(2, contents);
                    ps.setString(3, (String) ht.get("SMS_NAME"));
                    if(gubun.equals("M")){
                    	ps.setString(4, (String) ht.get("TYPE1"));
                    	ps.setString(5, (String) ht.get("FILE_PATH"));
                    }else{
                    	ps.setString(4, null);
                    	ps.setString(5, null);
                    }
//                    System.out.println("===>>> mms insertQuery >>>" +  insertQuery);
//                    System.out.println("===>>> mms contents >>>" +  contents);
//                    System.out.println("===>>> mms SMS_NAME >>>" +  (String)ht.get("SMS_NAME"));
//                    System.out.println("===>>> mms TYPE1 >>>" +  (String)ht.get("TYPE1"));
//                    System.out.println("===>>> mms FILE_PATH >>>" +  (String)ht.get("FILE_PATH"));
                    
                    rs = ps.executeQuery();
//                    System.out.println(" ===>>> mms insert 후 <<=== ");
                    while(rs.next()){	
                    	mmsSeq = String.valueOf(rs.getInt("seq"));
//                    	System.out.println("mmsSeq===>>>  " + mmsSeq);
//                    	System.out.println("===>>> mms insert <<<====");
                    }
                    
                    //htSMS.put("CREATE_TY", "999");
                    
                    doWork(htSMS, con, gubun, mmsSeq);
                }                
            }
            catch (Exception e) {
                System.err.println(e.getMessage());
                e.printStackTrace(System.err);
            }
            finally {
                if (ps != null) {
                    try {
                        ps.close();
                        ps = null;
                    }
                    catch (Exception e) {}
                }
            }
        	
    }
    private void doWork(Hashtable ht, Connection con, String gubun, String mmsSeq)throws UnsupportedEncodingException {
        JdbcConnection jc = null;
        Connection smsCon = null;
        PreparedStatement ps = null;

        Properties dbProps = ConfigLoader.getDBProperties();
        Properties dbProps2 = new Properties();

        // SMS 데이터베이스에 연결하기 위한 정보를 추출.
        dbProps2.setProperty("jdbc.driver.name", dbProps.getProperty("sms.jdbc.driver.name"));
        dbProps2.setProperty("jdbc.url", dbProps.getProperty("sms.jdbc.url"));
        dbProps2.setProperty("user", dbProps.getProperty("sms.db.user"));
        dbProps2.setProperty("password", dbProps.getProperty("sms.db.password"));

        //String insertQuery = "INSERT INTO UMS_DATA(MSGID, DUMMY2, DEST_MIN, CALL_BACK, SMS_MSG, COLLEGE_NO, O_CODE, P_CODE, " +
        //    "STATUS, CUST_NM) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 100, ?)";
        String insertQuery = "";
        if (gubun.equals("S")){       // SMS MSG_TYPE = 0
        	insertQuery = "INSERT INTO neo_smsqueue(TRAN_REFKEY,TRAN_ID,TRAN_PHONE,TRAN_CALLBACK,TRAN_STATUS,TRAN_DATE,TRAN_RSLTDATE,TRAN_REPORTDATE,TRAN_RSLT,TRAN_NET,TRAN_MSG,TRAN_ETC1,TRAN_ETC2,TRAN_ETC3,TRAN_ETC4,TRAN_TYPE,DEPT_CODE,SVC_CODE)" +
        						  "VALUES(?,'',?,?,'1',getdate(),'','','','',?,?,'','S','',4,?,?)";
        }else if(gubun.equals("M")){ // MMS MSG_TYPE = 5
        	insertQuery = "INSERT INTO neo_smsqueue(TRAN_REFKEY,TRAN_ID,TRAN_PHONE,TRAN_CALLBACK,TRAN_STATUS,TRAN_DATE,TRAN_RSLTDATE,TRAN_REPORTDATE,TRAN_RSLT,TRAN_NET,TRAN_MSG,TRAN_ETC1,TRAN_ETC2,TRAN_ETC3,TRAN_ETC4,TRAN_TYPE,DEPT_CODE,SVC_CODE)" + 
        						  "VALUES(?,'',?,?,'1',getdate(),'','','','',?,?,'','M'," + mmsSeq + ",6,?,?)";
//        	insertQuery = "INSERT INTO neo_smsqueue(TRAN_REFKEY,TRAN_ID,TRAN_PHONE,TRAN_CALLBACK,TRAN_STATUS,TRAN_DATE,TRAN_RSLTDATE,TRAN_REPORTDATE,TRAN_RSLT,TRAN_NET,TRAN_MSG,TRAN_ETC1,TRAN_ETC2,TRAN_ETC3,TRAN_ETC4,TRAN_TYPE)" + 
//        	"VALUES(?,'',?,?,'1',getdate(),'','','','',?,?,'',''," + mmsSeq + ",6)";
        }else if(gubun.equals("L")){ // MMS MSG_TYPE = 5
        	insertQuery = "INSERT INTO neo_smsqueue(TRAN_REFKEY,TRAN_ID,TRAN_PHONE,TRAN_CALLBACK,TRAN_STATUS,TRAN_DATE,TRAN_RSLTDATE,TRAN_REPORTDATE,TRAN_RSLT,TRAN_NET,TRAN_MSG,TRAN_ETC1,TRAN_ETC2,TRAN_ETC3,TRAN_ETC4,TRAN_TYPE,DEPT_CODE,SVC_CODE)" + 
    						"VALUES(?,'',?,?,'1',getdate(),'','','','',?,?,'','L'," + mmsSeq + ",6,?,?)";
        }
//        else if(gubun.equals("V")){  // VMS_MSG_TYPE = 3
//        	insertQuery = "INSERT INTO neo_smsqueue(TRAN_REFKEY,TRAN_ID,TRAN_PHONE,TRAN_CALLBACK,TRAN_STATUS,TRAN_DATE,TRAN_RSLTDATE,TRAN_REPORTDATE,TRAN_RSLT,TRAN_NET,TRAN_MSG,TRAN_ETC1,TRAN_ETC2,TRAN_ETC3,TRAN_ETC4,TRAN_TYPE)" + 
//        						  "VALUES(?,'',?,?,'1',getdate(),'','','','',?,?','','','',6)";
//        }
        
        try {
            // SMS 데이터베이스에 연결한다.
            jc = JdbcConnection.getInstance(dbProps2);
            smsCon = jc.getConnection();
            ps = smsCon.prepareStatement(insertQuery);
            ResultSet rs = null;
            
            String createType = (String) ht.get("CREATE_TY");
            
            String data = "", contents = "", msgid = (String) ht.get("MSGID"), subJect="";
            String cmid;
            //String uFilePath = (String)ht.get("FILE_PATH");
            //String phoneType = null;
            ArrayList phoneType = new ArrayList();
            int pIndex = 0;
            
//            System.out.println("msgid ===>>> " + msgid);
//            System.out.println("createType ===>>> " + createType);
            int count = 1;
            if ("003".equals(createType)) { // 파일그룹
                // 주소록파일을 로컬에서 직접 읽는다. (원래는 FileRequester를 통해 가져와야 하는데..)
                String segPath = (String) ht.get("SEG_FL_PATH");
                String filePath = ConfigLoader.getProperty("upload.dir") + segPath;
                
                //System.out.println("address filePath = " + filePath);
                LineNumberReader lnr = null;
                // 이소라 추가
                BufferedReader br = null;
                try {
                    if(filePath.toLowerCase().endsWith(".xls")) {
                    	System.out.println("xls 파일..");
                        Workbook myWorkbook = Workbook.getWorkbook(new File(filePath));
                        Sheet mySheet = myWorkbook.getSheet(0);

                        int cols = mySheet.getColumns();
                        int rows = mySheet.getRows(), index, phoneIndex = -1, nameIndex = -1;
                        String[] key = new String[cols];

                        for(index = 0; index < cols; index++) {
                            Cell myCell = mySheet.getCell(index, 0);
                            key[index] = myCell.getContents();
                            if(key[index].equalsIgnoreCase("phone")) {
                                phoneIndex = index;
                            }
                            else if(key[index].equalsIgnoreCase("name")) {
                                nameIndex = index;
                            }
                        }

                        for(index = 1; index < rows; index++) {
                            Hashtable htMerge = new Hashtable();
                            for(int cnt = 0; cnt < cols; cnt++) {
                                Cell myCell = mySheet.getCell(cnt, index);
                                htMerge.put("$:" + key[cnt] + ":$", myCell.getContents());
                            }
                            contents = replace( (String) ht.get("SMS_MESSAGE"), htMerge);
                            subJect = replace( (String) ht.get("SMS_NAME"), htMerge);

                            String phone = (String) htMerge.get("$:" + key[phoneIndex] + ":$");
                            String name = (nameIndex == -1) ? " " : (String) htMerge.get("$:" + key[nameIndex] + ":$");
                            if (phone == null || phone.trim().length() == 0) {
                                htMerge = null;
                                continue;
                            }
                            phone = replace(phone, ")", "-");
                            phone = replace(phone, " ", "");
                            phone = replace(phone, "(", "");
                         
                            try {
                            	System.out.println("==>> XLS <<==");
                            	cmid = (String)sdf.format(new java.util.Date()) + String.valueOf(myFormatter.format(count++));
//                            	System.out.println("cmid = "+cmid + "     dest_phone : " + phone);
                                ps.clearParameters();
                                ps.setString(1, cmid);
                                ps.setString(2, phone);
//                                ps.setString(3, contents);
                                ps.setString(3, (String) ht.get("SEND_TELNO"));
                                if(gubun.equals("M")){
                                    ps.setString(4,(String)ht.get("FILE_PATH"));
//                                    ps.setString(5,(String)ht.get("SMS_NAME"));
                                    ps.setString(5,subJect);
                                }else if(gubun.equals("V")){
                                	ps.setString(5, name);
                                }
                                ps.setString(6, (String)ht.get("DEPT_CODE"));
                                ps.setString(7, (String)ht.get("SVC_CODE"));
//                                ps.setString(1, cmid);
//                                ps.setString(2, phone);
//                                ps.setString(3, contents);
//                                ps.setString(4, (String) ht.get("SEND_TELNO"));
//                                if(gubun.equals("M")){
//                                	ps.setString(5,(String)ht.get("FILE_PATH"));
//                                	ps.setString(6,(String)ht.get("SMS_NAME"));
//                                }else if(gubun.equals("V")){
//                                	ps.setString(5, name);
//                                }
                                ps.executeUpdate();
                            }
                            catch(Exception e) {
                                System.err.println(e.getMessage());
                                e.printStackTrace(System.err);
                            }

                            htMerge = null;
                        }
                    }
                    else {
                    	System.out.println("cvs,txt 파일..");
                    	// 이소라 - 한글 깨짐 으로 인해 변환 추가
                    	br = new BufferedReader( new InputStreamReader (new FileInputStream(filePath), "UTF-8"));
                        
                        boolean first = true;
                        String[] key = null;
                        String phone = "";
                        String tmpphone = "";
                        int phoneIndex = -1;

                        if(!gubun.equals("S")){
                       		ps.setString(1, (String)ht.get("KEYGEN"));
	                          ps.setString(2, (String)ht.get("phone"));
	                          ps.setString(3, (String)ht.get("SEND_TELNO"));
	                          ps.setString(4, "");
	                          ps.setString(5, "");
	                          
	                          ps.setString(6, (String)ht.get("DEPT_CODE"));
	                          ps.setString(7, (String)ht.get("CAMPUS_NO"));
	                      	
	                          ps.executeUpdate();
//	                          htMerge = null;
//	                          ct = null;
                        }else{
                        
                        while ( (data = br.readLine()) != null) {
                            if (data.trim().length() == 0) {
                                continue;
                            }

                            CSVTokenizer ct = new CSVTokenizer(data);
                            int index = 0, nameIndex = -1;

                            // 첫번째 라인은 무조건 필드명으로 인식시킨다.(동작사양)
                            if (first) {
                                key = new String[ct.countTokens()];
                                while (ct.hasMoreTokens()) {
                                    String token = ct.nextToken();
                                    if (token.equalsIgnoreCase("phone")) {
                                    	
                                        phoneIndex = index; // 전화번호 필드 인덱스를 가져온다.
                                        
                                    }
                                    else if(token.equalsIgnoreCase("name")) {
                                        nameIndex = index;
                                    }
                                    key[index++] = token;
                                }
                                first = false;
                                ct = null;
                                continue;
                            }
                            
                            Hashtable htMerge = new Hashtable();
                            while (ct.hasMoreTokens()) {
                                htMerge.put("$:" + key[index++] + ":$", ct.nextToken());
                            }
                            contents = replace( (String) ht.get("SMS_MESSAGE"), htMerge);
                        	subJect = replace( (String) ht.get("SMS_NAME"), htMerge);
                            
                            phone = (String) htMerge.get("$:" + key[phoneIndex] + ":$");
                            tmpphone = (String) htMerge.get("$:" + key[phoneIndex] + ":$");
                            String name = (nameIndex == -1) ? " " : (String) htMerge.get("$:" + key[nameIndex] + ":$");
                            
                            if (phone == null || phone.trim().length() == 0) {
                                htMerge = null;
                                ct = null;
                                continue;
                            }
                            phone = replace(phone, ")", "-");
                            phone = replace(phone, " ", "");
                            phone = replace(phone, "(", "");

                            cmid = (String)sdf.format(new java.util.Date()) + String.valueOf(myFormatter.format(count++));

                            ps.clearParameters();
//                        	if(gubun.equals("S")){
                            	ps.setString(1, (String)ht.get("KEYGEN"));
                                ps.setString(2, phone);
                                ps.setString(3, (String)ht.get("SEND_TELNO"));
//                                    ps.setString(4, (String)ht.get("TRAN_MSG"));
                                ps.setString(4, contents);
                                ps.setString(5, (String)ht.get("FILE_PATH"));
//                                    ps.setString(5, (String)ht.get("FILE_PATH"));
//                                    ps.setString(6, (String)ht.get("SEG_FL_PATH"));
                                
                                ps.setString(6, (String)ht.get("DEPT_CODE"));
                                ps.setString(7, (String)ht.get("CAMPUS_NO"));	
                                
                                ps.executeUpdate();
                                htMerge = null;
                                ct = null; 
//                            }else{
                            	
                            	
//                            	System.out.println("sms 저장 (mms or lms)" + gubun+gubun+gubun);
//                            	System.out.println("파일 사이즈 ==>> " + phoneType.size());
//
//                            	if(phoneType.size()> 0){
//                            		for(int v=0; v < phoneType.size(); v++){
//                            			System.out.println("1111111111 ===>>  " + phoneType.get(v));
//                            			System.out.println("2222222222 ===>>  " + replace((String)ht.get("phone"), ")", "-"));
//                            			System.out.println("3333333333 ===>>  " + phone);
//                            			
//                              		  if(phoneType.get(v) != ht.get("phone")){
////                              		  if(phoneType.get(v) != tmpphone){
//                              			  System.out.println("같은 값이면 들어오면 안됨");
//                                            ps.setString(1, (String)ht.get("KEYGEN"));
//                                            ps.setString(2, phone);
//                                            ps.setString(3, (String)ht.get("SEND_TELNO"));
//                                            ps.setString(4, "");
//                                            ps.setString(5, "");
//                                            
//                                            ps.setString(6, (String)ht.get("DEPT_CODE"));
//                                            ps.setString(7, (String)ht.get("CAMPUS_NO"));
//                                        	
//                                            ps.executeUpdate();
//                                            htMerge = null;
//                                            ct = null; 
//                                            
//                                            //pIndex++;
//                                           // phoneType = replace((String)ht.get("phone"), ")", "-");
////                                            phoneType.add(replace((String)ht.get("phone"), ")", "-"));
//                                            
//                                  	  }  
//                              	  }                            		
//                            		
//                            	}else{
//                            		ps.setString(1, (String)ht.get("KEYGEN"));
//                                    ps.setString(2, phone);
//                                    ps.setString(3, (String)ht.get("SEND_TELNO"));
//                                    ps.setString(4, "");
//                                    ps.setString(5, "");
//                                    
//                                    ps.setString(6, (String)ht.get("DEPT_CODE"));
//                                    ps.setString(7, (String)ht.get("CAMPUS_NO"));
//                                	
//                                    ps.executeUpdate();
//                                    htMerge = null;
//                                    ct = null;
//                                    System.out.println("size 000000  ==>> " + replace((String)ht.get("phone"), ")", "-"));
//                                    
//                                    phoneType.add(replace((String)ht.get("phone"), ")", "-"));
//                            	
//                              }
                            }
                        }
                        
                    }
                }
                catch (Exception e) {
                    System.err.println(e.getMessage());
                    e.printStackTrace(System.err);
                }
                finally {
                }
            }else if ("999".equals(createType)) {
                String keygen = (String) ht.get("KEYGEN");
                Vector vt = getPhoneList(msgid, keygen);
                if (vt.size() > 0) {
                    Iterator it = vt.iterator();
                    while (it.hasNext()) {
                        String phone = (String) it.next();
                        if (phone == null || phone.trim().length() == 0) {
                            continue;
                        }
                        phone = replace(phone, ")", "-");
                        phone = replace(phone, " ", "");
                        phone = replace(phone, "(", "");

                        contents = replace( (String) ht.get("SMS_MESSAGE"), "$:PHONE:$", phone);
                        
                        cmid = (String)sdf.format(new java.util.Date()) + String.valueOf(myFormatter.format(count++));
                        
//                        System.out.println("cmid = "+cmid + "     dest_phone : " + phone);
                        System.out.println("단건 문자발송");
                        
                        ps.clearParameters();  
//                        ps.setString(1, (String)ht.get("KEYGEN"));
//                        ps.setString(2, phone);
//                        ps.setString(3, (String)ht.get("SEND_TELNO"));
////                        ps.setString(4, (String)ht.get("TRAN_MSG"));
//                        ps.setString(4, contents);
////                        ps.setString(5, (String)ht.get(""));
//                        ps.setString(5, (String)ht.get("FILE_PATH"));
////                        ps.setString(6, (String)ht.get("SEG_FL_PATH"));
//                        ps.setString(6, (String)ht.get("DEPT_CODE"));
//                        ps.setString(7, (String)ht.get("CAMPUS_NO"));
                        
                        if(gubun.equals("S")){
                        	ps.setString(1, (String)ht.get("KEYGEN"));
                            ps.setString(2, phone);
                            ps.setString(3, (String)ht.get("SEND_TELNO"));
//                            ps.setString(4, (String)ht.get("TRAN_MSG"));
                            ps.setString(4, contents);
                            ps.setString(5, (String)ht.get("FILE_PATH"));
//                            ps.setString(5, (String)ht.get("FILE_PATH"));
//                            ps.setString(6, (String)ht.get("SEG_FL_PATH"));
                            
                            ps.setString(6, (String)ht.get("DEPT_CODE"));
                            ps.setString(7, (String)ht.get("CAMPUS_NO"));	
                        }else{
//                        	System.out.println(" ===>>> MMS <<=== ");
                        	ps.setString(1, (String)ht.get("KEYGEN"));
                            ps.setString(2, phone);
                            ps.setString(3, (String)ht.get("SEND_TELNO"));
                            ps.setString(4, null);
                            ps.setString(5, null);
                            
                            ps.setString(6, (String)ht.get("DEPT_CODE"));
                            ps.setString(7, (String)ht.get("CAMPUS_NO"));
                        }                        
                        
//                        System.out.println("===>>> insertQuery >>>" +  insertQuery);
//                        System.out.println("===>>> KEYGEN >>>" +  (String)ht.get("KEYGEN"));
//                        System.out.println("===>>> phone >>>" +  phone);
//                        System.out.println("===>>> SEND_TELNO >>>" +  (String)ht.get("SEND_TELNO"));
//                        System.out.println("===>>> contents >>>" +  contents);
//                        System.out.println("===>>> FILE_PATH >>>" +  (String)ht.get("FILE_PATH"));
//                        System.out.println("===>>> SEG_FL_PATH >>>" +  (String)ht.get("SEG_FL_PATH"));
//                        System.out.println("===>>> DEPT_CODE >>>" +  (String)ht.get("DEPT_CODE"));
//                        System.out.println("===>>> CAMPUS_NO >>>" +  (String)ht.get("CAMPUS_NO"));
                        ps.executeUpdate();
                    }
                }
            } else {
                Properties dbprops = getDBInfo( (String) ht.get("DB_CONN_NO"), con);
                CSVTokenizer ct = new CSVTokenizer( (String) ht.get("MERGE_KEY"));
                String[] key = new String[ct.countTokens()];
                String phone = "";
                int phoneIndex = -1, nameIndex = -1, index = 0;

                while (ct.hasMoreTokens()) {
                    String token = ct.nextToken();
                    if (token.equalsIgnoreCase("phone")) {
                        phoneIndex = index; // 전화번호 필드 인덱스를 가져온다.
                    }
                    else if(token.equalsIgnoreCase("name")) {
                        nameIndex = index;
                    }
                    key[index++] = token;
                }
                ct = null;

                JdbcConnection dbcon = null;
                Connection dbconn = null;
                Statement stmt = null;

                try {
                    dbcon = JdbcConnection.getInstance(dbprops);
                    stmt = dbcon.createStatement();
                    rs = stmt.executeQuery( (String) ht.get("QUERY"));
                    while (rs.next()) {
                        Hashtable htMerge = new Hashtable();
                        for (int k = 0; k < key.length; k++) {
                            String dataValue = rs.getString(k + 1);
                            htMerge.put("$:" + key[k] + ":$", dataValue == null ? "" : dataValue);
                        }
                        contents = replace( (String) ht.get("SMS_MESSAGE"), htMerge);

                        phone = (String) htMerge.get("$:" + key[phoneIndex] + ":$");
                        String name = (nameIndex == -1) ? " " : (String) htMerge.get("$:" + key[nameIndex] + ":$");
                        if (phone == null || phone.trim().length() == 0) {
                            htMerge = null;
                            ct = null;
                            continue;
                        }
                        phone = replace(phone, ")", "-");
                        phone = replace(phone, " ", "");
                        phone = replace(phone, "(", "");

                        try {
//                        	cmid = (String)sdf.format(new java.util.Date()) + String.valueOf(myFormatter.format(count++));
//                        	System.out.println("cmid = "+cmid + "     dest_phone : " + phone);
                            cmid = (String)sdf.format(new java.util.Date()) + String.valueOf(myFormatter.format(count++));
//                            System.out.println("cmid = "+cmid + "     dest_phone : " + phone );
                            System.out.println("DB 추출 문자 발송");
                            
                            ps.clearParameters();                        

                            if(gubun.equals("S")){
                            	ps.setString(1, (String)ht.get("KEYGEN"));
                                ps.setString(2, phone);
                                ps.setString(3, (String)ht.get("SEND_TELNO"));
//                                ps.setString(4, (String)ht.get("TRAN_MSG"));
                                ps.setString(4, contents);
                                ps.setString(5, (String)ht.get("FILE_PATH"));
//                                ps.setString(5, (String)ht.get("FILE_PATH"));
//                                ps.setString(6, (String)ht.get("SEG_FL_PATH"));
                                
                                ps.setString(6, (String)ht.get("DEPT_CODE"));
                                ps.setString(7, (String)ht.get("CAMPUS_NO"));	
                            }else{
                            	ps.setString(1, (String)ht.get("KEYGEN"));
                                ps.setString(2, phone);
                                ps.setString(3, (String)ht.get("SEND_TELNO"));
                                ps.setString(4, "");
                                ps.setString(5, "");
                                
                                ps.setString(6, (String)ht.get("DEPT_CODE"));
                                ps.setString(7, (String)ht.get("CAMPUS_NO"));
                            }                            
                            
                            ps.executeUpdate();
                            
                        }
                        catch (Exception e) {
                            System.err.println(e.getMessage());
                            e.printStackTrace(System.err);
                        }
                        finally {
                        }

                        htMerge = null;
                    }
                }
                catch (Exception e) {
                    System.err.println(e.getMessage());
                    e.printStackTrace(System.err);
                }
                finally {
                    if (rs != null) {
                        try {
                            rs.close();
                            rs = null;
                        }
                        catch (Exception e) {}
                    }
                    if (stmt != null) {
                        try {
                            stmt.close();
                            stmt = null;
                        }
                        catch (Exception e) {}
                    }
                    if (dbcon != null) {
                        try {
                            dbcon.close();
                            dbcon = null;
                        }
                        catch (Exception e) {}
                    }
                }
            }
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace(System.err);
        }
        finally {
            if (ps != null) {
                try {
                    ps.close();
                    ps = null;
                }
                catch (Exception e) {}
            }
            if (smsCon != null) {
                try {
                    smsCon.close();
                    smsCon = null;
                }
                catch (Exception e) {}
            }
        }
    }

    private Vector getPhoneList(String msgid, String keygen) {
        Vector vt = new Vector();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        String SQL = "SELECT PHONE FROM NEO_SMS_PHONE WHERE MSGID = ? AND KEYGEN = ?";

        try {
            JdbcConnection jc = JdbcConnection.getWorkConnection();
            jc.newConnection();
            con = jc.getConnection();
            ps = con.prepareStatement(SQL);
            ps.setString(1, msgid);
            ps.setString(2, keygen);
            rs = ps.executeQuery();

            while (rs.next()) {
                vt.addElement(rs.getString("PHONE"));
            }
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace(System.err);
        }
        finally {
            if (rs != null) {
                try {
                    rs.close();
                    rs = null;
                }
                catch (Exception e) {}
            }
            if (ps != null) {
                try {
                    ps.close();
                    ps = null;
                }
                catch (Exception e) {}
            }
            if (con != null) {
                try {
                    con.close();
                    con = null;
                }
                catch (Exception e) {}
            }
        }
        return vt;
    }

    private void migrate() {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        System.out.println("migrate().start");
        /*
          String selectSql = "SELECT A.MSGID, A.KEYGEN, A.SEG_NO, A.CAMPUS_NO, A.SEND_TELNO, A.O_CODE, " +
           " A.P_CODE, A.SMS_MESSAGE, B.CREATE_TY, B.SEG_FL_PATH, B.SEPARATOR_, B.QUERY,  " +
           " B.MERGE_KEY, B.MERGE_COL, B.DB_CONN_NO" +
           " FROM NEO_SMS A, NEO_SEGMENT B WHERE A.SEG_NO = B.SEG_NO AND A.STATUS = '1'" +
           " AND A.SEND_DATE <= '" + sdf.format(new java.util.Date()) + "'";
         */

//        String selectSql = "SELECT A.MSGID,  " +
//            "	  A.KEYGEN,  " +
//            "	  A.SEG_NO, " +
//            "	  A.CAMPUS_NO, " +
//            "	  A.SEND_TELNO, " +
//            "	  A.O_CODE, " +
//            "	  A.P_CODE, " +
//            "	  A.SMS_MESSAGE, " +
//            "	  A.SEND_DATE," +	
//            "	  ISNULL((SELECT CREATE_TY FROM NEO_SEGMENT WHERE SEG_NO = A.SEG_NO), '999') CREATE_TY, " +
//            "	  ISNULL((SELECT SEG_FL_PATH FROM NEO_SEGMENT WHERE SEG_NO = A.SEG_NO), '') SEG_FL_PATH, " +
//            "	  ISNULL((SELECT SEPARATOR_ FROM NEO_SEGMENT WHERE SEG_NO = A.SEG_NO), '') SEPARATOR_, " +
//            "	  ISNULL((SELECT QUERY FROM NEO_SEGMENT WHERE SEG_NO = A.SEG_NO), '') QUERY, " +
//            "	  ISNULL((SELECT MERGE_KEY FROM NEO_SEGMENT WHERE SEG_NO = A.SEG_NO), '') MERGE_KEY, " +
//            "	  ISNULL((SELECT MERGE_COL FROM NEO_SEGMENT WHERE SEG_NO = A.SEG_NO), '') MERGE_COL, " +
//            "	  ISNULL((SELECT DB_CONN_NO FROM NEO_SEGMENT WHERE SEG_NO = A.SEG_NO), -1) DB_CONN_NO, " +
//            "     A.GUBUN, ISNULL (A.FILE_PATH, ' ') FILE_PATH , A.SMS_NAME" +
//            " FROM NEO_SMS A WHERE A.STATUS = '1' AND A.SEND_DATE <= '" + this.sdf.format(new java.util.Date()) + "'";
        
	        String selectSql = "SELECT A.MSGID,  \t  A.KEYGEN,  \t  A.SEG_NO, \t  A.CAMPUS_NO, \t  A.SEND_TELNO, \t  A.O_CODE, \t  A.P_CODE, \t  A.SMS_MESSAGE, \t  ISNULL((SELECT CREATE_TY FROM NEO_SEGMENT WHERE SEG_NO = A.SEG_NO), '999') CREATE_TY, \t  ISNULL((SELECT SEG_FL_PATH FROM NEO_SEGMENT WHERE SEG_NO = A.SEG_NO), '') SEG_FL_PATH, \t  ISNULL((SELECT SEPARATOR_ FROM NEO_SEGMENT WHERE SEG_NO = A.SEG_NO), '') SEPARATOR_, \t  ISNULL((SELECT QUERY FROM NEO_SEGMENT WHERE SEG_NO = A.SEG_NO), '') QUERY, \t  ISNULL((SELECT MERGE_KEY FROM NEO_SEGMENT WHERE SEG_NO = A.SEG_NO), '') MERGE_KEY, \t  ISNULL((SELECT MERGE_COL FROM NEO_SEGMENT WHERE SEG_NO = A.SEG_NO), '') MERGE_COL, \t  ISNULL((SELECT DB_CONN_NO FROM NEO_SEGMENT WHERE SEG_NO = A.SEG_NO), -1) DB_CONN_NO,      A.GUBUN, ISNULL (A.FILE_PATH, ' ') FILE_PATH , A.SMS_NAME, A.SEND_DATE, A.DEPT_NO, A.CAMPUS_NO FROM NEO_SMS A WHERE A.STATUS = '1' AND A.SEND_DATE <= '" + this.sdf.format(new Date()) + "'";

//	        System.out.println("selectSql===>>> " + selectSql);
        try {
            JdbcConnection jc = JdbcConnection.getWorkConnection();
            jc.newConnection();
            con = jc.getConnection();
            stmt = con.createStatement();
            rs = stmt.executeQuery(selectSql);
            //System.out.println("connection = " + con);
            while (rs.next()) {
                Hashtable htSMS = new Hashtable();
                Hashtable htMMS = new Hashtable();
                String msgid = rs.getString("MSGID");
                String keygen = rs.getString("KEYGEN");
                String segno = String.valueOf(rs.getInt("SEG_NO"));
                String campNo = rs.getString("CAMPUS_NO");
                String telno = rs.getString("SEND_TELNO");
            //    String o_code = rs.getString("O_CODE");
            //    String p_code = rs.getString("P_CODE");
                String message = rs.getString("SMS_MESSAGE");
                String createtype = rs.getString("CREATE_TY");
                String segflpath = rs.getString("SEG_FL_PATH");
                String separator = rs.getString("SEPARATOR_");
                String query = rs.getString("QUERY");
                String mergekey = rs.getString("MERGE_KEY");
                String mergecol = rs.getString("MERGE_COL");
                String dbconno = String.valueOf(rs.getInt("DB_CONN_NO"));
                String gubun = rs.getString("GUBUN");
                String file_path = rs.getString("FILE_PATH");
                String sms_name = rs.getString("SMS_NAME");
                String send_Date = rs.getString("SEND_DATE");
                String dept_code = rs.getString("DEPT_NO");
//                String svc_code = rs.getString("CAMPUS_NO");
                                
                htSMS.put("MSGID", msgid);
                htSMS.put("KEYGEN", keygen);
                htSMS.put("SEG_NO", segno);
                htSMS.put("CAMPUS_NO", campNo == null ? "" : campNo);
                htSMS.put("SEND_TELNO", telno);
                htSMS.put("SMS_MESSAGE", message);
                htSMS.put("CREATE_TY", createtype);
                htSMS.put("SEG_FL_PATH", segflpath == null ? "" : segflpath);
                htSMS.put("SEPARATOR_", separator == null ? "" : separator);
                htSMS.put("QUERY", query == null ? "" : query);
                htSMS.put("MERGE_KEY", mergekey);
                htSMS.put("MERGE_COL", mergecol);
                htSMS.put("DB_CONN_NO", dbconno);
                htSMS.put("GUBUN", gubun);
                htSMS.put("FILE_PATH", file_path);
                htSMS.put("SMS_NAME",sms_name);
                htSMS.put("SEND_DATE", send_Date);

                htSMS.put("TRAN_PHONE",telno);
                htSMS.put("TRAN_DATE",send_Date);
                htSMS.put("TRAN_MSG",message);
                
                htSMS.put("DEPT_CODE",dept_code);
                
                
                htMMS.put("CREATE_TY", createtype);
                htMMS.put("TRAN_MSG", message);
                htMMS.put("TYPE1", file_path == null ? "" : "IMG");
                htMMS.put("GUBUN", gubun);
                htMMS.put("FILE_PATH", file_path);
                htMMS.put("SEG_FL_PATH", segflpath == null ? "" : segflpath);
                htMMS.put("SMS_NAME",sms_name);
                htMMS.put("TRAN_DATE", send_Date);   
              
                String mmsSeq = null;
                if(gubun.equals("S")){
                	// sms insert
                	
                	doWork(htSMS, con, gubun, mmsSeq);
                	updateStatus(msgid, keygen, con);
                	
                }else{
                	// mms, lms insert
                	doMmsWork(htMMS, htSMS, con, gubun);
                	updateStatus(msgid, keygen, con);
                	
                }
                
            }
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace(System.err);
        }
        finally {
            if (rs != null) {
                try {
                    rs.close();
                    rs = null;
                }
                catch (Exception e) {}
            }
            if (stmt != null) {
                try {
                    stmt.close();
                    stmt = null;
                }
                catch (Exception e) {}
            }
            if (con != null) {
                try {
                    con.close();
                    con = null;
                }
                catch (Exception e) {}
            }
        }
    }
}