package messager.center.control;

import java.util.*;

import messager.center.repository.*;
import messager.common.util.*;

class ControlResponse
{
    private final static int RECORD_LENGTH = 26;
    private final static int CODE_OFFSET = 0;
    private final static int COUNT_OFFSET = 2;
    private final static int RECORD_OFFSET = 4;
    private final static int TASK_NO_OFFSET = 0;
    private final static int SUB_TASK_NO_OFFSET = 4;
    private final static int STATUS_OFFSET = 8;
    private final static int TOTAL_OFFSET = 10;
    private final static int SEND_OFFSET = 14;
    private final static int SUCCESS_OFFSET = 18;
    private final static int DELIVERY_OFFSET = 22;
    MessageMap messageMap;

    public ControlResponse() {
        messageMap = MessageMap.getInstance();
    }

    public byte[] list(short code) {
        MessageMap messageMap = MessageMap.getInstance();
        ArrayList messageList = messageMap.handlerList();
        int size = messageList.size();

        byte[] bytes = new byte[RECORD_OFFSET + (size * RECORD_LENGTH)];

        BytesUtil.short2bytes(code, bytes, CODE_OFFSET);
        BytesUtil.short2bytes( (short) size, bytes, COUNT_OFFSET);
        for (int i = 0; i < messageList.size(); i++) {
            MessageHandler msgHandler = (MessageHandler) messageList.get(i);
            int taskNo = msgHandler.getTaskNo();
            int subTaskNo = msgHandler.getSubTaskNo();
            int status = msgHandler.getSendStatus();
            int totalCount = msgHandler.getTotalCount();
            int sendCount = msgHandler.getSendCount();
            int successCount = msgHandler.getSuccessCount();
            int deliveryCount = msgHandler.getDeliveryCount();

            int pos = RECORD_OFFSET + (i * RECORD_LENGTH);

            BytesUtil.int2bytes(taskNo, bytes, (pos + TASK_NO_OFFSET));
            BytesUtil.int2bytes(subTaskNo, bytes, (pos + SUB_TASK_NO_OFFSET));
            BytesUtil.short2bytes( (short) status, bytes, (pos + STATUS_OFFSET));
            BytesUtil.int2bytes(totalCount, bytes, (pos + TOTAL_OFFSET));
            BytesUtil.int2bytes(sendCount, bytes, (pos + SEND_OFFSET));
            BytesUtil.int2bytes(successCount, bytes, (pos + SUCCESS_OFFSET));
            BytesUtil.int2bytes(deliveryCount, bytes, (pos + DELIVERY_OFFSET));
        }

        return bytes;
    }

    /**
     * 발송 진행 정보를 리턴한다.
     * 응답 성공 코드 : 0
     * 응답 실패 코드 : 1
     */
    public byte[] info(short code, int taskNo, int subTaskNo) {
        int status = 0;
        int totalCount = 0;
        int sendCount = 0;
        int successCount = 0;
        int deliveryCount = 0;

        String messageID = messageMap.createMessageID(taskNo, subTaskNo);

        MessageHandler msgHandler = (MessageHandler) messageMap
            .lookup(messageID);

        byte[] bytes = new byte[RECORD_OFFSET + RECORD_LENGTH];

        if (msgHandler != null) {
            status = msgHandler.getSendStatus();
            totalCount = msgHandler.getTotalCount();
            sendCount = msgHandler.getSendCount();
            successCount = msgHandler.getSuccessCount();
            deliveryCount = msgHandler.getDeliveryCount();

            BytesUtil.short2bytes( (short) 0, bytes, CODE_OFFSET);
            BytesUtil.short2bytes( (short) 1, bytes, COUNT_OFFSET);

            BytesUtil.int2bytes(taskNo, bytes, RECORD_OFFSET + TASK_NO_OFFSET);
            BytesUtil.int2bytes(subTaskNo, bytes, RECORD_OFFSET + SUB_TASK_NO_OFFSET);
            BytesUtil.short2bytes( (short) status, bytes, RECORD_OFFSET + STATUS_OFFSET);
            BytesUtil.int2bytes(totalCount, bytes, RECORD_OFFSET + TOTAL_OFFSET);
            BytesUtil.int2bytes(sendCount, bytes, RECORD_OFFSET + SEND_OFFSET);
            BytesUtil.int2bytes(successCount, bytes, RECORD_OFFSET + SUCCESS_OFFSET);
            BytesUtil.int2bytes(deliveryCount, bytes, RECORD_OFFSET + DELIVERY_OFFSET);
        }
        else {
            BytesUtil.short2bytes( (short) 1, bytes, CODE_OFFSET);
            BytesUtil.short2bytes( (short) 1, bytes, COUNT_OFFSET);
            BytesUtil.int2bytes(taskNo, bytes, RECORD_OFFSET + TASK_NO_OFFSET);
            BytesUtil.int2bytes(subTaskNo, bytes, RECORD_OFFSET + SUB_TASK_NO_OFFSET);
            BytesUtil.short2bytes( (short) 0, bytes, RECORD_OFFSET + STATUS_OFFSET);
            BytesUtil.int2bytes(0, bytes, RECORD_OFFSET + TOTAL_OFFSET);
            BytesUtil.int2bytes(0, bytes, RECORD_OFFSET + SEND_OFFSET);
            BytesUtil.int2bytes(0, bytes, RECORD_OFFSET + SUCCESS_OFFSET);
            BytesUtil.int2bytes(0, bytes, RECORD_OFFSET + DELIVERY_OFFSET);
        }

        return bytes;
    }

    public byte[] pause(int taskNo, int subTaskNo) {
        short code = 0x0001;

        String messageID = messageMap.createMessageID(taskNo, subTaskNo);
        MessageHandler msgHandler = (MessageHandler) messageMap
            .lookup(messageID);
        if (msgHandler != null && msgHandler.setPause()) {
            code = 0x0000;
        }

        return info(code, taskNo, subTaskNo);
    }

    public byte[] stop(int taskNo, int subTaskNo) {
        short code = 0x0001;
        String messageID = messageMap.createMessageID(taskNo, subTaskNo);
        MessageHandler msgHandler = (MessageHandler) messageMap
            .lookup(messageID);
        if (msgHandler != null && msgHandler.setStop()) {
            code = 0x0000;
        }
        return info(code, taskNo, subTaskNo);
    }

    public byte[] start(int taskNo, int subTaskNo) {
        short code = 0x0001;
        String messageID = messageMap.createMessageID(taskNo, subTaskNo);
        MessageHandler msgHandler = (MessageHandler) messageMap
            .lookup(messageID);
        if (msgHandler != null && msgHandler.setStart()) {
            code = 0x0000;
        }
        return info(code, taskNo, subTaskNo);
    }

    public byte[] getList() {
        short code = 0x0000;

        return list(code);
    }

    public byte[] getInfo(int taskNo, int subTaskNo) {
        short code = 0x0000;

        return info(code, taskNo, subTaskNo);
    }
}
