package com.bracelet.ble.bt;

import android.bluetooth.BluetoothDevice;

public class BleBT11AOA extends AbstractBleBT {

   public BleBT11AOA(BluetoothDevice device, int rssi, String rawString, String regex) {
      super(device, rssi, rawString, regex);
   }

   @Override
   public String getTypeName() {
      return BT11AOATypeName;
   }
}
