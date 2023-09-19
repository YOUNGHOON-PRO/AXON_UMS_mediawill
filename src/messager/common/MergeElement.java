package messager.common;

/**
 * Merge를 위한 MergeKey와 일반 텍스트를 저장한 클래스로 MergeKey와 일반 텍스트를 type으로 구분한다.
 */
public final class MergeElement implements java.io.Serializable {
	/** Normal Text */
	public static final int TEXT_TYPE = 0;

	/** MergeKey */
	public static final int MERGE_TYPE = 1;

	/** Agent */
	public static final int AGENT_TYPE = 2;

	/** 일반 텍스트또는 MergeKey의 key Name 
	 *  Agent 일경우 AgentAction을 나타내는 Class Name
	 */
	public String text; 

	/** 일반 텍스트와 MergeKey, Agent를 구분한다. */
	public int type; 

	/** 
	 * MergeKey일 경우 대상자 정보를 저장한 배열의 인덱스이고
	 * Agent 일 경우 Agent의 id
	 * 일반 텍스트 일경우 -1
	 */
	public int index; 

	/**
	 * MergeElement 객체를 생성한다.
	 * 
	 * @param aText MergeKey 또는 텍스트
	 * @param aType type
	 * @param aIndex 대상자정보를 저장한 배열에서 Index(MergeKey가 아닐 경우 -1)
	 */
	public MergeElement(String aText, int aType, int aIndex) {
		text = aText;
		index = aIndex;
		type = aType;
	}

	/**
	 * 머지 항목이 아닌 텍스트에 대해서 MergeElement 객체를 생성한다.
	 * 
	 * @param aText 텍스트
	 */
	public MergeElement(String aText) {
		this(aText, TEXT_TYPE, -1);
	}

	/**
	 * Agent 항목에 대한 MergeElement 객체를 생성한다.
	 * 
	 * @param agentAction 
	 * @param agentID
	 */
	public MergeElement(String agentAction, int agentID) {
		this(agentAction, AGENT_TYPE, agentID);
	}
}