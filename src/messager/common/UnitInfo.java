package messager.common;

import java.util.ArrayList;

/**
 * 대상자들의 정보를 저장한 클래스로 지정된 수만큼 대상자들의 정보를 저장되고 작업의 진행단위이다.
 */
public class UnitInfo
    extends ArrayList
    implements java.io.Serializable
{
    private String messageID;
    private int unitID;
    private int sendNo;

    /**
     * 500명의 대상자를 저장할 수 있는 Unit를 생성한다.
     *
     * @param messageID
     *            messageID
     * @param unitID
     *            unitID
     */
    public UnitInfo(String messageID, int unitID) {
        this(messageID, unitID, 0);
    }

    /**
     * 지정된 size의 대상자를 저장할 수 있는 Unit를 생성한다.
     *
     * @param messageID unit의 MessageID
     * @param unitID unit의 UnitID
     * @param sendNo 발송 번호
     */
    public UnitInfo(String messageID, int unitID, int sendNo) {
        super();
        this.messageID = messageID;
        this.unitID = unitID;
        this.sendNo = sendNo;
    }

    public int getSendNo() {
        return sendNo;
    }

    /**
     * Unit의 MessageID를 얻는다.
     * @return 메시지 아이디
     */
    public String getMessageID() {
        return messageID;
    }

    /**
     * Unit의 UnitID를 얻는다.
     * @return 유닛 아이디
     */
    public int getUnitID() {
        return unitID;
    }

    /**
     * 지정된 인덱스에 저장된 대상자를 ReceiverInfo 객체로 얻는다.
     *
     * @param rowNo 대상자가 저장된 순서번호
     * @return 대상자의 정보를 저장한 ReceiverInfo객체
     */
    public ReceiverInfo getReceiver(int rowNo) {
        if (rowNo < size()) {
            return (ReceiverInfo) get(rowNo);
        }
        throw new IndexOutOfBoundsException("index: " + rowNo + " size: "
                                            + size());
    }

    /**
     * 대상자의 정보를 저장한다.
     *
     * @param receiverInfo 대상자의 머지 정보
     */
    public void addReceiverInfo(ReceiverInfo receiverInfo) {
        add(receiverInfo);
    }

    /**
     * Unit의 UnitName을 얻는다. UnitName은 MessageID^UnitID의 형식이다.
     *
     * @return UnitName를 MessageID^UnitID의 형식의 String를 리턴한다.
     */
    public String getName() {
        StringBuffer buffer = new StringBuffer().append(messageID).append('^')
            .append(Integer.toString(unitID));
        return buffer.toString();
    }
}
