package messager.center.creator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.custinfo.safedata.CustInfoSafeData;

import messager.center.config.ConfigLoader;
import messager.center.db.JdbcConnection_bank;
import messager.common.util.EncryptUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
/**
 * 마케팅 수신동의 체크 
 * @author younghoon
 *
 */
public class MktCheck {
	
	private static final Logger LOGGER = LogManager.getLogger(MktCheck.class.getName());
	
	
	public HashMap<String, String>  MktCheckWhere(String gubun,  String queryWhere) throws FetchException {
		// TODO Auto-generated method stub

	    JdbcConnection_bank connection = null;
	    Statement stmt =null;
	    ResultSet rs = null;
	    
    	Properties dbProps = ConfigLoader.getDBProperties();
        Properties bankProps = new Properties();
        Properties cardProps = new Properties();
        Properties mydataProps = new Properties();
        Properties mydataBankProps = new Properties();

        
		String ALGORITHM = "PBEWithMD5AndDES";
		String KEYSTRING = "ENDERSUMS";
		//EncryptUtil enc =  new EncryptUtil();
		CustInfoSafeData CustInfo = new CustInfoSafeData();

		
		//마케팅수신동의 쿼리 파일 가져오기
		bankProps.setProperty("mkt.query.file", dbProps.getProperty("mkt.query.file"));
		cardProps.setProperty("mkt.query.file", dbProps.getProperty("mkt.query.file"));
		mydataProps.setProperty("mkt.query.file", dbProps.getProperty("mkt.query.file"));
		mydataBankProps.setProperty("mkt.query.file", dbProps.getProperty("mkt.query.file"));

		//=====================================
        //은행 DB 접속정보
		//=====================================
        bankProps.setProperty("jdbc.driver.name", dbProps.getProperty("bank.jdbc.driver.name"));
        bankProps.setProperty("jdbc.url", dbProps.getProperty("bank.jdbc.url"));
        bankProps.setProperty("user", dbProps.getProperty("bank.db.user"));
       
        if("Y".equals(dbProps.getProperty("db.password.yn"))) {
        	//bank 복호화
			String bank_db_password;
			try {
				bank_db_password = CustInfo.getEncrypt(dbProps.getProperty("bank.db.password"), KEYSTRING);
				bankProps.setProperty("password", bank_db_password);
			} catch (Exception e) {
				//e.printStackTrace();
				LOGGER.error(e);
			}
        		
        }else {
        	bankProps.setProperty("password", dbProps.getProperty("bank.db.password"));
        }
        
        //bankProps.setProperty("mktTable", dbProps.getProperty("bank.db.table"));
        //bankProps.setProperty("mktColumn", dbProps.getProperty("bank.db.mkcolumn"));
        //bankProps.setProperty("mktId", dbProps.getProperty("bank.db.mktid"));
        
		//=====================================
	    //카드사 DB 접속정보
		//=====================================
        cardProps.setProperty("jdbc.driver.name", dbProps.getProperty("card.jdbc.driver.name"));
        cardProps.setProperty("jdbc.url", dbProps.getProperty("card.jdbc.url"));
        cardProps.setProperty("user", dbProps.getProperty("card.db.user"));
        
        if("Y".equals(dbProps.getProperty("db.password.yn"))) {
        	//bank 복호화
        	String card_db_password;
			try {
				card_db_password = CustInfo.getDecrypt(dbProps.getProperty("card.db.password"), KEYSTRING);
				cardProps.setProperty("password", card_db_password);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				LOGGER.error(e);
			}
				
        }else {
        	bankProps.setProperty("password", dbProps.getProperty("card.db.password"));
        }
        
        //cardProps.setProperty("mktTable", dbProps.getProperty("card.db.table"));
        //cardProps.setProperty("mktColumn", dbProps.getProperty("card.db.mkcolumn"));
        //cardProps.setProperty("mktId", dbProps.getProperty("card.db.mktid"));
        
		//=====================================
        //마이데이터 DB 접속정보
		//=====================================
        mydataProps.setProperty("jdbc.driver.name", dbProps.getProperty("mydata.jdbc.driver.name"));
        mydataProps.setProperty("jdbc.url", dbProps.getProperty("mydata.jdbc.url"));
        mydataProps.setProperty("user", dbProps.getProperty("mydata.db.user"));
        
        if("Y".equals(dbProps.getProperty("db.password.yn"))) {
        	//bank 복호화
        	String mydata_db_password;
			try {
				mydata_db_password = CustInfo.getDecrypt(dbProps.getProperty("mydata.db.password"), KEYSTRING);
				mydataProps.setProperty("password", mydata_db_password);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				LOGGER.error(e);
			}
        		
        }else {
        	mydataProps.setProperty("password", dbProps.getProperty("mydata.db.password"));
        }
        
        //mydataProps.setProperty("mktTable", dbProps.getProperty("mydata.db.table"));
        //mydataProps.setProperty("mktColumn", dbProps.getProperty("mydata.db.mkcolumn"));
        //mydataProps.setProperty("mktId", dbProps.getProperty("mydata.db.mktid"));
        
		//=====================================
        //마이데이터 DB(은행) 접속정보
		//=====================================
        mydataBankProps.setProperty("jdbc.driver.name", dbProps.getProperty("mydata.jdbc.driver.name"));
        mydataBankProps.setProperty("jdbc.url", dbProps.getProperty("mydata.jdbc.url"));
        mydataBankProps.setProperty("user", dbProps.getProperty("mydata.db.user"));
        
        if("Y".equals(dbProps.getProperty("db.password.yn"))) {
        	//bank 복호화
        	String mydataBank_db_password;
			try {
				mydataBank_db_password = CustInfo.getDecrypt(dbProps.getProperty("mydataBank.db.password"), KEYSTRING);
				mydataBankProps.setProperty("password", mydataBank_db_password);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				LOGGER.error(e);
			}
        		
        }else {
        	mydataBankProps.setProperty("password", dbProps.getProperty("mydataBank.db.password"));
        }
        
        //mydataBankProps.setProperty("mktTable", dbProps.getProperty("mydataBank.db.table"));
        //mydataBankProps.setProperty("mktColumn", dbProps.getProperty("mydataBank.db.mkcolumn"));
        //mydataBankProps.setProperty("mktId", dbProps.getProperty("mydataBank.db.mktid"));
        
        
        queryWhere = queryWhere.trim();
        String[] queryWhereList = queryWhere.split(",");
        StringBuffer str = new StringBuffer();
        
        /* ---------------------------------------------------------------------------
	     	중복아이디일 경우 중봉아이디 뒤에 -1, -2 처럼 하이픈과 숫자를 추가하여 유니크한 아이디 값으로 사용하기로 정의 
	     	그래서 아이디 뒤에 붙은 -1, -2 제거 작업 필요
	     ---------------------------------------------------------------------------*/
		int idGap = 0;
		int idIdx = 0;
		String idList = "";
		Map<String, String> strIdMap = new HashMap<String, String>();
		
		for(String strId : queryWhereList) {
			idIdx++;
			//중복아이디로 인해 불필요한 값 제거 작업 
			if(strId.contains("-")) {
				idGap = strId.indexOf("-");
				strId = strId.trim();
				strIdMap.put(strId.replace("'", ""), strId.replace("'", ""));
				strId = strId.substring(0,idGap)+"'";
				idList += strId+","; 
			//중복아이디가 아닐경우
			}else {
				idList += strId+",";
			}
			//System.out.println("strId : " + strId);	
		}
		//마지막 ,Q, 값 제거 작업
		idList = idList.replace(",'Q',","");
		idList = idList.trim();
		//불필요값 제거된 아이디들 배열에 재 적재
		queryWhereList = idList.split(",");
		
		//System.out.println("idList :" +idList);
		//System.out.println("queryWhereList :" +queryWhereList);
		/* ---------------------------------------------------------------------------*/
		
        
        /* ---------------------------------------------------------------------------
         	마케팅 수신동의 쿼리 가져오기
         ---------------------------------------------------------------------------*/
    	String bankGubun ="BANK=";
    	String cardGubun ="CARD=";
    	String mydataGubun ="MYDATA=";
    	String mydataBankGubun ="MYDATA_BANK=";
    	
    	int	bankIdx =0;
    	int	cardIdx =0;
    	int	mydataIdx =0;
    	int	mydataBankIdx =0;
    	
    	//String strQuery = "C:\\query.sql";
    	String strQuery = dbProps.getProperty("mkt.query.file");
    	String rstQuery ="";
    	String bankQuery ="";
    	String cardQuery ="";
    	String mydataQuery ="";
    	String mydataBankQuery ="";
    	
    		try {
    			BufferedReader br = new BufferedReader( new FileReader( new File( strQuery ) ) );
    			String strLine = "";

    			while( ( strLine = br.readLine() ) != null ) {
    				rstQuery += strLine+" " ;
    			}
    			
    			bankIdx = rstQuery.indexOf(bankGubun);
    			cardIdx = rstQuery.indexOf(cardGubun);
    			mydataIdx = rstQuery.indexOf(mydataGubun);
    			mydataBankIdx = rstQuery.indexOf(mydataBankGubun);
    			
    			bankQuery = rstQuery.substring(bankIdx+bankGubun.length(),cardIdx);
    			cardQuery = rstQuery.substring(cardIdx+cardGubun.length(),mydataIdx);
    			mydataQuery = rstQuery.substring(mydataIdx+mydataGubun.length(),mydataBankIdx);
    			mydataBankQuery = rstQuery.substring(mydataBankIdx+mydataBankGubun.length());
    			//System.out.println("bankQuery : " +bankQuery);
    			//System.out.println("cardQuery : "+cardQuery);
    			//System.out.println("mydataQuery : "+mydataQuery);
    			//System.out.println("mydataBankQuery : "+mydataBankQuery);
    			
    		} catch (Exception e) {
    			//e.printStackTrace();
    			LOGGER.error(e);
    		}
    	/* ---------------------------------------------------------------------------*/
    		
    		        
    	String query_bank = String.format(bankQuery +" AND 1=0","'test'");
    	String query_card = String.format(cardQuery +" AND 1=0","'test'");
    	String query_mydata = String.format(mydataQuery +" AND 1=0","'test'");
    	String query_mydataBank = String.format(mydataBankQuery +" AND 1=0","'test'");
        
        for( int i = 0 ; i < queryWhereList.length ; i++) { 
        	str.append( (str.toString().length() > 0 ? "," : "" ) + queryWhereList[i]);
        	//500개씩 가져와 union (오라클에서는 1000개이상 조회시 오류)
        	if ( i % 500 == 0 && i > 0) { 
        		query_bank += String.format(" UNION ALL "+bankQuery, str.toString());
        		query_card += String.format(" UNION ALL "+cardQuery, str.toString());
        		query_mydata += String.format(" UNION ALL "+mydataQuery, str.toString());
        		query_mydataBank += String.format(" UNION ALL "+mydataBankQuery, str.toString());
        		str = new StringBuffer();
        	}
        }
        //500개미만 짜투리 
        if (!str.toString().equals("")) {
            	query_bank += String.format(" UNION ALL "+bankQuery, str.toString());
            	query_card += String.format(" UNION ALL "+cardQuery, str.toString());
            	query_mydata += String.format(" UNION ALL "+mydataQuery, str.toString());
            	query_mydataBank += String.format(" UNION ALL "+mydataBankQuery, str.toString());
        }
        
        HashMap<String, String> resultMap = new HashMap<String, String>();
        
        //bank
	    if("001".equals(gubun)) { 
	    	
	    	  try {
	  			connection = connection.getInstance(bankProps);
	  			stmt = connection.createStatement();
	  			rs = stmt.executeQuery(query_bank);
	  			
	  			while(rs.next()) {
	  				String id = rs.getString("CSTNO");
	  				resultMap.put(id, id);
	  				
	  					Set<String> keySet = strIdMap.keySet();
	  					for (String key : keySet) {
	  						if(strIdMap.get(key).contains("-")) {
	  							if(strIdMap.get(key).contains(id)) {
		  							resultMap.put(strIdMap.get(key), strIdMap.get(key));
		  						}
	  						}
	  						
	  				}
	  			}
	  			
	  		} catch (ClassNotFoundException e) {
	  			// TODO Auto-generated catch block
	  			//e.printStackTrace();
	  			LOGGER.error(e);
	  			throw new FetchException("마케팅동의 체크 오류 DB접속정보를 확인하세요 (ClassNotFoundException)",
	                      ErrorCode.MTK_CK_ERROR);
	  		} catch (SQLException e) {
	  			// TODO Auto-generated catch block
	  			//e.printStackTrace();
	  			LOGGER.error(e);
	  			throw new FetchException("마케팅동의 체크 오류 DB접속정보를 확인하세요 (SQLException)",
	                      ErrorCode.MTK_CK_ERROR);
	  		}finally {
	  			try {
	  				if(rs != null) {
	  					rs.close();
	  				}if(stmt != null) {
	  					stmt.close();
	  				}if(connection != null) {
	  					connection.close();
	  				}
	  			} catch (SQLException e) {
	  				// TODO Auto-generated catch block
	  				//e.printStackTrace();
	  				LOGGER.error(e);
	  			}
	  		}
	    
	    //card	
	    }else if("002".equals(gubun)) { 
	    	
	    	  try {
		  			connection = connection.getInstance(cardProps);
		  			stmt = connection.createStatement();
		  			rs = stmt.executeQuery(query_card);

		  			while(rs.next()) {
		  				String id = rs.getString("CSTNO");
		  				resultMap.put(id, id);
		  				
	  					Set<String> keySet = strIdMap.keySet();
	  					for (String key : keySet) {
	  						if(strIdMap.get(key).contains("-")) {
	  							if(strIdMap.get(key).contains(id)) {
		  							resultMap.put(strIdMap.get(key), strIdMap.get(key));
		  						}
	  						}
	  						
	  				}
		  			}
		  			
		  		} catch (ClassNotFoundException e) {
		  			// TODO Auto-generated catch block
		  			//e.printStackTrace();
		  			LOGGER.error(e);
		  			throw new FetchException("마케팅동의 체크 오류 DB접속정보를 확인하세요 (ClassNotFoundException)",
		                      ErrorCode.MTK_CK_ERROR);
		  		} catch (SQLException e) {
		  			// TODO Auto-generated catch block
		  			//e.printStackTrace();
		  			LOGGER.error(e);
		  			throw new FetchException("마케팅동의 체크 오류 DB접속정보를 확인하세요 (SQLException)",
		                      ErrorCode.MTK_CK_ERROR);
		  		}finally {
		  			try {
		  				if(rs != null) {
		  					rs.close();
		  				}if(stmt != null) {
		  					stmt.close();
		  				}if(connection != null) {
		  					connection.close();
		  				}
		  			} catch (SQLException e) {
		  				// TODO Auto-generated catch block
		  				//e.printStackTrace();
		  				LOGGER.error(e);
		  			}
		  		}
	    
	    //마이데이터
	    }else if("003".equals(gubun)) { 
	    	
	    	  try {
		  			connection = connection.getInstance(mydataProps);
		  			stmt = connection.createStatement();
		  			rs = stmt.executeQuery(query_mydata);

		  			while(rs.next()) {
		  				String id = rs.getString("CSTNO");
		  				resultMap.put(id, id);
		  				
	  					Set<String> keySet = strIdMap.keySet();
	  					for (String key : keySet) {
	  						if(strIdMap.get(key).contains("-")) {
	  							if(strIdMap.get(key).contains(id)) {
		  							resultMap.put(strIdMap.get(key), strIdMap.get(key));
		  						}
	  						}
	  						
	  				}
		  			}
		  			
		  		} catch (ClassNotFoundException e) {
		  			// TODO Auto-generated catch block
		  			//e.printStackTrace();
		  			LOGGER.error(e);
		  			throw new FetchException("마케팅동의 체크 오류 DB접속정보를 확인하세요 (ClassNotFoundException)",
		                      ErrorCode.MTK_CK_ERROR);
		  		} catch (SQLException e) {
		  			// TODO Auto-generated catch block
		  			//e.printStackTrace();
		  			LOGGER.error(e);
		  			throw new FetchException("마케팅동의 체크 오류 DB접속정보를 확인하세요 (SQLException)",
		                      ErrorCode.MTK_CK_ERROR);
		  		}finally {
		  			try {
		  				if(rs != null) {
		  					rs.close();
		  				}if(stmt != null) {
		  					stmt.close();
		  				}if(connection != null) {
		  					connection.close();
		  				}
		  			} catch (SQLException e) {
		  				// TODO Auto-generated catch block
		  				//e.printStackTrace();
		  				LOGGER.error(e);
		  			}
		  		}
	    
	    //마이데이터(은행)
	    }else if("004".equals(gubun)) { 
	    	
	    	  try {
		  			connection = connection.getInstance(mydataBankProps);
		  			stmt = connection.createStatement();
		  			rs = stmt.executeQuery(query_mydataBank);

		  			while(rs.next()) {
		  				String id = rs.getString("CSTNO");
		  				resultMap.put(id, id);
		  				
	  					Set<String> keySet = strIdMap.keySet();
	  					for (String key : keySet) {
	  						if(strIdMap.get(key).contains("-")) {
	  							if(strIdMap.get(key).contains(id)) {
		  							resultMap.put(strIdMap.get(key), strIdMap.get(key));
		  						}
	  						}
	  						
	  				}
		  			}
		  			
		  		} catch (ClassNotFoundException e) {
		  			// TODO Auto-generated catch block
		  			//e.printStackTrace();
		  			LOGGER.error(e);
		  			throw new FetchException("마케팅동의 체크 오류 DB접속정보를 확인하세요 (ClassNotFoundException)",
		                      ErrorCode.MTK_CK_ERROR);
		  		} catch (SQLException e) {
		  			// TODO Auto-generated catch block
		  			//e.printStackTrace();
		  			LOGGER.error(e);
		  			throw new FetchException("마케팅동의 체크 오류 DB접속정보를 확인하세요 (SQLException)",
		                      ErrorCode.MTK_CK_ERROR);
		  		}finally {
		  			try {
		  				if(rs != null) {
		  					rs.close();
		  				}if(stmt != null) {
		  					stmt.close();
		  				}if(connection != null) {
		  					connection.close();
		  				}
		  			} catch (SQLException e) {
		  				// TODO Auto-generated catch block
		  				//e.printStackTrace();
		  				LOGGER.error(e);
		  			}
		  		}
	    }
		
        return resultMap;
	}
	

	public static boolean MktCheck(String gubun,  String userID) throws FetchException {
		// TODO Auto-generated method stub

	    JdbcConnection_bank connection = null;
	    PreparedStatement pstmt =null;
	    ResultSet rs = null;
	    boolean CK = false;
	    int ct = 0;
	    
    	Properties dbProps = ConfigLoader.getDBProperties();
        Properties bankProps = new Properties();
        Properties cardProps = new Properties();

        //은행 DB 접속정보
        bankProps.setProperty("jdbc.driver.name", dbProps.getProperty("bank.jdbc.driver.name"));
        bankProps.setProperty("jdbc.url", dbProps.getProperty("bank.jdbc.url"));
        bankProps.setProperty("user", dbProps.getProperty("bank.db.user"));
        bankProps.setProperty("password", dbProps.getProperty("bank.db.password"));
        
	    //카드사 DB 접속정보
        cardProps.setProperty("jdbc.driver.name", dbProps.getProperty("card.jdbc.driver.name"));
        cardProps.setProperty("jdbc.url", dbProps.getProperty("card.jdbc.url"));
        cardProps.setProperty("user", dbProps.getProperty("card.db.user"));
        cardProps.setProperty("password", dbProps.getProperty("card.db.password"));
        
        String query_bank = "SELECT COUNT(*) FROM TEMP_BANK WHERE ID= ? AND RESP_YN='Y'";
        String query_card = "SELECT COUNT(*) FROM TEMP_CARD WHERE ID= ? AND RESP_YN='Y'";
        
        //bank
	    if("001".equals(gubun)) { 
	    	
	    	  try {
	  			connection = connection.getInstance(bankProps);
	  			pstmt = connection.prepareStatement(query_bank);
	  			
	  			pstmt.setString(1, userID);
	  			pstmt.executeUpdate();
	  			
	  			rs = pstmt.executeQuery();
	  			
	  			if(rs.next()) {
	  				if( rs.getInt(1) > 0) {
	  					CK= true;
	  				}
	  			}
	  			
	  		} catch (ClassNotFoundException e) {
	  			// TODO Auto-generated catch block
	  			//e.printStackTrace();
	  			LOGGER.error(e);
	  			throw new FetchException("마케팅동의 체크 오류 DB접속정보를 확인하세요 (ClassNotFoundException)",
	                      ErrorCode.MTK_CK_ERROR);
	  		} catch (SQLException e) {
	  			// TODO Auto-generated catch block
	  			//e.printStackTrace();
	  			LOGGER.error(e);
	  			throw new FetchException("마케팅동의 체크 오류 DB접속정보를 확인하세요 (SQLException)",
	                      ErrorCode.MTK_CK_ERROR);
	  		}finally {
	  			try {
	  				if(rs != null) {
	  					rs.close();
	  				}if(pstmt != null) {
	  					pstmt.close();
	  				}if(connection != null) {
	  					connection.close();
	  				}
	  			} catch (SQLException e) {
	  				// TODO Auto-generated catch block
	  				//e.printStackTrace();
	  				LOGGER.error(e);
	  			}
	  		}
	    
	    //card	
	    }else if("002".equals(gubun)) { 
	    	
	    	  try {
		  			connection = connection.getInstance(cardProps);
		  			pstmt = connection.prepareStatement(query_card);
		  			
		  			pstmt.setString(1, userID);
		  			pstmt.executeUpdate();
		  			
		  			rs = pstmt.executeQuery();
		  			
		  			if(rs.next()) {
		  				if( rs.getInt(1) > 0) {
		  					CK= true;
		  				}
		  			}
		  			
		  		} catch (ClassNotFoundException e) {
		  			// TODO Auto-generated catch block
		  			//e.printStackTrace();
		  			LOGGER.error(e);
		  			throw new FetchException("마케팅동의 체크 오류 DB접속정보를 확인하세요 (ClassNotFoundException)",
		                      ErrorCode.MTK_CK_ERROR);
		  		} catch (SQLException e) {
		  			// TODO Auto-generated catch block
		  			//e.printStackTrace();
		  			LOGGER.error(e);
		  			throw new FetchException("마케팅동의 체크 오류 DB접속정보를 확인하세요 (SQLException)",
		                      ErrorCode.MTK_CK_ERROR);
		  		}finally {
		  			try {
		  				if(rs != null) {
		  					rs.close();
		  				}if(pstmt != null) {
		  					pstmt.close();
		  				}if(connection != null) {
		  					connection.close();
		  				}
		  			} catch (SQLException e) {
		  				// TODO Auto-generated catch block
		  				//e.printStackTrace();
		  				LOGGER.error(e);
		  			}
		  		}
	    	
	    }
		
        return CK;
	}

	
	public static void main(String[] args) throws FetchException {
		
		// card or bank
		boolean testCK = MktCheck("001","test");
		//System.out.println("testCK : "  +testCK);
		LOGGER.info("testCK : "  +testCK);
	}
	
}
