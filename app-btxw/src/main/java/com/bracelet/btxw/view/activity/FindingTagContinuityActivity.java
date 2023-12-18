package com.bracelet.btxw.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.bracelet.ble.btxw.BTXW_Device;
import com.bracelet.ble.btxw.BTXW_Service;
import com.bracelet.ble.btxw.BTXW_ServiceImpl;
import com.bracelet.btxw.R;
import com.bracelet.btxw.entity.Asset;
import com.bracelet.btxw.utils.Configs;
import com.bracelet.btxw.utils.SharedPreferencesUtils;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FindingTagContinuityActivity extends BaseActivity{
    @BindView(R.id.llBackground)
    LinearLayout llBackground;
    @BindView(R.id.tvAssetName)
    TextView tvAssetName;
    @BindView(R.id.tvAssetNo)
    TextView tvAssetNo;
    @BindView(R.id.tvTagName)
    TextView tvTagName;
    @BindView(R.id.tvTagNo)
    TextView tvTagNo;
    @BindView(R.id.btnFindContinuity)
    QMUIRoundButton btnFindContinuity;
    @BindView(R.id.btnLightSetting)
    QMUIRoundButton btnLightSetting;
    @BindView(R.id.tvPrompt)
    TextView tvPrompt;
    @BindView(R.id.svLog)
    ScrollView svLog;

    private static final String EXTRA_ASSET = "asset";
    private Asset mAsset;
    private String macAddress;

    private long mLastClickTime = 0;
    private static final long CLICK_INTERVAL = 1000;

    private static final int SCAN_TIME = 60 * 1000;          //ble scan time
    private static final int BREAK_TIME = 2 * 1000;          //ble scan break time
    private static final int CONNECT_MIN_RSSI = -70;    //the min RSSI value to connect

    private BTXW_Service mBTXWService;
    private List<Integer> rssiList;
    private Handler mHandler;

    private Runnable startScanRunnable = new Runnable() {
        @Override
        public void run() {
            if (mBTXWService != null) {
                mBTXWService.startScan();
            }
            mHandler.postDelayed(stopScanRunnable, SCAN_TIME);
        }
    };

    private Runnable stopScanRunnable = new Runnable() {
        @Override
        public void run() {
            if (mBTXWService != null) {
                mBTXWService.stopScan();
            }
            mHandler.postDelayed(startScanRunnable, BREAK_TIME);
        }
    };

    private Runnable promptRunnable = new Runnable() {
        @Override
        public void run() {
            final byte soundSecond = 1;
            final byte lightSecond = 10;
            String lightSetting = SharedPreferencesUtils.getLightSetting(mContext);
            mBTXWDevice.openDeviceBeep(new BTXW_Device.OpenDeviceBeepCallback() {
                @Override
                public void onResult(int i) {
                    if (TextUtils.equals(lightSetting, Configs.sTagItems[0])) {
                        //BT11
                        byte[] chooseItem = new byte[] {BTXW_Device.LIGHT_RED_GROUP};
                        mBTXWDevice.openDeviceMultipleLights(new BTXW_Device.OpenDeviceLightCallback() {
                            @Override
                            public void onResult(int status) {
                                if (status == 0) {
                                    mHandler.postDelayed(promptRunnable, lightSecond * 1000);
                                } else {
                                    showFailTip(status);
                                }
                            }
                        }, chooseItem, lightSecond);
                    } else if (TextUtils.equals(lightSetting, Configs.sTagItems[1])) {
                        //BT07
                        byte lightingType = BTXW_Device.LIGHT_RED;
                        mBTXWDevice.openDeviceTwoLights(new BTXW_Device.OpenDeviceLightCallback() {
                            @Override
                            public void onResult(int status) {
                                if (status == 0) {
                                    mHandler.postDelayed(promptRunnable, lightSecond * 1000);
                                } else {
                                    showFailTip(status);
                                }
                            }
                        }, lightingType, lightSecond);
                    } else {
                        //BT01
                        mBTXWDevice.openDeviceLight(new BTXW_Device.OpenDeviceLightCallback() {
                            @Override
                            public void onResult(int status) {
                                if (status == 0) {
                                    mHandler.postDelayed(promptRunnable, lightSecond * 1000);
                                } else {
                                    showFailTip(status);
                                }
                            }
                        }, lightSecond);
                    }
                }
            }, soundSecond);
        }
    };

    public static void actionStart(Context context, Asset asset) {
        Intent intent = new Intent(context, FindingTagContinuityActivity.class);
        intent.putExtra(EXTRA_ASSET, asset);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finding_tag_continuity);
        ButterKnife.bind(this);
        initView();
        if (loadData()) {
            loadView();
            initService();
        } else {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finishFinding();
        disconnectBTXWDevice();
    }

    @Override
    public void onDeviceConnect() {
        super.onDeviceConnect();
        appendText(getResources().getString(R.string.content_find_lighting));
        mHandler.post(promptRunnable);
    }

    @Override
    public void onDeviceDisconnect() {
        super.onDeviceDisconnect();
    }

    @Override
    public void toConnectBTXW() {
        showToast(getResources().getString(R.string.toast_to_find));
    }

    public void onBtnFindContinuityClick(View view) {
        if (mBTXWDevice != null && mBTXWDevice.isConnecting()) {
            return;
        }
        if (SystemClock.elapsedRealtime() - mLastClickTime < CLICK_INTERVAL) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();
        if (TextUtils.equals(btnFindContinuity.getText().toString(), getResources().getString(R.string.btn_find_continuity_start))) {
            btnFindContinuity.setText(getResources().getString(R.string.btn_find_continuity_stop));
            tvPrompt.setText(getResources().getString(R.string.content_find_prompt));
            tvPrompt.append("\r\n");
            appendText(getResources().getString(R.string.content_find_start));
            mHandler.post(startScanRunnable);
        } else {
            btnFindContinuity.setText(getResources().getString(R.string.btn_find_continuity_start));
            finishFinding();
            disconnectBTXWDevice();
            appendText(getResources().getString(R.string.content_find_stop));
        }
    }

    private void initView() {
        setToolbarTitle(getResources().getString(R.string.title_find_long_range));
        btnLightSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLightingSettingGroup();
            }
        });
    }

    private boolean loadData() {
        mHandler = new Handler();
        rssiList = new ArrayList<>();
        mAsset = (Asset)getIntent().getSerializableExtra(EXTRA_ASSET);
        if (mAsset != null) {
            macAddress = mAsset.getTagMac();
            return !TextUtils.isEmpty(macAddress);
        }
        showToast(getResources().getString(R.string.toast_no_tag_info));
        return false;
    }

    private void loadView() {
        if (mAsset != null) {
            Asset asset = mAsset;
            tvAssetName.setText(String.format(getResources().getString(R.string.content_good), asset.getAssetName()));
            tvAssetNo.setText(String.format(getResources().getString(R.string.content_number), asset.getAssetNo()));
            tvTagName.setText(String.format(getResources().getString(R.string.content_tag), asset.getTagName()));
            tvTagNo.setText(String.format(getResources().getString(R.string.content_mac), asset.getTagMac()));
        }
    }

    private void initService() {
        try {
            mBTXWService = new BTXW_ServiceImpl(this, new BTXW_Service.SearchCallback() {
                @Override
                public void onDiscoverBleDevice(BTXW_Device device) {
                    if (TextUtils.equals(device.getAddress(), macAddress)) {
                        onDeviceFinding(device);
                    }
                }
            });
        } catch (Exception e) {
            showToast(e.getMessage());
        }
    }

    private void finishFinding() {
        mHandler.removeCallbacks(promptRunnable);
        mHandler.removeCallbacks(startScanRunnable);
        mHandler.removeCallbacks(stopScanRunnable);
        if (mBTXWService != null) {
            mBTXWService.stopScan();
        }
    }

    private void onDeviceFinding(BTXW_Device device) {
        int rssi = device.getRssi();
        rssiList.add(rssi);
        appendText(String.format(getResources().getString(R.string.content_current_signal), String.valueOf(rssi)));
        if (rssi > CONNECT_MIN_RSSI) {
            mBTXWDevice = device;
            finishFinding();
            connectBTXWDevice();
        }
    }

    private void appendText(String str) {
        tvPrompt.append(str);
        tvPrompt.append("\r\n");
        svLog.fullScroll(ScrollView.FOCUS_DOWN);
    }
}
