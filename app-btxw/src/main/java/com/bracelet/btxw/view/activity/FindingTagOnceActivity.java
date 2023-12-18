package com.bracelet.btxw.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.bracelet.ble.BleException;
import com.bracelet.ble.btxw.BTXW_Device;
import com.bracelet.ble.btxw.BTXW_ServiceImpl;
import com.bracelet.btxw.R;
import com.bracelet.btxw.entity.Asset;
import com.bracelet.btxw.utils.Configs;
import com.bracelet.btxw.utils.SharedPreferencesUtils;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;


import butterknife.BindView;
import butterknife.ButterKnife;

public class FindingTagOnceActivity extends BaseActivity {
    @BindView(R.id.tvAssetName)
    TextView tvAssetName;
    @BindView(R.id.tvAssetNo)
    TextView tvAssetNo;
    @BindView(R.id.tvTagName)
    TextView tvTagName;
    @BindView(R.id.tvTagNo)
    TextView tvTagNo;
    @BindView(R.id.etSeconds)
    EditText etSeconds;
    @BindView(R.id.btnFindOnce)
    Button btnFindOnce;
    @BindView(R.id.btnLightSetting)
    QMUIRoundButton btnLightSetting;

    private static final String EXTRA_ASSET = "asset";
    private Asset mAsset;

    private int countDownSecond = 0;
    private Handler mHandler;
    private Runnable countDownRunnable = new Runnable() {
        @Override
        public void run() {
            btnFindOnce.setText(String.format(getResources().getString(R.string.btn_find_countdown), String.valueOf(countDownSecond)));
            countDownSecond--;
            if (countDownSecond >= 0) {
                mHandler.postDelayed(countDownRunnable, 1000);
            } else {
                btnFindOnce.setText(getResources().getString(R.string.btn_find_once));
            }
        }
    };

    public static void actionStart(Context context, Asset asset) {
        Intent intent = new Intent(context, FindingTagOnceActivity.class);
        intent.putExtra(EXTRA_ASSET, asset);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finding_tag_once);
        ButterKnife.bind(this);
        initView();
        if (loadData()) {
            loadView();
            onConnectItemClick();
        } else {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disconnectBTXWDevice();
        mHandler.removeCallbacks(countDownRunnable);
    }

    public void toConnectBTXW() {
        connectBTXWDevice();
    }

    public void onBtnFindOnceClick(View view) {
        String lightSetting = SharedPreferencesUtils.getLightSetting(mContext);
        if (TextUtils.equals(btnFindOnce.getText().toString(), getResources().getString(R.string.btn_find_once))) {
            final int seconds = getInputSeconds();
            if (seconds >= 3 && seconds <= 20) {
                if (isDeviceConnected()) {
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
                                        } else {
                                            showFailTip(status);
                                        }
                                    }
                                }, chooseItem, (byte) seconds);
                            } else if (TextUtils.equals(lightSetting, Configs.sTagItems[1])) {
                                //BT07
                                byte lightingType = BTXW_Device.LIGHT_RED;
                                mBTXWDevice.openDeviceTwoLights(new BTXW_Device.OpenDeviceLightCallback() {
                                    @Override
                                    public void onResult(int status) {
                                        if (status == 0) {
                                        } else {
                                            showFailTip(status);
                                        }
                                    }
                                }, lightingType, (byte) seconds);
                            } else {
                                //BT01
                                mBTXWDevice.openDeviceLight(new BTXW_Device.OpenDeviceLightCallback() {
                                    @Override
                                    public void onResult(int status) {
                                        if (status == 0) {
                                        } else {
                                            showFailTip(status);
                                        }
                                    }
                                }, (byte) seconds);
                            }
                        }
                    }, (byte) seconds);
                    countDownSecond = seconds;
                    mHandler.post(countDownRunnable);
                }
            } else {
                showToast(getResources().getString(R.string.toast_time_input_limit));
            }
        } else {
            showToast(getResources().getString(R.string.toast_finding));
        }
    }

    private void initView() {
        setToolbarTitle(getResources().getString(R.string.title_find_close_range));
        btnLightSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLightingSettingGroup();
            }
        });
    }

    private boolean loadData() {
        mHandler = new Handler();
        mAsset = (Asset)getIntent().getSerializableExtra(EXTRA_ASSET);
        if (mAsset != null) {
            String address = mAsset.getTagMac();
            if (!TextUtils.isEmpty(address)) {
                try {
                    mBTXWDevice = new BTXW_ServiceImpl(mContext, null).obtainBTXW_Device(mContext, address);
                    return true;
                } catch (BleException e) {
                    showToast(e.getMessage());
                }
            }
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

    private int getInputSeconds() {
        String text = etSeconds.getText().toString();
        if (!TextUtils.isEmpty(text)) {
            return Integer.parseInt(text);
        }
        return -1;
    }
}
