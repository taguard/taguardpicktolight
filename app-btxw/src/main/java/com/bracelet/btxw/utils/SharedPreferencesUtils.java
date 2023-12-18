package com.bracelet.btxw.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesUtils {
    private final static String SP_DATA_FILE_NAME = "data";
    private final static String SP_KEY_MAC = "mac";
    private final static String SP_KEY_LIGHT_TYPE = "lightType";

    public static String getDeviceMac(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SP_DATA_FILE_NAME, Context.MODE_PRIVATE);
        return sp.getString(SP_KEY_MAC, "");
    }

    public static void setDeviceMac(Context context, String mac) {
        SharedPreferences.Editor editor = context.getSharedPreferences(SP_DATA_FILE_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(SP_KEY_MAC, mac);
        editor.apply();
    }

    public static String getLightSetting(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SP_DATA_FILE_NAME, Context.MODE_PRIVATE);
        return sp.getString(SP_KEY_LIGHT_TYPE, "BT11");
    }

    public static void setLightSetting(Context context, String type) {
        SharedPreferences.Editor editor = context.getSharedPreferences(SP_DATA_FILE_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(SP_KEY_LIGHT_TYPE, type);
        editor.apply();
    }
}
