package messager.common;

import java.util.HashMap;

/**
 * 회원 정보로 머지키를 변경하여 컨텐츠를 생성하지 않고 정의된 방법에 의해
 * 컨텐츠를 사용할 경우 사용된다.
 * 
 * Agent의 블럭을 구분할 id(컨텐츠의 위치에 의해 순서대로 지정된다)와 
 * 컨텐츠를 생성할 때 호출될 클래스명인 Action Name, 클래스의 파라미터로 구성된다.
 * Agent 부분은 WebAgent 이외에는 완성이 되지 않았다.
 * @author Administrator
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public final class Agent implements java.io.Serializable {
	/** Agent의 구분을 위한 키 */
	private int id;

	/** Agent의 Action Name <br> 컨텐츠을 생성할 때 사용될 등록된 클래스 명 */
	private String action;

	/** Action클래스에서 사용 될 파라미터 리스트*/
	private HashMap parameters;

	/** 
	 * Agent 객체를 생성한다.
	 * @param id agent를 구분하기 위한 컨텐츠내의 순서 번호
	 * @param action
	 * @param parameters
	 */
	public Agent(int id, String action, HashMap parameters) {
		this.id = id;
		this.action = action;
		this.parameters = parameters;
	}

	/**
	 * Agent의 ID를 얻는다.
	 * Agent의 ID는 순서 번호이다.
	 * @return id
	 */
	public int getID() {
		return id;
	}

	/**
	 * Agent가 실행할 Class의 Name를 얻는다.
	 * @return class의 Name
	 */
	public String getAction() {
		return action;
	}

	/**
	 * Agent의 파라미터를 얻는다.
	 * @return
	 */
	public HashMap getParameters() {
		return parameters;
	}
}