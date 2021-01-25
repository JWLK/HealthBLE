package com.jwlks.healthble.search;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayout;

import java.util.Objects;

import com.jwlks.healthble.R;

public class SearchMainFragment extends Fragment {
    ViewGroup viewGroup;
    TabLayout tabLayout;

    /*Chest*/
    SearchExerciseAdapter chestAdapter;
    ListView listViewChest;

    /*Leg*/
    SearchExerciseAdapter legAdapter;
    ListView listViewLeg;

    /*Back*/
    SearchExerciseAdapter backAdapter;
    ListView listViewBack;

    /*ABS*/
    SearchExerciseAdapter absAdapter;
    ListView listViewABS;

    /*Triceps*/
    SearchExerciseAdapter tricepsAdapter;
    ListView listViewTriceps;

    /*Biceps*/
    SearchExerciseAdapter bicepsAdapter;
    ListView listViewBiceps;

    /*Shoulder*/
    SearchExerciseAdapter shoulderAdapter;
    ListView listViewShoulder;

    /*Selected Routine*/
    SearchRoutineAdapter selectedAdapter;
    ListView listViewSelectedRoutine;

    /*Routine List*/
    SearchRoutineAdapter routineListAdapter;
    ListView listViewRoutineList;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewGroup = (ViewGroup) inflater.inflate(R.layout.search_main_sub, container,false);

        tabLayout = (TabLayout) viewGroup.findViewById(R.id.tab_layout);
        setTabControl();

        /*Chest*/
        chestAdapter = setChestAdapter(getContext());
        listViewChest = setListViewExercise("chest", chestAdapter, Objects.requireNonNull(getActivity()));
        listViewHeightSet(chestAdapter, listViewChest);

        /*Leg*/
        legAdapter = setLegAdapter(getContext());
        listViewLeg = setListViewExercise("leg", legAdapter, Objects.requireNonNull(getActivity()));
        listViewHeightSet(legAdapter, listViewLeg);

        /*Back*/
        backAdapter = setBackAdapter(getContext());
        listViewBack = setListViewExercise("back", backAdapter, Objects.requireNonNull(getActivity()));
        listViewHeightSet(backAdapter, listViewBack);

        /*ABS*/
        absAdapter = setABSAdapter(getContext());
        listViewABS = setListViewExercise("abs", absAdapter, Objects.requireNonNull(getActivity()));
        listViewHeightSet(absAdapter, listViewABS);

        /*Triceps*/
        tricepsAdapter = setTricepsAdapter(getContext());
        listViewTriceps = setListViewExercise("triceps", tricepsAdapter, Objects.requireNonNull(getActivity()));
        listViewHeightSet(tricepsAdapter, listViewTriceps);

        /*Biceps*/
        bicepsAdapter = setBicepsAdapter(getContext());
        listViewBiceps = setListViewExercise("biceps", bicepsAdapter, Objects.requireNonNull(getActivity()));
        listViewHeightSet(bicepsAdapter, listViewBiceps);

        /*Shoulder*/
        shoulderAdapter = setShoulderAdapter(getContext());
        listViewShoulder = setListViewExercise("shoulder", shoulderAdapter, Objects.requireNonNull(getActivity()));
        listViewHeightSet(shoulderAdapter, listViewShoulder);

        /*Selected Routine*/
        selectedAdapter = setSelectedAdapter(getContext());
        listViewSelectedRoutine = setListViewRoutine("selected_routine", selectedAdapter, Objects.requireNonNull(getActivity()));
        listViewHeightSet(selectedAdapter, listViewSelectedRoutine);

        /*Selected Routine*/
        routineListAdapter = setRoutineListAdapter(getContext());
        listViewRoutineList = setListViewRoutine("routine_list", routineListAdapter, Objects.requireNonNull(getActivity()));
        listViewHeightSet(routineListAdapter, listViewRoutineList);

        return viewGroup;
    }

    void setTabControl() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                changeView(position);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void changeView(int index) {
        FrameLayout Basket = (FrameLayout) viewGroup.findViewById(R.id.floating_basket);
        LinearLayout tabLayout0 = (LinearLayout) viewGroup.findViewById(R.id.tab_item_0);
        LinearLayout tabLayout1 = (LinearLayout) viewGroup.findViewById(R.id.tab_item_1);

        switch (index) {
            case 0 :
                tabLayout0.setVisibility(View.VISIBLE) ;
                Basket.setVisibility(View.VISIBLE);
                tabLayout1.setVisibility(View.GONE) ;
                break ;
            case 1 :
                tabLayout0.setVisibility(View.GONE) ;
                Basket.setVisibility(View.GONE);
                tabLayout1.setVisibility(View.VISIBLE) ;
                break ;
        }
    }


    private static void listViewHeightSet(BaseAdapter listAdapter, ListView listView){
        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++){
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    /*Tab0*/

    ListView setListViewExercise(String part, SearchExerciseAdapter searchExerciseAdapter, Activity activity) {
        ListView listView;
        String layoutId = "list_view_"+part;
        int resID = getResources().getIdentifier(layoutId, "id", activity.getPackageName());
        listView = (ListView) viewGroup.findViewById(resID);
        listView.setVerticalScrollBarEnabled(false);
        listView.setAdapter(searchExerciseAdapter);
        return listView;
    }

    SearchExerciseAdapter setChestAdapter(Context context) {
        SearchExerciseAdapter searchExerciseAdapter = new SearchExerciseAdapter();
        searchExerciseAdapter.addItem(ContextCompat.getDrawable(context,
                R.drawable.ic_baseline_fitness_center_48_white),
                "덤벨 프레스",
                "@String/exercise_dsc");
        searchExerciseAdapter.addItem(ContextCompat.getDrawable(context,
                R.drawable.ic_baseline_fitness_center_48_white),
                "덤벨 플라이",
                "@String/exercise_dsc");
        searchExerciseAdapter.addItem(ContextCompat.getDrawable(context,
                R.drawable.ic_baseline_fitness_center_48_white),
                "벤치프레스",
                "@String/exercise_dsc");
        searchExerciseAdapter.addItem(ContextCompat.getDrawable(context,
                R.drawable.ic_baseline_fitness_center_48_white),
                "인클라인 덤벨 프레스",
                "@String/exercise_dsc");
        searchExerciseAdapter.addItem(ContextCompat.getDrawable(context,
                R.drawable.ic_baseline_fitness_center_48_white),
                "케이블 크로스오버",
                "@String/exercise_dsc");

        return searchExerciseAdapter;

    }

    SearchExerciseAdapter setLegAdapter(Context context) {
        SearchExerciseAdapter searchExerciseAdapter = new SearchExerciseAdapter();
        searchExerciseAdapter.addItem(ContextCompat.getDrawable(context,
                R.drawable.ic_baseline_fitness_center_48_white),
                "런지",
                "@String/exercise_dsc");
        searchExerciseAdapter.addItem(ContextCompat.getDrawable(context,
                R.drawable.ic_baseline_fitness_center_48_white),
                "레그 익스텐션",
                "@String/exercise_dsc");
        searchExerciseAdapter.addItem(ContextCompat.getDrawable(context,
                R.drawable.ic_baseline_fitness_center_48_white),
                "레그 걸",
                "@String/exercise_dsc");
        searchExerciseAdapter.addItem(ContextCompat.getDrawable(context,
                R.drawable.ic_baseline_fitness_center_48_white),
                "레그 프레스",
                "@String/exercise_dsc");
        searchExerciseAdapter.addItem(ContextCompat.getDrawable(context,
                R.drawable.ic_baseline_fitness_center_48_white),
                "스쿼트",
                "@String/exercise_dsc");
        searchExerciseAdapter.addItem(ContextCompat.getDrawable(context,
                R.drawable.ic_baseline_fitness_center_48_white),
                "스트레이트 레그 데드리프트",
                "@String/exercise_dsc");
        searchExerciseAdapter.addItem(ContextCompat.getDrawable(context,
                R.drawable.ic_baseline_fitness_center_48_white),
                "시티드 카프 레이즈",
                "@String/exercise_dsc");
        searchExerciseAdapter.addItem(ContextCompat.getDrawable(context,
                R.drawable.ic_baseline_fitness_center_48_white),
                "프론트 스쿼트",
                "@String/exercise_dsc");

        return searchExerciseAdapter;

    }

    SearchExerciseAdapter setBackAdapter(Context context) {
        SearchExerciseAdapter searchExerciseAdapter = new SearchExerciseAdapter();
        searchExerciseAdapter.addItem(ContextCompat.getDrawable(context,
                R.drawable.ic_baseline_fitness_center_48_white),
                "덤벨 로우",
                "@String/exercise_dsc");
        searchExerciseAdapter.addItem(ContextCompat.getDrawable(context,
                R.drawable.ic_baseline_fitness_center_48_white),
                "데드리프트",
                "@String/exercise_dsc");
        searchExerciseAdapter.addItem(ContextCompat.getDrawable(context,
                R.drawable.ic_baseline_fitness_center_48_white),
                "바벨 로우",
                "@String/exercise_dsc");
        searchExerciseAdapter.addItem(ContextCompat.getDrawable(context,
                R.drawable.ic_baseline_fitness_center_48_white),
                "어시스티드 친업",
                "@String/exercise_dsc");
        searchExerciseAdapter.addItem(ContextCompat.getDrawable(context,
                R.drawable.ic_baseline_fitness_center_48_white),
                "어시스티드 풀업",
                "@String/exercise_dsc");
        searchExerciseAdapter.addItem(ContextCompat.getDrawable(context,
                R.drawable.ic_baseline_fitness_center_48_white),
                "친업",
                "@String/exercise_dsc");
        searchExerciseAdapter.addItem(ContextCompat.getDrawable(context,
                R.drawable.ic_baseline_fitness_center_48_white),
                "케이블 로우",
                "@String/exercise_dsc");
        searchExerciseAdapter.addItem(ContextCompat.getDrawable(context,
                R.drawable.ic_baseline_fitness_center_48_white),
                "풀다운",
                "@String/exercise_dsc");
        searchExerciseAdapter.addItem(ContextCompat.getDrawable(context,
                R.drawable.ic_baseline_fitness_center_48_white),
                "풀업",
                "@String/exercise_dsc");
        searchExerciseAdapter.addItem(ContextCompat.getDrawable(context,
                R.drawable.ic_baseline_fitness_center_48_white),
                "하이퍼익스텐션",
                "@String/exercise_dsc");
        return searchExerciseAdapter;

    }

    SearchExerciseAdapter setABSAdapter(Context context) {
        SearchExerciseAdapter searchExerciseAdapter = new SearchExerciseAdapter();
        searchExerciseAdapter.addItem(ContextCompat.getDrawable(context,
                R.drawable.ic_baseline_fitness_center_48_white),
                "덤벨 로우",
                "@String/exercise_dsc");
        searchExerciseAdapter.addItem(ContextCompat.getDrawable(context,
                R.drawable.ic_baseline_fitness_center_48_white),
                "데드리프트",
                "@String/exercise_dsc");

        return searchExerciseAdapter;

    }

    SearchExerciseAdapter setTricepsAdapter(Context context) {
        SearchExerciseAdapter searchExerciseAdapter = new SearchExerciseAdapter();
        searchExerciseAdapter.addItem(ContextCompat.getDrawable(context,
                R.drawable.ic_baseline_fitness_center_48_white),
                "딥스",
                "@String/exercise_dsc");
        searchExerciseAdapter.addItem(ContextCompat.getDrawable(context,
                R.drawable.ic_baseline_fitness_center_48_white),
                "어시스티드 딥스",
                "@String/exercise_dsc");
        searchExerciseAdapter.addItem(ContextCompat.getDrawable(context,
                R.drawable.ic_baseline_fitness_center_48_white),
                "클로즈 그립 벤치 프레스",
                "@String/exercise_dsc");
        searchExerciseAdapter.addItem(ContextCompat.getDrawable(context,
                R.drawable.ic_baseline_fitness_center_48_white),
                "트라이셉스 익스텐션",
                "@String/exercise_dsc");
        searchExerciseAdapter.addItem(ContextCompat.getDrawable(context,
                R.drawable.ic_baseline_fitness_center_48_white),
                "푸시다운",
                "@String/exercise_dsc");


        return searchExerciseAdapter;

    }

    SearchExerciseAdapter setBicepsAdapter(Context context) {
        SearchExerciseAdapter searchExerciseAdapter = new SearchExerciseAdapter();
        searchExerciseAdapter.addItem(ContextCompat.getDrawable(context,
                R.drawable.ic_baseline_fitness_center_48_white),
                "덤벨 바이셉 컬",
                "@String/exercise_dsc");
        searchExerciseAdapter.addItem(ContextCompat.getDrawable(context,
                R.drawable.ic_baseline_fitness_center_48_white),
                "바벨 바이셉 컬",
                "@String/exercise_dsc");
        searchExerciseAdapter.addItem(ContextCompat.getDrawable(context,
                R.drawable.ic_baseline_fitness_center_48_white),
                "컨센트레이션 컬",
                "@String/exercise_dsc");
        searchExerciseAdapter.addItem(ContextCompat.getDrawable(context,
                R.drawable.ic_baseline_fitness_center_48_white),
                "해머컬",
                "@String/exercise_dsc");

        return searchExerciseAdapter;

    }

    SearchExerciseAdapter setShoulderAdapter(Context context) {
        SearchExerciseAdapter searchExerciseAdapter = new SearchExerciseAdapter();
        searchExerciseAdapter.addItem(ContextCompat.getDrawable(context,
                R.drawable.ic_baseline_fitness_center_48_white),
                "덤벨 레터럴 레이즈",
                "@String/exercise_dsc");
        searchExerciseAdapter.addItem(ContextCompat.getDrawable(context,
                R.drawable.ic_baseline_fitness_center_48_white),
                "밀리터리 프레스",
                "@String/exercise_dsc");
        searchExerciseAdapter.addItem(ContextCompat.getDrawable(context,
                R.drawable.ic_baseline_fitness_center_48_white),
                "숄더 덤벨 프레스",
                "@String/exercise_dsc");
        searchExerciseAdapter.addItem(ContextCompat.getDrawable(context,
                R.drawable.ic_baseline_fitness_center_48_white),
                "업라이트 로우",
                "@String/exercise_dsc");

        return searchExerciseAdapter;

    }


    /*Tab1*/
    ListView setListViewRoutine(String part, SearchRoutineAdapter searchRoutineAdapter, Activity activity) {
        ListView listView;
        String layoutId = "list_view_"+part;
        int resID = getResources().getIdentifier(layoutId, "id", activity.getPackageName());
        listView = (ListView) viewGroup.findViewById(resID);
        listView.setVerticalScrollBarEnabled(false);
        listView.setAdapter(searchRoutineAdapter);
        return listView;
    }

    SearchRoutineAdapter setSelectedAdapter(Context context) {
        SearchRoutineAdapter searchRoutineAdapter = new SearchRoutineAdapter();
        searchRoutineAdapter.addItem(ContextCompat.getDrawable(context,
                R.drawable.ic_baseline_fitness_center_48_white),
                "Only 덤벨프레스",
                "1",
                "00:10:35");


        return searchRoutineAdapter;

    }

    SearchRoutineAdapter setRoutineListAdapter(Context context) {
        SearchRoutineAdapter searchRoutineAdapter = new SearchRoutineAdapter();
        searchRoutineAdapter.addItem(ContextCompat.getDrawable(context,
                R.drawable.ic_baseline_fitness_center_48_white),
                "운동 워밍업 루틴",
                "8",
                "01:10:23");
        searchRoutineAdapter.addItem(ContextCompat.getDrawable(context,
                R.drawable.ic_baseline_fitness_center_48_white),
                "Only 덤벨프레스",
                "1",
                "00:10:35");


        return searchRoutineAdapter;

    }



}
