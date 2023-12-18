package com.bracelet.btxw.view.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bracelet.ble.btxw.BTXW_Device;
import com.bracelet.btxw.R;
import com.bracelet.btxw.entity.Asset;
import com.bracelet.btxw.utils.Configs;
import com.bracelet.btxw.utils.SharedPreferencesUtils;
import com.bracelet.btxw.utils.TextFormatUtils;
import com.bracelet.btxw.view.activity.BindingTagActivity;
import com.bracelet.btxw.view.adapter.BluetoothDeviceAdapter;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;

import org.litepal.LitePal;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BindingTagFragment extends BaseFragment {
    @BindView(R.id.etAssetName)
    EditText etAssetName;
    @BindView(R.id.etAssetNo)
    EditText etAssetNo;
    @BindView(R.id.llTag)
    LinearLayout llTag;
    @BindView(R.id.tvBindingTagName)
    TextView tvBindingTagName;
    @BindView(R.id.tvBindingTagMac)
    TextView tvBindingTagMac;
    @BindView(R.id.tvBindingHint)
    TextView tvBindingHint;
    @BindView(R.id.btnSelectTag)
    Button btnSelectTag;
    @BindView(R.id.btnBindTag)
    Button btnBindTag;
    @BindView(R.id.btnLightSetting)
    QMUIRoundButton btnLightSetting;

    private Handler mHandler;
    private boolean again;
    private Runnable mLightingRunnable = new Runnable() {
        @Override
        public void run() {
            if (mBTXWDevice != null) {
                final byte second = 5;
                String lightSetting = SharedPreferencesUtils.getLightSetting(mBaseActivity);
                if (TextUtils.equals(lightSetting, Configs.sTagItems[0])) {
                    //BT11
                    byte[] chooseItem = new byte[] {BTXW_Device.LIGHT_RED_GROUP};
                    mBTXWDevice.openDeviceMultipleLights(new BTXW_Device.OpenDeviceLightCallback() {
                        @Override
                        public void onResult(int status) {
                            if (again) {
                                mHandler.postDelayed(mLightingRunnable, second * 1000 + 100);
                            }
                        }
                    }, chooseItem, second);
                } else if (TextUtils.equals(lightSetting, Configs.sTagItems[1])) {
                    //BT07
                    byte lightingType = BTXW_Device.LIGHT_RED;
                    mBTXWDevice.openDeviceTwoLights(new BTXW_Device.OpenDeviceLightCallback() {
                        @Override
                        public void onResult(int i) {
                            if (again) {
                                mHandler.postDelayed(mLightingRunnable, second * 1000 + 100);
                            }
                        }
                    }, lightingType, second);
                } else {
                    //BT01
                    mBTXWDevice.openDeviceLight(new BTXW_Device.OpenDeviceLightCallback() {
                        @Override
                        public void onResult(int i) {
                            if (again) {
                                mHandler.postDelayed(mLightingRunnable, second * 1000 + 100);
                            }
                        }
                    }, second);
                }
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_binding, container, false);
        ButterKnife.bind(this, view);
        initView();
        initData();
        return view;
    }

    @Override
    public void onDeviceConnect() {
        super.onDeviceConnect();
        if (mBTXWDevice != null) {
            loadTag();
            final byte second = 1;
            mBTXWDevice.openDeviceBeep(new BTXW_Device.OpenDeviceBeepCallback() {
                @Override
                public void onResult(int i) {
                    again = true;
                    mHandler.post(mLightingRunnable);
                }
            }, second);
        }
    }

    @Override
    public void onDeviceDisconnect() {
        super.onDeviceDisconnect();
        again = false;
        mHandler.removeCallbacks(mLightingRunnable);
    }

    @Override
    public void onScanCodeResult(String code) {
        super.onScanCodeResult(code);
        etAssetNo.setText(code);
    }

    private void initView() {
        btnSelectTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBtnSelectTagClick();
            }
        });
        btnBindTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBtnBindTagClick();
            }
        });
        etAssetNo.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_RIGHT = 2;
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(event.getRawX() >= (etAssetNo.getRight() -
                            etAssetNo.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        onScanIconClick();
                        return true;
                    }
                }
                return false;
            }
        });
        btnLightSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 mBaseActivity.showLightingSettingGroup();
            }
        });
    }

    private void initData() {
        mHandler = new Handler(Looper.getMainLooper());
    }

    private void onBtnSelectTagClick() {
        mBTXWDevice = mBaseActivity.getBTXWDevice();
        if (mBTXWDevice != null && mBTXWDevice.isConnecting()) {
            showToast(getResources().getString(R.string.toast_connecting));
            return;
        }
        if (mBTXWDevice != null && mBTXWDevice.isConnected()) {
            mBTXWDevice.disconnect();
        }
        hideTag();
        if (mBaseActivity.checkBlePermissions()) {
            mBaseActivity.openBleFragment(new BluetoothDeviceAdapter.OnAdapterItemClickListener() {
                @Override
                public void onClick(BTXW_Device device) {
                    showToast(getResources().getString(R.string.toast_connecting));
                }
            });
        }
    }

    private void onBtnBindTagClick() {
        if (TextUtils.isEmpty(etAssetName.getText().toString())) {
            showToast(getResources().getString(R.string.toast_input_name));
            return;
        }
        if (mBTXWDevice == null || !mBTXWDevice.isConnected()) {
            showToast(getResources().getString(R.string.toast_select_tag));
            return;
        }
        if (isDeviceMacExist(mBTXWDevice.getAddress())) {
            showToast(getResources().getString(R.string.toast_tag_bound));
            return;
        }
        String assetName = etAssetName.getText().toString();
        String assetNo = etAssetNo.getText().toString();
        String tagName = mBTXWDevice.getName();
        String tagMac = mBTXWDevice.getAddress();
        Asset asset = new Asset();
        asset.setAssetName(TextFormatUtils.formatString(assetName));
        asset.setAssetNo(TextFormatUtils.formatString(assetNo));
        asset.setTagName(TextFormatUtils.formatString(tagName));
        asset.setTagMac(TextFormatUtils.formatString(tagMac));
        asset.save();

        showToast(getResources().getString(R.string.toast_bind_complete));
        tvBindingHint.setText(getResources().getString(R.string.content_bound));
        BindingTagActivity activity = (BindingTagActivity) mBaseActivity;
        if (activity != null) {
            BindingListFragment fragment = activity.getBindingListFragment();
            fragment.addNewAsset(asset);
        }
        mBTXWDevice.disconnect();
    }

    private void loadTag() {
        tvBindingTagName.setText(mBTXWDevice.getName());
        tvBindingTagMac.setText(mBTXWDevice.getAddress());
        tvBindingHint.setText(getResources().getString(R.string.content_to_bind));
        llTag.setVisibility(View.VISIBLE);
    }

    private void hideTag() {
        llTag.setVisibility(View.INVISIBLE);
    }

    private void onScanIconClick() {
        etAssetNo.setText("");
        mBaseActivity.toScanCode();
    }

    private boolean isDeviceMacExist(String mac) {
        boolean exist = false;
        List<Asset> assets = LitePal
                .where("tagMac = ?", mac)
                .find(Asset.class);
        if (assets.size() > 0) {
            exist = true;
        }
        return exist;
    }
}
