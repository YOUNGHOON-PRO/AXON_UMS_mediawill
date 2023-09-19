package messager.center.repository;

import java.io.*;

import messager.center.config.*;
import messager.common.*;
import messager.common.util.*;

public class UnitFileManager
{
    private final static String dirName = "unit";

    private static File dirFile;
    static {
        String workPath = ConfigLoader.getString("work.path", "repository");
        dirFile = new File(workPath, dirName);
        if (!dirFile.exists()) {
            if (!dirFile.mkdirs()) {
                System.err.println("디렉토리를 생성 할수 없음 : " + dirFile);
                System.exit(1);
            }
        }
        else if (!dirFile.isDirectory()) {
            System.err.println("디렉토리가 아님 : " + dirFile);
            System.exit(1);
        }
    }

    private String messageID;

    public UnitFileManager(String messageID) {
        this.messageID = messageID;
    }

    /**
     * Unit을 저장소에 write한다.
     *
     * @param unit 저장할려고 하는 UnitInfo객체
     * @exception RepositoryException Message에 대한 디렉토리가 생성되지 않거나 입출력 에러가 발생할 경우
     */
    public void writeUnit(UnitInfo unit)
        throws RepositoryException {
        int sendNo = unit.getSendNo();
        String dirName = messageID + "-" + Integer.toString(sendNo);
        File msgDirFile = new File(dirFile, dirName);
        if (!msgDirFile.exists()) {
            msgDirFile.mkdirs();
        }
        int unitID = unit.getUnitID();

        File uFile = new File(msgDirFile, Integer.toString(unitID));
        try {
            ObjectFile.writeObject(uFile, unit);
        }
        catch (Exception ex) {
            throw new RepositoryException(ex.getMessage());
        }
    }

    /**
     * 지정된 UnitID로 저장소에서 Unit를 읽는다.
     *
     * @param unitID 읽을려고 하는 Unit의 UnitID
     * @return UnitInfo UnitInfo 객체
     * @exception RepositoryException
     *                저장소(디렉토리)가 존재하지 않거나, 입출력 에러가 발생할 경우, 파일이 Object 파일이 아닐경우,
     *                Object가 UnitInfo로 캐스팅이 실패될경우
     */
    public UnitInfo readUnit(String unitID, int sendNo)
        throws RepositoryException {
        String dirName = messageID + "-" + Integer.toString(sendNo);
        File msgDirFile = new File(dirFile, dirName);
        if (!msgDirFile.exists()) {
            throw new RepositoryException("Not exists Dir: " + msgDirFile);
        }
        File uFile = new File(msgDirFile, unitID);

        UnitInfo unit = null;
        try {
            unit = (UnitInfo) ObjectFile.readObject(uFile);
        }
        catch (Exception ex) {
            throw new RepositoryException(ex.getMessage());
        }
        return unit;
    }

    /**
     * 지정된 UnitID로 저장소에 저장된 UnitInfo객체 파일을 삭제한다.
     *
     * @param 삭제할려고
     *            하는 Unit의 UnitID
     * @return boolean Unit이 삭제 성공하거나 Unit이 존재하지 않으면 true
     */
    public boolean deleteUnit(int unitID, int sendNo) {
        String dirName = messageID + "-" + Integer.toString(sendNo);
        File msgDirFile = new File(dirFile, dirName);
        if (!msgDirFile.exists()) {
            return true;
        }
        File uFile = new File(msgDirFile, Integer.toString(unitID));
        if (uFile.exists()) {
            return uFile.delete();
        }
        return true;
    }

    /**
     * Message의 Unit 저장 디렉토리를 삭제한다.
     *
     * @return boolean 디렉토리가 존재하지 않거나 삭제 성공되면 true
     */
    public boolean delete(int sendNo) {
        String dirName = messageID + "-" + Integer.toString(sendNo);
        File msgDirFile = new File(dirFile, dirName);
        if (msgDirFile.exists()) {
            return msgDirFile.delete();
        }
        return true;
    }

    public boolean existsUnit(int sendNo) {
        boolean exists = false;
        String dirName = messageID + "-" + Integer.toString(sendNo);
        File msgDirFile = new File(dirFile, dirName);
        if (msgDirFile.exists()) {
            String[] list = msgDirFile.list();
            if (list.length > 0) {
                exists = true;
            }
            else {
                msgDirFile.delete();
            }
        }
        return exists;
    }
}
