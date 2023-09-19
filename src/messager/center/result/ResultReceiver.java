package messager.center.result;

import java.io.*;
import java.net.*;

import messager.center.repository.*;
import messager.common.util.*;

/**
 * Unit의 발송 결과를 Generator에서 소켓 통신으로 받는다.
 * 접속은 Generator 당 1개의 접속이 이루워 진다.
 *
 * @author Administrator
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ResultReceiver
    extends Thread
{
    /** Generator 로부터 수동적으로 접속이 이루어진 소켓 객체 */
    private Socket socket;

    /** 접속된 소켓의 입력 스트림 */
    private InputStream in;

    /** 접속된 소켓의 출력 스트림 */
    private OutputStream out;

    /** MessageHandler 객체를 얻는다. */
    private MessageMap messageMap;

    /**
     * 접속된 소켓으로 ResultReceiver 의 쓰레드를 생성한다.
     *
     * @param socket Generator로 수동적으로 접속된 Socket객체
     */
    public ResultReceiver(Socket socket) {
        this.socket = socket;
        messageMap = MessageMap.getInstance();
    }

    /**
     * 소켓의 입력 스트림을 얻는다.
     *
     * @return inputstream
     * @throws IOException
     */
    private InputStream getInputStream()
        throws IOException {
        if (in == null) {
            in = socket.getInputStream();
        }
        return in;
    }

    /**
     * 소켓의 출력 스트림을 얻는다.
     *
     * @return 출력 스트림
     * @throws IOException
     */
    private OutputStream getOutputStream()
        throws IOException {
        if (out == null) {
            out = socket.getOutputStream();
        }
        return out;
    }

    /**
     * 소켓에서 4byte를 읽어서 int형으로 변환한다.
     * 이건 발송 결과의 byte 배열의 크기이다.
     *
     * @param in 소켓의 입력 스트림
     * @return 발송 결과의 byte 배열의 크기
     * @throws IOException 입출력 에러 발생시
     */
    private int readInt(InputStream in)
        throws IOException {
        byte[] rbuf = new byte[4];
        int rc = readbytes(in, rbuf);
        if (rc != 4) {
            return -1;
        }

        return BytesUtil.bytes2int(rbuf, 0);
    }

    /**
     * 소켓에서 지정된 크기 만큼 데이타를 읽어서 지정된 크기의 byte 배열에 채운다.
     *
     * @param in 소켓의 입력 스트림
     * @param bytes 소켓에서 읽은 데이타를 채울 byte배열
     * @return 소켓에서 읽은 데이타의 byte 길이
     * @throws IOException 입출력 에러가 발생시
     */
    private int readbytes(InputStream in, byte[] bytes)
        throws IOException {
        if (bytes == null) {
            throw new NullPointerException();
        }
        int len = bytes.length;
        int off = 0;
        int rc;

        while (len > 0) {
            rc = in.read(bytes, off, len);
            if (rc == -1) {
                return -1;
            }

            off += rc;
            len -= rc;
        }
        return off;
    }

    /**
     * 소켓으로 int형의 데이터를 보낸다.
     *
     * @param out 소켓의 출력 스트림
     * @param n 소켓으로 보낼 int형의 데이타
     * @throws IOException 입출력 에러가 발생시
     */
    private void sendInt(OutputStream out, int n)
        throws IOException {
        byte[] bytes = new byte[4];

        BytesUtil.int2bytes(n, bytes, 0);
        out.write(bytes, 0, 4);
    }

    /**
     * 소켓으로 부터 읽은 발송 결과에 대한 응답 코드를 보낸다.
     *
     * @param code 응답 코드
     * @throws IOException
     */
    private void sendAccept(int code)
        throws IOException {
        OutputStream out = getOutputStream();
        sendInt(out, code);
        out.flush();
    }

    /**
     * Unit의 발송 결과를 소켓에서 읽는다.
     * 혀용되는 발송결과 의 크기는 10000byte이하이다.
     *
     * @return Unit의 발송 결과를 저장한 ResultUnit 객체
     * @throws IOException 입출력 에러가 발생시
     */
    private ResultUnit readResult()
        throws IOException {
        InputStream in = getInputStream();
        int length = readInt(in);
        if (length <= 0 && length >= 10000) {
            return null;
        }

        byte[] data = new byte[length];
        int rc = readbytes(in, data);

        if (rc != length) {
            throw new IOException();
        }

        return new ResultUnit(data);
    }

    /**
     * 쓰레드를 실행한다.
     */
    public void run() {
        try {
            while (true) {
                ResultUnit result = readResult();
                int code = 0;

                if (result != null) {
                    String messageID = result.getMessageID();
                    MessageHandler msgHandler = messageMap.lookup(messageID);
                    if (msgHandler != null) {
                        try {
                            msgHandler.writeResult(result);
                            code = 1;
                        }
                        catch (Exception ex) {
                            code = 2;
                        }
                    }
                }
                sendAccept(code);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
