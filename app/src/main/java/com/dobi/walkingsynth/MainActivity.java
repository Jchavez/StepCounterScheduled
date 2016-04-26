package com.dobi.walkingsynth;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.dobi.walkingsynth.accelerometer.AccelerometerDetector;

public class MainActivity extends AppCompatActivity {
    private TextView mStepCountTextView;

    ReceiveStepsCounterMessage receiveStepsCounterMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mStepCountTextView = (TextView)findViewById(R.id.stepcount_textView);
        mStepCountTextView.setText(String.valueOf(0));


        startPedometer(10000);
        stopPedometer(50000);

        receiveStepsCounterMessage = new ReceiveStepsCounterMessage();

        IntentFilter mStatusIntentFilter = new IntentFilter(
            Constants.BROADCAST_ACTION);

        LocalBroadcastManager.getInstance(this).registerReceiver(
            receiveStepsCounterMessage,
            mStatusIntentFilter);
    }

    private void startPedometer(int startDelay) {
        Intent accelerometerDetectorIntent = new Intent(this, AccelerometerDetector.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, accelerometerDetectorIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        long startDelayInMillis = SystemClock.elapsedRealtime() + startDelay;

        AlarmManager startAlarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        startAlarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, startDelayInMillis, pendingIntent);
    }

    private void stopPedometer(int stopDelay) {
        Intent cancelPedometerBroadcastReceiverIntent = new Intent(this, CancelPedometerBroadcastReceiver.class);
        PendingIntent cancellationPendingIntent = PendingIntent.getBroadcast(this, 0, cancelPedometerBroadcastReceiverIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        long stopDelayInMillis = SystemClock.elapsedRealtime() + stopDelay;

        AlarmManager stopAlarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        stopAlarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, stopDelayInMillis, cancellationPendingIntent);
    }

    public class ReceiveStepsCounterMessage extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constants.BROADCAST_ACTION)){
                Log.i("LISTENING BROADCAST", "YES");
            }
        }
    }
}
