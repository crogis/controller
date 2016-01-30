package com.controller.actions;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import com.controller.R;
import com.controller.BluetoothManager;


public class MoveActivity extends Activity {

    private BluetoothManager bluetoothManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_move);

        ActionBar actionBar = getActionBar();
        if(actionBar != null) {
            // Setting this to true enables the back button in the Action Bar
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Move");
            actionBar.setIcon(android.R.color.transparent);
        }

        bluetoothManager = BluetoothManager.getInstance();

        setUpControllerButtons();
    }

    @Override
    public void onResume() {
        super.onResume();
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

    private void setUpControllerButtons() {
        setActionOnClickListener(R.id.up_image_button, 2);
        setActionOnClickListener(R.id.left_image_button, 4);
        setActionOnClickListener(R.id.right_image_button, 6);
        setActionOnClickListener(R.id.down_image_button, 8);
    }

    private void setActionOnClickListener(int resId, final int command) {
        ImageButton imgBtn = (ImageButton) findViewById(resId);
        imgBtn.setOnClickListener(new View.OnClickListener() {
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
        System.out.println("ON DESTROY MOVE ACTIVITY");
    }
}
