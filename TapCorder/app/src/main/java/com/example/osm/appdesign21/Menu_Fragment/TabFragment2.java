package com.example.osm.appdesign21.Menu_Fragment;

import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.osm.appdesign21.DB_Excel.SpotData;
import com.example.osm.appdesign21.DB_Excel.SpotsDbAdapter;
import com.example.osm.appdesign21.Map.GPSTracker;
import com.example.osm.appdesign21.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.io.InputStream;
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
    private Marker mPoliceMarker;
    private Marker mPoliceMarker2;
    private Marker mUserMarker;
    private LatLng mCurrent_Location;

    private View inflatedView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.tab_fragment_2, null);
        mapView = (MapView) view.findViewById(R.id.gmap);
        mapView.onCreate(savedInstanceState);


        init_Property();
        init_Map();
        init_DB();

        return view;
    }
    public void init_Property() {
        this.mSpotDbAdapter = new SpotsDbAdapter(this.getActivity());
        this.mGpsTracker = new GPSTracker(this.getContext());
        this.mSpot_array = new ArrayList<>();
    }
    public void init_Map() {
        gMap = mapView.getMap();
        gMap.getUiSettings().setMyLocationButtonEnabled(false);

        mCurrent_Location = currentMyLocation();
        // 해당 위경도로 카메라 이동!
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(mCurrent_Location.latitude, mCurrent_Location.longitude), 15));


        LatLng a = new LatLng(37.546618,127.071346);
        LatLng b = new LatLng(mCurrent_Location.latitude,mCurrent_Location.longitude);
        /*mPoliceMarker = gMap.addMarker(new MarkerOptions().position(new LatLng(37.546618,127.071346))
                .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("policeoffice",130,130))));
        mPoliceMarker2 = gMap.addMarker(new MarkerOptions().position(new LatLng(37.560487,127.081460))
                .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("policeoffice",150,150))));
        mUserMarker = gMap.addMarker(new MarkerOptions().position(new LatLng(mCurrent_Location.latitude,mCurrent_Location.longitude))
                .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("userin",140,170))));
        Toast.makeText(getContext(),String.valueOf(CalculationByDistance(a,b)),Toast.LENGTH_SHORT).show();
        */
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

}