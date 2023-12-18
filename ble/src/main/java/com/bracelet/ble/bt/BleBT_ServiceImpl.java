package com.bracelet.ble.bt;

import android.content.Context;

import com.bracelet.ble.BleException;
import com.bracelet.ble.BleService;
import com.bracelet.ble.BleServiceImpl;

public class BleBT_ServiceImpl extends BleServiceImpl implements BleBT_Service {

    public BleBT_ServiceImpl(Context context, final BleService.SearchBTBroadcastCallback callback) throws BleException {
        super(context);
        setSearchBTBroadcastCallback(new SearchBTBroadcastCallback() {
            @Override
            public void onDiscoverBleBT(BleBT bt) {
                if (callback != null) {
                    callback.onDiscoverBleBT(bt);
                }
            }
        });
    }

}
