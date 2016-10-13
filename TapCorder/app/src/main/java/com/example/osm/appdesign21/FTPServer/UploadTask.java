package com.example.osm.appdesign21.FTPServer;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.example.osm.appdesign21.SharedPreferences;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.SocketException;

/**
 * Created by mh on 2016-09-14.
 */
public class UploadTask extends AsyncTask<Void, Void, Void>
{
    private String mUploadfName;
    private Context mContext;
    SharedPreferences pref;
    public UploadTask(String fname, Context context){
        mUploadfName = fname;
        mContext = context;
    }

    final static String TAG = "UploadTask";
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.e(TAG, "업로드 시작");
        pref = new SharedPreferences(mContext);
    }
    @Override
    protected Void doInBackground(Void... params) {

        try{
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

            String data = mUploadfName;

            String[] files = mFTP.listNames(); // public/files 폴더 안에 있는 파일 리스트 가져오기
            for(int i=0; i<files.length; i++){ // 파일 갯수만큼 반복
                if(files[i].equals(mUploadfName.substring(38))){ // 파일이 존재한다면
                    return null; // Task종료
                }
            }

            FileInputStream in = new FileInputStream(new File(data));
            boolean result = mFTP.storeFile(mUploadfName.substring(38), in);
            in.close();
            if(result) Log.v("upload result", "succeeded");

            mFTP.logout();
            mFTP.disconnect(); // ftp disconnect

        } catch (SocketException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        //this method will be running on UI thread
        Log.e(TAG, "업로드 완료");
    }

}
