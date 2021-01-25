package com.jwlks.healthble.main;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.greenrobot.eventbus.EventBus;

import com.jwlks.healthble.R;
import com.jwlks.healthble.calendar.CalendarMainFragment;
import com.jwlks.healthble.device.BluetoothLeService;
import com.jwlks.healthble.device.SampleGattAttributes;
import com.jwlks.healthble.health.HealthMainFragment;
import com.jwlks.healthble.model.MBluetoothBattery;
import com.jwlks.healthble.model.MBluetoothData;
import com.jwlks.healthble.model.MBluetoothEMG;
import com.jwlks.healthble.model.MBluetoothInfo;
import com.jwlks.healthble.model.MBluetoothTimer;
import com.jwlks.healthble.search.SearchMainFragment;
import com.jwlks.healthble.util.Dlog;

public class NavigationActivity extends AppCompatActivity {

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";


    /*Bluetooth*/
    public MBluetoothData BluetoothData;
    public MBluetoothInfo mBluetoothInfo;
    public MBluetoothBattery mBluetoothBattery;
    public MBluetoothTimer mBluetoothTimer;
    public MBluetoothEMG mBluetoothEMG;

    public static BluetoothLeService mBluetoothLeService;
//    private boolean mConnected = false;
    public static BluetoothGattCharacteristic mNotifyCharacteristic;
    public static BluetoothGattCharacteristic mWriteCharacteristic;

    BottomNavigationView bottomNavigationView;
    HealthMainFragment healthMainFragment;
    SearchMainFragment searchMainFragment;
    CalendarMainFragment calendarMainFragment;

    String bluetoothData;
    boolean bluetoothTrigger = false;
    String bluetoothName;
    String bluetoothAddress;
    String bluetoothBattery;
    boolean timerTrigger;
    boolean emgTrigger;
    int emgDataInt;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_navigation);
        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        createFragment();
        setFragmentControl();

        final Intent intent = getIntent();
        bluetoothName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        bluetoothAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        mBluetoothInfo = new MBluetoothInfo(bluetoothName, bluetoothAddress);
        mBluetoothBattery = new MBluetoothBattery("-1");
        mBluetoothTimer = new MBluetoothTimer(false);
        mBluetoothEMG = new MBluetoothEMG(0);

        /*Bluetooth Setting*/
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(bluetoothAddress);
            Dlog.d("Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //unregisterReceiver(mGattUpdateReceiver);
        Dlog.d("Connect request Pause");
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
        EventBus.getDefault().unregister(this);
    }


    /* Bluetooth Connection */
    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Dlog.e("Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(bluetoothAddress);
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
    public final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                bluetoothTrigger = true;
//                updateConnectionState(R.string.connected);
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                bluetoothTrigger = false;
//                updateConnectionState(R.string.disconnected);
                clearUI();

            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                targetMeasurementServiceAutoConnect(mBluetoothLeService.getTargetMeasurementService());

            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                bluetoothData = displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                if(intent.getStringExtra(BluetoothLeService.BATTERY_DATA) != null) {
                    bluetoothBattery = getBatteryData(intent.getStringExtra(BluetoothLeService.BATTERY_DATA));
                    mBluetoothBattery = new MBluetoothBattery(bluetoothBattery);
                }
                if(intent.getStringExtra(BluetoothLeService.TIMER_DATA) != null) {
                    timerTrigger = getTimerData(intent.getStringExtra(BluetoothLeService.TIMER_DATA));
                    mBluetoothTimer = new MBluetoothTimer(timerTrigger);
                }
                if(intent.getStringExtra(BluetoothLeService.EMG_DATA) != null) {
                    emgDataInt = getEMGData(intent.getStringExtra(BluetoothLeService.EMG_DATA));
                    mBluetoothEMG = new MBluetoothEMG(emgDataInt);
                }
            }

            if(bluetoothTrigger || bluetoothData != null){
                BluetoothData = new MBluetoothData(mBluetoothInfo, mBluetoothBattery, mBluetoothTimer, mBluetoothEMG);
                EventBus.getDefault().post(BluetoothData);
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
//        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
//            // If there is an active notification on a characteristic, clear
//            // it first so it doesn't update the data field on the user interface.
//            if (mNotifyCharacteristic != null) {
//                mBluetoothLeService.setCharacteristicNotification(
//                        mNotifyCharacteristic, false);
//                mNotifyCharacteristic = null;
//            }
//            mBluetoothLeService.readCharacteristic(gattCharacteristic);
////            Toast.makeText(getBaseContext(), "PROPERTY_READ Activate", Toast.LENGTH_SHORT).show();
//        }
        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
            mNotifyCharacteristic = gattCharacteristic;
            mBluetoothLeService.setCharacteristicNotification(
                    gattCharacteristic, true);

//            Toast.makeText(getBaseContext(), "PROPERTY_NOTIFY Activate", Toast.LENGTH_SHORT).show();
        }
        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
            mWriteCharacteristic = gattCharacteristic;
//            Toast.makeText(getBaseContext(), "PROPERTY_WRITE Activate", Toast.LENGTH_SHORT).show();
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

    /*Bluetooth Get Data*/
    private void clearUI() {
        Dlog.e("Clear UI");
        EventBus.getDefault().post(new MBluetoothData(
                new MBluetoothInfo("NaN", "NaN"),
                new MBluetoothBattery("-1"),
                new MBluetoothTimer(false),
                new MBluetoothEMG(0)));
    }

    private String displayData(String data) {
        String finalData = null;
        if (data != null) {
            Dlog.d("displayData: "+ data);
            finalData = data;
//            mDataField.setText(data);
        }
        return finalData;
    }


    private String getBatteryData(String data) {
        String finalState = null;
        if (data != null) {
//            Dlog.d("getBatteryData: "+ data);
            finalState = data;
        }
        return finalState;
    }

    private boolean getTimerData(String data) {
        boolean finalTimer = false;
        if (data != null) {
            if(data == "02"){
                finalTimer = true;
                Dlog.d("getTimerData: "+ data);
            }
        }
        return finalTimer;
    }

    private int getEMGData(String data) {
        int finalEMG = 0;
        if (data != null) {
            try {
                finalEMG = Integer.parseInt(data,10);
            } catch (Exception e){
                Dlog.e(e.toString());
            }
            Dlog.d("getEMGData: "+ finalEMG);
        }
        return finalEMG;
    }


    /* Interface Setting */
    void createFragment() {
        healthMainFragment = new HealthMainFragment();
        searchMainFragment = new SearchMainFragment();
        calendarMainFragment = new CalendarMainFragment();
//        profileMain = new ProfileMain();

    }

    void setFragmentControl() {
        /*init Fragment*/
        getSupportFragmentManager().beginTransaction().replace(R.id.origin_frame_layout, healthMainFragment).commitAllowingStateLoss();

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.health_item :
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.origin_frame_layout, healthMainFragment).commitAllowingStateLoss();
                        return true;
                    case R.id.search_item :
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.origin_frame_layout, searchMainFragment).commitAllowingStateLoss();
                        return true;
                    case R.id.calendar_item :
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.origin_frame_layout, calendarMainFragment).commitAllowingStateLoss();
                        return true;
//                    case R.id.profile_item :
//                        getSupportFragmentManager().beginTransaction()
//                                .replace(R.id.origin_frame_layout, profileMain).commitAllowingStateLoss();
//                        return true;

                    default: return false;
                }
            }
        });
    }

}
