package com.example.osm.appdesign21;


public class TimeItem extends AdapterItem {

    public TimeItem(long time) {
        super(time);
    }

    public TimeItem(int year, int month, int dayOfMonth,int hour,int minute, int second) {
        super(year, month, dayOfMonth,hour,minute,second);
    }

    @Override
    public int getType() {
        return TYPE_TIME;
    }
}
