package com.example.osm.appdesign21.Menu_Fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.osm.appdesign21.Pop;
import com.example.osm.appdesign21.R;
import com.example.osm.appdesign21.SelectModeActivity;
import com.example.osm.appdesign21.SharedPreferences;

public class TabFragment3 extends Fragment {

    public static TextView textView;
    private TextView tvBattery;

    private Button mChange; // 사용자 번호 변경 버튼

    SharedPreferences pref;

    private RelativeLayout changeModeLayout;
    private LinearLayout settings_layout;
    private ImageButton settings_btn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        LayoutInflater lf = getActivity().getLayoutInflater();
        View view = lf.inflate(R.layout.tab_fragment_3, null);
        pref = new SharedPreferences(getContext());
        // 저장해둔 사용자 번호 설정
        textView = (TextView)view.findViewById(R.id.textView_phoneNum);
        tvBattery = (TextView)view.findViewById(R.id.tvBattery);
        textView.setText("010" + pref.getValue("disablePnum", "번호를 저장하세요.", "disablePnum"));
        tvBattery.setText(pref.getValue("0", "98%", "bt"));
        mChange = (Button)view.findViewById(R.id.btn_change_num);
        mChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), Pop.class));
            }
        });

        //모드 변경 레이아웃
        changeModeLayout=(RelativeLayout)view.findViewById(R.id.changeMode);
        changeModeLayout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                changeModeDialog();
            }
        });


//        //설정 레이아웃 애니메이션
//        settings_layout=(LinearLayout)view.findViewById(R.id.layout_settings);
//
//        settings_btn=(ImageButton)view.findViewById(R.id.settings_down);
//        settings_btn.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View view) {
//                Log.i("settings_btn","설정 눌렸냐");
//                if(settings_layout.getVisibility()==View.VISIBLE){
//                    settings_layout.setVisibility(View.GONE);
////                    settings_layout.setAlpha(0.3f);
////                    settings_layout.startAnimation(AnimationUtils.loadAnimation(getActivity(),R.anim.slide_in));
//
//                }
//                else if(settings_layout.getVisibility()==View.GONE){
//                    settings_layout.setVisibility(View.VISIBLE);
////                    settings_layout.setAlpha(1.0f);
////                    settings_layout.startAnimation(AnimationUtils.loadAnimation(getActivity(),R.anim.slide_out));
//                }
//            }
//        });

        return view;
    }

    //모드 변경 레이아웃 클릭시 뜨는 다이알로그창
    private void changeModeDialog(){
        AlertDialog.Builder alt_bld = new AlertDialog.Builder(getContext());
        alt_bld.setMessage("모드를 변경하시겠습니까?\n'예'를 누르시면 모드 선택 초기화면으로 돌아갑니다.").setCancelable(
                false).setPositiveButton("예",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Action for 'Yes' Button
                        pref.removeAllPreferences("mode");
                        Intent intent = new Intent(getActivity(), SelectModeActivity.class);
                        getActivity().startActivity(intent);
                        getActivity().finish();
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



}