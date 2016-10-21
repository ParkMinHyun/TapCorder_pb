package com.example.osm.appdesign21;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.example.osm.appdesign21.FTPServer.CheckProtector;
import com.example.osm.appdesign21.FTPServer.DownloadBattery;
import com.example.osm.appdesign21.FTPServer.DownloadGPS;
import com.example.osm.appdesign21.FTPServer.DownloadTask;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutionException;


public class NewMainActivity extends AppCompatActivity {

    private static String TAG = "NewMainActivity";
    SharedPreferences pref;
    private boolean checkProtector;
    public static Context mContext;
    FrameLayout frameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_activity_main);

        mContext = this;

        init_layout();

        pref = new SharedPreferences(this);
        if(pref.getValue("disablePnum", "no", "disablePnum").equals("no")){
            startActivity(new Intent(NewMainActivity.this, Pop.class));
            finish();
        } else{
            Toast.makeText(this, "보호자 확인중입니다.", Toast.LENGTH_SHORT).show();
            TelephonyManager tMgr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
            String mPhoneNumber = tMgr.getLine1Number();
            String Phonenum = mPhoneNumber.substring(mPhoneNumber.length()-8);

            try {
                checkProtector = new CheckProtector(Phonenum, pref.getValue("disablePnum", "no", "disablePnum")).execute().get();
                if(checkProtector == false){
                    Toast.makeText(this ,"보호자로 등록되어있지 않습니다.\n등록 후 확인하실 수 있습니다.", Toast.LENGTH_SHORT).show();
                    pref.removeAllPreferences("mode");
                    pref.removeAllPreferences("disablePnum");
                    startActivity(new Intent(NewMainActivity.this, SelectModeActivity.class));
                    finish();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            try {
                new DownloadTask(pref.getValue("disablePnum","files","disablePnum")).execute().get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            new DownloadGPS(pref.getValue("disablePnum", "files", "disablePnum")).execute();
            new DownloadBattery(pref.getValue("disablePnum","files", "disablePnum")).execute();
            new CountDownTimer(4000, 1000) {
                @Override
                public void onTick(long l) {

                }

                @Override
                public void onFinish() {
                    ReadGPS();
                    ReadBattery();
                }
            }.start();
        }
    }

    public void init_layout()
    {
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("음성 파일"));
        tabLayout.addTab(tabLayout.newTab().setText("사용자 위치"));
        tabLayout.addTab(tabLayout.newTab().setText("기타"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        final PagerAdapter adapter = new PagerAdapter
                (getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        frameLayout = (FrameLayout)findViewById(R.id.mainmenu_new);
        frameLayout.getForeground().setAlpha(0);
    }

    public void ReadGPS(){
        String line = null;
        String[] gps = null;
        try{
            FileInputStream fileInputStream = new FileInputStream(new File("/storage/emulated/0/" + pref.getValue("disablePnum", "progress_recorder", "disablePnum") + "/gps.txt"));
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder stringBuilder = new StringBuilder();

            while((line = bufferedReader.readLine()) != null){
                stringBuilder.append(line + System.getProperty("line.separator"));
            }
            fileInputStream.close();
            line = stringBuilder.toString();
            bufferedReader.close();

            gps = line.split(",");
            pref.putValue("0", gps[0], "lati");
            pref.putValue("0", gps[1], "longi");

        } catch(FileNotFoundException e){

        } catch(IOException ex){

        }
    }
    public void ReadBattery(){
        String line = null;
        try{
            FileInputStream fileInputStream = new FileInputStream(new File("/storage/emulated/0/" + pref.getValue("disablePnum", "progress_recorder", "disablePnum") + "/bt.txt"));
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder stringBuilder = new StringBuilder();

            while((line = bufferedReader.readLine()) != null){
                stringBuilder.append(line + System.getProperty("line.separator"));
            }
            fileInputStream.close();
            line = stringBuilder.toString();
            bufferedReader.close();

            pref.putValue("0", line, "bt");

        } catch(FileNotFoundException e){

        } catch(IOException ex){

        }
    }
}