package messager.common;

import java.util.ArrayList;

/**
 * Unit에 대한 발송 정보를 저장한 클래스 발송 정보를 도메인별로 그룹화 하여 저장하는 List이다.
 */
public final class SendUnit extends ArrayList implements java.io.Serializable {

	/** MessageID */
	public String messageID;

	/** UnitID */
	public int unitID;

	public int taskNo;

	public int subTaskNo;

	/** 발송자의 EMAIL Address */
	public String senderEmail;

	/** 첨부 파일 존재 여부 true: 존재 false: 존재 하지 않음 */
	public boolean existsFileContent;

	/** Connection 발송 수 */
	public int connPerCount;

	/** Socket Timeout */
	public int socketTimeout;

	/** 재발송 수 */
	public int retryCount;

	/** Message의 재 발송된 카운트 */
	public int sendNo;

	/** true: 실발송 false: 가상발송 */
	public boolean sendMode;

	/** true: 테스트 발송 */
	public boolean isTest;

	/** 부서번호 */
	public int deptNo;

	/** 사용자 아이디 */
	public String userNo;

	/** 캠페인 타입 번호 */
	public String campaignType;

	/** 캠페인 번호 */
	public int campaignNo;

	/** 발송대상유형 */
	public String target_grp_ty;

	/**
	 * UnitEnvelope 객체를 생성한다.
	 *
	 * @param aMessageID
	 * @param aUnitID
	 * @param aSenderEmail
	 */
	public SendUnit(String messageID, int unitID) {
		super();
		this.messageID = messageID;
		this.unitID = unitID;
	}

	public String getName() {
		return messageID + "^" + Integer.toString(unitID);
	}
}

