package jp.ac.titech.itpro.sdl.trackballemulator;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Created by onuki on 2017/07/07.
 */

public class SettingsActivity extends Activity {

    String TAG = "SettingsActivity";

    float alpha, threshold, scale;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    TextView scaleText, thresholdText;
    SeekBar scaleBar, thresholdBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Log.d(TAG, "onCreate");

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();

        TypedValue outValue = new TypedValue();
        getResources().getValue(R.dimen.alpha, outValue, true);
        alpha = sharedPreferences.getFloat("alpha", outValue.getFloat());
        outValue = new TypedValue();
        getResources().getValue(R.dimen.threshold, outValue, true);
        threshold = sharedPreferences.getFloat("threshold", outValue.getFloat());
        outValue = new TypedValue();
        getResources().getValue(R.dimen.scale, outValue, true);
        scale = sharedPreferences.getFloat("scale", outValue.getFloat());

        findViewById(R.id.save_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.apply();
                TypedValue outValue = new TypedValue();
                outValue = new TypedValue();
                getResources().getValue(R.dimen.threshold, outValue, true);
                threshold = sharedPreferences.getFloat("threshold", 0);
                outValue = new TypedValue();
                getResources().getValue(R.dimen.scale, outValue, true);
                scale = sharedPreferences.getFloat("scale", -1);

                Log.d(TAG, "thre, scale = " + threshold + ", " + scale);
                finish();
            }
        });

        Log.d(TAG, "thre, scale = " + threshold + ", " + scale);

        scaleText = (TextView) findViewById(R.id.scale_text);
        thresholdText = (TextView) findViewById(R.id.threshold_text);
        scaleBar = (SeekBar) findViewById(R.id.scale_bar);
        thresholdBar = (SeekBar) findViewById(R.id.threshold_bar);
        // max value
        scaleBar.setMax(500);
        thresholdBar.setMax(500);
        // init value
        scaleBar.setProgress((int)(scale*100));
        thresholdBar.setProgress((int)(threshold*100));
        // set listener
        scaleBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                editor.putFloat("scale", (float)seekBar.getProgress() / 100);
                Log.d(TAG, "put scale " + (float)seekBar.getProgress() / 100);
            }
        });
        thresholdBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                editor.putFloat("threshold", (float)seekBar.getProgress() / 100);
                Log.d(TAG, "put threshold " + (float)seekBar.getProgress() / 100);
            }
        });
    }
}
