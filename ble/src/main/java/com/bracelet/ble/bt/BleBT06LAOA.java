package com.bracelet.ble.bt;

import android.bluetooth.BluetoothDevice;

public class BleBT06LAOA extends AbstractBleBT {

    public BleBT06LAOA(BluetoothDevice device, int rssi, String rawString, String regex) {
        super(device, rssi, rawString, regex);
    }

    @Override
    public String getTypeName() {
        return BT06LAOATypeName;
    }
}
