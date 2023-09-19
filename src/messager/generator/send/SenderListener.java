package messager.generator.send;

import java.io.*;
import java.net.*;
import java.util.*;

import messager.generator.config.*;

/**
 * MailSender 의 접속을 받아 들이고 데이타 전달를 하는 통신을 하는 클래스 메일 발송에 필요한 데이타는 통신시 전달이 이루어 지지
 * 않고 파일 공유로 이루어지므로 항상 localhost[127.0.0.1]로 접속이 이루어진다.
 */
public class SenderListener
    extends Thread
{
    private final static String LISTEN_HOST = "127.0.0.1";

    private static SenderListener instance;

    public static void execute() {
        SendSyncUnitFile.init();
        synchronized (SenderListener.class) {
            if (instance == null) {
                instance = new SenderListener();
            }
            if (!instance.isAlive()) {
                instance.start();
            }
        }
    }

    //listen socket의 port
    private int port;
    String ip;
    
    //listen socket의 backlog
    private int backlog;

    //접속된 MailSender을 저장(MailSender ID를 Key로 설정)
    private HashMap senderMap;

    private SenderListener() {
        port = ConfigLoader.getInt("sender.listen.port", -1);
        if (port == -1) {
            System.err.println("sender.listen.port: " + port);
            System.exit( -1);
        }
        
        ip = ConfigLoader.getString("generator.host", null);
        if (ip == null) {
            System.err.println("generator.host: " + ip);
            System.exit( -1);
        }
        
        backlog = ConfigLoader.getInt("sender.listen.backlog", 4);
        if (backlog <= 0) {
            backlog = 1;
            //접속된 MailSender를 MailSender의 ID로 저장한다.
            //같은 MailSender의 ID로 중복해서 실행을 방지한다.
        }
        senderMap = new HashMap();
    }

    /**
     *
     */
    public void run() {
        InetAddress localAddress = null;

        //접속을 받아들이는 ServerSocket
        ServerSocket server = null;

        Exception exception = null;

        try {
            //MailSender와의 통신은 local 통신으로만 이루어진다.
            localAddress = InetAddress.getByName(ip);

            //local로만 접속을 받아 들이는 ServerSocket 생성
            server = new ServerSocket(port, backlog, localAddress);
            

            while (true) {
                Socket socket = server.accept();
                UnitSender sender = new UnitSender(socket, senderMap);
                sender.start();
            }
        }
        catch (Exception ex) {
            System.err.println("SenderListener: " + ex.getMessage());
            exception = ex;
        }
        finally {
            if (exception != null) {
                if (server != null) {
                    try {
                        server.close();
                    }
                    catch (Exception ex) {}
                    server = null;
                }
            }
        }
    }

    /**
     * MailSender와 통신 하는 쓰레드 -> MailSender로 부터 MailSender ID를 전송받음 -> MailSender ID로
     * 현재 접속중인가 확인 (접속중이면 소켓을 닫고 작업을 종료한다.) -> MailSender로 부터 Unit 요청를
     * 받는다.([REQUEST\r\n]) -> MailSender로 Unit에 대한 UnitName[MessageID^UnitName]을
     * 보낸다. (SendQueue에 생성완료된 Unit이 존재 할 경우) -> 생성완료된 Unit이 존재하지 않을 경우 NOP\r\n를 보낸다.
     */

    class UnitSender
        extends Thread
    {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private HashMap senderMap;
        private String senderID;
        private String unitName;

        public UnitSender(Socket socket, HashMap hashMap) {
            this.socket = socket;
            senderMap = hashMap;
        }

        /**
         * 접속된 Socket에 대한 stream를 open
         */
        private void open()
            throws IOException {
            in = new BufferedReader(new InputStreamReader(
                socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream());
        }

        /**
         * 접속된 Socket close
         */
        private void socketClose() {
            if (in != null) {
                try {
                    in.close();
                }
                catch (IOException ex) {}
                in = null;
            }

            if (out != null) {
                out.close();
                out = null;
            }

            if (socket != null) {
                try {
                    socket.close();
                }
                catch (IOException ex) {}
                socket = null;
            }
        }

        /**
         * run
         */
        public void run() {
        	//MailSender에 대한 Unit의 동기화 파일을 관리
            SendSyncUnitFile syncUnitFile = null;

            try {
                open();
                
                //접속후 한 라인을 읽는다.(MailSender의 ID)
                String line = in.readLine();
                
                if (line != null) {
                    senderID = line;
                
                    //접속 여부 확인과 Thread의 활동유무
                    Thread thread = (Thread) senderMap.get(line);
                    if (thread == null || !thread.isAlive()) {  //sender가 보내준 스레드가 없으면 실행 
                        senderMap.put(senderID, this);
                        
                        //MailSender ID로 SendSyncUnitFile 객체(MailSender에 대한 Unit의 동기화 관리) 생성
                        syncUnitFile = new SendSyncUnitFile(senderID);
                        runMain();
                        
                    }
                    else { // 이미 접속 되어 있으면 종료
                        System.err.println("Sender : " + senderID + "이미 접속 됨");
                    }
                }
            }
            catch (Exception ex) {
                System.err.println("UnitSender: [" + senderID + "] " + ex.getMessage());
            }
            finally {
                socketClose();
            }

            // 	접속이 종료된 후 가장 나중 전송된 Unit이
            // MailSender에 등록되었나 확인(MailSender의 동기화 파일 존재 유무로 확인)
            if (unitName != null) {
                if (!syncUnitFile.existsUnit(unitName)) {
                	// 존재 하지 않을 경우 SendQueue에 push
                    SendQueue.push(unitName, true);
                }
            }

            //senderMap에서 접속이 종료된 MailSender를 제거한다.
            if (senderID != null) {
                senderMap.remove(senderID);
            }
        }

        /**
         * MailSender의 Unit의 요청과 전송을 처리한다.
         */
        private void runMain()
            throws IOException {
            String line = null;
            while (true) {
            	//라인을 읽는다.
                line = in.readLine();
                if (line == null) {
                    //null이 리턴되면 접속 종료로 처리되어 return한다.
                    return;
                }
                else if (line.equals("REQUEST")) {
                    //생성완료된 Unit요청
                    unitName = SendQueue.pop();
                    //SendQueue에서 Unit를 꺼내온다.
                    if (unitName != null) {
                    	//Unit 전송
                        out.print(unitName + "\r\n"); //unitname 엔터 보내고 
                        out.flush();
                        
                        String acceptData = in.readLine();  //응답 받기
                        
                        if (acceptData == null) {
                            return;
                        }
                        else if (acceptData.equals("OK+")) {
                            unitName = null;
                        }
                        else {
                            SendQueue.push(unitName, true);
                            unitName = null;
                        }
                    }
                    else {
                        //SendQueue에 Unit이 존재하지 않을 경우 NOP를 보낸다.
                        out.print("NOP\r\n");
                        out.flush();
                    }
                }
                else {
                    //REQUEST를 읽지 않았을 경우 NOP를 보낸다.
                    out.print("NOP\r\n");
                    out.flush();
                }
            }
        }
    }
}