package com.example.osm.appdesign21.Menu_Fragment;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.example.osm.appdesign21.R;
import com.example.osm.appdesign21.Recorder.MyData;
import com.example.osm.appdesign21.Recorder.RecFiles_makeDir;
import com.example.osm.appdesign21.Recorder.TimeRecyclerAdapter;
import com.example.osm.appdesign21.SharedPreferences;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class TabFragment1 extends Fragment implements MediaPlayer.OnCompletionListener, TimeRecyclerAdapter.OnItemClickListener {

    private View inflatedView;
    private RecyclerView mTimeRecyclerView;
    private FrameLayout layout_MainMenu;
    private TimeRecyclerAdapter adapter;

    private ArrayList<MyData> dataset = null;
    private File[] fileList = null;
    private String mFilePath ;                   //녹음파일 디렉터리 위치

    private MediaPlayer mPlayer = null;
    private static final int PLAY_STOP = 0;
    private static final int PLAYING = 1;
    private int mPlayerState = PLAY_STOP;

    SharedPreferences pref;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        pref = new SharedPreferences(getContext());

        inflatedView=inflater.inflate(R.layout.tab_fragment_1, container, false);

        mTimeRecyclerView = (RecyclerView) inflatedView.findViewById(R.id.mTimeRecyclerView);
        mTimeRecyclerView.setHasFixedSize(true);

        adapter = new TimeRecyclerAdapter(getDataset());
        adapter.setOnItemClickListener(this);

        mTimeRecyclerView.setAdapter(adapter);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        mTimeRecyclerView.setLayoutManager(layoutManager);

        layout_MainMenu = (FrameLayout)inflatedView.findViewById(R.id.mainmenu);
        layout_MainMenu.getForeground().setAlpha(0);

        return inflatedView;
    }

    private ArrayList<MyData> getDataset() {
        dataset = new ArrayList<>();

        // SD카드에 디렉토리를 만든다.
        mFilePath = RecFiles_makeDir.makeDir(pref.getValue("disablePnum", "progress_recorder", "disablePnum"));
        fileList = getFileList(mFilePath);

        // list에 dataset 넣기 ( 핸드폰 안에 있는 음성 파일 )
        for(int i=0; i < fileList.length; i++){
            if(fileList[i].getName().contains("gps")){
                continue;
            }
            insertRecFile(i,fileList,dataset);
        }

        return dataset;
    }

    @Override
    public void onItemClick(int position) {
        mBtnStartPlayOnClick(adapter.getItem(position).getName());
    }
    public void onCompletion(MediaPlayer mp) {
        mPlayerState = PLAY_STOP; // 재생이 종료됨
    }

    /* SD카드 경로에 있는 음성파일 TapCorder List에 시간 및 날짜별로 정리한 뒤 넣기 */
    private void insertRecFile(int order, File[] fileList_copy, ArrayList<MyData> dataset_copy)
    {
        Date lastModifiedDate=new Date(fileList_copy[order].lastModified());
        Calendar lastModifiedCalendar = new GregorianCalendar();
        lastModifiedCalendar.setTime(lastModifiedDate);

        dataset_copy.add(new MyData(fileList_copy[order].getName(),lastModifiedCalendar.get(Calendar.YEAR),
                lastModifiedCalendar.get(Calendar.MONTH),
                lastModifiedCalendar.get(Calendar.DAY_OF_MONTH),
                lastModifiedCalendar.get(Calendar.HOUR_OF_DAY),
                lastModifiedCalendar.get(Calendar.MINUTE),
                lastModifiedCalendar.get(Calendar.SECOND)
        ));
    }

    // 특정 폴더의 파일 목록을 구해서 반환
    public File[] getFileList(String strPath) {
        File[] files;
        // 폴더 경로를 지정해서 File 객체 생성
        File fileRoot = new File(strPath);
        // 해당 경로가 폴더가 아니라면 함수 탈출

        if (fileRoot.isDirectory() == false) {
            return null;
        } else {
            files = fileRoot.listFiles();
        }

        return files;
    }


    /* List 녹음 클릭했을 경우 Play 시키기 */
    private void mBtnStartPlayOnClick(String mFileName) {
        if (mPlayerState == PLAY_STOP) {
            mPlayerState = PLAYING;
            startPlay(mFileName);
        } else if (mPlayerState == PLAYING) {
            mPlayerState = PLAY_STOP;
            stopPlay();
        }
    }
    // 재생 시작
    private void startPlay(String mFileName) {
        // 미디어 플레이어 생성
        if (mPlayer == null)
            mPlayer = new MediaPlayer();
        else
            mPlayer.reset();

        mPlayer.setOnCompletionListener(this);

        String fullFilePath = mFilePath + mFileName;
        try {
            mPlayer.setDataSource(fullFilePath);
            mPlayer.prepare();

        } catch (Exception e) {
        }

        if (mPlayerState == PLAYING) {
            try {
                mPlayer.start();
            } catch (Exception e) {
            }
        }
    }

    //재생 중지
    private void stopPlay() {
        // 재생을 중지하고
        mPlayer.stop();
        mPlayer.release();
        mPlayer = null;
    }


}
