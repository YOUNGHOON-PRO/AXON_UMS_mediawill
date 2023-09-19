package messager.common;

/**
 * 대상자의 정보를 저장한다. 정보는 String 배열로 저장되고 
 * 배열의 인덱스 0은 사용되지 않는다.
 * 배열은 NEO_SEGEMENT.MERGE_KEY 의 순서로 채워진다.
 */
public class ReceiverInfo implements java.io.Serializable {
	/** 
	 * 대상자의 정보가 저장될 String 배열
	 */
	private String[] columns;

	/**
	 * 컨텐츠에 Agent가 포함될 경우 Agent의 결과가 저장될 배열 
	 * 머지 할 경우 agent의 id로 배열의 인덱스에서 얻어와서 채운다.
	 * 컨텐츠 생성하는 Generator에서만 사용되므로 객체 직렬화의 대상에서 제외된다.
	 */
	private transient String[] agentArray;
	
	/**
	 * 컨텐츠에 Agent2가 포함될 경우 Agent의 결과가 저장될 배열 
	 * 머지 할 경우 agent의 id로 배열의 인덱스에서 얻어와서 채운다.
	 * 컨텐츠 생성하는 Generator에서만 사용되므로 객체 직렬화의 대상에서 제외된다.
	 */
	private transient String[] agentArray2;
	



	// sjlee
	public String is_htx_update = "N";



	/**
	 * ReceiverInfo 객체를 생성한다.
	 * 
	 * @param columns 대상자의 정보를 포함한 배열
	 */
	public ReceiverInfo(String[] columns) {
		this.columns = columns;
	}

	/**
	 * 대상자의 정보중 지정한 컬럼을 얻는다.
	 * 
	 * @param col 얻을려고 하는 컬럼의 번호
	 * @return 대상자의 정보중 지정한 컬럼을 리턴한다
	 */
	public String getColumn(int col) {
		if (col >= 0 && col < columns.length) {
			return columns[col];
		}
		return null;
	}

	/**
	 * 대상자의 정보를 배열로 얻는다.
	 * 
	 * @return 대상자의 정보가 저장된 String 배열
	 */
	public String[] getColumns() {
		return columns;
	}

	/** 
	 * 컨텐츠에 Agent가 포함 될 경우 Agent의 결과가 저장될 배열을 초기화 한다.
	 * 
	 * @param size 컨텐츠에 포함된 Agent의 최대 수
	 */
	public void initAgent(int size) {
		agentArray = new String[size];
	}

	/** 
	 * 컨텐츠에 Agent가 포함 될 경우 Agent의 결과가 저장될 배열을 초기화 한다.
	 * 
	 * @param size 컨텐츠에 포함된 Agent의 최대 수
	 */
	public void initAgent2(int size) {
		agentArray2 = new String[size];
	}
	
	
	/** 
	 * 지정된 배열의 인덱스(Agent의 ID) 에 Agent가 생성한 결과를 저장한다.
	 * 컨텐츠 머지를 하기 전에 Agnet가 실행되어서 결과를 저장 시켜 좋는다.
	 * 
	 * @param id AgentID
	 * @param value Agent가 리턴한 결과
	 */
	public void putAgent(int id, String value) {
		if (agentArray != null) {
			agentArray[id] = value;
		}
	}
	
	/** 
	 * 지정된 배열의 인덱스(Agent의 ID) 에 Agent가 생성한 결과를 저장한다.
	 * 컨텐츠 머지를 하기 전에 Agnet가 실행되어서 결과를 저장 시켜 좋는다.
	 * 
	 * @param id AgentID
	 * @param value Agent가 리턴한 결과
	 */
	public void putAgent2(int id, String value) {
		if (agentArray2 != null) {
			agentArray2[id] = value;
		}
	}

	/** 
	 * Agent의 ID로 Agent가 실행결과를 저장한 배열에서 결과를 얻는다.
	 * 컨텐츠 머지할 때 호출된다.
	 * 
	 * @param id
	 * @return
	 */
	public String getAgent(int id) {
		if (agentArray == null)
			return null;

		return agentArray[id];
	}
	
	
	/** 
	 * Agent의 ID로 Agent가 실행결과를 저장한 배열에서 결과를 얻는다.
	 * 컨텐츠 머지할 때 호출된다.
	 * 
	 * @param id
	 * @return
	 */
	public String getAgent2(int id) {
		if (agentArray2 == null)
			return null;

		return agentArray2[id];
	}
}
