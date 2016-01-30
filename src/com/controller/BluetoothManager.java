package com.controller;

import java.io.*;

/*
    This class handles all the bluetooth connections (sending and receiving)
    1. BluetoothSocket
    2. InputStream and OutputStream
 */
public class BluetoothManager {

    private static BluetoothManager bluetoothManager = new BluetoothManager();

    // For sending raw binary data
    private static OutputStream outputStream;
    private static InputStream inputStream;

    // For strings - remove later
    private static DataOutputStream dataOutputStream;
    private static DataInputStream dataInputStream;

    /*
     * A private Constructor prevents any other
     * class from instantiating.
     */

    private BluetoothManager(){ }

    public static BluetoothManager getInstance( ) {
        return bluetoothManager;
    }

    public void initializeStreams(InputStream is, OutputStream os) {
        inputStream = is;
        outputStream = os;
        dataInputStream = new DataInputStream(inputStream);
        dataOutputStream = new DataOutputStream(outputStream);
    }

    public void resetStreams() {
        inputStream = null;
        outputStream = null;
        dataInputStream = null;
        dataOutputStream = null;
    }

    public boolean sendCommand(int command) {
        try {
            outputStream.write(command);
            outputStream.flush();
//        dataOutputStream.writeBytes(command + "\n");
//        dataOutputStream.flush();
            System.out.println("Sending command " + command);
            return true;
        } catch (Exception e) {
            System.out.println("Error sending command " + e);
            resetStreams();
            return false;
        }
    }
}
