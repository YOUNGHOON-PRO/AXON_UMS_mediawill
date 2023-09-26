package messager.generator.content;

import java.io.*;
import java.util.*;

import messager.common.*;
import messager.generator.agent.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class Part
{
	
	private static final Logger LOGGER = LogManager.getLogger(Part.class.getName());
	
    /** 컨텐츠의 라인 구분 String */
    protected final static String lineSeparator = "\r\n";

    /**
     * Message객체 <br>
     * Messge에 대한 정보가 포함된다
     */
    protected Message message;

    private ArrayList agentActionList;
    
    private ArrayList agentActionList2;


    private GeneratorException agentException;

    // sjlee
    private ReceiverInfo receiverInfo;

    public Part(Message message) {
        this.message = message;
        initAgent(message);
    }

    /**
     * 대상자의 정보로 메일 발송 컨텐츠를 생성하여 지정된 파일에 저장한다.
     *
     * @param receiverInfo
     *            대상자의 정보를 저장한 ReceiverInfo객체
     * @param contentFile
     *            메일 발송 컨텐츠를 저장할 파일
     * @param toUser
     *            대상자의 발송 정보를 저장한다.
     * @return Address 대상자의 email Address를 저장한 Address 객체
     * @throws GeneratorException
     *             컨텐츠 생성중 에러가 발생할 경우
     */
    protected abstract Address createContent(ReceiverInfo receiverInfo, File contentFile, SendTo toUser, Message message)  throws GeneratorException;

    public Address create(ReceiverInfo receiverInfo, File contentFile,
                          SendTo toUser)
        throws GeneratorException {
        createAgent(receiverInfo);
   
        // 웹에이전트 보안 HTML
        if("Y".equals(message.webagent_secuYn) && message.webagent_sourceUrl != null && "HTML".equals(message.webagent_secuAttTyp)) {
        	createAgent2(receiverInfo);
        
        // 웹에이전트 보안 PDF	
        }else if("Y".equals(message.webagent_secuYn) && message.webagent_sourceUrl != null && "PDF".equals(message.webagent_secuAttTyp)) {
        	createAgent2(receiverInfo);
        
        // 웹에이전트 보안 EXCEL
        }else if("Y".equals(message.webagent_secuYn) && message.webagent_sourceUrl != null && "EXCEL".equals(message.webagent_secuAttTyp)) {
        	createAgent2(receiverInfo);
        }

        // sjlee
        this.receiverInfo = receiverInfo;

        return createContent(receiverInfo, contentFile, toUser, message);
    }

    /**
     * 생성된 컨텐츠를 지정된 파일에 write한다.
     *
     * @param contentFile
     *            컨텐츠를 저장할 File
     * @param content
     *            생성된 컨텐츠
     * @param javaCharsetName
     *            컨텐츠의 charset
     * @throws GeneratorException
     *             write시 에러 발생할 경우
     */
    protected void writeContent(File contentFile, String content,
                                String javaCharsetName)
        throws GeneratorException {

        //System.out.println("----------------- File Name = > " + contentFile.getName());


        Writer out = null;
        Exception ex = null;
        try {
            out = new OutputStreamWriter(new BufferedOutputStream(
                new FileOutputStream(contentFile)), javaCharsetName);

            out.write(content);
        }
        catch (Exception exception) {
        	LOGGER.error(exception);
            ex = exception;
        }
        finally {
            if (out != null) {
                try {
                    out.close();
                }
                catch (IOException excep) {
                	LOGGER.error(excep);
                }
                out = null;
            }
        }
        if (ex != null) {
            throw new GeneratorException(ErrorCode.UNKNOWN_CHANNEL, ex
                                         .getMessage());
        }
    }

    public void initAgent(Message message) {
        ArrayList agentList = message.agentList;
        ArrayList agentList2 = message.agentList2;
        
        
        if ((agentList == null || agentList.size() == 0) && (agentList2 == null || agentList2.size() == 0)) {
            return;
        }
        String agentName = null;
        String agentName2 = null;
        Exception exception = null;
        
        try {
        	if(agentList != null) {
        		agentActionList = new ArrayList(agentList.size());	
        		for (int i = 0; i < agentList.size(); i++) {
                    Agent agent = (Agent) agentList.get(i);
                    agentName = agent.getAction();
                    Action action = Action.createAction(agent);
                    agentActionList.add(action);
                }
        	}
            
        	// 웹에이전트 보안 HTML
            if("Y".equals(message.webagent_secuYn) && message.webagent_sourceUrl != null && "HTML".equals(message.webagent_secuAttTyp)) {
            	if(agentList2 != null) {
            	    agentActionList2 = new ArrayList(agentList2.size());
                    //보안메일 적용
                    for (int i = 0; i < agentList2.size(); i++) {
                        Agent agent2 = (Agent) agentList2.get(i);
                        agentName2 = agent2.getAction();
                        Action action2 = Action.createAction(agent2);
                        agentActionList2.add(action2);
                    }
            	}
            
            // 웹에이전트 보안 PDF
            }else if("Y".equals(message.webagent_secuYn) && message.webagent_sourceUrl != null && "PDF".equals(message.webagent_secuAttTyp)) {
            	if(agentList2 != null) {
            	    agentActionList2 = new ArrayList(agentList2.size());
                    //보안메일 적용
                    for (int i = 0; i < agentList2.size(); i++) {
                        Agent agent2 = (Agent) agentList2.get(i);
                        agentName2 = agent2.getAction();
                        Action action2 = Action.createAction(agent2);
                        agentActionList2.add(action2);
                    }
            	}
            
            // 웹에이전트 보안 EXCEL
            }else if("Y".equals(message.webagent_secuYn) && message.webagent_sourceUrl != null && "EXCEL".equals(message.webagent_secuAttTyp)) {
            	if(agentList2 != null) {
            	    agentActionList2 = new ArrayList(agentList2.size());
                    //보안메일 적용
                    for (int i = 0; i < agentList2.size(); i++) {
                        Agent agent2 = (Agent) agentList2.get(i);
                        agentName2 = agent2.getAction();
                        Action action2 = Action.createAction(agent2);
                        agentActionList2.add(action2);
                    }
            	}
            
            }

            
        }
        catch (Exception ex) {
        	LOGGER.error(ex);
            exception = ex;
        }

        if (exception != null) {
            if (exception instanceof GeneratorException) {
                agentException = (GeneratorException) exception;
            }
            else {
                agentException = new GeneratorException(
                    ErrorCode.AGENT_INIT_FAIL, exception.getMessage());
            }
        }
    }

    protected void createAgent(ReceiverInfo receiver)
        throws GeneratorException {
        if (agentActionList == null && agentActionList2 == null ) {
            return;
        }

        if (agentException != null) {
            throw agentException;
        }

        if(agentActionList != null) {
            int size = agentActionList.size();
            receiver.initAgent(size);
            for (int i = 0; i < size; i++) {
                try {
                    Action action = (Action) agentActionList.get(i);
                    action.create(receiver);
                }
                catch (Exception ex) {
                	LOGGER.error(ex);
                    if (ex instanceof GeneratorException) {
                        throw (GeneratorException) ex;
                    }
                    else {
                        //ex.printStackTrace();
                        throw new GeneratorException(ErrorCode.UNKNOWN_ERROR, ex
                                                     .getMessage());
                    }
                }
            }
        }

    }

    
    protected void createAgent2(ReceiverInfo receiver)
            throws GeneratorException {
            if (agentActionList2 == null) {
                return;
            }

            if (agentException != null) {
                throw agentException;
            }

            int size = agentActionList2.size();
            receiver.initAgent2(size);
            for (int i = 0; i < size; i++) {
                try {
                    Action action2 = (Action) agentActionList2.get(i);
                    action2.create2(receiver);
                }
                catch (Exception ex) {
                	LOGGER.error(ex);
                    if (ex instanceof GeneratorException) {
                        throw (GeneratorException) ex;
                    }
                    else {
                        //ex.printStackTrace();
                        throw new GeneratorException(ErrorCode.UNKNOWN_ERROR, ex
                                                     .getMessage());
                    }
                }
            }
        }
    
    public void release() {
        if (agentActionList == null) {
            return;
        }

        for (int i = 0; i < agentActionList.size(); i++) {
            Action action = (Action) agentActionList.get(i);
            action.release();
        }
        agentActionList = null;
    }
}
