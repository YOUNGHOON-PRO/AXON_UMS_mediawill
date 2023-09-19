package messager.generator.repository;

import java.io.*;
import java.util.*;

import messager.common.*;
import messager.common.util.*;
import messager.generator.config.*;

/**
 * Unit의 발송시 사용되는 발송 정보 파일을 관리한다.
 *
 * @author Administrator
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SendUnitFile
{
    //dirName
    private static final String dirName = "envelope";

    //directory에 대한 File 객체
    private static File dirFile;

    static {
        //작업 디렉토리
        String workPath = ConfigLoader.getString("work.path", "repository");
        //directory에 대한 File 객체 생성
        dirFile = new File(workPath, dirName);
        if (!dirFile.exists()) {
            //directory가 존재하지 않으면 생성
            dirFile.mkdirs();
        }
    }

    /**
     * UnitEnvelope 객체를 파일에 저장한다. 파일명은 MessageID^UnitID.obj
     *
     * @param unitEnvelope
     */
    public static void write(SendUnit unit)
        throws IOException {
        String unitName = unit.getName();
        //UnitEnvelope객체가 저장될 File 객체
        File file = new File(dirFile, unitName);
        //file에 UnitEnvelope객체를 저장한다.
        ObjectFile.writeObject(file, unit);
    }

    /**
     * UnitEnvelope객체 파일들에 대한 UnitName(MessageID^UnitID) 리스트를 얻는다. UnitEnvelope
     * 객체파일의 로딩 지연 시간 때문에 파일 명으로 UnitName을 얻는다
     *
     * @return UnitName를 저장한 ArrayList
     */
    public static ArrayList loadList() {
        ArrayList list = new ArrayList();
        String[] unitList = dirFile.list();
        String unitName;

        for (int i = 0; i < unitList.length; i++) {
            unitName = unitList[i];
            list.add(unitName);
        }
        return list;
    }

    /**
     * Unit에 대한 UnitEnvelope객체 파일이 존재하는지 확인한다.
     *
     * @param unitName
     *            MessageID^UnitID
     * @return Unit에 대한 UnitEnvelope객체 파일이 존재하면 true
     */
    public static boolean exists(String unitName) {
        File file = new File(dirFile, unitName);
        return file.exists();
    }
}
