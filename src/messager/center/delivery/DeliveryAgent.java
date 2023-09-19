package messager.center.delivery;

import java.io.*;
import java.net.*;

import messager.center.repository.*;
import messager.common.*;
import messager.common.util.*;

/**
 * Generator의 요청에 의해 UnitInfo객체와 MessageInfo객체를 전달한다. UnitInfo객체 요청시
 * DeliveryQueue에서 UnitInfo객체를 꺼내어서 Generator에 전달한다. MessageInfo 객체를 요청시
 * MessageInfoRepository 에서 가져와서 전달한다. Generator 당 하나의 컨넥션으로 작업을 처리한다.
 */

class DeliveryAgent
    extends Thread
{
    private static final int UNIT_TYPE = 1;
    private static final int MESSAGE_TYPE = 2;
    private static final int ACCEPT_TYPE = 3;
    private DeliveryConnection connection;
    private DeliverySyncUnit deliverySync;
    private MessageMap messageMap;

    /**
     * Generator의 UnitInfo객체와 MessageInfo객체의 요청을 처리 하기 위한 DeliveryAgent 객체를
     * 생성한다.
     */
    public DeliveryAgent(Socket socket) {
        connection = new DeliveryConnection(socket); //Connection
        messageMap = MessageMap.getInstance();
    }

    /**
     * loop를 돌면서 Generator의 요청에 대한 UnitInfo객체, MessageInfo객체를 전달한다.
     */
    public void run() {
        String address = connection.getAddress();
        deliverySync = new DeliverySyncUnit(address);

        try {
            while (true) {
                Data data = new Data(connection);
                response(data);
            }
        }
        catch (Exception ex) {
        }
        finally {
            //Thread가 종료될 때는 Connectin를 닫는다.
            if (connection != null) {
                connection.close();
                connection = null;
            }
        }
    }

    private void response(Data data)
        throws IOException {
        switch (data.getType()) {

            case UNIT_TYPE:
                unitResponse(data);
                break;
            case MESSAGE_TYPE:
                messageResponse(data);
                break;
            case ACCEPT_TYPE:
                unitAccept(data);
                break;
            default:
                sendNull();
                break;
        }
    }

    private void unitAccept(Data data)
        throws IOException {

        byte[] body = data.body;
        if (body != null) {
            String unitName = new String(body);
            deliverySync.remove(unitName);
            Data sendData = new Data(ACCEPT_TYPE, 1, body);
            sendData.toConnection(connection);
        }
    }

    private void unitResponse(Data data)
        throws IOException {
        UnitInfo unit = null;
        String unitName = null;

        if (data.body == null) {
            unit = fetchUnit();
            if (unit != null) {
                unitName = unit.getName();
            }
        }
        else {
            unitName = new String(data.body);
            unit = fetchUnit(unitName);
        }

        if (unit != null) {
            sendObject(unit, 1, 1);
        }
        else {
            sendNull();
        }

    }

    private UnitInfo fetchUnit(String unitName)
        throws IOException {
        UnitInfo unit = null;
        int inx = unitName.indexOf('^');
        if (inx != -1) {
            String messageID = unitName.substring(0, inx);
            String unitID = unitName.substring(++inx);
            MessageHandler msgHandler = messageMap.lookup(messageID);
            if (msgHandler == null) {
                try {
                    msgHandler = new MessageHandler(messageID);
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            unit = msgHandler.readUnit(unitID);
        }

        return unit;
    }

    /**
     *
     */
    private UnitInfo fetchUnit()
        throws IOException {
        UnitInfo unit = UnitQueue.pop();
        if (unit != null) {
            String unitName = unit.getName();
            String messageID = unit.getMessageID();
            int unitID = unit.getUnitID();
            int size = unit.size();
            MessageHandler msgHandler = messageMap.lookup(messageID);
            if (msgHandler != null) {
                deliverySync.createUnit(unitName);
                msgHandler.deliveryUnit(Integer.toString(unitID), size);
            }
            else {
                System.err.println("lookup fail : " + messageID);
                unit = null;
            }
        }
        return unit;
    }

    /**
     * 요청을 받을 Message의 MessageInfo 객체를 보낸다.
     *
     * @@param data
     *            요청된 Message의 MessageID byte배열
     * @@exception IOException
     *                입출력 에러가 발생하였을 경우
     */
    private void messageResponse(Data data)
        throws IOException {
        MessageInfo messageInfo = null;
        String messageID = null;
        if (data.body != null) {
            messageID = new String(data.body);
            if (messageID != null) {
                MessageHandler msgHandler = messageMap.lookup(messageID);
                if (msgHandler != null) {
                    try {
                        messageInfo = msgHandler.readMessageInfo();
                    }
                    catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
        //MessageInfo 객체를 보낼 경우 type은 3이다.
        sendObject(messageInfo, 3, 1);
    }

    /**
     * 요청에 대한 응답을 보낸다. 응답에 대한 데이타가 없을 경우 8byte의 byte배열(값이 모두 0)인 헤더만 보낸다.
     */
    private void sendObject(Object obj, int type, int code)
        throws IOException {
        byte[] body = null;
        if (obj != null) {
            body = object2bytes(obj);
            Data data = new Data(type, code, body);
            data.toConnection(connection);
        }
        else {
            sendNull();
        }
    }

    /**
     * 객체(Object)를 byte배열로 변환한다.
     *
     * @@param obj
     *            변환될 객체
     * @@return byte[] 변환된 byte배열
     * @@exception IOExcepiton
     */
    private byte[] object2bytes(Object obj)
        throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(stream);
        oos.writeObject(obj);
        oos.flush();
        byte[] bytes = stream.toByteArray();
        return bytes;
    }

    private void sendNull()
        throws IOException {
        Data data = new Data(0, 0, null);
        data.toConnection(connection);
    }

    /**
     * Generator로부터 요청을 객체로 구현한다.
     */
    class Data
    {
        public byte[] header;
        public byte[] body; //요청 데이타 또는 응답 Data

        public Data(DeliveryConnection connection)
            throws IOException {
            fromConnection(connection);
        }

        public Data(int type, int code, byte[] bytes) {
            header = new byte[8];
            setType(type);
            setCode(code);
            setBody(bytes);
        }

        public int getType() {
            return (int) header[0];
        }

        public int getCode() {
            return (int) header[2];
        }

        public int length() {
            return BytesUtil.bytes2int(header, 4);
        }

        public void setBody(byte[] bytes) {
            if (bytes != null && bytes.length > 0) {
                BytesUtil.int2bytes(bytes.length, header, 4);
                body = bytes;
            }
        }

        public void setType(int type) {
            header[0] = (byte) type;
        }

        public void setCode(int code) {
            header[2] = (byte) code;
        }

        private void fromConnection(DeliveryConnection connection)
            throws IOException {
            header = new byte[8];
            connection.readbytes(header, 0, 8);
            int readsize = length();
            if (readsize > 0) {
                body = new byte[readsize];
                connection.readbytes(body, 0, readsize);
            }
        }

        public void toConnection(DeliveryConnection connection)
            throws IOException {
            connection.sendbytes(header, 0, 8);
            if (body != null && body.length > 0) {
                connection.sendbytes(body, 0, body.length);
            }
            connection.flush();
        }
    }
}
