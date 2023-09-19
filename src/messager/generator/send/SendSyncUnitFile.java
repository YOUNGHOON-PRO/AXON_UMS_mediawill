package messager.generator.send;

import java.io.*;

import messager.generator.config.*;

/**
 * MailSender가 발송 작업중인 Unit를 알아내려고 할 때 이 클래스를 이용한다. MailSender가
 * UnitName[messageID^unitID]을 전송 받으면 지정된 디렉토리에 UnitName으로 파일을 생성해 놓으므로 그 파일의 존재
 * 유무로 unit이 발송 중인가 알 수 있다. MailSender별 다른 디렉토리를 사용한다. (MailSEnder의 ID로 지정된
 * 디렉토리) Work_Dir/send/MailSender_ID/messageID^unitID의 경로로 생성된다.
 */
class SendSyncUnitFile
{

    //작업 디렉토리에 생성될 MailSender의 발송 중인 Unit를 기록해 놓을 디렉토리명
    private static final String dirName = "send";

    //MailSender의 발송 중인 Unit를 기록해 놓을 최상위 디렉토리에 대한 File 객체
    private static File syncDirFile;

    //MailSender들의 ID 배열
    private static String[] senderList;

    //MailSender의 최 상위 작업 디렉토리 를 생성한다.

    public static void init() {
        //작업디렉토리
        String path = ConfigLoader.getProperty("work.path");
        syncDirFile = new File(path, dirName);
        if (!syncDirFile.exists()) {
            if (!syncDirFile.mkdirs()) {
                System.err.println("Directory Create Fail: " + syncDirFile);
                System.exit(1);
            }
        }
    }

    //MailSender의 ID
    private String senderID;

    //MailSender의 발송중인 Unit를 기록할 디렉토리의 File 객체
    private File senderSyncFile;

    //접속한 MailSender의 ID로 객체 생성
    public SendSyncUnitFile(String id) {

        senderID = id;

        //File 객체 생성(디렉토리로서 접속한 MailSender가 이미 디렉토리는 생성 하였으므로 디렉토리는 생성하지 않는다.)
        senderSyncFile = new File(syncDirFile, id);

    }

    /**
     * 현재 접속 중인 MailSender가 Unit를 발송 중인가 확인한다. Unit의 MailSender의 동기화 파일이 존재유무로
     * 확인한다.
     *
     * @param messageID
     *            MessageID
     * @param unitID
     *            unitID
     * @return unit에 대한 동기화 파일이 존재하면 true
     */
    public boolean exists(String messageID, int unitID) {

        boolean breturn = false;

        String fileName = messageID + "^" + Integer.toString(unitID);

        if (senderSyncFile.exists()) {

            File unitFile = new File(senderSyncFile, fileName);

            breturn = unitFile.exists();

        }

        return breturn;

    }

    /**
     * 현재 접속중인 MailSender가 Unit를 발송 중인가 확인한다. UnitName (MessageID^UnitID)의 전송 후
     * UnitName의 전송이 성공했는가 확인
     *
     * @param unitName
     *            MessageID^UnitID
     * @return unit에 대한 동기화 파일이 존재하면 true
     */
    public boolean existsUnit(String unitName) {

        boolean breturn = false;

        if (senderSyncFile.exists()) {

            File unitFile = new File(senderSyncFile, unitName);

            breturn = unitFile.exists();

        }

        return breturn;

    }

    /**
     * Unit이 MailSender에 의해 발송 중이거나 발송을 시도 했거나를 확인 한다. 생성 완료된 Unit의 발송 유무를 확인
     * 하기위한 작업으로 recovery를 위한 과정이다
     *
     * @param unitName
     *            MessageID^UnitID
     * @param 어떤
     *            MailSender의 동기화 디렉토리에 Unit에 대한 파일이 존재하면 true
     */
    public static boolean exists(String unitName) {

        if (senderList == null) {

            senderList = syncDirFile.list();

        }

        //MailSender들의 모든 작업 디렉토리에서 존재 유무를 확인한다.
        for (int i = 0; i < senderList.length; i++) {

            String senderID = senderList[i];

            File senderSyncFile = new File(syncDirFile, senderID);

            File unitSyncFile = new File(senderSyncFile, unitName);

            if (unitSyncFile.exists()) {

                return true;

            }

        }

        return false;

    }

}
