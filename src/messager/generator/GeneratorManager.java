package messager.generator;

import java.util.*;

import com.enders.client.bulk.BulkInternalSendAgent;

import messager.common.*;
import messager.generator.config.ConfigLoader;
import messager.generator.repository.UnitInfoFile;
import messager.generator.send.SendQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 컨텐츠 생성 쓰레드를 실행하고 실행된 쓰레드를 관리한다.
 */
public class GeneratorManager {
	
	private static final Logger LOGGER = LogManager.getLogger(GeneratorManager.class.getName());
	
	private static GeneratorManager instance;

	/** 컨테츠 생성 쓰레드들의 그룹 */
	private ThreadGroup runnerGroup;

	/** 컨텐츠 생성 쓰레드의 최대 카운트 */
	private int maxCount;

	// 생성중인 Unit를 저장한다.
	// Thread 객체 실행 시 저장하고, Thread가 실행 완료후 제거한다.
	private HashMap unitMap;

	/** 인스턴스를 얻는다 */
	public static GeneratorManager getInstance() {
		synchronized (GeneratorManager.class) {
			if (instance == null) {
				int count = ConfigLoader.getInt("generator.max.count", 2);

				instance = new GeneratorManager(count);
			}
		}
		return instance;
	}

	/**
	 * 객체를 생성한다
	 *
	 * @param count Thread의 최대 카운트
	 */
	private GeneratorManager(int count) {
		runnerGroup = new ThreadGroup("Content_Generator");
		maxCount = count;
		unitMap = new HashMap();
	}

	/**
	 * Unit에 대한 컨텐츠 생성작업이 진행 되는지 확인한다.
	 *
	 * @param unitName
	 *            messageID^unitID
	 * @return 현재 컨텐츠 생성 작업이 진행 되고 있으면 true
	 */
	public boolean isRunningUnit(String unitName) {
		return unitMap.containsKey(unitName);
	}

	/**
	 * 컨텐츠 생성 작업 Thread를 실행 시킬 여유가 있는 지 확인
	 *
	 * @return false if activeCount < maxCount
	 */
	public boolean isFulled() {
		boolean b_Full = true;
		int activeCount = runnerGroup.activeCount();

		if (activeCount < maxCount) {
			b_Full = false;
		}
		return b_Full;
	}

	/**
	 * Generator Thread 를 실행한다.
	 *
	 * @param unitInfo
	 *            컨텐츠를 생성시킬 대상자의 정보
	 * @param messageInfo
	 *            message정보 (템플릿[첨부파일], 머지키(발송자 정보 포함), 메일 헤더 내용)
	 */
		public void runUnit(UnitInfo unit, MessageInfo messageInfo) {
		String messageID = unit.getMessageID();
		int unitID = unit.getUnitID();
		String name = unit.getName();

		UnitGenerator generator = new UnitGenerator(runnerGroup, name, unit, messageInfo);
		generator.start();
	}

	/**
	 * Unit에 대한 컨텐츠를 생성하는 쓰레드
	 */
	class UnitGenerator extends Thread {
		private final static String THTREAD_NAME = "generator_unit";

		/** 대상자의 발송 정보(머지 데이타)를 저장한 Unit */
		private UnitInfo unit;

		/** 컨텐츠를 제외한 Message의 정보를 저장 */
		private Message message;

		/** Message의 컨텐츠(템플릿, 첨부파일들)정보를 포함하고 있다. */
		private Contents contents;

		/** Message의 컨텐츠2(템플릿, 첨부파일들)정보를 포함하고 있다. */
		private Contents2 contents2;
		
		/**
		 * UnitRunner 객체를 생성한다.
		 *
		 * @param group
		 *            UnitGenerator 쓰레드를 관리하는 ThreadGroup객체
		 * @param name
		 *            Thread Name으로 UnitName(messageID^unitID)를 사용한다.
		 * @param aUnitInfo
		 *            대상자 정보를 담고 있는 UnitInfo 객체
		 * @param aMessageInfo
		 *            Message정보
		 */
		public UnitGenerator(ThreadGroup runnerGroup,
                                     String name,
                                     UnitInfo unit,
                                     MessageInfo messageInfo) {
                        //주어진 그룹에 유일이름으로 쓰레드를 등록한다.
			super(runnerGroup, name);
                        //컨텐츠 생성을 위한 정보
			this.unit = unit;
			message = messageInfo.getMessage();
			contents = messageInfo.getContents();
			contents2 = messageInfo.getContents2();
		}

		/**
		 * 쓰레드를 실행한다.(해당 unit에 대한 컨텐츠 생성)
		 */
		public void run() {
			String unitName = unit.getName();

			//작업중인 Unit를 관리하는 저장소에 UnitName(MessageID^UnitID)를 저장한다.(중복으로
			// 실행되는것을 피하기 위해)
			unitMap.put(unitName, this);
			
			ContentGenerator generator =null;
			try {
				//컨텐츠 생성객체를 생성한다.
				
				// 웹에이전트 보안 HTML
				if("Y".equals(message.webagent_secuYn) && message.webagent_sourceUrl != null && "HTML".equals(message.webagent_secuAttTyp)) {
					generator  = new ContentGenerator(unit, message, contents, contents2);
				
				// 웹에이전트 보안 PDF
				}else if("Y".equals(message.webagent_secuYn) && message.webagent_sourceUrl != null && "PDF".equals(message.webagent_secuAttTyp)) {
					generator  = new ContentGenerator(unit, message, contents, contents2);
				
				// 웹에이전트 보안 EXCEL
				}else if("Y".equals(message.webagent_secuYn) && message.webagent_sourceUrl != null && "EXCEL".equals(message.webagent_secuAttTyp)) {
					generator  = new ContentGenerator(unit, message, contents, contents2);
				
				}else {
					generator  = new ContentGenerator(unit, message, contents);
				}

				//Unit에 대한 컨텐츠를 생성한 후 발송 정보인 UnitEnvelope객체를 리턴 받는다.
				boolean addValue = generator.createUnit();
				//UnitInfo 객체 파일을 삭제한다. mcf 컨텐츠 생성이 완료된 UnitInfo 객체 파일을 삭제하기 위하여 호출된다.
				UnitInfoFile.deleteUnit(unitName);

				//윤노과장이 개발한 파일매니저를 통해 내부에서 외부서버로 파일 전송을 진행 후 SendQueue 에 적재를 한다.
				if (addValue) {
					String taskId = unitName.split("-")[0];
					BulkInternalSendAgent agent = new BulkInternalSendAgent(unitName);
					boolean state = agent.run();
					if ( state ) { 
						SendQueue.push(unitName);  
					} else { 
						state = agent.run();
						if ( state ) {
							SendQueue.push(unitName);  
						}else {
							throw new Exception (String.format("TaskID : %s, 파일 전송이 이뤄지지 않았습니다.", taskId));
						};
					}
				
//					SendQueue.push(unitName);  
				
				}
			} catch (Exception ex) {
				LOGGER.error(ex);
				//ex.printStackTrace();
			}


			//작업중인 Unit를 관리하는 저장소에서 Unit 제거
			unitMap.remove(unitName);
		}
	}
}
