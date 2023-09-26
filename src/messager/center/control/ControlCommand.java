package messager.center.control;

import java.io.*;

import messager.common.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
/**
 * 작업 관리자로 부터 보내온 스트림(byte배열)을 분석하여 실행할 명령 코드를 얻는다.
 * 스트림의 포맷
 * 0 ~ 1 (2byte) 명령 코드
 * 2 ~ 5 (4byte) TaskNo
 * 6 ~ 9 (4byte) SubTaskNo
 *
 * @author Administrator
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
class ControlCommand
{
	private static final Logger LOGGER = LogManager.getLogger(ControlCommand.class.getName());
	
    /** 발송 리스트 명령 코드 */
    public final static int CMD_LIST = 0;

    /** 일시 정지 명령 코드 */
    public final static int CMD_PAUSE = 1;

    /** 작업 중단 명령 코드 */
    public final static int CMD_STOP = 2;

    /** 발송 시작 명령 코드 */
    public final static int CMD_SEND = 3;

    /** 발송 정보 명령 코드 */
    public final static int CMD_INFO = 4;

    /** 종료 명령 코드 */
    public final static int CMD_QUIT = 65535;

    /** 스트림(byte[])의 길이 */
    public final static int CMD_LENGTH = 10;

    /** 스트림에서 명령 코드의 시작 위치 */
    public final static int OFFSET_CMD = 0;

    /** 스트림에서 TaskNo의 시작 위치 */
    public final static int OFFSET_TASK_NO = 2;

    /** 스트림에서 SubTaskNo의 시작 위치 */
    public final static int OFFSET_SUB_TASK_NO = 6;

    /** Command 를 읽어 와서 채울 스트림(byte배열) 버퍼  */
    public byte[] bytes;

    /** Command의 코드 번호 */
    public int command;

    /** taskNo */
    public int taskNo;

    /** SubTaskNO */
    public int subTaskNo;

    /** 객체를 생성한다 */
    public ControlCommand() {
        bytes = new byte[CMD_LENGTH];
    }

    /**
     * Command를 작업 관리자와 접속된 소켓에서 읽는다.
     *
     * @param in 작업 관리자와 접속된 소켓의 InputStream
     */
    public void readCommand(InputStream in) {
        try {
            // Command을 읽는다.
            int rc = readbytes(in);

            if (rc == -1) {
                // read count가 -1이면 접속이 종료
                command = CMD_QUIT;
                return;
            }

            //스트림에서 Command코드를  얻는다 (byte 배열에서 short형 데이타로 변환)
            command = BytesUtil.bytes2short(bytes, OFFSET_CMD);
            //스트림에서 TaskNo를 얻는다
            taskNo = BytesUtil.bytes2int(bytes, OFFSET_TASK_NO);
            //스트림에서 SubTaskNo를 얻는다.
            subTaskNo = BytesUtil.bytes2int(bytes, OFFSET_SUB_TASK_NO);

            //System.out.println("ControlCommand.readCommand() : command ==> " + command + ",taskNo ==>  " + taskNo + ",subTaskNo ==>  " + subTaskNo);
            LOGGER.info("ControlCommand.readCommand() : command ==> " + command + ",taskNo ==>  " + taskNo + ",subTaskNo ==>  " + subTaskNo);

        }
        catch (Exception ex) {
            //System.out.println("ControlCommand.readCommand() : 에러 발생 ==> " + ex.toString());
        	LOGGER.error("ControlCommand.readCommand() : 에러 발생 ==> " + ex.toString());
            command = CMD_QUIT;
        }
    }

    /**
     * 소켓의 InputStream에서 지정된 길이 (CMD_LENGTH)의 스트림(byte배열)을 읽는다.
     *
     * @param in 소켓의 InputStream
     * @return 읽은 byte의 길이(접속이 종료되었을 경우 -1)
     * @throws Exception 입출력 중 에러 발생시
     */
    private int readbytes(InputStream in)
        throws Exception {
        int len = CMD_LENGTH;
        int off = 0;
        int rc;

        //읽은 바이트수가 지정된 수만큼 읽는다.
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
}
