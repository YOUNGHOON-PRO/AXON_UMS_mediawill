package messager.mailsender.send;

import java.io.*;
import java.net.*;
import java.util.*;

import messager.mailsender.code.*;
import messager.mailsender.config.*;
import messager.mailsender.util.*;

public class ConnectManager
    extends Socket
{
    private String className = "ConnectManager";
    private InputStream iStream;
    private OutputStream oStream;
    private PrintWriter pw;
    private String mxHost = "";
    private int smtpPort = 25;

    private String response = ""; // DSN Code
    private String smtpCommand = ""; // SMTP Command
    private String errorMessage = "";
    public String initConnect_errorMsg;
    public String Connect_ErrorCode; // SMTP message stream
    private static InetAddress sendIP;

    public ConnectManager(String host, int port) {
        super();
        mxHost = host;
        smtpPort = port;
    }

    public boolean initConnect(int timeout) {
        smtpCommand = "CONNECT";
        boolean bResult = false;
        StringBuffer sb = null;

        try {
            setSoTimeout(timeout);
            setTcpNoDelay(true);
            setSoLinger(true, 1);
            setReuseAddress(true);
            setSendBufferSize(ConfigLoader.SEND_BUFFER);
            setReceiveBufferSize(ConfigLoader.READ_BUFFER);
            InetAddress sendIP = InetAddress.getByAddress(ConfigLoader.SEND_IP);
            bind(new InetSocketAddress(sendIP, 0));
            connect(new InetSocketAddress(mxHost, smtpPort), timeout);

            this.iStream = getInputStream();
            this.oStream = getOutputStream();
            pw = new PrintWriter(oStream);
            response = getResponseEx();
            if (!response.equals("220")) {
                cmdQuit();
            }
            else {
                bResult = true;
            }
        }
        catch (BindException be) {
            Connect_ErrorCode = ErrorCode.STS_BindException; // Network_BindException
            initConnect_errorMsg = be.getMessage();
            LogWriter.writeException("ConnectManager", "initConnect()", "sender.properties 의 SEND.IP를 확인해 보세요", be);
        }
        catch (UnknownHostException uhe) {
            Connect_ErrorCode = "unhost"; // Network_UnknownHostException
            initConnect_errorMsg = uhe.getMessage();
        }
        catch (NoRouteToHostException nrthe) {
            Connect_ErrorCode = ErrorCode.STS_NoRouteToHostException; // Network_NoRouteToHostException
            initConnect_errorMsg = nrthe.getMessage();
        }
        catch (ConnectException ce) {
            Connect_ErrorCode = ErrorCode.STS_ConnectException; // NetWork_ConnectException
            initConnect_errorMsg = ce.getMessage();
        }
        catch (ProtocolException pe) {
            Connect_ErrorCode = ErrorCode.STS_ConnectException; // Network_ProtocolException
            initConnect_errorMsg = pe.getMessage();
        }
        catch (MalformedURLException mue) {
            Connect_ErrorCode = ErrorCode.STS_MalformedURLException; // Network_MailformedURLException
            initConnect_errorMsg = mue.getMessage();
        }
        catch (UnknownServiceException use) {
            Connect_ErrorCode = ErrorCode.STS_UnknownServiceException; // Network_UnknownServiceException
            initConnect_errorMsg = use.getMessage();
        }
        catch (SocketTimeoutException ste) {
            Connect_ErrorCode = ErrorCode.STS_SockTimeoutException; // Network_SockTimeoutException
            initConnect_errorMsg = ste.getMessage();
        }
        catch (SocketException se) {
            Connect_ErrorCode = ErrorCode.STS_SocketException; // Network_SocketException
            initConnect_errorMsg = se.getMessage();
        }
        catch (IOException ioe) {
            Connect_ErrorCode = ErrorCode.STS_MalformedURLException; // Network_IOException
            initConnect_errorMsg = ioe.getMessage();
        }
        catch (Exception e) {
            Connect_ErrorCode = ErrorCode.STS_NetworkETC; // Network_ETC
            if (e == null) {
                initConnect_errorMsg = "Network Error";
            }
            else {
                initConnect_errorMsg = e.getMessage();
            }
        }

        return bResult;
    }

    public boolean sendLine(String command) {
        try {
            pw.print(command + "\r\n");
            pw.flush();
            return true;
        }
        catch (Exception e) {
            if (e instanceof IOException) {
                this.errorMessage = e.getMessage();
            }
            else if (e instanceof NullPointerException) {
                this.errorMessage = "Connection closed";
            }
            return false;
        }

    }

    public boolean cmdQuit() {
        if (this.isConnected()) {
            try {
                sendLine("QUIT");
                closeConnect();
                return true;
            }
            catch (Exception e) {
                closeConnect();
                LogWriter.writeException("ConnectManager", "cmdQuit()", "로그를 확인해 보세요", e);
                return false;
            }
        }
        else {
            return true;
        }
    }

    public boolean cmdHelo(String sendHost) {
        StringBuffer sb = new StringBuffer();
        boolean retVal = false;

        if (sendHost != null) {
            smtpCommand = sb.append("HELO ").append(sendHost).toString();
        }
        else {
            smtpCommand = sb.append("HELO ").append(getInetAddress().getHostName()).toString();
        }
        if (sendLine(smtpCommand)) {
            response = getResponseEx();
            if (response.equals("250")) {
                retVal = true;
            }
        }
        return retVal;
    }

    public boolean cmdMailFrom(String senderEmail) {
        boolean retVal = false;
        StringBuffer sb = new StringBuffer();
        String smtpCommand = sb.append("MAIL FROM: <").append(senderEmail).append(">").toString();

        try {
            if (sendLine(smtpCommand)) {
                response = getResponseEx();
                if (response.equals("250")) {
                    retVal = true;
                }
            }
        }
        catch (Exception e) {
            LogWriter.writeException("ConnectManger", "cmdMailFrom()", " ", e);
        }
        return retVal;
    }

    public boolean cmdRcptTo(String rcptEmail) {
        boolean retVal = false;
        StringBuffer sb = new StringBuffer();
        String smtpCommand = sb.append("RCPT TO: <").append(rcptEmail).append(">").toString();

        try {
            if (sendLine(smtpCommand)) {
                response = getResponseEx();
                if (response.equals("250") || response.equals("251")) {
                    retVal = true;
                }
            }
        }
        catch (Exception e) {
            LogWriter.writeException("ConnectManger", "cmdRcptTo()", " ", e);
        }
        return retVal;
    }

    public boolean cmdRset() {
        boolean retVal = false;
        smtpCommand = "RSET";
        try {
            if (sendLine(smtpCommand)) {
                response = getResponseEx();
                if (response.equals("250")) {
                    retVal = true;
                }
            }
        }
        catch (Exception e) {
            LogWriter.writeException("ConnectManger", "cmdRset()", " ", e);
        }
        return retVal;
    }

    public boolean cmdData() {
        boolean retVal = false;
        smtpCommand = "DATA";
        try {
            if (sendLine(smtpCommand)) {
                response = getResponseEx();
                if (response.equals("354")) {
                    retVal = true;
                }
            }
        }
        catch (Exception e) {
            LogWriter.writeException("ConnectManger", "cmdData()", " ", e);
        }
        return retVal;
    }

    public boolean cmdDataTransferComplete() {
        boolean retVal = false;

        String smtpCommand = "\r\n.";

        try {
            if (sendLine(smtpCommand)) {
                response = getResponseEx();
                if (response.equals("250")) {
                    retVal = true;
                }
            }
        }
        catch (Exception e) {
            LogWriter.writeException("ConnectManger", "cmdDataTransferComplete()", " ", e);
        }
        return retVal;
    }

    public String getConnectErrorCode() {
        return Connect_ErrorCode;
    }

    public String getErrorMessage() {
        return initConnect_errorMsg;
    }

    public void initialization() {
        Connect_ErrorCode = "";
        initConnect_errorMsg = "";
    }

    public void sendEmailData(byte[] data)
        throws IOException {
        ByteArrayInputStream bis = null;
        DataOutputStream dos = null;
        int buffsize = 2048;

        bis = new ByteArrayInputStream(data);
        dos = new DataOutputStream(oStream);
        byte[] sendbuff = new byte[buffsize];
        int readcnt = 0;
        while ( (readcnt = bis.read(sendbuff, 0, buffsize)) != -1) {
            if (readcnt < buffsize) {
                byte[] sTemp = new byte[readcnt];
                System.arraycopy(sendbuff, 0, sTemp, 0, readcnt);
                dos.write(sTemp);
                dos.flush();
                sTemp = null;
                break;
            }
            dos.write(sendbuff);
            dos.flush();
        }
        sendbuff = null;
        try {
            if (bis != null) {
                bis.close();
                bis = null;
            }
        }
        catch (Exception e) {}

    }

    public boolean readFully(StringBuffer sb)
        throws Exception {
        if (sb == null) {
            sb = new StringBuffer();
        }
        boolean bResult = false;
        DataInputStream dis = null;

        dis = new DataInputStream(this.iStream);
        String tempLine;
        while ( (tempLine = dis.readLine()) != null) {
            this.errorMessage = sb.append(tempLine).toString();
            if (tempLine.length() > 3) {
                if (tempLine.charAt(3) != '-') {
                    bResult = true;
                    break;
                }
            }
        }
        return bResult;
    }

    /**
     * SMTP명령을 서버에 보낸 다음에 서버로 부터 받은 메시지 전체를 반환한다.
     * @return SMTP의 응답 메시지
     */
    public String getResponseMessage() {
        StringTokenizer st = new StringTokenizer(this.errorMessage, "\r\n");
        int nCount = st.countTokens();
        StringBuffer sb = new StringBuffer();
        String temp = null;
        if (nCount > 0) {
            while (st.hasMoreElements()) {
                temp = sb.append(st.nextToken().trim()).toString();
            }
            this.errorMessage = temp;
        }
        else {
            return this.errorMessage;
        }

        return this.errorMessage;
    }

    public String getResponseCode() {
        /*
                   try{
         if(!this.response.equals("")){
          //int nCode = Integer.parseInt(this.response);
          //return nCode;
         }
                   }catch(NumberFormatException  e){
         return 999;
                   }
                   return 0;
         */
        if (!this.response.equals("")) {
            return this.response;
        }
        else {
            return "000";
        }
    }

    private String getResponseEx() {
        StringBuffer sb = new StringBuffer();
        this.errorMessage = "";
        String returnVal = "";

        boolean bResult = false;
        try {
            bResult = readFully(sb);
        }
        catch (Exception e) {
            returnVal = "999";
            bResult = true;
            if (e instanceof SocketTimeoutException) {
                sb.append("999 SocketTimeoutException(").append(e.getMessage()).append(")");
            }
            else if (e instanceof IOException) {
                sb.append("999 IOException(").append(e.getMessage()).append(")");
            }
            else {
                sb.append("999 Network_Error(").append(e.getMessage()).append(")");
            }
        }
        if (bResult) {
            this.errorMessage = sb.toString().trim();
            String temp = "";
            StringTokenizer st = new StringTokenizer(this.errorMessage, "\r\n");
            int nCount = st.countTokens();
            if (nCount > 0) {
                int nIndex = 0;
                String[] buffResp = new String[nCount];
                while (st.hasMoreElements()) {
                    buffResp[nIndex++] = st.nextToken().trim();
                }
                returnVal = buffResp[nCount - 1].substring(0, 3);
            }
            else {
                returnVal = "";
            }
        }
        else {
            returnVal = "";
        }
        return returnVal;
    }

    public void closeConnect() {
        try {
            if (pw != null) {
                pw.close();
            }
        }
        catch (Exception e) {}

        try {
            this.shutdownInput();
        }
        catch (Exception e) {}

        try {
            this.shutdownOutput();
        }
        catch (Exception e) {}

        try {
            close();
        }
        catch (Exception e) {}
    }
}
