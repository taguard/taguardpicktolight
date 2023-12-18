package com.bracelet.ble.bt;

import android.bluetooth.BluetoothDevice;
import android.text.TextUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BleBT06AOA extends AbstractBleBT {

    private boolean movingStatus;   //1byte, (moving: 08, resting: 09)

    //moving status fields
    private int accelerationX;  //1byte
    private int accelerationY;  //1byte
    private int accelerationZ;  //1byte

    //resting status fields
    private boolean tampered;       //1byte, bit5: tamper, (normal:0, tampered:1)
    private int version;            //1byte, dec
    private int voltage;            //1byte, (value * 100)mV

    public BleBT06AOA(BluetoothDevice device, int rssi, String rawString, String regex) {
        super(device, rssi, rawString, regex);
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(rawString);
        if (matcher.find() && matcher.groupCount() == 4) {
            movingStatus = parseMovingStatus(matcher.group(1));
            if (movingStatus) {
                accelerationX = parseAcceleration(matcher.group(2));
                accelerationY = parseAcceleration(matcher.group(3));
                accelerationZ = parseAcceleration(matcher.group(4));
            } else {
                tampered = parseTampered(matcher.group(2));
                version = parseVersion(matcher.group(3));
                voltage = parseVoltage(matcher.group(4));
            }
        }
    }

    @Override
    public String getTypeName() {
        return BT06AOATypeName;
    }

    public boolean isMovingStatus() {
        return movingStatus;
    }

    public int getAccelerationX() {
        return accelerationX;
    }

    public int getAccelerationY() {
        return accelerationY;
    }

    public int getAccelerationZ() {
        return accelerationZ;
    }

    public boolean isTampered() {
        return tampered;
    }

    public int getVersion() {
        return version;
    }

    public int getVoltage() {
        return voltage;
    }

    private boolean parseMovingStatus(String data) {
        return TextUtils.equals("08", data);
    }

    private int parseAcceleration(String data) {
        return Integer.parseInt(data, 16);
    }

    private boolean parseTampered(String data) {
        int value = Integer.parseInt(data, 16);
        return (value & 0x20) == 0x20;
    }

    private int parseVersion(String data) {
        return Integer.parseInt(data, 16);
    }

    private int parseVoltage(String data) {
        return Integer.parseInt(data, 16) * 100;
    }
}
