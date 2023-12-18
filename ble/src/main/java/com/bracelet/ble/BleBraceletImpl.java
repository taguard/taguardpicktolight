package com.bracelet.ble;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bracelet.ble.utils.ByteUtils;
import com.bracelet.ble.utils.DebugLog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class BleBraceletImpl implements BleBracelet {
    private static final UUID TRANSFER_SERVICE_UUID = UUID.fromString("0000ff00-0000-1000-8000-00805f9b34fb");
    private static final UUID MODULE_TO_PHONE_UUID = UUID.fromString("0000ff01-0000-1000-8000-00805f9b34fb");
    private static final UUID PHONE_TO_MODULE_UUID = UUID.fromString("0000ff02-0000-1000-8000-00805f9b34fb");
    private static final UUID CCC = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private ConnectionCallback callback;
    private BluetoothDevice device;
    private BluetoothGatt gatt;
    private BluetoothGattService transferService;
    private BluetoothGattCharacteristic moduleToPhoneCharacteristic;
    private BluetoothGattCharacteristic phoneToModuleCharacteristic;
    private Handler handler;
    private int rssi;
    private List<byte[]> packets;
    private Context mContext;
    private LocalBroadcastManager mLocalBroadcastManager;
    private SimpleDateFormat mTimeFormat;

    //in sometimes, some phones don't handle connectGatt timeout(30s) on the first call
    private static final int ConnectTimeoutMilliseconds = 31 * 1000;
    private Runnable connectTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            onDisconnect();
            onDisconnectSucceed();
        }
    };

    public BleBraceletImpl(Context context, BluetoothDevice device, int rssi) {
        mContext = context;
        this.device = device;
        this.rssi = rssi;
        this.handler = new Handler(Looper.getMainLooper());
        this.packets = new ArrayList<>();
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(mContext);
        mTimeFormat = new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault());
    }

    @Override
    public String getName() {
        return device == null ? "" : device.getName();
    }

    @Override
    public String getAddress() {
        return device == null ? "" : device.getAddress();
    }

    @Override
    public int getRssi() {
        return rssi;
    }

    @Override
    public synchronized void connect(Context context, ConnectionCallback callback) {
        if (this.gatt == null) {
            this.callback = callback;
            this.gatt = device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE);
            if (BuildConfig.DEBUG) {
                Intent intent = new Intent(DebugLog.BROADCAST_CONNECT);
                intent.putExtra(DebugLog.EXTRA_LOG,
                        String.format("%s BluetoothGatt start to connect", mTimeFormat.format(new Date())));
                mLocalBroadcastManager.sendBroadcast(intent);
            }
        }
        handler.postDelayed(connectTimeoutRunnable, ConnectTimeoutMilliseconds);
    }

    @Override
    public synchronized void disconnect() {
        if (gatt != null) {
            gatt.disconnect();
            handler.removeCallbacks(connectTimeoutRunnable);
        }
    }

    @Override
    public void writeBytes(byte[] bytes) {
        packets.clear();
        int packetSize = 20;
        for (int i=0; i<bytes.length; i+= packetSize) {
            byte[] packet = new byte[Math.min(packetSize, bytes.length - i)];
            System.arraycopy(bytes, i, packet, 0, packet.length);
            packets.add(packet);
        }
        writeNextPacket();
    }

    private void writeNextPacket() {
        if (packets.size() > 0) {
            byte[] bytes = packets.get(0);
            packets.remove(0);
            phoneToModuleCharacteristic.setValue(bytes);
            if (gatt !=null){
                gatt.writeCharacteristic(phoneToModuleCharacteristic);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof BleBraceletImpl) {
            return ((BleBraceletImpl)o).device.equals(device);
        }
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return device.hashCode();
    }

    @Override
    public String toString() {
        return device.getAddress() + " - " + device.getName();
    }

    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, final int status, final int newState) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    onConnectionStateChangeImpl(status, newState);
                }
            });
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, final int status) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    onServicesDiscoveredImpl(status);
                }
            });
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, final BluetoothGattDescriptor descriptor, final int status) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    onDescriptorWriteImpl(descriptor, status);
                }
            });
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            final byte[] bytes = characteristic.getValue();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    onCharacteristicChangedImpl(bytes);
                }
            });
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, final int status) {
            final byte[] bytes = characteristic.getValue();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    onCharacteristicWriteImpl(bytes, status);
                }
            });
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
        }
    };

    private void onConnectionStateChangeImpl(int status, int newState) {
        if (status != BluetoothGatt.GATT_SUCCESS) {
            onDisconnect();
            if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                onDisconnectSucceed();
            }
        } else {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.discoverServices();
                if (BuildConfig.DEBUG) {
                    Intent intent = new Intent(DebugLog.BROADCAST_CONNECT);
                    intent.putExtra(DebugLog.EXTRA_LOG,
                            String.format("%s BluetoothProfile STATE_CONNECTED", mTimeFormat.format(new Date())));
                    mLocalBroadcastManager.sendBroadcast(intent);
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                onDisconnectSucceed();
            }
        }
    }

    private void onServicesDiscoveredImpl(int status) {
        if (status != BluetoothGatt.GATT_SUCCESS) {
            onDisconnect();
        } else {
            transferService = gatt.getService(TRANSFER_SERVICE_UUID);
            if (transferService == null) {
                onDisconnect("transfer Service cannot be found");
                return;
            }
            moduleToPhoneCharacteristic = transferService.getCharacteristic(MODULE_TO_PHONE_UUID);
            if (moduleToPhoneCharacteristic == null) {
                onDisconnect("ModuleToPhone Characteristic cannot be found");
                return;
            }
            phoneToModuleCharacteristic = transferService.getCharacteristic(PHONE_TO_MODULE_UUID);
            if (phoneToModuleCharacteristic == null) {
                onDisconnect("PhoneToModule Characteristic cannot be found");
                return;
            }
            if (BuildConfig.DEBUG) {
                Intent intent = new Intent(DebugLog.BROADCAST_CONNECT);
                intent.putExtra(DebugLog.EXTRA_LOG,
                        String.format("%s BluetoothGatt discovered services", mTimeFormat.format(new Date())));
                mLocalBroadcastManager.sendBroadcast(intent);
            }
            if (!setAcsNotification(moduleToPhoneCharacteristic)) {
                onDisconnect("ModuleToPhone Characteristic set notification failed");
                return;
            }
        }
    }

    private boolean setAcsNotification(BluetoothGattCharacteristic characteristic) {
        gatt.setCharacteristicNotification(characteristic, true);
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CCC);
        if (descriptor != null) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            boolean isSuc = gatt.writeDescriptor(descriptor);
            Log.i("debugLog", String.format("writeDescriptor[%s]", isSuc));
            if (BuildConfig.DEBUG) {
                Intent intent = new Intent(DebugLog.BROADCAST_CONNECT);
                intent.putExtra(DebugLog.EXTRA_LOG,
                        String.format("%s BluetoothGatt start to write descriptor", mTimeFormat.format(new Date())));
                mLocalBroadcastManager.sendBroadcast(intent);
            }
            return isSuc;
        } else {
            return false;
        }
    }

    private void onDescriptorWriteImpl(BluetoothGattDescriptor descriptor, int status) {
        if (status != BluetoothGatt.GATT_SUCCESS) {
            onDisconnect();
        } else {
            if (descriptor.getCharacteristic().getUuid().equals(MODULE_TO_PHONE_UUID)) {
                // Note: BT0X could't enable notification of ff02.
                onConnect();
            }
        }
    }

    private void onCharacteristicChangedImpl(byte[] bytes) {
        Log.i("debugLog", "onCharacteristicChange: <<- " + ByteUtils.hex2str(bytes));
        if (BuildConfig.DEBUG) {
            Intent intent = new Intent(DebugLog.BROADCAST_RECEIVE);
            intent.putExtra(DebugLog.EXTRA_LOG,
                    String.format("%s <<- %s", mTimeFormat.format(new Date()),
                            ByteUtils.hex2str(bytes)));
            mLocalBroadcastManager.sendBroadcast(intent);
        }
        callback.onRead(bytes);
    }

    private void onCharacteristicWriteImpl(byte[] bytes, int status) {
        Log.i("debugLog", "onCharacteristicWrite:  ->> " + ByteUtils.hex2str(bytes));
        if (BuildConfig.DEBUG) {
            Intent intent = new Intent(DebugLog.BROADCAST_SEND);
            intent.putExtra(DebugLog.EXTRA_LOG,
                    String.format("%s ->> %s", mTimeFormat.format(new Date()),
                            ByteUtils.hex2str(bytes)));
            mLocalBroadcastManager.sendBroadcast(intent);
        }
        writeNextPacket();
    }

    private void onConnect() {
        handler.removeCallbacks(connectTimeoutRunnable);
        callback.onConnect();
        if (BuildConfig.DEBUG) {
            Intent intent = new Intent(DebugLog.BROADCAST_CONNECT);
            intent.putExtra(DebugLog.EXTRA_LOG,
                    String.format("\r\n%s Bluetooth connected", mTimeFormat.format(new Date())));
            mLocalBroadcastManager.sendBroadcast(intent);
        }
    }

    private void onDisconnect() {
        disconnect();
    }

    private void onDisconnect(String format, Object... args) {
        Log.w("debugLog", String.format(format, args));
        disconnect();
    }

    private synchronized void onDisconnectSucceed() {
        if (gatt != null) {
            gatt.close();
        }
        gatt = null;
        handler.post(new Runnable() {
            @Override
            public void run() {
                callback.onDisconnect();
                if (BuildConfig.DEBUG) {
                    Intent intent = new Intent(DebugLog.BROADCAST_DISCONNECT);
                    intent.putExtra(DebugLog.EXTRA_LOG,
                            String.format("%s Bluetooth disconnected", mTimeFormat.format(new Date())));
                    mLocalBroadcastManager.sendBroadcast(intent);
                }
            }
        });
    }
}
