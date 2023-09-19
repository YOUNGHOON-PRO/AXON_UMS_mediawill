package messager.center.repository;

import messager.common.util.BytesUtil;

/**
 * Unit의 발송 결과를 Object화 한다. 재발송을 위한 Unit의 재 조합에 사용되며, 발송 결과는 발송 실패 유무만 나타낸다. 발송
 * 결과 포맷 MsgID [32byte] : 0 UnitID [4byte] : 32 Count [2byte] : 36 SendNo
 * [2byte] : 38 SendTime [8byte] : 40 발송 결과 [Count byte] : 64
 *
 * @author Administrator TODO To change the template for this generated type
 *         comment go to Window - Preferences - Java - Code Style - Code
 *         Templates
 */
public class ResultUnit
{
    /** MsgID의 최대 길이 */
    private final static int MAX_MSG_ID_LEN = 32;

    /** MsgID가 저장된 시작 위치 */
    private final static int MSG_ID_OFFSET = 0;

    /** UnitID가 저장된 시작위치 */
    private final static int UNIT_ID_OFFSET = 32;

    /** Unit에 포함된 대상자의 수가 저장될 위치 */
    private final static int COUNT_OFFSET = 36;

    /** 발송수 가 저장된 위치 */
    private final static int SEND_NO_OFFSET = 38;

    /** SendTime이 저장된 위치 */
    private final static int SEND_TIME_OFFSET = 40;

    /** 대상자의 발송 결과가 저장된 위치 */
    private final static int CODE_OFFSET = 64;

    /** Unit의 발송 결과가 저장된 byte 배열 */
    private byte[] data;

    /** Unit의 MsgID */
    private String messageID;

    /** Unit의 UnitID */
    private int unitID;

    /** Unit에 포함된 대상자 수 */
    private int count;

    /** 발송 수 */
    private int sendNo;

    /** Unit의 발송 시간 */
    private long sendTime;

    /**
     * Unit의 발송 결과를 저장한 Byte 배열로 ResultUnit 객체를 생성한다.
     *
     * @param data Unit의 발송 결과를 저장한 Byte배열
     */
    public ResultUnit(byte[] data) {
        this.data = data;
    }

    public String getMessageID() {
        if (messageID == null) {
            int maxLen = MAX_MSG_ID_LEN;
            int off = 0;

            for (off = 0; off < maxLen; off++) {
                if (data[off] == 0) {
                    break;
                }
            }
            messageID = new String(data, 0, off);
        }
        return messageID;
    }

    /**
     * 발송된 Unit의 UnitID Unit을 구별 할 수 있는 Key이다.
     *
     * @return UnitID
     */
    public int getUnitID() {
        if (unitID == 0) {
            unitID = BytesUtil.bytes2int(data, UNIT_ID_OFFSET);
        }
        return unitID;
    }

    /**
     * 발송된 Unit에 포함된 대상자의 수를 얻는다. 대상자의 수는 컨텐츠 생성시 저장된 Unit내에 포함된 대상자의 수
     *
     * @return 대상자의 수
     */
    public int getCount() {
        if (count == 0) {
            count = BytesUtil.bytes2short(data, COUNT_OFFSET);
        }

        return count;
    }

    /**
     * 발송이전의 발송 카운트를 얻는다. 현재 발송의 재발송 회수는 제외된다.
     *
     * @return Unit에 대한 발송수
     */
    public int getSendNo() {
        if (sendNo == 0) {
            //2byte의 Byte배열에서 short 형을 얻는다.
            sendNo = BytesUtil.bytes2short(data, SEND_NO_OFFSET);
        }
        return sendNo;
    }

    /**
     * Unit의 Send Time를 얻는다. time은 System.currentTimeMillis()로 얻은 long(8byte)값
     *
     * @return Unit중 가장 최근에 발송된 발송 시간
     */
    public long getSendTime() {
        if (sendTime == 0) {
            sendTime = BytesUtil.bytes2long(data, SEND_TIME_OFFSET);
        }
        return sendTime;
    }

    /**
     * Unit에 대한 발송 결과를 저장한 Byte 배열을 얻는다.
     *
     * @return Unit에 대한 발송 결과를 저장한 Byte 배열
     */
    public byte[] getBytes() {
        return data;
    }

    /**
     * Retry 대상 여부를 확인한다. 1를 성공으로 하여 1이외의 결과값을 갖는 대상자를 추출한다.
     *
     * @param rowNo 재전송 대상인지 확인하기 위한 row 번호
     * @return Retry 대상이면 true
     */
    public boolean isRetry(int rowNo) {
        int offset = rowNo + CODE_OFFSET;
        byte value = 0;

        if (offset < data.length) {
            value = data[offset];
        }

        if (value == 1) {
            return false; //발송 성공
        }

        return true;
    }
}
