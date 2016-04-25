package com.dobi.walkingsynth;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.dobi.walkingsynth.accelerometer.AccelerometerDetector;

public class CancelPedometerBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("Cancel Pedometer", "YES!!");

        context.stopService(new Intent(context, AccelerometerDetector.class));
    }
}
