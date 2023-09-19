package messager.generator.repository;

import java.io.*;

import messager.generator.config.*;

/**
 * Unit의 발송 결과 파일의 경로 (File 객체)를 지정하고 얻는 클래스
 */
public class UnitSendLogFile
{
    //발송 결과 파일을 저장할 디렉토리명
    private static final String dirName = "sendlog";

    //발송 결과 파일을 저장할 디렉토리의 File 객체
    private static File logDirFile;

    //처음 호출될때 실행 된다.
    static {
        String workDir = ConfigLoader.getProperty("work.path");
        if (workDir == null) {
            workDir = "work";
        }

        //디렉토리의 File 객체 생성
        logDirFile = new File(workDir, dirName);

        if (!logDirFile.exists()) {
            logDirFile.mkdirs();
        }
    }

    /**
     * Unit에 대한 발송 결과 파일의 File 객체를 얻는다.
     *
     * @param unitName
     *            messageID^unitID
     * @return 발송 결과 파일의 File객체
     */
    public static File unitFile(String unitName) {
        return new File(logDirFile, unitName);
    }

    /**
     * 발송 결과 파일의 UnitName 배열을 얻는다. 발송 결과 파일를 MessageCenter 에 전달하기 위해 리스트를 얻는다.
     *
     * @return UnitName(MessageID^UnitID) 배열
     */
    public static String[] list() {
        return logDirFile.list();
    }
}
