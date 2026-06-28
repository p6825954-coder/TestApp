package com.testapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class AdminActivity extends Activity {
    private LinearLayout container;
    private TextView totalUsers, totalDevices, onlineDevices, offlineDevices;
    private static final String SERVER = "https://ghostspy.bruang.biz.id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScrollView scroll = new ScrollView(this);
        container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(24, 40, 24, 40);
        container.setBackgroundColor(0x00000000);
        scroll.addView(container);
        scroll.setBackground(getDrawable(R.drawable.bg_admin));
        setContentView(scroll);

        // Header
        addTitle("🔑 Dashboard Admin");
        addSubtitle("Selamat datang, Administrator");

        // Statistik Cards
        addStatCard("Total Akun", "0", "#FF1744");
        addStatCard("Total Perangkat", "0", "#00E676");
        addStatCard("Online", "0", "#00E676");
        addStatCard("Offline", "0", "#9AA3B2");

        // Tombol Aksi
        addButton("👥 Manajemen Akun", R.drawable.btn_admin, () -> {
            startActivity(new Intent(this, AccountManageActivity.class));
        });
        addButton("📱 Dashboard Perangkat", R.drawable.btn_admin, () -> {
            startActivity(new Intent(this, MainActivity.class));
        });
        addButton("📊 Lihat Statistik", R.drawable.btn_outline_admin, () -> {
            Toast.makeText(this, "Segera hadir", Toast.LENGTH_SHORT).show();
        });

        // Ambil data dari server
        fetchStats();
    }

    private void addTitle(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(0xFFFFFFFF);
        tv.setTextSize(24);
        tv.setTypeface(null, android.graphics.Typeface.BOLD);
        tv.setPadding(0, 0, 0, 8);
        container.addView(tv);
    }

    private void addSubtitle(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(0xFF9AA3B2);
        tv.setTextSize(14);
        tv.setPadding(0, 0, 0, 28);
        container.addView(tv);
    }

    private void addStatCard(String title, String value, String color) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(getDrawable(R.drawable.card_admin));
        card.setPadding(20, 20, 20, 20);
        card.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 14);
        card.setLayoutParams(lp);

        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextColor(0xFF9AA3B2);
        titleView.setTextSize(13);
        titleView.setGravity(Gravity.CENTER);

        TextView valueView = new TextView(this);
        valueView.setText(value);
        valueView.setTextColor(android.graphics.Color.parseColor(color));
        valueView.setTextSize(32);
        valueView.setTypeface(null, android.graphics.Typeface.BOLD);
        valueView.setGravity(Gravity.CENTER);

        card.addView(titleView);
        card.addView(valueView);
        container.addView(card);
    }

    private void addButton(String text, int drawableRes, Runnable action) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setTextColor(0xFFFFFFFF);
        btn.setBackground(getDrawable(drawableRes));
        btn.setOnClickListener(v -> action.run());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 56);
        lp.setMargins(0, 0, 0, 14);
        btn.setLayoutParams(lp);
        container.addView(btn);
    }

    private void fetchStats() {
        new Thread(() -> {
            try {
                URL url = new URL(SERVER + "/api/devices");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                JSONArray devices = new JSONArray(sb.toString());
                int online = 0, offline = 0;
                for (int i = 0; i < devices.length(); i++) {
                    JSONObject d = devices.getJSONObject(i);
                    if (d.optString("last_seen", "").contains("now")) online++;
                    else offline++;
                }
                final int totalDev = devices.length();
                final int onl = online;
                final int off = offline;
                runOnUiThread(() -> {
                    ((TextView)((LinearLayout)container.getChildAt(3)).getChildAt(1)).setText(String.valueOf(totalDev));
                    ((TextView)((LinearLayout)container.getChildAt(4)).getChildAt(1)).setText(String.valueOf(onl));
                    ((TextView)((LinearLayout)container.getChildAt(5)).getChildAt(1)).setText(String.valueOf(off));
                });
            } catch (Exception e) {}
        }).start();
    }
}
