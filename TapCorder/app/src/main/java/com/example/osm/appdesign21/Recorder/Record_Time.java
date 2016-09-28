package com.example.osm.appdesign21.Recorder;

import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

public class Record_Time {

    public static int REC_TIME1 = 12000;
    public static int REC_TIME2 = 18000;
    public static int REC_TIME3 = 30000;

    /* stopWatch에 쓰여지는 변수들 */
    long starttime = 0L;
    long timeInMilliseconds = 0L;
    long timeSwapBuff = 0L;
    long updatedtime = 0L;
    int t = 1;
    int secs = 0;
    int mins = 0;
    int milliseconds = 0;
    Handler handler = new Handler();


    public Runnable updateTimer = new Runnable() {

        public void run() {

            timeInMilliseconds = SystemClock.uptimeMillis() - starttime;

            updatedtime = timeSwapBuff + timeInMilliseconds;

            secs = (int) (updatedtime / 1000);
            mins = secs / 60;
            secs = secs % 60;
            milliseconds = (int) (updatedtime % 1000);
            handler.postDelayed(this, 0);
            Log.e("맥", mins + "");
        }

    };

}
