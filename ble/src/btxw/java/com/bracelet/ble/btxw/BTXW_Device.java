package com.bracelet.ble.btxw;

import android.content.Context;

public interface BTXW_Device {

    //region Constant

    // set indicator light time
    byte LIGHT_MIN = 1;
    byte LIGHT_MAX = (byte)250;
    byte LIGHT_DEFAULT = 5;

    // set beep time
    byte BEEP_MIN = 1;
    byte BEEP_MAX = (byte)250;
    byte BEEP_DEFAULT = 5;

    // transmission power region
    byte POWER_POINT_10_0 = 2;
    byte POWER_POINT_9_8 = 3;
    byte POWER_POINT_9_5 = 4;
    byte POWER_POINT_9_2 = 5;
    byte POWER_POINT_9_0 = 6;
    byte POWER_POINT_8_7 = 7;
    byte POWER_POINT_8_4 = 8;
    byte POWER_POINT_8_1 = 9;
    byte POWER_POINT_7_8 = 10;
    byte POWER_POINT_7_4 = 11;
    byte POWER_POINT_7_0 = 12;
    byte POWER_POINT_6_6 = 13;
    byte POWER_POINT_6_1 = 14;
    byte POWER_POINT_5_7 = 15;
    byte POWER_POINT_5_1 = 16;
    byte POWER_POINT_4_6 = 17;
    byte POWER_POINT_4_0 = 18;
    byte POWER_POINT_3_2 = 19;
    byte POWER_POINT_3_0 = 20;
    byte POWER_POINT_2_8 = 21;
    byte POWER_POINT_2_6 = 22;
    byte POWER_POINT_2_4 = 23;
    byte POWER_POINT_2_0 = 24;
    byte POWER_POINT_1_7 = 25;
    byte POWER_POINT_1_5 = 26;
    byte POWER_POINT_1_2 = 27;
    byte POWER_POINT_0_9 = 28;
    byte POWER_POINT_0_6 = 29;
    byte POWER_POINT_0_0 = 30;
    byte POWER_MINUS_POINT_0_1 = 31;
    byte POWER_MINUS_POINT_1_0 = 32;
    byte POWER_MINUS_POINT_1_4 = 33;
    byte POWER_MINUS_POINT_1_9 = 34;
    byte POWER_MINUS_POINT_2_5 = 35;
    byte POWER_MINUS_POINT_3_0 = 36;
    byte POWER_MINUS_POINT_3_6 = 37;
    byte POWER_MINUS_POINT_4_3 = 38;
    byte POWER_MINUS_POINT_5_0 = 39;
    byte POWER_MINUS_POINT_5_8 = 40;
    byte POWER_MINUS_POINT_6_7 = 41;
    byte POWER_MINUS_POINT_7_7 = 42;
    byte POWER_MINUS_POINT_8_7 = 43;
    byte POWER_MINUS_POINT_9_9 = 44;
    byte POWER_MINUS_POINT_11_4 = 45;
    byte POWER_MINUS_POINT_13_3 = 46;
    byte POWER_MINUS_POINT_15_9 = 47;
    byte POWER_MINUS_POINT_19_3 = 48;
    byte POWER_MINUS_POINT_25_2 = 49;

    byte POWER_DEFAULT = POWER_MINUS_POINT_15_9;

    byte LIGHT_RED = 0x01;
    byte LIGHT_BLUE = 0x02;
    byte LIGHT_RED_BLUE = 0x03;

    byte LIGHT_ALL = 0x3F;
    byte LIGHT_PURPLE_GROUP = 0x20;
    byte LIGHT_RED_GROUP = 0x10;
    byte LIGHT_WHITE_GROUP = 0x08;
    byte LIGHT_YELLOW_GROUP = 0x04;
    byte LIGHT_BLUE_GROUP = 0x02;
    byte LIGHT_ORANGE_GROUP = 0x01;
    //endregion

    //region bluetooth info

    /**
     * get device name from the bluetooth scan
     * @return device name of bluetooth
     */
    String getName();

    /**
     * get device address(mac) from the bluetooth scan
     * @return mac address of bluetooth
     */
    String getAddress();

    /**
     * get device rssi from the bluetooth scan
     * @return bluetooth rssi
     */
    int getRssi();

    /**
     * update Bluetooth rssi
     * @param rssi Bluetooth rssi value
     */
    void updateRssi(int rssi);
    //endregion

    //region connect && disconnect

    /**
     * connect ble device with callback
     * @param context context
     * @param callback event of onConnect and onDisconnect
     */
    void connect(Context context, ConnectionCallback callback);

    /**
     * disconnect ble device
     */
    void disconnect();

    /**
     * get status of connecting
     * @return true:connecting;
     */
    boolean isConnecting();

    /**
     * get status of connected
     * @return true:connected;
     */
    boolean isConnected();

    /**
     * callback of device connection status event
     */
    interface ConnectionCallback {

        /**
         * event of device connect
         */
        void onConnect();

        /**
         * event of device disconnect
         */
        void onDisconnect();

    }
    //endregion

    //region command

    /**
     * callback of get device mac
     */
    interface GetDeviceMacCallback {
        /**
         *
         * @param status success: 0;
         *               failed: refer to BleError.
         * @param mac mac of device.
         *            mac stored big-end-first.
         *            such as '01:FF:00:80:12:E1'.
         */
        void onResult(int status, String mac);
    }

    /**
     *
     * @param callback GetDeviceMacCallback
     */
    void getDeviceMac(GetDeviceMacCallback callback);

    /**
     * callback of get device version
     */
    interface GetDeviceVersionCallback {
        /**
         *
         * @param status success: 0;
         *               failed: refer to BleError.
         * @param version version of device.
         *                such as '50201(01)'.
         */
        void onResult(int status, String version);
    }

    /**
     *
     * @param callback GetDeviceVersionCallback
     */
    void getDeviceVersion(GetDeviceVersionCallback callback);

    /**
     * callback of get device power
     */
    interface GetDevicePowerCallback {
        /**
         *
         * @param status success: 0;
         *               failed: refer to BleError.
         * @param voltageValue voltageValue(mV) of device.
         * @param powerPercent power percent. the value is range from 0 to 100.
         */
        void onResult(int status, short voltageValue, byte powerPercent);
    }

    /**
     *
     * @param callback GetDevicePowerCallback
     */
    void getDevicePower(GetDevicePowerCallback callback);

    /**
     * callback of get device time
     */
    interface GetDeviceTimeCallback {
        /**
         *
         * @param status success: 0;
         *               failed: refer to BleError.
         * @param unixTimestamp unixTimestamp of device.
         */
        void onResult(int status, long unixTimestamp);
    }

    /**
     *
     * @param callback GetDeviceTimeCallback
     */
    void getDeviceTime(GetDeviceTimeCallback callback);

    /**
     * callback of set device time
     */
    interface SetDeviceTimeCallback {
        /**
         *
         * @param status success: 0;
         *               failed: refer to BleError.
         */
        void onResult(int status);
    }

    /**
     *
     * @param callback SetDeviceTimeCallback
     * @param unixTimeStampSecond unix timestamp, second
     */
    void setDeviceTime(SetDeviceTimeCallback callback, long unixTimeStampSecond);

    /**
     * callback of exchange random
     */
    interface ExchangeRandomCallback {
        /**
         *
         * @param status success: 0;
         *               failed: refer to BleError.
         * @param random_s random data generated by device, length is 8.
         */
        void onResult(int status, byte[] random_s);
    }

    /**
     * The method is designed for 'autoAuthenticate' method.
     *
     * @param callback ExchangeRandomCallback
     * @param random_m random data generated by phone, length is 8.
     */
    void exchangeRandom(ExchangeRandomCallback callback, byte[] random_m);

    /**
     * callback of authenticate
     */
    interface AuthenticateCallback {
        /**
         *
         * @param status success: 0;
         *               failed: refer to BleError.
         * @param authResult result of authentication.
         *                   0x00 means succeed.
         *                   0x01 means failed, need to call 'exchangeRandom' method first.
         *                   0x02 means failed, auth data is error.
         *                   0x03 means failed, the number of errors exceeded, please reconnect.
         *                   0x04 means failed, authentication is finished, don't authenticate repeatedly.
         */
        void onResult(int status, byte authResult);
    }

    /**
     *
     * The method is designed for 'autoAuthenticate' method.
     *
     * @param callback AuthenticateCallback
     * @param authData the data used for authenticate
     */
    void authenticate(AuthenticateCallback callback, byte[] authData);

    /**
     * callback of auto authenticate
     */
    interface AutoAuthenticateCallback {
        /**
         *
         * @param status success: 0;
         *               failed: refer to BleError.
         * @param authResult result of authentication.
         *                   0x00 means succeed.
         *                   0x01 means failed, need to call 'exchangeRandom' method first.
         *                   0x02 means failed, auth data is error.
         *                   0x03 means failed, the number of errors exceeded, please reconnect.
         *                   0x04 means failed, authentication is finished, don't authenticate repeatedly.
         */
        void onResult(int status, byte authResult);
    }

    /**
     * Unlock functions after authentication, including 'setDeviceTime', 'setDeviceName'
     * and 'shutDownDevice'.
     *
     * During device connection, only need to authenticate once.
     *
     * @param callback AutoAuthenticateCallback
     */
    void autoAuthenticate(AutoAuthenticateCallback callback);

    /**
     * callback of get device name
     */
    interface GetDeviceNameCallback {
        /**
         *
         * @param status success: 0;
         *               failed: refer to BleError.
         * @param deviceName  name of device.
         */
        void onResult(int status, String deviceName);
    }

    /**
     *
     * @param callback GetDeviceNameCallback
     */
    void getDeviceName(GetDeviceNameCallback callback);

    /**
     * callback of set device name
     */
    interface SetDeviceNameCallback {
        /**
         *
         * @param status success: 0;
         *               failed: refer to BleError.
         */
        void onResult(int status);
    }

    /**
     *
     * @param callback SetDeviceNameCallback
     * @param name bluetooth name, maximum length is 17.
     */
    void setDeviceName(SetDeviceNameCallback callback, String name);

    /**
     * callback of shut down
     */
    interface ShutDownDeviceCallback {
        /**
         *
         * @param status success: 0;
         *               failed: refer to BleError.
         */
        void onResult(int status);
    }

    /**
     *
     * @param callback ShutDownDeviceCallback
     */
    void shutDownDevice(ShutDownDeviceCallback callback);

    /**
     * callback of get device transmission power
     */
    interface GetDeviceTransmissionPowerCallback {
        /**
         *
         * @param status success: 0;
         *               failed: refer to BleError.
         * @param transmissionPower  transmission power of device.
         *                           example: POWER_POINT_10_0, POWER_MINUS_POINT_15_9
         */
        void onResult(int status, byte transmissionPower);
    }

    /**
     *
     * @param callback GetDeviceTransmissionPowerCallback
     */
    void getDeviceTransmissionPower(GetDeviceTransmissionPowerCallback callback);

    /**
     * callback of set device transmission power
     */
    interface SetDeviceTransmissionPowerCallback {
        /**
         *
         * @param status success: 0;
         *               failed: refer to BleError.
         */
        void onResult(int status);
    }

    /**
     *
     * @param callback SetDeviceTransmissionPowerCallback
     * @param transmissionPower transmission power of device.
     *                          example: POWER_POINT_10_0, POWER_MINUS_POINT_15_9
     */
    void setDeviceTransmissionPower(SetDeviceTransmissionPowerCallback callback, byte transmissionPower);

    /**
     * callback of open device light
     */
    interface OpenDeviceLightCallback {
        /**
         *
         * @param status success: 0;
         *               failed: refer to BleError.
         */
        void onResult(int status);
    }

    /**
     *
     * @param callback OpenDeviceLightCallback
     * @param openSeconds open light seconds
     */
    void openDeviceLight(OpenDeviceLightCallback callback, byte openSeconds);

    /**
     *
     * @param callback OpenDeviceLightCallback
     * @param lightType open light type, LIGHT_RED, LIGHT_BLUE, LIGHT_RED_BLUE
     * @param openSeconds open light seconds
     */
    void openDeviceTwoLights(OpenDeviceLightCallback callback, byte lightType, byte openSeconds);

    /**
     *
     * @param callback OpenDeviceLightCallback
     * @param lightGroup open light group, combination of LIGHT_PURPLE_GROUP,
     *                   LIGHT_RED_GROUP, LIGHT_WHITE_GROUP, LIGHT_YELLOW_GROUP, LIGHT_BLUE_GROUP, LIGHT_ORANGE_GROUP
     * @param openSeconds open light seconds
     */
    void openDeviceMultipleLights(OpenDeviceLightCallback callback, byte[] lightGroup, byte openSeconds);

    /**
     * callback of open device beep
     */
    interface OpenDeviceBeepCallback {
        /**
         *
         * @param status success: 0;
         *               failed: refer to BleError.
         */
        void onResult(int status);
    }

    /**
     *
     * @param callback OpenDeviceBeepCallback
     * @param openSeconds open beep seconds
     */
    void openDeviceBeep(OpenDeviceBeepCallback callback, byte openSeconds);

    /**
     * callback of set interval of bluetooth broadcast
     */
    interface SetBluetoothBroadcastIntervalCallback {
        /**
         *
         * @param status success: 0;
         *               failed: refer to BleError.
         */
        void onResult(int status);
    }

    /**
     *
     * @param callback SetBluetoothBroadcastIntervalCallback
     * @param interval interval of bluetooth interval, the value is range from 5 to 100, and the unit is 10ms
     */
    void setBluetoothBroadcastInterval(SetBluetoothBroadcastIntervalCallback callback, byte interval);

    /**
     * callback of get interval of bluetooth broadcast
     */
    interface GetBluetoothBroadcastIntervalCallback {
        /**
         *
         * @param status success: 0;
         *               failed: refer to BleError.
         * @param interval interval of bluetooth interval, the value is range from 5 to 100, and the unit is 10ms
         */
        void onResult(int status, byte interval);
    }

    /**
     *
     * @param callback GetBluetoothBroadcastIntervalCallback
     */
    void getBluetoothBroadcastInterval(GetBluetoothBroadcastIntervalCallback callback);
    //endregion
}
