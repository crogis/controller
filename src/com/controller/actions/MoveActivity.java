package com.controller.actions;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import com.controller.R;
import com.controller.StreamManager;


public class MoveActivity extends Activity {

    private ImageButton upImgBtn, downImgBtn, leftImgBtn, rightImgBtn;

    private StreamManager streamManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_move);

        ActionBar actionBar = getActionBar();
        if(actionBar != null) {
            // Setting this to true enables the back button in the Action Bar
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Move");
        }

        streamManager = StreamManager.getInstance();

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
        upImgBtn = (ImageButton) findViewById(R.id.up_image_button);
        downImgBtn = (ImageButton) findViewById(R.id.down_image_button);
        leftImgBtn = (ImageButton) findViewById(R.id.left_image_button);
        rightImgBtn = (ImageButton) findViewById(R.id.right_image_button);

        upImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    streamManager.sendCommand("2");
                } catch (Exception e) {
                    showUnableToConnectToast();
                }
            }
        });

        downImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    streamManager.sendCommand("8");
                } catch (Exception e) {
                    showUnableToConnectToast();
                }
            }
        });

        leftImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    streamManager.sendCommand("4");
                } catch (Exception e) {
                    showUnableToConnectToast();
                }
            }
        });

        rightImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    streamManager.sendCommand("6");
                } catch (Exception e) {
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
