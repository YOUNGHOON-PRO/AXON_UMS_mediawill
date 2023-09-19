package messager.center;

import messager.center.config.ConfigLoader;
import messager.center.repository.UnitAgentManager;
import messager.center.delivery.DeliveryListener;
import messager.center.creator.MessageListener;
import messager.center.result.ResultListener;
import messager.center.result.ResultHandler;
import messager.center.control.ControlListener;
import messager.center.repository.MessageCleaner;
import messager.center.repository.ProgressInsert;

/**
 * main
 */
public class Main
{
    public static void main(String[] args) {
        try {
        	// properties파일 로드
            ConfigLoader.load();
            new ControlListener().start(); //상태조회 out put  .. 사용사지 않는 것 같음
            
            new ResultHandler().start();  //2분마다 center/result 폴더를 확인해 있으면 진행완료로 DB 업데이트 및  파일 삭제 (unit, message, sync_unit, result )
            
            new ResultListener().start(); //Generatar에서 발송결과를 주면 center/result 폴더에 파일을 적재  //서버 13105
            
            UnitAgentManager.execute();// unitagentmanager 생성, generator에 보내질 unitinfo가 큐에 보관

           
            DeliveryListener.execute(); //UnitInfo객체와 MessageInfo객체 전달   //서버 13104
            
            //Mail Queue
            MessageListener.execute();  // DB에서 정보를 읽고 unit 과 message 폴더에 파일을 생성한다.

            MessageCleaner.executeThread();

            
            new ProgressInsert().start();//발송 진행 사항 UPDATE 수행
        }
        catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    public static void shutdown() {
        System.out.println("Center shutdown.");
        System.exit(0);
    }
}
