package com.jwlks.healthble.model;

public class MBluetoothInfo {

    public final String bluetoothName;
    public final String bluetoothAddress;

    public MBluetoothInfo(String bluetoothName, String bluetoothAddress) {
        this.bluetoothName = bluetoothName;
        this.bluetoothAddress = bluetoothAddress;
    }

    public String getBluetoothName() {
        return bluetoothName;
    }

    public String getBluetoothAddress() {
        return bluetoothAddress;
    }

}
