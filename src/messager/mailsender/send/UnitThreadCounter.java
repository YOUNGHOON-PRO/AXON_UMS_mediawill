package messager.mailsender.send;

import java.util.*;

public class UnitThreadCounter
{
    private Vector utc = new Vector();

    public void addUnitFactor(String agentNM) {
        utc.add(agentNM);
    }

    public void delUnitFactor(String agentNM) {
        utc.remove(agentNM);
    }

    public int getSize() {
        return utc.size();
    }
}
