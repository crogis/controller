package com.controller;

import android.app.Application;

public class ControllerApplication extends Application {

    public void onCreate() {
        super.onCreate();
        FontsOverride.setDefaultFont(this, "SANS_SERIF", "fonts/DroidSans.ttf");
    }
}
