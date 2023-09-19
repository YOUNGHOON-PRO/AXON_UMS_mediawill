package messager.mailsender.sendlog;

//import java.io.FileWriter;
import java.io.*;

import messager.mailsender.util.*;

/**
 * SendLogRecord Format
 * @value RowID			: 고유번호
 * @value DeptNo		: 부서 번호
 * @value UserNo		: 사용자번호
 * @value CampTyNo		: 캠페인 타입번호
 * @value CampNo		: 캠페인 번호
 * @value TaskNo 		: 업무 번호
 * @value SubTaskNo		: 업무 2번호
 * @value AccountID		: 고객번호(ID)
 * @value RetryCount	        : 재발송 횟수
 * @value Email			: 회원 이메일
 * @value Name			: 회원 이름
 * @value Time			: 발송 시간
 * @value ResultCode	        : 발송 결과 코드
 * @value Major Code	        : 오류코드(대)
 * @value pCode			: 오류코드(중)
 * @value sCode			: 오류코드(소)
 * @value rMessage		: 오류 메시지
 * @value Domain		: 회원 이메일의 도메인
 * @value target_grp_ty : 발송대상유형
 * --------------------------------------------
 * RowID | DeptNo | UserNo | CampTyNo | CampNo | TaskNo | SubTaskNo |
 * RetryCount | SendDate | ResultCode | MajorCode | PCode | SCode |
 * responseMsg | Email | AccountName | AccountID | target_grp_ty
 * --------------------------------------------
 * UnitLog Format
 * ----------------------------------
 *  msgID  | unitID | TotalCount | TimeStamp | ResultCode|
 * 32bytes | 4bytes |   2bytes   |   8bytes  |    1byte
 * ----------------------------------
 * @value resultCode : 발송 결과
 * 1 : 성공
 * 2 : 생성에러(재생성 불가능)
 * 3 : 생성에러
 * 4 : 발송에러(재발송 가능한 대상자)
 * 5 : 발송에러(재발송 대상이 에서 제외된 대상)
 */

public class SendLogFileAccess
{
    private PrintWriter accessFile;
    private RandomAccessFile unitFile;
    private String separator = "";
    private File logFile;
    private File unitlogFile;
    private final int recordPos = 64;
    private final long timePos = 40;
    private SendLogRecord log;

    public SendLogFileAccess(File logFile, File unitlogFile)
        throws IOException {
        Exception ex = null;
        this.logFile = logFile;
        this.unitlogFile = unitlogFile;
        try {
            //accessFile = new PrintWriter(new FileWriter(logFile, true));
            accessFile = new PrintWriter(new OutputStreamWriter(new FileOutputStream(logFile, true),
                "UTF-8"));
            unitFile = new RandomAccessFile(unitlogFile, "rw");
        }
        catch (Exception exception) {
            ex = exception;
            LogWriter.writeException("SendLogFileAccess", "SendLogFileAccess()",
                                     "파일 객체를 생성할수 없습니다.", ex);
        }

        if (ex != null) {
            accessFile.close();
            if (ex instanceof IOException) {
                throw (IOException) ex;
            }
            else {
                throw (RuntimeException) ex;
            }
        }
    }

    public RandomAccessFile getUnitFile() {
        return unitFile;
    }

    public void writeSendLogRecord(SendLogRecord log) {
        Exception ex = null;
        this.log = log;
        String newLineStr = System.getProperty("line.separator");
        StringBuffer sb = new StringBuffer();

        String logLine = sb.append(log.sendTest)
            .append(separator).append(log.rowID)
            .append(separator).append(log.deptNo)
            .append(separator).append(log.userNo)
            .append(separator).append(log.campTyNo)
            .append(separator).append(log.campNo)
            .append(separator).append(log.taskNo)
            .append(separator).append(log.subTaskNo)
            .append(separator).append(log.retryCount)
            .append(separator).append(log.sendDate)
            .append(separator).append(log.resultCode)
            .append(separator).append(log.majorCode)
            .append(separator).append(log.pCode)
            .append(separator).append(log.sCode)
            .append(separator).append(log.responseMessage)
            .append(separator).append(log.email)
            .append(separator).append(log.accountName)
            .append(separator).append(log.accountID)
            .append(separator).append(log.target_grp_ty)
            .append(separator).append(log.bizkey)
            .append(newLineStr).toString();

        if (accessFile != null) {
            synchronized (accessFile) {
                accessFile.print(logLine);
                accessFile.flush();
            }
        }
        else {
            try {
                //this.accessFile = new PrintWriter(new FileWriter(logFile, true));
                accessFile
                    = new PrintWriter(new OutputStreamWriter(new FileOutputStream(logFile, true), "UTF-8"));
                synchronized (accessFile) {
                    accessFile.print(logLine);
                    accessFile.flush();
                }
            }
            catch (IOException e1) {
                LogWriter.writeException("SendLogFileAccess", "writeSendLogRecord()",
                                         "에러를 확인해 보세요", e1);
            }
        }
        if (ex != null) {
            if (ex instanceof IOException) {

            }
            else {
                throw (RuntimeException) ex;
            }
        }
    }

    public boolean writeUnitLogEndTime(long endTime) {
        Exception ex = null;
        try {
            if (unitFile != null) {
                synchronized (unitFile) {
                    unitFile.seek(timePos);
                    unitFile.writeLong(endTime);
                }
            }
            else {
                this.unitFile = new RandomAccessFile(unitlogFile, "rw");
                synchronized (unitFile) {
                    unitFile.seek(timePos);
                    unitFile.writeLong(endTime);
                }
            }
        }
        catch (IOException e) {
            ex = e;
            LogWriter.writeException("SendLofFileAccess", "writeUnitLogEndTime",
                                     "에러 확인", e);
        }

        if (ex != null) {
            if (ex instanceof IOException) {
                return false;
            }
            else {
                throw (RuntimeException) ex;
            }
        }
        return true;
    }

    /**
     * UnitLog 기록
     * 1 : 성공
     * 2 : 생성에러(재생성 불가능)
     * 3 : 생성에러
     * 4 : 발송에러(재발송 가능한 대상자 : 발송 결과 코드(4,5,6))
     * 5 : 발송에러(재발송 대상이 에서 제외된 대상)
     * @param log
     * @return true : 기록
     * 	             false : 기록 실패
     * @throws IOException
     */
    public boolean writeUnitLog(int rowID, String resultCode) {
        Exception ex = null;
        byte[] bytes = new byte[1];

        if (resultCode.equals("000")) {
            bytes[0] = 1;
        }
        else if (resultCode.equals("004") || resultCode.equals("005") ||
                 resultCode.equals("006")) {
            bytes[0] = 4;
        }
        else {
            bytes[0] = 5;
        }

        long pos = recordPos + rowID;
        try {
            if (unitFile != null) {
                synchronized (unitFile) {
                    unitFile.seek(pos);
                    unitFile.write(bytes, 0, 1);
                }
            }
            else {
                this.unitFile = new RandomAccessFile(unitlogFile, "rw");
                synchronized (unitFile) {
                    unitFile.seek(pos);
                    unitFile.write(bytes, 0, 1);
                }
            }
        }
        catch (IOException e) {
            ex = e;
            LogWriter.writeException("SendLogFileAccess", "writeUnitLog()",
                                     "에러 로그 확인", e);
        }
        return true;
    }

    private void short2Bytes(short n, byte[] buf, int offset) {
        int i = offset;
        buf[i++] = (byte) (n >>> 8);
        buf[i] = (byte) (n >>> 0);
    }

    // int -> byte배열 변환
    private void int2Bytes(int n, byte[] buf, int offset) {
        int i = offset;
        buf[i++] = (byte) (n >>> 24);
        buf[i++] = (byte) (n >>> 16);
        buf[i++] = (byte) (n >>> 8);
        buf[i] = (byte) (n >>> 0);
    }

    // byte배열 -> short변환
    private static short bytes2Short(byte[] buf, int offset) {
        int i = offset;
        return (short) ( ( (short) buf[i++] << 8) | ( (short) (buf[i] & 0xFF) << 0));
    }

    public void close() {
        try {
            if (accessFile != null) {
                this.accessFile.close();
                this.accessFile = null;
            }
            if (unitFile != null) {
                this.unitFile.close();
                this.unitFile = null;
            }
        }
        catch (IOException e) {
            LogWriter.writeException("SendLogFileAccess", "close()", "에러 로그 확인", e);
        }

    }

}
