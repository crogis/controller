package com.controller;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import com.controller.actions.ChangeFormationActivity;
import com.controller.actions.GatherDataActivity;
import com.controller.actions.MoveActivity;
import com.controller.adapter.PairedDevicesAdapter;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends Activity {
    //Todo add close for input/output stream

    final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    SharedPreferences prefs;

    BluetoothDevice pairedDevice;

    private PairedDevicesAdapter pairedDevicesAdapter;

    // For sending raw binary data
    OutputStream outputStream;
    // For strings - remove later
    DataOutputStream dataOutputStream;

    InputStream inputStream;

    private Button sendStuffBtn;
    private Button chooseLeaderBtn, moveBtn, changeFormationBtn, gatherDataBtn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        setUpChooseLeaderButton();
        setUpActionButtons();
    }

    @Override
    public void onResume() {
        super.onResume();

        if(isBtEnabled()) {
            chooseLeaderBtn.setText("Choose Leader");
        } else {
            chooseLeaderBtn.setText("Turn On Bluetooth");
        }

        chooseLeaderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isBtEnabled()) {
                    showChooseLeaderDialog();
                } else {
                    showBluetoothSettings();
                }
            }
        });
    }

    private void setUpChooseLeaderButton() {
        chooseLeaderBtn = (Button) findViewById(R.id.choose_leader_button);
    }


    private void setUpActionButtons() {
        // Setting up the buttons
        moveBtn = (Button) findViewById(R.id.move_button);
        changeFormationBtn = (Button) findViewById(R.id.change_formation_button);
        gatherDataBtn = (Button) findViewById(R.id.gather_data_button);

        /*
            Adding listeners to the button so that it will know what action to take when the button is clicked
            The method startActivity inside the onClick method opens up the new page when the certain action button is clicked
         */
        moveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(MoveActivity.class);
            }
        });
        changeFormationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(ChangeFormationActivity.class);
            }
        });
        gatherDataBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(GatherDataActivity.class);
            }
        });
    }

    public void startActivity(Class<?> cls) {
        Intent moveIntent = new Intent(this, cls);
        startActivity(moveIntent);
    }

    private void showChooseLeaderDialog() {
        BluetoothDevice[] pairedDevices = getPairedDevices();
        pairedDevicesAdapter = new PairedDevicesAdapter(this, R.layout.item_paired_device, pairedDevices);
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("Choose Leader");
        dialogBuilder.setAdapter(pairedDevicesAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                connectToParentDevice(i);
            }
        });
        dialogBuilder.setNegativeButton("Cancel", null);
        AlertDialog dialog = dialogBuilder.create();
        dialog.show();
    }

    private void connectToParentDevice(int position) {
//        SharedPreferences.Editor e = prefs.edit();
//        e.putString("leaderDevice", device).commit();
        BluetoothDevice chosenDevice = pairedDevicesAdapter.getItem(position);

        System.out.println("CHOSEN DEVICE " + chosenDevice.getName());

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

    private void connect() throws IOException {
        BluetoothSocket socket = pairedDevice.createRfcommSocketToServiceRecord(uuid);
        socket.connect();

        System.out.println("is connected??? " + socket.isConnected());

        outputStream = socket.getOutputStream();
        inputStream = socket.getInputStream();
        dataOutputStream = new DataOutputStream(outputStream);
//        sendStuffBtn.setEnabled(true);
    }


    /*
        Bluetooth related functions
     */

    public BluetoothAdapter btAdapter() {
        return BluetoothAdapter.getDefaultAdapter();
    }

    public boolean isBtEnabled() {
        return btAdapter().isEnabled();
    }

    public BluetoothDevice[] getPairedDevices() {
        BluetoothAdapter btAdapter = btAdapter();
        Set<BluetoothDevice> devices = btAdapter.getBondedDevices();
        return devices.toArray(new BluetoothDevice[devices.size()]);

//        for(BluetoothDevice device : pairedDevices)
//        {
//            System.out.println("BT Device " + device.getName());
//            if(device.getName().contains("Regine")) {
//                pairedDevice = device;
//                System.out.println("Device? " + pairedDevice.getName());
//                try {
//                    connect();
//                } catch (IOException e) {
//                    System.out.println("Error creating Socket " + e.getMessage());
//                }
//                break;
//            }
//        }
    }

    public void showBluetoothSettings() {
        Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBluetooth, 1);
    }
}
