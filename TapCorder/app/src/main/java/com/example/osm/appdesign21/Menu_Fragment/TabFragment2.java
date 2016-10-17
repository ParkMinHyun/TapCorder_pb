package com.example.osm.appdesign21.Menu_Fragment;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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

import java.io.InputStream;
import java.text.DecimalFormat;
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

        // 이 사용자 현재 위치도 바꿔줘야해!!
        Marker mUserMarker = gMap.addMarker(new MarkerOptions().position(mCurrent_Location)
                .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("red_user", 120, 120))));


        LatLng short_policeStation = new LatLng(37.546757, 127.071366);
        Marker mPoliceMarker = gMap.addMarker(new MarkerOptions().position(short_policeStation)
                .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("policeoffice", 130, 130))));


        String strNumber = String.format("%.1f", CalculationByDistance(short_policeStation,mCurrent_Location)*1000);

        mDistance.setText(strNumber + "M ");
        mPoliceOffice_name.setText(" 서울 광진 경찰서 화양 지구대" );
        //mPoliceOffice_name.setText(mSpot_array.get(0).get_name());

        checkDangerousPermissions();
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

    /* 나중엔 JSON  파싱으로 네이버 지도에서 두 지점간의 거리를 정확하게 받아오는걸로 바꿀거임 */
    public double CalculationByDistance(LatLng StartP, LatLng EndP) {
        int Radius = 6371;// radius of earth in Km
        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double lon1 = StartP.longitude;
        double lon2 = EndP.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;
        double km = valueResult / 1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec = Integer.valueOf(newFormat.format(km));
        double meter = valueResult % 1000;
        int meterInDec = Integer.valueOf(newFormat.format(meter));

        return Radius * c;
    }

}