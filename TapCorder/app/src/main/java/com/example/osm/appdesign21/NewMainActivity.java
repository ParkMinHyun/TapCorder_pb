package com.example.osm.appdesign21;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;


public class NewMainActivity extends AppCompatActivity {

    private static String TAG = "NewMainActivity";
    SharedPreferences pref;
    public static Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_activity_main);

        mContext = this;

        init_layout();

        pref = new SharedPreferences(this);
        if(pref.getValue("disablePnum", "no", "disablePnum").equals("no")){
            startActivity(new Intent(NewMainActivity.this, Pop.class));
        } else{
            new DownloadTask(pref.getValue("disablePnum","files","disablePnum")).execute();
            new DownloadGPS(pref.getValue("disablePnum", "files", "disablePnum")).execute();
            new CountDownTimer(5000, 1000) {
                @Override
                public void onTick(long l) {

                }

                @Override
                public void onFinish() {
                    ReadGPS();
                }
            }.start();
        }
    }

    public void init_layout()
    {
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Tab 1"));
        tabLayout.addTab(tabLayout.newTab().setText("Tab 2"));
        tabLayout.addTab(tabLayout.newTab().setText("Tab 3"));
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
}