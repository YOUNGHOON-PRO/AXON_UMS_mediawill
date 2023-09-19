/*
 * Copyright 2004 Neocast. Inc All rights reserved. Neocast Engine terms.
 */

package messager.center.delivery;

/**
 * DeliveryAgent가 Generator로 전달된 Unit의 동기화를 위해서 전송중인 Unit의 링크(동기화 파일)를 생성하고 삭제를
 * 관리한다. 동기화 파일은 접속된 Generator의 IP Address로 생성된 디렉토리 아래에 Unit의
 * UnitName(MessageID^UnitID)으로 생성된다. 생성되는 시기는 UnitQueue에서 Unit를 가져왔을 때 생성되고
 * 삭제되는 시기는 Generator에서 Accept 메세지를 보냈을 때 삭제된다.
 *
 * @author Park MinChan
 * @version 2004/01/31
 */

import java.io.*;

import messager.center.config.*;

class DeliverySyncUnit
{
    private static final String dirName = "delivery";

    /**
     * DeliveryAgent가 Unit동기화를 위해 사용되는 디렉토리의 File 객체
     */
    private static File dirFile;

    static {
        String workPath = ConfigLoader.getString("work.path", "repository");
        dirFile = new File(workPath, dirName);
        if (!dirFile.exists()) {
            if (!dirFile.mkdirs()) {
                System.err.println("디렉토리를 생성 할수 없음 : " + dirFile);
                System.exit(1);
            }
        }
        else if (!dirFile.isDirectory()) {
            System.err.println("디렉토리가 아님 : " + dirFile);
            System.exit(1);
        }
    }

    /**
     * Agent에 대한 동기화 파일을 생성할 디렉토리의 File객체 디렉토리명은 접속한 Generator의 IP address 이다.
     */
    private File agentDirFile;

    /**
     * Generator의 IPAddress 로 디렉토리를 생성하여 Generator로 요청에 전달된 Unit의 동기화를 구현한다.
     */
    public DeliverySyncUnit(String ipAddress) {
        agentDirFile = new File(dirFile, ipAddress);
        if (!agentDirFile.exists()) {
            agentDirFile.mkdirs();
        }
    }

    /**
     * IP의 Generator에 전달 되었던 Unit중에 동기화 파일이 남아 있는 가장 최근 Unit를 얻는다 가장 최근에 생성된 파일만
     * 이전 생성된 파일들은 삭제된다.
     *
     * @return String UnitName (MessageID^UnitID), 만일 동기화 파일이 남아있지 않으면 null를
     *         리턴한다.
     */
    public String lastUnit() {
        String unitName = null;
        if (agentDirFile.exists()) {

            //디렉토리의 변경시간이 가장 최근 작업한 Unit의 시간이므로
            //디렉토리의 변경시간보다 Unit 파일의 변경시간이 이전인 파일은 삭제한다.
            long dirTime = agentDirFile.lastModified();
            File[] unitFileList = agentDirFile.listFiles();
            if (unitFileList.length > 0) {
                File unitFile = null;
                for (int i = 0; i < unitFileList.length; i++) {
                    unitFile = unitFileList[i];
                    if (dirTime > unitFile.lastModified()) {
                        unitFile.delete();
                    }
                    else {
                        unitName = unitFile.getName();
                    }
                }
            }
        }
        return unitName;
    }

    /**
     * Unit에 대한 delivery 동기화 파일을 생성한다.
     *
     * @param messageID
     *            Unit의 MessageID
     * @param unitID
     *            Unit의 UnitID
     */
    public void createUnit(String messageID, int unitID) {
        String unitName = messageID + "^" + Integer.toString(unitID);
        createUnit(unitName);
    }

    /**
     * Unit에 대한 Delivery동기화 파일을 생성한다.
     *
     * @param unitName
     *            Unit의 UnitName(MessageID^UnitID)
     */
    public void createUnit(String unitName) {
        if (!agentDirFile.exists()) {
            return;
        }
        File uFile = new File(agentDirFile, unitName);
        if (!uFile.exists()) {
            try {
                uFile.createNewFile();
            }
            catch (IOException ex) {
            }
        }
    }

    /**
     * /** Unit에 대한 정보로 delivery 동기화파일을 삭제한다.
      *
      * @param messageID
      *            Unit의 MessageID
      * @param unitID
      *            Unit의 UnitID
      * @return boolean Unit에 대한 동기화 파일이 존재하지 않거나 삭제를 성공하면 true
      */
     public boolean remove(String messageID, int unitID) {
         String unitName = messageID + "^" + Integer.toString(unitID);
         return remove(unitName);
     }

    /**
     * Unit의 UnitName[MessageID^UnitID]으로 delivery sync파일을 삭제한다.
     *
     * @param unitName
     *            Unit의 UnitName
     * @return boolean Unit에 대한 동기화 파일이 존재하지 않거나 삭제를 성공하면 true
     */
    public boolean remove(String unitName) {
        boolean success = true;
        if (agentDirFile.exists()) {
            File file = new File(agentDirFile, unitName);
            if (file.exists()) {
                success = file.delete();
            }
        }
        return success;
    }
}
