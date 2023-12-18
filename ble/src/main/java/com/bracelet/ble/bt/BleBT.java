package com.bracelet.ble.bt;

public interface BleBT {

    byte BT05 = 0x01;
    byte BT05L = 0x02;
    byte BT06AOA = 0x03;
    byte BT06L = 0x04;
    byte BT06LAOA = 0x05;
    byte BT07AOA = 0x06;
    byte BT11AOA = 0x07;

    String BT05TypeName = "BT05";
    String BT05LTypeName = "BT05L";
    String BT06AOATypeName = "BT06-AOA";
    String BT06LTypeName = "BT06L";
    String BT06LAOATypeName = "BT06L-AOA";
    String BT07AOATypeName = "BT07-AOA";
    String BT11AOATypeName = "BT11-AOA";

    String getName();

    String getAddress();

    int getRssi();

    String getRawString();

    String getRegex();

    long getSearedTimestamp();

    String getTypeName();
}
