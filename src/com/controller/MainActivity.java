package com.controller;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends Activity {
    //Todo add close for input/output stream

    private final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    private BluetoothDevice pairedDevice;

    // For sending raw binary data
    private OutputStream outputStream;
    // For strings - remove later
    private DataOutputStream dataOutputStream;

    private InputStream inputStream;

    private Button sendStuffBtn;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        setUpPairDeviceButton();
    }

    private void setUpControlButtons() {
//        sendStuffBtn = (Button) findViewById(R.id.sendStuff);
//        sendStuffBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                writeCommand("Hello World!");
//            }
//        });
//        sendStuffBtn.setEnabled(false);

    }

    private void writeCommand(String message) {
        try {
//            outputStream.write(message.getBytes());
//            outputStream.flush();
            dataOutputStream.write(message.getBytes());
            dataOutputStream.flush();
        } catch (IOException e) {
            System.out.println("Unable to write command");
        }
    }

    private void setUpPairDeviceButton() {
        Button pairDeviceBtn = (Button) findViewById(R.id.choose_leader_button);
        pairDeviceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("Clicking!");
                if(!btAdapter().isEnabled()) {
                    showBluetoothSettings();
                }

                Set<BluetoothDevice> pairedDevices = btAdapter().getBondedDevices();
                for(BluetoothDevice device : pairedDevices)
                {
                    System.out.println("BT Device " + device.getName());
                    if(device.getName().contains("Regine")) {
                        pairedDevice = device;
                        System.out.println("Device? " + pairedDevice.getName());
                        try {
                            connect();
                        } catch (IOException e) {
                            System.out.println("Error creating Socket " + e.getMessage());
                        }
                        break;
                    }
                }

            }
        });
    }

    private void connect() throws IOException {
        BluetoothSocket socket = pairedDevice.createRfcommSocketToServiceRecord(uuid);
        socket.connect();

        System.out.println("is connected??? " + socket.isConnected());

        outputStream = socket.getOutputStream();
        inputStream = socket.getInputStream();
        dataOutputStream = new DataOutputStream(outputStream);
//        sendStuffBtn.setEnabled(true);
    }

    private void showBluetoothSettings() {
        Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBluetooth, 1);
    }

    private BluetoothAdapter btAdapter() {
        return BluetoothAdapter.getDefaultAdapter();
    }
}
