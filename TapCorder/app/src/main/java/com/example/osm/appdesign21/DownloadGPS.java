package com.example.osm.appdesign21;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;

/**
 * Created by mh on 2016-10-11.
 */
public class DownloadGPS extends AsyncTask<Void, Void, Void> {

    final static String TAG = "DownloadGPS";
    private String mfileName;
    SharedPreferences pref;

    public DownloadGPS(String pNum){
        mfileName = pNum;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.e(TAG, "다운로드 시작");
        pref = new SharedPreferences(NewMainActivity.mContext);
    }

    @Override
    protected Void doInBackground(Void... params) {

        FTPClient ftpClient = new FTPClient(); // ftpclient object 생성

        try {
            ftpClient.connect("myungho.iptime.org", 21); // (주소, port)
            ftpClient.login("pi", "raspberry");

            ftpClient.setFileType(FTP.BINARY_FILE_TYPE); // 파일형식 바이너리 타입
            ftpClient.enterLocalPassiveMode(); // 패시브 모드로 접속

            ftpClient.cwd("public"); // ftp 상의 업로드 디렉토리
            ftpClient.cwd(mfileName); // 다운로드 디렉토리로 이동

            File downloadFile = new File("/storage/emulated/0/" + mfileName + "/gps.txt"); // 저장할 파일 이름(local file 형식으로 된 저장할 위치)
            File parentDir = downloadFile.getParentFile();
            if (!parentDir.exists()) {
                parentDir.mkdir();
            }
            OutputStream outputStream = null;
            try {
                outputStream = new BufferedOutputStream(new FileOutputStream(downloadFile)); // 파일 담기
                ftpClient.retrieveFile("/home/pi/" + mfileName + "/gps.txt", outputStream); // 서버로부터 파일 저장(서버 파일 경로, outputStream)

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            ftpClient.logout();
            ftpClient.disconnect();





        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        //ReadGPS();
    }

    public void ReadGPS(){
        String text = null;
        String[] gps = null;
        try{
            File file = NewMainActivity.mContext.getFileStreamPath("gps.txt");
            FileInputStream fIs = new FileInputStream(file);
            Reader in = new InputStreamReader(fIs);
            int size = fIs.available();
            char[] buffer = new char[size];
            in.read(buffer);
            in.close();

            text = new String(buffer);

            if(!text.equals("")){
                gps = text.split(",");
                pref.putValue("0", gps[0], "lati");
                pref.putValue("0", gps[1], "longi");
            }else {

            }

        } catch (IOException e){
            throw new RuntimeException(e);
        }


    }

}