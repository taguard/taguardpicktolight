package com.bracelet.btxw.utils;

import android.content.res.Resources;

import com.bracelet.ble.bt.BleBT;
import com.bracelet.ble.btxw.BTXW_Device;
import com.bracelet.btxw.R;
import com.bracelet.btxw.view.BleApplication;

import java.util.ArrayList;
import java.util.List;

public class Configs {

    private static Resources sResources = BleApplication.getContext().getResources();

    public static final String[] sTagItems = new String[]{"BT11", "BT07", "BT01"};

    public static ConfigItem[] sTransmissionPowers = generateTransmissionPowers();

    private static ConfigItem[] generateTransmissionPowers() {
        List<ConfigItem> list = new ArrayList<>();
        list.add(new ConfigItem(BTXW_Device.POWER_POINT_10_0, String.format(sResources.getString(R.string.config_transmission_power), 10.0f)));
        list.add(new ConfigItem(BTXW_Device.POWER_POINT_9_8, String.format(sResources.getString(R.string.config_transmission_power), 9.8f)));
        list.add(new ConfigItem(BTXW_Device.POWER_POINT_9_5, String.format(sResources.getString(R.string.config_transmission_power), 9.5f)));
        list.add(new ConfigItem(BTXW_Device.POWER_POINT_9_2, String.format(sResources.getString(R.string.config_transmission_power), 9.2f)));
        list.add(new ConfigItem(BTXW_Device.POWER_POINT_9_0, String.format(sResources.getString(R.string.config_transmission_power), 9.0f)));
        list.add(new ConfigItem(BTXW_Device.POWER_POINT_8_7, String.format(sResources.getString(R.string.config_transmission_power), 8.7f)));
        list.add(new ConfigItem(BTXW_Device.POWER_POINT_8_4, String.format(sResources.getString(R.string.config_transmission_power), 8.4f)));
        list.add(new ConfigItem(BTXW_Device.POWER_POINT_8_1, String.format(sResources.getString(R.string.config_transmission_power), 8.1f)));
        list.add(new ConfigItem(BTXW_Device.POWER_POINT_7_8, String.format(sResources.getString(R.string.config_transmission_power), 7.8f)));
        list.add(new ConfigItem(BTXW_Device.POWER_POINT_7_4, String.format(sResources.getString(R.string.config_transmission_power), 7.4f)));
        list.add(new ConfigItem(BTXW_Device.POWER_POINT_7_0, String.format(sResources.getString(R.string.config_transmission_power), 7.0f)));
        list.add(new ConfigItem(BTXW_Device.POWER_POINT_6_6, String.format(sResources.getString(R.string.config_transmission_power), 6.6f)));
        list.add(new ConfigItem(BTXW_Device.POWER_POINT_6_1, String.format(sResources.getString(R.string.config_transmission_power), 6.1f)));
        list.add(new ConfigItem(BTXW_Device.POWER_POINT_5_7, String.format(sResources.getString(R.string.config_transmission_power), 5.7f)));
        list.add(new ConfigItem(BTXW_Device.POWER_POINT_5_1, String.format(sResources.getString(R.string.config_transmission_power), 5.1f)));
        list.add(new ConfigItem(BTXW_Device.POWER_POINT_4_6, String.format(sResources.getString(R.string.config_transmission_power), 4.6f)));
        list.add(new ConfigItem(BTXW_Device.POWER_POINT_4_0, String.format(sResources.getString(R.string.config_transmission_power), 4.0f)));
        list.add(new ConfigItem(BTXW_Device.POWER_POINT_3_2, String.format(sResources.getString(R.string.config_transmission_power), 3.2f)));
        list.add(new ConfigItem(BTXW_Device.POWER_POINT_3_0, String.format(sResources.getString(R.string.config_transmission_power), 3.0f)));
        list.add(new ConfigItem(BTXW_Device.POWER_POINT_2_8, String.format(sResources.getString(R.string.config_transmission_power), 2.8f)));
        list.add(new ConfigItem(BTXW_Device.POWER_POINT_2_6, String.format(sResources.getString(R.string.config_transmission_power), 2.6f)));
        list.add(new ConfigItem(BTXW_Device.POWER_POINT_2_4, String.format(sResources.getString(R.string.config_transmission_power), 2.4f)));
        list.add(new ConfigItem(BTXW_Device.POWER_POINT_2_0, String.format(sResources.getString(R.string.config_transmission_power), 2.0f)));
        list.add(new ConfigItem(BTXW_Device.POWER_POINT_1_7, String.format(sResources.getString(R.string.config_transmission_power), 1.7f)));
        list.add(new ConfigItem(BTXW_Device.POWER_POINT_1_5, String.format(sResources.getString(R.string.config_transmission_power), 1.5f)));
        list.add(new ConfigItem(BTXW_Device.POWER_POINT_1_2, String.format(sResources.getString(R.string.config_transmission_power), 1.2f)));
        list.add(new ConfigItem(BTXW_Device.POWER_POINT_0_9, String.format(sResources.getString(R.string.config_transmission_power), 0.9f)));
        list.add(new ConfigItem(BTXW_Device.POWER_POINT_0_6, String.format(sResources.getString(R.string.config_transmission_power), 0.6f)));
        list.add(new ConfigItem(BTXW_Device.POWER_POINT_0_0, String.format(sResources.getString(R.string.config_transmission_power), 0.0f)));
        list.add(new ConfigItem(BTXW_Device.POWER_MINUS_POINT_0_1 , String.format(sResources.getString(R.string.config_transmission_power), -0.1f)));
        list.add(new ConfigItem(BTXW_Device.POWER_MINUS_POINT_1_0, String.format(sResources.getString(R.string.config_transmission_power), -1.0f)));
        list.add(new ConfigItem(BTXW_Device.POWER_MINUS_POINT_1_4, String.format(sResources.getString(R.string.config_transmission_power), -1.4f)));
        list.add(new ConfigItem(BTXW_Device.POWER_MINUS_POINT_1_9, String.format(sResources.getString(R.string.config_transmission_power), -1.9f)));
        list.add(new ConfigItem(BTXW_Device.POWER_MINUS_POINT_2_5, String.format(sResources.getString(R.string.config_transmission_power), -2.5f)));
        list.add(new ConfigItem(BTXW_Device.POWER_MINUS_POINT_3_0, String.format(sResources.getString(R.string.config_transmission_power), -3.0f)));
        list.add(new ConfigItem(BTXW_Device.POWER_MINUS_POINT_3_6, String.format(sResources.getString(R.string.config_transmission_power), -3.6f)));
        list.add(new ConfigItem(BTXW_Device.POWER_MINUS_POINT_4_3, String.format(sResources.getString(R.string.config_transmission_power), -4.3f)));
        list.add(new ConfigItem(BTXW_Device.POWER_MINUS_POINT_5_0, String.format(sResources.getString(R.string.config_transmission_power), -5.0f)));
        list.add(new ConfigItem(BTXW_Device.POWER_MINUS_POINT_5_8, String.format(sResources.getString(R.string.config_transmission_power), -5.8f)));
        list.add(new ConfigItem(BTXW_Device.POWER_MINUS_POINT_6_7 , String.format(sResources.getString(R.string.config_transmission_power), -6.7f)));
        list.add(new ConfigItem(BTXW_Device.POWER_MINUS_POINT_7_7 , String.format(sResources.getString(R.string.config_transmission_power), -7.7f)));
        list.add(new ConfigItem(BTXW_Device.POWER_MINUS_POINT_8_7 , String.format(sResources.getString(R.string.config_transmission_power), -8.7f)));
        list.add(new ConfigItem(BTXW_Device.POWER_MINUS_POINT_9_9 , String.format(sResources.getString(R.string.config_transmission_power), -9.9f)));
        list.add(new ConfigItem(BTXW_Device.POWER_MINUS_POINT_11_4, String.format(sResources.getString(R.string.config_transmission_power), -11.4f)));
        list.add(new ConfigItem(BTXW_Device.POWER_MINUS_POINT_13_3, String.format(sResources.getString(R.string.config_transmission_power), -13.3f)));
        list.add(new ConfigItem(BTXW_Device.POWER_MINUS_POINT_15_9, String.format(sResources.getString(R.string.config_transmission_power), -15.9f)));
        list.add(new ConfigItem(BTXW_Device.POWER_MINUS_POINT_19_3, String.format(sResources.getString(R.string.config_transmission_power), -19.3f)));
        list.add(new ConfigItem(BTXW_Device.POWER_MINUS_POINT_25_2, String.format(sResources.getString(R.string.config_transmission_power), -25.2f)));
        return list.toArray(new ConfigItem[list.size()]);
    }

    public static ConfigItem[] sTwoLightingType = generateTwoLightingType();

    private static ConfigItem[] generateTwoLightingType() {
        List<ConfigItem> list = new ArrayList<>();
        list.add(new ConfigItem(BTXW_Device.LIGHT_RED, sResources.getString(R.string.lighting_red)));
        list.add(new ConfigItem(BTXW_Device.LIGHT_BLUE, sResources.getString(R.string.lighting_blue)));
        list.add(new ConfigItem(BTXW_Device.LIGHT_RED_BLUE, sResources.getString(R.string.lighting_red_blue)));
        return list.toArray(new ConfigItem[list.size()]);
    }

    public static ConfigItem[] sMultipleLightingGroup = generateMultipleLightingGroup();

    private static ConfigItem[] generateMultipleLightingGroup() {
        List<ConfigItem> list = new ArrayList<>();
        list.add(new ConfigItem(BTXW_Device.LIGHT_ALL, sResources.getString(R.string.lighting_all)));
        list.add(new ConfigItem(BTXW_Device.LIGHT_PURPLE_GROUP, sResources.getString(R.string.lighting_purple_group)));
        list.add(new ConfigItem(BTXW_Device.LIGHT_RED_GROUP, sResources.getString(R.string.lighting_red_group)));
        list.add(new ConfigItem(BTXW_Device.LIGHT_WHITE_GROUP, sResources.getString(R.string.lighting_white_group)));
        list.add(new ConfigItem(BTXW_Device.LIGHT_YELLOW_GROUP, sResources.getString(R.string.lighting_yellow_group)));
        list.add(new ConfigItem(BTXW_Device.LIGHT_BLUE_GROUP, sResources.getString(R.string.lighting_blue_group)));
        list.add(new ConfigItem(BTXW_Device.LIGHT_ORANGE_GROUP, sResources.getString(R.string.lighting_orange_group)));
        return list.toArray(new ConfigItem[0]);
    }

    public static ConfigItem[] sBleBTGroup = generateBleBTGroup();

    private static ConfigItem[] generateBleBTGroup() {
        List<ConfigItem> list = new ArrayList<>();
        list.add(new ConfigItem(BleBT.BT05, BleBT.BT05TypeName));
        list.add(new ConfigItem(BleBT.BT05L, BleBT.BT05LTypeName));
        list.add(new ConfigItem(BleBT.BT06AOA, BleBT.BT06AOATypeName));
        list.add(new ConfigItem(BleBT.BT06L, BleBT.BT06LTypeName));
        list.add(new ConfigItem(BleBT.BT06LAOA, BleBT.BT06LAOATypeName));
        list.add(new ConfigItem(BleBT.BT07AOA, BleBT.BT07AOATypeName));
        list.add(new ConfigItem(BleBT.BT11AOA, BleBT.BT11AOATypeName));
        return list.toArray(new ConfigItem[0]);
    }

    public static String[] getConfigItemsNames(Configs.ConfigItem[] allItems) {
        String[] names = new String[allItems.length];
        for (int i = 0; i < names.length; i ++) {
            names[i] = allItems[i].getDescription();
        }
        return names;
    }

    public static class ConfigItem {
        private byte value;
        private String description;

        public ConfigItem(byte value, String description) {
            this.value = value;
            this.description = description;
        }

        public byte getValue() {
            return value;
        }

        public String getDescription() {
            return description;
        }
    }
}
