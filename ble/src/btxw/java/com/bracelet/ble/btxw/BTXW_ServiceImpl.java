package com.bracelet.ble.btxw;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;

import com.bracelet.ble.BleBracelet;
import com.bracelet.ble.BleBraceletImpl;
import com.bracelet.ble.BleException;
import com.bracelet.ble.BleService;
import com.bracelet.ble.BleServiceImpl;

public class BTXW_ServiceImpl extends BleServiceImpl implements BTXW_Service {
    public BTXW_ServiceImpl(Context context, final BTXW_Service.SearchCallback callback) throws BleException {
        super(context);
        setCallback(new BleService.SearchCallback() {
            @Override
            public void onDiscoverBleBracelet(BleBracelet bracelet) {
                if (callback != null) {
                    callback.onDiscoverBleDevice(new BTXW_DeviceImpl(bracelet));
                }
            }
        });
    }

    public BTXW_DeviceImpl obtainBTXW_Device(Context context, String macAddress) throws BleException {
        BluetoothAdapter adapter =  obtainBluetoothAdapter();
        BluetoothDevice device = adapter.getRemoteDevice(macAddress);
        if (device == null) {
            throw new BleException(-2, "Bluetooth is not found");
        }
        return new BTXW_DeviceImpl(new BleBraceletImpl(context, device, 0));
    }
}
