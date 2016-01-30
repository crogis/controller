package com.controller.actions;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.controller.R;
import com.controller.BluetoothManager;

public class ChangeFormationActivity extends Activity {

    private BluetoothManager bluetoothManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_formation);

        ActionBar actionBar = getActionBar();
        if(actionBar != null) {
            // Setting this to true enables the back button in the Action Bar
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Change Formation");
            actionBar.setIcon(android.R.color.transparent);
        }

        bluetoothManager = BluetoothManager.getInstance();

        setUpChangeFormationButtons();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            /*
                android.R.id.home refers to the back button
             */
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return false;
        }
    }

    private void setUpChangeFormationButtons() {
        setActionOnClickListener(R.id.straight_button, 5);
        setActionOnClickListener(R.id.square_button, 7);
        setActionOnClickListener(R.id.diamond_button, 9);
    }

    private void setActionOnClickListener(int resId, final int command) {
        Button button = (Button) findViewById(resId);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean success = bluetoothManager.sendCommand(command);
                if(!success) {
                    showUnableToConnectToast();
                }
            }
        });
    }

    private void showUnableToConnectToast() {
        Toast.makeText(this, "Unable to connect to device", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("ON DESTROY CHANGE FORMATION ACTIVITY");
    }
}
