package messager.mailsender.send.dns;

import java.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LookupCaller
{
	private static final Logger LOGGER = LogManager.getLogger(LookupCaller.class.getName());
	
    private String nameServer;
    private String domain;

    private Vector MXList = new Vector();

    public LookupCaller(String name, String domain) {
        nameServer = name;
        this.domain = domain;
        try {
        	this.getExchanger(nameServer, domain);
        } catch(Exception e) {
        	LOGGER.error(e);
        	//System.err.println("LookupCaller Error Message :: " + e.getMessage());
        }
    }

    public void getExchanger(String nameServer, String hostName) throws Exception {
        Vector exchangers_host = new Vector();
        Vector exchangers_ip = new Vector();
        /***************************************************************/
        // 메일 서버의 mx 서버 host 를 찾는다. 
        Lookup lookMX = new Lookup(nameServer, hostName, 15);
        exchangers_host = lookMX.getResult();
        
        for( int i=0 ; i < exchangers_host.size(); i++) { 
        	String mx = null; 
        	
        	try {
	        	mx = ((ResourceRecord) exchangers_host.get(i)).mx;
	        	Lookup lookMX2 = new Lookup(nameServer, mx, 1);
	        	exchangers_ip = lookMX2.getResult();
        	} catch(NullPointerException npe ) { 
        		LOGGER.error(npe);
        		// npe.printStackTrace();
        		//System.err.println("Error Message : " + npe.getMessage() +  ", nameServer : "+ nameServer + ", hostName : " + hostName);
        	} catch(Exception e) { 
        		LOGGER.error(e);
        		//e.printStackTrace();
        	}
        	
        	/***********
             * DEBUG 출력
             * writed by 오범석
                             System.out.println("LookupColler.java : ");
                             System.out.println("exchangers ==> " + exchangers);
             */
            if ( (exchangers_ip == null) || (exchangers_ip.size() == 0)) {
                MXList.add(hostName);
            }
            else {
                this.getMXAddresses(exchangers_ip, hostName);
            }
	            
        }
        
        
        
    }

    public void getMXAddresses(Vector elist, String hostName) {
        if ( (elist == null) || (elist.size() == 0)) {
            MXList.add(hostName);
        }
        else {
            ResourceRecord element;
            StringBuffer buf = new StringBuffer(15);
            for (int i = 0; i < elist.size(); i++) {
                element = (ResourceRecord) elist.remove(0);
                if (element != null) {
                    if (element.mx != null) {
                        MXList.add(element.mx);
                    }
                    if (element.ipAddress != null) {
                    	String ipAddress = "";
                    	for( int addr : element.ipAddress) { 
                    		ipAddress += (ipAddress.equals("") ? "" : ".") + addr;
                    	}
                    	MXList.add(ipAddress);
                    }
                }
            }
        }
    }

    public Vector getMxRecords() {
        return MXList;
    }

    public static boolean isUnknownHost() {
        return Lookup.isUnknownHost;
    }
}