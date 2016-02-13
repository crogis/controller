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
import android.util.Log;
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

/*
    Once you open the application, this is the Activity that is started. 
    You can think of an Activity as a screen where buttons, labels, and text fields are rendered.
    So each screen in your application, such as the Move screen, Change Formation screen, and Gather Data screen are called Activities that's why there is a corresponding file for each in the action folder.
*/

public class MainActivity extends Activity {
    private Activity context = this;

    private PairedDevicesAdapter pairedDevicesAdapter;

    private BluetoothDevice pairedDevice;

    private BluetoothManager bluetoothManager;

    private Button chooseLeaderBtn, moveBtn, changeFormationBtn, gatherDataBtn, reconnectBtn;
    private TextView leaderDeviceTextView;
    private ProgressDialog connectingProgressDialog;

    private String TAG = "MainActivity";

    AppPreference appPreference;

    /*
        The onCreate method is called once and is the FIRST one called when the application is opened. 
        So this handles creating what you see in the screen such as the buttons and labels.
    */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Sets the view/layout for the screen
        setContentView(R.layout.activity_main);

        // This class handles saving to the internal memory of your Android device which robot you have chosen as the leader
        appPreference = new AppPreference(this);

        // The following just sets up the buttons and their corresponding listeners (what the buttons will do if clicked)
        setUpChooseLeaderButton();
        setUpActionButtons();
        setUpLeaderTextView();
    }

    /*
        The onResume method is called after the onStart method (which is called after the onCreate method), which is not implemented in this code because it is not needed.
        So the process of an Activity once it is started is onCreate -> onStart -> onResume
        onResume is called when ...
        1. You first open the application 
        2. You press the back button from a previous page. For example you're in the Move Activity (screen), and you press the back button, this method will be triggered. Take note that onCreate is only called once since this activity has already been rendered. This method can be called several times.
        3. You pause the activity. For example, you press the home button in your device and you don't exit the application completely.
    */
    @Override
    public void onResume() {
        super.onResume();

        // Gets the Bluetooth Manager of Android. This can check if the Bluetooth is enabled for the device and can get the list of paired devices.
        bluetoothManager = BluetoothManager.getInstance();

        // Checks if the Bluetooth is turned on. If not, it prompts the user to turn it on. It can't pass through this method if the Bluetooth is turned off.
        if(!bluetoothManager.isBtEnabled()) {
            showBluetoothSettings();
        }

        /*
            Resetting pairedDevice (which is the reference to the leader robot) to null because every time the app opens, it should not access the old value of the
            paired device when the Activity was paused because it can have problems in the connectivity. 
            This can happen if you pause the activity (meaning you pressed the home button in your device) and unpair your Android device, then you open your application again. 
            The reference of the pairedDevice will be to the device you have just unpaired, which is no longer valid and you can't connect to it any longer. This is why you should always have a fresh reference to the leader robot to avoid connectivity issues.
         */
        pairedDevice = null;

        // Finds in the internal memory of your Android device the leader robot's name and Bluetooth Address
        String leaderName = appPreference.findStringPref(appPreference.LEADER_DEVICE_NAME);
        String leaderAddress = appPreference.findStringPref(appPreference.LEADER_DEVICE_ADDRESS);

        // Finds the pairedDevice using the name and Bluetooth Address stored in memory
        // At this point pairedDevice can still be equal to null or it can be equal to the device information of your leader robot
        findPairedDeviceUsingPrefs();

        /*
            pairedDevice will be null in 2 instances
            1. App is newly opened
            2. User unpairs the chosen leader device (that's why there is a checker if the leaderName is originally empty or not). The leader name at the start of the application is empty because there has been no chosen leader yet.
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
            // Sets the text view in the screen to the name of the pairedDevice
            leaderDeviceTextView.setText(leaderName);
        }

        // The following sets up the reconnect button in the upper right corner of the screen
        ActionBar actionBar = getActionBar();
        if(actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);

            LayoutInflater layoutInflater = LayoutInflater.from(this);
            View view = layoutInflater.inflate(R.layout.action_bar_custom, null);
            reconnectBtn = (Button) view.findViewById(R.id.reconnect_button);
            // The reconnectBtn is the reference for the Reconnect Button and we add listeners to buttons to know what actions they should do when clicked.
            // In this case, it tries to find the pairedDevice using the leader name and the bluetooth address and it connects to the pairedDevice
            reconnectBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    findPairedDeviceUsingPrefs();
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
        Log.i(TAG, "Exiting MainActivity");
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

    private void findPairedDeviceUsingPrefs() {
        String leaderName = appPreference.findStringPref(appPreference.LEADER_DEVICE_NAME);
        String leaderAddress = appPreference.findStringPref(appPreference.LEADER_DEVICE_ADDRESS);
        BluetoothDevice[] pairedDevices = bluetoothManager.getPairedDevices();
        for (BluetoothDevice btDevice: pairedDevices) {
            if( btDevice.getName().equals(leaderName) && btDevice.getAddress().equals(leaderAddress)) {
                Log.i(TAG, "Paired device " + btDevice);
                pairedDevice = btDevice;
                break;
            }
        }
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
                // Need to set pairedDevice to null because it was unable to pair the device
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
