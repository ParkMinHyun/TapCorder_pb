package com.example.osm.appdesign21;

import android.Manifest;
import android.annotation.TargetApi;
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
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
import android.widget.MediaController;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.osm.appdesign21.BlueTooth.BluetoothChatService;
import com.example.osm.appdesign21.BlueTooth.Bluetooth_MagicNumber;
import com.example.osm.appdesign21.BlueTooth.DeviceListActivity;
import com.example.osm.appdesign21.Recorder.MyData;
import com.example.osm.appdesign21.Recorder.RecFiles_makeDir;
import com.example.osm.appdesign21.Recorder.Record_Time;
import com.example.osm.appdesign21.Recorder.TimeRecyclerAdapter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;


public class MainActivity extends Activity implements MediaPlayer.OnPreparedListener,MediaPlayer.OnCompletionListener, TimeRecyclerAdapter.OnItemClickListener, MediaController.MediaPlayerControl {

    private Button btnStart,btnStop,btnPause;
    private TextView text;
    private SeekBar seekBar;
    public static String FullFilePath;
    private MediaPlayer mediaPlayer;
    private MediaController mediaController;
    private String audioFile;

    private static String TAG = "MainActivity";
    private TimeRecyclerAdapter adapter;
    public static PopupWindow pwindo;
    private Button btnClosePopup;
    private int mWidthPixels, mHeightPixels;
    private RadioButton option1, option2, option3;

    private FloatingActionButton fabButton_set,fabButton_addr;
    private RecyclerView mTimeRecyclerView;

    // 연락처 ListView
    private ListView lvPhone;
    private Button mbtnAddContact;
    private Button mbtnDeleteContact;

    DisplayMetrics mMetrics;
    TextView contentsText;
    Geocoder gc;

    SharedPreferences pref;
    ArrayList<PhoneBook> saveList;
    FrameLayout layout_MainMenu;

    /* 블루투스에 관한 것들 */
    private  boolean first_start = false;
    private String mConnectedDeviceName = null;               // 연결된 디바이스의 이름
    private ArrayAdapter<String> mConversationArrayAdapter;   // thread 소통을 위한 ArrayAdapter
    private StringBuffer mOutStringBuffer;                    // 송신을 위한 outGoing StringBuffer
    private BluetoothAdapter mBluetoothAdapter = null;        // 블루투스 어댑터
    private BluetoothChatService mChatService = null;         // 블루투스챗 서비스 클래스

    /* 녹음에 관한 것들 */
    private ArrayList<MyData> dataset = null;
    private File[] fileList = null;
    private Record_Time rec_time;
    private String mFilePath ;                   //녹음파일 디렉터리 위치
    private MediaRecorder mRecorder = null;
    private MediaPlayer mPlayer = null;
    private int newRecordNum=0;
    private int mCurRecTimeMs = 0;
    private int mCurProgressTimeDisplay = 0;
    private static final int PLAY_STOP = 0;
    private static final int PLAYING = 1;
    private int mPlayerState = PLAY_STOP;

    /* 스탑워치에 관한 것들 */
    private long starttime = 0L;
    private long timeInMilliseconds = 0L;
    private long timeSwapBuff = 0L;
    private long updatedtime = 0L;
    private int t = 1;
    private int secs = 0;
    private int mins = 0;
    private int milliseconds = 0;
    Handler stopwatch_handler = new Handler();

    /* 업로드에 관한 것들 */
    private int fcnt = 1;
    public Context mContext;

    private long lastTimeBackPresssed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 파일 자동 업로드
        mContext = this;
        while(fileExistance("/storage/emulated/0/progress_recorder/recordFile" + fcnt + ".amr")){
            new UploadTask("/storage/emulated/0/progress_recorder/recordFile" + fcnt + ".amr", mContext).execute();
            fcnt++;
        }

        /*--------------블루투스-------------*/

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();   //아답터 얻기

        // 만약 어댑터가 null이면 블루투스 종료
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        /*--------------지도-------------*/

        gc = new Geocoder(this, Locale.KOREAN);    // 지오코더 객체 생성

        /*--------------UI-------------*/

        fabButton_set =(FloatingActionButton)findViewById(R.id.fab_settings);
        fabButton_addr=(FloatingActionButton)findViewById(R.id.fab_phoneaddr);
        initFab();//FloatingButton Click에 따른 메서드

        mTimeRecyclerView = (RecyclerView) findViewById(R.id.mTimeRecyclerView);
        mTimeRecyclerView.setHasFixedSize(true);

        adapter = new TimeRecyclerAdapter(getDataset());
        adapter.setOnItemClickListener(this);
        mTimeRecyclerView.setAdapter(adapter);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mTimeRecyclerView.setLayoutManager(layoutManager);

        mMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(mMetrics);

        WindowManager w = getWindowManager();
        Display d = w.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        d.getMetrics(metrics);

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

        //btnStart = (Button)findViewById(R.id.button);
        //btnStart.setOnClickListener(new Button.OnClickListener() {
        //    @Override
        //    public void onClick(View view) {
        //        initiatePopupWindow(2);
        //    }
        //});
    }

    @Override
    public void onStart() {
        super.onStart();
        if(Bluetooth_MagicNumber.D) Log.e(TAG, "++ ON START ++");

        // 블루투스 아답터 연동시키기
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, Bluetooth_MagicNumber.REQUEST_ENABLE_BT);
        } else {
            if (mChatService == null) setupChat();
        }

        // onStart에서 블루투스 자동 커넥 시키기
        if ( first_start == false)
        {
            bluetooth_connect();
            first_start = true;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if(Bluetooth_MagicNumber.D) Log.e(TAG, "-- ON STOP --");
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if(Bluetooth_MagicNumber.D) Log.e(TAG, "+ ON RESUME +");

        if (mChatService != null) {
            // 이미 mChatService를 받았는지 안 받았는지 체크
            if (mChatService.getState() == Bluetooth_MagicNumber.BCSTATE_NONE) {
                // 블루투스챗서비스 시작
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
    public void onDestroy() {
        super.onDestroy();
        // 블루투스챗서비스 종료
        if (mChatService != null) mChatService.stop();
        if(Bluetooth_MagicNumber.D) Log.e(TAG, "--- ON DESTROY ---");
    }

/*--------------------------------------블루투스------------------------------------------*/
/*--------------------------------------------------------------------------------------*/

    private void setupChat() {
        Log.d(TAG, "setupChat()");

        // thread통신을 위한 adapter를 담는 배열아답터 추가
        mConversationArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
        // 블루투스 연결을 위한 service 초기화
        mChatService = new BluetoothChatService(this, mHandler);
        // outgoing messages를 담는 버퍼 초기화
        mOutStringBuffer = new StringBuffer("");
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

    private void sendMessage(String message) {
        if (mChatService.getState() != Bluetooth_MagicNumber.BCSTATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        if (message.length() > 0) {
            // 블루투스챗서비스에게 쓸 메세지를 알리기
            byte[] send = message.getBytes();
            mChatService.write(send);

            // outgoing메세지 초기화
            mOutStringBuffer.setLength(0);
        }
    }

    private final void setStatus(int resId) {
    }

    private final void setStatus(CharSequence subTitle) {
    }

    // 블루투스챗 서비스로 부터 정보를 얻는 핸들러
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
                /*---------블루투스 송신---------*/
                case Bluetooth_MagicNumber.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;

                    String writeMessage = new String(writeBuf);
                    mConversationArrayAdapter.add("Me:  " + writeMessage);
                    break;
                /*---------블루투스 수신---------*/
                case Bluetooth_MagicNumber.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;

                    String readMessage = new String(readBuf, 0, msg.arg1);        // 블루투스값 읽기
                    
                    mConversationArrayAdapter.add(mConnectedDeviceName+":  " + readMessage);
                    Toast.makeText(getApplicationContext(),readMessage,Toast.LENGTH_LONG).show();
                    if(readMessage.equals("R"))
                    {
                        startRec();
                        adapter = new TimeRecyclerAdapter(getDataset());
                        adapter.setOnItemClickListener(MainActivity.this);        // 녹음 시작시 파일 RecyclerView에 추가하기.
                        mTimeRecyclerView.setAdapter(adapter);

                        starttime = SystemClock.uptimeMillis();
                        stopwatch_handler.postDelayed(updateTimer, 0);            // 녹음 시작시 stopWatch 시작
                    }

                    break;
                /*---------블루투스 연결완료시---------*/
                case Bluetooth_MagicNumber.MESSAGE_DEVICE_NAME:
                    // device이름 저장
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
                // 디바이스와 커넥이 됬을 경우
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case Bluetooth_MagicNumber.REQUEST_CONNECT_DEVICE_INSECURE:
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case Bluetooth_MagicNumber.REQUEST_ENABLE_BT:
                // 블루투스 요청을 할수 있을 경우
                if (resultCode == Activity.RESULT_OK) {
                    // 블루투스 비활성화일 경우
                    setupChat();
                } else {
                    // 유저가 블루투스를 할 수 없을 경우
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }


    private void connectDevice(Intent data, boolean secure) {
        // 블루투스 페어링 되는 기기의 MAC주소 얻기
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // 블루투스 객체 얻기
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // device와 커넥시도
        mChatService.connect(device, secure);
    }


/*----------------------------------녹음 및 재생------------------------------------------*/
/*--------------------------------------------------------------------------------------*/

    private ArrayList<MyData> getDataset() {
        dataset = new ArrayList<>();

        // SD카드에 디렉토리를 만든다.
        mFilePath = RecFiles_makeDir.makeDir("progress_recorder");
        fileList = getFileList(mFilePath);

        // list에 dataset 넣기 ( 핸드폰 안에 있는 음성 파일 )
        for(int i=0; i < fileList.length; i++)
            insertRecFile(i,fileList,dataset);


        newRecordNum = fileList.length + 1;
        return dataset;
    }

    /* SD카드 경로에 있는 음성파일 TapCorder List에 시간 및 날짜별로 정리한 뒤 넣기 */
    private void insertRecFile(int order, File[] fileList_copy, ArrayList<MyData> dataset_copy)
    {
        Date lastModifiedDate=new Date(fileList_copy[order].lastModified());
        Calendar lastModifiedCalendar = new GregorianCalendar();
        lastModifiedCalendar.setTime(lastModifiedDate);

        dataset_copy.add(new MyData(fileList_copy[order].getName(),lastModifiedCalendar.get(Calendar.YEAR),
                lastModifiedCalendar.get(Calendar.MONTH),
                lastModifiedCalendar.get(Calendar.DAY_OF_MONTH),
                lastModifiedCalendar.get(Calendar.HOUR_OF_DAY),
                lastModifiedCalendar.get(Calendar.MINUTE),
                lastModifiedCalendar.get(Calendar.SECOND)
        ));
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
            return null;
        } else {
            files = fileRoot.listFiles();
        }

        return files;
    }

    /* List 녹음 클릭했을 경우 Play 시키기 */
    private void mBtnStartPlayOnClick(String mFileName) {
        if (mPlayerState == PLAY_STOP) {
            mPlayerState = PLAYING;
            startPlay(mFileName);
            initiatePopupWindow(2);
        } else if (mPlayerState == PLAYING) {
            mPlayerState = PLAY_STOP;
            stopPlay();
        }
    }

    // 녹음 시작 메서드
    private void startRec() {
        mCurRecTimeMs = 0;
        mCurProgressTimeDisplay = 0;

        if (mRecorder == null) {
            mRecorder = new MediaRecorder();
            mRecorder.reset();
        } else {
            mRecorder.reset();
        }

        // MediaRecorder를 통한 녹음 시작
        try {
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
            mRecorder.setOutputFile(mFilePath+"recordFile"+newRecordNum+".amr"); //newRecordFile명의 음성파일에 음성 녹음.

            mRecorder.prepare();
            mRecorder.start();
        } catch (IllegalStateException e) {
        } catch (IOException e) {
        }
    }

    // 녹음정지
    private void stopRec() {
        try {
            mRecorder.stop();
            newRecordNum++;
        } catch (Exception e) {
        } finally {
            mRecorder.release();
            mRecorder = null;
        }

        mCurRecTimeMs = -999;
    }

    // 재생 시작
    private void startPlay(String mFileName) {
        // 미디어 플레이어 생성
        if (mPlayer == null)
            mPlayer = new MediaPlayer();
        else
            mPlayer.reset();

        mPlayer.setOnCompletionListener(this);

        FullFilePath = mFilePath + mFileName;
        try {
            mPlayer.setDataSource(FullFilePath);
            mPlayer.prepare();

        } catch (Exception e) {
        }

        if (mPlayerState == PLAYING) {
            try {
                //mPlayer.start();
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


    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - lastTimeBackPresssed < 1500)
        {
            finish();
            super.onBackPressed();
        } else {

            Toast.makeText(this, "'뒤로' 버튼을 한번 더 누르면 종료됩니다.",Toast.LENGTH_SHORT).show();
            lastTimeBackPresssed = System.currentTimeMillis();
        }
    }
/*--------------------------------------스탑워치------------------------------------------*/
/*--------------------------------------------------------------------------------------*/

    public Runnable updateTimer = new Runnable() {

        public void run() {

            timeInMilliseconds = SystemClock.uptimeMillis() - starttime;

            updatedtime = timeSwapBuff + timeInMilliseconds;

            secs = (int) (updatedtime / 1000);
            mins = secs / 60;
            secs = secs % 60;
            milliseconds = (int) (updatedtime % 1000);
            stopwatch_handler.postDelayed(this, 0);

            if (secs > 10){
                stopRec();
                Toast.makeText(getApplicationContext(),"녹음 완료",Toast.LENGTH_SHORT).show();
                initStopWatch();
            }
        }

    };

    /* 스탑워치 reset */
    private void initStopWatch()
    {
        starttime = 0L;
        timeInMilliseconds = 0L;
        timeSwapBuff = 0L;
        updatedtime = 0L;
        t = 1;
        secs = 0;
        mins = 0;
        milliseconds = 0;
        stopwatch_handler.removeCallbacks(updateTimer);
    }

    /*--------------------------------------현재 위치------------------------------------------*/
/*--------------------------------------------------------------------------------------*/
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
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 위치 정보가 확인될 때 자동 호출되는 메소드
     */
    private class GPSListener implements LocationListener {
        public void onLocationChanged(Location location) {
            Double latitude = location.getLatitude();
            Double longitude = location.getLongitude();

            String msg = "Latitude : " + latitude + "\nLongitude:" + longitude;


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


/*--------------------------------------기타 UI------------------------------------------*/
/*--------------------------------------------------------------------------------------*/

    //RadioButton 눌렀을때의 반응
    private RadioButton.OnClickListener optionOnClickListener = new RadioButton.OnClickListener() {

        public void onClick(View v) {

            if (option1.isChecked()){
                sendMessage(String.valueOf(Record_Time.REC_TIME1));
            }
            else if (option2.isChecked()){
                sendMessage(String.valueOf(Record_Time.REC_TIME2));
            }
            else{
                sendMessage(String.valueOf(Record_Time.REC_TIME3));
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
    private View.OnClickListener cancel_music_click_listener = new View.OnClickListener() {

        public void onClick(View v) {
            pwindo.dismiss();
            onStop();
            Animation btnAnimOff = AnimationUtils.loadAnimation(MainActivity.this, R.anim.set_anim_off);
            fabButton_set.startAnimation(btnAnimOff);
            fabButton_addr.startAnimation(btnAnimOff);
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


    public void initiatePopupWindow(int arg2) {
        try {
            //  LayoutInflater 객체와 시킴
            LayoutInflater inflater = (LayoutInflater) MainActivity.this
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            switch (arg2) {
                case 0:
                    View layout = inflater.inflate(R.layout.popup_settings,
                            (ViewGroup) findViewById(R.id.popup_layout_0));
                    pwindo = new PopupWindow(layout, mWidthPixels - 100, mHeightPixels - 320, true);

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
                case 2:
                    View layout2 = inflater.inflate(R.layout.music_player,
                            (ViewGroup) findViewById(R.id.popup_layout_2));
                    pwindo = new PopupWindow(layout2, mWidthPixels - 100, mHeightPixels - 320, true);

                    pwindo.showAtLocation(layout2, Gravity.CENTER, 0, 0);

                    // 뒷배경은 흐리게
                    layout_MainMenu.getForeground().setAlpha( 100);
                    btnClosePopup = (Button) layout2.findViewById(R.id.closebtn_popup_2);
                    btnClosePopup.setOnClickListener(cancel_music_click_listener);

                    //seekBar = (SeekBar) findViewById(R.id.seekBar);
                    //seekBar.setVisibility(SeekBar.VISIBLE);
                    //seekBar.setMax(mediaPlayer.getDuration());
                    //text = (TextView)findViewById(R.id.textView2);
                    //btnStart = (Button)findViewById(R.id.play);
                    //btnStop = (Button)findViewById(R.id.stop);
                    //btnPause = (Button)findViewById(R.id.pause);

                    audioFile = this.getIntent().getStringExtra("audioPath");
                    audioFile = "음악파일 경로";

                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mediaPlayer.setOnPreparedListener(this);

                    mediaController = new MediaController(pwindo.getContentView().getContext());
                    mediaController.setMediaPlayer(mPlayer);
                    mediaController.setAnchorView((MediaController)findViewById(R.id.mediaController));
                    mediaController.setVisibility(View.VISIBLE);

                    try{
                        mediaPlayer.setDataSource(FullFilePath);
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                    }catch(IOException e){
                        Toast.makeText(this, e.toString(),Toast.LENGTH_LONG).show();
                    }
                    //seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//
                    //    @Override
                    //    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    //        if(fromUser) {
                    //            mediaPlayer.seekTo(progress);
                    //        }
                    //        int m = progress / 60000;
                    //        int s = (progress % 60000) / 1000;
                    //        String strTime = String.format("%02d:%02d", m, s);
                    //        text.setText(strTime);
                    //    }
                    //    @Override
                    //    public void onStartTrackingTouch(SeekBar seekBar) {
                    //    }
                    //    @Override
                    //    public void onStopTrackingTouch(SeekBar seekBar) {
                    //    }
                    //});
                    //btnStart.setOnClickListener(new View.OnClickListener() {
                    //    @Override
                    //    public void onClick(View v) {
                    //        mediaPlayer.start();
//
                    //        new Thread();
                    //    }
                    //});
                    //btnStop.setOnClickListener(new View.OnClickListener() {
                    //    @Override
                    //    public void onClick(View v) {
                    //        mediaPlayer.stop();
                    //        try
                    //        {
                    //            mediaPlayer.prepare();
                    //        }
                    //        catch(IOException ie)
                    //        {
                    //            ie.printStackTrace();
                    //        }
                    //        mediaPlayer.seekTo(0);
                    //    }
                    //});
                    //btnPause.setOnClickListener(new View.OnClickListener() {
                    //    @Override
                    //    public void onClick(View v) {
                    //        mediaPlayer.pause();
                    //    }
                    //});



                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void onPrepared(MediaPlayer mediaPlayer){
        mediaController = new MediaController(this);
        mediaController.setMediaPlayer(this);
        mediaController.setAnchorView((MediaController)findViewById(R.id.mediaController));
    }
    @Override
    public boolean onTouchEvent(MotionEvent event){
        mediaController.show();
        return true;
    }

    @Override
    public void start() {

    }

    @Override
    public void pause() {

    }

    @Override
    public int getDuration() {
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        return 0;
    }

    @Override
    public void seekTo(int i) {

    }

    @Override
    public boolean isPlaying() {
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause(){
        return true;
    }
    @Override
    public boolean canSeekBackward(){
        return true;
    }

    @Override
    public boolean canseekForward() {
        return false;
    }

    @Override
    public boolean canSeekForward() {
        return false;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    public boolean fileExistance(String fname){
        File file = new File(fname);
        return file.exists();
    }


}
