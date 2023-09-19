package messager.generator.repository;

import java.io.*;
import java.text.*;
import java.util.*;

import messager.common.*;
import messager.generator.config.*;
import messager.generator.content.*;

/**
 * Unit의 SendLog를 파일에 write한다.
 *
 * @author Administrator
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SendLogWriter
{
    private static String logTimeFormat;

    private static String fieldSeparator;

    private static String recordSeparator;

    static {
        logTimeFormat = ConfigLoader.getString("log.time.format", "yyyyMMddHHmm");
        fieldSeparator = ConfigLoader.getString("log.field.separator", "^H^H");
        recordSeparator = ConfigLoader.getString("log.record.separator", System.getProperty("line.separator"));
    }

    /** 출력 스트림 */
    private PrintWriter out;

    /** NEO_TASK.TASK_NO */
    private String taskNo;

    /** NEO_TASK.SUB_TASK_NO */
    private String subTaskNo;

    /** NEO_TASK.DEPT_NO */
    private String deptNo;

    /** NEO_TASK.USER_NO */
    private String userNo;

    /** NEO_TASK.CAMP_NO */
    private String campaignNo;

    /** NEO_TASK.CAMP_TY */
    private String campaignType;

    /** 재발송 시작 카운트 */
    private int sendCount;

    /** 재발송 횟수 */
    private int retryMaxCount;

    /** 발송대상 유형 */
    private String target_grp_ty;

    /**
     * Unit에 포함된 대상자들의 컨텐츠 생성시 실패원인을 저장할 SendLog 파일을 Open한다.
     *
     * @param logFile 컨텐츠 생성시 실패원인을 저장할 SendLog파일
     * @param message Message (Task)의 정보가 저장된 객체
     * @param sendCount 발송 시작시 발송 횟수(발송 시작 카운트)
     * @param retryCount 재발송 횟수
     * @throws Exception File open시 오류가 발생할 경우
     */
    public SendLogWriter(File logFile, Message message, int sendCount)
        throws Exception {
        taskNo = Integer.toString(message.taskNo);
        subTaskNo = Integer.toString(message.subTaskNo);
        deptNo = Integer.toString(message.deptNo);
        userNo = message.userNo;
        campaignNo = Integer.toString(message.campaignNo);
        campaignType = message.campaignType;
        retryMaxCount = message.retryCount;
        this.sendCount = sendCount;
        target_grp_ty = message.target_grp_ty;

        out = new PrintWriter(
            new OutputStreamWriter(
            new BufferedOutputStream(
            new FileOutputStream(logFile)), "UTF-8"));

    }

    /**
     * 대상자의 발송 로그(컨텐츠 생성시 실패 로그)를 파일에 Write
     *
     * @param testYN 테스트 발송 여부
     * @param toUser 대상자의 정보를 담은 SendTo 객체
     * @param errorCode 대상자의 컨텐츠 생성 실패 코드
     * @param errMsg 대상자의 컨텐츠 생성 실패 메세지
     */
    public void write(String testYN, SendTo toUser, ErrorCode errorCode, String errMsg) {
        String rowNo = Integer.toString(toUser.rowID);
        SimpleDateFormat formatter = new SimpleDateFormat(logTimeFormat);
        String time = formatter.format(new Date());

        //초기 재발송 수를 시작 카운트로 설정
        //int retryCount = sendCount;

        //재발송 수만큼 재발송 횟수를 증가하면서 Write한다.
        //for (int i = 0; i <= retryMaxCount; i++) {
        StringBuffer record = new StringBuffer();
        record.append(testYN).append(fieldSeparator)
            .append(rowNo).append(fieldSeparator)
            .append(deptNo).append(fieldSeparator)
            .append(userNo).append(fieldSeparator)
            .append(campaignType).append(fieldSeparator)
            .append(campaignNo).append(fieldSeparator)
            .append(taskNo).append(fieldSeparator)
            .append(subTaskNo).append(fieldSeparator)
            //.append(Integer.toString(retryCount++)).append(fieldSeparator)
            .append(Integer.toString(sendCount)).append(fieldSeparator)
            .append(time).append(fieldSeparator)
            .append(errorCode.code).append(fieldSeparator)
            .append(errorCode.code1).append(fieldSeparator)
            .append(errorCode.code2).append(fieldSeparator)
            .append(errorCode.code3).append(fieldSeparator)
            .append(errMsg).append(fieldSeparator)
            .append(toUser.email).append(fieldSeparator)
            .append(toUser.name).append(fieldSeparator)
            .append(toUser.id).append(fieldSeparator)
            .append(target_grp_ty).append(fieldSeparator)
            .append(toUser.bizKey)
            .append(recordSeparator);
        out.print(record.toString());
        out.flush();
        //}
    }

    /** SendLog 파일 닫는다 */
    public void close() {
        if (out != null) {
            out.close();
            out = null;
        }
    }
}
