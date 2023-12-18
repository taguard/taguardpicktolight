package com.bracelet.ble.btxw;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.bracelet.ble.BleBracelet;
import com.bracelet.ble.BleError;
import com.bracelet.ble.BleException;
import com.bracelet.ble.utils.AesECBUtils;
import com.bracelet.ble.utils.RandomUtils;
import com.bracelet.ble.utils.TimeCalibration;

import java.nio.ByteBuffer;

public class BTXW_DeviceImpl implements BTXW_Device {
    //region command code
    private static final byte GetDeviceMac = 0x01;
    private static final byte GetDeviceVersion = 0x02;
    private static final byte GetDevicePower = 0x03;
    private static final byte GetDeviceTime = 0x04;
    private static final byte SetDeviceTime = 0x05;
    private static final byte ExchangeRandom = 0x06;
    private static final byte Authenticate = 0x07;
    private static final byte GetDeviceName = 0x08;
    private static final byte SetDeviceName = 0x09;
    private static final byte ShutDownDevice = 0x0C;
    private static final byte GetDeviceTransmissionPower = 0x0E;
    private static final byte SetDeviceTransmissionPower = 0x0F;
    private static final byte OpenDeviceLight = 0x14;
    private static final byte OpenDeviceBeep = 0x15;
    private static final byte SetBluetoothBroadcastInterval = 0x16;
    private static final byte GetBluetoothBroadcastInterval = 0x17;

    private static final byte ReplyOffset = (byte)0x80;     //receive cmd code = send cmd code + ReplyOffset
    //endregion

    //region variate
    private BleBracelet bracelet;
    private int rssi;
    private Handler handler;
    private boolean connecting;
    private boolean connected;
    private ReadCallback readCallback;
    private BTXW_Parse mBT0XParse;

    private static final int TimeoutMilliseconds = 2000;
    private Runnable timeoutRunnable = new Runnable() {
        @Override
        public void run() {
            mBT0XParse = null;
            ReadCallback callback = readCallback;
            readCallback = null;
            BleException e = new BleException(BleError.COMMUNICATE_NO_REPLY, "Communicate timeout");
            if (callback != null){
                callback.onRead(e, null);
            }
        }
    };
    //endregion

    public BTXW_DeviceImpl(BleBracelet bracelet) {
        this.bracelet = bracelet;
        rssi = bracelet.getRssi();
        this.handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BTXW_DeviceImpl) {
            return ((BTXW_DeviceImpl) obj).bracelet.equals(bracelet);
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return bracelet.hashCode();
    }

    //region implement methods
    @Override
    public String getName() {
        return bracelet.getName();
    }

    @Override
    public String getAddress() {
        return bracelet.getAddress();
    }

    @Override
    public int getRssi() {
        return rssi;
    }

    @Override
    public void updateRssi(int rssi) {
        this.rssi = rssi;
    }

    @Override
    public void connect(Context context, final ConnectionCallback callback) {
        connecting = true;
        bracelet.connect(context, new BleBracelet.ConnectionCallback() {
            @Override
            public void onConnect() {
                connecting = false;
                connected = true;
                readCallback = null;
                if (callback!= null){
                    callback.onConnect();
                }
            }

            @Override
            public void onDisconnect() {
                connecting = false;
                connected = false;
                if (callback != null){
                    callback.onDisconnect();
                }
                readCallback = null;
            }

            @Override
            public void onRead(byte[] bytes) {
                try {
                    if (mBT0XParse == null) {
                        return;
                    }
                    int ret = mBT0XParse.onReply(bytes);
                    if (ret <= BTXW_Parse.CMD_FINISH) {
                        BTXW_Reply reply = null;
                        BleException e = null;
                        if (ret < BTXW_Parse.CMD_FINISH) {
                            e = new BleException(BleError.COMMUNICATE_FAILED, "Communicate failed");
                        } else {
                            reply = mBT0XParse.getReply();
                        }
                        handler.removeCallbacks(timeoutRunnable);
                        mBT0XParse = null;
                        ReadCallback callback = readCallback;
                        readCallback = null;
                        callback.onRead(e, reply);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Log.e("debugLog", String.format("[%s]", ex.getMessage()));
                }
            }
        });
    }

    @Override
    public void disconnect() {
        bracelet.disconnect();
    }

    @Override
    public boolean isConnecting() {
        return connecting;
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public void getDeviceMac(final GetDeviceMacCallback callback) {
        writeData(createCommand(GetDeviceMac,
                toByte((byte)0x01)), new ReadCallback() {
            @Override
            public void onRead(BleException e, BTXW_Reply reply) {
                if (e != null) {
                    callback.onResult(e.getErrorCode(), "");
                } else {
                    byte[] data = reply.getReplyData();
                    StringBuilder mac = new StringBuilder();
                    if (data != null && data.length > 0) {
                        for (byte b : data) {
                            mac.append(String.format("%02X", b));
                            mac.append(":");
                        }
                        mac.deleteCharAt(mac.lastIndexOf(":"));
                    }
                    callback.onResult(0, mac.toString());
                }
            }
        });
    }

    @Override
    public void getDeviceVersion(final GetDeviceVersionCallback callback) {
        writeData(createCommand(GetDeviceVersion,
                toByte((byte)0x01)), new ReadCallback() {
            @Override
            public void onRead(BleException e, BTXW_Reply reply) {
                if (e != null) {
                    callback.onResult(e.getErrorCode(), "");
                } else {
                    callback.onResult(0, new String(reply.getReplyData()).trim());
                }
            }
        });
    }

    @Override
    public void getDevicePower(final GetDevicePowerCallback callback) {
        writeData(createCommand(GetDevicePower,
                toByte((byte)0x01)), new ReadCallback() {
            @Override
            public void onRead(BleException e, BTXW_Reply reply) {
                if (e != null) {
                    callback.onResult(e.getErrorCode(), (short) 0, (byte)0);
                } else {
                    byte[] data = reply.getReplyData();
                    short voltageValue = (short)((data[0] & 0xFF) << 8 | data[1] & 0xFF);
                    byte powerPercent = data[2];
                    callback.onResult(0, voltageValue, powerPercent);
                }
            }
        });
    }

    @Override
    public void getDeviceTime(final GetDeviceTimeCallback callback) {
        writeData(createCommand(GetDeviceTime,
                toByte((byte)0x01)), new ReadCallback() {
            @Override
            public void onRead(BleException e, BTXW_Reply reply) {
                if (e != null) {
                    callback.onResult(e.getErrorCode(), 0);
                } else {
                    byte[] data = reply.getReplyData();
                    long unixTimestamp = (long)(data[0] & 0xFF) << 24
                            | (long)(data[1] & 0xFF) << 16
                            | (long)(data[2] & 0xFF) << 8
                            | (long)(data[3] & 0xFF);
                    long calibratedTimestamp = unixTimestamp - TimeCalibration.getTimeZoneOffset();
                    callback.onResult(0, calibratedTimestamp);
                }
            }
        });
    }

    @Override
    public void setDeviceTime(final SetDeviceTimeCallback callback, long unixTimestampSecond) {
        long calibratedTimestamp = unixTimestampSecond + TimeCalibration.getTimeZoneOffset();
        writeData(createCommand(SetDeviceTime,
                toBytes(calibratedTimestamp)), new ReadCallback() {
            @Override
            public void onRead(BleException e, BTXW_Reply reply) {
                if (e != null) {
                    callback.onResult(e.getErrorCode());
                } else {
                    if (reply.getReplyData()[0] == 0x00) {
                        callback.onResult(0);
                    } else {
                        callback.onResult(BleError.DEVICE_FAILED);
                    }
                }
            }
        });
    }

    @Override
    public void getDeviceName(final GetDeviceNameCallback callback) {
        writeData(createCommand(GetDeviceName,
                toByte((byte)0x01)), new ReadCallback() {
            @Override
            public void onRead(BleException e, BTXW_Reply reply) {
                if (e != null) {
                    callback.onResult(e.getErrorCode(), "");
                } else {
                    callback.onResult(0, new String(reply.getReplyData()).trim());
                }
            }
        });
    }

    @Override
    public void setDeviceName(final SetDeviceNameCallback callback, String name) {
        if (name == null || name.length() > 17 || name.length() == 0) {
            callback.onResult(BleError.INVALID_PARAMETER);
            return;
        }
        byte[] nameBytes = new byte[name.length()];
        for (int i = 0; i < name.length(); i++){
            nameBytes[i] = (byte)(name.charAt(i) & 0xFF);
        }

        writeData(createCommand(SetDeviceName,
                toBytes(nameBytes)), new ReadCallback() {
            @Override
            public void onRead(BleException e, BTXW_Reply reply) {
                if (e != null) {
                    callback.onResult(e.getErrorCode());
                } else {
                    if (reply.getReplyData()[0] == 0x00) {
                        callback.onResult(0);
                    } else {
                        callback.onResult(BleError.DEVICE_FAILED);
                    }
                }
            }
        });
    }

    @Override
    public void exchangeRandom(final ExchangeRandomCallback callback, final byte[] random_m) {
        if (random_m == null || random_m.length != 8) {
            callback.onResult(BleError.INVALID_PARAMETER, new byte[0]);
            return;
        }
        writeData(createCommand(ExchangeRandom,
                toBytes(random_m),
                toBytes(new byte[9])), new ReadCallback() {
            @Override
            public void onRead(BleException e, BTXW_Reply reply) {
                if (e != null) {
                    callback.onResult(e.getErrorCode(), new byte[0]);
                } else {
                    byte[] random_s = new byte[8];
                    System.arraycopy(reply.getReplyData(), 0, random_s, 0, random_s.length);
                    callback.onResult(0, random_s);
                }
            }
        });
    }

    @Override
    public void authenticate(final AuthenticateCallback callback, final byte[] authData) {
        if (authData == null || authData.length != 16) {
            callback.onResult(BleError.INVALID_PARAMETER, (byte)0xFF);
            return;
        }
        writeData(createCommand(Authenticate,
                toBytes(authData),
                toBytes(new byte[1])), new ReadCallback() {
            @Override
            public void onRead(BleException e, BTXW_Reply reply) {
                if (e != null) {
                    callback.onResult(e.getErrorCode(), (byte)0xFF);
                } else {
                    byte authResult = reply.getReplyData()[16];
                    if (authResult == (byte) 0x00) {
                        callback.onResult(0, authResult);
                    } else {
                        callback.onResult(BleError.DEVICE_FAILED, authResult);
                    }
                }
            }
        });
    }

    @Override
    public void autoAuthenticate(final AutoAuthenticateCallback callback) {
        //generate random_m by phone
        final byte[] random_m = RandomUtils.generateRandom(8);
        //send ExchangeRandom command
        exchangeRandom(new ExchangeRandomCallback() {
            @Override
            public void onResult(int status, byte[] random_s) {
                if (status == 0) {
                    //joint random_m and random_s
                    byte[] random = new byte[16];
                    System.arraycopy(random_m, 0, random, 0, random_m.length);
                    System.arraycopy(random_s, 0, random, random_m.length, random_s.length);
                    //generate authData by AES128 algorithm
                    byte[] authData = new byte[16];
                    byte[] encryptedData = AesECBUtils.encryptByDeviceKey(random);
                    System.arraycopy(encryptedData, 0, authData, 0, authData.length);
                    authenticate(new AuthenticateCallback() {
                        @Override
                        public void onResult(int status, byte authResult) {
                            callback.onResult(status, authResult);
                        }
                    }, authData);
                } else {
                    callback.onResult(status, (byte)0xFF);
                }
            }
        }, random_m);
    }


    @Override
    public void shutDownDevice(final ShutDownDeviceCallback callback) {
        writeData(createCommand(ShutDownDevice,
                toByte((byte)0x01)), new ReadCallback() {
            @Override
            public void onRead(BleException e, BTXW_Reply reply) {
                if (e != null) {
                    callback.onResult(e.getErrorCode());
                } else {
                    callback.onResult(0);
                }
            }
        });
    }

    @Override
    public void getDeviceTransmissionPower(final GetDeviceTransmissionPowerCallback callback) {
        writeData(createCommand(GetDeviceTransmissionPower,
                toByte((byte)0x01)), new ReadCallback() {
            @Override
            public void onRead(BleException e, BTXW_Reply reply) {
                if (e != null) {
                    callback.onResult(e.getErrorCode(), (byte)0xFF);
                } else {
                    byte transmissionPower = reply.getReplyData()[0];
                    callback.onResult(0, transmissionPower);
                }
            }
        });
    }

    @Override
    public void setDeviceTransmissionPower(final SetDeviceTransmissionPowerCallback callback, byte transmissionPower) {
        writeData(createCommand(SetDeviceTransmissionPower,
                toByte((byte)0xAA),
                toByte(transmissionPower)), new ReadCallback() {
            @Override
            public void onRead(BleException e, BTXW_Reply reply) {
                if (e != null) {
                    callback.onResult(e.getErrorCode());
                } else {
                    if (reply.getReplyData()[0] == 0x00) {
                        callback.onResult(0);
                    } else {
                        callback.onResult(BleError.DEVICE_FAILED);
                    }
                }
            }
        });
    }

    @Override
    public void openDeviceLight(final OpenDeviceLightCallback callback, byte openSeconds) {
        writeData(createCommand(OpenDeviceLight,
                toByte((byte)0),
                toByte(openSeconds)), new ReadCallback() {
            @Override
            public void onRead(BleException e, BTXW_Reply reply) {
                if (e != null) {
                    callback.onResult(e.getErrorCode());
                } else {
                    if (reply.getReplyData()[0] == 0x00) {
                        callback.onResult(0);
                    } else {
                        callback.onResult(BleError.DEVICE_FAILED);
                    }
                }
            }
        });
    }

    @Override
    public void openDeviceTwoLights(final OpenDeviceLightCallback callback, byte lightType, byte openSeconds) {
        writeData(createCommand(OpenDeviceLight,
                toByte(lightType),
                toByte(openSeconds)), new ReadCallback() {
            @Override
            public void onRead(BleException e, BTXW_Reply reply) {
                if (e != null) {
                    callback.onResult(e.getErrorCode());
                } else {
                    if (reply.getReplyData()[0] == 0x00) {
                        callback.onResult(0);
                    } else {
                        callback.onResult(BleError.DEVICE_FAILED);
                    }
                }
            }
        });
    }

    @Override
    public void openDeviceMultipleLights(final OpenDeviceLightCallback callback, byte[] lightGroup, byte openSeconds) {
        byte lightType = 0;
        if (lightGroup != null && lightGroup.length > 0) {
            for (byte light : lightGroup) {
                if (light == LIGHT_ALL || light == LIGHT_PURPLE_GROUP || light == LIGHT_RED_GROUP
                        || light == LIGHT_WHITE_GROUP || light == LIGHT_YELLOW_GROUP
                        || light == LIGHT_BLUE_GROUP || light == LIGHT_ORANGE_GROUP) {
                    lightType = (byte)(lightType | light);
                } else {
                    callback.onResult(BleError.INVALID_PARAMETER);
                    return;
                }
            }
        }
        writeData(createCommand(OpenDeviceLight,
                toByte(lightType),
                toByte(openSeconds)), new ReadCallback() {
            @Override
            public void onRead(BleException e, BTXW_Reply reply) {
                if (e != null) {
                    callback.onResult(e.getErrorCode());
                } else {
                    if (reply.getReplyData()[0] == 0x00) {
                        callback.onResult(0);
                    } else {
                        callback.onResult(BleError.DEVICE_FAILED);
                    }
                }
            }
        });
    }

    @Override
    public void openDeviceBeep(final OpenDeviceBeepCallback callback, byte openSeconds) {
        writeData(createCommand(OpenDeviceBeep,
                toByte((byte)0),
                toByte(openSeconds)), new ReadCallback() {
            @Override
            public void onRead(BleException e, BTXW_Reply reply) {
                if (e != null) {
                    callback.onResult(e.getErrorCode());
                } else {
                    if (reply.getReplyData()[0] == 0x00) {
                        callback.onResult(0);
                    } else {
                        callback.onResult(BleError.DEVICE_FAILED);
                    }
                }
            }
        });
    }

    @Override
    public void setBluetoothBroadcastInterval(final SetBluetoothBroadcastIntervalCallback callback, byte interval) {
        if (interval < 5 || interval > 100) {
            callback.onResult(BleError.INVALID_PARAMETER);
            return;
        }
        writeData(createCommand(SetBluetoothBroadcastInterval,
                toByte((byte)0),
                toByte(interval)), new ReadCallback() {
            @Override
            public void onRead(BleException e, BTXW_Reply reply) {
                if (e != null) {
                    callback.onResult(e.getErrorCode());
                } else {
                    if (reply.getReplyData()[0] == 0x00) {
                        callback.onResult(0);
                    } else {
                        callback.onResult(BleError.DEVICE_FAILED);
                    }
                }
            }
        });
    }

    @Override
    public void getBluetoothBroadcastInterval(final GetBluetoothBroadcastIntervalCallback callback) {
        writeData(createCommand(GetBluetoothBroadcastInterval,
                toByte((byte)0x01)), new ReadCallback() {
            @Override
            public void onRead(BleException e, BTXW_Reply reply) {
                if (e != null) {
                    callback.onResult(e.getErrorCode(), (byte)0xFF);
                } else {
                    byte interval = reply.getReplyData()[0];
                    callback.onResult(0, interval);
                }
            }
        });
    }

    //endregion

    //region create command
    private byte[] createCommand(byte commandCode, Param... args) {
        byte length = 0;
        for (Param arg : args) length += arg.length();
        ByteBuffer buffer = ByteBuffer.allocate(length + 3);
        buffer.put(commandCode);
        buffer.put(length);
        for (Param arg : args) buffer.put(arg.data());
        buffer.put(BTXW_Check.sumBuffer(buffer.array(), buffer.array().length - 1));
        return buffer.array();
    }

    private void writeData(byte[] cmd, ReadCallback callback) {
        if (!connected) {
            BleException e = new BleException(BleError.DEVICE_NOT_OPENED, "Device not opened");
            callback.onRead(e, null);
            return;
        }
        if (readCallback != null) {
            BleException e = new BleException(BleError.DEVICE_BUSY, "Device busy");
            callback.onRead(e, null);
            return;
        }
        readCallback = callback;
        mBT0XParse = new BTXW_Parse(cmd[0]);
        handler.postDelayed(timeoutRunnable, TimeoutMilliseconds);
        bracelet.writeBytes(cmd);
    }

    interface Param {
        int length();
        byte[] data();
    }

    private static Param toByte(final byte b) {
        return new Param() {
            @Override
            public int length() {
                return 1;
            }

            @Override
            public byte[] data() {
                return new byte[]{b};
            }
        };
    }

    private static Param toBytes(final short s) {
        return new Param() {
            @Override
            public int length() {
                return 2;
            }

            @Override
            public byte[] data() {
                return new byte[]{
                        (byte) (s >> 8),
                        (byte) s
                };
            }
        };
    }

    private static Param toBytes(final long l) {
        return new Param() {
            @Override
            public int length() {
                return 4;
            }

            @Override
            public byte[] data() {
                return new byte[]{
                        (byte) (l >> 24),
                        (byte) (l >> 16),
                        (byte) (l >> 8),
                        (byte) l
                };
            }
        };
    }

    private static Param toBytes(final byte[] bytes){
        return new Param() {
            @Override
            public int length() {
                return bytes.length;
            }

            @Override
            public byte[] data() {
                return bytes;
            }
        };
    }

    private static Param toBytes(final float f){
        return new Param() {
            @Override
            public int length() {
                return 2;
            }

            @Override
            public byte[] data() {
                int i = (int)(f * 10);
                return new byte[]{(byte)(i >> 8), (byte)i};
            }
        };
    }
    //endregion

    interface ReadCallback {
        void onRead(BleException e, BTXW_Reply reply);
    }
}
