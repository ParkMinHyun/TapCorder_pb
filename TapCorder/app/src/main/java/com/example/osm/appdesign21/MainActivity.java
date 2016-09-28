package com.example.osm.appdesign21;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.osm.appdesign21.BlueTooth.BluetoothChatService;
import com.example.osm.appdesign21.BlueTooth.Bluetooth_MagicNumber;
import com.example.osm.appdesign21.BlueTooth.DeviceListActivity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;


public class MainActivity extends ActionBarActivity implements MediaPlayer.OnCompletionListener, TimeRecyclerAdapter.OnItemClickListener {

    private TimeRecyclerAdapter adapter;
    private static String TAG = "MainActivity";
    private PopupWindow pwindo;
    private Button btnClosePopup;
    private int mWidthPixels, mHeightPixels;
    DisplayMetrics mMetrics;
    private RadioButton option1, option2, option3;

    TextView contentsText;
    Geocoder gc;

    //미리 상수 선언
    private static final int PLAY_STOP = 0;
    private static final int PLAYING = 1;
    private static final int PLAY_PAUSE = 2;

    private MediaPlayer mPlayer = null;
    private int mPlayerState = PLAY_STOP;
    private String mFilePath; //녹음파일 디렉터리 위치

    private FloatingActionButton fabButton_set,fabButton_addr;
    private RecyclerView mTimeRecyclerView;

    // 연락처 ListView
    private ListView lvPhone;
    private Button mbtnAddContact;
    private Button mbtnDeleteContact;

    SharedPreferences pref;
    ArrayList<PhoneBook> saveList;
    FrameLayout layout_MainMenu;

    /*블루투스에 관한 것들*/
    private  boolean first_start = false;
    // 연결된 디바이스의 이름
    private String mConnectedDeviceName = null;
    // thread 소통을 위한 ArrayAdapter
    private ArrayAdapter<String> mConversationArrayAdapter;
    // 송신을 위한 outGoing StringBuffer
    private StringBuffer mOutStringBuffer;
    // 블루투스 어댑터
    private BluetoothAdapter mBluetoothAdapter = null;
    // 블루투스챗 서비스 클래스
    private BluetoothChatService mChatService = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //아답터 얻기
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // 만약 어댑터가 null이면 블루투스 종료
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        gc = new Geocoder(this, Locale.KOREAN);// 지오코더 객체 생성


        fabButton_set =(FloatingActionButton)findViewById(R.id.fab_settings);
        fabButton_addr=(FloatingActionButton)findViewById(R.id.fab_phoneaddr);
        initFab();//FloatingButton Click에 따른 메서드

        mTimeRecyclerView = (RecyclerView) findViewById(R.id.mTimeRecyclerView);
        mTimeRecyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mTimeRecyclerView.setLayoutManager(layoutManager);

        adapter = new TimeRecyclerAdapter(getDataset());
        adapter.setOnItemClickListener(this);
        mTimeRecyclerView.setAdapter(adapter);

        mMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(mMetrics);

        WindowManager w = getWindowManager();
        Display d = w.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        d.getMetrics(metrics);
        // since SDK_INT = 1;
        mWidthPixels = metrics.widthPixels;
        mHeightPixels = metrics.heightPixels;

        // 상태바와 메뉴바의 크기를 포함해서 재계산
        if (Build.VERSION.SDK_INT >= 14 && Build.VERSION.SDK_INT < 17)
            try {
                mWidthPixels = (Integer) Display.class.getMethod("getRawWidth").invoke(d);
                mHeightPixels = (Integer) Display.class.getMethod("getRawHeight").invoke(d);
            } catch (Exception ignored) {
            }
        // 상태바와 메뉴바의 크기를 포함
        if (Build.VERSION.SDK_INT >= 17)
            try {
                Point realSize = new Point();
                Display.class.getMethod("getRealSize", Point.class).invoke(d, realSize);
                mWidthPixels = realSize.x;
                mHeightPixels = realSize.y;
            } catch (Exception ignored) {
            }

        saveList = new ArrayList<>();
        String name;
        String phoneNumber;
        pref = new SharedPreferences(this);
        for(int i = 0; i < 5; i++){
            name = pref.getValue(Integer.toString(i), "no", "name");
            phoneNumber = pref.getValue(Integer.toString(i), "no", "phoneNum");
            if(!name.equals("no")){
                saveList.add(new PhoneBook(name, phoneNumber));
            }
        }
        layout_MainMenu = (FrameLayout) findViewById(R.id.mainmenu);
        layout_MainMenu.getForeground().setAlpha( 0);
    }

    @Override
    public void onStart() {
        super.onStart();
        if(Bluetooth_MagicNumber.D) Log.e(TAG, "++ ON START ++");

        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, Bluetooth_MagicNumber.REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else {
            if (mChatService == null) setupChat();
            // onStart에서 블루투스 자동 커넥 시키기
        }

        if ( first_start == false)
        {
            bluetooth_connect();
            first_start = true;
        }
    }

    // 블루투스 커넥
    public void bluetooth_connect()
    {
        String address = "00:14:03:05:CC:3E";
        // 블루투스 디바이스 얻기
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // device와 블루투스 connect 시작하기
        mChatService.connect(device, true);
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if(Bluetooth_MagicNumber.D) Log.e(TAG, "+ ON RESUME +");

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is BCSTATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == Bluetooth_MagicNumber.BCSTATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }
    }
    @Override
    public synchronized void onPause() {
        super.onPause();
        if(Bluetooth_MagicNumber.D) Log.e(TAG, "- ON PAUSE -");
    }

    @Override
    public void onStop() {
        super.onStop();
        if(Bluetooth_MagicNumber.D) Log.e(TAG, "-- ON STOP --");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth chat services
        if (mChatService != null) mChatService.stop();
        if(Bluetooth_MagicNumber.D) Log.e(TAG, "--- ON DESTROY ---");
    }

    private void ensureDiscoverable() {
        if(Bluetooth_MagicNumber.D) Log.d(TAG, "ensure discoverable");
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != Bluetooth_MagicNumber.BCSTATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
        }
    }

    private final void setStatus(int resId) {
        //final ActionBar actionBar = getActionBar();
        //actionBar.setSubtitle(resId);
    }

    private final void setStatus(CharSequence subTitle) {
        //final ActionBar actionBar = getActionBar();
        //actionBar.setSubtitle(subTitle);
    }
    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Bluetooth_MagicNumber.MESSAGE_STATE_CHANGE:
                    if(Bluetooth_MagicNumber.D) Log.i(TAG, "MESSAGE_BCSTATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case Bluetooth_MagicNumber.BCSTATE_CONNECTED:
                            setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                            mConversationArrayAdapter.clear();
                            break;
                        case Bluetooth_MagicNumber.BCSTATE_CONNECTING:
                            setStatus(R.string.title_connecting);
                            break;
                        case Bluetooth_MagicNumber.BCSTATE_LISTEN:
                        case Bluetooth_MagicNumber.BCSTATE_NONE:
                            setStatus(R.string.title_not_connected);
                            break;
                    }
                    break;
                case Bluetooth_MagicNumber.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    mConversationArrayAdapter.add("Me:  " + writeMessage);
                    break;
                case Bluetooth_MagicNumber.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    mConversationArrayAdapter.add(mConnectedDeviceName+":  " + readMessage);
                    Toast.makeText(getApplicationContext(),readMessage,Toast.LENGTH_LONG).show();
                    break;
                case Bluetooth_MagicNumber.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Bluetooth_MagicNumber.DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case Bluetooth_MagicNumber.MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(Bluetooth_MagicNumber.TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(Bluetooth_MagicNumber.D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case Bluetooth_MagicNumber.REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case Bluetooth_MagicNumber.REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case Bluetooth_MagicNumber.REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupChat();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }


    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device, secure);
    }


    @Override
    public void onItemClick(int position) {
        //재생되는지 테스팅
        mBtnStartPlayOnClick(adapter.getItem(position).getName());
    }

    // 특정 폴더의 파일 목록을 구해서 반환
    public File[] getFileList(String strPath) {
        File[] files;
        // 폴더 경로를 지정해서 File 객체 생성
        File fileRoot = new File(strPath);
        // 해당 경로가 폴더가 아니라면 함수 탈출
        if (fileRoot.isDirectory() == false) {
            Log.i("getFileList~~", "해당 경로가 폴더가 아닙니다");
            return null;
        } else {
            Log.i("getFileList~~", strPath);

            files = fileRoot.listFiles();
        }

        Log.i("~~~getfileList~~Count", "fileList의 갯수는 " + files.length);
        return files;
    }

    private void mBtnStartPlayOnClick(String mFileName) {
        if (mPlayerState == PLAY_STOP) {
            mPlayerState = PLAYING;
            startPlay(mFileName);
        } else if (mPlayerState == PLAYING) {
            mPlayerState = PLAY_STOP;
            stopPlay();
        }
    }

    // 재생 시작
    private void startPlay(String mFileName) {
        // 미디어 플레이어 생성
        if (mPlayer == null)
            mPlayer = new MediaPlayer();
        else
            mPlayer.reset();

        mPlayer.setOnCompletionListener(this);

        String fullFilePath = mFilePath + mFileName;
        Log.v("RecFiles_makeDir", "녹음파일명 ==========> " + fullFilePath);

        try {
            mPlayer.setDataSource(fullFilePath);
            mPlayer.prepare();

        } catch (Exception e) {
            Log.v("RecFiles_makeDir", "미디어 플레이어 Prepare Error ==========> " + e);
        }

        if (mPlayerState == PLAYING) {
            try {
                mPlayer.start();
            } catch (Exception e) {
            }
        }
    }

    //재생 중지
    private void stopPlay() {
        // 재생을 중지하고
        mPlayer.stop();
        mPlayer.release();
        mPlayer = null;
    }

    public void onCompletion(MediaPlayer mp) {
        mPlayerState = PLAY_STOP; // 재생이 종료됨
    }


    private void initiatePopupWindow(int arg2) {
        try {
            //  LayoutInflater 객체와 시킴
            LayoutInflater inflater = (LayoutInflater) MainActivity.this
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            switch (arg2) {
                case 0:
                    View layout = inflater.inflate(R.layout.popup_settings,
                            (ViewGroup) findViewById(R.id.popup_layout_0));
                    pwindo = new PopupWindow(layout, mWidthPixels - 100, mHeightPixels - 320, true);

//                    pwindo = new PopupWindow(layout, mWidthPixels - 175, mHeightPixels - 450, true);
                    //pwindo.setAnimationStyle(R.style.animationName);
                    pwindo.showAtLocation(layout, Gravity.CENTER, 0, 0);

                    // 뒷배경은 흐리게
                    layout_MainMenu.getForeground().setAlpha( 100);
                    btnClosePopup = (Button) layout.findViewById(R.id.closebtn_popup_0);
                    btnClosePopup.setOnClickListener(cancel_setbutton_click_listener);


                    option1 = (RadioButton) layout.findViewById(R.id.option1);
                    option2 = (RadioButton) layout.findViewById(R.id.option2);
                    option3 = (RadioButton) layout.findViewById(R.id.option3);
                    option1.setOnClickListener(optionOnClickListener);
                    option2.setOnClickListener(optionOnClickListener);
                    option3.setOnClickListener(optionOnClickListener);
                    option1.setChecked(true);
                    contentsText = (TextView) layout.findViewById(R.id.contentsText);
                    Switch sw = (Switch) layout.findViewById(R.id.switch_gps);
                    //스위치의 체크 이벤트를 위한 리스너 등록
                    sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                            Toast.makeText(MainActivity.this, "체크상태 = " + isChecked, Toast.LENGTH_SHORT).show();

                            //체크상태가 true일때
                            if (isChecked == true) {
                                // 위치 정보 확인을 위해 정의한 메소드 호출
                                startLocationService();


                            } else {
                                contentsText.setText("GPS상태를 확인하세요.");
                            }
                        }
                    });
                    break;

                case 1:
                    View layout1 = inflater.inflate(R.layout.phonebook_list,
                            (ViewGroup)findViewById(R.id.popup_layout_1));
                    pwindo = new PopupWindow(layout1, mWidthPixels - 100, mHeightPixels - 320, true);
                    pwindo.showAtLocation(layout1, Gravity.CENTER, 0, 0);
                    // 뒷배경은 흐리게
                    layout_MainMenu.getForeground().setAlpha( 100);
                    lvPhone = (ListView)layout1.findViewById(R.id.listPhone);

                    final List<PhoneBook> listPhoneBook = new ArrayList<PhoneBook>();
                    for(int i = 0; i < saveList.size() ; i++){
                        listPhoneBook.add(new PhoneBook(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher),
                                saveList.get(i).getmName(), saveList.get(i).getmPhone(), ""));
                    }

                    final PhoneBookAdapter adapter = new PhoneBookAdapter(this, listPhoneBook);
                    lvPhone.setAdapter(adapter);

                    mbtnAddContact = (Button) layout1.findViewById(R.id.btn_add);
                    mbtnDeleteContact = (Button) layout1.findViewById(R.id.btn_delete);
                    btnClosePopup = (Button) layout1.findViewById(R.id.closebtn_popup_1);
                    btnClosePopup.setOnClickListener(cancel_addrbutton_click_listener);

                    mbtnAddContact.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            finish();
                            Intent intent = new Intent(MainActivity.this, ContactActivity.class);
                            MainActivity.this.startActivity(intent);
                        }
                    });
                    mbtnDeleteContact.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            pref.removeAllPreferences("name");
                            pref.removeAllPreferences("phoneNum");
                            listPhoneBook.clear();
                            saveList.clear();
                            Toast.makeText(MainActivity.this, "삭제 완료되었습니다.", Toast.LENGTH_SHORT).show();
                        }
                    });

                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 위치 정보 확인을 위해 정의한 메소드
     */
    private void startLocationService() {
        // 위치 관리자 객체 참조
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // 위치 정보를 받을 리스너 생성
        GPSListener gpsListener = new GPSListener();
        long minTime = 10000;
        float minDistance = 0;

        // GPS를 이용한 위치 요청
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        manager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                minTime,
                minDistance,
                gpsListener);

        // 네트워크를 이용한 위치 요청
        manager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                minTime,
                minDistance,
                gpsListener);

        // 위치 확인이 안되는 경우에도 최근에 확인된 위치 정보 먼저 확인
        try {
            Location lastLocation = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastLocation != null) {
                Double latitude = lastLocation.getLatitude();
                Double longitude = lastLocation.getLongitude();

//                Toast.makeText(getApplicationContext(), "Last Known Location : " + "Latitude : " + latitude + "\nLongitude:" + longitude, Toast.LENGTH_LONG).show();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
//        Toast.makeText(getApplicationContext(), "위치 확인이 시작되었습니다. 로그를 확인하세요.", Toast.LENGTH_SHORT).show();

    }


    /**
     * 리스너 클래스 정의
     */
    private class GPSListener implements LocationListener {
        /**
         * 위치 정보가 확인될 때 자동 호출되는 메소드
         */
        public void onLocationChanged(Location location) {
            Double latitude = location.getLatitude();
            Double longitude = location.getLongitude();

            String msg = "Latitude : " + latitude + "\nLongitude:" + longitude;
            Log.i("GPSListener", msg);

//            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();

            // 위치 좌표를 이용해 주소를 검색하는 메소드 호출
            if (latitude != null && longitude != null) {
                searchLocation(latitude, longitude);
            }
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

    }


    /**
     * 위치 좌표를 이용해 주소를 검색하는 메소드 정의
     */
    private void searchLocation(double latitude, double longitude) {
        List<Address> addressList = null;

        try {
            addressList = gc.getFromLocation(latitude, longitude, 3);

            if (addressList != null) {
                //contentsText.append("\nCount of Addresses for [" + latitude + ", " + longitude + "] : " + addressList.size());
//                for (int i = 0; i < addressList.size(); i++) {
                for (int i = 0; i < 1; i++) {
                    Address outAddr = addressList.get(i);
                    int addrCount = outAddr.getMaxAddressLineIndex() + 1;
                    StringBuffer outAddrStr = new StringBuffer();
                    for (int k = 0; k < addrCount; k++) {
                        outAddrStr.append(outAddr.getAddressLine(k));
                    }
                    outAddrStr.append("\n\t위도 : " + outAddr.getLatitude());
                    outAddrStr.append("\n\t경도 : " + outAddr.getLongitude());

                    contentsText.setText("\n\t주소 : " + outAddrStr.toString());
                }
            }

        } catch (IOException ex) {
            Log.d(TAG, "예외 : " + ex.toString());
        }

    }


    //RadioButton 눌렀을때의 반응
    private RadioButton.OnClickListener optionOnClickListener = new RadioButton.OnClickListener() {

        public void onClick(View v) {

            if (option1.isChecked()){
                sendMessage("120000");
            }
            else if (option2.isChecked()){
                sendMessage("180000");
            }
            else{
                sendMessage("300000");
            }
        }
    };

    //팝업창 닫기
    private View.OnClickListener cancel_setbutton_click_listener = new View.OnClickListener() {

        public void onClick(View v) {
            pwindo.dismiss();
            Animation btnAnimOff = AnimationUtils.loadAnimation(MainActivity.this, R.anim.set_anim_off);
            fabButton_set.startAnimation(btnAnimOff);
            fabButton_addr.startAnimation(btnAnimOff);
            layout_MainMenu.getForeground().setAlpha( 0); // restore
        }
    };
    private View.OnClickListener cancel_addrbutton_click_listener = new View.OnClickListener() {

        public void onClick(View v) {
            pwindo.dismiss();
            Animation btnAnimOff = AnimationUtils.loadAnimation(MainActivity.this, R.anim.addr_anim_off);
            fabButton_addr.startAnimation(btnAnimOff);
            fabButton_set.startAnimation(btnAnimOff);
            layout_MainMenu.getForeground().setAlpha( 0); // restore
        }
    };

    //FloatingActionButton클릭에 따른 반응
    private void initFab() {

        findViewById(R.id.fab_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initiatePopupWindow(0); //팝업창 띄우기
                //FloatingActionButton 애니메이션
                Animation btnAnimOn = AnimationUtils.loadAnimation(MainActivity.this, R.anim.set_anim_on);
                fabButton_set.startAnimation(btnAnimOn);
                fabButton_addr.startAnimation(btnAnimOn);
                sendMessage("1");
            }
        });

        findViewById(R.id.fab_phoneaddr).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initiatePopupWindow(1); //팝업창 띄우기
                //FloatingActionButton 애니메이션
                Animation btnAnimOn = AnimationUtils.loadAnimation(MainActivity.this, R.anim.addr_anim_on);
                fabButton_addr.startAnimation(btnAnimOn);
                fabButton_set.startAnimation(btnAnimOn);
            }
        });
    }


    private void setupChat() {
        Log.d(TAG, "setupChat()");

        // Initialize the array adapter for the conversation thread
        mConversationArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(this, mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }


    private ArrayList<MyData> getDataset() {
        ArrayList<MyData> dataset = new ArrayList<>();
        ////////~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~/////////
        mFilePath="/storage/emulated/0/progress_recorder/";
        Log.i("mFilePath~~??",mFilePath); ///storage/emulated/0/progress_recorder/
        File[] fileList = getFileList(mFilePath);

        for(int i=0; i < fileList.length; i++)
        {
            Log.d("~~~~fileList[i]~~~", fileList[i].getName());
            Date lastModifiedDate=new Date(fileList[i].lastModified());
            Calendar lastModifiedCalendar = new GregorianCalendar();
            lastModifiedCalendar.setTime(lastModifiedDate);

            dataset.add(new MyData(fileList[i].getName(),lastModifiedCalendar.get(Calendar.YEAR),
                    lastModifiedCalendar.get(Calendar.MONTH),
                    lastModifiedCalendar.get(Calendar.DAY_OF_MONTH),
                    lastModifiedCalendar.get(Calendar.HOUR_OF_DAY),
                    lastModifiedCalendar.get(Calendar.MINUTE),
                    lastModifiedCalendar.get(Calendar.SECOND)
            ));
        }

        return dataset;
    }

}
