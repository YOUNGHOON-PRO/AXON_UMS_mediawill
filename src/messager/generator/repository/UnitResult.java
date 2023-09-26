package messager.generator.repository;

import java.io.*;

import messager.common.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Unit에 대한 발송 결과를 스트림(byte배열)에 저장하거나 읽어온다.
 * 발송 결과는 대상자의 발송 성공 유무와 Unit의 발송 완료 시각이 저장된다.
 * 헤더의 길이는 64바이트로 unit의 구분 정보와 대상자의 수, 발송 완료 시각이 저장되고
 * 대상자의 발송 성공 유무는 대상자의 rowID순으로 64바이트 이후부터 1byte로 저장된다.
 * 발송 성공은 1로 표현 되고 기타 코드는 발송 실패(컨텐츠 생성실패)이다.
 */
public class UnitResult
{
	
	private static final Logger LOGGER = LogManager.getLogger(UnitResult.class.getName());
	
    /** MessageID의 최대 길이 */
    private final static int MAX_MSG_ID_LEN = 32;

    /** MessageID를 스트림에 저장할 위치 */
    private final static int MSG_ID_OFFSET = 0;

    /** UnitID를 스트림에 저장할 위치 */
    private final static int UNIT_ID_OFFSET = 32;

    /** Unit내에 포함된 대상자의 수를 스트림에 저장할 위치 */
    private final static int COUNT_OFFSET = 36;

    /** 재발송 시작 횟수를 스트림에 저장할 위치 */
    private final static int SEND_NO_OFFSET = 38;

    /** Unit의 발송 완료 시각을 스트림에 저장할 시작 위치 */
    private final static int SEND_TIME_OFFSET = 40;

    /** 대상자의 발송 성공 유무를 스트림에 저장할 시작위치(헤더의 길이) */
    private final static int RECORD_OFFSET = 64;

    /** 발송 결과를 저장할 스트림 (byte배열) */
    private byte[] bytes;

    /**
     * Unit에 대한 발송 결과(컨텐츠 생성결과)를 저장하기 위해 객체를 생성한다.
     *
     * @param messageID MessageID
     * @param unitID	unitID
     * @param count		Unit내 포함된 대상자의 수
     * @param sendNo	retryCount시작 번호
     */
    public UnitResult(String messageID, int unitID, int count, int sendNo) {
        int len = RECORD_OFFSET + count;
        bytes = new byte[len];
        byte[] bytesMsgID = messageID.getBytes();
        System.arraycopy(bytesMsgID, 0, bytes, 0, bytesMsgID.length);
        BytesUtil.int2bytes(unitID, bytes, UNIT_ID_OFFSET);
        BytesUtil.short2bytes( (short) count, bytes, COUNT_OFFSET);
        BytesUtil.short2bytes( (short) sendNo, bytes, SEND_NO_OFFSET);
    }

    /**
     * UnitName으로 Unit에 대한 발송 결과 파일을 읽어서 객체를 생성한다.
     * 발송 완료된 Unit의 발송 결과를 MessageCenter에 전송할때 사용된다.
     *
     * @param unitName
     */
    public UnitResult(String unitName) {
        bytes = readFromFile(unitName);
    }

    /**
     * 대상자의 발송 결과(컨텐츠 생성 실패)를 대상자의 Unit내의 키로 사용되는 RowID를 이용해
     * 지정된 위치에 저장한다. (컨텐츠 성공시 저장하지 않는다)
     * 저장할 위치는 64(헤더 길이) + rowID
     *
     * @param rowID Unit내에서 대상자를 구분 하기 위해 부여된 순서번호
     * @param code 컨텐츠 실패 코드
     */
    public void putRecord(int rowID, int code) {
        int offset = RECORD_OFFSET + rowID;
        bytes[offset] = (byte) code;
    }

    /**
     * 발송 결과를 파일에 write한다.
     * 파일에 write하는 시점은 Unit에 포함된 대상자들의 컨텐츠를 생성후
     * Unit에 대한 SendUnit 객체 파일을 생성후 write한다.
     *
     * @param unitName Unit를 구분하는 키(MessageID^UnitID)로 파일 명으로 사용된다.
     */
    public void writeToFile(String unitName) {
        FileOutputStream out = null;
        long time = System.currentTimeMillis();
        BytesUtil.long2bytes(time, bytes, SEND_TIME_OFFSET);

        try {
            File file = UnitResultFile.unitFile(unitName);  //unitlog 로그 파일 생성
            out = new FileOutputStream(file);
            out.write(bytes, 0, bytes.length);
        }
        catch (Exception ex) {
        	LOGGER.error(ex);
            //ex.printStackTrace();
        }
        finally {
            if (out != null) {
                try {
                    out.close();
                }
                catch (Exception ex) {
                	LOGGER.error(ex);
                }
                out = null;
            }
        }
    }

    /**
     * unitName으로 Unit의 발송 결과를 파일에서 읽는다.
     *
     * @param unitName unit를 구분하는 키 (MessageID^UnitID)
     * @return 발송 결과를 저장한 스트림
     */
    private byte[] readFromFile(String unitName) {
        FileInputStream in = null;
        byte[] bytes = null;

        try {
            File file = UnitResultFile.unitFile(unitName);
            int len = (int) file.length();
            bytes = new byte[len];
            in = new FileInputStream(file);
            in.read(bytes, 0, len);
        }
        catch (Exception ex) {
        	LOGGER.error(ex);
            //ex.printStackTrace();
            bytes = null;
        }
        finally {
            if (in != null) {
                try {
                    in.close();
                }
                catch (Exception ex) {
                	LOGGER.error(ex);
                }
                in = null;
            }
        }
        return bytes;
    }

    /**
     * Unit의 발송 결과를 스트림 형태로 얻는다.
     *
     * @return 발송 결과를 저장한 스트림
     */
    public byte[] getBytes() {
        return bytes;
    }
}
