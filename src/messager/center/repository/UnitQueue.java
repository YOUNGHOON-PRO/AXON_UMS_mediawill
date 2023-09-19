package messager.center.repository;

import java.util.*;

import messager.center.config.*;
import messager.common.*;

/**
 * Unit 저장하는 Queue
 */
public class UnitQueue
{
    private static HashMap unitMap;

    /**
     * Unit이 저장된 배열에서
     */
    private static ArrayList sendList;
    private static Object lock;
    private static int maxSize;

    static {
        maxSize = ConfigLoader.getInt("unit.queue.size", 20);
        unitMap = new HashMap(maxSize * 2);
        sendList = new ArrayList(maxSize);
        lock = new Object();
    }

    public static boolean isFulled() {
        boolean isFull = true;
        synchronized (lock) {
            if (unitMap.size() < maxSize) {
                isFull = false;
            }
        }
        return isFull;
    }

    /**
     * 저장된 Unit이 존재하는지 확인한다.
     */
    public static boolean isEmpty() {
        boolean isEmpty = true;
        synchronized (lock) {
            if (unitMap.size() > 0) {
                isEmpty = false;
            }
        }
        return isEmpty;
    }

    public static void push(UnitInfo unit) {
        String unitName = unit.getName();
        boolean isSuccess = false;
        synchronized (lock) {
            do {
                if (unitMap.size() < maxSize) {
                    unitMap.put(unitName, unit);
                    sendList.add(unitName);
                    isSuccess = true;
                }
                else {
                    try {
                        lock.wait();
                    }
                    catch (InterruptedException ex) {}
                }
            }
            while (!isSuccess);
        }
    }

    public static UnitInfo pop() {
        UnitInfo unit = null;
        synchronized (lock) {
            if (sendList.size() > 0) {
                String unitName = (String) sendList.remove(0);
                if (unitName != null) {
                    unit = (UnitInfo) unitMap.remove(unitName);
                    if (unit != null) {
                        lock.notifyAll();
                    }
                }
            }
        }
        return unit;
    }

    public static void remove(String unitName) {
        synchronized (lock) {
            UnitInfo unit = (UnitInfo) unitMap.remove(unitName);
            if (unit != null) {
                lock.notifyAll();
                unit = null;
            }
        }
    }
}
