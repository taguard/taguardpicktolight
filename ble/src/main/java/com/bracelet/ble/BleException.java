package com.bracelet.ble;

public class BleException extends Exception {

    private int errorCode;

    public BleException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }

}
