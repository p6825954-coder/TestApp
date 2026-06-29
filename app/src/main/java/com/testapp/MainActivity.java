package com.testapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.graphics.Typeface;
import android.widget.*;

public class MainActivity extends Activity {
    private TextView statusText;
    private LinearLayout deviceContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Root layout
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF080808);
        setContentView(root);

        // Header
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setBackgroundColor(0xCC151515);
        header.setPadding(16, 12, 16, 12);
        root.addView(header);

        TextView title = new TextView(this);
        title.setText("📱 Perangkat");
        title.setTextColor(0xFFFFFFFF);
        title.setTextSize(20);
        title.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        title.setLayoutParams(titleParams);
        header.addView(title);

        statusText = new TextView(this);
        statusText.setText("⏳");
        statusText.setTextColor(0xFF00E676);
        statusText.setTextSize(12);
        statusText.setBackground(getDrawable(R.drawable.card_admin));
        statusText.setPadding(12, 6, 12, 6);
        header.addView(statusText);

        // Filter
        LinearLayout filter = new LinearLayout(this);
        filter.setOrientation(LinearLayout.HORIZONTAL);
        filter.setPadding(16, 12, 16, 12);
        root.addView(filter);

        addFilterChip(filter, "Semua", true);
        addFilterChip(filter, "Online", false);
        addFilterChip(filter, "Offline", false);

        // ScrollView
        ScrollView scroll = new ScrollView(this);
        root.addView(scroll);

        deviceContainer = new LinearLayout(this);
        deviceContainer.setOrientation(LinearLayout.VERTICAL);
        deviceContainer.setPadding(16, 8, 16, 8);
        scroll.addView(deviceContainer);

        // Bottom nav
        LinearLayout bottom = new LinearLayout(this);
        bottom.setOrientation(LinearLayout.HORIZONTAL);
        bottom.setGravity(Gravity.CENTER);
        bottom.setBackground(getDrawable(R.drawable.card_admin));
        bottom.setPadding(0, 12, 0, 12);
        root.addView(bottom);

        addNavItem(bottom, "🏠 Beranda", true);
        addNavItem(bottom, "🎛 Kontrol", false);
        addNavItem(bottom, "👤 Profil", false);

        // Dummy cards
        addDeviceCard("REALME RMX3939", "Android 15", "192.168.1.5", "22%", "ID", "Online");
        addDeviceCard("REDMI 23053RN02A", "Android 15", "10.0.0.12", "18%", "ID", "Offline");
    }

    private void addFilterChip(LinearLayout parent, String text, boolean active) {
        TextView chip = new TextView(this);
        chip.setText(text);
        chip.setTextColor(0xFFFFFFFF);
        chip.setBackground(active ? getDrawable(R.drawable.btn_admin) : getDrawable(R.drawable.btn_outline_admin));
        chip.setPadding(16, 8, 16, 8);
        chip.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 8, 0);
        chip.setLayoutParams(lp);
        parent.addView(chip);
    }

    private void addNavItem(LinearLayout parent, String text, boolean active) {
        TextView item = new TextView(this);
        item.setText(text);
        item.setTextColor(active ? 0xFFFF1744 : 0xFF9AA3B2);
        item.setTextSize(11);
        item.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        item.setLayoutParams(lp);
        parent.addView(item);
    }

    private void addDeviceCard(String model, String android, String ip, String battery, String region, String status) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(getDrawable(R.drawable.card_admin));
        card.setPadding(20, 20, 20, 20);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 12);
        card.setLayoutParams(lp);

        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView modelView = new TextView(this);
        modelView.setText("📱 " + model);
        modelView.setTextColor(0xFFFFFFFF);
        modelView.setTextSize(16);
        modelView.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams modelParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        modelView.setLayoutParams(modelParams);

        TextView badge = new TextView(this);
        badge.setText(status);
        badge.setTextColor(status.equals("Online") ? 0xFF00E676 : 0xFF9AA3B2);
        badge.setTextSize(11);
        badge.setBackgroundColor(0x22000000);
        badge.setPadding(8, 4, 8, 4);

        topRow.addView(modelView);
        topRow.addView(badge);
        card.addView(topRow);

        TextView info = new TextView(this);
        info.setText("Android " + android + " | IP: " + ip + "\nBat: " + battery + " | " + region);
        info.setTextColor(0xFF9AA3B2);
        info.setTextSize(12);
        card.addView(info);

        card.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, ControlActivity.class);
            i.putExtra("deviceId", model);
            i.putExtra("deviceModel", model);
            i.putExtra("battery", battery);
            startActivity(i);
        });

        deviceContainer.addView(card);
    }
}
