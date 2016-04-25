package com.dobi.walkingsynth.accelerometer;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

public class AccelerometerDetector extends Service implements SensorEventListener {

    private static final String TAG = AccelerometerDetector.class.getSimpleName();

    public static final int CONFIG_SENSOR = SensorManager.SENSOR_DELAY_GAME;

    private double[] mAccelResult = new double[AccelerometerSignals.count];

    private AccelerometerProcessing mAccelProcessing = AccelerometerProcessing.getInstance();

    private SensorManager sensorManager;
    private Sensor sensor;

    private OnStepCountChangeListener onStepCountChangeListener;

    private int stepsCount = 0;

    public final static String MY_ACTION = "MY_ACTION";

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {

        Log.i("Notification", "TRIGGER!!");

        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            this.setStepCountChangeListener(new OnStepCountChangeListener() {

                @Override
                public void onStepCountChange(long eventMsecTime) {
                    ++stepsCount;

                    intent.setAction(MY_ACTION);
                    intent.putExtra("STEPS", stepsCount);
                    sendBroadcast(intent);

                    Log.i("STEPS", String.valueOf(stepsCount));
                }

            });

            this.startDetector();

        } else {
            Log.d(TAG, "Failure! No accelerometer.");
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
            Log.d(TAG,"The sensor is not supported and unsuccessfully enabled.");
        }
    }

    public void stopDetector() {
        sensorManager.unregisterListener(this, sensor);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        mAccelProcessing.setEvent(event);
        final long eventMsecTime = mAccelProcessing.timestampToMilliseconds();

        mAccelResult[0] = mAccelProcessing.calcMagnitudeVector(0);
        mAccelResult[0] = mAccelProcessing.calcExpMovAvg(0);
        mAccelResult[1] = mAccelProcessing.calcMagnitudeVector(1);
        //Log.d(TAG, "Vec: x= " + mAccelResult[0] + " C=" + eventMsecTime);

        // step detection
        if (mAccelProcessing.stepDetected(1)) {
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
