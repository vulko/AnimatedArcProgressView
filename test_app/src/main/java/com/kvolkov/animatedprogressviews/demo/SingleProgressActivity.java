package com.kvolkov.animatedprogressviews.demo;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;

import com.kvolkov.animatedprogressviews.ArcLoadingView;

import java.util.ArrayList;
import java.util.List;

public class SingleProgressActivity extends AppCompatActivity {

    // colors for demo
    private static List<Integer> sColorList = new ArrayList<>();
    static {
        sColorList.add(Color.RED);
        sColorList.add(Color.BLUE);
        sColorList.add(Color.GREEN);
        sColorList.add(Color.GRAY);
        sColorList.add(Color.CYAN);
        sColorList.add(Color.MAGENTA);
        sColorList.add(Color.YELLOW);
        sColorList.add(Color.DKGRAY);
        sColorList.add(Color.RED);
        sColorList.add(Color.BLUE);
        sColorList.add(Color.GREEN);
        sColorList.add(Color.GRAY);
        sColorList.add(Color.CYAN);
        sColorList.add(Color.MAGENTA);
        sColorList.add(Color.YELLOW);
        sColorList.add(Color.DKGRAY);
        sColorList.add(Color.RED);
        sColorList.add(Color.BLUE);
        sColorList.add(Color.GREEN);
        sColorList.add(Color.GRAY);
        sColorList.add(Color.CYAN);
        sColorList.add(Color.MAGENTA);
        sColorList.add(Color.YELLOW);
        sColorList.add(Color.DKGRAY);
        sColorList.add(Color.RED);
        sColorList.add(Color.BLUE);
        sColorList.add(Color.GREEN);
        sColorList.add(Color.GRAY);
        sColorList.add(Color.CYAN);
        sColorList.add(Color.MAGENTA);
        sColorList.add(Color.YELLOW);
        sColorList.add(Color.DKGRAY);
        sColorList.add(Color.RED);
        sColorList.add(Color.BLUE);
        sColorList.add(Color.GREEN);
        sColorList.add(Color.GRAY);
        sColorList.add(Color.CYAN);
        sColorList.add(Color.MAGENTA);
        sColorList.add(Color.YELLOW);
        sColorList.add(Color.DKGRAY);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single);

        getSupportActionBar().hide();

        final Spinner animationTypeSelector = (Spinner) findViewById(R.id.animationTypeSelector);
        final Spinner opacityAnimationTypeSelector = (Spinner) findViewById(R.id.opacityAnimationTypeSelector);
        final CheckBox useColorsCB = (CheckBox) findViewById(R.id.useColorListCB);
        final SeekBar arcNumSeekBar = (SeekBar) findViewById(R.id.arcCountBar);
        final SeekBar arcStrokeWidthSeekBar = (SeekBar) findViewById(R.id.arcStrokeWidthBar);
        final SeekBar arcPaddingSeekBar = (SeekBar) findViewById(R.id.arcPaddingBar);
        final ArcLoadingView progressView = (ArcLoadingView) findViewById(R.id.progress);

        useColorsCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    List<Integer> colorList = new ArrayList<>();
                    for (int i = 0; i < progressView.getArcCount(); i++) {
                        colorList.add(sColorList.get(i));
                    }
                    progressView.setColorList(colorList);
                } else {
                    progressView.setColorList(null);
                }
            }
        });

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
                if (progress > 0) {
                    progressView.setArcCount(progress);
                    List<Integer> colorList = new ArrayList<>();
                    for (int i = 0; i < progress; i++) {
                        int channelVal = Math.round(255.f * i / progress);
                        colorList.add(Color.argb(channelVal, channelVal, channelVal, channelVal));
                    }
                    progressView.setColorList(colorList);
                }

                // update color list when num arcs changed
                if (useColorsCB.isChecked()) {
                    List<Integer> colorList = new ArrayList<>();
                    for (int i = 0; i < progressView.getArcCount(); i++) {
                        colorList.add(sColorList.get(i));
                    }
                    progressView.setColorList(colorList);
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
