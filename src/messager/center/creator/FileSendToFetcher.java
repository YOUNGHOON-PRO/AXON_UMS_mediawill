package messager.center.creator;

import java.io.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import com.custinfo.safedata.CustInfoSafeData;

import messager.center.config.ConfigLoader;
import messager.center.db.JdbcConnection;
import messager.center.db.JdbcConnection_bank;
import messager.common.*;

/**
 * 대상자가 Address 파일일 경우 Address파일을 FileTransfer에 요청해서 필드를 구분하고 UnitInfo 객체를 생성해서
 * MsgUnitManager 객체를 이용해서 저장소에 저장한다
 */
public class FileSendToFetcher
    extends SendToFetcher
{
    //Address 파일을 line단위로 읽기 위해
    private BufferedReader in;
    
    //필드의 구분문자.
    private String separator;

    private int separatorLen;


    /**
     * FileSendToFetcher 객체를 생성한다.
     *
     * @param message
     * @param requester
     *            FileTransfer에 Address File을 요청
     * @param filePath
     *            요청할 Address File의 경로
     * @param charsetName
     *            컨텐츠의 charset(Address File를 Reader로 읽을때 사용된다)
     * @param aSeparator
     *            필드 구분문자(Address File일 경우 필드 구분문자는 Segment 테이블의 _From 컬럼에서
     *            가져온다.
     */
    public FileSendToFetcher(Message message, FileRequester requester)
        throws FetchException {
        super(message);
        init(requester);
    }

    private void init(FileRequester requester)
        throws FetchException {
        HashMap taskMap = message.taskMap;
        String path = (String) taskMap.remove("NEO_SEGMENT.SEG_FL_PATH");
        separator = (String) taskMap.remove("NEO_SEGMENT.SEPARATOR_");
        separatorLen = separator.length();

        if (path == null || path.length() == 0) {
            String detail = "[" + message.messageID
                + "] NOT FOUND ADDRESS FILE PATH : " + path;
            throw new FetchException(detail,
                                     ErrorCode.INVALID_ADDRESS_FILE_PATH);
        }

        //String charsetName = CharsetTable.javaCharsetName(message.charsetCode);
        String charsetName = message.charsetCode;

        try {
            //FileTransfer에 file요청
            InputStream stream = requester.request(path);
            //BufferedReader 객체 생성
            //in = new BufferedReader(new InputStreamReader(stream, charsetName));
            //송진우 : UTF-8 인코딩 처리
            in = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
        }
        catch (Exception ex) {
            throw new FetchException(ex.getMessage(),
                                     ErrorCode.ADDRESS_FILE_FETCH_FAIL);
        }
    }

    private boolean isValidColumn(String record) {
        String[] fields = lineToFields(record);
        // Field의 갯수는  MergeKey 수보다 항상 1보다 크다
        if (message.keySize >= fields.length) {
            return false;
        }
        HashMap keyMap = message.keyMap;
        boolean isValid = true;
        for (int i = 1; i < fields.length; i++) {
            String fieldName = fields[i];
            Object obj = keyMap.get(fieldName);
            if (obj != null && (obj instanceof Integer)) {
                int n = ( (Integer) obj).intValue();
                if (n != i) {
                    isValid = false;
                }
            }
            else {
                isValid = false;
                break;
            }
        }
        return isValid;
    }

    /**
     * Address File를 읽어서 UnitInfo객체를 생성한 후 MessageRespository객체를 이용해서 저장소에
     * UnitInfo객체를 저장한다.
     *
     * @param repository
     *            UnitInfo를 저장
     */
    public void fetch()
        throws FetchException {
        Exception exception = null;
        String messageID = message.messageID;

        String mmsSeq = null;
        
        try {
            int unitID = 1;
            //UnitInfo 객체 생성
            //첫라인은 필드 명(Alias)으로 사용된다.
            String line = in.readLine();
        
            CustInfoSafeData safeDbEnc = new CustInfoSafeData();
            
            
            /*------------------------------------------------------------------------
              	암호화된 대상자컬럼들을  복호화 하기 위한 사전 준비
             ------------------------------------------------------------------------*/
            StringTokenizer st3;
            st3 = new StringTokenizer(line, ",");
            
            //암호화 대상 center.properties에서 가져오기 
            String dec_merge = ConfigLoader.getString("DEC_MERGE", "");
            //구분자 변경
            dec_merge = dec_merge.replace(";",",");
            
			HashMap<Integer, String> merge_map= new HashMap<Integer, String>();
			//머지 항목 갯수
			int merge_cnt=1;
			String merge_value="";
			
			while(st3.hasMoreElements()) {
				merge_value = st3.nextToken();
				 if(dec_merge.contains(merge_value)){
					 merge_map.put(merge_cnt, merge_value);
				 }
				 merge_cnt++;
			}
			/*------------------------------------------------------------------------*/
            
			
            /*-------------------------------------------------------------------------
            	마케팅 수신동의 체크를 위한 ID 발취 (ID 값이 몇번째 컬럼인지 찾음)
            	Email 복호화를  위한 EMAIL 발취 (EMAIL 값이 몇번째 컬럼인지 찾음)
            -------------------------------------------------------------------------*/
            StringTokenizer st;
            StringTokenizer st2;
            st = new StringTokenizer(line, ",");
            int i =0;
            int ID_i =0;
            int EMAIL_i =0;
            int BIZKEY_i =0;
            
            String temp = "";
            while(st.hasMoreElements()) {
            	temp = st.nextToken();
            	//System.out.println(temp);
            	i++;
            	if("ID".equals(temp)) {
            		ID_i = i;
            	}
            	if("EMAIL".equals(temp)){
            		EMAIL_i=i;
            	}
            	if("BIZKEY".equals(temp)){
            		BIZKEY_i=i;
            	}
            }
            /*-------------------------------------------------------------------------*/
                        
            
            if (!isValidColumn(line)) {
                String detail = "[" + message.messageID
                    + "] unmatched columns : " + line;
                throw new FetchException(detail,
                                         ErrorCode.UNMATCHED_MEMBER_COLUMNS);
            }
            String[] fields = null;
            UnitInfo unit = new UnitInfo(messageID, unitID++);
            
            Map<Integer, String> map = new HashMap<Integer, String>();
            String line2 = "";
    		int k=1;
            
            /*
             * 테스트 발송 여부를 확인하여 테스트 발송대상자 수를 얻어온다.
             * writed by 오범석
             */
            if (message.isTest) { //테스트 발송이라면
                int send_test_cnt = message.send_test_cnt;
                for (int j = 0; j < send_test_cnt; ) {
                    if ( (line = in.readLine()) != null) {
                        //공백라인제거
                        if (line.trim().equals("")) {
                            continue;
                        }

                        /*이메일 복호화 추가------------------------------------------------------------------------
	                    	1. 파일 라인 읽기 해쉬맵에 담기
	                    	2. 맵에서  복호화 대상컬럼들 추출해 복호화 이후  기존 맵의 데이터들 replace
	                    	3. map 데이터 ,(콤마) 구분으로 String으로 변환
	                    	4. 불필요한 앞뒤 콤마 제거 
	                    ------------------------------------------------------------------------------------*/
	                    
	                   //1. 파일 라인 읽기 해쉬맵에 담기
	                    st2 = new StringTokenizer(line, ",");
	                    k=1;
	                    map.put(0, ""); //map이 0번부터라 0번째 값은 null 삽입
	                    
	                    while(st2.hasMoreElements()) {
	                    	map.put(k, st2.nextToken());
	                    	k++;
	                    	}
	
	                    //2. 맵에서  email 부분만 추출해 복호화 이후  기존 맵의 email 데이터 replace
//	                    if(EMAIL_i != 0) {
//	                    	String deEmail = map.get(EMAIL_i); //Email 인데스 키값으로 암호화된 이메일 추출 
//	                        deEmail = safeDbEnc.getDecrypt(deEmail, "NOT_RNNO"); //이메일 복호화
//	                        map.replace(EMAIL_i, deEmail);
//	                    }
	                    
                        //2. 맵에서  복호화 대상컬럼들 추출해 복호화 이후  기존 맵의 데이터들 replace
	                    if(merge_map.size()!=0) {
	                    	for(int j1=1; j1 <= merge_cnt; j1++) {
	                    		if(merge_map.get(j1)!=null) {
	                    			String deEmail = map.get(j1);
	                    			deEmail = safeDbEnc.getDecrypt(deEmail, "NOT_RNNO"); //이메일 복호화
	    	                        map.replace(j1, deEmail);
	                    		}
	                    		
	                    	}
	                    }
	                    
	                    //3. map 데이터 ,(콤마) 구분으로 String으로 변환
	                    line2="";
	                    for (Integer key : map.keySet()) {
	                    	line2 +=  map.get(key)+",";        	
	                    }
	                    
	                    //4. 불필요한 앞뒤 콤마 제거 
	                    line2 = line2.substring(1, line2.length()); //첫번째 콤마 제거
	                    line2 = line2.substring(0, line2.length() - 1); //마지막 콤마 제거
	                    /*------------------------------------------------------------------------------------*/
                        
                        receiverCount++; //수신 대상자 수 증가
                        j++;
                        //라인를 String[]로 변화
                        fields = lineToFields(line2);
                        ReceiverInfo receiverInfo = new ReceiverInfo(fields);
                        unit.add(receiverInfo);
                    }
                    else {
                        break; //if(rs.next()) ............. END
                    }
                } //for (int i = 0; ............... END

                //UnitInfo에 대상자 수가 지정된 수만큼 되었을 경우 UnitInfo를 FileSystem에 저장 후
                writeUnit(unit);
            }
            else {
            	
            	/* TODO
            	 *  1. 연결된 Stream 에서 모든 라인을 읽어서 저장한다. ( String ) 
            	 *  2. 데이터에서 ID 만 추출해서 SQL 을 준비한다. 
            	 */
            		
            		HashMap<String, String[]> streamMap = new HashMap<String, String[]>();
            		String sqlWhere = "";
            		
            		while ( (line = in.readLine()) != null) {
            			//공백라인제거
                        if (line.trim().equals("")) {
                            continue;
                        }
                        
                        /*이메일 복호화 추가------------------------------------------------------------------------
                        	1. 파일 라인 읽기 해쉬맵에 담기
                        	2. 맵에서  복호화 대상컬럼들 추출해 복호화 이후  기존 맵의 데이터들 replace
                        	3. map 데이터 ,(콤마) 구분으로 String으로 변환
                        	4. 불필요한 앞뒤 콤마 제거 
                        ------------------------------------------------------------------------------------*/
                        
                       //1. 파일 라인 읽기 해쉬맵에 담기
                        st2 = new StringTokenizer(line, ",");
                        k=1;
                        map.put(0, ""); //map이 0번부터라 0번째 값은 null 삽입
                        
                        while(st2.hasMoreElements()) {
                        	map.put(k, st2.nextToken());
                        	k++;
                        	}

                        //2. 맵에서  email 부분만 추출해 복호화 이후  기존 맵의 email 데이터 replace
//                        if(EMAIL_i != 0) {
//                        	String deEmail = map.get(EMAIL_i); //Email 인데스 키값으로 암호화된 이메일 추출 
//                            deEmail = safeDbEnc.getDecrypt(deEmail, "NOT_RNNO"); //이메일 복호화
//                            map.replace(EMAIL_i, deEmail);
//                        }
                      
                        //2. 맵에서  복호화 대상컬럼들 추출해 복호화 이후  기존 맵의 데이터들 replace
	                    if(merge_map.size()!=0) {
	                    	for(int j2=1; j2 <= merge_cnt; j2++) {
	                    		if(merge_map.get(j2)!=null) {
	                    			String deEmail = map.get(j2);
	                    			deEmail = safeDbEnc.getDecrypt(deEmail, "NOT_RNNO"); //이메일 복호화
	    	                        map.replace(j2, deEmail);
	                    		}
	                    		
	                    	}
	                    }
                        
                        //3. map 데이터 ,(콤마) 구분으로 String으로 변환
                        line2="";
                        for (Integer key : map.keySet()) {
                        	line2 +=  map.get(key)+",";        	
                        }
                        
                        //4. 불필요한 앞뒤 콤마 제거 
                        line2 = line2.substring(1, line2.length()); //첫번째 콤마 제거
                        line2 = line2.substring(0, line2.length() - 1); //마지막 콤마 제거
                        /*------------------------------------------------------------------------------------*/
                        
            			fields = lineToFields(line2);
            			streamMap.put(fields[ID_i], fields);
            			//streamMap.put(fields[BIZKEY_i], fields);
            			sqlWhere += "'" + fields[ID_i] + "' ,"; 
            		}
            		
            		sqlWhere += "'Q'";
            	
        		/*
            	 * TODO 
            	 * 1. DB SQL Call
            	 * 2. 결과값을 Hashmap 형태로 보관한다. (= resultMap) 
            	 */
        		
            	HashMap<String, String> resultMap = null;
            		
        		if(message.mail_mkt_gb != null) {
        			MktCheck mktCheck = new MktCheck();
        			resultMap = mktCheck.MktCheckWhere(message.mail_mkt_gb,sqlWhere);
        		}
        		
            	
            	/*  TODO 
            	 *  1. 기존 Reader 에서 비교하던 부분을  Hashmap(= StreamMap ) 에서 꺼내서 사용한다.
            	 *  2. DB 에서 마케팅 동의자에 대한 값이 있는 경우만 처리한다.  
            	 */
            		Set<String> set = streamMap.keySet();
            		
            		Iterator<String> it = set.iterator();
            		
            		while(it.hasNext()) { 
            			String id = it.next();

            			String[] fileds = streamMap.get(id);
            			ReceiverInfo receiverInfo = new ReceiverInfo(fileds);
            			
            			if (message.mail_mkt_gb == null) {
                        	//System.out.println("마케팅 수신동의 미 체크 ");
                        	unit.add(receiverInfo);
                        	receiverCount++;
                        } else { 
                        	//-------------------------------------------------------------------------
                        	//마케팅 수신동의 체크
                        	//System.out.println("마케팅 수신동의  체크 ");
                        	String mktYn = resultMap.get(id);
                        	//String mktYn = resultMap.get(fileds[ID_i]);
                        	// 마케팅 미동의자에 대한 처리 
                        	if ( null == mktYn || "".equals(mktYn)) { 
                        		//continue;
                        		
                        		//미동의자 수집
                        		message.mkttList.add(id);
                        		message.mktIdIdx=ID_i;
                        	} 
                        	unit.add(receiverInfo);
                        	receiverCount++;
                        	//-------------------------------------------------------------------------
                        }
            			
                        	
            			if (unit.size() >= receiversPerUnit) {
                            //Unit에 저장된 대상자 수가 receiversPerUnit(500개)보다 크거나 같으면 UnitInfo 객체를
                            // 저장한다.
                            writeUnit(unit);
                            //새로운 UnitInfo 객체 생성
                            unit = new UnitInfo(messageID, unitID++);
                        }
            			
            		}
               
                if (unit.size() > 0) {
                    //마지막 생성된 Unit에 대상자 수가 0보다 크면 UnitInfo 객체를 저장한다.
                    writeUnit(unit);  // unit폴더에 유닛 생성
                }
            }
        }
        catch (Exception ex) {
            exception = ex;
        }

        if (exception != null) {
            if (exception instanceof FetchException) {
                throw (FetchException) exception;
            }
            else {
                throw new FetchException(exception.getMessage(),
                                         ErrorCode.MEMBER_FETCH_FAIL);
            }
        }
    }

    /**
     * BufferedReader객체 close
     */
    protected void close() {
        if (in != null) {
            try {
                in.close();
            }
            catch (IOException ex) {
            }
            in = null;
        }
    }

    /**
     * Line를 지정된 필드 구분문자로 필드를 나누어서 String[]에 순서대로 채운다. 첫 필드는 UnitInfo에 저장될
     * 순서번호이다.
     *
     * @param line
     * @param offset
     *            line에서 필드의 시작 위치(0)
     * @param len
     *            line에서 필드이 길이(line.length())
     * @param String[]
     *            필드를 구분하여 저장한 String 배열
     */
    private String[] lineToFields(String line) {
        ArrayList list = new ArrayList();
        //첫 필드는 사용되지 않는다.
        list.add(null);
        int inx;
        int off = 0;
        String field;

        while ( (inx = line.indexOf(separator, off)) != -1) {
            field = line.substring(off, inx);
            list.add(field);
            off = inx + separatorLen;
        }

        field = line.substring(off);
        list.add(field);
        String[] fields = new String[list.size()];
        list.toArray(fields);
        return fields;
    }
}
