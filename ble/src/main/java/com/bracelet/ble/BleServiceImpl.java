package com.bracelet.ble;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.ParcelUuid;
import android.text.TextUtils;
import android.util.Log;

import com.bracelet.ble.bt.BleBT05;
import com.bracelet.ble.bt.BleBT05L;
import com.bracelet.ble.bt.BleBT06AOA;
import com.bracelet.ble.bt.BleBT06L;
import com.bracelet.ble.bt.BleBT06LAOA;
import com.bracelet.ble.bt.BleBT07AOA;
import com.bracelet.ble.bt.BleBT11AOA;
import com.bracelet.ble.utils.ByteUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BleServiceImpl implements BleService {
    private static final UUID TRANSFER_SERVICE_UUID = UUID.fromString("0000ff00-0000-1000-8000-00805f9b34fb");

    private Context context;
    private SearchCallback callback;
    private SearchBTBroadcastCallback mSearchBTBroadcastCallback;
    private BluetoothAdapter adapter;
    private BluetoothLeScanner scanner;

    //region not AOA broadcast

    //BT05:
    // group1: vibrate, 1byte, (true:0x11, false:0x00)
    // group2: temperature, 2bytes, (value1 + value2 * 0.1)℃, if T is negative, value1 + 0x80
    // group3: voltage, 1byte, (value1 << 8 | value2)mV
    private static final String BT05Regex = "^06FF" +
            "([0-9A-Fa-f]{2})" +
            "([0-9A-Fa-f]{4})" +
            "([0-9A-Fa-f]{4})" +
            "[0-9A-Fa-f]*";

    //BT05L:
    // group1: ble name, 8bytes, (ASCII)
    // group2: voltage, 1byte, (value * 0.03125)V
    // group3: inner temperature, 2bytes, ((value2 << 8 | value1) * 0.01)℃
    // group4: power on time, 4bytes, (value4 << 24 | value3 << 16 | value2 << 8 | value1)s
    // group5: button, 1byte, (up:0xDD, down:0xD9)
    // group6: outer temperature, 2bytes, ((value2 << 8 | value1) * 0.0625 - 50.0625)℃
    private static final String BT05LRegex = "^0909" +
            "([0-9A-Fa-f]{16})" +
            "11FF0505" +
            "([0-9A-Fa-f]{2})" +
            "([0-9A-Fa-f]{4})" +
            "([0-9A-Fa-f]{8})" +
            "(DD|D9)" +
            "[0-9A-Fa-f]{8}" +
            "([0-9A-Fa-f]{4})" +
            "[0-9A-Fa-f]*";

    //BT06L:
    // group1: ble name, 8bytes, (ASCII)
    // group2: voltage, 1 byte, (value * 0.03125)V
    // group3: temperature, 2bytes, ((value2 << 8 | value1) * 0.01)℃
    // group4: power on time, 4bytes, (value4 << 24 | value3 << 16 | value2 << 8 | value1)s
    // group5: button, 1byte, (up:0xFF, down:0xDF)
    private static final String BT06LRegex = "^0909" +
            "([0-9A-Fa-f]{16})" +
            "0BFF0505" +
            "([0-9A-Fa-f]{2})" +
            "([0-9A-Fa-f]{4})" +
            "([0-9A-Fa-f]{8})" +
            "(FF|DF)" +
            "[0-9A-Fa-f]*";

    //endregion

    //region AOA broadcast

    //AOA:
    // group1: for calculating AOA CRC
    // group2: AOA CRC
    private static final String AOARegex = "^(1EFF0D0004[0-9A-Fa-f]{0,8})" +
            "([0-9A-Fa-f]{4})" +
            "2F61ACCC274567F7DB34C4038E5C0BAA973056E6[0-9A-Fa-f]*";

    //BT06AOA: part1
    // group1: moving status, 1byte
    // group2: acceleration sensor X, 1byte
    // group3: acceleration sensor Y, 1byte
    // group4: acceleration sensor Z, 1byte
    private static final String BT06AOAPART1Regex = "^1EFF0D0004" +
            "(08)" +
            "([0-9A-Fa-f]{2})" +
            "([0-9A-Fa-f]{2})" +
            "([0-9A-Fa-f]{2})" +
            "[0-9A-Fa-f]{4}2F61ACCC274567F7DB34C4038E5C0BAA973056E6[0-9A-Fa-f]*";

    //BT06AOA: part2
    // group1: resting status, 1byte
    // group2: button, 1byte, bit5: tamper, (normal:0, tampered:1)
    // group3: version, 1byte, dec
    // group4: voltage, 1byte, (value * 100)mV
    private static final String BT06AOAPART2Regex = "^1EFF0D0004" +
            "(09)" +
            "([0-9A-Fa-f]{2})" +
            "([0-9A-Fa-f]{2})" +
            "([0-9A-Fa-f]{2})" +
            "[0-9A-Fa-f]{4}2F61ACCC274567F7DB34C4038E5C0BAA973056E6[0-9A-Fa-f]*";

    //BT06L-AOA:
    // no ble name, no group
    private static final String BT06LAOARegex = "^1EFF0D000400000000[0-9A-Fa-f]{4}2F61ACCC274567F7DB34C4038E5C0BAA973056E6[0-9A-Fa-f]*";

    //BT07-AOA:
    // group1: button, 1byte, bit4: button, (up:0, down:1)
    // group2: version, 1byte, dec
    // group3: voltage, 1byte, (value * 100)mV
    private static final String BT07AOARegex = "^1EFF0D000409" +
            "([0-9A-Fa-f]{2})" +
            "([0-9A-Fa-f]{2})" +
            "([0-9A-Fa-f]{2})" +
            "[0-9A-Fa-f]{4}2F61ACCC274567F7DB34C4038E5C0BAA973056E6[0-9A-Fa-f]*";

    //BT11-AOA: same as BT06L-AOA but have ble name
    // have ble name, no group
    private static final String BT11AOARegex = "^1EFF0D000400000000[0-9A-Fa-f]{4}2F61ACCC274567F7DB34C4038E5C0BAA973056E6[0-9A-Fa-f]*";

    //endregion

    private Pattern mAOAPattern;

    public BleServiceImpl(Context context) throws BleException {

        this.context = context;

        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            throw new BleException(-1, "BLE is not supported");
        }

        BluetoothManager manager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        adapter = manager.getAdapter();
        if (adapter == null) {
            throw new BleException(-1, "Bluetooth is not supported");
        }

        if (!adapter.isEnabled()) {
            adapter.enable();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            scanner = adapter.getBluetoothLeScanner();
            if (scanner == null) {
                throw new BleException(-1, "Bluetooth Scanner is not found");
            }
        }

        mAOAPattern = Pattern.compile(AOARegex);
    }

    public BluetoothAdapter obtainBluetoothAdapter(){
        return adapter;
    }

    public void setCallback(SearchCallback callback) {
        this.callback = callback;
    }

    public void startScan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            List<ScanFilter> filters = new ArrayList<>();
            ScanFilter filter = new ScanFilter.Builder()
                    .setServiceUuid(new ParcelUuid(TRANSFER_SERVICE_UUID))
                    .build();
            filters.add(filter);

            ScanSettings settings = new ScanSettings.Builder()
                    .setScanMode( ScanSettings.SCAN_MODE_LOW_LATENCY )
                    .build();

            scanner.startScan(filters, settings, mScanCallback);
        } else {
            adapter.startLeScan(scanCallback);
        }
    }

    public void stopScan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (adapter != null
                    && adapter.isEnabled()
                    && adapter.getState() == BluetoothAdapter.STATE_ON) {
                scanner.stopScan(mScanCallback);
            }
        } else {
            adapter.stopLeScan(scanCallback);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            ScanRecord record = result.getScanRecord();
            if (record != null) {
                byte[] rawData = record.getBytes();
                if (rawData != null && rawData.length >= 7) {
                    //BLE device
                    if (rawData[0] == 0x02 && rawData[1] == 0x01 && rawData[2] == 0x06) {
                        //Manufacturer Specific Data
                        if ((rawData[3] == 0x08 || rawData[3] == 0x09) && rawData[4] == (byte)0xFF
                                && rawData[5] == 0x22 && rawData[6] == 0x07) {
                            BluetoothDevice device = result.getDevice();
                            if (device != null) {
                                if (callback != null) {
                                    BleBracelet bracelet = new BleBraceletImpl(context, device, result.getRssi());
                                    callback.onDiscoverBleBracelet(bracelet);
                                }
                            }
                        }
                    } else {
                        //AOA定位广播包格式，数据长
                        if (rawData.length >= ((5 + 2 + 10))) {
                            BluetoothDevice device = result.getDevice();
                            if (device != null) {
                                String rawString = ByteUtils.hex2str(rawData, "");
                                if (rawString.matches(AOARegex)) {
                                    //AOA location broadcast
                                    Matcher matcher = mAOAPattern.matcher(rawString);
                                    if (matcher.find() && matcher.groupCount() == 2) {
                                        byte[] crc;
                                        try {
                                            //截取CRC前的内容计算CRC
                                            crc = crcAOA(device.getAddress(), matcher.group(1));
                                        } catch (Exception ex) {
                                            ex.printStackTrace();
                                            return;
                                        }
                                        String s = ByteUtils.hex2str(crc, "");
                                        //校验CRC正确
                                        if (TextUtils.equals(s, matcher.group(2))) {
                                            if (callback != null) {
                                                BleBracelet bracelet = new BleBraceletImpl(context, device, result.getRssi());
                                                callback.onDiscoverBleBracelet(bracelet);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };

    @Deprecated
    private BluetoothAdapter.LeScanCallback scanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            if (scanRecord != null && scanRecord.length >= 7) {
                //BLE device
                if (scanRecord[0] == 0x02 && scanRecord[1] == 0x01 && scanRecord[2] == 0x06) {
                    //Manufacturer Specific Data
                    if ((scanRecord[3] == 0x08 || scanRecord[3] == 0x09) && scanRecord[4] == (byte)0xFF
                            && scanRecord[5] == 0x22 && scanRecord[6] == 0x07) {
                        if (callback != null) {
                            BleBracelet bracelet = new BleBraceletImpl(context, device, rssi);
                            callback.onDiscoverBleBracelet(bracelet);
                        }
                    }
                } else {
                    if (scanRecord.length >= (5 + 2 + 10)) {
                        String rawString = ByteUtils.hex2str(scanRecord, "");
                        if (rawString.matches(AOARegex)) {
                            //AOA location broadcast
                            Matcher matcher = mAOAPattern.matcher(rawString);
                            if (matcher.find() && matcher.groupCount() == 2) {
                                byte[] crc;
                                try {
                                    //截取CRC前的内容计算CRC
                                    crc = crcAOA(device.getAddress(), matcher.group(1));
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                    return;
                                }
                                String s = ByteUtils.hex2str(crc, "");
                                //校验CRC正确
                                if (TextUtils.equals(s, matcher.group(2))) {
                                    if (callback != null) {
                                        BleBracelet bracelet = new BleBraceletImpl(context, device, rssi);
                                        callback.onDiscoverBleBracelet(bracelet);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    };

    public void setSearchBTBroadcastCallback(SearchBTBroadcastCallback callback) {
        mSearchBTBroadcastCallback = callback;
    }

    @Override
    public void startScanForBTBroadcast() {
        List<ScanFilter> filters = new ArrayList<>();
        ScanFilter filter = new ScanFilter.Builder()
                .build();
        filters.add(filter);

        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode( ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();

        scanner.startScan(filters, settings, mForBroadCastScanCallback);
    }

    @Override
    public void stopScanForBTBroadcast() {
        if (adapter != null
                && adapter.isEnabled()
                && adapter.getState() == BluetoothAdapter.STATE_ON) {
            scanner.stopScan(mForBroadCastScanCallback);
        }
    }

    private ScanCallback mForBroadCastScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            if (mSearchBTBroadcastCallback == null) {
                return;
            }
            ScanRecord record = result.getScanRecord();
            if (record != null) {
                byte[] rawData = record.getBytes();
                if (rawData != null && rawData.length >= 7) {
                    //BLE device
                    BluetoothDevice device = result.getDevice();
                    int rssi = result.getRssi();
                    if (device != null) {
                        String rawString = ByteUtils.hex2str(rawData, "");
                        //Log.d("广播包",rawString);
                        if (rawString.matches(AOARegex)) {
                            //AOA格式广播包
                            Matcher matcher = mAOAPattern.matcher(rawString);
                            if (matcher.find() && matcher.groupCount() == 2) {
                                byte[] crc;
                                try {
                                    //截取CRC前的内容计算CRC
                                    crc = crcAOA(device.getAddress(), matcher.group(1));
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                    return;
                                }
                                String s = ByteUtils.hex2str(crc, "");
                                //校验CRC正确
                                if (TextUtils.equals(s, matcher.group(2))) {
                                    if (rawString.matches(BT06AOAPART1Regex)) {
                                        mSearchBTBroadcastCallback.onDiscoverBleBT(new BleBT06AOA(device, rssi, rawString, BT06AOAPART1Regex));
                                    } else if (rawString.matches(BT06AOAPART2Regex) && !TextUtils.isEmpty(device.getName()) && device.getName().contains("BT06")) {
                                        mSearchBTBroadcastCallback.onDiscoverBleBT(new BleBT06AOA(device, rssi, rawString, BT06AOAPART2Regex));
                                    } else if (rawString.matches(BT06LAOARegex) && TextUtils.isEmpty(device.getName())) {
                                        mSearchBTBroadcastCallback.onDiscoverBleBT(new BleBT06LAOA(device, rssi, rawString, BT06LAOARegex));
                                    } else if (rawString.matches(BT07AOARegex) && !TextUtils.isEmpty(device.getName()) && device.getName().contains("BT07")) {
                                        mSearchBTBroadcastCallback.onDiscoverBleBT(new BleBT07AOA(device, rssi, rawString, BT07AOARegex));
                                    } else if (rawString.matches(BT11AOARegex)) {
                                        mSearchBTBroadcastCallback.onDiscoverBleBT(new BleBT11AOA(device, rssi, rawString, BT11AOARegex));
                                    }
                                }
                            }
                        } else {
                            //非AOA格式广播包
                            if (rawString.matches(BT05Regex)) {
                                mSearchBTBroadcastCallback.onDiscoverBleBT(new BleBT05(device, rssi, rawString, BT05Regex));
                            } else if (rawString.matches(BT05LRegex)) {
                                mSearchBTBroadcastCallback.onDiscoverBleBT(new BleBT05L(device, rssi, rawString, BT05LRegex));
                            } else if (rawString.matches(BT06LRegex)) {
                                mSearchBTBroadcastCallback.onDiscoverBleBT(new BleBT06L(device, rssi, rawString, BT06LRegex));
                            }
                        }
                    }
                }
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };

    private byte[] crc16Modbus(byte[] data) {
        int CRC = 0x0000ffff;
        int POLYNOMIAL = 0x0000a001;

        int i, j;
        for (i = 0; i < data.length; i++) {
            CRC ^= ((int) data[i] & 0x000000ff);
            for (j = 0; j < 8; j++) {
                if ((CRC & 0x00000001) != 0) {
                    CRC >>= 1;
                    CRC ^= POLYNOMIAL;
                } else {
                    CRC >>= 1;
                }
            }
        }
        String s = "0000" + Integer.toHexString(CRC);
        s = s.substring(s.length() - 4);
        return ByteUtils.str2hex(s);
    }

    private byte[] crcAOA(String mac, String raw) {
        byte[] data = new byte[6 + raw.length() / 2];
        byte[] macData = parseMac(mac);
        System.arraycopy(macData, 0, data, 0, 6);
        byte[] rawData = ByteUtils.str2hex(raw);
        System.arraycopy(rawData, 0, data, 6, rawData.length);
        byte[] crc = crc16Modbus(data);
        byte[] reverseCrc = new byte[2];
        reverseCrc[0] = crc[1];
        reverseCrc[1] = crc[0];
        return reverseCrc;
    }

    private byte[] parseMac(String mac) {
        String[] s = mac.split(":");
        if (s.length != 6) {
            throw new NumberFormatException(mac);
        }
        byte[] b = new byte[6];
        for (int i = 0; i < b.length; i ++) {
            b[i] = (byte)Integer.parseInt(s[s.length - i - 1], 16);
        }
        return b;
    }
}
