package com.example.osm.appdesign21;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.osm.appdesign21.BlueTooth.BluetoothChatService;
import com.example.osm.appdesign21.BlueTooth.Bluetooth_MagicNumber;


public class NewMainActivity extends AppCompatActivity {

    private static String TAG = "NewMainActivity";
    /* 블루투스에 관한 것들 */
    private  boolean first_start = false;
    private String mConnectedDeviceName = null;               // 연결된 디바이스의 이름
    private ArrayAdapter<String> mConversationArrayAdapter;   // thread 소통을 위한 ArrayAdapter
    private StringBuffer mOutStringBuffer;                    // 송신을 위한 outGoing StringBuffer
    private BluetoothAdapter mBluetoothAdapter = null;        // 블루투스 어댑터
    private BluetoothChatService mChatService = null;         // 블루투스챗 서비스 클래스

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_activity_main);

        init_layout();

        /*--------------블루투스-------------*/

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();   //아답터 얻기

        // 만약 어댑터가 null이면 블루투스 종료
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

    }

    public void init_layout()
    {
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Tab 1"));
        tabLayout.addTab(tabLayout.newTab().setText("Tab 2"));
        tabLayout.addTab(tabLayout.newTab().setText("Tab 3"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        final PagerAdapter adapter = new PagerAdapter
                (getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }
    @Override
    public void onStart() {
        super.onStart();

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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 블루투스챗서비스 종료
        if (mChatService != null) mChatService.stop();
    }

    private void setupChat() {

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
                        /*
                        startRec();
                        adapter = new TimeRecyclerAdapter(getDataset());
                        adapter.setOnItemClickListener(MainActivity.this);        // 녹음 시작시 파일 RecyclerView에 추가하기.
                        mTimeRecyclerView.setAdapter(adapter);

                        starttime = SystemClock.uptimeMillis();
                        stopwatch_handler.postDelayed(updateTimer, 0);            // 녹음 시작시 stopWatch 시작
                        */
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


}