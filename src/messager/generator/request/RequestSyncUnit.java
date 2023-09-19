package messager.generator.request;

import java.io.*;
import java.util.*;

import messager.generator.config.*;

/**
 * MessageCenter로 전송받은 Unit의 동기화를 위한 작업을 한다. 전송받은 Unit의
 * UnitName(MessageID^UnitID)으로 동기화 파일을 생성한 후 UnitInfo 객체를 파일로 저장한 후 동기화 파일을
 * 삭제한다.
 */
class RequestSyncUnit
{

    //디렉토리명
    private static final String dirName = "request";

    //Instance
    private static RequestSyncUnit requestSyncUnit;

    /**
     * RequestSyncUnit에 대한 Instance를 얻는다.
     */
    public static RequestSyncUnit instance() {

        //instance가 생성되어 있지 않을 경우는 생성한다.
        if (requestSyncUnit == null) {

            requestSyncUnit = new RequestSyncUnit();

        }

        return requestSyncUnit;

    }

    //동기화 파일이 저장될 디렉토리에 대한 File 객체
    private File syncDirFile;

    /**
     * RequestSyncUnit에 대한 객체를 생성한다.
     */
    private RequestSyncUnit() {

        //작업 경로
        String workPath = ConfigLoader.getProperty("work.path");

        if (workPath == null) {

            workPath = "work";

        }

        //directory 에 대한 File객체 생성및 디렉토리 생성
        syncDirFile = new File(workPath, dirName);

        if (!syncDirFile.exists()) {

            syncDirFile.mkdirs();

        }

    }

    /**
     * Unit의 동기화 파일의 File 객체를 얻는다.
     *
     * @param unitName
     *            messageID^unitID
     */
    public File unitFile(String unitName) {

        File unitSyncFile = new File(syncDirFile, unitName);

        return unitSyncFile;

    }

    /**
     * Unit의 동기화 파일를 생성한다..
     *
     * @param unitName
     *            MessageID^UnitID
     */
    public void create(String unitName) {

        File unitSyncFile = unitFile(unitName);

        try {

            unitSyncFile.createNewFile();

        }
        catch (IOException ex) {
        }
    }

    /**
     * Unit에 대한 동기화 파일을 삭제한다.
     *
     * @param unitName
     *            MessageID^UnitID
     */
    public void delete(String unitName) {

        File unitSyncFile = unitFile(unitName);

        if (unitSyncFile.exists()) {

            unitSyncFile.delete();

        }

    }

    /**
     * 존재하는 동기화 Unit 리스트를 얻는다.
     */
    public ArrayList load() {

        ArrayList list = new ArrayList();

        File[] fileList = syncDirFile.listFiles();

        for (int i = 0; i < fileList.length; i++) {

            File syncUnitFile = fileList[i];

            String unitName = syncUnitFile.getName();

            list.add(unitName);

        }
        return list;

    }

}
