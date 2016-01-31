package com.controller;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
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


public class MainActivity extends Activity {
    //Todo add close for input/output stream
    //Todo add closing of stuff in onDestroy

    private Activity context = this;

    private PairedDevicesAdapter pairedDevicesAdapter;

    private BluetoothDevice pairedDevice;

    private BluetoothManager bluetoothManager;

    private Button chooseLeaderBtn, moveBtn, changeFormationBtn, gatherDataBtn, reconnectBtn;
    private TextView leaderDeviceTextView;
    private ProgressDialog connectingProgressDialog;

    AppPreference appPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appPreference = new AppPreference(this);

        setUpChooseLeaderButton();
        setUpActionButtons();
        setUpLeaderTextView();
    }

    @Override
    public void onResume() {
        super.onResume();

        bluetoothManager = BluetoothManager.getInstance();

        if(!bluetoothManager.isBtEnabled()) {
            showBluetoothSettings();
        }

        /*
            Resetting pairedDevice to null because every time the app opens, it should not access the old value of the
            paired device when the activity was paused (meaning you press the home button on your device,
            so the application is paused, not exited)
         */
        pairedDevice = null;

        String leaderName = appPreference.findStringPref(appPreference.LEADER_DEVICE_NAME);
        String leaderAddress = appPreference.findStringPref(appPreference.LEADER_DEVICE_ADDRESS);

        BluetoothDevice[] pairedDevices = bluetoothManager.getPairedDevices();
        for (BluetoothDevice btDevice: pairedDevices) {
            if( btDevice.getName().equals(leaderName) && btDevice.getAddress().equals(leaderAddress)) {
                System.out.println("Paired device " + btDevice);
                pairedDevice = btDevice;
                break;
            }
        }

        /*
            pairedDevice will be null in 2 instances
            1. App is newly opened
            2. User unpairs the chosen leader device (that's why there is a checker if the leaderName is originally empty or not)
         */
        if(pairedDevice == null) {
            leaderDeviceTextView.setText("-");
            /*
                This will happen if the user unpairs the chosen leader device.
                But this should not happen unless you manually unpair your device from the Bluetooth settings.
             */
            if(!leaderName.isEmpty()) {
                Toast.makeText(this, "Unable to find chosen leader device. Please choose another one.", Toast.LENGTH_LONG).show();
            }
        } else {
            leaderDeviceTextView.setText(leaderName);
        }


        ActionBar actionBar = getActionBar();
        if(actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);

            LayoutInflater layoutInflater = LayoutInflater.from(this);
            View view = layoutInflater.inflate(R.layout.action_bar_custom, null);
            reconnectBtn = (Button) view.findViewById(R.id.reconnect_button);
            reconnectBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    connectToParentDevice();
                }
            });

            actionBar.setCustomView(view);
            actionBar.setDisplayShowCustomEnabled(!leaderName.isEmpty() &&
                                                  !leaderAddress.isEmpty() &&
                                                  pairedDevice != null &&
                                                  !bluetoothManager.isConnected());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        bluetoothManager.closeBluetoothConnection();
        System.out.println("ON DESTROY MAIN ACTIVITY");
    }

    private void setUpChooseLeaderButton() {
        chooseLeaderBtn = (Button) findViewById(R.id.choose_leader_button);
        chooseLeaderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChooseLeaderDialog();
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

    private void setUpLeaderTextView() {
        leaderDeviceTextView = (TextView) findViewById(R.id.leader_text_view);
    }

    public void startActivity(Class<?> cls) {
        Intent moveIntent = new Intent(this, cls);
        startActivity(moveIntent);
    }

    private void showChooseLeaderDialog() {
        BluetoothDevice[] pairedDevices = bluetoothManager.getPairedDevicesWithoutDevice(pairedDevice);
        if(pairedDevices.length == 0 && pairedDevice == null) {
            Toast.makeText(this, "No paired devices", Toast.LENGTH_SHORT).show();
        } else if (pairedDevices.length == 0 && pairedDevice != null) {
            Toast.makeText(this, "No other paired devices", Toast.LENGTH_SHORT).show();
        } else if (pairedDevices.length > 0) {
            pairedDevicesAdapter = new PairedDevicesAdapter(this, R.layout.item_paired_device, pairedDevices);
            final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            dialogBuilder.setTitle("Choose Leader");

            dialogBuilder.setAdapter(pairedDevicesAdapter, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    pairedDevice = pairedDevicesAdapter.getItem(i);
                    connectToParentDevice();
                }
            });
            dialogBuilder.setNegativeButton("Cancel", null);
            AlertDialog dialog = dialogBuilder.create();
            dialog.show();
        }
    }

    private void connectToParentDevice() {
        showConnectingProgressDialog();
        Thread thread = new Thread(new Runnable(){
            @Override
            public void run(){
                boolean success = bluetoothManager.initializeBluetoothSocket(pairedDevice);

                if(success) {
                    setDevicePrefs();
                    //calls made to the main thread
                    onSuccessfulConnection();
                } else {
                    //calls made to the main thread
                    onFailedConnection();
                }
                connectingProgressDialog.dismiss();
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

    // Calls that need to be made on the main thread
    private void onSuccessfulConnection() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String deviceName = pairedDevice.getName();
                leaderDeviceTextView.setText(deviceName);
                showReconnectButton();
                Toast.makeText(context, "Successfully connected to " + deviceName, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onFailedConnection() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showReconnectButton();
                Toast.makeText(context, "Unable to connect to " + pairedDevice.getName(), Toast.LENGTH_SHORT).show();
                pairedDevice = null;
            }
        });
    }

    private void showReconnectButton() {
        String leaderName = appPreference.findStringPref(appPreference.LEADER_DEVICE_NAME);
        String leaderAddress = appPreference.findStringPref(appPreference.LEADER_DEVICE_ADDRESS);
        ActionBar actionBar = getActionBar();
        if(actionBar != null) {
            actionBar.setDisplayShowCustomEnabled(!leaderName.isEmpty() &&
                                                  !leaderAddress.isEmpty() &&
                                                  pairedDevice != null &&
                                                  !bluetoothManager.isConnected());
        }
    }

    public void showBluetoothSettings() {
        Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBluetooth, 1);
    }
}
