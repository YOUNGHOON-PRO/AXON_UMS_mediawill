package messager.generator.agent;

import messager.generator.content.*;

/**
 * MessageInfo에 필요한 데이타가 존재하지 않을 경우 Throw된다.
 */
public class AgentException
    extends GeneratorException
{
    public AgentException(ErrorCode code, String errMsg) {
        super(code, errMsg);
    }
}
