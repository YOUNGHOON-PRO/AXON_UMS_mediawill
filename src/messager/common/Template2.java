package messager.common;

import java.util.ArrayList;
import java.net.URLEncoder;

/**
 * 머지가 필요한 컨텐츠 또는 머지가 필요한 텍스트를 MergeElement로 구성하여
 * 리스트 한 클래스, 여기에는 Mesage의 정보는 컨텐츠 분석시 Replace 하므로
 * 포함되지 않는다.
 *
 * 대상자의 정보인 ReceiverInfo객체를 이용하여 대상자의 MergeElement를 대상자의
 * 정보로 Replace하여 StringBuffer에 순서대로 append 하여 머지를 실행한다.
 * @author Administrator
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Template2 extends ArrayList implements java.io.Serializable {
	/** 객체를 생성한다 */
	public Template2() {
		super();
	}

	/**
	 * 머지를 실행한다.
	 * 리스트에서 MergeElemnet 객체를 순서대로 가져와서 대상자의 정보이면 ReceiverInfo 객체
	 * 에서 정보를 꺼내와서 Replace하여 StringBuffer에 Append하고 일반 텍스트이면 텍스트를
	 * Append한다.
	 *
	 * @param receiver 대상자의 정보를 담고 있는 ReceiverInfo객체
	 * @return 머지가 실행된 컨텐츠
	 * @throws MergeException 머지할 대상자의 정보가 존재하지 않을 경우
	 */
	public String create(ReceiverInfo receiver) throws MergeException {
		String[] columns = receiver.getColumns(); //[null, hun1110@enders.co.kr, 김순대, test1, 30, Male, serviece]

    StringBuffer stringbuffer = new StringBuffer();
		int size = size();

		for (int i = 0; i < size; i++) {
			
			Object obj = get(i);
			if (obj instanceof MergeElement) {
				
				MergeElement element = (MergeElement) obj;
				
				if (element.type == MergeElement.TEXT_TYPE) {
					stringbuffer.append(element.text);
				
				
				} else if (element.type == MergeElement.AGENT_TYPE) {
					int index = element.index;
					String string = receiver.getAgent2(index);
					if (string == null) {
						throw new MergeException("Not Found Agent Content");
					}
					stringbuffer.append(string);
				
				
				} else {
					int index = element.index;
					String string = columns[index];  
					if (string == null) {
						throw new MergeException("Merge element empty");
					} else {
						stringbuffer.append(string);
					}
				}
			} else if (obj instanceof Agent) {
				Agent agent = (Agent) obj;
				int id = agent.getID();

				String value = receiver.getAgent2(id);
				if (value == null) {
					throw new MergeException("Not Found Agent Content");
				} else {
					if (value instanceof String) {
						stringbuffer.append((String) value);
					} else {
						throw new MergeException("Not Found Agent Type: "
								+ value.getClass().getName());
					}
				}
			} else {
				throw new MergeException("Not Found Type: " + obj.getClass().getName());
			}
		}

		return stringbuffer.toString();
	}

	/**
	 * 웹 에이전트 머지를 수행한다.
	 * 머지를 실행한다.
	 * 리스트에서 MergeElemnet 객체를 순서대로 가져와서 대상자의 정보이면 ReceiverInfo 객체
	 * 에서 정보를 꺼내와서 Replace하여 StringBuffer에 Append하고 일반 텍스트이면 텍스트를
	 * Append한다.
	 *
	 * @param receiver 대상자의 정보를 담고 있는 ReceiverInfo객체
	 * @return 머지가 실행된 컨텐츠
	 * @throws MergeException 머지할 대상자의 정보가 존재하지 않을 경우
	 */
	public String createURL(ReceiverInfo receiver) throws MergeException {
		String[] columns = receiver.getColumns();

    StringBuffer stringbuffer = new StringBuffer();
		int size = size();

		for (int i = 0; i < size; i++) {
			Object obj = get(i);
			if (obj instanceof MergeElement) {
				
				MergeElement element = (MergeElement) obj;
				
				if (element.type == MergeElement.TEXT_TYPE) {
					stringbuffer.append(element.text);
					
					
				} else if (element.type == MergeElement.AGENT_TYPE) {
					int index = element.index;
					String string = receiver.getAgent2(index);
					if (string == null) {
						throw new MergeException("Not Found Agent Content");
					}
					stringbuffer.append(string);
					
					
				} else {
					int index = element.index;
					String string = columns[index];
					if (string == null) {
						throw new MergeException("Merge element empty");
					} else {
						string = URLEncoder.encode(string);
						stringbuffer.append(string);
					}
				}
			} else if (obj instanceof Agent) {
				Agent agent = (Agent) obj;
				int id = agent.getID();

				String value = receiver.getAgent2(id);
				if (value == null) {
					throw new MergeException("Not Found Agent Content");
				} else {
					if (value instanceof String) {
						stringbuffer.append((String) value);
					} else {
						throw new MergeException("Not Found Agent Type: "
								+ value.getClass().getName());
					}
				}
			} else {
				throw new MergeException("Not Found Type: " + obj.getClass().getName());
			}

		}

		return stringbuffer.toString();
	}
}
