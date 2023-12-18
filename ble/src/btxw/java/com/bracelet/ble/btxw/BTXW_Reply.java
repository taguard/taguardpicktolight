package com.bracelet.ble.btxw;

public class BTXW_Reply {

    private byte commandCode;
    private byte replyLength;
    private byte[] replyData;

    public BTXW_Reply(byte commandCode, byte replyLength, byte[] replyData) {
        this.commandCode = commandCode;
        this.replyLength = replyLength;
        this.replyData = replyData;
    }

    public byte getCommandCode() {
        return commandCode;
    }

    public byte getReplyLength() {
        return replyLength;
    }

    public byte[] getReplyData() {
        return replyData;
    }
}
