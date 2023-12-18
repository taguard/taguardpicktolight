package com.bracelet.ble.btxw;

public class BTXW_Parse {
    //reply command code is added 0x80
    //example: send cmd code 0x01
    //         receive cmd code 0x81
    private static final byte ReplyOffset = (byte)0x80;

    static final int CMD_FINISH = 0;
    static final int CMD_PARTIAL = 1;
    static final int ERROR_HEAD = -1;
    static final int ERROR_SUM = -2;
    static final int ERROR_REPLY = -3;

    private byte commandCode;
    private byte[] buffer = new byte[0];

    public BTXW_Parse(byte commandCode) {
        this.commandCode = (byte)(commandCode + ReplyOffset);
    }

    BTXW_Reply getReply() {
        byte dataLength = buffer[1];
        byte[] data = new byte[dataLength];
        System.arraycopy(buffer, 2, data, 0, dataLength);
        return new BTXW_Reply(commandCode, dataLength, data);
    }

    int onReply(byte[] bytes) {
        byte[] buffer2 = new byte[buffer.length + bytes.length];
        System.arraycopy(buffer, 0, buffer2, 0, buffer.length);
        System.arraycopy(bytes, 0, buffer2, buffer.length, bytes.length);
        buffer = buffer2;

        if (buffer.length < 4) {
            return CMD_PARTIAL;
        }

        int dataLength = buffer[1];
        if (buffer.length < dataLength + 3){
            return CMD_PARTIAL;
        }

        if (buffer[0] != commandCode) {
            return ERROR_HEAD;
        }

        if (buffer[buffer.length - 1] != BTXW_Check.sumBuffer(buffer, buffer.length - 1)) {
            return ERROR_SUM;
        }

        if (buffer.length == dataLength + 3) {
            return CMD_FINISH;
        }
        return ERROR_REPLY;
    }
}
