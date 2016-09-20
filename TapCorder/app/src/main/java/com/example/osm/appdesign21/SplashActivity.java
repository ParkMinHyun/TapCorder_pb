package com.example.osm.appdesign21;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;


public class SplashActivity extends Activity {

    private TextView teamTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        Handler hd = new Handler();
        hd.postDelayed(new Runnable() {

            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                SplashActivity.this.startActivity(intent);
                finish();       // 3 초후 이미지를 닫아버림
            }
        }, 2000);

        teamTitle=(TextView)findViewById(R.id.teamName);
        Typeface tf_team = Typeface.createFromAsset(getAssets(), "BMJUA_ttf.ttf");
        teamTitle.setTypeface(tf_team);
    }


}
