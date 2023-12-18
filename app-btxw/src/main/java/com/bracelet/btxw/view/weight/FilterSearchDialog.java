package com.bracelet.btxw.view.weight;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.bracelet.ble.bt.BleBT;
import com.bracelet.btxw.R;
import com.bracelet.btxw.meta.BleBTFilter;
import com.bracelet.btxw.utils.Configs;

import java.util.ArrayList;
import java.util.List;

public class FilterSearchDialog {

    private Context mContext;
    private Activity mActivity;

    private AlertDialog.Builder mBuilder;
    private AlertDialog mDialog;

    private EditText etFilterByAddress;
    private ImageButton ibScan;
    private ImageButton ibClear;
    private SeekBar sbRssi;
    private TextView tvRssi;
    private CheckBox cbBT05;
    private CheckBox cbBT05L;
    private CheckBox cbBT06AOA;
    private CheckBox cbBT06L;
    private CheckBox cbBT06LAOA;
    private CheckBox cbBT07AOA;
    private CheckBox cbBT11AOA;

    private OnConfirmButtonClickListener mOnConfirmButtonClickListener;
    private OnScanButtonClickListener mOnScanButtonClickListener;
    private OnScanListener mOnScanListener;

    private BleBTFilter mBleBTFilter;
    private final int RSSI_MAX = -40;
    private final int RSSI_MIN = -100;

    public FilterSearchDialog(Activity activity, BleBTFilter filter) {
        mContext = activity;
        mActivity = activity;
        mBuilder = new AlertDialog.Builder(activity);
        mBleBTFilter = filter;
        setDialog();
    }

    public void showDialog() {
        if (!mActivity.isFinishing()) {
            mDialog.show();
        }
    }

    public void dismissDialog() {
        mDialog.dismiss();
    }

    public void setScanResult(String result) {
        etFilterByAddress.setText(result);
        etFilterByAddress.setSelection(result.length());
    }

    private void setDialog() {
        View view = View.inflate(mContext, R.layout.view_filter, null);
        etFilterByAddress = view.findViewById(R.id.etFilterByAddress);
        ibScan = view.findViewById(R.id.ibScan);
        ibClear = view.findViewById(R.id.ibClear);
        sbRssi = view.findViewById(R.id.sbRssi);
        tvRssi = view.findViewById(R.id.tvRssi);

        cbBT05 = view.findViewById(R.id.cbBT05);
        cbBT05L = view.findViewById(R.id.cbBT05L);
        cbBT06AOA = view.findViewById(R.id.cbBT06AOA);
        cbBT06L = view.findViewById(R.id.cbBT06L);
        cbBT06LAOA = view.findViewById(R.id.cbBT06LAOA);
        cbBT07AOA = view.findViewById(R.id.cbBT07AOA);
        cbBT11AOA = view.findViewById(R.id.cbBT11AOA);

        //filter address
        etFilterByAddress.setText(mBleBTFilter.getAddress());

        //filter RSSI
        sbRssi.setMax(RSSI_MAX - RSSI_MIN);
        sbRssi.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvRssi.setText(String.format("%sdBm", RSSI_MAX - progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        sbRssi.setProgress(RSSI_MAX - mBleBTFilter.getRssi());

        //filter BT
        setFilterBT();

        ibScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnScanButtonClickListener != null) {
                    mOnScanButtonClickListener.onClick();
                }
            }
        });

        ibClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etFilterByAddress.setText("");
            }
        });

        mBuilder.setView(view);
        mBuilder.setPositiveButton(mActivity.getResources().getString(R.string.btn_confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismissDialog();
                setBleBTFilter();
                if (mOnConfirmButtonClickListener != null) {
                    mOnConfirmButtonClickListener.onClick(mBleBTFilter);
                }
            }
        });
        mBuilder.setNegativeButton(mActivity.getResources().getString(R.string.btn_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismissDialog();
            }
        });
        mDialog = mBuilder.create();
    }

    private void setFilterBT() {
        Configs.ConfigItem[] configItems = mBleBTFilter.getBleBTItems();
        for (Configs.ConfigItem item : configItems) {
            if (item.getValue() == BleBT.BT05) {
                cbBT05.setChecked(true);
            } else if (item.getValue() == BleBT.BT05L) {
                cbBT05L.setChecked(true);
            } else if (item.getValue() == BleBT.BT06AOA) {
                cbBT06AOA.setChecked(true);
            } else if (item.getValue() == BleBT.BT06L) {
                cbBT06L.setChecked(true);
            } else if (item.getValue() == BleBT.BT06LAOA) {
                cbBT06LAOA.setChecked(true);
            } else if (item.getValue() == BleBT.BT07AOA) {
                cbBT07AOA.setChecked(true);
            } else if (item.getValue() == BleBT.BT11AOA) {
                cbBT11AOA.setChecked(true);
            }
        }
    }

    private void setBleBTFilter() {
        mBleBTFilter.setAddress(etFilterByAddress.getText().toString().replace(":", ""));
        mBleBTFilter.setRssi(RSSI_MAX - sbRssi.getProgress());

        Configs.ConfigItem[] items = Configs.sBleBTGroup;
        List<Configs.ConfigItem> list = new ArrayList<>();
        if (cbBT05.isChecked()) {
            list.add(items[0]);
        }
        if (cbBT05L.isChecked()) {
            list.add(items[1]);
        }
        if (cbBT06AOA.isChecked()) {
            list.add(items[2]);
        }
        if (cbBT06L.isChecked()) {
            list.add(items[3]);
        }
        if (cbBT06LAOA.isChecked()) {
            list.add(items[4]);
        }
        if (cbBT07AOA.isChecked()) {
            list.add(items[5]);
        }
        if (cbBT11AOA.isChecked()) {
            list.add(items[6]);
        }
        mBleBTFilter.setBleBTItems(list.toArray(new Configs.ConfigItem[0]));
    }

    public interface OnConfirmButtonClickListener {
        void onClick(BleBTFilter filter);
    }

    public void setOnConfirmButtonClickListener(OnConfirmButtonClickListener listener) {
        mOnConfirmButtonClickListener = listener;
    }

    public interface OnScanButtonClickListener {
        void onClick();
    }

    public void setOnScanButtonClickListener(OnScanButtonClickListener listener) {
        mOnScanButtonClickListener = listener;
    }

    public interface OnScanListener {
        void onScan(String result);
    }

    public void setOnScanListener(OnScanListener listener) {
        mOnScanListener = listener;
    }
}
