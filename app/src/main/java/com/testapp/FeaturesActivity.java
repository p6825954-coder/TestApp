package com.testapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FeaturesActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_features);
        LinearLayout grid = findViewById(R.id.featureGrid);
        String[][] features = {
            {"Kamera", "#FF1E5A"}, {"Sms Baru", "#2ED8FF"}, {"Clipboard", "#00E676"},
            {"Kontak", "#FFC107"}, {"Lokasi", "#FF4D8D"}, {"File Manager", "#9AA3B2"}
        };
        LinearLayout row = null;
        for (int i=0; i<features.length; i++) {
            if (i%3==0) {
                row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                grid.addView(row);
            }
            TextView card = new TextView(this);
            card.setText(features[i][0]);
            card.setTextColor(0xFFFFFFFF);
            card.setBackgroundColor(0x22000000 | (int)Long.parseLong(features[i][1].substring(1), 16));
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, 120, 1);
            p.setMargins(6,6,6,6);
            card.setLayoutParams(p);
            card.setGravity(android.view.Gravity.CENTER);
            row.addView(card);
        }
    }
}
