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

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends Activity {
    //Todo add close for input/output stream

    private final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    private SharedPreferences prefs;

    private BluetoothDevice pairedDevice;

    // For sending raw binary data
    private OutputStream outputStream;
    // For strings - remove later
    private DataOutputStream dataOutputStream;

    private InputStream inputStream;

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

    private void setUpChooseLeaderButton() {
        chooseLeaderBtn = (Button) findViewById(R.id.choose_leader_button);
        chooseLeaderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAlertDialog();
            }
        });
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

    private void startActivity(Class<?> cls) {
        Intent moveIntent = new Intent(this, cls);
        startActivity(moveIntent);
    }

    private void showAlertDialog() {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("Choose Leader");
        String[] devices = {"Device 1", "Device 2", "Device 3"};
        final ArrayAdapter<String> devicesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, devices);
        dialogBuilder.setAdapter(devicesAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String deviceChosen = devicesAdapter.getItem(i);
                setParentDevice(deviceChosen);
            }
        });

        dialogBuilder.setNegativeButton("Cancel", null);
        AlertDialog dialog = dialogBuilder.create();
        dialog.show();
    }

    private void setParentDevice(String device) {
        SharedPreferences.Editor e = prefs.edit();
        e.putString("leaderDevice", device).commit();
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

    /*

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
     */

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
