package com.example.osm.appdesign21;


public class MyData extends AdapterItem {
    private String name;

    public MyData(String name, long time) {
        super(time);
        this.name = name;
    }

    public MyData(String name, int year, int month, int dayOfMonth,int hour,int minute, int second) {
        super(year, month, dayOfMonth,hour,minute,second);
        this.name = name;
    }

    @Override
    public int getType() {
        return TYPE_DATA;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
