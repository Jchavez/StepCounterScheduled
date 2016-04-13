package com.dobi.walkingsynth;

import android.content.Context;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.dobi.walkingsynth.accelerometer.AccelerometerDetector;
import com.dobi.walkingsynth.accelerometer.AccelerometerProcessing;
import com.dobi.walkingsynth.accelerometer.OnStepCountChangeListener;
import com.dobi.walkingsynth.music.SynthesizerSequencer;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private int mStepCount = 0;
    private AccelerometerDetector mAccelDetector;
    private TextView mStepCountTextView;

    // constant reference
    private final AccelerometerProcessing mAccelerometerProcessing = AccelerometerProcessing.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // set default locale:
        Locale.setDefault(Locale.ENGLISH);

        // base note spinner:
        initializeNotesSpinner();

        // scales spinner:
        initializeScalesSpinner();

        // step intervals spinner:
        initializeStepsSpinner();

        // get and configure text views
        mStepCountTextView = (TextView)findViewById(R.id.stepcount_textView);
        mStepCountTextView.setText(String.valueOf(0));

        // initialize accelerometer
        SensorManager sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        mAccelDetector = new AccelerometerDetector(sensorManager);
        mAccelDetector.setStepCountChangeListener(new OnStepCountChangeListener() {

            @Override
            public void onStepCountChange(long eventMsecTime) {
                ++mStepCount;
                mStepCountTextView.setText(String.valueOf(mStepCount));
            }
        });
    }

    private void initializeNotesSpinner() {
        ArrayList<String> notesList = new ArrayList<>();
        for ( String key : SynthesizerSequencer.notes.keySet())
        {
            notesList.add(key);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,R.layout.support_simple_spinner_dropdown_item,notesList);
    }

    private void initializeScalesSpinner() {
        ArrayList<String> scalesList = new ArrayList<>();
        for ( String key : SynthesizerSequencer.scales.keySet())
        {
            scalesList.add(key);
        }
        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(this,R.layout.support_simple_spinner_dropdown_item,scalesList);
    }

    private void initializeStepsSpinner() {
        ArrayList<Integer> stepsList = new ArrayList<>();
        int l3 = SynthesizerSequencer.stepIntervals.length;
        for (int i = 0; i < l3; i++)
        {
            stepsList.add(SynthesizerSequencer.stepIntervals[i]);
        }
        ArrayAdapter<Integer> adapter3 = new ArrayAdapter<>(this,R.layout.support_simple_spinner_dropdown_item,stepsList);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main_menu, menu);

        // set string values for menu
        String[] titles = getResources().getStringArray(R.array.nav_drawer_items);
        for (int i = 0; i < titles.length; i++) {
            menu.getItem(i).setTitle(titles[i]);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_threshold:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }



    @Override
    protected void onResume() {
        super.onResume();
        mAccelDetector.startDetector();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "OnPause");
        mAccelDetector.stopDetector();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "OnStop");
    }
}
