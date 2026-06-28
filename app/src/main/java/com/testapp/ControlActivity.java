package com.testapp;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class ControlActivity extends Activity {
    private String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        deviceId = getIntent().getStringExtra("deviceId");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(0xFF0B0E17);
        layout.setPadding(32, 32, 32, 32);

        Button btn = new Button(this);
        btn.setText("📳 Test Vibrate");
        btn.setOnClickListener(v -> Toast.makeText(this, "Device: " + deviceId, Toast.LENGTH_SHORT).show());
        layout.addView(btn);

        setContentView(layout);
    }
}
