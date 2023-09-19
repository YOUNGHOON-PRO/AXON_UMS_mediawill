package messager.common;

/**
 * Unit내에 포함된 대상자들에 대한 도메인으로 그룹화한 객체  
 * 대상자의 발송 정보는 SendTo객체에 담아서 저장한다.
 */
public final class SendDomain extends java.util.ArrayList implements
		java.io.Serializable {

	/** 도메인 명 */
	private String name; //도메인 명

	/**
	 * 객체 생성
	 * 
	 * @param aDomain 도메인 명
	 */
	public SendDomain(String name) {
		super();

		this.name = name;

	}

	/**
	 * 도메인 명을 얻는다.
	 * 
	 * @return 도메인 명
	 */
	public String getName() {

		return name;

	}

	/**
	 * 대상자를 발송 정보를 SendTo 객체에 담아서 추가한다.
	 * 
	 * @param rowID UnitInfo객체에 저장된 대상자의 인덱스
	 * @param id 대상자의 고객번호 
	 * @param email 대상자의 이메일
	 * @param name 대상자의 이름
	 */
	public void addSendTo(int rowID, String id, String email, String name) {

		SendTo sendTo = new SendTo(rowID, id, email, name);

		add(sendTo);

	}

	/**
	 * 대상자의 발송정보가 저장된 SendTo 객체를 추가한다.
	 * 
	 * @param sendTo 대상자의 발송 정보 
	 */
	public void addSendTo(SendTo sendTo) {

		add(sendTo);

	}

	/**
	 * 지정된 인덱스로 대상자의 발송 정보를 얻는다.
	 * 
	 * @param index
	 * @return 대상자의 발송 정보가 저장된 SendTo 객체
	 */
	public SendTo getSendTo(int index) {

		SendTo sendTo = null;

		if (index < size()) {

			sendTo = (SendTo) get(index);

		}

		return sendTo;
	}
}

