package com.kvolkov.animatedprogressviews.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SeekBar;
import android.widget.Spinner;

import com.kvolkov.animatedprogressviews.AnimatedArcIndefiniteProgressView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Spinner animationTypeSelector = (Spinner) findViewById(R.id.animationTypeSelector);
        final Spinner opacityAnimationTypeSelector = (Spinner) findViewById(R.id.opacityAnimationTypeSelector);
        final SeekBar arcNumSeekBar = (SeekBar) findViewById(R.id.arcCountBar);
        final SeekBar arcStrokeWidthSeekBar = (SeekBar) findViewById(R.id.arcStrokeWidthBar);
        final SeekBar arcPaddingSeekBar = (SeekBar) findViewById(R.id.arcPaddingBar);
        final AnimatedArcIndefiniteProgressView progressView = (AnimatedArcIndefiniteProgressView) findViewById(R.id.progress);

        animationTypeSelector.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                progressView.setProgressAnimationType(position - 1); // -1 magic, because of test stub for opacity animation, or special effects
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        opacityAnimationTypeSelector.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                progressView.setOpacityAnimationType(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        arcNumSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress > 0)
                    progressView.setArcCount(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        arcStrokeWidthSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress > 0)
                    // normalize it here, though values supported might be up to 500, for better usability of test app
                    progressView.setArcStrokeWidth((float) progress / 100.f);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        arcPaddingSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress > 0)
                    progressView.setArcSpacing(1.f + (float) progress / 10.f);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
}
