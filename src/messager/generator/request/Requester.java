package messager.generator.request;

import java.io.*;
import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import messager.common.*;
import messager.common.util.*;
import messager.generator.*;
import messager.generator.config.*;
import messager.generator.repository.*;

/**
 * MessageCenter와 Connection를 맺어서 UnitInfo객체와 MessageInfo객체를 전달 받는다.
 */
public class Requester
{
	private static final Logger LOGGER = LogManager.getLogger(Requester.class.getName());
	
    private static boolean b_run = false;

    public static void execute() {
        synchronized (Requester.class) {
            if (b_run) {
                return;
            }

            //Request 대기 시간
            long requestPeriod = -1;
            String str = ConfigLoader.getProperty("unit.request.period");
            try {
                requestPeriod = Integer.parseInt(str) * 1000;
            }
            catch (Exception ex) {
            	LOGGER.error(ex);
                requestPeriod = 2000;
            }

            RequestRunner runner = new RequestRunner(requestPeriod);
            runner.start();
            b_run = true;
        }
    }

    /**
     * Message Center에 접속하여 UnitInfo 객체와 MessageInfo 객체를 요청한다. 헤더 8byte 0 : type
     * [0: nop, 1: unit, 2: messageInfo, 3: accept] 2 : code [성공 : 1, 실패 0] 4 :
     * 데이타 길이 데이타 요청 type이 1일때 (UnitInfo 재요청시) : mesageID길이 (2byte) | messageID |
     * unitID(4byte) type이 2일때 (MessageInfo 요청시) : messageID type이 3일때 :
     * messageID 길이(2byte) | messageID | unitID(4byte) 응답 type이 1일때 : byte배열로
     * 변환된 UnitInfo 객체 type이 2일때 : byte배열로 변환된 MessageInfo객체 type이 3일때 :
     * messageID(2byte) | messageID | unitID(4byte)
     */
    static class RequestRunner
        extends Thread
    {
        private static final int NOP_TYPE = 0;

        private static final int REQUEST_UNITINFO_TYPE = 1;

        private static final int REQUEST_MESSAGEINFO_TYPE = 2;

        private static final int UNITINFO_ACCEPT_TYPE = 3;

        private GeneratorManager generatorMgr = null;

        private MessageInfoCache messageInfoCache = null;

        private RequestSyncUnit syncUnit = null;

        private long requestPeriod;

        /**
         */
        public RequestRunner(long period) {
            generatorMgr = GeneratorManager.getInstance();
            messageInfoCache = MessageInfoCache.getInstance();
            syncUnit = RequestSyncUnit.instance();
            requestPeriod = period;
        }

        /**
         * MessageCenter에 재 접속시 중단된 Unit의 컨텐츠 생성을 재실행 한다.
         * (체크하는 directory : repository/transfer/unit)
         * UnitInfo객체파일들을 로딩해서 현재 생성 중에 있는 Unit과 생성이 완료된 Unit를 제외한 Unit들을 다시 생성한다.
         */
        private void recovery(CenterConnection connection)
            throws IOException {
            //UnitInfo 객체가 저장된 디렉토리에서 UnitInfo 객체 파일들을 로딩한다
            String[] fileList = UnitInfoFile.unitList();
            long accessTime = System.currentTimeMillis();
            long curTime = accessTime;
            long time = 0;

            for (int i = 0; i < fileList.length; i++) {
                try {
                    curTime = System.currentTimeMillis();
                    time = curTime - accessTime;

                    //Socket timeout 지정되어 있을 경우 접속을 유지하기 위해서
                    //작업이 없는 데이타를 보낸다.
                    if (time > requestPeriod) {
                        sendNop(connection);
                        readResponse(connection);
                        accessTime = curTime;
                    }

                    String unitName = fileList[i];
                    //UnitInfo이 컨텐츠 생성중에 있는 가 확인
                    if (generatorMgr.isRunningUnit(unitName)) {
                        // 동기화 참조 파일 존재확인, 존재 하면 삭제
                        File syncFile = syncUnit.unitFile(unitName);
                        if (syncFile.exists()) {
                            syncUnit.delete(unitName);
                        }
                    }
                    else {
                        // 생성 완료 확인
                        if (SendUnitFile.exists(unitName)) {
                            //생성이 완료 되었으면 UnitInfo 객체 파일 삭제
                            UnitInfoFile.deleteUnit(unitName);
                            continue;
                        }

                        //UnitInfo 객체 로딩
                        UnitInfo unit = UnitInfoFile.readUnit(unitName);

                        if (unit == null) { //생성완료
                            syncUnit.delete(unitName);
                            continue;
                        }

                        String messageID = unit.getMessageID();
                        int unitID = unit.getUnitID();

                        // 이미 로딩된 MessageInfo 객체 검색
                        MessageInfo messageInfo = messageInfoCache
                            .lookup(messageID);
                        if (messageInfo == null) {
                            // Message Center에 MessageInfo객체를 요청한다.
                            messageInfo = requestMessageInfo(connection,
                                messageID);
                            accessTime = System.currentTimeMillis();
                            if (messageInfo != null) {
                                //MessageInfo객체를 MessageInfoCache 에 넣는다.
                                messageInfoCache.put(messageInfo);
                            }
                        }

                        if (messageInfo != null) {
                            //Generator를 실행 할 수 있는가 체크
                            while (generatorMgr.isFulled()) {
                                curTime = System.currentTimeMillis();
                                time = curTime - accessTime;
                                if (time > requestPeriod) {

                                    // Socket time out 지정시 소켓의 연결을 유지
                                    sendNop(connection);
                                    readResponse(connection);
                                    time = curTime;
                                }

                                try {
                                    sleep(requestPeriod);
                                }
                                catch (InterruptedException ex) {
                                	LOGGER.error(ex);
                                }
                            }

                            // UnitInfo와 MessageInfo객체로 컨텐츠 Generator 를 실행한다.
                            generatorMgr.runUnit(unit, messageInfo);
                        }
                        else {
                            System.err.println("MessageInfo Not Found : "
                                               + messageID);
                        }
                    }
                }
                catch (Exception ex) {
                	LOGGER.error(ex);
                    if (ex instanceof IOException) {
                        throw (IOException) ex;
                    }
                    //ex.printStackTrace();
                }
            }
        }

        public void run() {
            CenterConnection connection = null;

            while (true) {
                try {
                    if (connection == null) {
                        connection = new CenterConnection();
                    }
                    recovery(connection);
                    requestMain(connection);
                }
                catch (IOException ex) {
                	LOGGER.error(ex);
                    //ex.printStackTrace();
                }
                catch (Exception ex) {
                	LOGGER.error(ex);
                    //ex.printStackTrace();
                }
                finally {
                    if (connection != null) {
                        connection.close();
                        connection = null;
                    }
                }
            }
        }

        /**
         * Unit를 요청한다. 
         * 1. type = 1로 MessageCenter에 요청한다. 
         * 2. 응답 코드가 1일경우 Unit에 대한 동기화 참조파일을 생성한다(reqMessageID와 reqUnitID가 존재하지 않을 경우) 
         * 3. Unit에 대한 확인 메세지를 주고 받는다. 
         * 4. Unit를 저장한다. 
         * 5. Unit에 대한 동기화 참조파일을 삭제한다.
         */
        private UnitInfo requestUnit(CenterConnection connection, String reqUnitName)   // reqUnitName 파람은 쓰는곳을 아직 못찾음..  
            throws IOException {
            UnitInfo unit = null;
            
            
            //1. type = 1로 MessageCenter에 요청한다. 
            sendUnitRequest(connection, reqUnitName); 		//center에 unit 정보를  요청
            Response response = readResponse(connection);  // 요청 정보를 Response 객체로 받음
            
            //2. 응답 코드가 1일경우 Unit에 대한 동기화 참조파일을 생성한다(reqMessageID와 reqUnitID가 존재하지 않을 경우)
            if (response.code == 1) {
                unit = (UnitInfo) response.object;   //unit을 오브젝트 스트림을 받았어
                if (unit != null) {
                    String unitName = unit.getName();
                    syncUnit.create(unitName);    // 받은걸 request폴더에 파일로 생성
                   
                    //3. Unit에 대한 확인 메세지를 주고 받는다.
                    sendUnitAccept(connection, unitName, 1);
                    UnitAccept unitAccept = readUnitAccept(connection);
                    
                     
                    if (unitAccept.code == 1) {
                    	//4. Unit를 저장한다.
                        UnitInfoFile.writeUnit(unit);

                        //5. Unit에 대한 동기화 참조파일을 삭제한다.
                        syncUnit.delete(unitName);
                    }
                    else {
                        unit = null;
                    }
                }
            }
            return unit;
        }

        /**
         * MessageCenter에 데이터(MessageInfo 객체와 UnitInfo 객체) 요청하고 Generator를 실행한다.
         * (체크하는 directory : repository/transfer/request)
         */
        private void requestMain(CenterConnection connection)
            throws IOException {

            // Unit에 대한 동기화 참조 파일 리스트 로딩
            // 정상적으로 작업이 진행 되었을 경우 파일은 1개또는 존재 하지 않는다.
            ArrayList syncList = syncUnit.load();
            while (true) {
                // 여유 Generator가 있는 지 확인
                if (generatorMgr.isFulled()) {
                    //여유 Generator가 없으면 NOP(No operation)를 보내고 받는다.
                    sendNop(connection);  //ping 
                    readResponse(connection);

                    // Socket Time보다 적은 시간동안 sleep
                    try {
                        sleep(requestPeriod);
                    }
                    catch (InterruptedException ex) {
                    	LOGGER.error(ex);
                    }
                    continue;
                }

                String unitName = null;
                if (syncList.size() > 0) {  //request폴더의 파일명들 
                    unitName = (String) syncList.remove(0);
                }

                // 1.center에 unti 정보 요청
                // 2.request 폴더에 동기화 파일 생성
                // 3.unit폴더에 unit파일 생성후 request 폴더에 동기화 파일 삭제
                UnitInfo unit = requestUnit(connection, unitName);

                if (unit != null) {
                    String messageID = unit.getMessageID();
                    int unitID = unit.getUnitID();

                    MessageInfo messageInfo = messageInfoCache
                        .lookup(messageID);
                    if (messageInfo == null) {
                        int retry = 0;

                        do {
                            messageInfo = requestMessageInfo(connection, messageID);
                            messageInfoCache.put(messageInfo);
                            retry++;
                            try {
                                sleep(1000);
                            }
                            catch (Exception ex) {
                            	LOGGER.error(ex);
                            }
                        }
                        while (messageInfo != null && retry < 5);
                    }
                    //메세지에 대한 컨텐츠를 생성시킨다.
                    if (messageInfo != null) {
                        generatorMgr.runUnit(unit, messageInfo);
                    }
                }
                else {
                    try {
                        sleep(requestPeriod);
                    }
                    catch (InterruptedException ex) {
                    	LOGGER.error(ex);
                    }
                }
            }
        }

        /**
         * MessageInfo 객체를 요청을 보낸다.
         */
        private MessageInfo requestMessageInfo(CenterConnection connection,
                                               String messageID)
            throws IOException {
            byte[] header = new byte[8];
            byte[] data = messageID.getBytes();
            header[0] = (byte) REQUEST_MESSAGEINFO_TYPE;
            BytesUtil.int2bytes(data.length, header, 4);
            connection.sendbytes(header, 0, 8);
            connection.sendbytes(data, 0, data.length);
            connection.flush();
            Response response = readResponse(connection);
            if (response.code == 1) {
                return (MessageInfo) response.object;
            }
            return null;
        }

        /**
         * UnitInfo 겍체를 요청하기 위한 패킷을 보낸다.
         */
        private void sendUnitRequest(CenterConnection connection,
                                     String unitName)
            throws IOException {
            byte[] header = new byte[8];
            byte[] bytes = null;
            header[0] = (byte) REQUEST_UNITINFO_TYPE;
            if (unitName != null) {
                bytes = unitName.getBytes();
                int len = bytes.length;

                BytesUtil.int2bytes(len, header, 4);
            }
            connection.sendbytes(header, 0, 8);

            if (bytes != null) {
                connection.sendbytes(bytes, 0, bytes.length);
            }
            connection.flush();
        }

        /**
         * 요청한 UnitInfo나 MessageInfo 객체를 읽는다.
         */
        private Response readResponse(CenterConnection connection)
            throws IOException {
            byte[] header = new byte[8];
            Object object = null;
            int rc = connection.readbytes(header, 0, 8);

            if (rc != 8) {
                throw new IOException("stream read count unmatched");
            }
            int type = (int) header[0];
            int code = (int) header[2];
            int len = BytesUtil.bytes2int(header, 4);

            if (len > 0) {
                byte[] data = new byte[len];
                rc = connection.readbytes(data, 0, len);
                if (rc != len) {
                    throw new IOException("stream read count unmathced");
                }
                object = bytes2Object(data);
            }
            return new Response(type, code, object);
        }

        /**
         * UnitInfo객체의 확인 패킷을 보낸다.
         */
        private void sendUnitAccept(CenterConnection connection,
                                    String unitName, int code)
            throws IOException {
            byte[] header = new byte[8];
            byte[] bytes = unitName.getBytes();
            int len = bytes.length;
            header[0] = (byte) UNITINFO_ACCEPT_TYPE;
            header[2] = (byte) code;
            BytesUtil.int2bytes(len, header, 4);
            connection.sendbytes(header, 0, 8);
            connection.sendbytes(bytes, 0, len);
            connection.flush();
        }

        /**
         * UnitInfo겍체의 확인 패킷을 받는다.
         */
        private UnitAccept readUnitAccept(CenterConnection connection)
            throws IOException {
            byte[] header = new byte[8];
            int rc = connection.readbytes(header, 0, 8);

            if (rc != 8) {
                throw new IOException("read count unmatched");
            }
            UnitAccept unitAccept = null;
            int type = (int) header[0];
            int code = (int) header[2];
            int len = BytesUtil.bytes2int(header, 4);

            if (len > 0) {
                byte[] data = new byte[len];
                connection.readbytes(data, 0, len);
                String unitName = new String(data);
                unitAccept = new UnitAccept(unitName, code);
            }
            return unitAccept;
        }

        /**
         * No Operation 패킷을 보낸다.
         */
        private void sendNop(CenterConnection connection)
            throws IOException {
            byte[] header = new byte[8];
            connection.sendbytes(header, 0, 8);
            connection.flush();
        }

        /**
         * byte배열을 Object로 변환 한다.
         */
        private Object bytes2Object(byte[] bytes) {
            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            ObjectInput inobj = null;
            Object object = null;
            try {
                inobj = new ObjectInputStream(in);
                object = inobj.readObject();
            }
            catch (IOException ex) {
            	LOGGER.error(ex);
                //ex.printStackTrace();
            }
            catch (Exception ex) {
            	LOGGER.error(ex);
                //ex.printStackTrace();
            }
            finally {
                if (inobj != null) {
                    try {
                        inobj.close();
                    }
                    catch (IOException ex) {
                    	LOGGER.error(ex);
                    }
                }
                try {
                    in.close();
                }
                catch (IOException ex) {
                	LOGGER.error(ex);
                }
            }
            return object;
        }

        /**
         * Messager Center로 부터 응답를 저장하기 위한 객체
         */
        public class Response
        {
            public int type;

            public int code;

            public Object object;

            public Response(int aType, int aCode, Object obj) {
                type = aType;
                code = aCode;
                object = obj;
            }
        }

        /**
         * UnitInfo객체의 전달을 확인 하기 위한 데이타를 저장한 객체
         */
        class UnitAccept
        {
            public int type;

            public int code;

            public String unitName;

            public UnitAccept(String unitName, int aCode) {
                type = UNITINFO_ACCEPT_TYPE;
                code = aCode;
                this.unitName = unitName;
            }
        }
    }
}
