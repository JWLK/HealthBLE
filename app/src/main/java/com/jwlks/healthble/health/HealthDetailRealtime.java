package com.jwlks.healthble.health;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import com.jwlks.healthble.R;

import com.jwlks.healthble.model.MBluetoothData;
import com.jwlks.healthble.util.Dlog;

import static com.jwlks.healthble.main.NavigationActivity.mBluetoothLeService;
import static com.jwlks.healthble.main.NavigationActivity.mWriteCharacteristic;

public class HealthDetailRealtime extends AppCompatActivity {

    ImageButton backButton;
    Button EmgTriggerButton;

    /*Graph Setting*/
    int getEMGdataINT;

    LineChart lineChart;
    int DATA_RANGE = 50;
    LineData lineData;
    LineDataSet setValueTransfer;
    ArrayList<Entry> entryData;
    GraphThread thread = null;
    boolean isRunning = false;

    Boolean signalTrigger = false;
    byte[] startData = {(byte)0xF1, 0x04, (byte)0x0F2};
    byte[] stopData = {(byte)0xF1, 0x05, (byte)0x0F2};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.health_detail_realtime);

        backButton = findViewById(R.id.button_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        /*Get Data*/
//        EventBus.getDefault().register(this);
        /*GraphSetting*/
        lineChart = (LineChart) findViewById(R.id.chart_emg);
        chartClear();

        EmgTriggerButton = findViewById(R.id.button_emg_trigger);
        EmgTriggerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRunning = !isRunning;
                if(isRunning){
                    EmgTriggerButton.setText("ON");
                    EmgTriggerButton.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.colorActiveBlue));

                    Log.i("JWLK","Data START");
                    mBluetoothLeService.writeCharacteristic(mWriteCharacteristic, startData);

                    if(thread == null){
                        chartInit();
                        thread = new GraphThread();
                        thread.setDaemon(true);
                        thread.start();
                    }

                } else {
                    EmgTriggerButton.setText("OFF");
                    EmgTriggerButton.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.colorGray80));

                    Log.i("JWLK","Data STOP");
                    mBluetoothLeService.writeCharacteristic(mWriteCharacteristic, stopData);

                }
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
        if(thread != null){
            isRunning = false;
            thread.interrupt();
            thread = null;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        if(thread != null){
            isRunning = false;
            thread.interrupt();
            thread = null;
        }

    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void BluetoothEvent(MBluetoothData event){
        getEMGdataINT = event.getBluetoothEMG().getGraphDataInt();
        signalTrigger = true;
        Dlog.d("GET EVENT DATA: " + getEMGdataINT);
    }


    private void chartInit() {

        lineChart.setAutoScaleMinMaxEnabled(true);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getXAxis().setAxisMaximum(60f);
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.getAxisLeft().setAxisMaximum(1024f);
        lineChart.getAxisLeft().setAxisMinimum(0f);

        entryData = new ArrayList<Entry>();
        setValueTransfer = new LineDataSet(entryData, "EMG DATA");
        setValueTransfer.setDrawValues(false);
        setValueTransfer.setDrawCircles(false);
        setValueTransfer.setAxisDependency(YAxis.AxisDependency.LEFT);

        lineData = new LineData();
        lineData.addDataSet(setValueTransfer);
        lineChart.setData(lineData);
        lineChart.invalidate();
    }

    private void chartClear() {
        lineChart.setData(null);
        lineChart.invalidate();
        lineChart.setNoDataText("실시간 EMG 데이터 확인을 위해 스위치를 ON 하세요!");
        lineChart.setNoDataTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
    }

    public void chartUpdate(int data) {
        if(entryData.size() > DATA_RANGE){
            entryData.remove(0);
            for(int i = 0; i < DATA_RANGE; i++){
                entryData.get(i).setX(i);
            }
        }
        entryData.add(new Entry(entryData.size(), data));
        setValueTransfer.notifyDataSetChanged();
        lineChart.notifyDataSetChanged();
        lineChart.invalidate();
    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 0) {
//                int data = 0;
//                data = (int)(Math.random()*1024);
//                chartUpdate(data);
                if(signalTrigger){
                    signalTrigger = false;
                    chartUpdate(getEMGdataINT);
                    getEMGdataINT = 0;
                } else {
                    chartUpdate(getEMGdataINT);
                }
                Log.d("Handler", getEMGdataINT+"");
            }
        }
    };

    class GraphThread extends Thread {
        @Override
        public void run() {
            int i = 0;
            while(true){
                while (isRunning){
                    handler.sendEmptyMessage(i);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e){
                        e.printStackTrace();
                    }

                }
            }
        }
    }


}
