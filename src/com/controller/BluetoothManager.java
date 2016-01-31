package com.controller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/*
    This class handles all the bluetooth connections (sending and receiving)
    1. BluetoothSocket
    2. InputStream and OutputStream
 */
public class BluetoothManager {

    private static BluetoothManager bluetoothManager = new BluetoothManager();

    private static UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    // For sending raw binary data
    private static OutputStream outputStream;
    private static InputStream inputStream;

    private static BluetoothSocket bluetoothSocket;

    // For strings - remove later
    private static DataOutputStream dataOutputStream;
    private static DataInputStream dataInputStream;

    private String TAG = "BluetoothManager";

    /*
     * A private Constructor prevents any other
     * class from instantiating.
     */

    private BluetoothManager(){ }

    /**
     * @return single instance of BluetoothManager
     */
    public static BluetoothManager getInstance( ) {
        return bluetoothManager;
    }

    private static BluetoothAdapter btAdapter() {
        return BluetoothAdapter.getDefaultAdapter();
    }

    public boolean isBtEnabled() {
        return btAdapter().isEnabled();
    }

    public BluetoothDevice[] getPairedDevices() {
        Set<BluetoothDevice> devices = getBondedDevices();
        return devices.toArray(new BluetoothDevice[devices.size()]);
    }

    public BluetoothDevice[] getPairedDevicesWithoutDevice(BluetoothDevice remove) {
        Set<BluetoothDevice> devices = new HashSet<BluetoothDevice>(getBondedDevices());
        devices.remove(remove);
        return devices.toArray(new BluetoothDevice[devices.size()]);
    }

    private Set<BluetoothDevice> getBondedDevices() {
       return btAdapter().getBondedDevices();
    }

    public boolean initializeBluetoothSocket(BluetoothDevice btDevice) {
        try {
            bluetoothSocket = btDevice.createRfcommSocketToServiceRecord(uuid);
            bluetoothSocket.connect();

            //Initializes the input and output streams
            initializeStreams(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());

            //Sends command device has connected to paired device
            sendCommand(1);
            return true;
        } catch (Exception e) {
            Log.w(TAG, "Unable to connect to device", e);
            closeBluetoothConnection();
            return false;
        }

    }

    private void initializeStreams(InputStream is, OutputStream os) {
        inputStream = is;
        outputStream = os;
    }

    public boolean sendCommand(int command) {
        try {
            outputStream.write(command);
            outputStream.flush();
            Log.i(TAG, "Sending command " + command);
            return true;
        } catch (Exception e) {
            Log.w(TAG, "Error sending command", e);
            closeBluetoothConnection();
            return false;
        }
    }

    public boolean isConnected() {
        return bluetoothSocket != null && bluetoothSocket.isConnected();
    }

    public void closeBluetoothConnection() {
        try {
            if(inputStream != null) {
                inputStream.close();
            }
            if(outputStream != null) {
                outputStream.close();
            }
            if(bluetoothSocket != null) {
                bluetoothSocket.close();
            }
        } catch (Exception e) {
            Log.w(TAG, "Error in closing streams/socket", e);
        }

        inputStream = null;
        outputStream = null;
        bluetoothSocket = null;
    }
}
