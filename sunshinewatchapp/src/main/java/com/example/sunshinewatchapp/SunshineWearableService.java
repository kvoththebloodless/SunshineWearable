package com.example.sunshinewatchapp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.google.android.gms.wearable.WearableListenerService;

public class SunshineWearableService extends WearableListenerService {
    public static final String WEATHER_TODAY = "/today";
    public static final String WEATHER_TOMORROW = "/tomm";
    public static final String WEATHER_DAY_AFTER = "/next";
    public static final String ASSET = "weatherpic";
    public static final String DATA = "data";
    public static final String[] PATH = {"/today", "/tomm", "/next"};
}
