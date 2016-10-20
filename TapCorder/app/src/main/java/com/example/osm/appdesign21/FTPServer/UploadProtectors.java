package com.example.osm.appdesign21.FTPServer;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.example.osm.appdesign21.SharedPreferences;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.IOException;
import java.net.SocketException;

/**
 * Created by mh on 2016-10-20.
 */
public class UploadProtectors extends AsyncTask<Void, Void, Void> {
    private String pnum;
    private Context mContext;
    SharedPreferences pref;
    private String mProtector;

    final static String TAG = "UploadProtectors";

    public UploadProtectors(Context context, String protector){
        mContext = context;
        mProtector = protector;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.e(TAG, "보호자 등록 시작");
        pref = new SharedPreferences(mContext);
        pnum = pref.getValue("fpnum", "files", "fpnum");
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
            mFTP.mkd(pnum+"pt"); // public 아래로 files 디렉토리 생성
            mFTP.cwd(pnum+"pt"); // public/files 로 이동 (이 디렉토리로 업로드가 진행)

            mFTP.mkd(mProtector);

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
        Log.e(TAG, "보호자 등록 완료");
    }
}
