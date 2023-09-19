package messager.center.repository;

import java.io.*;

import messager.center.config.*;
import messager.common.*;

public class MessageStatus
{
    /** Message를 가져오는 중인 상태 */
    public final static int CREATE_RUN = 0;

    /** 발송 중인 상태 */
    public final static int SEND_RUN = 1;

    /**
     * 발송 대기 중인 상태 (Retry를 위해 대기중인 Message상태) 대기 상태로 변경은 사용자가 설정 할 수 없고 SEND_END
     * 에서 SEND_WAIT로 시스템에 의해 변경된다 상태 변경은 SEND_PAUSE(일시정지)와 SEND_STOP(발송 중지)로만 할수
     * 있다.
     */
    public final static int SEND_WAIT = 2;

    /** 발송 일시 정지된 상태 */
    public final static int SEND_PAUSE = 3;

    /** 발송 중지된 상태 (더이상 상태 변경이 되지 않는다.) */
    public final static int SEND_STOP = 4;

    /** Unit의 ID가 last UnitID로 도달 되지 않은 상태 */
    public final static int UNIT_LOAD_RUN = 1;

    /** 현재 UnitID가 last UnitID인 상태 */
    public final static int UNIT_LOAD_END = 2;

    public final static int UNIT_SEND_END = 3;

    /**
     * 발송 상태가 저장될 파일의 위치 값은 short(2byte)로 저장된다 발송 대기중(SEND_WAIT), 발송 중
     * (SEND_RUN), 발송 일시 정지(SEND_PAUSE) Unit전달 완료 (SEND_END), 발송 중지 (SEND_STOP)
     */
    private final static int SEND_STATUS_POS = 0;

    /** Unit의 진행 상태 (UNIT_LOAD_RUN, UNIT_LOAD_END)가 저장되는 위치 */
    private final static int UNIT_STATUS_POS = 1;

    /** Unit의 총수가 저장될 위치 */
    private final static int TOTAL_UNIT_CNT_POS = 4;

    /** 마지막 UnitID가 저장될 위치 */
    private final static int LAST_UNIT_ID_POS = 8;

    /** 마지막 load된 Unit의 UnitID가 저장될 위치 */
    private final static int LOAD_UNIT_ID_POS = 12;

    /** 대상자의 총수 가 저장될 위치 */
    private final static int TOTAL_CNT_POS = 16;

    /** Generator로 전달이 완료된 Unit의 수를 저장될 위치 */
    private final static int DELIVERY_CNT_POS = 20;

    /** 발송완료된 대상자수가 저장될 위치 */
    private final static int SEND_CNT_POS = 24;

    /** 발송 완료된 Unit수가 저장될 위치 */
    private final static int SEND_UNIT_CNT_POS = 28;

    /** 발송 성공 대상자 카운트가 저장될 위치 */
    private final static int SUCCESS_CNT_POS = 32;

    /** 발송 된 Unit 인서트 카운트가 저장될 위치 */
    private final static int INSERT_CNT_POS = 36;

    /** 재발송을 위하여 생성되는 Unit의 ID */
    private final static int CREATE_UNIT_ID_POS = 40;

    /** 발송 완료 시간이 저장 된다 */
    private final static int END_TIME_POS = 44;

    /** 재발송 수가 저장될 파일의 위치 값은 2byte short로 저장된다. */
    private final static int SEND_NO_POS = 52;

    /** 재발송 카운트가 저장된다 */
    private final static int RETRY_CNT_POS = 54;

    /** Message 객체가 저장될 위치가 저장된다. */
    private final static int MESSAGE_OBJ_POS = 64;

    /** Contents 객체가 저장될 위치가 저장된다 */
    private final static int CONTENT_OBJ_POS = 68;

    
    /** Contents2 객체가 저장될 위치가 저장된다 */
    private final static int CONTENT_OBJ_POS2 = 100;
    
    
    /** Message 객체가 저장될 위치 */
    private final static int MESSAGE_POS = 512;

    /**
     * 상태 파일이 저장될 디렉토리명 ${work.path} 디렉토리아래에 생성된다.
     */
    private final static String dirName = "message";

    /**
     * 상태 파일이 위치할 디렉토리의 File 객체
     */
    private static File dirFile;
    static {

        //작업 디렉토리 경로를 얻는다.
        //설정되지 않았을 경우 repository를 사용한다.
        String workPath = ConfigLoader.getString("work.path", "repository");
        dirFile = new File(workPath, dirName);
        if (!dirFile.exists()) {
            if (!dirFile.mkdirs()) {
                System.err.println("디렉토리를 생성할 수 없음: " + dirFile);
                System.exit(1);
            }
        }
        else {
            if (!dirFile.isDirectory()) {
                System.err.println("디렉토리가 아님: " + dirFile);
                System.exit(1);
            }
        }
    }

    /** MessageID[TASK_NO-SUB_TASKNO]  발송 업무의 구분할 수 있는 Key로 사용된다. */
    private String messageID;

    /** Message의 발송 상태 [run, pause, stop, wait] */
    private int status;

    /** Unit의 상태 */
    private int unitStatus;

    /** 총 Unit 수 */
    private int totalUnitCount;

    /** 마지막 Unit의 ID */
    private int lastUnitID;

    /** 마지막 읽혀진 Unit의 ID */
    private int loadUnitID;

    /** Generator로 전달된 Unit의 카운트 */
    private int deliveryUnitCount;

    /** 발송완료된 Unit의 카운트 */
    private int sendUnitCount;

    /** 총 대상자 수 */
    private int totalCount;

    /** Generator로 전달된 대상자의 수 */
    private int deliveryCount;

    /** 발송 완료된 Unit에 포함된 대상자의 수 */
    private int sendCount;

    /** 발송 완료된 Unit에 포함된 대상자 중 발송 성공수 */
    private int successCount;

    private int insertUnitCount;

    /** Message의 상태와 정보를 저장한 파일의 File객체 */
    private File messageFile;

    /** 가장 최근 생성된 Unit의 ID */
    private int createUnitID;

    /** 초기 재발송 수, 재발송할 경우 이전 발송 수에 1증가한 값이다 */
    private int sendNo;

    /** 가장 최근 발송 한 대상자의 발송 시각, Message의 발송 완료 시각 */
    private long endTime;

    /**
     * MessageStatus 객체를 생성한다.
     * @param messageID 메시지 아이디
     * */
    public MessageStatus(String messageID) {
        messageFile = new File(dirFile, messageID);
    }

    /**
     * 파일의 존재 유무를 검사한다.
     * 존재 할경우 등록된 Message로 간주한다.
     *
     * @return 파일이 존재할 경우 true
     */
    public boolean exists() {
        return messageFile.exists();
    }

    /**
     * Mesage의 상태 정보로 파일을 생성한다.
     *
     * @param totalUnitCnt Unit수
     * @param lastUnit 마지막 UnitID
     * @param totalCnt 대상자 수
     * @throws IOException 파일 입출력시 에러가 발생하면
     */
    public void createFile(int totalUnitCnt, int lastUnit, int totalCnt)
        throws IOException {
        totalUnitCount = totalUnitCnt;
        lastUnitID = lastUnit;
        totalCount = totalCnt;
        status = SEND_RUN;
        unitStatus = UNIT_LOAD_RUN;
        createUnitID = lastUnit + 1;

        RandomAccessFile out = null;
        IOException exception = null;
        try {
            out = new RandomAccessFile(messageFile, "rw");
            out.seek(SEND_STATUS_POS);
            out.writeByte( (byte) status);
            out.seek(UNIT_STATUS_POS);
            out.writeByte( (byte) unitStatus);
            out.seek(TOTAL_UNIT_CNT_POS);
            out.writeInt(totalUnitCnt);
            out.seek(LAST_UNIT_ID_POS);
            out.writeInt(lastUnit);
            out.seek(TOTAL_CNT_POS);
            out.writeInt(totalCnt);
            out.seek(CREATE_UNIT_ID_POS);
            out.writeInt(createUnitID);
        }
        catch (IOException ex) {
            exception = ex;
        }
        finally {
            if (out != null) {
                try {
                    out.close();
                }
                catch (IOException ex) {
                }
                out = null;
            }
        }
        if (exception != null) {
            throw exception;
        }
    }

    /**
     * Message의 상태 파일로 부터 Message의 상태정보를 읽어온다.
     *
     * @throws IOException 파일 입출력시 에러가 발생하면
     */
    public void readFile()
        throws IOException {
        RandomAccessFile in = null;
        IOException exception = null;
        try {
            in = new RandomAccessFile(messageFile, "rw");
            in.seek(SEND_STATUS_POS);
            status = in.readByte();
            in.seek(UNIT_STATUS_POS);
            unitStatus = in.readByte();
            in.seek(SEND_NO_POS);
            sendNo = in.readShort();
            in.seek(TOTAL_UNIT_CNT_POS);
            totalUnitCount = in.readInt();
            in.seek(LAST_UNIT_ID_POS);
            lastUnitID = in.readInt();
            in.seek(LOAD_UNIT_ID_POS);
            loadUnitID = in.readInt();
            in.seek(TOTAL_CNT_POS);
            totalCount = in.readInt();
            in.seek(DELIVERY_CNT_POS);
            deliveryCount = in.readInt();
            in.seek(SEND_CNT_POS);
            sendCount = in.readInt();
            in.seek(SEND_UNIT_CNT_POS);
            sendUnitCount = in.readInt();
            in.seek(SUCCESS_CNT_POS);
            successCount = in.readInt();
            in.seek(CREATE_UNIT_ID_POS);
            createUnitID = in.readInt();
            in.seek(END_TIME_POS);
            endTime = in.readLong();
        }
        catch (IOException ex) {
            exception = ex;
        }
        finally {
            if (in != null) {
                try {
                    in.close();
                }
                catch (IOException ex) {
                }
                in = null;
            }
        }
        if (exception != null) {
            throw exception;
        }
    }

    /**
     * Message의 발송 카운트를 얻는다.
     * 발송 카운트는 이전 발송한 카운트이다.
     *
     * @return sendNo
     */
    public int getSendNo() {
        return sendNo;
    }

    /**
     * Message의 발송 상태를 얻는다.
     *
     * @return 발송 상태
     */
    public int getSendStatus() {
        return status;
    }

    /**
     * Message의 Unit상태를 얻는다.
     *
     * @return Unit의 상태
     */
    public int getUnitStatus() {
        return unitStatus;
    }

    /**
     * 대상자 수를 얻는다.
     *
     * @return 대상자 수
     */
    public int getTotalCount() {
        return totalCount;
    }

    /**
     * 발송 완료된 대상자 수를 얻는다.
     * 발송 결과파일에서 얻는다.
     * 재발송시 누적되지 않는다.
     *
     * @return 발송 완료된 대상자 수
     */
    public int getSendCount() {
        return sendCount;
    }

    /**
     * 발송 성공된 대상자수를 얻는다.
     * 발송 결과 파일에서 얻고 재발송시 누적되지 않는다.
     *
     * @return 성공된 대상자 수
     */
    public int getSuccessCount() {
        return successCount;
    }

    /**
     * 마지막 Unit의 ID를 얻는다.
     *
     * @return LastUnitID
     */
    public int getLastUnitID() {
        return lastUnitID;
    }

    /**
     * Message의 발송 진행 상태를 변경하고 상태파일에 반영한다.
     *
     * @param status   변경될 Message의 발송 진행 상태
     * @exception Exception  입출력 에러가 발생할 경우
     */
    public void setSendStatus(int status)
        throws Exception {
        RandomAccessFile out = null;
        Exception exception = null;
        this.status = status;
        try {
            out = new RandomAccessFile(messageFile, "rw");
            out.seek(SEND_STATUS_POS);
            out.writeByte( (byte) status);
        }
        catch (Exception ex) {
            exception = ex;
        }
        finally {
            if (out != null) {
                try {
                    out.close();
                }
                catch (IOException ex) {
                }
                out = null;
            }
        }
        if (exception != null) {
            throw exception;
        }
    }

    /**
     * Message의 unit상태를 변경한다.
     *
     * @param unitStatus Message의 Unit상태
     * @throws Exception 입출력 에러가 발생할 경우
     */
    public void setUnitStatus(int unitStatus)
        throws Exception {
        RandomAccessFile out = null;
        Exception exception = null;
        this.unitStatus = unitStatus;
        try {
            out = new RandomAccessFile(messageFile, "rw");
            out.seek(UNIT_STATUS_POS);
            out.writeByte( (byte) unitStatus);
        }
        catch (Exception ex) {
            exception = ex;
        }
        finally {
            if (out != null) {
                try {
                    out.close();
                }
                catch (IOException ex) {
                }
                out = null;
            }
        }

        if (exception != null) {
            throw exception;
        }

    }

    public int getCreateUnitID() {
        if (createUnitID == 0) {
            createUnitID++;
        }
        return createUnitID;
    }

    public void writeCreateUnitID(int unitID)
        throws Exception {
        RandomAccessFile outFile = null;
        Exception ex = null;
        createUnitID = unitID;
        try {
            outFile = new RandomAccessFile(messageFile, "rw");
            outFile.seek(CREATE_UNIT_ID_POS);
            outFile.writeInt(unitID);
        }
        catch (Exception ex1) {
            ex = ex1;
        }
        finally {
            if (outFile != null) {
                try {
                    outFile.close();
                }
                catch (IOException ex2) {
                }
                outFile = null;
            }
        }
        if (ex != null) {
            throw ex;
        }
    }

    /**
     * 다음 읽을 Unit의 UnitID를 얻고 상태파일에 반영한다.
     *
     * @return 다음 읽을 Unit의 UnitID, 만일 UnitID가 마지막 UnitID보다 크면 -1를 리턴한다.
     * @exception IOException
     *                파일에 대한 입출력 에러가 발생하였을 경우
     */
    public int nextUnit() {
        if (loadUnitID >= lastUnitID) {
            try {
                setUnitStatus(UNIT_LOAD_END);
            }
            catch (Exception ex) {
            }
            return -1;
        }
        return++loadUnitID;
    }

    /**
     * 로드된 Unit의 ID를 파일에 write한다.
     * 프로세스 중단시 동기화를 위해 실행한다.
     *
     * @throws RepositoryException 입출력 에러가 발생할 경우
     */
    public void syncUnit()
        throws RepositoryException {
        IOException exception = null;
        RandomAccessFile out = null;
        try {
            out = new RandomAccessFile(messageFile, "rw");
            out.seek(LOAD_UNIT_ID_POS);
            out.writeInt(loadUnitID);
        }
        catch (IOException ex) {
            exception = ex;
        }
        finally {
            if (out != null) {
                try {
                    out.close();
                }
                catch (IOException ex) {
                }
                out = null;
            }
        }
        if (exception != null) {
            throw new RepositoryException(exception.getMessage());
        }
    }

    /**
     * Generator로 전달된 대상자 수를 얻는다
     *
     * @return 전송 카운트 반환
     */
    public int getDeliveryCount() {
        return deliveryCount;
    }

    /**
     * 전달된 대상자 수를 업데이트 한다.
     *
     * @param size 전달된 Unit에 포함된 대상자 수
     * @exception Exception File에 write시 입출력 에러가 발생하였을 경우
     */
    public void increaseDelivery(int size)
        throws Exception {
        Exception exception = null;
        RandomAccessFile out = null;
        deliveryCount += size;
        try {
            out = new RandomAccessFile(messageFile, "rw");
            out.seek(DELIVERY_CNT_POS);
            out.writeInt(deliveryCount);
        }
        catch (Exception ex) {
            exception = ex;
        }
        finally {
            if (out != null) {
                try {
                    out.close();
                }
                catch (IOException ex) {
                }
                out = null;
            }
        }
        if (exception != null) {
            throw exception;
        }
    }

    /**
     * Message의 발송중인 상태인지 확인한다.
     *
     * @return 발송중이면 true, 그렇지 않으면 false
     */
    public boolean isSendRun() {
        boolean b_run = false;
        switch (status) {
            case SEND_RUN:
                b_run = true;
        }
        return b_run;
    }

    /*
     * 가장 최근 발송된 대상자의 발송 시각을 Write한다.
     *
     * @param 가장 최근 발송된 대상자의 발송 시각
     */
    public void setEndTime(long time) {
        RandomAccessFile out = null;
        endTime = time;
        try {
            out = new RandomAccessFile(messageFile, "rw");
            out.seek(END_TIME_POS);
            out.writeLong(time);
        }
        catch (Exception ex) {
        }
        finally {
            if (out != null) {
                try {
                    out.close();
                }
                catch (Exception ex) {
                }
                out = null;
            }
        }
    }

    /**
     * Message의 이전 가장 최근 발송된 시각을 얻는다.
     *
     * @return 이전 발송된 시각
     */
    public long getEndTime() {
        return endTime;
    }

    /**
     * Message의 정보와 컨텐츠를 정해진 위치에 write한다.
     *
     * @param message	Message의 발송 정보
     * @param contents Message의 컨텐츠 정보
     * @throws Exception
     */
    public void writeMessage(Message message, Contents contents)
        throws Exception {
        RandomAccessFile out = null;
        Exception exception = null;
        byte[] bytes = null;
        try {
            out = new RandomAccessFile(messageFile, "rw");

            //Message 객체 write
            bytes = ObjectStream.toBytes(message);   // message : neo_task + neo_segmnet 정보 
            int len = bytes.length;
            int pos = MESSAGE_POS;
            out.seek(MESSAGE_OBJ_POS);
            out.writeInt(pos);
            out.seek(pos);
            out.writeInt(len);
            pos += 4;
            out.seek(pos);
            out.write(bytes, 0, len);

            //Contents 객체 write
            bytes = ObjectStream.toBytes(contents); // contents : 메일 본문 내용
            pos += len;
            len = bytes.length;
            out.seek(CONTENT_OBJ_POS);
            out.writeInt(pos);
            out.seek(pos);
            pos += 4;
            out.writeInt(len);
            out.seek(pos);
            out.write(bytes, 0, len);
            
        }
        catch (Exception ex) {
            exception = ex;
        }
        finally {
            if (out != null) {
                try {
                    out.close();
                }
                catch (Exception ex) {
                }
                out = null;
            }
            bytes = null;
        }
        if (exception != null) {
            throw exception;
        }
    }
    
    /**
     * Message의 정보와 컨텐츠를 정해진 위치에 write한다.
     *
     * @param message	Message의 발송 정보
     * @param contents Message의 컨텐츠 정보
     * @throws Exception
     */
    public void writeMessage(Message message, Contents contents, Contents2 contents2)
        throws Exception {
        RandomAccessFile out = null;
        Exception exception = null;
        byte[] bytes = null;
        try {
            out = new RandomAccessFile(messageFile, "rw");

            //Message 객체 write
            bytes = ObjectStream.toBytes(message);   // message : neo_task + neo_segmnet 정보 
            int len = bytes.length;
            int pos = MESSAGE_POS;
            out.seek(MESSAGE_OBJ_POS);
            out.writeInt(pos);
            out.seek(pos);
            out.writeInt(len);
            pos += 4;
            out.seek(pos);
            out.write(bytes, 0, len);

            //Contents 객체 write
            bytes = ObjectStream.toBytes(contents); // contents : 메일 본문 내용
            pos += len;
            len = bytes.length;
            out.seek(CONTENT_OBJ_POS);
            out.writeInt(pos);
            out.seek(pos);
            pos += 4;
            out.writeInt(len);
            out.seek(pos);
            out.write(bytes, 0, len);
            
            
            //Contents2 객체 write
            bytes = ObjectStream.toBytes(contents2); // contents2 : 메일 본문 내용
            pos += len;
            len = bytes.length;
            out.seek(CONTENT_OBJ_POS2);
            out.writeInt(pos);
            out.seek(pos);
            pos += 4;
            out.writeInt(len);
            out.seek(pos);
            out.write(bytes, 0, len);
        }
        catch (Exception ex) {
            exception = ex;
        }
        finally {
            if (out != null) {
                try {
                    out.close();
                }
                catch (Exception ex) {
                }
                out = null;
            }
            bytes = null;
        }
        if (exception != null) {
            throw exception;
        }
    }

    /**
     * Message의 발송 정보를 파일에서 읽는다.
     *
     * @return Message의 발송 정보
     * @throws Exception 파일 입출력시 에러가 발생하거나 잘못된 객체를 읽어 올 경우
     */
    public Message readMessage()
        throws Exception {
        RandomAccessFile in = null;
        Exception exception = null;
        Message message = null;
        byte[] bytes = null;
        try {
            in = new RandomAccessFile(messageFile, "rw");
            in.seek(MESSAGE_OBJ_POS);
            int pos = in.readInt();
            in.seek(pos);
            int len = in.readInt();
            bytes = new byte[len];
            pos += 4;
            in.seek(pos);
            in.read(bytes, 0, len);
            message = (Message) ObjectStream.toObject(bytes);
        }
        catch (Exception ex) {
            exception = ex;
        }
        finally {
            if (in != null) {
                try {
                    in.close();
                }
                catch (Exception ex) {
                }
                in = null;
            }
            bytes = null;
        }
        if (exception != null) {
            throw exception;
        }

        return message;
    }

    /**
     * Message의 컨텐츠를 저장한 Contents객체를 파일에서 읽는다.
     *
     * @return 파일에서 읽은 Message의 Contents객체
     * @throws Exception 입출력 에러가 발생할 경우
     */
    public Contents readContent()
        throws Exception {
        RandomAccessFile in = null;
        Exception exception = null;
        Contents contents = null;
        byte[] bytes = null;
        try {
            in = new RandomAccessFile(messageFile, "rw");
            in.seek(CONTENT_OBJ_POS);
            int pos = in.readInt();
            in.seek(pos);
            int len = in.readInt();
            bytes = new byte[len];
            pos += 4;
            in.seek(pos);
            in.read(bytes, 0, len);
            contents = (Contents) ObjectStream.toObject(bytes);
        }
        catch (Exception ex) {
            exception = ex;
        }
        finally {
            if (in != null) {
                try {
                    in.close();
                }
                catch (Exception ex) {
                }
                in = null;
            }
            bytes = null;
        }
        if (exception != null) {
            throw exception;
        }

        return contents;
    }
    
    
    /**
     * Message의 컨텐츠를 저장한 Contents객체를 파일에서 읽는다.
     *
     * @return 파일에서 읽은 Message의 Contents객체
     * @throws Exception 입출력 에러가 발생할 경우
     */
    public Contents2 readContent2()
        throws Exception {
        RandomAccessFile in = null;
        Exception exception = null;
        Contents2 contents2 = null;
        byte[] bytes = null;
        try {
            in = new RandomAccessFile(messageFile, "rw");
            in.seek(CONTENT_OBJ_POS2);
            int pos = in.readInt();
            in.seek(pos);
            int len = in.readInt();
            bytes = new byte[len];
            pos += 4;
            in.seek(pos);
            in.read(bytes, 0, len);
            contents2 = (Contents2) ObjectStream.toObject(bytes);
        }
        catch (Exception ex) {
            exception = ex;
        }
        finally {
            if (in != null) {
                try {
                    in.close();
                }
                catch (Exception ex) {
                }
                in = null;
            }
            bytes = null;
        }
        if (exception != null) {
            throw exception;
        }

        return contents2;
    }

    /**
     * 발송 완료된 Unit에 포함된 대상자의 수와 성공한 대상자의 수를 저장한다.
     *
     * @param send 발송 완료된 Unit에 포함된 대상자의 수
     * @param success 발송 성공한 대상자의 수
     * @throws Exception 입출력 에러가 발생할 경우
     */
    public void putSendCount(int send, int success)
        throws Exception {
        sendCount += send;
        successCount += success;
        RandomAccessFile out = null;
        Exception exception = null;
        try {
            out = new RandomAccessFile(messageFile, "rw");
            out.seek(SEND_CNT_POS);
            out.writeInt(sendCount);
            out.seek(SUCCESS_CNT_POS);
            out.writeInt(successCount);
        }
        catch (Exception ex) {
            exception = ex;
        }
        finally {
            if (out != null) {
                try {
                    out.close();
                }
                catch (Exception ex) {
                }
                out = null;
            }
        }
    }

    /**
     * Message의 발송 상태를 재발송 대기 상태로 변경한다.
     *
     * @param retryCount Message의 발송 카운트를 얻기 위해
     */
    public void setWait(int retryCount) {
        RandomAccessFile out = null;
        lastUnitID = createUnitID;
        createUnitID++;
        deliveryCount = 0;
        status = SEND_WAIT;
        unitStatus = UNIT_LOAD_RUN;
        sendNo += (retryCount + 1);

        try {
            out = new RandomAccessFile(messageFile, "rw");
            out.seek(SEND_STATUS_POS);
            out.writeByte(status);
            out.seek(UNIT_STATUS_POS);
            out.writeByte(unitStatus);

            out.seek(LAST_UNIT_ID_POS);
            out.writeInt(lastUnitID);

            out.seek(DELIVERY_CNT_POS);
            out.writeInt(deliveryCount);

            out.seek(CREATE_UNIT_ID_POS);
            out.writeInt(createUnitID);

            out.seek(SEND_NO_POS);
            out.writeShort(sendNo);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        finally {
            if (out != null) {
                try {
                    out.close();
                }
                catch (Exception ex) {}
                out = null;
            }
        }
    }

    /**
     * Message 파일을 삭제한다.
     */
    public void delete() {
        if (messageFile.exists()) {
            messageFile.delete();
        }
    }

    /**
     * MessageID로 Mesage 파일을 삭제한다.
     * @param messageID 삭제하려고 하는 Message의 ID
     */
    public static void delete(String messageID) {
        File messageFile = new File(dirFile, messageID);
        if (messageFile.exists()) {
            messageFile.delete();
        }
    }

    /**
     * Message의 파일 리스트로 MesageID 배열을 얻는다.
     *
     * @return MessageID 배열
     */
    public static String[] list() {
        return dirFile.list();
    }
}
