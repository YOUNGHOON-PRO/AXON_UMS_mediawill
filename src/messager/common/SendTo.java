package messager.common;

/** 
 * 대상자의 발송 정보를 저장하는 객체 
 * 
 * @author Administrator
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public final class SendTo implements java.io.Serializable {
	/** UnitInfo에 저장된 위치를 나타내는 인덱스 번호 */
	public int rowID;

	/** 대상자의 이메일 주소 */
	public String email;

	/** 대상자의 이름 */
	public String name;

	/** 대상자의 고객번호 */
	public String id;
	
	/** 대상자의 보안메일 암호값 */
	public String encKey;
	
	/** 대상자의 EAI연계 메시지 값 */
	public String bizKey;

	/**
	 * SendTo 객체를 생성한다. 
	 * 
	 * @param rowID
	 */
	public SendTo(int rowID) {
		this.rowID = rowID;
	}

	/** 
	 * SendTo 객체 생성
	 * 
	 * @param rowID
	 * @param id
	 * @param email
	 * @param name
	 */
	public SendTo(int rowID, String id, String email, String name) {
		this.rowID = rowID;
		this.id = id;
		this.email = email;
		this.name = name;
	}
	
	public SendTo(int rowID, String id, String email, String name, String encKey, String bizKey) {
		this.rowID = rowID;
		this.id = id;
		this.email = email;
		this.name = name;
		this.encKey = encKey;
		this.bizKey = bizKey;
	}
	
}
