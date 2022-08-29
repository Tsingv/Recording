package com.example.recording;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MyTimeUtils {
    public static long getTimeLong() {
        return System.currentTimeMillis();
    }

    public static int getTimeInt(String filter) {
        SimpleDateFormat format = new SimpleDateFormat(filter);
        String time = format.format(new Date());
        return Integer.parseInt(time);
    }


    public static final String getTimeString() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(new Date(getTimeLong()));
    }

    public static final String getTimeString(long time) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(new Date(time));
    }

    public static final String getTimeString(long time, String filter) {
        SimpleDateFormat format = new SimpleDateFormat(filter);
        return format.format(new Date(time));
    }

    public static final String getTimeString(String filter) {
        SimpleDateFormat format = new SimpleDateFormat(filter);
        return format.format(new Date(getTimeLong()));
    }

    public static Long getTimeLong(String filter, String date) {
        try {
            SimpleDateFormat format = new SimpleDateFormat(filter);
            Date dateTime = format.parse(date);
            return dateTime.getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0L;
    }
}
