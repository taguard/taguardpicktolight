package com.bracelet.btxw.utils;

import android.content.Context;
import android.text.TextUtils;

import com.bracelet.ble.bt.BleBT05;
import com.bracelet.ble.bt.BleBT05L;
import com.bracelet.ble.bt.BleBT06AOA;
import com.bracelet.ble.bt.BleBT06L;
import com.bracelet.ble.bt.BleBT06LAOA;
import com.bracelet.ble.bt.BleBT07AOA;
import com.bracelet.ble.bt.BleBT11AOA;
import com.bracelet.btxw.R;
import com.bracelet.btxw.view.BleApplication;

public class TextFormatUtils {
    public static String formatString(String s) {
        return TextUtils.isEmpty(s) ? "" : s;
    }

    public static final String DEFAULT_TEXT = "N/A";

    public static String getBT05DataText(BleBT05 ble) {
        if (ble != null) {
            Context context = BleApplication.getContext();
            return String.format("%s次  %s℃  %smV",
                    ble.getVibrated(),
                    ble.getTemperature(),
                    ble.getVoltage());
        }
        return DEFAULT_TEXT;
    }

    public static String getBT05LDataText(BleBT05L ble) {
        if (ble != null) {
            Context context = BleApplication.getContext();
            return String.format("%s  %sV  %s℃(内)  %s  %s℃(外)",
                    ble.getBleName(),
                    ble.getVoltage(),
                    ble.getInnerTemperature(),
                    ble.isButtonPressed() ? context.getResources().getString(R.string.bt_pressed) : context.getResources().getString(R.string.bt_not_pressed),
                    ble.getOuterTemperature());
        }
        return DEFAULT_TEXT;
    }

    public static String getBT06AOADataText(BleBT06AOA ble) {
        if (ble != null) {
            Context context = BleApplication.getContext();
            if (ble.isMovingStatus()) {
                return String.format("%s  X:%s  Y:%s  Z:%s",
                        context.getResources().getString(R.string.bt_moving),
                        ble.getAccelerationX(),
                        ble.getAccelerationY(),
                        ble.getAccelerationZ());
            } else {
                return String.format("%s  %s  V:%s  %smV",
                        context.getResources().getString(R.string.bt_resting),
                        ble.isTampered() ? context.getResources().getString(R.string.bt_tampered) : context.getResources().getString(R.string.bt_not_tampered),
                        ble.getVersion(),
                        ble.getVoltage());
            }
        }
        return DEFAULT_TEXT;
    }

    public static String getBT06LDataText(BleBT06L ble) {
        if (ble != null) {
            Context context = BleApplication.getContext();
            return String.format("%s  %sV  %s℃  %s",
                    ble.getBleName(),
                    ble.getVoltage(),
                    ble.getTemperature(),
                    ble.isButtonPressed() ? context.getResources().getString(R.string.bt_pressed) : context.getResources().getString(R.string.bt_not_pressed));
        }
        return DEFAULT_TEXT;
    }

    public static String getBT06LAOADataText(BleBT06LAOA ble) {
        if (ble != null) {
            return DEFAULT_TEXT;
        }
        return DEFAULT_TEXT;
    }

    public static String getBT07AOADataText(BleBT07AOA ble) {
        if (ble != null) {
            Context context = BleApplication.getContext();
            return String.format("%s  V:%s  %smV",
                    ble.isButtonPressed() ? context.getResources().getString(R.string.bt_pressed) : context.getResources().getString(R.string.bt_not_pressed),
                    ble.getVersion(),
                    ble.getVoltage());
        }
        return DEFAULT_TEXT;
    }

    public static String getBT11AOADataText(BleBT11AOA ble) {
        if (ble != null) {
            return DEFAULT_TEXT;
        }
        return DEFAULT_TEXT;
    }
}
