package messager.common;

/**
 * 머지 할 경우 에러가 발생시 Throw 된다
 * 머지 항목에 대한 대상자의 정보가 없거나 잘못 되었을 경우 발생한다.
 * @author Administrator
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MergeException extends Exception {

	/**
	 * ErrorCode 번호
	 * 머지할 경우 발생할 수 있는 에러에 대한 정의된 코드번호
	 */
	private int errorCode;

	/**
	 * 에러 메세지로 객체를 생성한다. 
	 * @param msg 에러 메세지
	 */
	public MergeException(String msg) {
		super(msg);

	}

	/**
	 * 에러코드와 에러 메세지로 객체를 생성한다.
	 * 
	 * @param code 에러 코드
	 * @param msg 에러 메세지
	 */
	public MergeException(int code, String msg) {

		super(msg);

		errorCode = code;

	}
}