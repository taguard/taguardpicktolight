package com.bracelet.btxw.view.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.bracelet.ble.btxw.BTXW_Device;
import com.bracelet.btxw.BuildConfig;
import com.bracelet.btxw.R;
import com.bracelet.btxw.utils.Configs;
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmui.widget.grouplist.QMUICommonListItemView;
import com.qmuiteam.qmui.widget.grouplist.QMUIGroupListView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SDKMainActivity extends BaseActivity {
    @BindView(R.id.listViewFinding)
    QMUIGroupListView listViewFinding;
    @BindView(R.id.listViewNormal)
    QMUIGroupListView listViewNormal;
    @BindView(R.id.listViewAuthentication)
    QMUIGroupListView listViewAuthentication;

    private byte mTwoLightingType = BTXW_Device.LIGHT_RED;
    private Configs.ConfigItem[] checkedItems = new Configs.ConfigItem[] {Configs.sMultipleLightingGroup[1]};
    private byte effectiveSeconds = 5;
    private boolean hasAuth = false;

    public interface OnSheetClickListener {
        void onClick(int position);
    }

    public interface OnConfirmClickListener {
        void onClick();
    }

    QMUICommonListItemView itemLighting;
    QMUICommonListItemView itemBeeping;
    QMUICommonListItemView itemTwoLighting;
    QMUICommonListItemView itemMultipleLighting;
    QMUICommonListItemView itemEffectiveSecond;

    QMUICommonListItemView itemGetMac;
    QMUICommonListItemView itemGetVersion;
    QMUICommonListItemView itemGetPower;
    QMUICommonListItemView itemGetTime;
    QMUICommonListItemView itemGetName;
    QMUICommonListItemView itemGetTransmissionPower;
    QMUICommonListItemView itemGetBluetoothBroadcastInterval;
    QMUICommonListItemView itemSetBluetoothBroadcastInterval;
    QMUICommonListItemView itemLog;

    QMUICommonListItemView itemAuth;
    QMUICommonListItemView itemSetTime;
    QMUICommonListItemView itemSetName;
    QMUICommonListItemView itemSetTransmissionPower;
    QMUICommonListItemView itemShutDown;

    private View.OnClickListener onItemLightingClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (isDeviceConnected()) {
                setItemViewDetailText(itemLighting, "");
                mBTXWDevice.openDeviceLight(new BTXW_Device.OpenDeviceLightCallback() {
                    @Override
                    public void onResult(int status) {
                        if (status == 0) {
                            setItemViewDetailText(itemLighting, getResources().getString(R.string.toast_command_success));
                        } else {
                            showFailTip(status);
                        }
                    }
                }, effectiveSeconds);
            }
        }
    };

    private View.OnClickListener onItemBeepingClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (isDeviceConnected()) {
                setItemViewDetailText(itemBeeping, "");
                mBTXWDevice.openDeviceBeep(new BTXW_Device.OpenDeviceBeepCallback() {
                    @Override
                    public void onResult(int status) {
                        if (status == 0) {
                            setItemViewDetailText(itemBeeping, getResources().getString(R.string.toast_command_success));
                        } else {
                            showFailTip(status);
                        }
                    }
                }, effectiveSeconds);
            }
        }
    };

    private View.OnClickListener onItemTwoLightingClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (isDeviceConnected()) {
                showTwoLightingType(new OnSheetClickListener() {
                    @Override
                    public void onClick(int position) {
                        setItemViewDetailText(itemTwoLighting, "");
                        mBTXWDevice.openDeviceTwoLights(new BTXW_Device.OpenDeviceLightCallback() {
                            @Override
                            public void onResult(int status) {
                                if (status == 0) {
                                    setItemViewDetailText(itemTwoLighting, getResources().getString(R.string.toast_command_success));
                                } else {
                                    showFailTip(status);
                                }
                            }
                        }, mTwoLightingType, effectiveSeconds);
                    }
                });
            }
        }
    };

    private View.OnClickListener onItemMultipleLightingClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (isDeviceConnected()) {
                showMultipleLightingGroup(new OnConfirmClickListener() {
                    @Override
                    public void onClick() {
                        setItemViewDetailText(itemMultipleLighting, "");
                        mBTXWDevice.openDeviceMultipleLights(new BTXW_Device.OpenDeviceLightCallback() {
                            @Override
                            public void onResult(int status) {
                                if (status == 0) {
                                    setItemViewDetailText(itemMultipleLighting, getResources().getString(R.string.toast_command_success));
                                } else {
                                    showFailTip(status);
                                }
                            }
                        }, getCheckedItemsValue(checkedItems), effectiveSeconds);
                    }
                });
            }
        }
    };

    private View.OnClickListener onItemEffectiveSecondClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (isDeviceConnected()) {
                final QMUIDialog.EditTextDialogBuilder builder = new QMUIDialog.EditTextDialogBuilder(mContext);
                builder.setTitle("")
                        .setPlaceholder(getResources().getString(R.string.content_input_effective_seconds))
                        .setInputType(InputType.TYPE_CLASS_NUMBER)
                        .addAction(getResources().getString(R.string.btn_cancel), new QMUIDialogAction.ActionListener() {
                            @Override
                            public void onClick(QMUIDialog dialog, int index) {
                                dialog.dismiss();
                            }
                        })
                        .addAction(getResources().getString(R.string.btn_confirm), new QMUIDialogAction.ActionListener() {
                            @Override
                            public void onClick(QMUIDialog dialog, int index) {
                                CharSequence count = builder.getEditText().getText();
                                if (!TextUtils.isEmpty(count)) {
                                    if (count.toString().length() < 4) {
                                        int i = Integer.parseInt(count.toString());
                                        if (i >= 1 && i <= 250) {
                                            effectiveSeconds = (byte)i;
                                            itemEffectiveSecond.setDetailText(count.toString());
                                            dialog.dismiss();
                                            return;
                                        }
                                    }
                                    showToast(getResources().getString(R.string.toast_effective_seconds_range));
                                }
                            }
                        })
                        .create().show();
            }
        }
    };

    private View.OnClickListener onItemGetMacClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (isDeviceConnected()) {
                setItemViewDetailText(itemGetMac, "");
                mBTXWDevice.getDeviceMac(new BTXW_Device.GetDeviceMacCallback() {
                    @Override
                    public void onResult(int status, String mac) {
                        if (status == 0) {
                            setItemViewDetailText(itemGetMac, mac);
                        } else {
                            showFailTip(status);
                        }
                    }
                });
            }
        }
    };

    private View.OnClickListener onItemGetVersionClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (isDeviceConnected()) {
                setItemViewDetailText(itemGetVersion, "");
                mBTXWDevice.getDeviceVersion(new BTXW_Device.GetDeviceVersionCallback() {
                    @Override
                    public void onResult(int status, String version) {
                        if (status == 0) {
                            setItemViewDetailText(itemGetVersion, version);
                        } else {
                            showFailTip(status);
                        }
                    }
                });
            }
        }
    };

    private View.OnClickListener onItemGetPowerClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (isDeviceConnected()) {
                setItemViewDetailText(itemGetPower, "");
                mBTXWDevice.getDevicePower(new BTXW_Device.GetDevicePowerCallback() {
                    @Override
                    public void onResult(int status, short voltageValue, byte powerPercent) {
                        if (status == 0) {
                            setItemViewDetailText(itemGetPower, String.format("%smV\r\n%s%%",
                                    voltageValue, powerPercent));
                        } else {
                            showFailTip(status);
                        }
                    }
                });
            }
        }
    };

    private View.OnClickListener onItemGetTimeClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (isDeviceConnected()) {
                setItemViewDetailText(itemGetTime, "");
                mBTXWDevice.getDeviceTime(new BTXW_Device.GetDeviceTimeCallback() {
                    @Override
                    public void onResult(int status, long unixTimestampSecond) {
                        if (status == 0) {
                            setItemViewDetailText(itemGetTime,
                                    new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
                                    .format(new Date(unixTimestampSecond * 1000)));
                        } else {
                            showFailTip(status);
                        }
                    }
                });
            }
        }
    };

    private View.OnClickListener onItemGetNameClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (isDeviceConnected()) {
                setItemViewDetailText(itemGetName, "");
                mBTXWDevice.getDeviceName(new BTXW_Device.GetDeviceNameCallback() {
                    @Override
                    public void onResult(int status, String deviceName) {
                        if (status == 0) {
                            setItemViewDetailText(itemGetName, deviceName);
                        } else {
                            showFailTip(status);
                        }
                    }
                });
            }
        }
    };

    private View.OnClickListener onItemGetTransmissionPowerClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (isDeviceConnected()) {
                TransmissionPowerActivity.actionStart(mContext, TransmissionPowerActivity.MODE_GET_TRANSMISSION_POWER);
            }
        }
    };

    private View.OnClickListener onItemGetBluetoothBroadcastInterval = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (isDeviceConnected()) {
                setItemViewDetailText(itemGetBluetoothBroadcastInterval, "");
                mBTXWDevice.getBluetoothBroadcastInterval(new BTXW_Device.GetBluetoothBroadcastIntervalCallback() {
                    @Override
                    public void onResult(int status, byte interval) {
                        if (status == 0) {
                            setItemViewDetailText(itemGetBluetoothBroadcastInterval, String.valueOf(interval * 10));
                        } else {
                            showFailTip(status);
                        }
                    }
                });
            }
        }
    };

    private View.OnClickListener onItemSetBluetoothBroadcastInterval = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (isDeviceConnected()) {
                final QMUIDialog.EditTextDialogBuilder builder = new QMUIDialog.EditTextDialogBuilder(mContext);
                builder.setTitle("")
                        .setPlaceholder(getResources().getString(R.string.content_input_broadcast_interval_placeholder))
                        .setInputType(InputType.TYPE_CLASS_NUMBER)
                        .addAction(getResources().getString(R.string.btn_cancel), new QMUIDialogAction.ActionListener() {
                            @Override
                            public void onClick(QMUIDialog dialog, int index) {
                                dialog.dismiss();
                            }
                        })
                        .addAction(getResources().getString(R.string.btn_confirm), new QMUIDialogAction.ActionListener() {
                            @Override
                            public void onClick(QMUIDialog dialog, int index) {
                                CharSequence count = builder.getEditText().getText();
                                if (!TextUtils.isEmpty(count)) {
                                    if (count.toString().length() < 5) {
                                        int i = Integer.parseInt(count.toString());
                                        if (i >= 50 && i <= 1000) {
                                            setItemViewDetailText(itemSetBluetoothBroadcastInterval, String.valueOf(i));
                                            mBTXWDevice.setBluetoothBroadcastInterval(new BTXW_Device.SetBluetoothBroadcastIntervalCallback() {
                                                @Override
                                                public void onResult(int status) {
                                                    if (status == 0) {
                                                        setItemViewDetailText(itemSetBluetoothBroadcastInterval, getResources().getString(R.string.toast_command_success));
                                                    } else {
                                                        showFailTip(status);
                                                    }
                                                }
                                            }, (byte)(i / 10));
                                            dialog.dismiss();
                                            return;
                                        }
                                    }
                                    showToast(getResources().getString(R.string.toast_effective_broadcast_interval_range));
                                }
                            }
                        })
                        .create().show();
            }
        }
    };

    private View.OnClickListener onItemLogClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ReportActivity.actionStart(mContext, mBleApplication.getLogString());
        }
    };

    private View.OnClickListener onItemAuthClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (isDeviceConnected()) {
                if (hasAuth) {
                    showInfoTip(getResources().getString(R.string.toast_authenticated));
                    return;
                }
                setItemViewDetailText(itemAuth, "");
                mBTXWDevice.autoAuthenticate(new BTXW_Device.AutoAuthenticateCallback() {
                    @Override
                    public void onResult(int status, byte authResult) {
                        if (status == 0) {
                            setItemViewDetailText(itemAuth, getResources().getString(R.string.toast_command_success));
                            hasAuth = true;
                            updateAuthView();
                        } else {
                            showFailTip(status);
                        }
                    }
                });
            }
        }
    };

    private View.OnClickListener onItemSetTimeClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (isDeviceConnected()) {
                setItemViewDetailText(itemSetTime, "");
                mBTXWDevice.setDeviceTime(new BTXW_Device.SetDeviceTimeCallback() {
                    @Override
                    public void onResult(int status) {
                        if (status == 0) {
                            setItemViewDetailText(itemSetTime, getResources().getString(R.string.toast_command_success));
                        } else {
                            showFailTip(status);
                        }
                    }
                }, System.currentTimeMillis() / 1000);
            }
        }
    };

    private View.OnClickListener onItemSetNameClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (isDeviceConnected()) {
                final QMUIDialog.EditTextDialogBuilder builder = new QMUIDialog.EditTextDialogBuilder(mContext);
                builder.setTitle(getResources().getString(R.string.title_set_name))
                        .setPlaceholder(getResources().getString(R.string.content_input_name_placeholder))
                        .setInputType(InputType.TYPE_CLASS_TEXT)
                        .addAction(getResources().getString(R.string.btn_cancel), new QMUIDialogAction.ActionListener() {
                            @Override
                            public void onClick(QMUIDialog dialog, int index) {
                                dialog.dismiss();
                            }
                        })
                        .addAction(getResources().getString(R.string.btn_confirm), new QMUIDialogAction.ActionListener() {
                            @Override
                            public void onClick(QMUIDialog dialog, int index) {
                                CharSequence name = builder.getEditText().getText();
                                if (!TextUtils.isEmpty(name)) {
                                    setItemViewDetailText(itemSetName, name.toString());
                                    mBTXWDevice.setDeviceName(new BTXW_Device.SetDeviceNameCallback() {
                                        @Override
                                        public void onResult(int status) {
                                            if (status == 0) {
                                                setItemViewDetailText(itemSetName, getResources().getString(R.string.toast_command_success));
                                            } else {
                                                showFailTip(status);
                                            }
                                        }
                                    }, name.toString());
                                    dialog.dismiss();
                                } else {
                                    showInfoTip(getResources().getString(R.string.content_input_name_placeholder));
                                }
                            }
                        })
                        .create().show();
                EditText editText = builder.getEditText();
                if (editText != null) {
                    editText.setFilters(new InputFilter[] {new InputFilter.LengthFilter(17)});
                }
            }
        }
    };

    private View.OnClickListener onItemSetTransmissionPowerClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (isDeviceConnected()) {
                TransmissionPowerActivity.actionStart(mContext, TransmissionPowerActivity.MODE_SET_TRANSMISSION_POWER);
            }
        }
    };

    private View.OnClickListener onItemShutDownClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (isDeviceConnected()) {
                setItemViewDetailText(itemShutDown, "");
                mBTXWDevice.shutDownDevice(new BTXW_Device.ShutDownDeviceCallback() {
                    @Override
                    public void onResult(int status) {
                        if (status == 0) {
                            setItemViewDetailText(itemShutDown, getResources().getString(R.string.toast_command_success));
                        } else {
                            showFailTip(status);
                        }
                    }
                });
            }
        }
    };

    public static void actionStart(Context context) {
        context.startActivity(new Intent(context, SDKMainActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sdk_main);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disconnectBTXWDevice();
    }

    @Override
    public void onDeviceConnect() {
        super.onDeviceConnect();
    }

    @Override
    public void onDeviceDisconnect() {
        super.onDeviceDisconnect();
        hasAuth = false;
        updateAuthView();
    }

    private void initView() {
        String appVersion = String.format("Version: %s%s",
                BuildConfig.DEBUG ? "debug-" : "", BuildConfig.VERSION_NAME);

        itemLighting = listViewFinding.createItemView(getResources().getString(R.string.title_lighting));
        itemLighting.setOrientation(QMUICommonListItemView.HORIZONTAL);

        itemBeeping = listViewFinding.createItemView(getResources().getString(R.string.title_beeping));
        itemBeeping.setOrientation(QMUICommonListItemView.HORIZONTAL);

        itemTwoLighting = listViewFinding.createItemView(getResources().getString(R.string.title_two_lighting));
        itemTwoLighting.setOrientation(QMUICommonListItemView.HORIZONTAL);

        itemMultipleLighting = listViewFinding.createItemView(getResources().getString(R.string.title_multiple_lighting));
        itemMultipleLighting.setOrientation(QMUICommonListItemView.HORIZONTAL);

        itemEffectiveSecond = listViewFinding.createItemView(getResources().getString(R.string.title_effective_second));
        itemEffectiveSecond.setOrientation(QMUICommonListItemView.HORIZONTAL);
        itemEffectiveSecond.setDetailText(String.valueOf(effectiveSeconds));

        QMUIGroupListView.newSection(this)
                .setTitle(appVersion)
                .addItemView(itemLighting, onItemLightingClick)
                .addItemView(itemBeeping, onItemBeepingClick)
                .addItemView(itemTwoLighting, onItemTwoLightingClick)
                .addItemView(itemMultipleLighting, onItemMultipleLightingClick)
                .addItemView(itemEffectiveSecond, onItemEffectiveSecondClick)
                .addTo(listViewFinding);

        itemGetMac = listViewNormal.createItemView(getResources().getString(R.string.title_get_mac));
        itemGetMac.setOrientation(QMUICommonListItemView.HORIZONTAL);

        itemGetVersion = listViewNormal.createItemView(getResources().getString(R.string.title_get_version));
        itemGetVersion.setOrientation(QMUICommonListItemView.HORIZONTAL);

        itemGetPower = listViewNormal.createItemView(getResources().getString(R.string.title_get_power));
        itemGetPower.setOrientation(QMUICommonListItemView.HORIZONTAL);

        itemGetTime = listViewNormal.createItemView(getResources().getString(R.string.title_get_time));
        itemGetTime.setOrientation(QMUICommonListItemView.HORIZONTAL);

        itemGetName = listViewNormal.createItemView(getResources().getString(R.string.title_get_name));
        itemGetName.setOrientation(QMUICommonListItemView.HORIZONTAL);

        itemGetTransmissionPower = listViewNormal.createItemView(getResources().getString(R.string.title_get_transmission_power));
        itemGetTransmissionPower.setOrientation(QMUICommonListItemView.HORIZONTAL);
        itemGetTransmissionPower.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON);

        itemGetBluetoothBroadcastInterval = listViewNormal.createItemView(getResources().getString(R.string.title_get_bluetooth_broadcast_interval));
        itemGetBluetoothBroadcastInterval.setOrientation(QMUICommonListItemView.HORIZONTAL);

        itemSetBluetoothBroadcastInterval = listViewNormal.createItemView(getResources().getString(R.string.title_set_bluetooth_broadcast_interval));
        itemSetBluetoothBroadcastInterval.setOrientation(QMUICommonListItemView.HORIZONTAL);

        itemLog = listViewNormal.createItemView(getResources().getString(R.string.title_log));
        itemLog.setOrientation(QMUICommonListItemView.HORIZONTAL);
        itemLog.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON);
        itemLog.setVisibility(BuildConfig.DEBUG ? View.VISIBLE : View.GONE);

        QMUIGroupListView.newSection(this)
                .setTitle(getResources().getString(R.string.title_normal_section))
                .addItemView(itemGetMac, onItemGetMacClick)
                .addItemView(itemGetVersion, onItemGetVersionClick)
                .addItemView(itemGetPower, onItemGetPowerClick)
                .addItemView(itemGetTime, onItemGetTimeClick)
                .addItemView(itemGetName, onItemGetNameClick)
                .addItemView(itemGetTransmissionPower, onItemGetTransmissionPowerClick)
                .addItemView(itemGetBluetoothBroadcastInterval, onItemGetBluetoothBroadcastInterval)
                .addItemView(itemSetBluetoothBroadcastInterval, onItemSetBluetoothBroadcastInterval)
                .addItemView(itemLog, onItemLogClick)
                .addTo(listViewNormal);

        itemAuth = listViewAuthentication.createItemView(getResources().getString(R.string.title_authenticate));
        itemAuth.setOrientation(QMUICommonListItemView.HORIZONTAL);

        itemSetTime = listViewAuthentication.createItemView(getResources().getString(R.string.title_set_time));
        itemSetTime.setOrientation(QMUICommonListItemView.HORIZONTAL);

        itemSetName = listViewAuthentication.createItemView(getResources().getString(R.string.title_set_name));
        itemSetName.setOrientation(QMUICommonListItemView.HORIZONTAL);

        itemSetTransmissionPower = listViewAuthentication.createItemView(getResources().getString(R.string.title_set_transmission_power));
        itemSetTransmissionPower.setOrientation(QMUICommonListItemView.HORIZONTAL);
        itemSetTransmissionPower.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON);

        itemShutDown = listViewAuthentication.createItemView(getResources().getString(R.string.title_shut_down));
        itemShutDown.setOrientation(QMUICommonListItemView.HORIZONTAL);

        QMUIGroupListView.newSection(this)
                .setTitle(getResources().getString(R.string.title_authenticate_section))
                .addItemView(itemAuth, onItemAuthClick)
                .addItemView(itemSetTime, onItemSetTimeClick)
                .addItemView(itemSetName, onItemSetNameClick)
                .addItemView(itemSetTransmissionPower, onItemSetTransmissionPowerClick)
                .addItemView(itemShutDown, onItemShutDownClick)
                .addTo(listViewAuthentication);

        updateAuthView();
    }

    private void updateAuthView() {
        enableItemView(itemSetTime, hasAuth);
        enableItemView(itemSetName, hasAuth);
        enableItemView(itemSetTransmissionPower, hasAuth);
        enableItemView(itemShutDown, hasAuth);
        if (!hasAuth) {
            itemAuth.setDetailText("");
        }
    }

    private void enableItemView(QMUICommonListItemView itemView, boolean enable) {
        itemView.setEnabled(enable);
        itemView.setDetailText(enable ? "" : getResources().getString(R.string.content_need_auth));
    }

    private void showTwoLightingType(OnSheetClickListener listener) {
        Configs.ConfigItem[] items = Configs.sTwoLightingType;
        final QMUIBottomSheet.BottomListSheetBuilder builder = new QMUIBottomSheet.BottomListSheetBuilder(mContext,
                true);
        for (int i = 0; i < items.length; i ++) {
            builder.addItem(items[i].getDescription());
            if (items[i].getValue() == mTwoLightingType) {
                builder.setCheckedIndex(i);
            }
        }
        builder.setOnSheetItemClickListener(new QMUIBottomSheet.BottomListSheetBuilder.OnSheetItemClickListener() {
            @Override
            public void onClick(QMUIBottomSheet dialog, View itemView, int position, String tag) {
                mTwoLightingType = items[position].getValue();
                dialog.dismiss();
                if (listener != null) {
                    listener.onClick(position);
                }
            }
        });
        builder.build().show();
    }

    private void showMultipleLightingGroup(final OnConfirmClickListener listener) {
        Configs.ConfigItem[] items = Configs.sMultipleLightingGroup;
        int[] checked = getCheckedItemsIndex(items, checkedItems);
        String[] names = Configs.getConfigItemsNames(items);

        final QMUIDialog.MultiCheckableDialogBuilder builder = new QMUIDialog.MultiCheckableDialogBuilder(mContext)
                .setCheckedItems(checked);
        builder.addItems(names, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //第一个值全部，与多选互斥
                BitSet checked = builder.getCheckedItemRecord();
                if (which == 0) {
                    //选第一个值则互斥其余
                    if (checked.get(0)) {
                        checked.clear();
                        checked.set(0);
                    }
                } else {
                    if (checked.get(0)) {
                        //如果已选某个值，则互斥去掉第一个
                        checked.clear(0);
                    }
                }
                builder.setCheckedItems(checked);
                updateMultiCheckableDialog(builder, checked);
            }
        });
        builder.addAction(getResources().getString(R.string.btn_cancel), new QMUIDialogAction.ActionListener() {
            @Override
            public void onClick(QMUIDialog dialog, int index) {
                dialog.dismiss();
            }
        });
        builder.addAction(getResources().getString(R.string.btn_confirm), new QMUIDialogAction.ActionListener() {
            @Override
            public void onClick(QMUIDialog dialog, int index) {
                List<Configs.ConfigItem> list = new ArrayList<>();
                for (int i = 0; i < builder.getCheckedItemIndexes().length; i++) {
                    list.add(items[builder.getCheckedItemIndexes()[i]]);
                }
                checkedItems = list.toArray(new Configs.ConfigItem[0]);
                dialog.dismiss();
                if (listener != null) {
                    listener.onClick();
                }
            }
        });
        builder.create().show();
    }

    private byte[] getCheckedItemsValue(Configs.ConfigItem[] checkedItem) {
        byte[] values = new byte[checkedItem.length];
        for (int i = 0; i < values.length; i ++) {
            values[i] = checkedItem[i].getValue();
        }
        return values;
    }

    private int[] getCheckedItemsIndex(Configs.ConfigItem[] allItems, Configs.ConfigItem[] checkedItem) {
        int[] values = new int[checkedItem.length];
        for (int i = 0; i < values.length; i ++) {
            for (int j = 0; j < allItems.length; j ++) {
                if (checkedItem[i].getValue() == allItems[j].getValue()) {
                    values[i] = j;
                    break;
                }
            }
        }
        return values;
    }
}
