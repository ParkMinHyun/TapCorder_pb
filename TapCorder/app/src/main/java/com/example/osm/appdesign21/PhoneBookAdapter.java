package com.example.osm.appdesign21;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by mh on 2016-09-19.
 */
public class PhoneBookAdapter extends BaseAdapter {

    private Context mContext;
    private List<PhoneBook> mListPhoneBook;

    public PhoneBookAdapter(Context context, List<PhoneBook> list){
        mContext = context;
        mListPhoneBook = list;
    }
    @Override
    public int getCount() {
        return mListPhoneBook.size();
    }

    @Override
    public Object getItem(int arg0) {
        return mListPhoneBook.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        PhoneBook entry = mListPhoneBook.get(i);

        if(view == null){
            LayoutInflater inflater = LayoutInflater.from(mContext);
            view = inflater.inflate(R.layout.phonebook_row, null);
        }

        ImageView ivAvartar = (ImageView)view.findViewById(R.id.imgAvatar);
        ivAvartar.setImageBitmap(entry.getmAvartar());

        TextView tvName = (TextView)view.findViewById(R.id.tvName);
        tvName.setText(entry.getmName());

        TextView tvPhone = (TextView)view.findViewById(R.id.tvPhone);
        tvPhone.setText(entry.getmPhone());

//        TextView tvEmail = (TextView)view.findViewById(R.id.tvEmail);
//        tvEmail.setText(entry.getmEmail());

        return view;
    }
}
