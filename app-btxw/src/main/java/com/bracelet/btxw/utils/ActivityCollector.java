package com.bracelet.btxw.utils;

import com.bracelet.btxw.view.activity.BaseActivity;

import java.util.ArrayList;
import java.util.List;

public class ActivityCollector {
    private static List<BaseActivity> sActivities = new ArrayList<>();

    public static void addActivity(BaseActivity activity){
        sActivities.add(activity);
    }

    public static void removeActivity(BaseActivity activity){
        sActivities.remove(activity);
    }

    public static void invalidateOptionsMenu(){
        for (BaseActivity activity: sActivities){
            activity.invalidateOptionsMenu();
        }
    }

    public static void onDeviceConnect(){
        for (BaseActivity activity : sActivities){
            activity.onDeviceConnect();
        }
    }

    public static void onDeviceDisconnect(){
        for (BaseActivity activity : sActivities){
            activity.onDeviceDisconnect();
        }
    }
}
