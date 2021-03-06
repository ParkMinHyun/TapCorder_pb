package com.example.osm.appdesign21;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.SmsManager;
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
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.osm.appdesign21.BlueTooth.BluetoothChatService;
import com.example.osm.appdesign21.BlueTooth.Bluetooth_MagicNumber;
import com.example.osm.appdesign21.BlueTooth.DeviceListActivity;
import com.example.osm.appdesign21.FTPServer.UploadBattery;
import com.example.osm.appdesign21.FTPServer.UploadGPS;
import com.example.osm.appdesign21.FTPServer.UploadProtectors;
import com.example.osm.appdesign21.FTPServer.UploadTask;
import com.example.osm.appdesign21.Recorder.MyData;
import com.example.osm.appdesign21.Recorder.RecFiles_makeDir;
import com.example.osm.appdesign21.Recorder.Record_Time;
import com.example.osm.appdesign21.Recorder.TimeRecyclerAdapter;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;


public class MainActivity extends ActionBarActivity implements MediaPlayer.OnCompletionListener, TimeRecyclerAdapter.OnItemClickListener {

    private static String TAG = "MainActivity";

    private GPSListener gpsListener;
    private double userLatitude;
    private double userLongitude;

    private TimeRecyclerAdapter adapter;
    private PopupWindow pwindo;
    private Button btnClosePopup;
    private int mWidthPixels, mHeightPixels;
    private RadioButton option1, option2, option3;
    private RelativeLayout changeModeLayout;

    private FloatingActionButton fabButton_set;
    private RecyclerView mTimeRecyclerView;
    private TextView contentsText;
    private ScrollView lvScrollPhone;

    // 연락처 ListView
    private ListView lvPhone;
    private RelativeLayout mbtnAddContact;
    private RelativeLayout mbtnDeleteContact;

    private boolean mUploadingGPS = false;

    DisplayMetrics mMetrics;

    SharedPreferences pref;
    ArrayList<PhoneBook> saveList;
    FrameLayout layout_MainMenu;

    /* 블루투스에 관한 것들 */
    private String mConnectedDeviceName = null;               // 연결된 디바이스의 이름
    private ArrayAdapter<String> mConversationArrayAdapter;   // thread 소통을 위한 ArrayAdapter
    private StringBuffer mOutStringBuffer;                    // 송신을 위한 outGoing StringBuffer
    private BluetoothAdapter mBluetoothAdapter = null;        // 블루투스 어댑터
    private BluetoothChatService mChatService = null;         // 블루투스챗 서비스 클래스

    /* 녹음에 관한 것들 */
    private ArrayList<MyData> dataset = null;
    private File[] fileList = null;
    private String mFilePath;                   //녹음파일 디렉터리 위치
    private MediaRecorder mRecorder = null;
    private MediaPlayer mPlayer = null;
    private int newRecordNum = 0;
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
    Handler stopwatch_handler = new Handler();

    private GoogleApiClient client;

    /* 업로드에 관한 것들 */
    private int fcnt = 1;
    public Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        /*------------ Server ------------*/
        //파일 자동 업로드
        mContext = this;
        while (fileExistance("/storage/emulated/0/progress_recorder/recordFile" + fcnt + ".amr")) {
            new UploadTask("/storage/emulated/0/progress_recorder/recordFile" + fcnt + ".amr", mContext).execute();
            fcnt++;
        }

        /*------------- GPS --------------*/

        chkGpsService();    // 켜져있는지 안 켜져있는지 확인 -> 안 켜져 있을시 Alert창을 통해 설정할 수 있음
        startLocationService();      // 위치확인 시작~

        /*--------------블루투스-------------*/

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();   //아답터 얻기

        /*--------------UI-------------*/

        fabButton_set = (FloatingActionButton) findViewById(R.id.fab_settings);
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

        for (int i = 0; i < 5; i++) {
            name = pref.getValue(Integer.toString(i), "no", "name");
            phoneNumber = pref.getValue(Integer.toString(i), "no", "phoneNum");
            if (!name.equals("no")) {
                saveList.add(new PhoneBook(name, phoneNumber));
            }
        }

        layout_MainMenu = (FrameLayout) findViewById(R.id.mainmenu);
        layout_MainMenu.getForeground().setAlpha(0);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctxt, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            new UploadBattery(mContext, String.valueOf(level) + "%").execute();
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();

        // 블루투스 아답터 연동시키기
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, Bluetooth_MagicNumber.REQUEST_ENABLE_BT);
        } else {
            if (mChatService == null) {
                setupChat();
                bluetooth_connect();
            }
        }

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.osm.appdesign21/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);

    }

    @Override
    public void onStop() {
        super.onStop();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.osm.appdesign21/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.disconnect();
    }

    @Override
    public synchronized void onResume() {
        super.onResume();

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
        if (Bluetooth_MagicNumber.D) Log.e(TAG, "- ON PAUSE -");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 블루투스챗서비스 종료
        if (mChatService != null) mChatService.stop();
        if (Bluetooth_MagicNumber.D) Log.e(TAG, "--- ON DESTROY ---");
    }

/*--------------------------------------블루투스------------------------------------------*/
/*--------------------------------------------------------------------------------------*/

    private void setupChat() {

        // thread통신을 위한 adapter를 담는 배열아답터 추가
        mConversationArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
        // 블루투스 연결을 위한 service 초기화
        mChatService = new BluetoothChatService(this, mHandler);
        // outgoing messages를 담는 버퍼 초기화
        mOutStringBuffer = new StringBuffer("");
    }

    // 블루투스 커넥
    public void bluetooth_connect() {
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
                    if (Bluetooth_MagicNumber.D) Log.i(TAG, "MESSAGE_BCSTATE_CHANGE: " + msg.arg1);
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
                    mConversationArrayAdapter.add(mConnectedDeviceName + ":  " + readMessage);
                    Toast.makeText(getApplicationContext(), readMessage, Toast.LENGTH_LONG).show();
                    if (readMessage.equals("R")) {
                        for(int i = 0; i < 5; i++){
                            if(pref.getValue(Integer.toString(i), "no", "phoneNum").equals("no")){
                                break;
                            }else{
                                sendSMS(pref.getValue(Integer.toString(i), "no", "phoneNum"),
                                        "Tapcorder가 실행되었습니다. 현재 위치는 " + "서울특별시 광진구 군자동 114" + "입니다.");
                            }
                        }
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
        if (Bluetooth_MagicNumber.D) Log.d(TAG, "onActivityResult " + resultCode);
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
                    bluetooth_connect();
                } else {
                    // 유저가 블루투스를 할 수 없을 경우
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
        int j = 1;
        // list에 dataset 넣기 ( 핸드폰 안에 있는 음성 파일 )
        for (int i = 0; i < fileList.length; i++) {
            if (!fileList[i].getName().contains("record")) {
                continue;
            }else{
                j++;
            }
            insertRecFile(i, fileList, dataset);
        }
        newRecordNum = j;
        return dataset;
    }

    /* SD카드 경로에 있는 음성파일 TapCorder List에 시간 및 날짜별로 정리한 뒤 넣기 */
    private void insertRecFile(int order, File[] fileList_copy, ArrayList<MyData> dataset_copy) {
        Date lastModifiedDate = new Date(fileList_copy[order].lastModified());
        Calendar lastModifiedCalendar = new GregorianCalendar();
        lastModifiedCalendar.setTime(lastModifiedDate);

        dataset_copy.add(new MyData(fileList_copy[order].getName(), lastModifiedCalendar.get(Calendar.YEAR),
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

    @Override
    public boolean canPause() {
        return false;
    }

    @Override
    public boolean canSeekBackward() {
        return false;
    }

    @Override
    public boolean canseekForward() {
        return false;
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
        } else if (mPlayerState == PLAYING) {
            mPlayerState = PLAY_STOP;
            stopPlay();
        }
    }

    // 녹음 시작 메서드
    private void startRec() {

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
            mRecorder.setOutputFile(mFilePath + "recordFile" + newRecordNum + ".amr"); //newRecordFile명의 음성파일에 음성 녹음.

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
        try {
            mPlayer.setDataSource(fullFilePath);
            mPlayer.prepare();

        } catch (Exception e) {
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


/*--------------------------------------스탑워치------------------------------------------*/
/*--------------------------------------------------------------------------------------*/

    public Runnable updateTimer = new Runnable() {

        public void run() {

            timeInMilliseconds = SystemClock.uptimeMillis() - starttime;

            updatedtime = timeSwapBuff + timeInMilliseconds;

            secs = (int) (updatedtime / 1000);
            mins = secs / 60;
            secs = secs % 60;
            stopwatch_handler.postDelayed(this, 0);

            if (secs > 10) {
                for(int i = 0; i < 5; i++){
                    if(pref.getValue(Integer.toString(i), "no", "phoneNum").equals("no")){
                        break;
                    }else{
                        sendSMS(pref.getValue(Integer.toString(i), "no", "phoneNum"),
                                "Tapcorder음성이 전송 완료되었습니다. App을 열어 확인해 주세요.");
                    }
                }
                stopRec();
                Toast.makeText(getApplicationContext(), "녹음 완료", Toast.LENGTH_SHORT).show();
                initStopWatch();

                new UploadTask("/storage/emulated/0/progress_recorder/recordFile" + fcnt + ".amr", mContext).execute();
                fcnt++;

            }
        }

    };

    /* 스탑워치 reset */
    private void initStopWatch() {
        starttime = 0L;
        timeInMilliseconds = 0L;
        timeSwapBuff = 0L;
        updatedtime = 0L;
        t = 1;
        secs = 0;
        mins = 0;
        stopwatch_handler.removeCallbacks(updateTimer);
    }



/*--------------------------------------기타 UI------------------------------------------*/
/*--------------------------------------------------------------------------------------*/

    //RadioButton 눌렀을때의 반응
    private RadioButton.OnClickListener optionOnClickListener = new RadioButton.OnClickListener() {

        public void onClick(View v) {

            if (option1.isChecked()) {
                sendMessage(String.valueOf(Record_Time.REC_TIME1));
            } else if (option2.isChecked()) {
                sendMessage(String.valueOf(Record_Time.REC_TIME2));
            } else {
                sendMessage(String.valueOf(Record_Time.REC_TIME3));
            }
        }
    };

    //팝업창 닫기

    private View.OnClickListener cancel_addrbutton_click_listener = new View.OnClickListener() {

        public void onClick(View v) {
            pwindo.dismiss();
            Animation btnAnimOff = AnimationUtils.loadAnimation(MainActivity.this, R.anim.addr_anim_off);
            fabButton_set.startAnimation(btnAnimOff);
            layout_MainMenu.getForeground().setAlpha(0); // restore
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
                sendMessage("1");
            }
        });

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
//                    pwindo = new PopupWindow(layout, mWidthPixels - 100, mHeightPixels - 320, true);
                    pwindo = new PopupWindow(layout, mWidthPixels, mHeightPixels-150, true);

                    pwindo.showAtLocation(layout, Gravity.BOTTOM, 0, 0);

                    // 뒷배경은 흐리게
                    layout_MainMenu.getForeground().setAlpha(150);


                    option1 = (RadioButton) layout.findViewById(R.id.option1);
                    option2 = (RadioButton) layout.findViewById(R.id.option2);
                    option3 = (RadioButton) layout.findViewById(R.id.option3);
                    option1.setOnClickListener(optionOnClickListener);
                    option2.setOnClickListener(optionOnClickListener);
                    option3.setOnClickListener(optionOnClickListener);
                    option1.setChecked(true);


                    lvPhone = (ListView) layout.findViewById(R.id.listPhone);
                    lvScrollPhone=(ScrollView)layout.findViewById(R.id.scroll_lvphone);
                    final List<PhoneBook> listPhoneBook = new ArrayList<PhoneBook>();
                    for (int i = 0; i < saveList.size(); i++) {
                        String pnum = saveList.get(i).getmPhone();
                        listPhoneBook.add(new PhoneBook(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher),
                                saveList.get(i).getmName(), pnum, ""));
                    }

                    final PhoneBookAdapter adapter = new PhoneBookAdapter(this, listPhoneBook);
                    lvPhone.setAdapter(adapter);

                    lvPhone.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View view, MotionEvent motionEvent) {
                            lvScrollPhone.requestDisallowInterceptTouchEvent(true);

                            return false;
                        }
                    });

                    mbtnAddContact = (RelativeLayout) layout.findViewById(R.id.btn_add);
                    mbtnDeleteContact = (RelativeLayout) layout.findViewById(R.id.btn_delete);
                    btnClosePopup = (Button) layout.findViewById(R.id.closebtn_popup_1);
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
                            new UploadProtectors(mContext, "").execute();
                            Toast.makeText(MainActivity.this, "삭제 완료되었습니다.", Toast.LENGTH_SHORT).show();
                        }
                    });

                    changeModeLayout = (RelativeLayout) layout.findViewById(R.id.changeMode);
                    changeModeLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            changeModeDialog();
                        }
                    });


                    break;

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //모드 변경 레이아웃 클릭시 뜨는 다이알로그창
    private void changeModeDialog() {
        AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
        alt_bld.setMessage("모드를 변경하시겠습니까?\n'예'를 누르시면 모드 선택 초기화면으로 돌아갑니다.").setCancelable(
                false).setPositiveButton("예",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Action for 'Yes' Button
                        pref.removeAllPreferences("mode");
                        Intent intent = new Intent(MainActivity.this, SelectModeActivity.class);
                        MainActivity.this.startActivity(intent);
                        finish();
                    }
                }).setNegativeButton("아니오",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Action for 'NO' Button
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alt_bld.create();
        // Title for AlertDialog
        alert.setTitle("애플리케이션 모드 변경");
        // Icon for AlertDialog
//        alert.setIcon(R.drawable.icon);
        alert.show();
    }


    public boolean fileExistance(String fname) {
        File file = new File(fname);
        return file.exists();
    }

/*------------------------------------- GPS   ------------------------------------------*/
/*--------------------------------------------------------------------------------------*/

    //GPS 설정 체크
    private boolean chkGpsService() {

        String gps = android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        if (!(gps.matches(".*gps.*") && gps.matches(".*network.*"))) {

            // GPS OFF 일때 Dialog 표시
            AlertDialog.Builder gsDialog = new AlertDialog.Builder(this);
            gsDialog.setTitle("위치 서비스 설정");
            gsDialog.setMessage("무선 네트워크 사용, GPS 위성 사용을 모두 체크하셔야 정확한 위치 서비스가 가능합니다.\n위치 서비스 기능을 설정하시겠습니까?");
            gsDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // GPS설정 화면으로 이동
                    Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    startActivity(intent);
                }
            })
                    .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            return;
                        }
                    }).create().show();
            return false;

        } else {
            return true;
        }
    }

    /**
     * 위치 정보 확인을 위해 정의한 메소드
     */
    private void startLocationService() {
        // 위치 관리자 객체 참조
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // 위치 정보를 받을 리스너 생성
        gpsListener = new GPSListener();
        long minTime = 1000;
        float minDistance = 0;

        try {
            // GPS를 이용한 위치 요청
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
            Location lastLocation = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastLocation != null) {
                Double latitude = lastLocation.getLatitude();
                Double longitude = lastLocation.getLongitude();

                // Toast.makeText(getApplicationContext(), "Last Known Location : " + "Latitude : " + latitude + "\nLongitude:" + longitude, Toast.LENGTH_LONG).show();
            }
        } catch (SecurityException ex) {
            ex.printStackTrace();
        }

    }

    /**
     * 리스너 클래스 정의
     */
    private class GPSListener implements LocationListener {
        /**
         * 위치 정보가 확인될 때 자동 호출되는 메소드
         */
        public void onLocationChanged(Location location) {
            userLatitude = location.getLatitude();
            userLongitude = location.getLongitude();

            LatLng userCurrentLatlng = new LatLng(userLatitude, userLongitude);

            if (mUploadingGPS == false) {
                new UploadGPS(mContext, userLatitude + "," + userLongitude).execute();
                mUploadingGPS = true;
            }
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }
    private void sendSMS(String phoneNumber, String message){
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, null, null);
    }
}