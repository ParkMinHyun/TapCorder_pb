package com.example.osm.appdesign21.Menu_Fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.osm.appdesign21.Pop;
import com.example.osm.appdesign21.R;
import com.example.osm.appdesign21.SharedPreferences;

public class TabFragment3 extends Fragment {

    public TextView textView;
    private Button mChange; // 사용자 번호 변경 버튼
    SharedPreferences pref;

    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context ctxt, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            textView.setText(String.valueOf(level) + "%");
            //Toast.makeText(getContext(),String.valueOf(level) + "%",Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        LayoutInflater lf = getActivity().getLayoutInflater();
        View view = lf.inflate(R.layout.tab_fragment_3, null);
        pref = new SharedPreferences(getContext());
        // 저장해둔 사용자 번호 설정
        textView = (TextView)view.findViewById(R.id.textView_phoneNum);
        textView.setText(pref.getValue("disablePnum", "번호를 저장하세요.", "disablePnum"));
        mChange = (Button)view.findViewById(R.id.btn_change_num);
        mChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), Pop.class));
            }
        });


        getActivity().registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        return view;
    }




}