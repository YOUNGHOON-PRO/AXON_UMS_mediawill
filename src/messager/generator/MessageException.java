package messager.generator;

/**
 * MessageInfo에 필요한 데이타가 존재하지 않을 경우 Throw된다.
 */
public class MessageException extends Exception {

	public MessageException() {

		super();

	}

	public MessageException(String str) {

		super(str);

	}

}

