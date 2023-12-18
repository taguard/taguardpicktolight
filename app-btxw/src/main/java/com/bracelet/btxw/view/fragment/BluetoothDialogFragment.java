package com.bracelet.btxw.view.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bracelet.ble.btxw.BTXW_Device;
import com.bracelet.ble.btxw.BTXW_Service;
import com.bracelet.ble.btxw.BTXW_ServiceImpl;
import com.bracelet.btxw.R;
import com.bracelet.btxw.view.adapter.BluetoothDeviceAdapter;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;

import java.lang.reflect.Field;

public class BluetoothDialogFragment extends DialogFragment {

    private static final int ScanSecond = 60;

    private BTXW_Service mBTXWService;
    private SwipeRefreshLayout srLayout;
    private BluetoothDeviceAdapter mAdapter = new BluetoothDeviceAdapter();

    private OnConnectLastClickListener mOnConnectLastClickListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_blue_scan, null);

        RecyclerView rvBluetooth = view.findViewById(R.id.rvBluetooth);
        rvBluetooth.setAdapter(mAdapter);
        rvBluetooth.setLayoutManager(new LinearLayoutManager(getContext()));

        QMUIRoundButton btnConnectLast = view.findViewById(R.id.btnConnectLast);
        btnConnectLast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnConnectLastClickListener != null) {
                    mOnConnectLastClickListener.onClick();
                }
            }
        });

        srLayout = view.findViewById(R.id.srScan);
        srLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                startScan();
            }
        });
        srLayout.setColorSchemeResources(R.color.colorPrimary);

        try {
            mBTXWService =  new BTXW_ServiceImpl(getContext(), new BTXW_Service.SearchCallback() {
                @Override
                public void onDiscoverBleDevice(BTXW_Device device) {
                    Log.i("debugLog", String.format("device [%s][%s]", device.getName(), device.getRssi()));
                    mAdapter.addDevice(device);
                }
            });
            startScan();
        }catch (Exception e) {
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getResources().getString(R.string.title_device_list));
        builder.setView(view);
        return builder.create();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mBTXWService != null) {
            mBTXWService.stopScan();
        }
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        try{
            Class<?> c = Class.forName("android.support.v4.app.DialogFragment");
            Field dismissed = c.getDeclaredField("mDismissed");
            dismissed.setAccessible(true);
            dismissed.set(this, false);
            Field shownByMe = c.getDeclaredField("mShownByMe");
            shownByMe.setAccessible(true);
            shownByMe.set(this, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        FragmentTransaction ft = manager.beginTransaction();
        ft.add(this, tag);
        ft.commitAllowingStateLoss();
    }

    public void setOnAdapterItemClickListener(BluetoothDeviceAdapter.OnAdapterItemClickListener listener) {
        mAdapter.setOnAdapterItemClickListener(listener);
    }

    public interface OnConnectLastClickListener {
        void onClick();
    }

    public void setOnConnectLastClickListener(OnConnectLastClickListener listener) {
        mOnConnectLastClickListener = listener;
    }

    private void startScan() {
        mAdapter.clearDevices();
        mBTXWService.startScan();
        srLayout.setRefreshing(true);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mBTXWService.stopScan();
                srLayout.setRefreshing(false);
            }
        }, 1000 * ScanSecond);
    }

}
