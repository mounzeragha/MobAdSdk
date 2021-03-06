package com.oqunet.mobad_sdk.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.oqunet.mobad_sdk.receiver.PhoneCallReceiver;

public class AdJobService extends Service {
    private static final String LOG_TAG = AdJobService.class.getSimpleName();
    private PhoneCallReceiver phoneCallReceiver = new PhoneCallReceiver();


    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(LOG_TAG, "OnCreate AdJobService");

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.BOOT_COMPLETED");
        intentFilter.addAction("android.intent.action.QUICKBOOT_POWERON");
        intentFilter.addAction("android.intent.action.PHONE_STATE");
        this.registerReceiver(phoneCallReceiver, intentFilter);

        Log.i(LOG_TAG, "PhoneCallReceiver Created....");


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        this.unregisterReceiver(phoneCallReceiver);
        Log.i(LOG_TAG, "PhoneCallReceiver Destroyed....");
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


}
