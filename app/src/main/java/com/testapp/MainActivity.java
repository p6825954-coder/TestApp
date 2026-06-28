package com.testapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    private LinearLayout deviceContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        deviceContainer = findViewById(R.id.deviceContainer);
        TextView statusText = findViewById(R.id.statusText);
        statusText.setText("🟢 UI Ready (socket disabled)");

        // Contoh dummy device
        addDeviceCard("REALME RMX3939 ANDROID 15", "192.168.1.5", true);
        addDeviceCard("REDMI 23053RN02A ANDROID 15", "10.0.0.12", false);
    }

    private void addDeviceCard(String model, String ip, boolean online) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setBackground(getDrawable(R.drawable.card_border));
        item.setPadding(20, 20, 20, 20);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 12);
        item.setLayoutParams(lp);

        TextView modelView = new TextView(this);
        modelView.setText("📱 " + model);
        modelView.setTextColor(0xFFFFFFFF);
        modelView.setTextSize(16);

        TextView ipView = new TextView(this);
        ipView.setText("IP: " + ip + (online ? " 🟢" : " 🔴"));
        ipView.setTextColor(0xFF9AA3B2);

        item.addView(modelView);
        item.addView(ipView);

        item.setOnClickListener(v -> {
            Intent i = new Intent(this, ControlActivity.class);
            i.putExtra("deviceId", model);
            startActivity(i);
        });

        deviceContainer.addView(item);
    }
}
