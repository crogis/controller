package com.controller.adapter;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.controller.R;

public class PairedDevicesAdapter extends ArrayAdapter<BluetoothDevice> {

    Context context;
    int resourceId;
    BluetoothDevice[] pairedDevices;

    public PairedDevicesAdapter(Context context, int resourceId, BluetoothDevice[] pairedDevices) {
        super(context, resourceId, pairedDevices);
        this.context = context;
        this.resourceId = resourceId;
        this.pairedDevices = pairedDevices;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = ((Activity)context).getLayoutInflater();

        View v = inflater.inflate(resourceId, null);

        TextView tv = (TextView) v.findViewById(R.id.paired_device_text_view);

        BluetoothDevice pairedDevice = pairedDevices[position];
        tv.setText(pairedDevice.getName());
        return v;
    }
}
