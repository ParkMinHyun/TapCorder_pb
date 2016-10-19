package com.example.osm.appdesign21.FTPServer;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by mh on 2016-09-19.
 */
public class DownloadTask extends AsyncTask<Void, Void, Void> {

    final static String TAG = "DownloadTask";
    private String mfileName;
    private int j;


    public DownloadTask(String pNum){
        mfileName = pNum;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.e(TAG, "다운로드 시작");
        j = 0;
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

            String[] files = ftpClient.listNames();
            for(int i=0; i<files.length;i++) {
                if(files[i].equals("gps.txt") || files[i].equals("bt.txt")){
                    continue;
                }else{
                    j++;
                }

                File downloadFile = new File("/storage/emulated/0/" + mfileName + "/recordFile" + (j) + ".amr"); // 저장할 파일 이름(local file 형식으로 된 저장할 위치)
                if(downloadFile.exists()){
                    continue; // 다운로드 하려는 파일이 존재하면 다운로드 하지않음
                }
                File parentDir = downloadFile.getParentFile();
                if (!parentDir.exists()) {
                    parentDir.mkdir();
                }
                OutputStream outputStream = null;
                try {
                    outputStream = new BufferedOutputStream(new FileOutputStream(downloadFile)); // 파일 담기
                    ftpClient.retrieveFile("/home/pi/" + mfileName + "/recordFile" + (j) + ".amr", outputStream); // 서버로부터 파일 저장(서버 파일 경로, outputStream)

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
    }

}

