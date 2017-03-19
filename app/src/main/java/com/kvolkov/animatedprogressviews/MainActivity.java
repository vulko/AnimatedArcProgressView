package com.kvolkov.animatedprogressviews;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Spinner animationTypeSelector = (Spinner) findViewById(R.id.animationTypeSelector);
        final Spinner opacityAnimationTypeSelector = (Spinner) findViewById(R.id.opacityAnimationTypeSelector);
        final AnimatedProgressView progressView = (AnimatedProgressView) findViewById(R.id.progress);

        animationTypeSelector.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                progressView.setAnimationType(position - 1); // -1 magic, because of test stub for opacity animation, or special effects
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
    }
}
