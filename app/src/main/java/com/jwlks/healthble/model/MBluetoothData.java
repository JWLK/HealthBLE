package com.jwlks.healthble.model;

public class MBluetoothData {

    public MBluetoothInfo BluetoothInfo;
    public MBluetoothBattery BluetoothBattery;
    public MBluetoothTimer BluetoothTimer;
    public MBluetoothEMG BluetoothEMG;

    public MBluetoothData(MBluetoothInfo mBluetoothInfo, MBluetoothBattery mBluetoothBattery, MBluetoothTimer mBluetoothTimer, MBluetoothEMG mBluetoothEMG) {
        this.BluetoothInfo = mBluetoothInfo;
        this.BluetoothBattery = mBluetoothBattery;
        this.BluetoothTimer = mBluetoothTimer;
        this.BluetoothEMG = mBluetoothEMG;
    }

    /* SET */
    public void setBluetoothInfo(MBluetoothInfo bluetoothInfo) {
        BluetoothInfo = bluetoothInfo;
    }

    public void setBluetoothBattery(MBluetoothBattery bluetoothBattery) {
        BluetoothBattery = bluetoothBattery;
    }

    public void setBluetoothTimer(MBluetoothTimer bluetoothTimer) {
        BluetoothTimer = bluetoothTimer;
    }

    public void setBluetoothEMG(MBluetoothEMG bluetoothEMG) {
        BluetoothEMG = bluetoothEMG;
    }

    /* GET */
    public MBluetoothInfo getBluetoothInfo() {
        return BluetoothInfo;
    }

    public MBluetoothBattery getBluetoothBattery() {
        return BluetoothBattery;
    }

    public MBluetoothTimer getBluetoothTimer() {
        return BluetoothTimer;
    }

    public MBluetoothEMG getBluetoothEMG() {
        return BluetoothEMG;
    }

}
