package com.example.osm.appdesign21;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.RelativeLayout;

public class SelectModeActivity extends AppCompatActivity {

    SharedPreferences pref;

    private RelativeLayout mProtectorLayout;
    private RelativeLayout mDisabledLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_mode);

        pref = new SharedPreferences(this);
        pref.removeAllPreferences("mode");
        pref.removeAllPreferences("disablePnum");

        mProtectorLayout = (RelativeLayout)findViewById(R.id.protector);
        mProtectorLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pref.putValue("mode","protector","mode");
                Intent intent = new Intent(SelectModeActivity.this, NewMainActivity.class);
                SelectModeActivity.this.startActivity(intent);
                finish();
            }
        });
        mDisabledLayout = (RelativeLayout)findViewById(R.id.disabled);
        mDisabledLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TelephonyManager tMgr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
                String mPhoneNumber = tMgr.getLine1Number();
                //pref.putValue("fpnum",mPhoneNumber.substring(mPhoneNumber.length()-8),"fpnum");
                pref.putValue("mode", "disabled", "mode");
                Intent intent = new Intent(SelectModeActivity.this, MainActivity.class);
                SelectModeActivity.this.startActivity(intent);
                finish();
            }
        });
    }
}
