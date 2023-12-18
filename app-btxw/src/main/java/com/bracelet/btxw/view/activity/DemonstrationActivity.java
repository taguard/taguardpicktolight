package com.bracelet.btxw.view.activity;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bracelet.ble.btxw.BTXW_Device;
import com.bracelet.ble.btxw.BTXW_Service;
import com.bracelet.ble.btxw.BTXW_ServiceImpl;
import com.bracelet.btxw.R;
import com.bracelet.btxw.utils.Configs;
import com.bracelet.btxw.utils.SharedPreferencesUtils;
import com.bracelet.btxw.view.adapter.MultiTagsAdapter;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DemonstrationActivity extends BaseActivity {

    @BindView(R.id.etSecond)
    EditText etSecond;
    @BindView(R.id.rvBluetooth)
    RecyclerView rvBluetooth;
    @BindView(R.id.srScan)
    SwipeRefreshLayout srLayout;
    @BindView(R.id.btnLight)
    QMUIRoundButton btnLight;
    @BindView(R.id.btnLightSetting)
    QMUIRoundButton btnLightSetting;

    private BTXW_Service mBTXWService;
    private MultiTagsAdapter mAdapter;
    private Handler mHandler;
    private static final int ScanSecond = 5;
    private Runnable stopSearchRunnable = new Runnable() {
        @Override
        public void run() {
            mBTXWService.stopScan();
            srLayout.setRefreshing(false);
        }
    };

    public static void actionStart(Context context) {
        context.startActivity(new Intent(context, DemonstrationActivity.class));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demonstration);
        ButterKnife.bind(this);
        initView();
        if (checkBlePermissions()) {
            initBTXW();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mBTXWService != null) {
            mBTXWService.stopScan();
        }
        mHandler.removeCallbacks(stopSearchRunnable);

        List<BTXW_Device> devices = new ArrayList<>();
        devices.addAll(mAdapter.getConnectingDevices());
        devices.addAll(mAdapter.getConnectedDevices());
        for(BTXW_Device d : devices){
            d.disconnect();
        }
    }

    @Override
    public void onConnectItemClick() {
        initBTXW();
    }

    private void initView() {
        mHandler = new Handler(Looper.getMainLooper());
        useBle = false;
        setToolbarTitle(getResources().getString(R.string.btn_demonstration));

        mAdapter = new MultiTagsAdapter(this);
        onItemClick();
        rvBluetooth.setAdapter(mAdapter);
        rvBluetooth.setLayoutManager(new LinearLayoutManager(this));

        srLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!isGPSOpen()) {
                        showOpenGPSDialog();
                        srLayout.setRefreshing(false);
                        mAdapter.removeDevices();
                        return;
                    }
                }
                startScan();
            }
        });
        srLayout.setColorSchemeResources(R.color.colorPrimary);

        btnLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<BTXW_Device> devices = mAdapter.getConnectedDevices();
                final int effectiveSeconds = getInputSeconds();
                if (effectiveSeconds < 1 || effectiveSeconds > 250) {
                    showToast(getResources().getString(R.string.toast_effective_seconds_range));
                    return;
                }
                if(!devices.isEmpty()) {
                    String lightSetting = SharedPreferencesUtils.getLightSetting(mContext);
                    for (BTXW_Device d : devices) {
                        if (d.isConnected()) {
                            showInfoTip(getResources().getString(R.string.toast_finding));
                            if (TextUtils.equals(lightSetting, Configs.sTagItems[0])) {
                                //BT11
                                byte[] chooseItem = new byte[] {BTXW_Device.LIGHT_RED_GROUP};
                                d.openDeviceMultipleLights(new BTXW_Device.OpenDeviceLightCallback() {
                                    @Override
                                    public void onResult(int status) {
                                        if (status == 0) {

                                        } else {
                                            d.disconnect();
                                        }
                                    }
                                }, chooseItem, (byte)effectiveSeconds);
                            } else if (TextUtils.equals(lightSetting, Configs.sTagItems[1])) {
                                //BT07
                                byte lightingType = BTXW_Device.LIGHT_RED;
                                d.openDeviceTwoLights(new BTXW_Device.OpenDeviceLightCallback() {
                                    @Override
                                    public void onResult(int status) {
                                        if (status == 0) {

                                        } else {
                                            d.disconnect();
                                        }
                                    }
                                }, lightingType, (byte)effectiveSeconds);
                            } else {
                                //BT01
                                d.openDeviceLight(new BTXW_Device.OpenDeviceLightCallback() {
                                    @Override
                                    public void onResult(int status) {
                                        if (status == 0) {

                                        } else {
                                            d.disconnect();
                                        }
                                    }
                                }, (byte) effectiveSeconds);
                            }
                        }
                    }
                } else {
                    showInfoTip(getResources().getString(R.string.toast_to_connect));
                }
            }
        });

        btnLightSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLightingSettingGroup();
            }
        });
    }

    private void initBTXW() {
        try {
            mBTXWService = new BTXW_ServiceImpl(this, new BTXW_Service.SearchCallback() {
                @Override
                public void onDiscoverBleDevice(BTXW_Device device) {
                    mAdapter.addDevice(device);
                }
            });
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!isGPSOpen()) {
                    showOpenGPSDialog();
                    return;
                }
            }
            startScan();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void onItemClick() {
        mAdapter.setOnAdapterItemClickListener(new MultiTagsAdapter.OnAdapterItemClickListener() {
            @Override
            public void onClick(BTXW_Device device,int position) {
                if (device != null) {
                    if (device.isConnected()) {
                        device.disconnect();
                    } else {
                        if (!device.isConnecting()) {
                            connectDevice(device);
                            mAdapter.notifyItemChanged(position);
                        }
                    }
                }
            }
       });
    }

    private void startScan() {
        mAdapter.removeDevices();
        mBTXWService.startScan();
        srLayout.setRefreshing(true);
        mHandler.postDelayed(stopSearchRunnable,1000 * ScanSecond);
    }

    public void connectDevice(BTXW_Device device) {
        device.connect(mContext, new BTXW_Device.ConnectionCallback() {
            @Override
            public void onConnect() {
                mAdapter.updateConnectionState(device);
                showToast(getResources().getString(R.string.toast_ble_connect));
            }

            @Override
            public void onDisconnect() {
                mAdapter.updateConnectionState(device);
                showToast(getResources().getString(R.string.toast_ble_disconnect));
            }
        });
    }

    private int getInputSeconds() {
        String text = etSecond.getText().toString();
        if (!TextUtils.isEmpty(text)) {
            if(text.length() < 4) {
                return Integer.parseInt(text);
            }
        }
        return -1;
    }

}
