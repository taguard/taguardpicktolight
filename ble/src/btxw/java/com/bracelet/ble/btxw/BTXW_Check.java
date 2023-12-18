package com.bracelet.ble.btxw;

public class BTXW_Check {
    public static byte sumBuffer(byte[] data, int length) {
        byte sum = 0;
        for (int b = 0; b < length; b ++) {
            sum += data[b];
        }
        return sum;
    }
}
