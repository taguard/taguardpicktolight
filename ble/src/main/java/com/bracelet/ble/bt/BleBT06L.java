package com.bracelet.ble.bt;

import android.bluetooth.BluetoothDevice;
import android.text.TextUtils;

import com.bracelet.ble.utils.ByteUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BleBT06L extends AbstractBleBT {

   private String bleName;             //8bytes, (ASCII)
   private double voltage;             //1byte, (value * 0.03125)V
   private double temperature;         //2bytes, ((value2 << 8 | value1) * 0.01)℃
   private long powerOnTime;           //4bytes, (value4 << 24 | value3 << 16 | value2 << 8 | value1)s
   private boolean buttonPressed;      //1byte, (up:0xFF, down:0xDF)

   public BleBT06L(BluetoothDevice device, int rssi, String rawString, String regex) {
      super(device, rssi, rawString, regex);
      Pattern pattern = Pattern.compile(regex);
      Matcher matcher = pattern.matcher(rawString);
      if (matcher.find() && matcher.groupCount() == 5) {
         bleName = parseBleName(matcher.group(1));
         voltage = parseVoltage(matcher.group(2));
         temperature = parseTemperature(matcher.group(3));
         powerOnTime = parsePowerOnTime(matcher.group(4));
         buttonPressed = parseButtonPressed(matcher.group(5));
      }
   }

   @Override
   public String getTypeName() {
      return BT06LTypeName;
   }

   public String getBleName() {
      return bleName;
   }

   public double getVoltage() {
      return voltage;
   }

   public double getTemperature() {
      return temperature;
   }

   public long getPowerOnTime() {
      return powerOnTime;
   }

   public boolean isButtonPressed() {
      return buttonPressed;
   }

   private String parseBleName(String data) {
      return new String(ByteUtils.str2hex(data));
   }

   private double parseVoltage(String data) {
      double v = Integer.parseInt(data, 16) * 0.03125;
      return new BigDecimal(v).setScale(5, RoundingMode.HALF_UP).doubleValue();
   }

   private double parseTemperature(String data) {
      double t;
      int value1 = Integer.parseInt(data.substring(0, 2), 16);
      int value2 = Integer.parseInt(data.substring(2, 4), 16);
      t = ((value2 << 8) | value1) * 0.01;
      return new BigDecimal(t).setScale(2, RoundingMode.HALF_UP).doubleValue();
   }

   private long parsePowerOnTime(String data) {
      long t;
      long value1 = Long.parseLong(data.substring(0, 2), 16);
      long value2 = Long.parseLong(data.substring(2, 4), 16);
      long value3 = Long.parseLong(data.substring(4, 6), 16);
      long value4 = Long.parseLong(data.substring(6, 8), 16);
      t = (value4 << 24) | (value3 << 16) | (value2 << 8) | value1;
      return t;
   }

   private boolean parseButtonPressed(String data) {
      return TextUtils.equals("DF", data);
   }
}
