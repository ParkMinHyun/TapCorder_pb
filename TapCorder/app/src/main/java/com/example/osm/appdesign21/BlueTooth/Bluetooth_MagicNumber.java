package com.example.osm.appdesign21.BlueTooth;

/**
 * Created by BlackMan on 2016-09-28.
 */
public class Bluetooth_MagicNumber {

    /* 블루투스에 관한 상수 */
    public static final boolean D = true;

    // 블루투스 핸들러가 어떤 상태인지
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // 블루투스 핸들러에서 받은 Key 이름
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Device 블루투스 상태
    public static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    public static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    public static final int REQUEST_ENABLE_BT = 3;


    // 현재 블루투스 상태
    public static final int BCSTATE_NONE = 0;       // we're doing nothing
    public static final int BCSTATE_LISTEN = 1;     // now listening for incoming connections
    public static final int BCSTATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int BCSTATE_CONNECTED = 3;  // now connected to a remote device
}
