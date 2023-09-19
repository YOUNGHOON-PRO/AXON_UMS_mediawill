package messager.generator.agent;

import java.util.*;

import messager.common.*;

public abstract class Action
{
    private final static String AGENT_PKG_NAME = "messager.generator.agent.";

    public static Action createAction(Agent agent)
        throws Exception {
        String actionName = agent.getAction();
        String className = AGENT_PKG_NAME + actionName;
        Class actionClass = Class.forName(className);
        Action action = (Action) actionClass.newInstance();

        action.setAgent(agent);
        action.init();
        return action;
    }

    protected Agent agent;

    protected Action() {
    }

    private void setAgent(Agent agent) {
        this.agent = agent;
    }

    protected abstract void init()
        throws AgentException;

    public abstract void create(ReceiverInfo receiverInfo)
        throws AgentException;

    public abstract void create2(ReceiverInfo receiverInfo)
            throws AgentException;

    
    public abstract void release();

    public int getID() {
        return agent.getID();
    }

    public String getName() {
        return agent.getAction();
    }

    public HashMap getParameters() {
        return agent.getParameters();
    }
}
