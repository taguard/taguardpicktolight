package com.bracelet.ble;

import com.bracelet.ble.bt.BleBT;

public interface BleService {

    void startScan();

    void stopScan();

    interface SearchCallback {

        void onDiscoverBleBracelet(BleBracelet bracelet);
    }

    void startScanForBTBroadcast();

    void stopScanForBTBroadcast();

    interface SearchBTBroadcastCallback {
        void onDiscoverBleBT(BleBT bt);
    }
}
