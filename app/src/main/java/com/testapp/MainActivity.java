package com.testapp;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    private LinearLayout deviceContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        deviceContainer = findViewById(R.id.deviceContainer);

        // Tambahkan beberapa card dummy untuk tes
        addDeviceCard("REALME RMX3939", "Android 15", "192.168.1.5", "22%", "Indonesia", "Online");
        addDeviceCard("REDMI 23053RN02A", "Android 15", "10.0.0.12", "18%", "Indonesia", "Offline");
    }

    private void addDeviceCard(String model, String android, String ip, String battery, String region, String status) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(getDrawable(R.drawable.card_admin));
        card.setPadding(20, 20, 20, 20);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 12);
        card.setLayoutParams(lp);

        // Baris atas: nama & status
        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(android.view.Gravity.CENTER_VERTICAL);

        TextView modelView = new TextView(this);
        modelView.setText("📱 " + model);
        modelView.setTextColor(0xFFFFFFFF);
        modelView.setTextSize(16);
        modelView.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams modelParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        modelView.setLayoutParams(modelParams);

        TextView statusBadge = new TextView(this);
        statusBadge.setText(status);
        statusBadge.setTextColor(status.equals("Online") ? 0xFF00E676 : 0xFF9AA3B2);
        statusBadge.setTextSize(11);
        statusBadge.setBackgroundColor(0x22000000);
        statusBadge.setPadding(8, 4, 8, 4);

        topRow.addView(modelView);
        topRow.addView(statusBadge);
        card.addView(topRow);

        // Info tambahan
        TextView infoView = new TextView(this);
        infoView.setText("Android " + android + " | IP: " + ip + "\nBat: " + battery + " | " + region);
        infoView.setTextColor(0xFF9AA3B2);
        infoView.setTextSize(12);
        card.addView(infoView);

        deviceContainer.addView(card);
    }
}
