package com.bracelet.btxw.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bracelet.ble.btxw.BTXW_Device;
import com.bracelet.btxw.R;
import com.bracelet.btxw.view.BleApplication;
import com.bracelet.btxw.view.activity.BaseActivity;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;

public class BaseFragment extends Fragment {

    protected BaseActivity mBaseActivity;
    protected BTXW_Device mBTXWDevice;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBaseActivity = (BaseActivity) getActivity();
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public void onDeviceConnect() {
        if (mBaseActivity != null) {
            mBTXWDevice = mBaseActivity.getBTXWDevice();
        }
    }

    public void onDeviceDisconnect() {

    }

    public void disconnectBTXWDevice() {
        if (mBaseActivity != null) {
            mBTXWDevice = mBaseActivity.getBTXWDevice();
        }
        if (mBTXWDevice != null && (mBTXWDevice.isConnected() || mBTXWDevice.isConnecting())) {
            mBTXWDevice.disconnect();
        }
    }

    public void onScanCodeResult(String code) {

    }

    protected void showToast(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }

}
