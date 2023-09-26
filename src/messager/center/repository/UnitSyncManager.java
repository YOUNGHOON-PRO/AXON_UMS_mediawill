package messager.center.repository;

import java.io.*;
import java.util.*;

import messager.center.config.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Unit의 대한 동기화 파일을 Message 단위로 관리하기 위한 객체이다. 동기화 파일은 Unit의 load가 된 시점부터
 * Delivery될때까지 생성되었다가 Delivery가 완료되면 제거된다. Message의 동기화 디렉토리는
 * MessageID_RetryCount의 이름으로 생성된다. 동기화 파일은 UnitID로 된 파일명으로 생성된다.
 * ${work.path}/sync_unit/messageID_retryCount/unitID
 */
public class UnitSyncManager
{
	private static final Logger LOGGER = LogManager.getLogger(UnitSyncManager.class.getName());
	
    private static final String dirName = "sync_unit";

    private static File dirFile;

    static {
        String path = ConfigLoader.getString("work.path", "repository");
        dirFile = new File(path, dirName);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
    }

    /**
     * Message에 대한 Unit의 동기화 파일을 저장하는 디렉토리의 File객체
     */
    private File msgDirFile;

    /**
     * UnitSyncManager 객체를 생성한다. Message에 포함되는 Unit의 동기화 파일을 저장하는 디렉토리의 File객체를
     * 생성한다. 디렉토리 명은 MessageID_retryCount 이다.
     *
     * @param messageID
     *            Message의 messageID
     * @param retryCnt
     *            Message의 retryCount
     */
    UnitSyncManager(String messageID) {
        String msgDirName = messageID;
        msgDirFile = new File(dirFile, msgDirName);
    }

    /**
     * Unit에 대한 동기화 파일을 생성한다.
     *
     * @param unitID
     *            Unit의 UnitID
     * @exception RepositoryException
     *                이미 존재 하거나, 파일 생성을 실패 하면 발생
     */
    void create(int unitID)
        throws RepositoryException {
        create(Integer.toString(unitID));
    }

    /**
     * Unit에 대한 동기화 파일을 생성한다.
     *
     * @param unitID
     *            Unit의 UnitID
     * @exception RepositoryException
     *                이미 존재하거나, 파일 생성을 실패 하면 발생
     */
    void create(String unitID)
        throws RepositoryException {
        if (!msgDirFile.exists()) {
            msgDirFile.mkdirs();
        }
        File unitSyncFile = new File(msgDirFile, unitID);

        if (unitSyncFile.exists()) {
            throw new RepositoryException(unitSyncFile
                                          + "Already exists SyncFile");
        }
        try {
            unitSyncFile.createNewFile();
        }
        catch (Exception ex) {
        	LOGGER.error(ex);
            throw new RepositoryException(ex.getMessage());
        }
    }

    /**
     * Unit에 대한 동기화 파일을 삭제한다.
     *
     * @param unitID
     *            삭제하려고 하는 Unit의 UnitID
     */
    boolean remove(int unitID) {
        return remove(Integer.toString(unitID));
    }

    /**
     * Unit에 대한 동기화 파일을 삭제한다.
     *
     * @param unitID
     *            삭제하려고 하는 Unit의 UnitID
     */
    boolean remove(String unitID) {
        if (msgDirFile.exists()) {
            File unitSyncFile = new File(msgDirFile, unitID);
            return unitSyncFile.delete();
        }
        return true;
    }

    /**
     * Message에 대한 Unit의 동기화 파일 리스트를 리턴한다.
     *
     * @retrun ArrayList 동기화를 위해 생성된 파일이 가르키는 Unit의 ID리스트
     */
    public ArrayList unitList() {
        if (!msgDirFile.exists()) {
            return null;
        }
        String[] array = msgDirFile.list();
        ArrayList list = new ArrayList();
        for (int i = 0; i < array.length; i++) {
            list.add(array[i]);
        }
        return list;
    }

    /**
     * Message에 대한 Unit의 동기화 파일을 저장한 디렉토리를 삭제한다.
     *
     * @param flag
     *            true일 경우 디렉토리에 파일이 존재하여도 삭제한다.
     * @return boolean 삭제를 성공하면 true를 리턴
     */
    boolean remove(boolean flag) {
        boolean success = true;
        if (msgDirFile.exists()) {
            String[] list = msgDirFile.list();
            if (flag) {
                for (int i = 0; i < list.length; i++) {
                    File syncFile = new File(msgDirFile, list[i]);
                    syncFile.delete();
                }
            }
            else {
                if (list.length > 0) {
                    success = false;
                }
            }
            success = msgDirFile.delete();
        }
        return success;
    }

    public boolean isEmpty() {
        boolean empty = true;
        if (msgDirFile.exists()) {
            String[] list = msgDirFile.list();
            if (list.length > 0) {
                empty = false;
            }
        }
        return empty;
    }

    /*
     * public static boolean exists(String messageID) { File messageDirFile =
     * new File(dirFile, messageDirName); return messageDirFile.exists(); }
     */
}
