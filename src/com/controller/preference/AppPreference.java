package com.controller.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class AppPreference {

    private SharedPreferences prefs;

    public String LEADER_DEVICE_NAME = "leaderDeviceName";
    public String LEADER_DEVICE_ADDRESS = "leaderDeviceAddress";

    public AppPreference(Context context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void setStringPref(String key, String value) {
       prefs.edit().putString(key, value).commit();
    }

    public String findStringPref(String key) {
        return prefs.getString(key, "-");
    }

}
