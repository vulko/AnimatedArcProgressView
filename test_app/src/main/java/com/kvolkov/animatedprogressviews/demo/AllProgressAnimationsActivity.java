package com.kvolkov.animatedprogressviews.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SeekBar;
import android.widget.Spinner;

import com.kvolkov.animatedprogressviews.AnimatedArcIndefiniteProgressView;

import java.util.ArrayList;
import java.util.List;

public class AllProgressAnimationsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_animations);

        final Spinner opacityAnimationTypeSelector = (Spinner) findViewById(R.id.opacityAnimationTypeSelector);
        final SeekBar arcNumSeekBar = (SeekBar) findViewById(R.id.arcCountBar);
        final SeekBar arcStrokeWidthSeekBar = (SeekBar) findViewById(R.id.arcStrokeWidthBar);
        final SeekBar arcPaddingSeekBar = (SeekBar) findViewById(R.id.arcPaddingBar);

        final List<AnimatedArcIndefiniteProgressView> progressViews = new ArrayList<>();
        progressViews.add((AnimatedArcIndefiniteProgressView) findViewById(R.id.progress0));
        progressViews.add((AnimatedArcIndefiniteProgressView) findViewById(R.id.progress1));
        progressViews.add((AnimatedArcIndefiniteProgressView) findViewById(R.id.progress2));
        progressViews.add((AnimatedArcIndefiniteProgressView) findViewById(R.id.progress3));
        progressViews.add((AnimatedArcIndefiniteProgressView) findViewById(R.id.progress4));
        progressViews.add((AnimatedArcIndefiniteProgressView) findViewById(R.id.progress5));
        progressViews.add((AnimatedArcIndefiniteProgressView) findViewById(R.id.progress6));
        progressViews.add((AnimatedArcIndefiniteProgressView) findViewById(R.id.progress7));
        progressViews.add((AnimatedArcIndefiniteProgressView) findViewById(R.id.progress8));
        progressViews.add((AnimatedArcIndefiniteProgressView) findViewById(R.id.progress9));
        progressViews.add((AnimatedArcIndefiniteProgressView) findViewById(R.id.progress10));
        progressViews.add((AnimatedArcIndefiniteProgressView) findViewById(R.id.progress11));
        progressViews.add((AnimatedArcIndefiniteProgressView) findViewById(R.id.progress12));

        opacityAnimationTypeSelector.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                for (AnimatedArcIndefiniteProgressView progressView : progressViews) {
                    progressView.setOpacityAnimationType(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        arcNumSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                for (AnimatedArcIndefiniteProgressView progressView : progressViews) {
                    if (progress > 0)
                        progressView.setArcCount(progress);
                }
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
                for (AnimatedArcIndefiniteProgressView progressView : progressViews) {
                    if (progress > 0)
                        // normalize it here, though values supported might be up to 500, for better usability of test app
                        progressView.setArcStrokeWidth((float) progress / 100.f);
                }
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
                for (AnimatedArcIndefiniteProgressView progressView : progressViews) {
                    if (progress > 0)
                        progressView.setArcSpacing(1.f + (float) progress / 10.f);
                }
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
