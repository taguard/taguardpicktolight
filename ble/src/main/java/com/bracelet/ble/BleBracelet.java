package com.bracelet.ble;

import android.content.Context;

public interface BleBracelet {
    String getName();

    String getAddress();

    int getRssi();

    void connect(Context context, ConnectionCallback callback);

    void disconnect();

    void writeBytes(byte[] bytes);

    interface ConnectionCallback {

        void onConnect();

        void onDisconnect();

        void onRead(byte[] bytes);

    }
}
