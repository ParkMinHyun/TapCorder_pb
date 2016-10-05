package com.example.osm.appdesign21.Menu_Fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.osm.appdesign21.DB_Excel.SpotData;
import com.example.osm.appdesign21.DB_Excel.SpotsDbAdapter;
import com.example.osm.appdesign21.Map.GPSTracker;
import com.example.osm.appdesign21.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.List;

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

        return view;
    }
    public void init_Property() {
        this.mSpotDbAdapter = new SpotsDbAdapter(this.getActivity());
        this.mGpsTracker = new GPSTracker(this.getContext());
        this.mSpot_array = new ArrayList<>();
    }
}