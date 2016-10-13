package com.example.osm.appdesign21.FTPServer;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.example.osm.appdesign21.SharedPreferences;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.SocketException;

/**
 * Created by mh on 2016-10-14.
 */
public class UploadBattery extends AsyncTask<Void, Void, Void> {
    private String mBt;
    private Context mContext;
    SharedPreferences pref;

    final static String TAG = "UploadBattery";

    public UploadBattery(Context context, String bt){
        mContext = context;
        mBt = bt;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.e(TAG, "업로드 시작");
        pref = new SharedPreferences(mContext);
    }

    @Override
    protected Void doInBackground(Void... params) {

        try {
            FTPClient mFTP = new FTPClient();

            mFTP.connect("myungho.iptime.org", 21); // ftp로 접속
            mFTP.login("pi", "raspberry"); // ftp 로그인 게정/비번
            mFTP.setFileType(FTP.BINARY_FILE_TYPE); // 바이너리 파일
            //mFTP.setBufferSize(1024 * 1024); // 버퍼 사이즈
            mFTP.enterLocalPassiveMode(); // 패시브 모드로 접속

            // 업로드 경로 수정(선택사항)
            mFTP.cwd("public"); // ftp 상의 업로드 디렉토리
            mFTP.mkd(pref.getValue("fpnum", "files", "fpnum")); // public 아래로 files 디렉토리 생성
            mFTP.cwd(pref.getValue("fpnum", "files", "fpnum")); // public/files 로 이동 (이 디렉토리로 업로드가 진행)

            generateNoteOnSD(mBt);

            FileInputStream in = new FileInputStream(new File("/storage/emulated/0/progress_recorder/bt.txt"));
            boolean result = mFTP.storeFile("bt.txt", in);
            in.close();
            if (result) Log.v("upload result", "succeeded");

            mFTP.logout();
            mFTP.disconnect(); // ftp disconnect

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        //this method will be running on UI thread
        Log.e(TAG, "bt업로드 완료");
    }

    private void generateNoteOnSD(String sBody){
        try{
            File btFile = new File("/storage/emulated/0/progress_recorder", "bt.txt");
            FileWriter writer = new FileWriter(btFile);
            writer.append(sBody);
            writer.flush();
            writer.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

}
