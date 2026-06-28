package com.testapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DashboardActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        LinearLayout container = findViewById(R.id.deviceContainer);
        // Contoh dummy device card
        addDeviceCard(container, "REALME RMX3939 ANDROID 15", "192.168.1.5", true);
        addDeviceCard(container, "REDMI 23053RN02A ANDROID 15", "10.0.0.12", false);
    }

    private void addDeviceCard(LinearLayout parent, String name, String ip, boolean online) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(getDrawable(R.drawable.neon_border));
        card.setPadding(24,24,24,24);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0,0,0,16);
        card.setLayoutParams(lp);

        TextView nameView = new TextView(this);
        nameView.setText(name);
        nameView.setTextColor(0xFFFFFFFF);
        nameView.setTextSize(16);
        TextView ipView = new TextView(this);
        ipView.setText(ip);
        ipView.setTextColor(0xFF9AA3B2);
        TextView status = new TextView(this);
        status.setText(online ? "ONLINE" : "OFFLINE");
        status.setTextColor(online ? 0xFF00E676 : 0xFF9AA3B2);

        card.addView(nameView);
        card.addView(ipView);
        card.addView(status);

        card.setOnClickListener(v -> startActivity(new Intent(this, FeaturesActivity.class)));
        parent.addView(card);
    }
}
