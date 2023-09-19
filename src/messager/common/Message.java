package messager.common;

import java.util.HashMap;

import java.util.ArrayList;

/**
 * Message의 발송 정보와 컨텐츠 생성시 필요한 정보가 포함된다.
 *
 * @author Administrator
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public final class Message implements java.io.Serializable {
	/**
	 * Message의 식별명 <br>
	 * taskNo + '-' + subTaskNo로 이루어진다.
	 */
	public String messageID;

	/** 업무번호 */
	public int taskNo;

	/** 보조 업무 번호 */
	public int subTaskNo;

	/** 발송자 명 (NEO_TASK.MAIL_FROM_NM), 헤더의 From 필드 */
	public String fromName;

	/** 발송자 이메일 (NEO_TASK.MAIL_FROM_EM) 헤더의 From 필드*/
	public String fromEmail;

	/** 회신 이메일 (NEO_TASK.REPLY_TO_EM) 헤더의 REPLY-TO 필드 */
	public String replyTo;

	/** 리턴 이메일 주소(NEO_TASK.RETURN_EM) SMTP의 MAIL FROM명령의 파라미터 */
	public String returnPath;

	/** 헤더의 To필드 (NEO_TASK.SEND_TO_NM_MERGE) 수신자 명의 접미사 ==> 사용안함 writed by 오범석 */
	public String toNameSuffix;

	/** 헤더의 To필드 (NEO_TASK.NM_MERGE) 수신자 명*/
	public Template toName;

	/** 헤더의 Subject (NEO_TASK.SUBJECT) 머지를 위해 Template 객체로 가져온다.*/
	public Template subject;

	/** Socket Timeout (NEO_TASK.SOCKET_TIMEOUT) Mail Sender */
	public int socketTimeout;

	/** Connection 당 발송 메일 수 (NEO_TASK.CONN_PER_CNT) Mail Sender */
	public int connPerCount;

	/** 재발송 수 (NEO_TASK.RETRY_CNT) */
	public int retryCount;

	/** 2006-11-24 복호화여부 (NEO_SEGMENT.DECODE) */
	public String decode;


	/**
	 * 메일 헤더의 인코딩 타입 (0: 8bit, 2: base64)[HEADER_ENC]
         * 수정 : 2004.10.13
         * 인코딩 타입이 바로 저장됨.
	 */
        public String headerEncodingCode;

	/**
	 * 본문의 인코딩 타입 (0: 8bit, 2: base64) [BODY_ENC]
	 * 첨부파일들은 모두 base64로 인코딩 된다.
         * 수정 : 2004.10.13
         * 인코딩 타입이 바로 저장됨.
	 */
	public String bodyEncodingCode;

	/**
	 * 메일 컨텐츠의 charset [CHAR_SET]
         * 수정 : 2004.10.13
         * 인코딩 타입이 바로 저장됨.
	 */
	public String charsetCode;

	/** true: 실제 발송, false: 가상 발송 */
	public boolean sendMode;

	/** true: 테스트 모드 */
	public boolean isTest;

	/** 테스트 발송시 발송 할 메일 주소 */
	public String testTo[];

	/** 테스트 발송대상자수 */
	public int send_test_cnt;

	/** 캠페인 타입 (NEO_TASK.CAMP_TY] */
	public String campaignType;

	/** 캠페인 번호 (NEO_TASK.CAMP_NO] */
	public int campaignNo;

	/** 등록자 (NEO_TASK.USER_ID] */
	public String userNo;

	/** 부서 번호 [NEO_TASK.DEPT_NO] */
	public int deptNo;

	/** 컨텐츠에 Agent가 존재할때 Agent 객체가 저장된다 */
	public ArrayList agentList;
	
	/** 컨텐츠에 Agent2가 존재할때 Agent 객체가 저장된다 */
	public ArrayList agentList2;

	/** 발송대상유형	*/
	public String target_grp_ty;
	
	/** 보안메일 웹에이전트 attach no*/
	public String webagent_attNo;
	
	/** 보안메일 웹에이전트 sourceUrl*/
	public String webagent_sourceUrl;
	
	/** 보안메일 웹에이전트 secyYn*/
	public String webagent_secuYn;
	
	/** 보안메일 웹에이전트 secuAttTyp*/
	public String webagent_secuAttTyp;
	
	/** 마케팅수신동의 체크*/
	public String mail_mkt_gb;
	
	/** 개인정보체크(제목)*/
	public String title_chk_yn;
	
	/** 개인정보체크(본문)*/
	public String body_chk_yn;
	
	/** 개인정보체크(일반첨부파일)*/
	public String attach_file_chk_yn;
	
	/** 개인정보체크(보안메일첨부파일)*/
	public String secu_mail_chk_yn;
	
	/** 재발송유형*/
	public String rty_typ;
	
	/** 재발송캠페인업무정의등록번호*/
	public String rty_task_no;
	
	/** 재발송보조업무번호*/
	public String rty_sub_task_no;
	
	/** 재발송대상코드*/
	public String rty_code;
	
	/** 테스트수신ID*/
	public String test_send_id;
	
	
	/**
	 * Merge항목으로 사용될수 있는 메세지의 정보와 대상자의 정보가 저장된다
	 * key에 MergeKey 가 되고 value는 메세지의 정보일 경우 실제 값이 들어가고
	 * 대상자 항목 일 경우 NEO_SEGMENT.MERGE_KEY 의 순서번호가 Integer객체로 저장된다.
	 */
	public HashMap keyMap;

	/**
	 *
	 */
	public long sendTime;

	public long retryEndTime;

	/**
	 * DB에서 검색된 데이터를 임시 저장 <br>
	 * NEO_SUBTASK, NEO_TASK, NEO_SEGMENT테이블 <br>
	 * key는 TableName.FieldName, value는 검색된 데이터 <br>
	 * Message이 가져온 후에는 사용되지 않는다.
	 */
	public transient HashMap taskMap;

	/**
	 * NEO_SUBTASK 검색시 에러코드
	 */
	public transient String errorCode;

	/**
	 * 머지키의 유효성을 검사하기 위해 머지키 수를 임시 저장한다.
	 */
	public transient int keySize;


	/**
	 * 마케팅수신동의 체크중 미동의자들 수집
	 */
	public ArrayList mkttList = new ArrayList();
	
	/**
	 * 마케팅수신동의 체크중 미동의자들의 ID값을 찾기위해 인덱스값 수집
	 */
	public int mktIdIdx; 
	
	
	/**
	 * MessageID로 Message객체를 생성한다.
	 *
	 * @param messageID taskNo + '-' + subTaskNo
	 */
	public Message(String messageID) {
		this.messageID = messageID;
	}
}
