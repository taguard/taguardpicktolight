package com.bracelet.btxw.view.activity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bracelet.ble.btxw.BTXW_Device;
import com.bracelet.ble.btxw.BTXW_ServiceImpl;
import com.bracelet.ble.utils.DebugLog;
import com.bracelet.ble.BleException;
import com.bracelet.btxw.BuildConfig;
import com.bracelet.btxw.R;
import com.bracelet.btxw.utils.ActivityCollector;
import com.bracelet.btxw.utils.Configs;
import com.bracelet.btxw.utils.SharedPreferencesUtils;
import com.bracelet.btxw.view.BleApplication;
import com.bracelet.btxw.view.adapter.BluetoothDeviceAdapter;
import com.bracelet.btxw.view.fragment.BluetoothDialogFragment;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogMenuItemView;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import com.qmuiteam.qmui.widget.grouplist.QMUICommonListItemView;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class BaseActivity extends AppCompatActivity {
    private static final int REQUEST_BLE_PERMISSION = 0x100;
    private static final int REQUEST_CAMERA_PERMISSION = 0x101;
    private static final int REQUEST_CODE = 0x200;
    protected Context mContext;
    protected BleApplication mBleApplication;
    private Handler mHandler;
    protected BTXW_Device mBTXWDevice;
    private BluetoothDialogFragment mBluetoothDialogFragment;
    private LocalBroadcastManager mLocalBroadcastManager;
    private LogLocalReceiver mLogLocalReceiver;
    protected boolean useBle;
    private BluetoothReceiver bluetoothReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCollector.addActivity(this);
        initData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (BuildConfig.DEBUG) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(DebugLog.BROADCAST_SEND);
            intentFilter.addAction(DebugLog.BROADCAST_RECEIVE);
            intentFilter.addAction(DebugLog.BROADCAST_CONNECT);
            intentFilter.addAction(DebugLog.BROADCAST_DISCONNECT);
            mLocalBroadcastManager.registerReceiver(mLogLocalReceiver, intentFilter);
        }
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(bluetoothReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (BuildConfig.DEBUG) {
            mLocalBroadcastManager.unregisterReceiver(mLogLocalReceiver);
        }
        unregisterReceiver(bluetoothReceiver);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_BLE_PERMISSION:
                if (grantResults.length > 0) {
                    boolean reject = false;
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            reject = true;
                            break;
                        }
                    }
                    if (reject) {
                        showToast(getResources().getString(R.string.toast_permission_denied));
                    } else {
                        onConnectItemClick();
                    }
                } else {
                    showToast(getResources().getString(R.string.toast_permission_error));
                }
                break;
            case REQUEST_CAMERA_PERMISSION:
                if (grantResults.length > 0) {
                    boolean reject = false;
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            reject = true;
                            break;
                        }
                    }
                    if (reject) {
                        showToast(getResources().getString(R.string.toast_permission_denied));
                    } else {
                        toScanCode();
                    }
                } else {
                    showToast(getResources().getString(R.string.toast_permission_error));
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (data != null) {
                String barcode = data.getStringExtra(LivePreviewActivity.INTENT_EXTRA_KEY_SCAN_RESULT);
                onScanCodeResult(barcode);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        if (useBle) {
            getMenuInflater().inflate(R.menu.menu_multiple_operation, menu);
            MenuItem connectItem = menu.findItem(R.id.connect);
            connectItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if (checkBlePermissions()) {
                        onConnectItemClick();
                    }
                    return true;
                }
            });
            if (mBTXWDevice != null) {
                if (mBTXWDevice.isConnecting()) {
                    connectItem.setActionView(R.layout.view_progress);
                } else {
                    MenuItemCompat.setIconTintList(connectItem, ColorStateList.valueOf(getResources()
                            .getColor(mBTXWDevice.isConnected() ? R.color.colorAccent : android.R.color.white)));
                }
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    public void onBluetoothOpen() {

    }

    public void onBluetoothClose() {

    }

    public void onDeviceConnect() {
        mBTXWDevice = mBleApplication.getBTXWDevice();
    }

    public void onDeviceDisconnect() {

    }

    public boolean checkBlePermissions() {
        boolean hasPermission = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> permissions = new ArrayList<>();
            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            }
            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
            if (permissions.size() > 0) {
                String[] request = permissions.toArray(new String[0]);
                ActivityCompat.requestPermissions(BaseActivity.this, request,
                        REQUEST_BLE_PERMISSION);
                hasPermission = false;
            }
        }
        return hasPermission;
    }

    public boolean checkCameraPermissions() {
        boolean hasPermission = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> permissions = new ArrayList<>();
            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.CAMERA);
            }
            if (permissions.size() > 0) {
                String[] request = permissions.toArray(new String[0]);
                ActivityCompat.requestPermissions(BaseActivity.this, request,
                        REQUEST_CAMERA_PERMISSION);
                hasPermission = false;
            }
        }
        return hasPermission;
    }

    public void toScanCode() {
        if (checkCameraPermissions()) {
            LivePreviewActivity.actionStartForResult(this, REQUEST_CODE);
        }
    }

    public void onScanCodeResult(String code) {

    }

    public void openBleFragment(final BluetoothDeviceAdapter.OnAdapterItemClickListener listener) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!isGPSOpen()) {
                showOpenGPSDialog();
                return;
            }
        }
        if (mBluetoothDialogFragment == null) {
            mBluetoothDialogFragment = new BluetoothDialogFragment();
            mBluetoothDialogFragment.setOnAdapterItemClickListener(new BluetoothDeviceAdapter.OnAdapterItemClickListener() {
                @Override
                public void onClick(BTXW_Device device) {
                    mBTXWDevice = device;
                    connectBTXWDevice();
                    mBluetoothDialogFragment.dismiss();
                    if (listener != null) {
                        listener.onClick(mBTXWDevice);
                    }
                }
            });
            mBluetoothDialogFragment.setOnConnectLastClickListener(new BluetoothDialogFragment.OnConnectLastClickListener() {
                @Override
                public void onClick() {
                    connectLastBTXWDevice();
                }
            });
        }
        if (mBluetoothDialogFragment.isAdded()) {
            return;
        }
        mBluetoothDialogFragment.show(getSupportFragmentManager() ,"bluetooth");
    }

    public void onConnectItemClick() {
        if (mBTXWDevice != null && mBTXWDevice.isConnected()) {
            new QMUIDialog.MessageDialogBuilder(mContext)
                    .setTitle(getResources().getString(R.string.title_prompt))
                    .setMessage(getResources().getString(R.string.content_confirm_disconnect))
                    .addAction(getResources().getString(R.string.btn_cancel), new QMUIDialogAction.ActionListener() {
                        @Override
                        public void onClick(QMUIDialog dialog, int index) {
                            dialog.dismiss();
                        }
                    })
                    .addAction(0, getResources().getString(R.string.btn_confirm),
                            QMUIDialogAction.ACTION_PROP_NEGATIVE, new QMUIDialogAction.ActionListener() {
                                @Override
                                public void onClick(QMUIDialog dialog, int index) {
                                    mBTXWDevice.disconnect();
                                    dialog.dismiss();
                                }
                            })
                    .create().show();
        } else {
            toConnectBTXW();
        }
    }

    public void toConnectBTXW() {
        openBleFragment(null);
    }

    public BTXW_Device getBTXWDevice() {
        return mBTXWDevice;
    }

    public void connectBTXWDevice() {
        ActivityCollector.invalidateOptionsMenu();
        mBTXWDevice.connect(mContext, new BTXW_Device.ConnectionCallback() {
            @Override
            public void onConnect() {
                SharedPreferencesUtils.setDeviceMac(mContext, mBTXWDevice.getAddress());
                mBleApplication.setBTXWDevice(mBTXWDevice);
                showToast(getResources().getString(R.string.toast_ble_connect));
                ActivityCollector.invalidateOptionsMenu();
                ActivityCollector.onDeviceConnect();
            }

            @Override
            public void onDisconnect() {
                showToast(getResources().getString(R.string.toast_ble_disconnect));
                ActivityCollector.invalidateOptionsMenu();
                ActivityCollector.onDeviceDisconnect();
            }
        });
    }

    public void disconnectBTXWDevice() {
        if (mBTXWDevice != null && (mBTXWDevice.isConnected() || mBTXWDevice.isConnecting())) {
            mBTXWDevice.disconnect();
        }
    }

    protected void onReceiveLog(Intent intent) {
        if (intent.getAction() != null) {
            switch (intent.getAction()) {
                case DebugLog.BROADCAST_SEND:
                case DebugLog.BROADCAST_RECEIVE:
                case DebugLog.BROADCAST_CONNECT:
                case DebugLog.BROADCAST_DISCONNECT:
                    mBleApplication.addLogString(intent.getStringExtra(DebugLog.EXTRA_LOG));
                    break;
            }
        }
    }

    protected void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    protected void setToolbarTitle(String title) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initData() {
        mContext = this;
        mBleApplication = (BleApplication)getApplication();
        mHandler = new Handler(Looper.getMainLooper());
        mBTXWDevice = mBleApplication.getBTXWDevice();
        useBle = true;
        if (BuildConfig.DEBUG) {
            mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
            mLogLocalReceiver = new LogLocalReceiver();
        }

        bluetoothReceiver = new BluetoothReceiver();
    }

    public boolean isDeviceConnected() {
        boolean connected = false;
        if (mBTXWDevice != null && mBTXWDevice.isConnected()) {
            connected = true;
        } else {
            showInfoTip(getResources().getString(R.string.toast_to_connect));
        }
        return connected;
    }

    protected void setItemViewDetailText(QMUICommonListItemView itemView, String text) {
        itemView.setDetailText(text);
    }

    protected void updateMultiCheckableDialog(QMUIDialog.MultiCheckableDialogBuilder builder, BitSet checked) {
        try {
            Class<? extends QMUIDialog.MultiCheckableDialogBuilder> builderClass = builder.getClass();
            ParameterizedType baseClassType = (ParameterizedType)builderClass.getGenericSuperclass();
            Class baseClass = (Class) baseClassType.getRawType();   //获取不带泛型的父类
            Field mMenuItemViews = baseClass.getDeclaredField("mMenuItemViews");
            mMenuItemViews.setAccessible(true);
            ArrayList<QMUIDialogMenuItemView> items = (ArrayList<QMUIDialogMenuItemView>) mMenuItemViews.get(builder);
            for (int i = 0; i < items.size(); i++) {
                QMUIDialogMenuItemView itemView = items.get(i);
                itemView.setChecked(checked.get(i));
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    protected void showInfoTip(String info) {
        final QMUITipDialog tipDialog = new QMUITipDialog.Builder(this)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_INFO)
                .setTipWord(info)
                .create();
        if (!isFinishing()) {
            tipDialog.show();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    tipDialog.dismiss();
                }
            }, 1500);
        }
    }

    protected void showSuccessTip() {
        final QMUITipDialog tipDialog = new QMUITipDialog.Builder(this)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_SUCCESS)
                .setTipWord(getResources().getString(R.string.toast_command_success))
                .create();
        if (!isFinishing()) {
            tipDialog.show();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    tipDialog.dismiss();
                }
            }, 1500);
        }
    }

    protected void showFailTip(int status) {
        final QMUITipDialog tipDialog = new QMUITipDialog.Builder(this)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
                .setTipWord(String.format(getResources().getString(R.string.toast_command_failed), status))
                .create();
        if (!isFinishing()) {
            tipDialog.show();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    tipDialog.dismiss();
                }
            }, 1500);
        }
    }

    private void connectLastBTXWDevice() {
        String address = SharedPreferencesUtils.getDeviceMac(mContext);
        if (!TextUtils.isEmpty(address)) {
            try {
                mBTXWDevice = new BTXW_ServiceImpl(mContext, null).obtainBTXW_Device(mContext, address);
                connectBTXWDevice();
            } catch (BleException e) {
                showToast(e.getMessage());
            }
            mBluetoothDialogFragment.dismiss();
        } else {
            showToast(getResources().getString(R.string.toast_ble_none));
        }
    }

    protected boolean checkBleSwitch() {
        BluetoothManager manager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        if (manager != null) {
            BluetoothAdapter bluetoothAdapter = manager.getAdapter();
            if (bluetoothAdapter != null) {
                if (!bluetoothAdapter.isEnabled()) {
                    bluetoothAdapter.enable();
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean isGPSOpen() {
        boolean isOpen = false;
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            isOpen = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }
        return isOpen;
    }

    protected void showOpenGPSDialog() {
        new QMUIDialog.MessageDialogBuilder(mContext)
                .setTitle(getResources().getString(R.string.title_prompt))
                .setMessage(getResources().getString(R.string.toast_open_gps))
                .addAction(getResources().getString(R.string.btn_cancel), new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                })
                .addAction(getResources().getString(R.string.btn_confirm), new QMUIDialogAction.ActionListener() {
                        @Override
                        public void onClick(QMUIDialog dialog, int index) {
                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                            dialog.dismiss();
                        }
                    })
                .create().show();
    }

    public void showLightingSettingGroup() {
        int checkedIndex = 0;
        String lightSetting = SharedPreferencesUtils.getLightSetting(mContext);
        if (TextUtils.equals(lightSetting, Configs.sTagItems[0])) {
            checkedIndex = 0;
        } else if (TextUtils.equals(lightSetting, Configs.sTagItems[1])) {
            checkedIndex = 1;
        } else if (TextUtils.equals(lightSetting, Configs.sTagItems[2])) {
            checkedIndex = 2;
        }
        new QMUIDialog.CheckableDialogBuilder(mContext)
                .setCheckedIndex(checkedIndex)
                .addItems(Configs.sTagItems, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferencesUtils.setLightSetting(mContext, Configs.sTagItems[which]);
                        showToast(String.format(getString(R.string.toast_choose_tag_type), Configs.sTagItems[which]));
                        dialog.dismiss();
                    }
                })
                .create(com.qmuiteam.qmui.R.style.QMUI_Dialog).show();
    }

    class LogLocalReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            onReceiveLog(intent);
        }
    }

    class BluetoothReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                switch (state) {
                    case BluetoothAdapter.STATE_ON:
                        onBluetoothOpen();
                        break;
                    case BluetoothAdapter.STATE_OFF:
                        onBluetoothClose();
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        break;
                    default:
                        break;
                }
            }
        }
    }
}
