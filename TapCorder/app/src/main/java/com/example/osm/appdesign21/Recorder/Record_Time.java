package com.example.osm.appdesign21.Recorder;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import java.io.File;

public class Record_Time {

    public static String OUTPUT_FILE;
    public static int REC_TIME1 = 12000;
    public static int REC_TIME2 = 18000;
    public static int REC_TIME3 = 30000;

    private MediaPlayer mediaPlayer;
    private MediaRecorder recorder;

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

    public void beginRecording() throws Exception
    {
        ditchMediaRecorder();

        File outFile = new File(OUTPUT_FILE);

        if(outFile.exists())
        {
            outFile.delete();
        }

        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(OUTPUT_FILE);
        recorder.prepare();
        recorder.start();

        starttime = SystemClock.uptimeMillis();
        handler.postDelayed(updateTimer, 0);
    }

    public void stopRecording()
    {
        if (recorder != null){
            recorder.stop();
        }
    }

    public void time_reset()
    {
        starttime = 0L;
        timeInMilliseconds = 0L;
        timeSwapBuff = 0L;
        updatedtime = 0L;
        t = 1;
        secs = 0;
        mins = 0;
        milliseconds = 0;
        handler.removeCallbacks(updateTimer);
    }

    private void ditchMediaRecorder()
    {
        if(recorder != null)
        {
            recorder.release();
        }
    }
    public Runnable updateTimer = new Runnable() {

        public void run() {

            timeInMilliseconds = SystemClock.uptimeMillis() - starttime;

            updatedtime = timeSwapBuff + timeInMilliseconds;

            secs = (int) (updatedtime / 1000);
            mins = secs / 60;
            secs = secs % 60;
            milliseconds = (int) (updatedtime % 1000);
            //time.setText("" + mins + ":" + String.format("%02d", secs) + ":"
            //        + String.format("%03d", milliseconds));
            //time.setTextColor(Color.RED);
            handler.postDelayed(this, 0);
            Log.e("맥", mins + "");
            //if ( mins == 1)
            //{
              //  stopRecording();
                //time_reset();
            //}
        }

    };

}
