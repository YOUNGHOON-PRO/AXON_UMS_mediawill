package messager.mailsender.message;

import java.text.*;
import java.util.*;

import messager.mailsender.send.*;
import messager.mailsender.sendlog.*;

/**
 * DMSender을 위한 UNIT 객체
 * @value message			: 도메인 객체
 * @value access				: 파일 객체
 * @value cManager			: 카운터 객체
 * @value appendedFile		: 첨부파일 데이터
 * @value socketTimeOut	: 소켓 타임 아웃 셋팅값
 * @value sendMode			: 발송 모드(true : 실 발송 , false: 가상 발송)
 * @value sendTest			: 테스트 발송 여부 (true : 테스트 , false : 실제 발송)
 * @value fmt						: 로그 시간 포맷
 * @value ipVec					: IP 리스트
 * @value tGroup				: 발송 ThreadGroup
 * @value currentDomain		: 도메인
 * @value sender				: 보내는이
 * @value unitName			: 유닛 이름
 * @value deptNo				: 부서 번호
 * @value userNo				: 사용자 번호
 * @value campTyNo			: 캠페인타입번호
 * @value campNo				: 캠페인 번호
 * @value taskNo				: 업무 번호
 * @value subTaskNo			: 서브 업무 번호
 * @value retryCount			: 재발송 횟수
 * @value target_grp_ty 	: 발송대상 유형
 * */
public class DMSendUnit
{
    public DomainMessage message;
    public SendLogFileAccess access;
    public CountManager cManager;

    public byte[] appendedFile;
    public int socketTimeOut;
    public boolean sendMode;
    public boolean sendTest;
    public boolean isAppended;
    public SimpleDateFormat fmt;

    public Vector mxRecord;
    public String sender;
    public String unitName;
    public String agentNM;
    public int deptNo;
    public String userNo;
    public String campTyNo;
    public int campNo;
    public int taskNo;
    public int subTaskNo;
    public int sendNo;
    public int retryCount;
    public String target_grp_ty;

    public void setDeptNo(int deptNo) {
        this.deptNo = deptNo;
    }

    public void setUserNo(String userNo) {
        this.userNo = userNo;
    }

    public void setCampTyNo(String campTyNo) {
        this.campTyNo = campTyNo;
    }

    public void setCampNo(int campNo) {
        this.campNo = campNo;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public void setTaskNo(int taskNo) {
        this.taskNo = taskNo;
    }

    public void setSubTaskNo(int subTaskNo) {
        this.subTaskNo = subTaskNo;
    }

    public void setMessage(DomainMessage message) {
        this.message = message;
    }

    public void setSendLogFileAccess(SendLogFileAccess access) {
        this.access = access;
    }

    public void setCountManager(CountManager cManager) {
        this.cManager = cManager;
    }

    public void setAppendedFile(byte[] appendedFile) {
        this.appendedFile = appendedFile;
    }

    public void setSocketTimeOut(int socketTimeOut) {
        this.socketTimeOut = socketTimeOut;
    }

    public void setSendMode(boolean sendMode) {
        this.sendMode = sendMode;
    }

    public void setSendTest(boolean sendTest) {
        this.sendTest = sendTest;
    }

    public void setmxRecord(Vector mxRecord) {
        this.mxRecord = mxRecord;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setSendNo(int sendNo) {
        this.sendNo = sendNo;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public void setDateFormatter(SimpleDateFormat fmt) {
        this.fmt = fmt;
    }

    public void setIsAppended(boolean isAppended) {
        this.isAppended = isAppended;
    }

    public void setAgentName(String agentNM) {
        this.agentNM = agentNM;
    }

    public void setTargetGrpTY(String target_grp_ty) {
        this.target_grp_ty = target_grp_ty;
    }
}
