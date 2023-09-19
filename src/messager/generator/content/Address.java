package messager.generator.content;

/**
 * 이메일 주소에서 도메인 네임을 추출하고 이메일 주소에 대한 단순 유효성을 체크한다.
 * 유효성 체크는 '@'문자의 중복성만 체크한다.
 * 잘못된 이메일 주소일 경우 IllegalArgumentException 이 발생한다.
 *
 * @author Administrator
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Address
{
    /** 발송 대상 이메일 주소 */
    public String email;
    /** 발송 대상 이메일 주소에 대한 도메인 네임 */
    public String domain;
    /**
     * 이메일 주소로 Address 객체를 생성한다.
     *
     * @param email 대상자의 이메일 주소
     */
    public Address(String email) {
        this.email = email;
        this.domain = getDomain(email);
    }

    /**
     * 이메일 주소에서 도메인 네임을 추출한다.
     *
     * @param email
     * @return 도메인 명
     */
    private String getDomain(String email) {
        String domain = null;
        int inx = email.indexOf('@');
        if (inx >= 1) {
            if (++inx >= email.length()) {
                throw new IllegalArgumentException("Address: " + email);
            }
            if (email.indexOf('@', inx) != -1) {
                throw new IllegalArgumentException("Address: " + email);
            }
            domain = email.substring(inx).toLowerCase();
        }
        else {
            throw new IllegalArgumentException("Address: " + email);
        }
        return domain;
    }
}
