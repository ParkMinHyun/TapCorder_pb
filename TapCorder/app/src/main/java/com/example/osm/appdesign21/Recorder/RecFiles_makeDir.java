package com.example.osm.appdesign21.Recorder;

import android.os.Environment;
import android.util.Log;

import java.io.File;

public class RecFiles_makeDir {
    // 디렉토리를 만든다.
    public static String makeDir(String dirName) {
        String mRootPath = Environment.getExternalStorageDirectory()
                .getAbsolutePath() + File.separator + dirName; //dirName="progress_recorder"

        //녹음파일 저장소 위치 ///storage/emulated/0/progress_recorder
        Log.i("~~~~mRootPath~~~~",mRootPath);
        //외부저장소 절대적 경로 :getAbsolutePath ///storage/emulated/0
        Log.i("~~~~getAbsolutePath()",Environment.getExternalStorageDirectory().getAbsolutePath().toString());

        try {
            File fRoot = new File(mRootPath);
            if (fRoot.exists() == false) {
                if (fRoot.mkdirs() == false) {
                    throw new Exception("");
                }
            }
        } catch (Exception e) {
            mRootPath = "-1";
        }

        return mRootPath + "/";
    }
}