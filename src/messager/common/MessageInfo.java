package messager.common;

/**
 * Message의 발송을 위한 정보중 대상자 정보를 제외한 
 * Message객체와 Contents객체를 포함한다.
 * 이 인스턴스는 Generator로 Message객체와 Contents객체를 보내줄 때 
 * 사용된다. 
 * 
 * @author Administrator
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MessageInfo implements java.io.Serializable {

	/** Message의 id */
	private String messageID;

	/** Message 객체 */
	private Message message;

	/** 컨텐츠 */
	private Contents contents;

	/** 컨텐츠2 */
	private Contents2 contents2;

	
	/** 
	 * 인스턴스를 생성한다.
	 * 
	 * @param message
	 * @param contents
	 */
	public MessageInfo(Message message, Contents contents) {

		this.messageID = message.messageID;
		this.message = message;
		this.contents = contents;

	}

	/** 
	 * 인스턴스를 생성한다.2
	 * 
	 * @param message
	 * @param contents
	 */
	public MessageInfo(Message message, Contents contents, Contents2 contests2) {

		this.messageID = message.messageID;
		this.message = message;
		this.contents = contents;
		this.contents2 = contests2;

	}

	
	/**
	 * message의 id를 얻는다.
	 * @return
	 */
	public String getMessageID() {

		return messageID;

	}

	/**
	 * Message 객체를 얻는다.
	 * 
	 * @return
	 */
	public Message getMessage() {

		return message;

	}

	/**
	 * 컨텐츠를 얻는다
	 * 
	 * @return
	 */
	public Contents getContents() {

		return contents;

	}
	
	/**
	 * 컨텐츠를 얻는다
	 * 
	 * @return
	 */
	public Contents2 getContents2() {

		return contents2;

	}

}

