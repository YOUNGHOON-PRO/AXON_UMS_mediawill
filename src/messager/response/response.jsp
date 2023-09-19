
<%@ page contentType = "text/html; charset=UTF-8"%>
<%@ page import="java.io.*"%>
<%@page import="java.util.*" %>
<%@page import="java.util.Date" %>
<%@page import="java.text.SimpleDateFormat" %>

<%
	//파일경로
	String responseFolder = "E:\\JAVA\\workspace_oxygen\\AXON_UMS\\response";

	//파일명
	String responseFileeName = "ResponseConfirmInfo.log";
	
	String qry_str = request.getQueryString();
	
	qry_str = qry_str.replaceAll("&&","|");
	
	StringTokenizer stk = new StringTokenizer(qry_str,"|");
	
	String[] arr = new String[10];
	int idx = 0;
	
	while(stk.hasMoreTokens()){
		arr[idx] = stk.nextToken();
		idx++;
	}
	
	Date now = new Date();
	SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmss");
	String today = sf.format(now);
	sf = new SimpleDateFormat("yyyyMMddHHmm");
	today = sf.format(now);

	//<img src='"+util.getEnv("RESPONSE_URL")+"?$:RESP_END_DT:$&&000&&"+p_id_merge+"&&$:TASK_NO:$&&$:SUB_TASK_NO:$&&$:DEPT_NO:$&&$:USER_ID:$&&$:CAMP_TY:$&&$:CAMP_NO:$&&$:TARGET_GRP_TY:$
	//202109092137``000``test1``310``1``1``ADMIN``003``1``$:TARGET_GRP_TY:$
	//http://localhost:8080/response.jsp?202108311809|000|test1|315|1|1|ADMIN|003|1|000
	//http://211.55.75.242:9090/ems/resp/response2.jsp?202108311809|000|test3|317|1|1|ADMIN|003|1|000
			
	//String receiveUsrInfo = today+"``"+arr[1]+"``"+arr[2]+"``"+arr[3]+"``"+arr[4]+"``"+arr[5]+"``"+arr[6]+"``"+arr[7]+"``"+arr[8]+"``"+arr[9]+"\r\n";
	String receiveUsrInfo = today+"``"+arr[1]+"``"+arr[2]+"``"+arr[3]+"``"+arr[4]+"``"+arr[5]+"``"+arr[6]+"``"+arr[7]+"``"+arr[8]+"``000"+"\r\n";
	System.out.println("수신확인 : " +receiveUsrInfo);

	FileWriter fw = new FileWriter(responseFolder+File.separator+responseFileeName,true);
	fw.write(receiveUsrInfo);
	fw.close();

%>

 
