package com.dobi.walkingsynth.accelerometer;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.dobi.walkingsynth.Constants;
import com.dobi.walkingsynth.MainActivity;

public class AccelerometerDetector extends Service implements SensorEventListener {

    public static final int CONFIG_SENSOR = SensorManager.SENSOR_DELAY_GAME;

    private double[] accelerometerSignals = new double[AccelerometerSignals.count];

    private AccelerometerProcessing accelerometerProcessing = AccelerometerProcessing.getInstance();

    private SensorManager sensorManager;
    private Sensor sensor;

    private OnStepCountChangeListener onStepCountChangeListener;

    private int stepsCount = 0;

    Intent stepsCountMessageIntent;

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {

        Log.i("START PEDOMETER", "YES!!");

        stepsCountMessageIntent = new Intent(Constants.BROADCAST_ACTION);
        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            this.setStepCountChangeListener(new OnStepCountChangeListener() {

                @Override
                public void onStepCountChange(long eventMsecTime) {
                    ++stepsCount;

                    stepsCountMessageIntent.putExtra(Constants.EXTENDED_DATA_STATUS, String.valueOf(stepsCount));
                    LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(stepsCountMessageIntent);

                    Log.i("STEPS", String.valueOf(stepsCount));
                }

            });

            this.startDetector();

        } else {
            Log.d("FAIL", "Failure! No accelerometer.");
        }

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void setStepCountChangeListener(OnStepCountChangeListener listener) {
        onStepCountChangeListener = listener;
    }

    public void startDetector() {
        if (!sensorManager.registerListener(this, sensor, CONFIG_SENSOR)) {
            Log.d("FAIL","The sensor is not supported and unsuccessfully enabled.");
        }
    }

    public void stopDetector() {
        sensorManager.unregisterListener(this, sensor);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        accelerometerProcessing.setEvent(event);
        final long eventMsecTime = accelerometerProcessing.timestampToMilliseconds();

        accelerometerSignals[0] = accelerometerProcessing.calcMagnitudeVector(0);
        accelerometerSignals[0] = accelerometerProcessing.calcExpMovAvg(0);
        accelerometerSignals[1] = accelerometerProcessing.calcMagnitudeVector(1);
        //Log.d(TAG, "Vec: x= " + mAccelResult[0] + " C=" + eventMsecTime);

        // step detection
        if (accelerometerProcessing.stepDetected(1)) {
            // step is found!
            // notify potential listeners
            if (onStepCountChangeListener != null)
                onStepCountChangeListener.onStepCountChange(eventMsecTime);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onDestroy() {
        this.stopDetector();
        super.onDestroy();
    }
}
