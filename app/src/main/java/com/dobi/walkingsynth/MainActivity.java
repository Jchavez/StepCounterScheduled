package com.dobi.walkingsynth;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.dobi.walkingsynth.accelerometer.AccelerometerDetector;
import com.dobi.walkingsynth.accelerometer.AccelerometerGraph;
import com.dobi.walkingsynth.accelerometer.AccelerometerProcessing;
import com.dobi.walkingsynth.accelerometer.OnStepCountChangeListener;
import com.dobi.walkingsynth.music.MusicCreator;
import com.dobi.walkingsynth.music.SynthesizerSequencer;
import com.dobi.walkingsynth.music.TimeCounter;

import org.achartengine.GraphicalView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Starting point. Sets the whole UI.
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private int mStepCount = 0;
    private AccelerometerDetector mAccelDetector;
    private AccelerometerGraph mAccelGraph;
    private TextView mThreshValTextView;
    private TextView mStepCountTextView;
    private MusicCreator mMusicCreator;

    // constant reference
    private final AccelerometerProcessing mAccelerometerProcessing = AccelerometerProcessing.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // set default locale:
        Locale.setDefault(Locale.ENGLISH);

        // instantiate music analyzer
        mMusicCreator = new MusicCreator(getResources(),getCacheDir());

        // accelerometer graph setup:
        mAccelGraph = new AccelerometerGraph(AccelerometerProcessing.THRESH_INIT_VALUE);

        // base note spinner:
        initializeNotesSpinner();

        // scales spinner:
        initializeScalesSpinner();

        // step intervals spinner:
        initializeStepsSpinner();

        // get and configure text views
        mThreshValTextView = (TextView)findViewById(R.id.threshval_textView);
        formatThreshTextView(AccelerometerProcessing.THRESH_INIT_VALUE);
        mStepCountTextView = (TextView)findViewById(R.id.stepcount_textView);
        mStepCountTextView.setText(String.valueOf(0));


        // UI default setup
        GraphicalView graphicalView = mAccelGraph.getView(this);
        graphicalView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));

        LinearLayout graphLayout = (LinearLayout)findViewById(R.id.graph_layout);
        graphLayout.addView(graphicalView);

        // initialize accelerometer
        SensorManager sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        mAccelDetector = new AccelerometerDetector(sensorManager, mAccelGraph);
        mAccelDetector.setStepCountChangeListener(new OnStepCountChangeListener() {
            @Override
            public void onStepCountChange(long eventMsecTime) {
                ++mStepCount;
                mStepCountTextView.setText(String.valueOf(mStepCount));
                mMusicCreator.getAnalyzer().onStep(eventMsecTime);
                mMusicCreator.invalidateStep(mStepCount);
            }
        });

        // seek bar configuration
        initializeSeekBar();

    }

    private void initializeNotesSpinner() {
        ArrayList<String> notesList = new ArrayList<>();
        for ( String key : SynthesizerSequencer.notes.keySet())
        {
            notesList.add(key);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,R.layout.support_simple_spinner_dropdown_item,notesList);
        Spinner baseNotesSpinner = (Spinner) findViewById(R.id.base_notes_spinner);
        baseNotesSpinner.setAdapter(adapter);
        baseNotesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mMusicCreator.invalidateBaseNote(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void initializeScalesSpinner() {
        ArrayList<String> scalesList = new ArrayList<>();
        for ( String key : SynthesizerSequencer.scales.keySet())
        {
            scalesList.add(key);
        }
        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(this,R.layout.support_simple_spinner_dropdown_item,scalesList);
        Spinner scalesSpinner = (Spinner) findViewById(R.id.scale_spinner);
        scalesSpinner.setAdapter(adapter2);
        scalesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mMusicCreator.invalidateScale(parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void initializeStepsSpinner() {
        ArrayList<Integer> stepsList = new ArrayList<>();
        int l3 = SynthesizerSequencer.stepIntervals.length;
        for (int i = 0; i < l3; i++)
        {
            stepsList.add(SynthesizerSequencer.stepIntervals[i]);
        }
        ArrayAdapter<Integer> adapter3 = new ArrayAdapter<>(this,R.layout.support_simple_spinner_dropdown_item,stepsList);
        Spinner timeIntervalsSpinner = (Spinner)findViewById(R.id.steps_interval_spinner);
        timeIntervalsSpinner.setAdapter(adapter3);
        timeIntervalsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mMusicCreator.invalidateStepInterval(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    /**
     * SeekBar is the publisher.
     * The subscribers are: AccelerometerGraph and AccelerometerProcessing instances.
     */
    private void initializeSeekBar() {
        final SeekBar seekBar = (SeekBar)findViewById(R.id.offset_seekBar);
        seekBar.setMax(130 - 90);
        seekBar.setProgress((int) AccelerometerProcessing.getInstance().getThresholdValue());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                double threshold = AccelerometerProcessing.THRESH_INIT_VALUE * (progress + 90) / 100;
                mAccelerometerProcessing.onThresholdChange(threshold);
                mAccelGraph.onThresholdChange(threshold);
                formatThreshTextView(threshold);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void formatThreshTextView(double v) {
        final DecimalFormat df = new DecimalFormat("#.##");
        mThreshValTextView.setText(String.valueOf(df.format(v)));
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
        mMusicCreator.startCSound();
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
        mMusicCreator.destroyCSound();
    }
}
