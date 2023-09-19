package messager.mailsender.send;

import java.util.*;

/*
 * element format of storeAgentVector
 * msgID_unitID_AgentPriority_domain
 * ) 1_1_0_www.yahoo.co.kr
 *   1_1_1_www.yahoo.co.kr
 *   1_1_2_www.yahoo.co.kr
 */
public class AgentControler
{
    private static Vector storeAgentVector = new Vector();

    public void addAgentGroup(String domain) {
        storeAgentVector.add(domain);
    }

    public boolean removeAgentGroup(String domain) {
        storeAgentVector.trimToSize();
        return (storeAgentVector.remove(domain));
    }

    public int getSize() {
        return storeAgentVector.size();
    }

    public Vector getList() {
        storeAgentVector.trimToSize();
        return storeAgentVector;
    }
}