package com.example.osm.appdesign21.Recorder;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.osm.appdesign21.AdapterItem;
import com.example.osm.appdesign21.R;

import java.util.ArrayList;

public class TimeRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ArrayList<AdapterItem> itemList;

    private OnItemClickListener listener;

    public static class TimeViewHolder extends RecyclerView.ViewHolder {
        public TextView timeItemView;

        public TimeViewHolder(View v) {
            super(v);
            timeItemView = (TextView) v.findViewById(R.id.timeItemView);
        }
    }

    public static class DataViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView timeView, nameView;

        private OnViewHolderClickListener listener;

        public DataViewHolder(View v, OnViewHolderClickListener listener) {
            super(v);
            timeView = (TextView) v.findViewById(R.id.timeView);
            nameView = (TextView) v.findViewById(R.id.nameView);
            v.setOnClickListener(this);
            this.listener = listener;
        }

        @Override
        public void onClick(View v) {
            if(listener != null)
                listener.onViewHolderClick(getPosition());
        }

        private interface OnViewHolderClickListener {
            void onViewHolderClick(int position);
        }
    }

    public TimeRecyclerAdapter(ArrayList<MyData> dataset) {
        itemList = initItemList(orderByTimeDesc(dataset));
    }

    private ArrayList<AdapterItem> initItemList(ArrayList<MyData> dataset) {
        ArrayList<AdapterItem> result = new ArrayList<>();

        int year = 0, month = 0, dayOfMonth = 0;
        for(MyData data:dataset) {
            if(year != data.getYear() || month != data.getMonth()
                    || dayOfMonth != data.getDayOfMonth() )
            {
                result.add(new TimeItem(data.getYear(), data.getMonth(), data.getDayOfMonth(),data.getHour(),data.getMinute(),data.getSecond()));
                year = data.getYear();
                month = data.getMonth();
                dayOfMonth = data.getDayOfMonth();
            }
            result.add(data);
        }
        return result;
    }

    private ArrayList<MyData> orderByTimeDesc(ArrayList<MyData> dataset) {
        ArrayList<MyData> result = dataset;
        for(int i=0; i<result.size()-1; i++) {
            for(int j=0; j<result.size()-i-1; j++) {
                if(result.get(j).getTime() < result.get(j+1).getTime()) {
                    MyData temp2 = result.remove(j+1);
                    MyData temp1 = result.remove(j);
                    result.add(j, temp2);
                    result.add(j+1, temp1);
                }
            }
        }
        return result;
    }

    @Override
    public int getItemViewType(int position) {
        return itemList.get(position).getType();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == AdapterItem.TYPE_TIME)
            return new TimeViewHolder(
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.recycler_item_time, parent, false));
        else
            return new DataViewHolder(
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.recycler_item_data, parent, false),
                    new DataViewHolder.OnViewHolderClickListener() {
                        @Override
                        public void onViewHolderClick(int position) {
                            if(listener != null)
                                listener.onItemClick(position);
                        }
                    }
            );
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof TimeViewHolder) {
            TimeViewHolder tHolder = (TimeViewHolder) holder;
            tHolder.timeItemView.setText(itemList.get(position).getShortTimeToString());
        } else {
            DataViewHolder dHolder = (DataViewHolder) holder;
            dHolder.timeView.setText(itemList.get(position).getLongTimeToString());
            dHolder.nameView.setText(
                    ((MyData)itemList.get(position))
                            .getName());
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public MyData getItem(int position) {
        return (MyData)itemList.get(position);
    }

    public interface OnItemClickListener {
        public void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}