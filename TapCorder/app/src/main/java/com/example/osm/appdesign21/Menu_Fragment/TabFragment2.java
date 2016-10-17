package com.example.osm.appdesign21.Menu_Fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.osm.appdesign21.DB_Excel.SpotData;
import com.example.osm.appdesign21.DB_Excel.SpotsDbAdapter;
import com.example.osm.appdesign21.NewMainActivity;
import com.example.osm.appdesign21.R;
import com.example.osm.appdesign21.SharedPreferences;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import jxl.Sheet;
import jxl.Workbook;

public class TabFragment2 extends Fragment {

    private SpotsDbAdapter mSpotDbAdapter;
    private List<SpotData> mSpot_array;

    private MapView mapView;
    private GoogleMap gMap;
    private LatLng mCurrent_Location;
    private double mCamera_Position_latitude;
    private double mCamera_Position_longitude;
    private float mZoomLevel = 15;

    private View inflatedView;
    public TextView mDistance;
    public TextView mPoliceOffice_name;
    public Button mCall_btn;
    private FloatingActionButton mZoonIn_btn;
    private FloatingActionButton mZoonOut_btn;
    private RelativeLayout layout_police;
    private RelativeLayout layout_user;


    public static StringBuilder URL = new StringBuilder("https://m.map.naver.com/spirra/findCarRoute.nhn?route=route3&output=json&coord_type=latlng&search=0&car=0&mileage=12.4&start=127.0738840,37.5514706&destination=126.9522394,37.4640070");
    LatLng short_policeStation = new LatLng(37.546757, 127.071366);
    SharedPreferences pref;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        pref = new SharedPreferences(NewMainActivity.mContext);
        LayoutInflater lf = getActivity().getLayoutInflater();
        View view = lf.inflate(R.layout.tab_fragment_2, null);

        init_Property(view,savedInstanceState);
        init_DB();
        init_Map(view);
        set_ClickEvent();


        return view;
    }

    public void init_Property(View view,Bundle savedInstanceState) {
        this.mSpotDbAdapter = new SpotsDbAdapter(this.getActivity());
        this.mSpot_array = new ArrayList<>();

        this.mDistance = (TextView)view.findViewById(R.id.between_distance);
        this.mPoliceOffice_name = (TextView)view.findViewById(R.id.policeStationName);
        this.mCall_btn = (Button)view.findViewById(R.id.call_police);
        this.mZoonIn_btn = (FloatingActionButton)view.findViewById(R.id.zoom_in);
        this.mZoonOut_btn = (FloatingActionButton)view.findViewById(R.id.zoom_out);

        layout_police=(RelativeLayout)view.findViewById(R.id.sub_layout_police);
        layout_user=(RelativeLayout)view.findViewById(R.id.sub_layout_user);

        mapView = (MapView) view.findViewById(R.id.gmap);
        mapView.onCreate(savedInstanceState);

    }

    public void set_ClickEvent(){

        mCall_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:"+mCall_btn.toString()));
                startActivity(callIntent);
            }
        });

        mZoonIn_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getCameraPosition_ConvertZoom("ZoomIn");
            }
        });
        mZoonOut_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getCameraPosition_ConvertZoom("ZoomOut");
            }
        });
        gMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

            public boolean onMarkerClick(Marker marker) {

                //사용자 마커일때
                if (marker.getId().equals("m0")) {
                    layout_user.setVisibility(View.VISIBLE);
                    layout_police.setVisibility(View.GONE);
                }
                //경찰 마커일때
                else if(marker.getId().equals("m1")){
                    layout_police.setVisibility(View.VISIBLE);
                    layout_user.setVisibility(View.GONE);
                }

                return false;
            }
        });
    }


    public void init_Map(View view) {
        gMap = mapView.getMap();
        gMap.getUiSettings().setMyLocationButtonEnabled(false);

        // User 현재 위치
        mCurrent_Location = new LatLng(Double.parseDouble(pref.getValue("0", "37.546757", "lati")), Double.parseDouble(pref.getValue("0", "127.074007", "longi")));
        // 해당 위경도로 카메라 이동! --> 나중엔 서버에서 사용자 현재위치 받아서 위, 경도값 넣어줘~
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                mCurrent_Location, mZoomLevel));

        Marker mUserMarker = gMap.addMarker(new MarkerOptions().position(mCurrent_Location)
                .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("red_user", 120, 120))));
        Marker mPoliceMarker = gMap.addMarker(new MarkerOptions().position(short_policeStation)
                .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("policeoffice", 130, 130))));

        ExecuteJSONparsing();

        mPoliceOffice_name.setText(" 서울 광진 경찰서 화양 지구대" );

        checkDangerousPermissions();
    }

    private void ExecuteJSONparsing()
    {
        if (isConnected()) {
        }
        URL = new StringBuilder("https://m.map.naver.com/spirra/findCarRoute.nhn?route=route3&output=json&coord_type=latlng&search=0&car=0&mileage=12.4" +
            "&start=" + mCurrent_Location.longitude + "," + mCurrent_Location.latitude + "&destination=" + short_policeStation.longitude +"," + short_policeStation.latitude);
        // 이 URL에 대한 Json 파싱시작 -> HttpAsyncTask() 메소드로 감.
        new HttpAsyncTask().execute(URL.toString());
    }

    private void init_DB() {
        //엑셀파일 데이터를 데이터베이스에 저장
        mSpotDbAdapter.open();

        Cursor result = mSpotDbAdapter.fetchAllSpots();
        if (result.getCount() == 0)
            copyExcelDataToDbSpot();

        result.moveToFirst();
        while (!result.isAfterLast()) {

            SpotData spotData = new SpotData(result.getString(0), result.getString(1), result.getString(2), result.getString(3));
            mSpot_array.add(spotData);

            result.moveToNext();
        }

        result.close();
        mSpotDbAdapter.close();
    }

    public void getCameraPosition_ConvertZoom(String ZoomState)
    {
        switch (ZoomState){
            case "ZoomIn":
                mZoomLevel += 0.3;
                break;
            case "ZoomOut" :
                mZoomLevel -= 0.3;
                break;
        }
        /* 현재 내 Map 가운데의 위,경도를 받은 후 그 곳에 Zoom 상태를 바꾸기 */
        mCamera_Position_latitude = gMap.getCameraPosition().target.latitude;
        mCamera_Position_longitude = gMap.getCameraPosition().target.longitude;
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(mCamera_Position_latitude,mCamera_Position_longitude), mZoomLevel));
    }

    @Override
    public void onStart(){
        super.onStart();
    }
    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    // 이미지 줄여주는 메소드
    public Bitmap resizeMapIcons(String iconName, int width, int height){
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),getResources().getIdentifier(iconName, "drawable",
                this.getActivity().getPackageName()));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }

    private void checkDangerousPermissions() {
        String[] permissions = {
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
        };

        int permissionCheck = PackageManager.PERMISSION_GRANTED;
        for (int i = 0; i < permissions.length; i++) {
            permissionCheck = ContextCompat.checkSelfPermission(this.getActivity(), permissions[i]);
            if (permissionCheck == PackageManager.PERMISSION_DENIED) {
                break;
            }
        }

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this.getActivity(), "권한 있음", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this.getActivity(), "권한 없음", Toast.LENGTH_LONG).show();

            if (ActivityCompat.shouldShowRequestPermissionRationale(this.getActivity(), permissions[0])) {
                Toast.makeText(this.getActivity(), "권한 설명 필요함.", Toast.LENGTH_LONG).show();
            } else {
                ActivityCompat.requestPermissions(this.getActivity(), permissions, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this.getActivity(), permissions[i] + " 권한이 승인됨.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this.getActivity(), permissions[i] + " 권한이 승인되지 않음.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void copyExcelDataToDbSpot() {
        Workbook workbook = null;
        Sheet sheet = null;

        try {
            InputStream is = this.getResources().getAssets().open("police_office.xls");
            workbook = Workbook.getWorkbook(is);

            if (workbook != null) {

                sheet = workbook.getSheet(0);

                if (sheet != null) {

                    int nMaxColumn = 2;
                    int nRowStartIndex = 0;
                    int nRowEndIndex = sheet.getColumn(nMaxColumn - 1).length - 1;
                    int nColumnStartIndex = 0;

                    mSpotDbAdapter.open();
                    for (int nRow = nRowStartIndex + 1; nRow <= nRowEndIndex; nRow++) {

                        String id = sheet.getCell(nColumnStartIndex, nRow).getContents();
                        String name = sheet.getCell(nColumnStartIndex + 1, nRow).getContents();
                        String addr = sheet.getCell(nColumnStartIndex + 2, nRow).getContents();
                        String tel_num = sheet.getCell(nColumnStartIndex + 3, nRow).getContents();

                        mSpotDbAdapter.createSpot(id, name, addr, tel_num);
                    }
                    mSpotDbAdapter.close();

                } else {
                    System.out.println("Sheet is null!!");
                }
            } else {
                System.out.println("WorkBook is null!!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (workbook != null) {
                workbook.close();
            }
        }
    }

    public static String GET(String url) {
        InputStream inputStream = null;
        String result = "";
        try {
            // create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // make GET request to the given URL
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));

            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // convert inputstream to string
            if (inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        return result;
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while ((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;
    }

    public boolean isConnected() {
        ConnectivityManager connMgr = (ConnectivityManager)getContext().getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }

    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            // GET 메소드로 이동동
            return GET(urls[0]);
        }

        // onPostExecute displays the results of the AsyncTask.
        // AsyncTask 작업이 완료된 후 실행되는 Method -> Json 파싱 다듬기 및 Alert창 띄우기
        @Override
        protected void onPostExecute(String result) {
            int index_dist = result.indexOf("totalDistance");                                 // Json 파싱 후 전체 Text에서 짜르고 싶은 부분을 나누기 위해
            int totalTime = result.indexOf("totalTime");                                      // 첫 index 값 과 끝 index 값 저장을 위한 변수 생성.

            StringBuilder stringBuilder = new StringBuilder(result);                          // Json 파싱한 Text ( result )를 StringBuilder에 넣기
            stringBuilder.delete(0, index_dist);
            stringBuilder.delete(totalTime - index_dist, stringBuilder.length());             // Json 파싱 결과값 다듬기

            String a = stringBuilder.toString();                                              // 총 거리 정보를
            String[] split_stringBuilder = a.split("[:,]");                                   // 배열부분에 담는다
            StringBuilder distance = new StringBuilder(split_stringBuilder[1]);

            mDistance.setText(distance.toString()+ " M");
        }
    }
}