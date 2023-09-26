package messager.center.creator;

import java.io.*;
import java.net.*;

import messager.center.config.*;
import messager.common.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Promotion 에 포함된 템플릿, 첨부파일 리스트, Address 파일등을 FileTransfer에 접속하여 전송 받는다.
 * Message당 하나의 Connection으로 여러 파일들을 전달 받는다. 요청시에는 요청 File의 경로의 byte[]
 * 길이(4byte), File의 경로(byte[]) 응답은 데이타의 byte[] 길이 (4byte),
 * 실제데이타(ResponseStream으로 읽는다.)
 */
public class FileRequester
{
	
	private static final Logger LOGGER = LogManager.getLogger(FileRequester.class.getName());
	
    private static String host; //Promotion이 위치한 Host
    private static int port; //File Transfer의 서비스 포트번호

    static {
        host = ConfigLoader.getProperty("file.request.host");
        String str = ConfigLoader.getProperty("file.request.port");
        if (str != null) {
            try {
                port = Integer.parseInt(str);
            }
            catch (NumberFormatException ex) {
            	LOGGER.error(ex);
            }
        }
    }

    private Socket socket;
    private InputStream in;
    private OutputStream out;

    /**
     * FileTransfer와 Socket으로 접속하여 FileRequester 객체를 생성한다.
     */
    public FileRequester()
        throws IOException {
        //Socket open
        socket = new Socket(host, port);
    }

    /**
     * Socket 객체에서 데이타를 읽기위해 InputStream객체를 얻는다.
     *
     * @return Socket에서 얻은 InputStream 객체를 BufferedInputStream으로 변환한 객체
     */
    private InputStream getInputStream()
        throws IOException {
        if (in == null) {
            in = new BufferedInputStream(socket.getInputStream());
        }
        return in;
    }

    /**
     * Socket 객체에 데이타를 쓰기위해 OutputStream 객체를 얻는다.
     *
     * @return Socket에서 얻은 OutputStream객체를 BufferedOutputStream으로 변환한 객체
     */
    private OutputStream getOutputStream()
        throws IOException {
        if (out == null) {
            out = new BufferedOutputStream(socket.getOutputStream());
        }
        return out;
    }

    /**
     * FileTransfer에 file 요청하여 ResponseStream으로 응답을 받는다.
     *
     * @param filePath
     *            요청할 파일의 경로
     * @return InputStream ResponseStream
     */
    public InputStream request(String filePath)
        throws IOException {
        try {
            sendRequest(filePath);
            return responseStream();
        }
        catch (Exception ex) {
        	LOGGER.error(ex);
            //ex.printStackTrace();
            throw (IOException) ex;
        }
    }

    /**
     * FileTransfer에 File을 요청 한다.
     *
     * @param filePath
     *            요청할 파일의 경로
     */
    private void sendRequest(String filePath)
        throws IOException {

//debug
//     System.out.println("FileRequester:" + filePath);
        OutputStream stream = getOutputStream();
        //filePath를 byte[]로 변환
        byte[] databytes = filePath.getBytes();
        int len = databytes.length;
        //byte[]로 변환된 filePath의 길이
        byte[] sizebytes = new byte[4];

        //길이를 byte[]로 변환
        BytesUtil.int2bytes(len, sizebytes, 0);

        //길이를 보낸다.
        stream.write(sizebytes, 0, 4);
        stream.flush();
        //byte[]로 변환된 filePath를 보낸다.
        stream.write(databytes, 0, len);
        stream.flush();
    }

    /**
     * FileTransfer에서 응답의 길이(File의 byte 길이)를 읽는다.
     *
     * @return 파일의 길이
     */
    private int readsize()
        throws IOException {
        InputStream stream = getInputStream();
        byte[] bytes = new byte[4];
        int off = 0;
        int rc;

        while (off < 4) {
            rc = stream.read(bytes, off, 4);
            if (rc == -1) {
                return -1;
            }
            off += rc;
        }

        return BytesUtil.bytes2int(bytes, 0);
    }

    /**
     * FileTransfer에서 요청에 대한 응답을 InputStream으로 얻는다.
     *
     * @return InputStream ResponseStream
     */
    private InputStream responseStream()
        throws IOException {
        int size = readsize();
        if (size > 0) {
            //ResponseStream 객체 생성
            return new ResponseStream(getInputStream(), size);
        }
        return null;
    }

    /**
     * Socket close
     */
    public void close() {
        if (out != null) {
            try {
                out.close();
            }
            catch (IOException ex) {
            	LOGGER.error(ex);
            }
            out = null;
        }

        if (in != null) {
            try {
                in.close();
            }
            catch (IOException ex) {
            	LOGGER.error(ex);
            }
            in = null;
        }

        if (socket != null) {
            try {
                socket.close();
            }
            catch (IOException ex) {
            	LOGGER.error(ex);
            }
            socket = null;
        }
    }

    public static void main(String[] args)
        throws Exception {
        ConfigLoader.load();
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String line;
        FileRequester requester = new FileRequester();

        while ( (line = in.readLine()) != null) {
            requester.sendRequest(line);
            InputStream stream = requester.responseStream();
            byte[] buf = new byte[1024];
            int rc;
            while ( (rc = stream.read(buf, 0, 1024)) != -1) {
                ;
            }
        }
    }
}
