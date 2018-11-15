package com.oqunet.admob_sdk.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.oqunet.admob_sdk.AppController;
import com.oqunet.admob_sdk.service.AdHeadService;
import com.oqunet.admob_sdk.utils.AppUtils;

import java.util.Random;



public class PhoneStateReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = PhoneStateReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {

        String advertiser = AppController.advertisers[new Random().nextInt(AppController.advertisers.length)];
        AppController.setAdvertiser(advertiser);

        try {
            Log.d(LOG_TAG, "Receiver start");
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);

            if(state.equals(TelephonyManager.EXTRA_STATE_RINGING)){
                Log.d(LOG_TAG, "Incoming Call State");
                Log.d(LOG_TAG, "Ringing State Number is -" + incomingNumber);
            //    Toast.makeText(context,"Incoming Call State",Toast.LENGTH_SHORT).show();
            //    Toast.makeText(context,"Ringing State Number is -"+incomingNumber,Toast.LENGTH_SHORT).show();


            }
            if ((state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK))){
                Log.d(LOG_TAG, "Call Received State");
            //    Toast.makeText(context,"Call Received State",Toast.LENGTH_SHORT).show();
            }
            if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)){
                Log.d(LOG_TAG, "Call Idle State");

                if(AppUtils.canDrawOverlays(context)) {
                    Intent it = new Intent(context, AdHeadService.class);
                    context.startService(it);
                }

            //    Toast.makeText(context,"Call Idle State",Toast.LENGTH_SHORT).show();
                if(state.equals(TelephonyManager.CALL_STATE_OFFHOOK)){
                    //Answered Call which is ended
                //    Intent showAd = new Intent(context, AdsActivity.class);
                //    showAd.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //    context.startActivity(showAd);
                }
                if(state.equals(TelephonyManager.CALL_STATE_RINGING)){
                    //Rejected or Missed call
                }

            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }
}

