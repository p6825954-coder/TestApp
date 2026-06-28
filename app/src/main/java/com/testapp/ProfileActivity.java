package com.testapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class ProfileActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        String model = getIntent().getStringExtra("deviceModel");
        String battery = getIntent().getStringExtra("battery");
        String network = getIntent().getStringExtra("network");
        String sim1 = getIntent().getStringExtra("sim1");
        String sim2 = getIntent().getStringExtra("sim2");

        ((TextView) findViewById(R.id.profileDeviceInfo)).setText(model + " | Bat: " + battery + "%");
        ((TextView) findViewById(R.id.profModel)).setText(model);
        ((TextView) findViewById(R.id.profBattery)).setText(battery + "%");
        ((TextView) findViewById(R.id.profNetwork)).setText(network);
        ((TextView) findViewById(R.id.profSim1)).setText(sim1);
        ((TextView) findViewById(R.id.profSim2)).setText(sim2);
    }

    public void goBack(View v) { finish(); }
}
