package com.example.osm.appdesign21;

import android.graphics.Bitmap;

/**
 * Created by mh on 2016-09-19.
 */
public class PhoneBook {

    private Bitmap mAvartar;
    private String mName;
    private String mPhone;
    private String mEmail;
    public PhoneBook(Bitmap mAvartar, String mName, String mPhone, String mEmail){
        this.mAvartar = mAvartar;
        this.mName = mName;
        this.mPhone = mPhone;
        this.mEmail = mEmail;
    }
    public PhoneBook(String mName, String mPhone){
        this.mName = mName;
        this.mPhone = mPhone;
    }
    public Bitmap getmAvartar(){
        return mAvartar;
    }
    public String getmName(){
        return mName;
    }
    public String getmPhone(){
        return mPhone;
    }
    public String getmEmail(){
        return mEmail;
    }
    public void setmAvartar(Bitmap mAvartar){
        this.mAvartar = mAvartar;
    }
    public void setmName(String mName){
        this.mName = mName;
    }
    public void setmPhone(String mPhone){
        this.mPhone = mPhone;
    }
    public void setmEmail(String mEmail){
        this.mEmail = mEmail;
    }
}
