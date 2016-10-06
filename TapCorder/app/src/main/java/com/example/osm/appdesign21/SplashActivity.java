package com.example.osm.appdesign21;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;


public class SplashActivity extends Activity {

    private TextView teamTitle;
    SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        pref = new SharedPreferences(this);
        pref.removeAllPreferences("mode");

        Handler hd = new Handler();
        hd.postDelayed(new Runnable() {

            @Override
            public void run() {
                switch(pref.getValue("mode", "no", "mode")){
                    case "protector":
                        Intent intent_protector = new Intent(SplashActivity.this, NewMainActivity.class);
                        SplashActivity.this.startActivity(intent_protector);
                        finish();       // 3 초후 이미지를 닫아버림
                        break;
                    case "disabled":
                        Intent intent_disabled = new Intent(SplashActivity.this, MainActivity.class);
                        SplashActivity.this.startActivity(intent_disabled);
                        finish();       // 3 초후 이미지를 닫아버림
                        break;
                    default:
                        Intent intent = new Intent(SplashActivity.this, SelectModeActivity.class);
                        SplashActivity.this.startActivity(intent);
                        finish();       // 3 초후 이미지를 닫아버림
                        break;
                }
            }
        }, 2000);

        teamTitle=(TextView)findViewById(R.id.teamName);
        Typeface tf_team = Typeface.createFromAsset(getAssets(), "BMJUA_ttf.ttf");
        teamTitle.setTypeface(tf_team);
    }


}
