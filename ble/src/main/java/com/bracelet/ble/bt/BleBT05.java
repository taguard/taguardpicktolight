package com.bracelet.ble.bt;

import android.bluetooth.BluetoothDevice;
import android.text.TextUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BleBT05 extends AbstractBleBT {

    private int vibrated;          //1byte, (true:0x11, false:0x00)
    private double temperature;     //2bytes, (value1 + value2 * 0.1)℃, if T is negative, value1 + 0x80
    private int voltage;            //1byte, (value1 << 8 | value2)mV

    public BleBT05(BluetoothDevice device, int rssi, String rawString, String regex) {
        super(device, rssi, rawString, regex);
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(rawString);
        if (matcher.find() && matcher.groupCount() == 3) {
            vibrated = parseVibrateFlag(matcher.group(1));
            temperature = parseTemperature(matcher.group(2));
            voltage = parseVoltage(matcher.group(3));
        }
    }

    @Override
    public String getTypeName() {
        return BT05TypeName;
    }

    public int getVibrated() {
        return vibrated;
    }

    public double getTemperature() {
        return temperature;
    }

    public int getVoltage() {
        return voltage;
    }

    private int parseVibrateFlag(String data) {
        return Integer.parseInt(data, 16);
    }

    private double parseTemperature(String data) {
        double t;
        int value1 = Integer.parseInt(data.substring(0, 2), 16);
        int value2 = Integer.parseInt(data.substring(2, 4), 16);
        t = value1 >= 0x80 ? -(value1 - 0x80) + value2 * 0.1 : value1 + value2 * 0.1;
        return new BigDecimal(t).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    private int parseVoltage(String data) {
        int v;
        int value1 = Integer.parseInt(data.substring(0, 2), 16);
        int value2 = Integer.parseInt(data.substring(2, 4), 16);
        v = (value1 << 8) | value2;
        return v;
    }
}
