package messager.generator.agent;

import java.io.*;
import java.net.*;
import java.util.*;

import messager.common.*;
import messager.generator.content.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WebAction
    extends Action
{
	private static final Logger LOGGER = LogManager.getLogger(WebAction.class.getName());
	
    private final static String lineSeparator = "\r\n";

    private final static String urlKey = "web.url";

    private Template urlTemplate;

    private int id;

    protected void init()
        throws AgentException {
        try {
            HashMap parameters = (HashMap) agent.getParameters();
            urlTemplate = (Template) parameters.get(urlKey);
            id = agent.getID();
        }
        catch (Exception ex) {
        	LOGGER.error(ex);
            throw new AgentException(ErrorCode.CONTENT_INVALID, ex.getMessage());
        }
    }

    public void create(ReceiverInfo receiver)
        throws AgentException {
        String url = null;

        try {
            url = urlTemplate.createURL(receiver);
        }
        catch (Exception ex) {
        	LOGGER.error(ex);
            throw new AgentException(ErrorCode.MERGE_ERROR, ex.getMessage());
        }

        String webContents = fetch(url);
        receiver.putAgent(id, webContents);
    }
    
    public void create2(ReceiverInfo receiver)
            throws AgentException {
            String url = null;

            try {
                url = urlTemplate.createURL(receiver);
            }
            catch (Exception ex) {
            	LOGGER.error(ex);
                throw new AgentException(ErrorCode.MERGE_ERROR, ex.getMessage());
            }

            String webContents = fetch(url);
            receiver.putAgent2(id, webContents);
        }
    

    public void release() {
    }

    private String fetch(String http_url)
        throws AgentException {
        BufferedReader reader = null;
        Exception exception = null;
        StringBuffer buffer = new StringBuffer();
        URL url = null;

        String retval = null;

        try {

            //&amp; 부분을 & 으로 변경한다.
            http_url = http_url.replaceAll("&amp;", "&");   

            url = new URL(http_url);  // 웹에서 에이전트 + 머지 사용  방식 : ^:http://localhost:8080/name.jsp?name=$:NAME:$&name2=$:ID:$&name3=$:AGE:$:^
            int responseCode = 0;
            URLConnection urlConnection = url.openConnection();
            responseCode = ( (HttpURLConnection) urlConnection)
                .getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new AgentException(ErrorCode.HTTP_RESPONSE_ERROR,
                                         "ResponseCode = " + responseCode);
            }

            String content_type = urlConnection.getContentType();

            //charset 을 추출한다.
            int charset_pos = (content_type.toLowerCase()).indexOf("charset");

            String charset = null;

            if (charset_pos < 0) {
                //기본 charset 으로 설정
                charset = "EUC-KR";
            }
            else {
                charset = content_type.substring(charset_pos);
                charset = charset.substring(charset.indexOf("=") + 1);
                int deli = charset.indexOf(";");
                if (deli > 0) {
                    charset = charset.substring(0, deli);
                }
                charset = charset.trim();
            }

            InputStream is = urlConnection.getInputStream();

            //System.out.println("CONTENT TYPE ==>  " + content_type);
            //System.out.println("CHARSET ==>  " + charset);

            reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

            String line;
            String lowerLine;
            while ( (line = reader.readLine()) != null) {
                lowerLine = line.toLowerCase();
                if ( (lowerLine.indexOf("<!--nodata-->") != -1)
                    || (lowerLine.indexOf("micronsoft ole db provider") != -1 && lowerLine
                        .indexOf("error") != -1)
                    || (lowerLine
                        .indexOf("microsoft vbscript runtime error") != -1)) {
                    throw new AgentException(
                        ErrorCode.WEB_CONTENT_SCRIPT_ERROR, line);
                }
                buffer.append(line).append(lineSeparator);
            }

            retval = buffer.toString();
            
        }
        catch (Exception ex) {
        	LOGGER.error(ex);
            exception = ex;
        }
        finally {
            if (reader != null) {
                try {
                    reader.close();
                }
                catch (Exception ex) {
                	LOGGER.error(ex);
                }
                reader = null;
            }
        }
        if (exception != null) {
            if (exception instanceof AgentException) {
                throw (AgentException) exception;
            }
            else if (exception instanceof MalformedURLException) {
                throw new AgentException(ErrorCode.MALFORM_URL, exception
                                         .getMessage());
            }
            else {
                throw new AgentException(ErrorCode.WEB_ERROR, exception
                                         .getMessage());
            }
        }

        return retval;

    }
}
