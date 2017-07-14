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

    float inc_alpha, dec_alpha, threshold, scale;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    TextView scaleText, thresholdText, incText, decText;
    SeekBar scaleBar, thresholdBar, incBar, decBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Log.d(TAG, "onCreate");

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();

        getSettings();

        findViewById(R.id.initiate_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TypedValue outValue = new TypedValue();
                getResources().getValue(R.dimen.inc_alpha, outValue, true);
                inc_alpha = outValue.getFloat();
                outValue = new TypedValue();
                getResources().getValue(R.dimen.dec_alpha, outValue, true);
                dec_alpha = outValue.getFloat();
                outValue = new TypedValue();
                getResources().getValue(R.dimen.threshold, outValue, true);
                threshold = outValue.getFloat();
                outValue = new TypedValue();
                getResources().getValue(R.dimen.scale, outValue, true);
                scale = outValue.getFloat();
                scaleBar.setProgress((int)(scale*100));
                thresholdBar.setProgress((int)(threshold*100));
                incBar.setProgress((int)(inc_alpha*100-1));
                decBar.setProgress((int)(dec_alpha*100-1));
            }
        });

        findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.save_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putFloat("scale", (float)scaleBar.getProgress() / 100);
                editor.putFloat("threshold", (float)thresholdBar.getProgress() / 100);
                editor.putFloat("inc_alpha", (float)(incBar.getProgress()+1) / 100);
                editor.putFloat("dec_alpha", (float)(decBar.getProgress()+1) / 100);
                editor.apply();
                finish();
            }
        });

        Log.d(TAG, "thre, scale = " + threshold + ", " + scale);

        scaleText = (TextView) findViewById(R.id.scale_text);
        thresholdText = (TextView) findViewById(R.id.threshold_text);
        incText = (TextView) findViewById(R.id.inc_alpha_text);
        decText = (TextView) findViewById(R.id.dec_alpha_text);
        scaleBar = (SeekBar) findViewById(R.id.scale_bar);
        thresholdBar = (SeekBar) findViewById(R.id.threshold_bar);
        incBar = (SeekBar) findViewById(R.id.inc_alpha_bar);
        decBar = (SeekBar) findViewById(R.id.dec_alpha_bar);
        // max value
        scaleBar.setMax(300);
        thresholdBar.setMax(500);
        incBar.setMax(98);
        decBar.setMax(98);
        // init value
        scaleBar.setProgress((int)(scale*100));
        thresholdBar.setProgress((int)(threshold*100));
        incBar.setProgress((int)(inc_alpha*100-1));
        decBar.setProgress((int)(dec_alpha*100-1));
    }

    private void getSettings() {
        TypedValue outValue = new TypedValue();
        getResources().getValue(R.dimen.inc_alpha, outValue, true);
        inc_alpha = sharedPreferences.getFloat("inc_alpha", outValue.getFloat());
        outValue = new TypedValue();
        getResources().getValue(R.dimen.dec_alpha, outValue, true);
        dec_alpha = sharedPreferences.getFloat("dec_alpha", outValue.getFloat());
        outValue = new TypedValue();
        getResources().getValue(R.dimen.threshold, outValue, true);
        threshold = sharedPreferences.getFloat("threshold", outValue.getFloat());
        outValue = new TypedValue();
        getResources().getValue(R.dimen.scale, outValue, true);
        scale = sharedPreferences.getFloat("scale", outValue.getFloat());
    }
}
