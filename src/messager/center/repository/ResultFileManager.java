package messager.center.repository;

import java.io.*;

import messager.center.config.*;

/**
 * Unit의 발송 결과 파일들을 Message별로 관리한다.
 * Unit의 발송 결과 파일은 작업 디렉토리아래에
 * result 디렉토리에 Message별로 디렉토리를 생성하여 관리한다.
 * @author Administrator
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ResultFileManager
{
    private final static String dirName = "result";

    private static File dirFile;

    static {
        String workPath = ConfigLoader.getString("work.path", "repository");

        dirFile = new File(workPath, dirName);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
    }

    /** Message의 발송 결과 파일들을 저장할 디렉토리의 File 객체 */
    private File msgDirFile;

    public ResultFileManager(String messageID) {
        msgDirFile = new File(dirFile, messageID);
        if (!msgDirFile.exists()) {
            msgDirFile.mkdirs();
        }
    }

    /**
     * 발송 결과를 파일에 write 한다.
     *
     * @param result Unit의 발송 결과가 저장된 ResultUnit 객체
     * @throws IOException 입출력 오류가 발생할 경우
     */
    public void writeResult(ResultUnit result)
        throws IOException {
        int unitID = result.getUnitID();
        byte[] data = result.getBytes();

        String uid = Integer.toString(unitID);
        String name = uid;

        if (!msgDirFile.exists()) {
            return;
        }
        File unitFile = new File(msgDirFile, name);
        int c = 0;

        while (unitFile.exists()) {
            name = uid + "_" + c++;
            unitFile = new File(msgDirFile, name);
        }
        OutputStream out = null;
        Exception ex = null;

        try {
            out = new FileOutputStream(unitFile);
            out.write(data, 0, data.length);
        }
        catch (Exception exception) {
            ex = exception;
        }
        finally {
            if (out != null) {
                try {
                    out.close();
                }
                catch (IOException ex1) {
                }
                out = null;
            }
        }

        if (ex != null) {
            if (ex instanceof IOException) {
                throw (IOException) ex;
            }
            else {
                throw (RuntimeException) ex;
            }
        }
    }

    /**
     * 발송 결과 파일을 읽어서 ResultUnit 객체를 얻는다.
     *
     * @param name 발송 결과 파일의 파일명
     * @return Unit의 발송 결과가 저장된 ResultUnit객체
     * @throws IOException 입출력 에러가 발생할 경우
     */
    public ResultUnit readResult(String name)
        throws IOException {
        File unitFile = new File(msgDirFile, name);
        if (!unitFile.exists()) {
            return null;
        }
        int length = (int) unitFile.length();
        byte[] data = new byte[length];

        InputStream in = null;
        Exception ex = null;
        ResultUnit result = null;

        try {
            in = new FileInputStream(unitFile);
            int rc = in.read(data, 0, length);
            if (rc == length) {
                result = new ResultUnit(data);
            }
        }
        catch (Exception ex1) {
            ex = ex1;
        }
        finally {
            if (in != null) {
                try {
                    in.close();
                }
                catch (IOException ex2) {
                }
                in = null;
            }
        }

        if (ex != null) {
            if (ex instanceof IOException) {
                throw (IOException) ex;
            }
            else {
                throw (RuntimeException) ex;
            }
        }
        return result;
    }

    /**
     * 발송 결과 파일 리스트를 로딩한다.
     *
     * @return 발송 결과 파일명이 저장된 배열
     */
    public String[] list() {
        if (msgDirFile.exists()) {
            return msgDirFile.list();
        }
        return null;
    }

    /**
     * Unit의 발송 결과 파일을 삭제한다.
     *
     * @param name 발송 결과 파일의 이름
     */
    public void delete(String name) {
        File resultFile = new File(msgDirFile, name);

        if (resultFile.exists()) {
            resultFile.delete();
        }
    }

    /**
     * Message의 발송 결과 파일들이 저장된 디렉토리를 삭제한다.
     */
    public void delete() {
        if (msgDirFile.exists()) {
            msgDirFile.delete();
        }
    }

}
