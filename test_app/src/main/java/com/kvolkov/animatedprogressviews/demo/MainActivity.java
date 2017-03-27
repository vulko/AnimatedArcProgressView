package com.kvolkov.animatedprogressviews.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button btnSingle = (Button) findViewById(R.id.btn_single);
        final Button btnAll = (Button) findViewById(R.id.btn_all);

        btnSingle.setOnClickListener(this);
        btnAll.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btn_single:
                Intent intentS = new Intent(this, SingleProgressActivity.class);
                startActivity(intentS);
                break;

            case R.id.btn_all:
                Intent intentA = new Intent(this, AllProgressAnimationsActivity.class);
                startActivity(intentA);
                break;

        }
    }
}
