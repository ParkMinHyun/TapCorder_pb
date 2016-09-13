package com.example.osm.appdesign21;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {
    private static String TAG = "MainActivity";
    private PopupWindow pwindo;
    private Button btnClosePopup;
    private int mWidthPixels, mHeightPixels;
    DisplayMetrics mMetrics;
    private RadioButton option1, option2, option3;

    TextView contentsText;
    Geocoder gc;

    //대량의 문자열 데이터를 저장할 Arraylist 객체 생성
    ArrayList<String> mDatas = new ArrayList<String>();

    ListView listview; //ListView 참조변수
    private TextView popupTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 지오코더 객체 생성
        gc = new Geocoder(this, Locale.KOREAN);
        //FloatingButton Click에 따른 메서드
        initFab();



////////~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~/////////
        for(int i=0;i<15;i++){
            mDatas.add(String.valueOf(i));
        }
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, mDatas);
        listview = (ListView) findViewById(R.id.listview);
        listview.setAdapter(adapter); //위에 만들어진 Adapter를 ListView에 설정 : xml에서 'entries'속성

        //ListView의 아이템 하나가 클릭되는 것을 감지하는 Listener객체 설정 (Button의 OnClickListener와 같은 역할)
        //listview.setOnItemClickListener(listener);

        //////~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~///////////////////
        mMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(mMetrics);

        ////////////////////////////////////////////////////////////

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
                    pwindo = new PopupWindow(layout, mWidthPixels-50, mHeightPixels - 300, true);

//                    pwindo = new PopupWindow(layout, mWidthPixels - 175, mHeightPixels - 450, true);
                    pwindo.setAnimationStyle(R.style.animationName);
                    pwindo.showAtLocation(layout, Gravity.CENTER|Gravity.BOTTOM, 0, 0);

                    btnClosePopup = (Button) layout.findViewById(R.id.closebtn_popup_0);
                    btnClosePopup.setOnClickListener(cancel_button_click_listener);
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
                            Toast.makeText(MainActivity.this, "체크상태 = " + isChecked, Toast.LENGTH_SHORT).show();

                            //체크상태가 true일때
                            if (isChecked == true) {
                                // 위치 정보 확인을 위해 정의한 메소드 호출
                                startLocationService();


                            }else{
                                contentsText.setText("GPS상태를 확인하세요.");
                            }


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

                Toast.makeText(getApplicationContext(), "Last Known Location : " + "Latitude : " + latitude + "\nLongitude:" + longitude, Toast.LENGTH_LONG).show();
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }

        Toast.makeText(getApplicationContext(), "위치 확인이 시작되었습니다. 로그를 확인하세요.", Toast.LENGTH_SHORT).show();

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

            String msg = "Latitude : "+ latitude + "\nLongitude:"+ longitude;
            Log.i("GPSListener", msg);

            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();

            // 위치 좌표를 이용해 주소를 검색하는 메소드 호출
            if(latitude!=null && longitude!=null)
            {
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
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

        } catch(IOException ex) {
            Log.d(TAG, "예외 : " + ex.toString());
        }

    }


    //RadioButton 눌렀을때의 반응
    private RadioButton.OnClickListener optionOnClickListener= new RadioButton.OnClickListener() {

        public void onClick(View v) {
            Log.i("OnClick~~",String.valueOf(option1.isChecked()));
            Log.i("OnClick~~",String.valueOf(option2.isChecked()));
            Log.i("OnClick~~",String.valueOf(option3.isChecked()));
        }
    };

    //팝업창 닫기
    private View.OnClickListener cancel_button_click_listener = new View.OnClickListener() {

                public void onClick(View v) {
                    pwindo.dismiss();
                }
            };

    //FloatingButton클릭에 따른 반응
    private void initFab() {
        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                initiatePopupWindow(0);
            }
        });
    }



}
