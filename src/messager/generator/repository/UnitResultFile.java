package messager.generator.repository;

import java.io.File;
import messager.generator.config.ConfigLoader;

/**
 * Unit의 발송 결과 파일을 저장한 디렉토리를 관리한다.
 * 발송 결과 파일은 컨텐츠 생성시 생성된다.
 *
 * @author Administrator
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class UnitResultFile
{
    private final static String dirName = "unitlog";

    private static File dirFile;
    static {
        String workdir = ConfigLoader.getString("work.path", "repository");
        dirFile = new File(workdir, dirName);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
    }

    /**
     * 발송 결과파일의 리스트를 얻는다
     *
     * @return 발송 결과 파일의 파일명 배열
     */
    public static String[] list() {
        return dirFile.list();
    }

    /**
     * 발송 결과 파일을 삭제한다.
     *
     * @param unitName
     * @return 파일이 삭제되면 true
     */
    public static boolean delete(String unitName) {
        File unitFile = new File(dirFile, unitName);
        return unitFile.delete();
    }

    /**
     * Unit의 발송 결과 파일의 File 객체를 얻는다.
     *
     * @param unitName
     * @return File
     */
    public static File unitFile(String unitName) {
        return new File(dirFile, unitName);
    }
}
