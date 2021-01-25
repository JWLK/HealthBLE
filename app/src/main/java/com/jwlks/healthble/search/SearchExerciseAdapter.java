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

public class SearchExerciseAdapter extends BaseAdapter {

    private ArrayList<SearchExerciseModel> searchExerciseModelList = new ArrayList<SearchExerciseModel>();

    public SearchExerciseAdapter() {

    }

    @Override
    public int getCount() {
        return searchExerciseModelList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return searchExerciseModelList.get(position);
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        final int pos = position;
        final Context context = viewGroup.getContext();

        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate( R.layout.listitem_exercise, viewGroup, false);
        }

        // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
        ImageView iconImageView = (ImageView) view.findViewById(R.id.exercise_image) ;
        TextView titleTextView = (TextView) view.findViewById(R.id.exercise_title) ;

        // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
        SearchExerciseModel searchExerciseModel = searchExerciseModelList.get(position);

        // 아이템 내 각 위젯에 데이터 반영
        iconImageView.setImageDrawable(searchExerciseModel.getIcon());
        titleTextView.setText(searchExerciseModel.getTitle());
//        descTextView.setText(searchExerciseModel.getDesc());

        return view;
    }

    public void addItem(Drawable icon, String title, String desc) {
        SearchExerciseModel item = new SearchExerciseModel();

        item.setIcon(icon);
        item.setTitle(title);
        item.setDesc(desc);

        searchExerciseModelList.add(item);
    }
}
