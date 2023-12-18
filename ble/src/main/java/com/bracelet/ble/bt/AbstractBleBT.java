package com.bracelet.ble.bt;

import android.bluetooth.BluetoothDevice;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import java.util.Date;

abstract class AbstractBleBT implements BleBT {
    private final BluetoothDevice device;
    private final int rssi;
    private final long searedTimestamp;

    private final String rawString;
    private final String regex;

    public AbstractBleBT(BluetoothDevice device, int rssi, String rawString, String regex) {
        this.device = device;
        this.rssi = rssi;
        this.rawString = rawString;
        this.regex = regex;
        searedTimestamp = new Date().getTime();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof AbstractBleBT) {
            return ((AbstractBleBT)obj).device.equals(device);
        }
        return super.equals(obj);
    }

    @Override
    public String getName() {
        return device == null ? "null" : device.getName();
    }

    @Override
    public String getAddress() {
        return device == null ? "null" : device.getAddress();
    }

    @Override
    public int getRssi() {
        return rssi;
    }

    @Override
    public String getRawString() {
        return rawString;
    }

    @Override
    public String getRegex() {
        return regex;
    }

    @Override
    public long getSearedTimestamp() {
        return searedTimestamp;
    }

    @Override
    public String getTypeName() {
        return null;
    }
}
