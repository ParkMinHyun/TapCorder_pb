package com.example.osm.appdesign21;

import android.app.Activity;
import android.content.Context;

/**
 * Created by mh on 2016-09-20.
 */
public class SharedPreferences {

    static Context mContext;

    public SharedPreferences(Context c){
        mContext = c;
    }

    /**
     * 값 넣기
     * @param paramKey (값이 없을경우 default: ""공백 리턴)
     * @param value 저장할 값
     * @param prefName 파일 이름
     */
    public void putValue(String paramKey, String value, String prefName){
        android.content.SharedPreferences pref = mContext.getSharedPreferences(prefName,
                Activity.MODE_PRIVATE);
        android.content.SharedPreferences.Editor editor = pref.edit();
        editor.putString(paramKey, value);
        editor.commit();
    }

    /**
     *
     * @param paramKey 키값
     * @param value 가져올 값
     * @param prefName 파일 이름
     * @return
     */
    public String getValue(String paramKey, String value, String prefName){
        android.content.SharedPreferences pref = mContext.getSharedPreferences(prefName,
                Activity.MODE_PRIVATE);
        try{
            return pref.getString(paramKey, value);
        } catch (Exception e){
            return value;
        }
    }
    public void removeAllPreferences(String prefName){
        android.content.SharedPreferences pref = mContext.getSharedPreferences(prefName,
                Activity.MODE_PRIVATE);
        android.content.SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.commit();
    }
}
