package messager.generator.repository;

import java.io.*;

import messager.common.*;
import messager.common.util.*;
import messager.generator.config.*;

/**
 * UnitInfo 객체 파일을 관리한다. work.path Property에 지정된 경로 아래에 unit으로 디렉토리를 생성하여
 * UnitInfo객체파일을 생성, 저장, 삭제를 하고 UnitInfo객체 파일의 존재 유무로 Unit의 컨텐츠 생성완료를 확인 한다.
 */
public class UnitInfoFile
{
    //UnitInfo 객체 파일을 저장할 디렉토리 명
    private static final String dirName = "unit";

    //UnitInfo 객체 파일을 저장할 디렉토리의 File 객체
    private static File dirFile;

    static {
        //work.path Property 를 얻는다.
        String workPath = ConfigLoader.getString("work.path", "repository");
        dirFile = new File(workPath, dirName);
        if (!dirFile.exists()) {
            if (!dirFile.mkdirs()) {
                System.err.println("디렉토리를 생성할 수 없음: " + dirFile);
                System.exit(1);
            }
        }
        else if (!dirFile.isDirectory()) {
            System.err.println("디렉토리가 아님 : " + dirFile);
            System.exit(1);
        }
    }

    /**
     * 존재하는 UnitInfo객체 파일들의 UnitName(MessageID^UnitID) 리스트를 얻는다. UnitInfo객체 파일은
     * UnitName으로 생성되므로 파일명 리스트는 UnitName 리스트이다.
     *
     * @return String[] 디렉토리에 저장된 UnitInfo객체에 대한 UnitName 배열
     */
    public static String[] unitList() {
        return dirFile.list();
    }

    /**
     * 디렉토리에 UnitInfo 객체를 UnitName(messageID^UnitID)의 파일에 write
     *
     * @param unitInfo
     */
    public static void writeUnit(UnitInfo unit)
        throws IOException {
        String unitName = unit.getName();
        File unitFile = new File(dirFile, unitName);
        //객체 Serializable를 통해서 File에 Write
        ObjectFile.writeObject(unitFile, unit);
    }

    /**
     * UnitInfo 객체 파일을 읽는다.
     *
     * @param unitName
     *            MessageID^UnitID
     * @return UnitInfo unitName에 해당하는 UnitInfo 객체
     */
    public static UnitInfo readUnit(String unitName)
        throws IOException {
        File unitFile = new File(dirFile, unitName);
        //객체 Serializable를 통해서 UnitInfo 객체를 읽는다.
        return (UnitInfo) ObjectFile.readObject(unitFile);
    }

    /**
     * UnitInfo 객체 파일을 삭제한다. 컨텐츠 생성이 완료된 UnitInfo 객체 파일을 삭제하기 위하여 호출된다.
     *
     * @param unitName
     *            삭제할 Unit의 UnitName(MessageID^UnitID)
     * @return 삭제가 성공하면 true
     */
    public static boolean deleteUnit(String unitName) {
        File unitFile = new File(dirFile, unitName);
        return unitFile.delete();
    }

    /**
     * UnitInfo 객체 파일의 존재를 확인한다.
     *
     * @param unitName
     * @return UnitInfo 객체 파일이 존재하면 true
     */
    public static boolean exists(String unitName) {
        File unitFile = new File(dirFile, unitName);
        return unitFile.exists();
    }
}
