package com.bracelet.ble;

/**
 * the error code of operating bluetooth bracelet;
 */
public class BleError {

    /**
     * operate successfully
     */
    public static final int STATUS_SUCCESS = 0;

    /**
     * invalid parameter
     */
    public static final int INVALID_PARAMETER = -101;

    /**
     * device is not opened
     */
    public static final int DEVICE_NOT_OPENED = -201;

    /**
     * device is busy
     */
    public static final int DEVICE_BUSY = -202;

    /**
     * device rely error
     */
    public static final int DEVICE_NOASK = -203;

    /**
     * device operate failed
     */
    public static final int DEVICE_FAILED = -204;

    /**
     * communication time out
     */
    public static final int COMMUNICATE_NO_REPLY = -301;

    /**
     * communication error, reply format is mismatch
     */
    public static final int COMMUNICATE_ERROR = -302;

    /**
     * communication failed, the analysis is incorrect
     */
    public static final int COMMUNICATE_FAILED = -303;

}
