package com.testapp;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ControlActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String deviceId = getIntent().getStringExtra("deviceId");

        LinearLayout layout = new LinearLayout(this);
        layout.setBackgroundColor(0xFF0B0E17);
        layout.setPadding(32, 32, 32, 32);

        TextView tv = new TextView(this);
        tv.setText("Device: " + deviceId + "\n\nFitur kontrol akan ditambahkan.");
        tv.setTextColor(0xFFFFFFFF);
        tv.setTextSize(16);
        layout.addView(tv);

        setContentView(layout);
    }
}
