package com.bracelet.ble.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimeCalibration {

    public static int getTimeZoneOffset() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"),
                Locale.getDefault());
        Date currentLocalTime = calendar.getTime();
        DateFormat date = new SimpleDateFormat("Z");
        //GMT: +0800  -0700 -0430
        String GMT = date.format(currentLocalTime);
        try {
            int gmt = Integer.parseInt(GMT);
            int GMTOffset = (gmt / 100 * 60 + gmt % 100) * 60;
            //BT04 display time in timezone GMT+8
            return GMTOffset - 8 * 60 * 60;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

}
