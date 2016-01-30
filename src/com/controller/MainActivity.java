package com.controller;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.controller.actions.ChangeFormationActivity;
import com.controller.actions.GatherDataActivity;
import com.controller.actions.MoveActivity;
import com.controller.adapter.PairedDevicesAdapter;
import com.controller.preference.AppPreference;

import java.io.*;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends Activity {
    //Todo add close for input/output stream
    //Todo add closing of stuff in onDestroy

    final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    private Activity context = this;

    private PairedDevicesAdapter pairedDevicesAdapter;
    private BluetoothSocket btSocket;
    private BluetoothDevice pairedDevice;

    private StreamManager streamManager;

    private Button sendStuffBtn;
    private Button chooseLeaderBtn, moveBtn, changeFormationBtn, gatherDataBtn;
    private TextView leaderDeviceTextView;
    private ProgressDialog connectingProgressDialog;

    AppPreference appPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getActionBar();
        if(actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);
            LayoutInflater layoutInflater = LayoutInflater.from(this);
            View view = layoutInflater.inflate(R.layout.action_bar_custom, null);
            actionBar.setCustomView(view);
            actionBar.setDisplayShowCustomEnabled(true);
        }

        appPreference = new AppPreference(this);
        streamManager = StreamManager.getInstance();

        setUpChooseLeaderButton();
        setUpActionButtons();
        setUpLeaderTextView();
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

        String leaderName = appPreference.findStringPref(appPreference.LEADER_DEVICE_NAME);
        leaderDeviceTextView.setText(leaderName);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("ON DESTROY MAIN ACTIVITY");
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

    private void setUpLeaderTextView() {
        leaderDeviceTextView = (TextView) findViewById(R.id.leader_text_view);
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
                pairedDevice = pairedDevicesAdapter.getItem(i);
                showConnectingProgressDialog();
                connectToParentDevice();
            }
        });
        dialogBuilder.setNegativeButton("Cancel", null);
        AlertDialog dialog = dialogBuilder.create();
        dialog.show();
    }

    private void connectToParentDevice() {
        final String deviceName = pairedDevice.getName();

        Thread thread = new Thread(new Runnable(){
            @Override
            public void run(){
                try {
                    btSocket = pairedDevice.createRfcommSocketToServiceRecord(uuid);
                    btSocket.connect();

                    //Initializes the input and output streams
                    streamManager.initializeStreams(btSocket.getInputStream(), btSocket.getOutputStream());
                    streamManager.sendCommand(1);

                    setDevicePrefs();
                    connectingProgressDialog.dismiss();
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            leaderDeviceTextView.setText(deviceName);
                            Toast.makeText(context, "Successfully connected to " + deviceName, Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (IOException e) {
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            connectingProgressDialog.dismiss();
                            Toast.makeText(context, "Unable to connect to " + deviceName, Toast.LENGTH_SHORT).show();
                        }
                    });

                    System.out.println("Unable to connect to device " + e.getMessage());
                }
            }
        });
        thread.start();
    }

    private void setDevicePrefs() {
        appPreference.setStringPref(appPreference.LEADER_DEVICE_NAME, pairedDevice.getName());
        appPreference.setStringPref(appPreference.LEADER_DEVICE_ADDRESS, pairedDevice.getAddress());
    }

    private void showConnectingProgressDialog() {
        connectingProgressDialog = new ProgressDialog(context);
        connectingProgressDialog.setMessage("Connecting to " + pairedDevice.getName());
        connectingProgressDialog.setIndeterminate(true);
        connectingProgressDialog.setCancelable(false);
        connectingProgressDialog.setCanceledOnTouchOutside(false);
        connectingProgressDialog.show();
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
    }

    public void showBluetoothSettings() {
        Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBluetooth, 1);
    }
}
