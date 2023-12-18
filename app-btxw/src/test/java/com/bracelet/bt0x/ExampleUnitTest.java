package com.bracelet.bt0x;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

import com.bracelet.ble.utils.ByteUtils;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {

        String[] crcList = new String[] {
                "00:51:8A:DB:A0:40"
                };
        byte[] crc;
        String rawData = "0x0400000000";
        String finalData = "2F61ACCC274567F7DB34C4038E5C0BAA973056e6";
        int index = 0 ;
        for (String value : crcList) {
            try {
                //截取CRC前的内容计算CRC
                crc = crcAOA(value, "1EFF0D000400000000");
                index ++ ;
            } catch (Exception ex) {
                ex.printStackTrace();
                System.out.println(index + "error");
                return;
            }
            String s = ByteUtils.hex2str(crc, "");
            System.out.println(rawData + s + finalData);
        }
    }

    public byte[] crcAOA(String mac, String raw) {
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

    public byte[] crc16Modbus(byte[] data) {
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
    @Test
    public void Test1(){
        byte[] crc;
        String rawData = "1EFF0D000400000000";
        String finalData = "2F61ACCC274567F7DB34C4038E5C0BAA973056e6";
        try {
            //截取CRC前的内容计算CRC
            crc = crcAOA("00:51:8A:DB:A0:40", "1EFF0D000400000000");
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
        String s = ByteUtils.hex2str(crc, "");
        System.out.println(rawData + s + finalData);
    }
}