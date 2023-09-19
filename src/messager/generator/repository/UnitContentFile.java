package messager.generator.repository;

import java.io.*;

import messager.generator.config.*;

/**
 * Unit에 대한 컨텐츠를 디렉토리를 생성후 컨텐츠의 Charset으로 변환하여 저장한다. 디렉토리의 이름은
 * UnitName[MessageID^UnitID]이고 컨텐츠 파일명은 대상자의 RowID + .eml 이다.
 */
public class UnitContentFile
{
    /** eml 파일의 확장자 */
    private final static String FILE_EXT = ".mcf";

    /**
     * eml 파일이 저장될 디렉토리의 File 객체 설정 파일의 message.eml.path에 지정된 절대 경로를 사용하며 지정되지
     * 않을 경우 repository/eml이 사용된다.
     */
    private static File dirFile;

    //컨텐츠가 저장될 디렉토리 경로를 가져온다.
    static {
        String dirPath = ConfigLoader.getString("content.path",
                                                "repository/content");
        dirFile = new File(dirPath);
        if (!dirFile.exists()) {
            if (!dirFile.mkdirs()) {
                System.err.println("디렉토리를 생성할수 없음 : " + dirFile);
                System.exit(1);
            }
        }
        else if (!dirFile.isDirectory()) {
            System.err.println("디렉토리가 아님 : " + dirFile);
            System.exit(1);
        }
    }

    /** Unit의 eml 파일을 저장하기위한 디렉토리의 File 객체 */
    private File unitDirFile;

    /**
     * 컨텐츠를 저장하는 UnitEmlFile 객체를 생성한다.
     *
     * @param unitName
     *            Unit의 UnitName[MessageID^UnitID]
     * @param charsetName
     *            컨텐츠의 Charset
     */
    public UnitContentFile(String unitName) {
        unitDirFile = new File(dirFile, unitName);
        if (!unitDirFile.exists()) {
            unitDirFile.mkdirs();
        }
    }

    /**
     * rowID로 대상자의 컨텐츠 파일에 대한 File 객체를 얻는다.
     *
     * @param rowID Unit안의 대상자의 순서 번호
     * @return 컨텐츠 파일에 대한 File 객체
     */
    public File contentFile(int rowID) {
        String contentFileName = Integer.toString(rowID) + FILE_EXT; //0.mcf
        return new File(unitDirFile, contentFileName);
    }

    /**
     * Unit에 포함된 대상자들의 컨텐츠 파일들을 삭제한다.
     */
    public void delete() {
        if (unitDirFile.exists()) {
            File[] listFiles = unitDirFile.listFiles();

            for (int i = 0; i < listFiles.length; i++) {
                File contentFile = listFiles[i];
                contentFile.delete();
            }
            unitDirFile.delete();
        }
    }
}
