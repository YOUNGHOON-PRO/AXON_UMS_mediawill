package messager.generator.send;

import java.io.*;
import java.net.*;

import messager.common.util.*;
import messager.generator.config.*;
import messager.generator.repository.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 발송 완료된 Unit에 대한 발송 결과 파일을 MessageCenter로 전송한다. 발송 결과 파일들을 로딩 한 후 Unit에 대한 발송
 * 완료를 확인하여 전송한다. Unit에 대한 발송 완료 확인은 UnitEnvelope 객체파일의 존재유무로 판단한다. 통신 send : 발송
 * 결과 파일의 길이(4byte) 발송결과 파일(byte[]) read : 발송 결과 파일의 길이(4byte)
 */

public class UnitLogSender
    extends Thread
{
	private static final Logger LOGGER = LogManager.getLogger(UnitLogSender.class.getName());
	

    private static UnitLogSender instance;

    public static void executeThread() {
        synchronized (UnitLogSender.class) {
            if (instance == null) {
                instance = new UnitLogSender();
            }

            if (!instance.isAlive()) {
                instance.start();
            }
        }
    }

    private String centerHost;
    private int connectPort;
    private long unitLogFetchPeriod;

    private UnitLogSender() {
        centerHost = ConfigLoader.getString("center.host", null);
        connectPort = ConfigLoader.getInt("result.connect.port", -1);
        if (connectPort == -1) {
            System.err.println("result.connect.port : " + connectPort);
            System.exit(1);
        }

        unitLogFetchPeriod = ConfigLoader.getInt("result.period", (5 * 60)) * 1000;
    }

    private void threadSleep() {
        try {
            sleep(unitLogFetchPeriod);
        }
        catch (Exception ex) {LOGGER.error(ex);}
    }

    public void run() {
        while (true) {
            unitLogSend();
            threadSleep();
        }
    }

    private void unitLogSend() {
        SendConnection connection = null;
        try {
            connection = new SendConnection(centerHost, connectPort);

            while (true) {
                //UnitLog File List Load
                String[] unitList = UnitResultFile.list();
                if(unitList != null && unitList.length > 0) {
                    sendList(connection, unitList);
                }
                threadSleep();
            }
        }
        catch (Exception ex) {
        	LOGGER.error(ex);
            //System.err.println("UnitLogSender : " + ex.getMessage());
        }
        finally {
            if (connection != null) {
                connection.close();
                connection = null;
            }
        }
    }

    private void sendList(SendConnection connection, String[] unitList)
        throws Exception {
    	
    	for (int i = 0; i < unitList.length; i++) {
    		String unitName = unitList[i];
            if (!access(unitName)) {
                // UnitEnvelope 객체 파일이 존재하는지 확인해서 존재 하지 않으면
                // MessageCenter로 발송결과 파일을 전송한다.
                UnitResult unitResult = new UnitResult(unitName);
                byte[] data = unitResult.getBytes();
        
                if (data.length > 64) {
                            if (send(connection, unitName, data)) {
                        boolean success = UnitResultFile.delete(unitName);
                        if (!success) {
                            //System.out.println("UnitLog Delete Fail 1: " + unitName);
                        	LOGGER.info("UnitLog Delete Fail 1: " + unitName);
                        }
                    }
                    else {
                        //System.out.println("UnitLog Send Fail: " + unitName);
                        LOGGER.info("UnitLog Send Fail: " + unitName);
                        
                        //추가  --------------------------------------------------------
                        //System.out.println("UnitLog Send Fail: " + unitName +" => delete start");
                        LOGGER.info("UnitLog Send Fail: " + unitName +" => delete start");
                        
                        boolean success = UnitResultFile.delete(unitName);
                        //System.out.println("UnitLog Send Fail: " + unitName +" => delete complete");
                        LOGGER.info("UnitLog Send Fail: " + unitName +" => delete complete");
                        
                        if (!success) {
                            //System.out.println("UnitLog Delete Fail 2: " + unitName);
                        	LOGGER.info("UnitLog Delete Fail 2: " + unitName);
                        }
                        //  ----------------------------------------------------------
                    }
                }
                
                //추가  --------------------------------------------------------
                if (data.length == 0) {
                	//System.out.println("UnitLog Data : " + data.length + " " + unitName +" => delete start");
                	LOGGER.info("UnitLog Data : " + data.length + " " + unitName +" => delete start");
                	
                	boolean success2 = UnitResultFile.delete(unitName);
                	//System.out.println("UnitLog Data : " + data.length + " " + unitName +" => delete complete");
                	LOGGER.info("UnitLog Data : " + data.length + " " + unitName +" => delete complete");
                	
                	if (!success2) {
                        //System.out.println("UnitLog Delete Fail 3: " + unitName);
                		LOGGER.info("UnitLog Delete Fail 3: " + unitName);
                    }
                }
                //  ----------------------------------------------------------
            }
        }
    }

    /**
     * 발송 완료를 확인 하기 위하여 UnitEnvelope 객체 파일의 존재유무를 확인한다.
     *
     * @param unitName
     *            MessageID^UnitID
     * @return UnitEnvelope 객체 파일이 존재하면 true
     */
    private boolean access(String unitName) {
        return SendUnitFile.exists(unitName);
    }

    /**
     * 발송 결과 파일 전송
     *
     * @param logFile
     *            발송 결과 파일의 File객체
     * @return 발송 결과 파일의 길이
     */
    private boolean send(SendConnection connection, String unitName, byte[] bytes)
        throws IOException {
        connection.sendInt(bytes.length);
        connection.sendBytes(bytes, 0, bytes.length);
        connection.flush();
        return accept(connection);
    }

    /**
     * MessageCenter로 부터 확인 메세지(발송 결과 파일의 길이)를 읽는다
     *
     * @return 발송 결과 파일의 길이
     */
    private boolean accept(SendConnection connection)
        throws IOException {
        int size = connection.readInt();
        if (size > 0) {
            return true;
        }
        else {
            return false;
        }
    }

    class SendConnection
    {
        private Socket socket;
        private InputStream in;
        private OutputStream out;

        public SendConnection(String host, int port)
            throws IOException {
            socket = new Socket(host, port);
        }

        private InputStream getInputStream()
            throws IOException {
            if (in == null) {
                in = socket.getInputStream();
            }
            return in;
        }

        private OutputStream getOutputStream()
            throws IOException {
            if (out == null) {
                out = socket.getOutputStream();
            }
            return out;
        }

        public void close() {
            if (in != null) {
                try {
                    in.close();
                }
                catch (IOException ex) {LOGGER.error(ex);}
                in = null;
            }
            if (out != null) {
                try {
                    out.close();
                }
                catch (IOException ex) {LOGGER.error(ex);}
                out = null;
            }
            if (socket != null) {
                try {
                    socket.close();
                }
                catch (IOException ex) {LOGGER.error(ex);}
                socket = null;
            }
        }

        /**
         * Socket에 4byte int형의 Number를 전송한다.
         * Number는 4byte의 int형으로 big endian에 따라 byte배열로 변환한다.
         *
         * @param n 소켓에 보낼 int형의 Number
         * @exception Socket에 데이타를 보낼때 IO 에러가 발생할 경우
         */
        public void sendInt(int n)
            throws IOException {
            OutputStream out = getOutputStream();
            byte[] buf = new byte[4];

            //int를 byte배열로 변환한다.
            BytesUtil.int2bytes(n, buf, 0);
            out.write(buf, 0, 4);
            buf = null;
        }

        /**
         * Socket에서 4byte의 Int형 Number를 읽는다.
         *
         * @return Socket에 읽은 Int형의 Number
         */
        public int readInt()
            throws IOException {
            InputStream in = getInputStream();
            byte[] bytes = new byte[4];
            int rc = 0;
            int len = 4;
            int offset = 0;

            //읽은 byte가 4가 될때까지 읽는다.
            while (offset < 4) {
                rc = in.read(bytes, offset, len);
                if (rc == -1) {
                    return -1;
                }
                offset += rc;
                len -= rc;
            }
            // byte배열 -> int
            return BytesUtil.bytes2int(bytes, 0);
        }

        public void sendBytes(byte[] data, int off, int len)
            throws IOException {
            OutputStream out = getOutputStream();
            out.write(data, off, len);
        }

        public void flush()
            throws IOException {
            OutputStream out = getOutputStream();
            out.flush();
        }
    }
}
