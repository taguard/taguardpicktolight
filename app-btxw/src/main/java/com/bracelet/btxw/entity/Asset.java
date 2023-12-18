package com.bracelet.btxw.entity;

import org.litepal.crud.LitePalSupport;

import java.io.Serializable;

public class Asset extends LitePalSupport implements Serializable {
    private String assetName;
    private String assetNo;
    private String tagName;
    private String tagMac;

    public Asset() {
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public String getAssetNo() {
        return assetNo;
    }

    public void setAssetNo(String assetNo) {
        this.assetNo = assetNo;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getTagMac() {
        return tagMac;
    }

    public void setTagMac(String tagMac) {
        this.tagMac = tagMac;
    }

    //get key id in database
    public long getKeyId() {
        return getBaseObjId();
    }
}
