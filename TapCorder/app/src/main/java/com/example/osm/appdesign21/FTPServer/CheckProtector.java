package com.example.osm.appdesign21.FTPServer;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.IOException;

/**
 * Created by mh on 2016-10-21.
 */
public class CheckProtector extends AsyncTask<Void, Boolean, Boolean> {

    final static String TAG = "DownloadBattery";
    private String mfileName;
    private String myPhoneNum;
    private boolean checkProtector;

    public CheckProtector(String protectorNum, String paramDisableNum){
        mfileName = paramDisableNum;
        myPhoneNum = protectorNum;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        checkProtector = false;
        Log.e(TAG, "보호자 확인 시작");
    }

    @Override
    protected Boolean doInBackground(Void... params) {

        FTPClient ftpClient = new FTPClient(); // ftpclient object 생성

        try {
            ftpClient.connect("myungho.iptime.org", 21); // (주소, port)
            ftpClient.login("pi", "raspberry");

            ftpClient.setFileType(FTP.BINARY_FILE_TYPE); // 파일형식 바이너리 타입
            ftpClient.enterLocalPassiveMode(); // 패시브 모드로 접속

            ftpClient.cwd("public"); // ftp 상의 업로드 디렉토리
            ftpClient.cwd(mfileName + "pt"); // 다운로드 디렉토리로 이동

            String[] fileNames = ftpClient.listNames();
            for(int i=0; i < fileNames.length; i++){
                if(fileNames[i].equals(myPhoneNum)){
                    checkProtector = true;
                    break;
                }
            }

            ftpClient.logout();
            ftpClient.disconnect();





        } catch (IOException e) {
            e.printStackTrace();
        }
        return checkProtector;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        Log.e(TAG, "보호자 확인 완료");


    }
}