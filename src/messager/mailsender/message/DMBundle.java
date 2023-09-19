package messager.mailsender.message;

import java.util.*;

public class DMBundle
{
    public String unitName;
    public int deptNo;
    public String userNo;
    public String campTyNo;
    public int campNo;
    public int taskNo;
    public int subTaskNo;
    public String senderEmail; // 보내는이의 메일 주소
    public boolean appendedFile;
    public boolean sendMode;
    public boolean sendTest;
    public int socketTimeOut;
    public int sendNo;
    public int retryCount;
    public String target_grp_ty;

    Vector bundle; // DomainMessage 의 Vector
    Vector emailErrorBundle; // UserMessage의 Vector

    public DMBundle() {
        bundle = new Vector();
        emailErrorBundle = new Vector();
    }

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

    public void setTaskNo(int taskNo) {
        this.taskNo = taskNo;
    }

    public void setSubTaskNo(int subTaskNo) {
        this.subTaskNo = subTaskNo;
    }

    public void setSocketTimeOut(int socketTimeOut) {
        this.socketTimeOut = socketTimeOut;
    }

    public void setAppendedFile(boolean appendedFile) {
        this.appendedFile = appendedFile;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    public void setSendMode(boolean sendMode) {
        this.sendMode = sendMode;
    }

    public void setSendTest(boolean sendTest) {
        this.sendTest = sendTest;
    }

    public void setSendNo(int sendNo) {
        this.sendNo = sendNo;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public void setTargetGrpTY(String target_grp_ty) {
        this.target_grp_ty = target_grp_ty;
    }

    public void addMessage(DomainMessage message) {
        bundle.add(message);
    }

    // DMBundle 생성시 이메일이 잘못된 사용자 리스트
    public void addEmailError(UserMessage message) {
        emailErrorBundle.add(message);
    }

    public Vector getMessage() {
        return bundle;
    }

    public Vector getErrorEmail() {
        return emailErrorBundle;
    }

    public int getSize() {
        return bundle.size();
    }
}
