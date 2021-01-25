/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jwlks.healthble.device;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.jwlks.healthble.R;
import com.jwlks.healthble.util.Dlog;

import static com.jwlks.healthble.util.ConvertDataType.hexStringToByte8bit2HexArray;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class DeviceControlActivity extends AppCompatActivity {
    private final static String TAG = DeviceControlActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    /*Interface*/
    private TextView mConnectionState;
    private TextView mBatteryState;
    private TextView mDataField;
    private EditText mDataWrite;
    private Button mDataSend;
    private Button mDataStart;
    private Button mDataClear;
    private Button mTimer5;
    private Button mTimer10;
    private String mDeviceName;
    private String mDeviceAddress;
    private String stateString;
    private int getEMGdataINT;
    private String getEMGdataString;
    private ExpandableListView mGattServicesList;
    boolean mDataClearTrigger = false;

    /*Graph Setting*/
    LineChart lineChart;
    int DATA_RANGE = 30;
    LineData lineData;
    LineDataSet setValueTransfer;
    ArrayList<Entry> entryData;
    GraphThread thread = null;
    boolean isRunning = false;

    /*Bluetooth*/
    private BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;
    private static BluetoothGattCharacteristic mNotifyCharacteristic;
    private static BluetoothGattCharacteristic mWriteCharacteristic;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    @SuppressLint("CutPasteId")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_control);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        ((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);
        mGattServicesList = (ExpandableListView) findViewById(R.id.gatt_services_list);
        mGattServicesList.setOnChildClickListener(servicesListClickListner);
        mConnectionState = (TextView) findViewById(R.id.connection_state);
        mBatteryState = (TextView) findViewById(R.id.battery_state);
        mDataField = (TextView) findViewById(R.id.data_value);

        /*GraphSetting*/
        lineChart = (LineChart) findViewById(R.id.chart_emg);
        chartClear();

        mDataWrite = findViewById(R.id.data_write);
        mDataSend = findViewById(R.id.data_send);
        mDataSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String getData = mDataWrite.getText().toString().trim();
                String wrapString = "F1"+getData+"F2";
                byte[] convertData = hexStringToByte8bit2HexArray(wrapString);
                for(byte bytes : hexStringToByte8bit2HexArray(wrapString)){
                    Log.i("JWLK",String.format("0x%02X", bytes));
                }
                mBluetoothLeService.writeCharacteristic(mWriteCharacteristic, convertData);
            }
        });

        mDataStart = findViewById(R.id.emg_start);
        mDataStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRunning = !isRunning;
                if(isRunning){
                    byte[] startData = {(byte)0xF1, 0x04, (byte)0x0F2};
                    Log.i("JWLK","Data START");
                    mBluetoothLeService.writeCharacteristic(mWriteCharacteristic, startData);

                    mDataStart.setText("STOP");
                    mDataStart.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.colorMainRed));
                    mDataClear.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.colorGray80));
                    mDataClear.setEnabled(false);
                    if(thread == null){
                        chartInit();
                        thread = new GraphThread();
                        thread.setDaemon(true);
                        thread.start();
                    }
                    mDataClearTrigger = true;

                } else {
                    byte[] stopData = {(byte)0xF1, 0x05, (byte)0x0F2};
                    mBluetoothLeService.writeCharacteristic(mWriteCharacteristic, stopData);
                    mDataStart.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.colorPrimaryDark));
                    mDataClear.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.colorPrimaryDark));
                    Log.i("JWLK","Data STOP");
                    mDataStart.setText("START");
                    mDataClear.setEnabled(true);
                }
            }
        });
        mDataClear = findViewById(R.id.emg_clear);
        mDataClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mDataClearTrigger){
                    byte[] startData = {(byte)0xF1, 0x05, (byte)0x0F2};
                    Log.i("JWLK","Data CLEAR");
                    chartClear();
                    thread.interrupt();
                    thread = null;
                    mDataClearTrigger = false;
                    mDataClear.setEnabled(false);
                }
            }
        });
        mTimer5 = findViewById(R.id.timer_5);
        mTimer5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] startData = {(byte)0xF1, 0x06,  0x00, 0x05, (byte)0x0F2};
                Log.i("JWLK","Data Timer5");
                mBluetoothLeService.writeCharacteristic(mWriteCharacteristic, startData);
            }
        });

        mTimer10 = findViewById(R.id.timer_10);
        mTimer10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] startData = {(byte)0xF1, 0x06,  0x00, 0x10, (byte)0x0F2};
                Log.i("JWLK","Data Timer10");
                mBluetoothLeService.writeCharacteristic(mWriteCharacteristic, startData);
            }
        });

        getSupportActionBar().setTitle(mDeviceName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        // Sets up UI references.

    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();

            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                clearUI();

            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                /* [displayGattServices] = Show all the supported services and characteristics on the user interface.
                * */
                //displayGattServices(mBluetoothLeService.getSupportedGattServices());

                /* [targetMeasurementServiceAutoConnect] = Only Target Service & Characteristic Auto Connected
                * If you want Change Service, Edit SampleGattAttributes
                * */
                targetMeasurementServiceAutoConnect(mBluetoothLeService.getTargetMeasurementService());

            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                getBatteryData(intent.getStringExtra(BluetoothLeService.BATTERY_DATA));
                getTimerData(intent.getStringExtra(BluetoothLeService.TIMER_DATA));
                getEMGData(intent.getStringExtra(BluetoothLeService.EMG_DATA));
            }
        }
    };

    private void targetMeasurementServiceAutoConnect(BluetoothGattCharacteristic gattCharacteristic) {
        if (gattCharacteristic == null) {
            Dlog.e("Get gattCharacteristics Null");
            return;
        }
        String uuidCharacteristic = gattCharacteristic.getUuid().toString();
        String getCharacteristicName = SampleGattAttributes.lookup(uuidCharacteristic, "unknownCharaString");
        Dlog.d("Get gattCharacteristics : ["+ uuidCharacteristic +"] "+ getCharacteristicName);

        final int charaProp = gattCharacteristic.getProperties();
        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
            // If there is an active notification on a characteristic, clear
            // it first so it doesn't update the data field on the user interface.
            if (mNotifyCharacteristic != null) {
                mBluetoothLeService.setCharacteristicNotification(
                        mNotifyCharacteristic, false);
                mNotifyCharacteristic = null;
            }
            mBluetoothLeService.readCharacteristic(gattCharacteristic);
            Toast.makeText(getBaseContext(), "PROPERTY_READ Activate", Toast.LENGTH_SHORT).show();
        }
        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
            mNotifyCharacteristic = gattCharacteristic;
            mBluetoothLeService.setCharacteristicNotification(
                    gattCharacteristic, true);

            Toast.makeText(getBaseContext(), "PROPERTY_NOTIFY Activate", Toast.LENGTH_SHORT).show();
        }
        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
            mWriteCharacteristic = gattCharacteristic;
            Toast.makeText(getBaseContext(), "PROPERTY_WRITE Activate", Toast.LENGTH_SHORT).show();
        }
    }


    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(
                        LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }

        SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
                this,
                gattServiceData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 },
                gattCharacteristicData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 }
        );
        mGattServicesList.setAdapter(gattServiceAdapter);
    }

    // If a given GATT characteristic is selected, check for supported features.  This sample
    // demonstrates 'Read' and 'Notify' features.  See
    // http://d.android.com/reference/android/bluetooth/BluetoothGatt.html for the complete
    // list of supported characteristic features.
    private final ExpandableListView.OnChildClickListener servicesListClickListner =
            new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                            int childPosition, long id) {
                    if (mGattCharacteristics != null) {
                        final BluetoothGattCharacteristic characteristic =
                                mGattCharacteristics.get(groupPosition).get(childPosition);
                        final int charaProp = characteristic.getProperties();
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                            // If there is an active notification on a characteristic, clear
                            // it first so it doesn't update the data field on the user interface.
                            if (mNotifyCharacteristic != null) {
                                mBluetoothLeService.setCharacteristicNotification(
                                        mNotifyCharacteristic, false);
                                mNotifyCharacteristic = null;
                            }
                            mBluetoothLeService.readCharacteristic(characteristic);
                            Toast.makeText(getBaseContext(), "PROPERTY_READ Activate", Toast.LENGTH_SHORT).show();
                        }
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                            mNotifyCharacteristic = characteristic;
                            mBluetoothLeService.setCharacteristicNotification(
                                    characteristic, true);

                            Toast.makeText(getBaseContext(), "PROPERTY_NOTIFY Activate", Toast.LENGTH_SHORT).show();
                        }
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
                            mWriteCharacteristic = characteristic;
                            Toast.makeText(getBaseContext(), "PROPERTY_WRITE Activate", Toast.LENGTH_SHORT).show();
                        }
                        return true;
                    }
                    return false;
                }
            };



    private void clearUI() {
        mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
        mDataField.setText(R.string.no_data);
        mBatteryState.setText(R.string.no_data);
    }


    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText(resourceId);
            }
        });
    }

    private void displayData(String data) {
        if (data != null) {
            Dlog.d("displayData: "+ data);
            mDataField.setText(data);
        }
    }

    private void getBatteryData(String data) {
        if (data != null) {
//            Dlog.d("getBatteryData: "+ data);
            switch (data) {
                case "03":
                    stateString = "80% ~ 100%";
                    break;
                case "02":
                    stateString = "40% ~ 80%";
                    break;
                case "01":
                    stateString = "10% ~ 40%";
                    break;
                case "00":
                    stateString = "0% ~ 10%";
                    break;
                default:
                    stateString = "Error";
                    break;
            }
        }
        final String finalState = stateString;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mBatteryState.setText(finalState);
            }
        });
    }

    private void getTimerData(String data) {
        if (data != null) {
            Dlog.d("getTimerData: "+ data);
        }
    }

    private void getEMGData(String data) {
        if (data != null) {
            getEMGdataINT = Integer.parseInt(data,10);
            Dlog.d("getEMGData: "+ getEMGdataINT);
        }
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
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
        lineChart.setNoDataText("Click the Start button to view EMG DATA");
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
                chartUpdate(getEMGdataINT);
//               Log.d("Handler", data+"");
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
