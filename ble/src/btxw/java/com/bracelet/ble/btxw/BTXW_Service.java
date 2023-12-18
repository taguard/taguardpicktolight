package com.bracelet.ble.btxw;

import android.content.Context;

import com.bracelet.ble.BleException;

public interface BTXW_Service {
    void startScan();

    void stopScan();

    BTXW_DeviceImpl obtainBTXW_Device(Context context, String macAddress) throws BleException;

    public interface SearchCallback {
        void onDiscoverBleDevice(BTXW_Device device);
    }
}
