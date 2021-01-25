package com.jwlks.healthble.health;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Objects;

import com.jwlks.healthble.R;

import com.jwlks.healthble.model.MBluetoothData;
import com.jwlks.healthble.model.MBluetoothInfo;
import com.jwlks.healthble.play.PlayExerciseInit;
import com.jwlks.healthble.util.Dlog;

public class HealthMainFragment extends Fragment {
    ViewGroup viewGroup;

    private TextView viewDeviceName;
    private TextView viewDeviceAddress;
    private TextView viewDeviceConnectionState;
    private TextView viewDeviceBatteryText;
    private ImageView viewDeviceBatteryImage;

    private EventBus eventBus;
    private MBluetoothInfo mBluetoothInfo;
    private boolean dataTrigger;
    private String mDeviceName;
    private String mDeviceAddress;
    private String mDeviceConnectionState;
    private String mBatteryState;
    private int mBatteryStateTextVisible = View.VISIBLE;

    @SuppressLint("CutPasteId")
    @Nullable
    @Override public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        viewGroup = (ViewGroup) inflater.inflate(R.layout.health_main_sub, container,false);
        viewDeviceName = viewGroup.findViewById(R.id.device_name);
        viewDeviceAddress = viewGroup.findViewById(R.id.device_address);
        viewDeviceConnectionState = viewGroup.findViewById(R.id.device_connection_state);
        viewDeviceBatteryText = viewGroup.findViewById(R.id.device_battery_text);
        viewDeviceBatteryImage = viewGroup.findViewById(R.id.device_battery_image);
        OnClickEvent();

        return viewGroup;
    }
    @Override
    public void onResume() {
        super.onResume();
        /*Get Data*/
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void BluetoothEvent(MBluetoothData event){
        viewDeviceName.setText(event.getBluetoothInfo().getBluetoothName());
        viewDeviceAddress.setText(event.getBluetoothInfo().getBluetoothAddress());
        viewDeviceConnectionState.setText("Connected");
        mBatteryState = event.getBluetoothBattery().getBluetoothBattery();
        Dlog.e("mBatteryState: "+mBatteryState);
        switch (mBatteryState) {
            case "03":
                viewDeviceBatteryText.setVisibility(mBatteryStateTextVisible);
                viewDeviceBatteryText.setText("충분");
                viewDeviceBatteryText.setTextColor(ContextCompat.getColor(Objects.requireNonNull(getContext()), R.color.colorIndicatorGreen));
                viewDeviceBatteryImage.setImageResource(R.drawable.ic_battery_three_quarters);
                break;
            case "02":
                viewDeviceBatteryText.setVisibility(mBatteryStateTextVisible);
                viewDeviceBatteryText.setText("양호");
                viewDeviceBatteryText.setTextColor(ContextCompat.getColor(Objects.requireNonNull(getContext()), R.color.colorIndicatorYellow));
                viewDeviceBatteryImage.setImageResource(R.drawable.ic_battery_half);
                break;
            case "01":
                viewDeviceBatteryText.setVisibility(mBatteryStateTextVisible);
                viewDeviceBatteryText.setText("낮음");
                viewDeviceBatteryText.setTextColor(ContextCompat.getColor(Objects.requireNonNull(getContext()), R.color.colorIndicatorRed));
                viewDeviceBatteryImage.setImageResource(R.drawable.ic_battery_quarter);
                break;
            case "00":
                viewDeviceBatteryText.setVisibility(View.VISIBLE);
                viewDeviceBatteryText.setText("충전필요");
                viewDeviceBatteryText.setTextColor(ContextCompat.getColor(Objects.requireNonNull(getContext()), R.color.colorIndicatorRed));
                viewDeviceBatteryImage.setImageResource(R.drawable.ic_battery_empty);
                break;
            default:
                viewDeviceBatteryText.setText("Error");
                viewDeviceBatteryImage.setImageResource(R.drawable.ic_battery_empty);
                break;
        }

    }


    private void OnClickEvent() {
        Button realTimeViewButton = viewGroup.findViewById(R.id.button_realtime_view);
        realTimeViewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), HealthDetailRealtime.class);
                startActivity(intent);
            }
        });

        Button startExerciseButton = viewGroup.findViewById(R.id.start_exercise);
        startExerciseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PlayExerciseInit.class);
                startActivity(intent);
            }
        });

    }


}
