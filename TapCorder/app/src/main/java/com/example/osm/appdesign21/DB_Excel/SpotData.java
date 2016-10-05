package com.example.osm.appdesign21.DB_Excel;

public class SpotData {

    private String no;
    private String name;
    private String addr;
    private String tel_num;
    private double spot_x;
    private double spot_y;

    public SpotData() {
    }

    public SpotData(String no, String name, String addr, String tel_num) {
        this.no = no;
        this.name = name;
        this.addr = addr;
        this.tel_num = tel_num;
    }

    public SpotData(String no, String name, String addr, String tel_num,
                    double spot_x, double spot_y) {
        this.no = no;
        this.name = name;
        this.addr = addr;
        this.tel_num = tel_num;
        this.spot_x = spot_x;
        this.spot_y = spot_y;
    }

    public String get_no() {
        return no;
    }

    public String get_name() {
        return name;
    }

    public String get_addr() {
        return addr;
    }

    public String get_tel_num() {
        return tel_num;
    }

    public Double get_spot_x() {
        return spot_x;
    }

    public Double get_spot_y() {
        return spot_y;
    }

    public void set_spot_x(double spot_x) {
        this.spot_x = spot_x;
    }

    public void set_spot_y(double spot_y) {
        this.spot_y = spot_y;
    }


}
