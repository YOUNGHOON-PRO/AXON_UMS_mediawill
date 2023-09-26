package messager.center.creator;

import java.util.*;

import messager.center.config.*;
import messager.center.repository.*;
import messager.common.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 이 추상 클래스는 대상자를 가져오는 클래스의 수퍼 클래스이다. 대상자를 가져오는 클래스는 DB에서 대상자를 가져오는
 * DBSendToFetcher과 AddressFile에서 대상자를 가져오는 FileSendToFetcher클래스가 있다.
 */
public abstract class SendToFetcher
{
	private static final Logger LOGGER = LogManager.getLogger(SendToFetcher.class.getName());
	
    //하나의 UnitInfo객체에 포함될수 있는 대상자의 수
    protected static int receiversPerUnit;

    static {
        receiversPerUnit = ConfigLoader.getInt("receivers.per.unit", 500);
    }

    protected Message message;

    //대상자 총 수
    protected int receiverCount;

    //마지막 UnitID
    protected int lastUnitID;

    //대상자의 추출될 MergeKey의 List
    protected ArrayList columnList;

    protected int unitCount;

    protected UnitFileManager unitFileMgr;

    /**
     * SendToFetcher
     */
    public SendToFetcher(Message message) {
        this.message = message;
        unitFileMgr = new UnitFileManager(message.messageID);
    }

    /**
     * 대상자의 총 수를 얻는다.
     *
     * @return receiverCount 대상자의 총수로 대상자를 가져온 후 설정된다.
     */
    public int getReceiverCount() {
        return receiverCount;
    }

    /**
     * 마지막 Unit의 UnitID를 얻는다.
     *
     * @return lastUnitID 마지막 생성된 UnitInfo 객체의 UnitID
     */
    public int getLastUnitID() {
        return lastUnitID;
    }

    public int getUnitCount() {
        return unitCount;
    }

    /**
     * UnitInfo객체를 repository 객체로 저장한다.
     *
     * @param repository
     *            UnitInfo 객체를 저장될 저장소를 관리한다.
     * @param unitInfo
     *            현재 저장될 UnitInfo 객체
     * @exception CreatorException
     */
    protected void writeUnit(UnitInfo unit)
        throws FetchException {
        try {
            //lastUnitID를 저장될 UnitID로 설정 한다.
            lastUnitID = unit.getUnitID();
            unitCount++;

            //repository 객체로 UnitInfo 객체를 파일에 저장한다.
            unitFileMgr.writeUnit(unit);
        }
        catch (Exception ex) {
        	LOGGER.error(ex);
            String detail = "[" + message.messageID + "] " + ex.getMessage();
            throw new FetchException(detail, ErrorCode.REPOSITORY_EXCEPTION);
        }
    }

    /**
     * 대상자의 MergeKey의 List를 얻는다.(현재 사용되지 않는다)
     *
     * @return columnList 대상자의 MergeKey List
     */
    public ArrayList getColumnList() {
        return columnList;
    }

    /**
     * 대상자들을 가져와서 UnitInfo 객체를 생성한 후 저장한다.
     *
     * @param repository
     *            UnitInfo 객체를 저장할 때 사용되는 MsgUnitManager 객체
     */
    public abstract void fetch()
        throws FetchException;

    /**
     * 대상자 추출에 사용된 더이상 사용되지 않을 DB의 Connection 이나 File Address Stream를 닫는다.
     */
    protected abstract void close();

    /**
     * close 를 실행 한 후 Message의 상태를 나타내는 MessageStatus 객체에 마지막 UnitID, 총 Unit수, 총
     * 대상자 수를 설정한다.
     *
     * @param msgStatus
     *            Message의 상태를 관리한다.
     */
    public void release() {
        close();
    }
}
