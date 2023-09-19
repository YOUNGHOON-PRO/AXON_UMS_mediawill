package messager.mailsender.message;

import java.util.Vector;

/**
 * 발송 도메인에 대한 상세 정보를 가진 객체
 * DMBUNDLE > DOMAINMESSAGE > USERMESSAGE
 * @value domain : 도메인
 * @value ipVector : 도메인의 MX RECORD VALUES
 * @value recvList : 해당 도메인의 발송 대상자 그룹
 */

public class DomainMessage
{
    public String domain; // 발송 대상 도메인
    public Vector MXRecord; // 발송 대상 도메인의 IP LISTS
    public Vector recvList = new Vector(); // 발송 대상 도메인에 속해 있는 수신 대상자 그룹

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public void setMXRecord(Vector vec) {
        this.MXRecord = vec;
    }

    public void addRecvList(UserMessage message) {
        recvList.add(message);
    }
}