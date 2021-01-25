package com.jwlks.healthble.search;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import com.jwlks.healthble.R;

public class SearchRoutineAdapter extends BaseAdapter {

    private ArrayList<SearchRoutineModel> searchRoutineModelList = new ArrayList<SearchRoutineModel>();

    public SearchRoutineAdapter() {

    }

    @Override
    public int getCount() {
        return searchRoutineModelList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return searchRoutineModelList.get(position);
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        final int pos = position;
        final Context context = viewGroup.getContext();

        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.listitem_routine, viewGroup, false);
        }

        // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
        ImageView iconImageView = (ImageView) view.findViewById(R.id.routine_image) ;
        TextView titleTextView = (TextView) view.findViewById(R.id.routine_title) ;
        TextView counterTextView = (TextView) view.findViewById(R.id.routine_counter) ;
        TextView timerTextView = (TextView) view.findViewById(R.id.routine_timer) ;

        // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
        SearchRoutineModel searchRoutineModel = searchRoutineModelList.get(position);

        // 아이템 내 각 위젯에 데이터 반영
        iconImageView.setImageDrawable(searchRoutineModel.getIcon());
        titleTextView.setText(searchRoutineModel.getTitle());
        counterTextView.setText(searchRoutineModel.getCounter());
        timerTextView.setText(searchRoutineModel.getTimer());

        return view;
    }

    public void addItem(Drawable icon, String title, String counter, String timer) {
        SearchRoutineModel item = new SearchRoutineModel();

        item.setIcon(icon);
        item.setTitle(title);
        item.setCounter(counter);
        item.setTimer(timer);

        searchRoutineModelList.add(item);
    }
}
