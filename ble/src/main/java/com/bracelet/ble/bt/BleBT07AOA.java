package com.bracelet.ble.bt;

import android.bluetooth.BluetoothDevice;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BleBT07AOA extends AbstractBleBT {

   private boolean buttonPressed;   //1byte, bit4: button, (up:0, down:1)
   private int version;             //1byte, dec
   private int voltage;             //1byte, (value * 100)mV

   public BleBT07AOA(BluetoothDevice device, int rssi, String rawString, String regex) {
      super(device, rssi, rawString, regex);
      Pattern pattern = Pattern.compile(regex);
      Matcher matcher = pattern.matcher(rawString);
      if (matcher.find() && matcher.groupCount() == 3) {
         buttonPressed = parseButtonPressed(matcher.group(1));
         version = parseVersion(matcher.group(2));
         voltage = parseVoltage(matcher.group(3));
      }
   }

   @Override
   public String getTypeName() {
      return BT07AOATypeName;
   }

   public boolean isButtonPressed() {
      return buttonPressed;
   }

   public int getVersion() {
      return version;
   }

   public int getVoltage() {
      return voltage;
   }

   private boolean parseButtonPressed(String data) {
      int value = Integer.parseInt(data, 16);
      return (value & 0x10) == 0x10;
   }

   private int parseVersion(String data) {
      return Integer.parseInt(data, 16);
   }

   private int parseVoltage(String data) {
      return Integer.parseInt(data, 16) * 100;
   }
}
