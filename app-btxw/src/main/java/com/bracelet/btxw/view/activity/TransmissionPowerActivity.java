package com.bracelet.btxw.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.bracelet.ble.btxw.BTXW_Device;
import com.bracelet.btxw.R;
import com.bracelet.btxw.utils.Configs;
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet;
import com.qmuiteam.qmui.widget.grouplist.QMUICommonListItemView;
import com.qmuiteam.qmui.widget.grouplist.QMUIGroupListView;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TransmissionPowerActivity extends BaseActivity {
    @BindView(R.id.listViewTransmissionPower)
    QMUIGroupListView listViewTransmissionPower;
    @BindView(R.id.btnGetTransmissionPower)
    QMUIRoundButton btnGetTransmissionPower;
    @BindView(R.id.btnSetTransmissionPower)
    QMUIRoundButton btnSetTransmissionPower;
    @BindView(R.id.btnFactoryTransmissionPower)
    QMUIRoundButton btnFactoryTransmissionPower;

    public static final int MODE_GET_TRANSMISSION_POWER = 0x801;
    public static final int MODE_SET_TRANSMISSION_POWER = 0x802;

    public static final String EXTRA_MODE = "mode";

    //Note: device needs to re-authenticate after re-connected
    private  boolean hasDisconnected = false;

    private byte mTransmissionPower = BTXW_Device.POWER_DEFAULT;

    QMUICommonListItemView itemTransmissionPower;

    private View.OnClickListener onItemTransmissionPowerClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showTransmissionPowers();
        }
    };

    private View.OnClickListener onBtnGetTransmissionPowerClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (isDeviceConnected()) {
                setItemViewDetailText(itemTransmissionPower, "");
                mBTXWDevice.getDeviceTransmissionPower(new BTXW_Device.GetDeviceTransmissionPowerCallback() {
                    @Override
                    public void onResult(int status, byte transmissionPower) {
                        if (status == 0) {
                            mTransmissionPower = transmissionPower;
                            setItemViewDetailText(itemTransmissionPower, getPowerDescription(transmissionPower));
                        } else {
                            showFailTip(status);
                        }
                    }
                });
            }
        }
    };

    private View.OnClickListener onBtnSetTransmissionPowerClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (isDeviceConnected()) {
                if (hasDisconnected) {
                    showInfoTip(getResources().getString(R.string.content_need_auth));
                    return;
                }
                mBTXWDevice.setDeviceTransmissionPower(new BTXW_Device.SetDeviceTransmissionPowerCallback() {
                    @Override
                    public void onResult(int status) {
                        if (status == 0) {
                            showSuccessTip();
                        } else {
                            showFailTip(status);
                        }
                    }
                }, mTransmissionPower);
            }
        }
    };

    private View.OnClickListener onBtnFactoryTransmissionPowerClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (isDeviceConnected()) {
                if (hasDisconnected) {
                    showInfoTip(getResources().getString(R.string.content_need_auth));
                    return;
                }
                mTransmissionPower = BTXW_Device.POWER_DEFAULT;

                mBTXWDevice.setDeviceTransmissionPower(new BTXW_Device.SetDeviceTransmissionPowerCallback() {
                    @Override
                    public void onResult(int status) {
                        if (status == 0) {
                            showSuccessTip();
                            setItemViewDetailText(itemTransmissionPower, getPowerDescription(mTransmissionPower));
                        } else {
                            showFailTip(status);
                        }
                    }
                }, mTransmissionPower);
            }
        }
    };

    public static void actionStart(Context context, int mode) {
        Intent intent = new Intent(context, TransmissionPowerActivity.class);
        intent.putExtra(EXTRA_MODE, mode);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transimission_power);
        ButterKnife.bind(this);
        initView();
        getTransmissionPower();
    }

    @Override
    public void onDeviceConnect() {
        super.onDeviceConnect();
        getTransmissionPower();
    }

    @Override
    public void onDeviceDisconnect() {
        super.onDeviceDisconnect();
        hasDisconnected = true;
    }

    private void initView() {
        int mode = getIntent().getIntExtra(EXTRA_MODE, MODE_SET_TRANSMISSION_POWER);

        itemTransmissionPower = listViewTransmissionPower.createItemView(getResources().getString(R.string.title_transmission_power));
        itemTransmissionPower.setOrientation(QMUICommonListItemView.HORIZONTAL);
        itemTransmissionPower.setEnabled(mode == MODE_SET_TRANSMISSION_POWER);

        QMUIGroupListView.newSection(mContext)
                .setTitle(getResources().getString(R.string.title_transmission_setting))
                .setDescription(getResources().getString(R.string.content_transmission_power_limit))
                .addItemView(itemTransmissionPower, onItemTransmissionPowerClick)
                .addTo(listViewTransmissionPower);

        btnGetTransmissionPower.setOnClickListener(onBtnGetTransmissionPowerClick);
        btnSetTransmissionPower.setOnClickListener(onBtnSetTransmissionPowerClick);
        btnFactoryTransmissionPower.setOnClickListener(onBtnFactoryTransmissionPowerClick);

        btnSetTransmissionPower.setVisibility(mode == MODE_SET_TRANSMISSION_POWER ? View.VISIBLE : View.GONE);
        btnFactoryTransmissionPower.setVisibility(mode == MODE_SET_TRANSMISSION_POWER ? View.VISIBLE : View.GONE);
    }

    private void getTransmissionPower() {
        btnGetTransmissionPower.performClick();
    }

    private void showTransmissionPowers() {
        final QMUIBottomSheet.BottomListSheetBuilder builder = new QMUIBottomSheet.BottomListSheetBuilder(mContext,
                true);
        for (int i = 0; i < Configs.sTransmissionPowers.length; i ++) {
            builder.addItem(Configs.sTransmissionPowers[i].getDescription());
            if (Configs.sTransmissionPowers[i].getValue() == mTransmissionPower) {
                builder.setCheckedIndex(i);
            }
        }
        builder.setOnSheetItemClickListener(new QMUIBottomSheet.BottomListSheetBuilder.OnSheetItemClickListener() {
            @Override
            public void onClick(QMUIBottomSheet dialog, View itemView, int position, String tag) {
                mTransmissionPower = Configs.sTransmissionPowers[position].getValue();
                itemTransmissionPower.setDetailText(Configs.sTransmissionPowers[position].getDescription());
                dialog.dismiss();
            }
        });
        builder.build().show();
    }

    private String getPowerDescription(byte power) {
        String description = getResources().getString(R.string.content_unknown);
        for (Configs.ConfigItem item : Configs.sTransmissionPowers) {
            if (item.getValue() == power) {
                description = item.getDescription();
                break;
            }
        }
        return description;
    }
}
