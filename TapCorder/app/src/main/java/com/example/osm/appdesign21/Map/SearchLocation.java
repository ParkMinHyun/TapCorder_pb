package com.example.osm.appdesign21.Map;

import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;


public class SearchLocation {

    private static final String TAG = "SearchLocation";
    private GoogleMap gMap;
    private Geocoder geocoder;

    private LatLng searchLatLng = null;
    private Marker policeOffice_marker;


    private LatLng current_Location;
    private Double searchLatLng_latitude;
    private Double searchLatLng_longitude;


    // 위치 찾기 위한 메소드
    public void findLocation(String searchStr) {
        List<Address> addressList = null;

        try {
            // Geocoder객체인 gc에서 Location이름에 대한 정보를 List에 담기.
            addressList = geocoder.getFromLocationName(searchStr, 1);

            if (addressList.size() != 0) {
                // 위경도값 받기.
                searchLatLng_latitude = addressList.get(0).getLatitude();
                searchLatLng_longitude = addressList.get(0).getLongitude();
                searchLatLng = new LatLng(searchLatLng_latitude, searchLatLng_longitude);

                policeOffice_marker = gMap.addMarker(new MarkerOptions().position(new LatLng(searchLatLng_latitude,searchLatLng_longitude)));
                Log.i(TAG, "위도" + searchLatLng.latitude + ", 경도 " +searchLatLng.longitude);
                policeOffice_marker.showInfoWindow();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
