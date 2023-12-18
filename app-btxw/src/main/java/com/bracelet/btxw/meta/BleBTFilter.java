package com.bracelet.btxw.meta;

import com.bracelet.btxw.utils.Configs;

public class BleBTFilter {
    private String address;
    private int rssi;
    private Configs.ConfigItem[] bleBTItems;

    public BleBTFilter() {
        this.rssi = -70;
        this.bleBTItems = Configs.sBleBTGroup;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public Configs.ConfigItem[] getBleBTItems() {
        return bleBTItems;
    }

    public void setBleBTItems(Configs.ConfigItem[] bleBTItems) {
        this.bleBTItems = bleBTItems;
    }
}
