package com.example.osm.appdesign21;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.osm.appdesign21.FTPServer.DownloadTask;
import com.example.osm.appdesign21.Menu_Fragment.TabFragment3;

public class Pop extends AppCompatActivity {

    private Button mStore;
    private Button mCancel;
    private TextView mPhoneNumView_Pop;
    private String mPhoneNum;
    SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_pop);

        mPhoneNumView_Pop = (TextView)findViewById(R.id.editText_pop);

        pref = new SharedPreferences(this);
        // 팝업 윈도우로 사용하기 위한 설정
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int heigth = dm.heightPixels;
        getWindow().setLayout((int)(width*.8), (int)(heigth*.4));

        mStore = (Button)findViewById(R.id.btn_store);
        mStore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPhoneNum = mPhoneNumView_Pop.getText().toString();
                if(mPhoneNum.length() != 11){
                    Toast.makeText(Pop.this, "올바르지 않은 번호입니다.",Toast.LENGTH_SHORT).show();
                }else{
                    pref.putValue("disablePnum",mPhoneNum,"disablePnum");
                    new DownloadTask(pref.getValue("disablePnum","files","disablePnum")).execute();
                    TabFragment3.textView.setText(mPhoneNum);
                    finish();
                }
            }
        });
        mCancel = (Button)findViewById(R.id.btn_cancel_pop);
        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }
}
