package com.bracelet.btxw.meta;

import androidx.annotation.Nullable;

import com.bracelet.ble.bt.BleBT;
import com.bracelet.ble.bt.BleBT05;
import com.bracelet.ble.bt.BleBT05L;
import com.bracelet.ble.bt.BleBT06AOA;
import com.bracelet.ble.bt.BleBT06L;
import com.bracelet.ble.bt.BleBT06LAOA;
import com.bracelet.ble.bt.BleBT07AOA;
import com.bracelet.ble.bt.BleBT11AOA;
import com.bracelet.btxw.R;
import com.bracelet.btxw.utils.TextFormatUtils;

public class BleBTShow {
    private BleBT mBleBT;

    private long interval;
    private String dataText;

    public BleBTShow(BleBT bleBT) {
        mBleBT = bleBT;
        dataText = getDataText(bleBT);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof BleBTShow) {
            return ((BleBTShow)obj).mBleBT.equals(mBleBT);
        }
        return super.equals(obj);
    }

    public BleBT getBleBT() {
        return mBleBT;
    }

    public long getInterval() {
        return interval;
    }

    public String getDataText() {
        return dataText;
    }

    public int getRssi(){
        return mBleBT.getRssi();
    }

    public void updateBleBt(BleBT bleBT) {
        interval = bleBT.getSearedTimestamp() - mBleBT.getSearedTimestamp();
        mBleBT = bleBT;
        dataText = getDataText(bleBT);
    }

    public int getColor(BleBT bleBT) {
        int color = R.color.app_color_theme_1;
        if (bleBT instanceof BleBT05) {
            color = R.color.app_color_theme_3;
        } else if (bleBT instanceof BleBT05L) {
            color = R.color.app_color_theme_4;
        } else if (bleBT instanceof BleBT06AOA) {
            color = R.color.app_color_theme_5;
        } else if (bleBT instanceof BleBT06L) {
            color = R.color.app_color_theme_6;
        } else if (bleBT instanceof BleBT06LAOA) {
            color = R.color.app_color_theme_7;
        } else if (bleBT instanceof BleBT07AOA) {
            color = R.color.app_color_theme_8;
        } else if (bleBT instanceof BleBT11AOA) {
            color = R.color.app_color_theme_9;
        }
        return color;
    }

    private String getDataText(BleBT bleBT) {
        String dataText = TextFormatUtils.DEFAULT_TEXT;
        if (bleBT instanceof BleBT05) {
            dataText = TextFormatUtils.getBT05DataText((BleBT05)bleBT);
        } else if (bleBT instanceof BleBT05L) {
            dataText = TextFormatUtils.getBT05LDataText((BleBT05L)bleBT);
        } else if (bleBT instanceof BleBT06AOA) {
            dataText = TextFormatUtils.getBT06AOADataText((BleBT06AOA)bleBT);
        } else if (bleBT instanceof BleBT06L) {
            dataText = TextFormatUtils.getBT06LDataText((BleBT06L)bleBT);
        } else if (bleBT instanceof BleBT06LAOA) {
            dataText = TextFormatUtils.getBT06LAOADataText((BleBT06LAOA)bleBT);
        } else if (bleBT instanceof BleBT07AOA) {
            dataText = TextFormatUtils.getBT07AOADataText((BleBT07AOA)bleBT);
        } else if (bleBT instanceof BleBT11AOA) {
            dataText = TextFormatUtils.getBT11AOADataText((BleBT11AOA)bleBT);
        }
        return dataText;
    }
}
