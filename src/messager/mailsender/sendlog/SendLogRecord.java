package messager.mailsender.sendlog;

import messager.mailsender.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SendLogRecord
{
	private static final Logger LOGGER = LogManager.getLogger(SendLogRecord.class.getName());
	
    public boolean sendTest;
    public int rowID;
    public int taskNo;
    public int subTaskNo;
    public int retryCount;
    public String sendDate;
    public String accountID;
    public String email;
    public String accountName;
    public int deptNo;
    public String userNo;
    public String campTyNo;
    public int campNo;
    public String resultCode;
    public String majorCode; // 오류코드(대분류)
    public String pCode; // 오류코드(중분류)
    public String sCode; // 오류코드(소분류)
    public String responseMessage; // 메일서버로 부터 받은 응답 메세지
    public String target_grp_ty; //발송대상 유형
    public String bizkey; // EAI연계 메시지 값

    public SendLogRecord(
        boolean aSendTest,
        int aRowID,
        int aDeptNo,
        String aUserNo,
        String aCampTyNo,
        int aCampNo,
        int ataskNo,
        int aSubTaskNo,
        String aAcountID,
        int aRetryCount,
        String aEmail,
        String aAcountName,
        String aSendDate,
        String aResultCode,
        String aMajorCode,
        String aPCode,
        String aSCode,
        String aResponseMessage,
        String atarget_grp_ty,
        String abizkey) {
        try {

            sendTest = aSendTest;
            rowID = aRowID;
            deptNo = aDeptNo;
            userNo = aUserNo;
            campTyNo = aCampTyNo;
            campNo = aCampNo;
            taskNo = ataskNo;
            subTaskNo = aSubTaskNo;
            retryCount = aRetryCount;
            sendDate = aSendDate;
            resultCode = aResultCode;
            majorCode = aMajorCode;
            pCode = aPCode;
            sCode = aSCode;
            responseMessage = aResponseMessage.trim().replaceAll("\r\n", " ");
            responseMessage = responseMessage.replaceAll("\n", " ");
            email = aEmail.trim();
            accountName = aAcountName.trim();
            accountID = aAcountID.trim();
            target_grp_ty = atarget_grp_ty;
            bizkey = abizkey.trim();

        }
        catch (NullPointerException ex) {
        	LOGGER.error(ex);
            LogWriter.writeException("SendLogRecord", "Constructor()", "Null Pointer"
                                     + "  sendTest : " + sendTest
                                     + "  rowID : " + rowID
                                     + "  deptNo : " + deptNo
                                     + "  userNo : " + userNo
                                     + "  campTyNo : " + campTyNo
                                     + "  campNo : " + campNo
                                     + "  taskNo : " + taskNo
                                     + "  subTaskNo : " + subTaskNo
                                     + "  retryCount : " + retryCount
                                     + "  sendDate : " + sendDate
                                     + "  resultCode : " + resultCode
                                     + "  majorCode : " + majorCode
                                     + "  pCode : " + pCode
                                     + "  sCode : " + sCode
                                     + "  responseMessage : " + responseMessage
                                     + "  email : " + email
                                     + "  accountName : " + accountName
                                     + "  accountID : " + accountID
                                     + "  target_grp_ty : " + target_grp_ty
                                     + "  bizkey : " + bizkey, ex);
        }
    }
}
