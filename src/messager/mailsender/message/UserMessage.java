package messager.mailsender.message;

/**
 * 발송 대상자에 대한 상세 정보를 가진 객체
 * DMBUNDLE > DOMAINMESSAGE > USERMESSAGE
 * @value rowID
 * @value accountID : 대상자 아이디
 * @value emailAddr : 대상자 이메일 주소
 * @value accountName : 대상자 이름
 */
public class UserMessage
{
    private int rowID; // 발송 대상자의 ROWID
    private String accountID; // 발송 대상자의 회원 아이디
    private String emailAddr; // 발송 대상자의 이메일 주소
    private String accountName; // 발송 대상자의 회원 이름
    private String enckey; // 보안메일 암호값
    private String bizkey; // EAI연계 메시지 값

    public void setRowID(int rowID) {
        this.rowID = rowID;
    }

    public void setAccountID(String accountID) {
        this.accountID = accountID;
    }

    public void setEmailAddr(String emailAddr) {
        this.emailAddr = emailAddr;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public int getRowID() {
        return rowID;
    }

    public String geAccountID() {
        return accountID;
    }

    public String getEmailAddr() {
        return emailAddr;
    }

    public String getAccountName() {
        return accountName;
    }

	public String getEnckey() {
		return enckey;
	}

	public void setEnckey(String enckey) {
		this.enckey = enckey;
	}

	public String getBizkey() {
		return bizkey;
	}

	public void setBizkey(String bizkey) {
		this.bizkey = bizkey;
	}
    

}