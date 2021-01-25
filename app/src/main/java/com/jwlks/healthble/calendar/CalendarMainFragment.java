package com.jwlks.healthble.calendar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.jwlks.healthble.R;

public class CalendarMainFragment extends Fragment {
    ViewGroup viewGroup;
    CalendarView calendarView;
    TextView calendarDate;
    Button addRecord;
    TextView offRecord;
    FrameLayout onRecord;
    Boolean recordTrigger = false;

    @Nullable
    @Override public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        viewGroup = (ViewGroup) inflater.inflate(R.layout.calendar_main_sub, container,false);

        calendarDate = viewGroup.findViewById(R.id.calendar_date);
        calendarView = viewGroup.findViewById(R.id.calendar_view);

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth)
            {
                String date = year + "." + (month + 1) + "." + dayOfMonth;
                calendarDate.setText(date); // 선택한 날짜로 설정

            }
        });

        offRecord = viewGroup.findViewById(R.id.off_record);
        onRecord = viewGroup.findViewById(R.id.on_record);
        addRecord = viewGroup.findViewById(R.id.button_add_record);
        addRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!recordTrigger){
                    recordTrigger = true;
                    onRecord.setVisibility(View.VISIBLE);
                    offRecord.setVisibility(View.GONE);
                } else {
                    recordTrigger = false;
                    onRecord.setVisibility(View.GONE);
                    offRecord.setVisibility(View.VISIBLE);
                }
            }
        });
        return viewGroup;
    }

}
