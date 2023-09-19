package messager.generator;

import java.util.HashMap;
import java.util.ArrayList;
import java.io.File;
import messager.common.*;
import messager.common.util.MessageKey;
import messager.generator.repository.UnitContentFile;
import messager.generator.repository.UnitSendLogFile;
import messager.generator.repository.UnitResult;
import messager.generator.repository.SendLogWriter;
import messager.generator.repository.SendUnitFile;
import messager.generator.content.Part;
import messager.generator.content.BodyPart;
import messager.generator.content.MultiPart;
import messager.generator.content.Address;
import messager.generator.content.ErrorCode;
import messager.generator.content.GeneratorException;
import messager.generator.config.ConfigLoader;

/**
 * Unit에 포함된 대상자의 이메일 컨텐츠를 생성한다.
 * @author Administrator
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ContentGenerator {
	private static String logTimeFormat;

	private static String separator = "|";

	static {
		logTimeFormat = ConfigLoader.getString("log.time.format", "yyyyMMddHHmm");
		separator = ConfigLoader.getString("log.field.separator", "|");
	}

	/** Unit */
	private UnitInfo unit;

	/** Message */
	private Message message;

	/** Content */
	private Contents contents;

	/** Content2 */
	private Contents2 contents2;

	
	/** unitName */
	private String unitName;

	/** messageID */
	private String messageID;

	/** unitID */
	private int unitID;

	/** 대상자의 Name 이 존재하는 ReceiverInfo 객체내의 배열의 인덱스 */
	private int toNameNo;

	/** 대상자의 email이 존재하는 배열의 인덱스 */
	private int toEmailNo;

	/** 대상자의 UserID 인덱스 */
	private int toUserIDNo;
	
	/** 대상자의 보안메일 암호 값 인덱스 */
	private int toUserEnckeyNo;
	
	/** 대상자의 EAI연계 메시지 값 인덱스 */
	private int toUserBizkeyNo;

	/** 발송 로그를 Write 할 객체 */
	private SendLogWriter logWriter;

	/** Unit의 컨텐츠 파일을 관리 하는 객체 */
	private UnitContentFile unitContentFile;

	/** Unit의 발송(생성) 결과를 저장한다. */
	private UnitResult unitResult;

	/** Unit의 발송 정보를 저장한다 */
	private SendUnit sendUnit;

	/** Unit의 컨텐츠 생성시 이용 */
	private Part part;

	/** 테스트 발송 이메일 주소 리스트 */
	private ArrayList testList;

	public ContentGenerator(UnitInfo unit, Message message, Contents contents) {
		this.unit = unit;
		this.message = message;
		this.contents = contents;
		this.unitName = unit.getName();
		this.messageID = message.messageID;
		this.unitID = unit.getUnitID();

		HashMap keyMap = message.keyMap;
		toNameNo = intValue(keyMap.get(MessageKey.TO_NAME));
		toEmailNo = intValue(keyMap.get(MessageKey.TO_EMAIL));
		toUserIDNo = intValue(keyMap.get(MessageKey.TO_ID));
		toUserEnckeyNo = intValue(keyMap.get(MessageKey.TO_ENCKEY));
		toUserBizkeyNo = intValue(keyMap.get(MessageKey.TO_BIZKEY));

    /* ===============================================
    StringBuffer sb = new StringBuffer();
    String key = null;
    String value = null;
    java.util.Iterator iter = keyMap.keySet().iterator();
    while(iter.hasNext()){
 	  	key = (String)iter.next();
    	value = (String)keyMap.get(key);
    	sb.append(key).append(" : ").append(value).append("\r\n");
    }
    try{
    	java.io.FileOutputStream fos = new java.io.FileOutputStream(System.currentTimeMillis() + ".html");
    	fos.write(sb.toString().getBytes());
    	fos.flush();
    	fos.close();
    }catch(Exception e){
    	e.printStackTrace();
    }
    ============================================= */
	}

	
	public ContentGenerator(UnitInfo unit, Message message, Contents contents, Contents2 contents2) {
		this.unit = unit;
		this.message = message;
		this.contents = contents;
		this.unitName = unit.getName();
		this.messageID = message.messageID;
		this.unitID = unit.getUnitID();
		this.contents2 = contents2;

		HashMap keyMap = message.keyMap;
		toNameNo = intValue(keyMap.get(MessageKey.TO_NAME));
		toEmailNo = intValue(keyMap.get(MessageKey.TO_EMAIL));
		toUserIDNo = intValue(keyMap.get(MessageKey.TO_ID));
		toUserEnckeyNo = intValue(keyMap.get(MessageKey.TO_ENCKEY));
		toUserBizkeyNo = intValue(keyMap.get(MessageKey.TO_BIZKEY));

	}
	
	/**
	 * Object로 저장된 Int형 데이터를 int 타입으로 변환한다.
	 *
	 * @param intObj
	 * @return
	 */
	private int intValue(Object intObj) {
		int intvalue = -1;

		if (intObj != null && intObj instanceof Integer) {
			intvalue = ((Integer) intObj).intValue();
		}
		return intvalue;
	}

	/**
	 * 대상자의 정보는 채워지지 않고 Message의 발송 정보로 SendUnit객체를 생성한다.
	 *
	 * @return Unit의 발송에 필요한 정보를 저장되는 SendUnit객체
	 */
	private SendUnit createSendUnit() {
		SendUnit sendUnit = new SendUnit(messageID, unitID);
		sendUnit.taskNo = message.taskNo;
		sendUnit.subTaskNo = message.subTaskNo;
		sendUnit.senderEmail = message.returnPath;
		sendUnit.connPerCount = message.connPerCount;
		sendUnit.socketTimeout = message.socketTimeout;
		sendUnit.retryCount = message.retryCount;
		sendUnit.sendMode = message.sendMode;
		sendUnit.isTest = message.isTest;
		sendUnit.deptNo = message.deptNo;
		sendUnit.userNo = message.userNo;
		sendUnit.campaignType = message.campaignType;
		sendUnit.campaignNo = message.campaignNo;
		sendUnit.sendNo = unit.getSendNo();
		sendUnit.target_grp_ty = message.target_grp_ty;

		return sendUnit;
	}

	/**
	 * Unit에 대한 컨텐츠 생성 초기 작업을 실행한다.
	 * SendUnit객체를 생성하고, 대상자의 컨텐츠 생성을 실행할 Part 객체를 생성하고
	 * SendLog 파일을 생성하고, Unit의 발송 결과를 저장할 객체를 생성하고,
	 * 컨텐츠를 저장할 디렉토리를 생성한다.
	 *
	 * @throws Exception
	 */
	private void initUnit() throws Exception {
		
		//Message객체 정보를 sendUnit 객체로 저장
		sendUnit = createSendUnit();
		
		//첨부냐,, 일반메일이냐 구분
		part = createPart(sendUnit);
		
		int sendNo = sendUnit.sendNo;
		int size = unit.size(); //unit 파일안에 대상자 수 (최대 500명)
		
		//sendlog 폴더 생성 및 파일 생성
		File sendLogFile = UnitSendLogFile.unitFile(unitName);
		
		logWriter = new SendLogWriter(sendLogFile, message, sendNo);
		
		unitResult = new UnitResult(messageID, unitID, size, sendNo);
		
		//contet폴더 생성
		unitContentFile = new UnitContentFile(unitName);
		
		//테스트 발송 일 경우
		if (message.isTest) {
			testList = testToAddress(message.testTo);
		}
		
	}

	/**
	 * Unit에 대한 메일 컨텐츠를 생성한다.
	 *
	 * @return 하나이상 대상자의 컨텐츠가 생성되면 true
	 * @exception Exception
	 */
	public boolean createUnit() throws Exception {
		boolean boolValue = true;
		Exception exception = null;

		try {
			initUnit();
			HashMap domainMap = new HashMap();

			for (int i = 0; i < unit.size(); i++) {
				ReceiverInfo receiver = unit.getReceiver(i);
				createReceiver(i, receiver, domainMap);
			}

			boolValue = releaseUnit();
		} catch (Exception ex) {
			exception = ex;
		}

		if (exception != null) {
			throw exception;
		}

		return boolValue;
	}

	/**
	 * Unit에 대한 컨텐츠 생성작업 완료 작업을 한다.
	 * SendUnit객체 write (생성이 모두 실패 하였을 경우 컨텐츠 디렉토리 삭제)
	 * Part 객체 release
	 * SendLog 의 FileWriter close
	 * Unit의 발송 결과 파일 Write
	 *
	 * @return 하나 이상 컨텐츠가 생성되었을 경우 true
	 */
	private boolean releaseUnit() throws Exception {
		boolean boolValue = true;

		if (sendUnit.size() > 0) {
			SendUnitFile.write(sendUnit);  //envelope 폴더 및 파일 생성
		} else {
			
			boolValue = false;
			unitContentFile.delete(); // sendunit 정보가 없다면 mcf 파일및 폴더를 삭제한다.
		}

		if (part != null) part.release();
		if (logWriter != null) 	logWriter.close();
		if (unitResult != null) unitResult.writeToFile(unitName); //unitlog 로그 파일 생성

		return boolValue;
	}

	/**
	 * 대상자의 발송 정보가 저장되는 SendTo 객체를 생성한다.
	 *
	 * @param rowID Unit에 대상자의 정보가 저장된 인덱스(순서번호)로 컨텐츠 파일을 읽는데 이용된다.
	 * @param receiver 대상자의 정보
	 * @return SendTo객체
	 */
	private SendTo sendTo(int rowID, ReceiverInfo receiver) {
		String toName = receiver.getColumn(toNameNo);
		String toEmail = receiver.getColumn(toEmailNo);
 		String toUserID = receiver.getColumn(toUserIDNo);
		String toUserEnckey ="";
 		String toUserBizkey ="";
		
		if(toUserEnckeyNo == -1) {
			toUserEnckey = "NoENCKEY";	
		}else {
			toUserEnckey = receiver.getColumn(toUserEnckeyNo);
		}
		
		if(toUserBizkeyNo == -1) {
			toUserBizkey = "NoEAI";
		}else {
			toUserBizkey = receiver.getColumn(toUserBizkeyNo);
		}
		
		//return new SendTo(rowID, toUserID, toEmail, toName);
		return new SendTo(rowID, toUserID, toEmail, toName, toUserEnckey, toUserBizkey);
	}

	/**
	 * 대상자의 컨텐츠를 생성한다.
	 *
	 * @param rowNo 컨텐츠 파일의 파일명으로 이용된다.
	 * @param receiver	대상자의 정보
	 * @param domainMap 도메인 그룹화를 위해 사용된다.
	 */
	private void createReceiver(int rowNo,
                                    ReceiverInfo receiver,
                                    HashMap domainMap) {
		Exception ex = null;
		SendTo toUser = sendTo(rowNo, receiver);
		File contentFile = unitContentFile.contentFile(rowNo);  //.\repository\transfer\content\313-1^1\0.mcf

		//테스트 발송 여부를 저장한다.
		String testYN = "false";

		try {
			Address toAddress = part.create(receiver, contentFile, toUser);  // 메일컨텐츠 생성 
			if (testList == null) {
				String domainName = toAddress.domain;
				putToUser(domainName, domainMap, sendUnit, toUser);
			} else {
				// 테스트 발송
				testYN = "true";
				for (int i = 0; i < testList.size(); i++) {
					Address address = (Address) testList.get(i);
					SendTo testToUser = new SendTo(rowNo);

					testToUser.email = address.email;
					testToUser.id = toUser.id;
					testToUser.name = toUser.name;
					testToUser.encKey = toUser.encKey;
					testToUser.bizKey = toUser.bizKey; 
					putToUser(address.domain, domainMap, sendUnit, testToUser);
				}
			}
		} catch (Exception exception) {
			ex = exception;
		}
		if (ex != null) {

			ErrorCode errorCode = ErrorCode.UNKNOWN_CHANNEL;
			if (ex instanceof GeneratorException) {
				errorCode = ((GeneratorException) ex).getErrorCode();
			}
			if (logWriter != null) {
				
				//테스트발송중에 고객정보가 포함되던지 exception발생하면 여기서 test발송인지 확인하여 체크함 
				if(testList!=null) {
					testYN = "true";
				}
				logWriter.write(testYN,toUser, errorCode, ex.getMessage());
			}

			if (unitResult != null) {
				int code = 2;
				unitResult.putRecord(rowNo, code);
			}
		}
	}

	/**
	 * Unit의 발송 정보에 대상자의 발송 정보를 채운다.
	 *
	 * @param domainName	대상자 이메일 주소의 도메인명
	 * @param domainMap		도메인 그룹화를 위해 사용된다.
	 * @param unit			Unit의 발송 정보를 저장할 SendUnit객체
	 * @param sendTo		대상자의 발송 정보
	 */
	private void putToUser(String domainName, HashMap domainMap, SendUnit unit,
			SendTo sendTo) {
		SendDomain sendDomain = (SendDomain) domainMap.get(domainName);
		
		if (sendDomain == null) {
			sendDomain = new SendDomain(domainName);
			unit.add(sendDomain);
			domainMap.put(domainName, sendDomain);
		}
		sendDomain.addSendTo(sendTo);
	}

	/**
	 * 컨텐츠를 생성할 Part 객체를 생성한다.
	 *
	 * @param sendUnit Unit의 발송 정보를 저장한 객체
	 * @return	Part 객체로 첨부 파일이 포함되었을 경우 MultiPart 객체가 리턴된다
	 * @throws Exception
	 */
	private Part createPart(SendUnit sendUnit) throws Exception {
		TemplateContent mainContent = contents.getMainContent();
		
		
		TemplateContent2 mainContent2 =null;
		
		// 웹에이전트 보안 HTML
		if("Y".equals(message.webagent_secuYn) && message.webagent_sourceUrl != null && "HTML".equals(message.webagent_secuAttTyp)) {
			mainContent2 = contents2.getMainContent2(); 
		
		// 웹에이전트 보안 PDF
		}else if("Y".equals(message.webagent_secuYn) && message.webagent_sourceUrl != null && "PDF".equals(message.webagent_secuAttTyp)) {
			mainContent2 = contents2.getMainContent2();
		
		// 웹에이전트 보안 EXCEL
		}else if("Y".equals(message.webagent_secuYn) && message.webagent_sourceUrl != null && "EXCEL".equals(message.webagent_secuAttTyp)) {
			mainContent2 = contents2.getMainContent2();
		
		}
		
		
		ArrayList templateList = contents.getTemplateList();
		ArrayList fileList = contents.getFileList();
		Part part = null;

		if (contents.isMultipart()) {
			
			// 웹에이전트 보안 HTML
			if("Y".equals(message.webagent_secuYn) && message.webagent_sourceUrl != null && "HTML".equals(message.webagent_secuAttTyp)) {
				part = new MultiPart(message, mainContent, templateList, fileList, mainContent2);
				sendUnit.existsFileContent = ((MultiPart) part).writeAttachFileList(unitName);
			
			// 웹에이전트 보안 PDF	
			}else if("Y".equals(message.webagent_secuYn) && message.webagent_sourceUrl != null && "PDF".equals(message.webagent_secuAttTyp)) {
				part = new MultiPart(message, mainContent, templateList, fileList, mainContent2);
				sendUnit.existsFileContent = ((MultiPart) part).writeAttachFileList(unitName);
			
			// 웹에이전트 보안 EXCEL
			}else if("Y".equals(message.webagent_secuYn) && message.webagent_sourceUrl != null && "EXCEL".equals(message.webagent_secuAttTyp)) {
				part = new MultiPart(message, mainContent, templateList, fileList, mainContent2);
				sendUnit.existsFileContent = ((MultiPart) part).writeAttachFileList(unitName);
			
			}else {
				part = new MultiPart(message, mainContent, templateList, fileList);
				sendUnit.existsFileContent = ((MultiPart) part).writeAttachFileList(unitName);
			}
			
		} else {
			// 웹에이전트 보안 HTML
			if("Y".equals(message.webagent_secuYn) && message.webagent_sourceUrl != null && "HTML".equals(message.webagent_secuAttTyp)) {
				part = new BodyPart(message, mainContent, mainContent2);
			
			// 웹에이전트 보안 PDF
			}else if("Y".equals(message.webagent_secuYn) && message.webagent_sourceUrl != null && "PDF".equals(message.webagent_secuAttTyp)) {
				part = new BodyPart(message, mainContent, mainContent2);
			
			// 웹에이전트 보안 EXCEL
			}else if("Y".equals(message.webagent_secuYn) && message.webagent_sourceUrl != null && "EXCEL".equals(message.webagent_secuAttTyp)) {
				part = new BodyPart(message, mainContent, mainContent2);
			
			}else {
				part = new BodyPart(message, mainContent);
			}
			
		}

		return part;
	}

	/**
	 * 테스트 발송 업무일 경우 테스트메일 주소에 대한 유효성을 체크해 잘못된 메일 일 경우
	 * 제거한다. 단순 유효성만 체크한다.
	 *
	 * @param str 테스트 메일 주소를 저장한 배열
	 * @return
	 */
	private ArrayList testToAddress(String[] str) {
		ArrayList list = new ArrayList();

		for (int i = 0; i < str.length; i++) {

			try {
				Address address = new Address(str[i]);

				list.add(address);
			} catch (Exception ex) {
			}
		}
		return list;
	}
}
