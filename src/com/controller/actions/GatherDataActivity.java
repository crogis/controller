package com.controller.actions;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import com.controller.R;

public class GatherDataActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gather_data);

        ActionBar actionBar = getActionBar();
        if(actionBar != null) {
            // Setting this to true enables the back button in the Action Bar
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Gather Data");
            actionBar.setIcon(android.R.color.transparent);
        }
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
}
