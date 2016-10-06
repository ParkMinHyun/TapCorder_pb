package com.example.osm.appdesign21.Menu_Fragment;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.osm.appdesign21.DB_Excel.SpotData;
import com.example.osm.appdesign21.DB_Excel.SpotsDbAdapter;
import com.example.osm.appdesign21.Map.GPSTracker;
import com.example.osm.appdesign21.R;
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

    private GPSTracker mGpsTracker;
    private SpotsDbAdapter mSpotDbAdapter;
    private List<SpotData> mSpot_array;

    private MapView mapView;
    private GoogleMap gMap;
    private LatLng mCurrent_Location;

    private View inflatedView;
    public TextView mDistance;
    public TextView mPoliceOffice_name;
    public Button mCall_btn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        LayoutInflater lf = getActivity().getLayoutInflater();
        View view = lf.inflate(R.layout.tab_fragment_2, null);
        mapView = (MapView) view.findViewById(R.id.gmap);
        mapView.onCreate(savedInstanceState);

        init_Property(view);
        init_DB();
        init_Map(view);

        return view;
    }

    public void init_Property(View view) {
        this.mSpotDbAdapter = new SpotsDbAdapter(this.getActivity());
        this.mGpsTracker = new GPSTracker(this.getContext());
        this.mSpot_array = new ArrayList<>();

        this.mDistance = (TextView)view.findViewById(R.id.between_distance);
        this.mPoliceOffice_name = (TextView)view.findViewById(R.id.policeStationName);
        this.mCall_btn = (Button)view.findViewById(R.id.call);

        mCall_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:"+mCall_btn.toString()));
                startActivity(callIntent);
            }
        });
    }

    public void init_Map(View view) {
        gMap = mapView.getMap();
        gMap.getUiSettings().setMyLocationButtonEnabled(false);

        mCurrent_Location = currentMyLocation();
        // 해당 위경도로 카메라 이동!
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(mCurrent_Location.latitude, mCurrent_Location.longitude), 15));

        LatLng short_policeStation = new LatLng(37.546618,127.071346);
        Marker mPoliceMarker = gMap.addMarker(new MarkerOptions().position(short_policeStation)
                .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("policeoffice", 130, 130))));
        Marker mUserMarker = gMap.addMarker(new MarkerOptions().position(mCurrent_Location)
                .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("userin", 140, 170))));

        String strNumber = String.format("%.1f", CalculationByDistance(short_policeStation,mCurrent_Location)*1000);

        mDistance.setText("약 : " + strNumber + "M ");
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

    public LatLng currentMyLocation() {

        // gpsTracker를 이용해 현재 위치를 받기
        if (mGpsTracker.canGetLocation()) {
            mCurrent_Location = new LatLng(mGpsTracker.getLatitude(), mGpsTracker.getLongitude());
        } else {
            mGpsTracker.showSettingsAlert();
        }
        return mCurrent_Location;
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