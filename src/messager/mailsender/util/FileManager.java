package messager.mailsender.util;

import java.io.*;

import messager.mailsender.code.*;
import messager.mailsender.config.*;

public class FileManager
{
    /**
     * Create Process file
     * @param path
     * @param objName
     * @return
     */
    private static StringBuffer sb;

    public static String makeNameFile(String path, String objName) {
        try {
            return makeFile(new StringBuffer(path).append(objName).toString());
        }
        catch (Exception e) {
            LogWriter.writeException("FileManager", "makeNameFile", "Make Fail", e);
            return ErrorCode.EXCEPT;
        }
    }

    /**
     * 디렉토리 생성
     * @param path
     */
    public static void makeDirectory(String path) {
        try {
            File targetDir = new File(new StringBuffer(ConfigLoader.TRANSFER_ROOT_DIR)
                                      .append(ConfigLoader.TRANSFER_REPOSITORY_DIR)
                                      .append("send/").append(path).toString());
            targetDir.mkdir();
        }
        catch (Exception e) {
            LogWriter.writeException("FileManager", "makeDirectory", "Make Fail", e);
        }
    }

    /**
     * Process file이나 로그 파일 등을 새로운 파일을 생성
     * @param path
     * @return
     */
    private static String makeFile(String path) {
        try {
            File targetFile = new File(path);
            if (targetFile.exists()) {
                return ErrorCode.ALREADYEXIST;
            }
            else {
                if (targetFile.createNewFile()) {
                    return ErrorCode.SUCCESS;
                }
                else {
                    return ErrorCode.MAKEFAIL;
                }
            }
        }
        catch (Exception e) {
            LogWriter.writeException("FileManager", "makeFile", "Make Fail" + path, e);
            return ErrorCode.EXCEPT;
        }
    }

    public static void deleteEmlFiles(String srcFile, int rowID) {
        // delete eml(or mcf) file
        sb = new StringBuffer();
        File emlFile = new File(srcFile);
        if (emlFile.exists()) {
            if (!emlFile.delete()) {
                LogWriter.writeError("FileManager", "deleteFile()" + srcFile, "파일을 지울수 없습니다.", " ");
            }
        }
        else {
            LogWriter.writeError("FileManager", "deleteEmlFile()", "파일이 존재 하지 않습니다..",
                                 emlFile.getAbsolutePath() + "경로 확인 요망");
        }
    }

    /**
     * 발송 완료 파일 삭제
     * @param unitName
     * @param rowID
     */
    public static void deleteUnitFiles(String unitName, boolean isAppended) {
        // ProcessFile
        sb = new StringBuffer();
        File processFile = new File(sb.append(ConfigLoader.TRANSFER_ROOT_DIR)
                                    .append(ConfigLoader.TRANSFER_REPOSITORY_DIR)
                                    .append("send").append(File.separator)
                                    .append(ConfigLoader.MAILSENDER_ID)
                                    .append(File.separator).append(unitName).toString());
        //Object File
        sb = new StringBuffer();
        File objFile = new File(sb.append(ConfigLoader.TRANSFER_ROOT_DIR)
                                .append(ConfigLoader.TRANSFER_REPOSITORY_DIR)
                                .append("envelope/").append(unitName).toString());

        // Content directory
        sb = new StringBuffer();
        File dirFile = new File(sb.append(ConfigLoader.TRANSFER_ROOT_DIR)
                                .append(ConfigLoader.TRANSFER_REPOSITORY_DIR)
                                .append("content/").append(unitName).toString());

        if (objFile.exists()) {
            if (!objFile.delete()) {
                if (objFile.canWrite()) {
                    LogWriter.writeError("FileManager", "deleteFile()", "OBJ 파일을 지울수 없습니다.", " ");
                }
                else {
                    LogWriter.writeError("FileManager", "deleteFile()", "OBJ 파일을 지울수 없습니다.",
                                         "파일 쓰기 권한이 없습니다.");
                }
            }
        }

        if (processFile.exists()) {
            if (!processFile.delete()) {
                if (processFile.canWrite()) {
                    LogWriter.writeError("FileManager", "deleteFile()", "PROCESS 파일을 지울수 없습니다.", " ");
                }
                else {
                    LogWriter.writeError("FileManager", "deleteFile()", "PROCESS 파일을 지울수 없습니다.",
                                         "파일에 대한 쓰기권한이 없습니다.");
                }
            }
        }

        String[] filelist;
        if ( (filelist = dirFile.list()) == null) {
            if (dirFile.exists()) {
                if (!dirFile.delete()) {
                    LogWriter.writeError("FileManager", "deleteFile()", "CONTENT DIR 파일을 지울수 없습니다.", " ");
                }
            }
        }
        else {
            for (int k = 0; k < filelist.length; k++) {
                sb = new StringBuffer();
                File tempFile = new File(sb.append(ConfigLoader.TRANSFER_ROOT_DIR)
                                         .append(ConfigLoader.TRANSFER_REPOSITORY_DIR)
                                         .append("content/").append(unitName).append(File.separator)
                                         .append(filelist[k]).toString());
                if (tempFile.exists()) {
                    if (!tempFile.delete()) {
                        if (tempFile.canWrite()) {
                            LogWriter.writeError("FileManager", "deleteFile()", "CONTENT FILE 파일을 지울수 없습니다.", " ");
                        }
                        else {
                            LogWriter.writeError("FileManager", "deleteFile()", "CONTENT FILE 파일을 지울수 없습니다.",
                                                 "파일 쓰기 권한이 업습니다.");
                        }
                        LogWriter.writeError("FileManager", "deleteFile()", "CONTENT FILE 파일을 지울수 없습니다.", " ");
                    }
                }
            }
            if (!dirFile.delete()) {
                LogWriter.writeError("FileManager", "deleteFile()", "CONTENT DIR 파일을 지울수 없습니다.",
                                     dirFile.getAbsolutePath() + "경로 확인 요망");
            }
        }

        // Appended File
        if (isAppended) {
            sb = new StringBuffer();
            File appendedFile = new File(sb.append(ConfigLoader.TRANSFER_ROOT_DIR)
                                         .append(ConfigLoader.TRANSFER_REPOSITORY_DIR)
                                         .append("attach/").append(unitName).toString());
            if (appendedFile.exists()) {
                if (!appendedFile.delete()) {
                    if (appendedFile.canWrite()) {
                        LogWriter.writeError("FileManager", "deleteFile()", "첨부 파일을 지울수 없습니다.", " ");
                    }
                    else {
                        LogWriter.writeError("FileManager", "deleteFile()", "첨부 파일을 지울수 없습니다.",
                                             "파일 쓰기 권한이 없습니다.");
                    }

                }
            }
        }
    }

    public static void deleteLogFile(String unitName) {

        File logFile = new File(unitName);
        if (logFile.exists()) {
            if (!logFile.delete()) {
                LogWriter.writeError("FileManager", "deleteFile()", "LOG 파일을 지울수 없습니다.", " ");
            }
        }
        else {
            LogWriter.writeError("FileManager", "deleteFile()", "LOG 파일이 존재 하지 않습니다.",
                                 logFile.getAbsolutePath() + "경로 확인 요망");
        }
    }

    /**
     * Eml파일을 바이트 형태로 읽어 들인다.
     * @param emlPath : 이메일 파일 경로
     * @return eml : 바이트 배열
     */
    public static byte[] loadEml(String emlPath) {
    	
    	FileInputStream fis = null;
        File emlFile = new File(emlPath);
        int length = (int) emlFile.length();
        byte[] buffer = new byte[length];
        byte[] eml = new byte[length];

        try {
            /**
             * 송진우 2004.11.22
             * 발송할 데이터가 없는 것도 에러로그에 남겨야 함.
             */
            //if (emlFile.exists()) {
            fis = new FileInputStream(emlFile);
            if ( (fis.read(buffer, 0, length)) != -1) {
                System.arraycopy(buffer, 0, eml, 0, length);
            }
            //}
        }
        catch (IOException e) {
            LogWriter.writeException("FileManager", "loadEml()", "MCF 파일을 읽는데 실패했습니다", e);
        }
        finally {
            try {
                if (fis != null) {
                    fis.close();
                    fis = null;
                }
            }
            catch (Exception e) {}
        }
        return eml;
    }

    public static byte[] loadAttachFile(String attachPath) {
        FileInputStream fis = null;
        File attach = new File(attachPath);
        int length = (int) attach.length();
        byte[] buffer = new byte[length];
        byte[] attachData = new byte[length];

        try {
            if (attach.exists()) {
                fis = new FileInputStream(attach);
                if ( (fis.read(buffer, 0, length)) != -1) {
                    System.arraycopy(buffer, 0, attachData, 0, length);
                }
            }
        }
        catch (IOException e) {
            LogWriter.writeException("FileManager", "loadAttachFile()", "첨부 파일을 읽는데 실패했습니다", e);
        }
        finally {
            try {
                if (fis != null) {
                    fis.close();
                    fis = null;
                }
            }
            catch (Exception e) {}
        }
        return attachData;
    }
}
