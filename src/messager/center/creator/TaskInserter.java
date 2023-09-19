package messager.center.creator;

import java.sql.*;
import java.text.*;
import java.util.*;

import messager.center.db.*;
import messager.common.*;

/**
 * 정기 메일일 경우 신규 업무를 Neo_SubTask에 인서트한다. Task_No는 기존 값을 채우고 Sub_Task_No는 기존 값에
 * 1증가한 값으로 한다.
 *
 * @author Administrator TODO To change the template for this generated type
 *         comment go to Window - Preferences - Java - Code Style - Code
 *         Templates
 */
public class TaskInserter
{
    /** 메일 발송 예약 시간의 포맷 */
    private final static String SEND_TIME_FORMAT = "yyyyMMddHHmm";

    /** 인서트 쿼리문
     * 코드값에 맞춰 메세지 정보를 새로 인서트 한다.
     * WORK_STATUS = '001' (발송승인)
     * SEND_TEST = 'N' (실발송)
     * CHANNEL = '000' (이메일 채널)
     * STAUTS = '000' (정상)
     */
    private final static String INSERT_SQL = "INSERT INTO NEO_SUBTASK "
        + "(TASK_NO, SUB_TASK_NO, SEND_DT, RESP_END_DT, WORK_STATUS, RETRY_CNT, SEND_TEST_YN, CHANNEL, STATUS) "
        + "VALUES(?, ?, ?, ?, '001', ?, 'N', '000','000')";	//chk cskim 07.09.05

    /** Message 객체 */
    private Message message;

    /** 정의된 포맷의 String객체로 시간을 얻는다. */
    private SimpleDateFormat timeFormat;

    /** 시간을 증가시킨다. */
    private Calendar calendar;

    /**
     * 정기메일의 새로운 발송 업무를 인서트 할 TaskInserter 객체를 생성한다.
     *
     * @param message
     *            Neo_SubTask, Neo_Task테이블에서 검색한 발송 정보를 저장한 객체
     */
    public TaskInserter(Message message) {
        this.message = message;
        timeFormat = new SimpleDateFormat(SEND_TIME_FORMAT);
        calendar = Calendar.getInstance();
    }

    /**
     * 다음 발송 시간을 얻는다.
     *
     * @return 다음 발송 시간이 현재보다 이후인 시간을 지정된 포맷으로 변환한 String
     * @throws Exception
     */
    private String nextSendTime()
        throws Exception {
        Exception ex = null;
        String nextSendTime = null;

        try {

        	HashMap taskMap = message.taskMap;

        	//char type = ((Character) taskMap.remove("NEO_TASK.SEND_TERM_LOOP_TY")).charValue();
            //String type = (String) taskMap.remove("NEO_TASK.SEND_TERM_LOOP_TY");
        	String type = (String) taskMap.get("NEO_TASK.SEND_TERM_LOOP_TY");

            int iType = Integer.parseInt(type);

            String curSendTime = (String) taskMap.get("NEO_SUBTASK.SEND_DT");

            String sendEndTime = (String) taskMap
                .get("NEO_TASK.SEND_TERM_END_DT");
//            System.out.println("ggggggggggggggggggggggggg");
//            System.out.println(" 이게 문제 NEO_TASK.SEND_TERM_LOOP : " + (Integer) taskMap.get("NEO_TASK.SEND_TERM_LOOP") );
//            System.out.println(" 이게 문제 neo_task.send_term_loop : " + (Integer) taskMap.get("neo_task.send_term_loop") );
//            System.out.println(" 이게 문제 NEO_TASK.send_term_loop : " + (Integer) taskMap.get("NEO_TASK.send_term_loop") );
//            System.out.println(" SEND_DT : " + taskMap.get("NEO_SUBTASK.SEND_DT") );
//            System.out.println(" 1 : " + taskMap.get("NEO_TASK.TARGET_GRP_TY") );
//            System.out.println(" 1 : " + taskMap.get("NEO_TASK.target_grp_ty") );
//            System.out.println(" 2 : " + taskMap.get("NEO_TASK.SEND_TERM_LOOP_TY") );
//            System.out.println(" 2 : " + taskMap.get("NEO_TASK.send_term_loop_ty") );
//            System.out.println(" 3 : " + taskMap.get("NEO_TASK.CHNNEL") );
//            System.out.println(" 3 : " + taskMap.get("NEO_TASK.chnnel") );
//            System.out.println(" 4 : " + taskMap.get("NEO_TASK.CONT_TY") );
//            System.out.println(" 4 : " + taskMap.get("NEO_TASK.cont_ty") );
//            System.out.println(" 5 : " + taskMap.get("NEO_TASK.REG_DT") );
//            System.out.println(" 5 : " + taskMap.get("NEO_TASK.reg_dt") );
//            System.out.println(" 6 : " + taskMap.get("NEO_TASK.Retry_cnt") );
//            System.out.println(" 6 : " + taskMap.get("NEO_TASK.retry_cnt") );
//            System.out.println(" 7 : " + taskMap.get("NEO_TASK.TASK_NO") );
//            System.out.println(" 8 : " + taskMap.get("NEO_TASK.task_no") );
           int increase = ( (Integer) taskMap.get("NEO_TASK.SEND_TERM_LOOP"))
                .intValue();

           if (increase <= 0) {
        	   new Exception("NEO_TASK.SEND_TERM_LOOP: " + increase);
            }

           java.util.Date date = timeFormat.parse(curSendTime);
           java.util.Date endDate = timeFormat.parse(sendEndTime);

           calendar.setTime(date);

           long time = System.currentTimeMillis();
            long endTime = endDate.getTime();
            long nextTime = 0;

            if (endTime != 0 && endTime < time) {
                return null;
            }

            do {
                switch (iType) {
                    case 0:
                        calendar.add(Calendar.MINUTE, increase);
                        break;
                    case 1:
                        calendar.add(Calendar.HOUR, increase);
                        break;
                    case 2:
                        calendar.add(Calendar.DATE, increase);
                        break;
                    case 3:
                        calendar.add(Calendar.MONTH, increase);
                        break;
                    case 4:
                        calendar.add(Calendar.WEEK_OF_MONTH, increase);
                        break;
                    case 5:
                        calendar.add(Calendar.YEAR, increase);
                        break;
                    default:
                        throw new Exception("Invalid Type: " + type);
                }
                date = calendar.getTime();
                nextTime = date.getTime();
            }
            while (nextTime <= time);

            if (nextTime <= endTime) {
                nextSendTime = timeFormat.format(date);
            }

        }
        catch (Exception ex1) {
            ex = ex1;
        }
        if (ex != null) {
            throw ex;
        }
        return nextSendTime;

    }

    /**
     * 다음 발송업무의 수신 완료 시간을 얻는다.
     *
     * @return 다음 발송 업무의 완료 시간을 지정된 포맷으로 변환한 String
     */
    private String nextRespEndTime() {
        HashMap taskMap = message.taskMap;
        int amount = ( (Integer) taskMap.get("NEO_TASK.RESP_LOG")).intValue();
        calendar.add(Calendar.DATE, amount);
        java.util.Date respEndDate = calendar.getTime();
        return timeFormat.format(respEndDate);
    }

    /**
     * 새로운 발송 업무를 인서트한다.
     *
     * @param connection : 인서트를 실행할 Work DB의 Connection 객체
     */
    public void insert(JdbcConnection connection) {
        PreparedStatement pstmt = null;
        int taskNo = message.taskNo;
        int subTaskNo = message.subTaskNo + 1;
        int retryCount = message.retryCount;

        try {
            String nextSendTime = nextSendTime();
            String respEndTime = nextRespEndTime();

            if (nextSendTime == null) {
                return;
            }
            pstmt = connection.prepareStatement(INSERT_SQL);
            pstmt.setInt(1, taskNo);
            pstmt.setInt(2, subTaskNo);
            pstmt.setString(3, nextSendTime);
            pstmt.setString(4, respEndTime);
            pstmt.setInt(5, retryCount);
            pstmt.executeUpdate();
        }
        catch (Exception ex) {
            int errorCode = -1;
            if (ex instanceof SQLException) {
                errorCode = ( (SQLException) ex).getErrorCode();
            }
            System.err.println("Insert Fail: [TaskNo] " + taskNo
                               + " [SubTaskNo] " + subTaskNo + " DB ErrorCode : "
                               + errorCode);
        }
        finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                }
                catch (Exception ex) {
                }
                pstmt = null;
            }
        }
    }
}
