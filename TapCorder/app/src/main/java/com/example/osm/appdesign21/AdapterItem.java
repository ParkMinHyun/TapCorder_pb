package com.example.osm.appdesign21;

import java.util.Calendar;


public abstract class AdapterItem {
    public static final int TYPE_TIME = 1;
    public static final int TYPE_DATA = 2;

    private long time;

    public AdapterItem(long time) {
        this.time = time;
    }

    public AdapterItem(int year, int month, int dayOfMonth,int hour,int minute,int second) {
        setTime(year, month, dayOfMonth,hour,minute,second);
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setTime(int year, int month, int dayOfMonth,int hour,int minute,int second) {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(year, month-1, dayOfMonth,hour,minute,second);
        time = cal.getTimeInMillis();
    }

    public int getYear() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        return cal.get(Calendar.YEAR);
    }

    public int getMonth() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        return cal.get(Calendar.MONTH)+1;
    }

    public int getDayOfMonth() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        return cal.get(Calendar.DAY_OF_MONTH);
    }

    public int getHour(){
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        return cal.get(Calendar.HOUR_OF_DAY);
    }


    public int getMinute(){
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        return cal.get(Calendar.MINUTE);
    }


    public int getSecond(){
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        return cal.get(Calendar.SECOND);
    }

    public long getTime() {
        return time;
    }

    public String getLongTimeToString() {
        return getHour()+"시 "+getMinute()+"분 "+getSecond()+"초";
    }

    public String getShortTimeToString() {
        return getYear() + "년 " + (getMonth()+1) + "월 " + getDayOfMonth()+"일";
    }

    public abstract int getType();

}
