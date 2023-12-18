package com.bracelet.btxw.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bracelet.ble.BleService;
import com.bracelet.ble.bt.BleBT;
import com.bracelet.ble.bt.BleBT_ServiceImpl;
import com.bracelet.btxw.R;
import com.bracelet.btxw.meta.BleBTFilter;
import com.bracelet.btxw.utils.Configs;
import com.bracelet.btxw.view.adapter.BTBroadcastAdapter;
import com.bracelet.btxw.view.weight.FilterSearchDialog;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BroadcastResultActivity extends BaseActivity {
    @BindView(R.id.tvFilter)
    TextView tvFilter;
    @BindView(R.id.srScan)
    SwipeRefreshLayout srScan;
    @BindView(R.id.rvBleBTDevices)
    RecyclerView rvBleBTDevices;

    private Handler mHandler;
    private BleService mBleService;
    private boolean searching;
    private boolean requestSearch;
    private boolean clearing;

    private BTBroadcastAdapter mBTBroadcastAdapter;

    private BleBTFilter mBleBTFilter;
    private FilterSearchDialog mFilterSearchDialog;

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            mBTBroadcastAdapter.orderByRssi();
            mHandler.postDelayed(this,1000);
        }
    };

    public static void actionStart(Context context) {
        context.startActivity(new Intent(context, BroadcastResultActivity.class));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_broadcast_result);
        ButterKnife.bind(this);
        initData();
        initView();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (searching) {
            stopScan();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_scan_operation, menu);
        MenuItem scanItem = menu.findItem(R.id.scan);
        scanItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (TextUtils.equals(item.getTitle(), getResources().getString(R.string.menu_scan))) {
                    checkAndStartScan();
                } else {
                    stopScan();
                }
                return true;
            }
        });
        scanItem.setTitle(searching ? getResources().getString(R.string.menu_stop_scan) : getResources().getString(R.string.menu_scan));
        MenuItem filterItem = menu.findItem(R.id.filter);
        filterItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                showFilterSearchDialog();
                return true;
            }
        });
        return true;
    }

    @Override
    public void onBluetoothOpen() {
        super.onBluetoothOpen();
        if (requestSearch) {
            startScan();
            requestSearch = false;
        }
    }

    @Override
    public void onBluetoothClose() {
        super.onBluetoothClose();
    }

    @Override
    public void onScanCodeResult(String code) {
        super.onScanCodeResult(code);
        if (mFilterSearchDialog != null) {
            mFilterSearchDialog.setScanResult(code);
        }
    }

    @Override
    public void onConnectItemClick() {
        if (!searching) {
            checkAndStartScan();
        }
    }

    private void initData() {
        mBTBroadcastAdapter = new BTBroadcastAdapter(this);
        mBleBTFilter = new BleBTFilter();
        mHandler = new Handler(Looper.getMainLooper());
    }

    private void initView() {
        rvBleBTDevices.setAdapter(mBTBroadcastAdapter);
        rvBleBTDevices.setLayoutManager(new LinearLayoutManager(this));

        srScan.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                clearing = true;
                mBTBroadcastAdapter.clearDevices();
                clearing = false;
                srScan.setRefreshing(false);
                if (!searching) {
                    checkAndStartScan();
                }
            }
        });
        srScan.setColorSchemeResources(R.color.colorPrimary);

        showFilterCondition(mBleBTFilter);
        initFilterDialog();
    }

    private void initFilterDialog() {
        mFilterSearchDialog = new FilterSearchDialog(BroadcastResultActivity.this, mBleBTFilter);
        mFilterSearchDialog.setOnConfirmButtonClickListener(new FilterSearchDialog.OnConfirmButtonClickListener() {
            @Override
            public void onClick(BleBTFilter filter) {
                mBleBTFilter = filter;
                clearing = true;
                mBTBroadcastAdapter.filterClearDevices(filter.getRssi(),filter.getAddress(),filter.getBleBTItems());
                clearing = false;
                showFilterCondition(mBleBTFilter);

                if (!searching) {
                    checkAndStartScan();
                }
            }
        });
        mFilterSearchDialog.setOnScanButtonClickListener(new FilterSearchDialog.OnScanButtonClickListener() {
            @Override
            public void onClick() {
                toScanCode();
            }
        });
    }

    private void checkAndStartScan() {
        if (checkBlePermissions()) {
            if (checkBleSwitch()) {
                startScan();
            } else {
                requestSearch = true;
            }
        }
    }

    private void startScan() {

        mHandler.postDelayed(runnable,1000);
        if (mBleService == null) {
            try {
                mBleService = new BleBT_ServiceImpl(this, new BleService.SearchBTBroadcastCallback() {
                    @Override
                    public void onDiscoverBleBT(BleBT bleBT) {
                        if (clearing) {
                            return;
                        }
                        //filter Rssi
                        int filterRssi = mBleBTFilter.getRssi();
                        if (bleBT.getRssi() < filterRssi) {
                            return;
                        }

                        //filter address
                        String filterAddress = mBleBTFilter.getAddress();
                        if (!TextUtils.isEmpty(filterAddress)) {
                            if (!bleBT.getAddress().replace(":", "").contains(filterAddress)) {
                                return;
                            }
                        }

                        //filter BT
                        boolean filterBT = false;
                        Configs.ConfigItem[] filterBTItems = mBleBTFilter.getBleBTItems();
                        for (Configs.ConfigItem item : filterBTItems) {
                            if (TextUtils.equals(item.getDescription(), bleBT.getTypeName())) {
                                filterBT = true;
                                break;
                            }
                        }
                        if (!filterBT) {
                            return;
                        }

                        mBTBroadcastAdapter.addDevice(bleBT);
                    }
                });
            } catch (Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
        mBTBroadcastAdapter.clearDevices();
        searching = true;
        invalidateOptionsMenu();

        mBleService.startScanForBTBroadcast();
    }

    private void stopScan() {
        mBleService.stopScanForBTBroadcast();
        mHandler.removeCallbacks(runnable);

        searching = false;
        invalidateOptionsMenu();
    }

    private void showFilterSearchDialog() {
        if (mFilterSearchDialog == null) {
            initFilterDialog();
        }
        mFilterSearchDialog.showDialog();
    }

    private void showFilterCondition(BleBTFilter filter) {
        StringBuilder sb = new StringBuilder();
        if (!TextUtils.isEmpty(filter.getAddress())) {
            sb.append(String.format("%s", filter.getAddress()));
        }
        if (sb.length() > 0) {
            sb.append(",");
        }
        sb.append(String.format(">=%sdBm", filter.getRssi()));
        if (filter.getBleBTItems().length > 0) {
            for (Configs.ConfigItem item : filter.getBleBTItems()) {
                if (sb.length() > 0) {
                    sb.append(",");
                }
                sb.append(String.format("%s", item.getDescription()));
            }
        }
        tvFilter.setText(sb.toString());
    }
}
