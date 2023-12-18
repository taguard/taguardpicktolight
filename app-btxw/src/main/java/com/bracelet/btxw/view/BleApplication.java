package com.bracelet.btxw.view;

import android.app.Application;
import android.content.Context;

import com.bracelet.ble.btxw.BTXW_Device;

import org.litepal.LitePal;

public class BleApplication extends Application {
    private static Context context;
    private StringBuilder logString;
    private BTXW_Device mBTXWDevice;

    @Override
    public void onCreate() {
        super.onCreate();
        initData();
    }

    public String getLogString() {
        return logString.toString();
    }

    public void addLogString(String log) {
        logString.insert(0, "\r\n");
        logString.insert(0, log);
    }

    public BTXW_Device getBTXWDevice() {
        return mBTXWDevice;
    }

    public void setBTXWDevice(BTXW_Device BTXWDevice) {
        mBTXWDevice = BTXWDevice;
    }

    private void initData() {
        logString = new StringBuilder();
        context = getApplicationContext();
        LitePal.initialize(this);
    }

    public static Context getContext() {
        return context;
    }
}
