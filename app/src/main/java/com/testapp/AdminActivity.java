package com.testapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.*;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class AdminActivity extends Activity {
    private LinearLayout container;
    private static final String SERVER = "https://ghostspy.bruang.biz.id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScrollView scroll = new ScrollView(this);
        container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(24, 32, 24, 32);
        container.setBackgroundColor(0xFF080808);
        scroll.addView(container);
        setContentView(scroll);

        addTitle("🔑 Admin Dashboard");
        addSubtitle("Selamat datang, Administrator");

        // Statistik
        addCard("Total Akun", "0", "#FF1744");
        addCard("Perangkat Online", "0", "#00E676");
        addCard("Perangkat Offline", "0", "#9AA3B2");

        // Tombol Manajemen Akun
        addButton("👥 Manajemen Akun", () -> startActivity(new Intent(this, AccountManageActivity.class)));
        // Tombol Dashboard Perangkat
        addButton("📱 Dashboard Perangkat", () -> startActivity(new Intent(this, MainActivity.class)));

        // Ambil data dari server (dummy dulu)
        fetchStats();
    }

    private void addTitle(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(0xFFFFFFFF);
        tv.setTextSize(22);
        tv.setTypeface(null, android.graphics.Typeface.BOLD);
        tv.setPadding(0, 0, 0, 12);
        container.addView(tv);
    }

    private void addSubtitle(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(0xFF9AA3B2);
        tv.setTextSize(14);
        tv.setPadding(0, 0, 0, 24);
        container.addView(tv);
    }

    private void addCard(String title, String value, String color) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(0xFF151515);
        card.setPadding(20, 20, 20, 20);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 12);
        card.setLayoutParams(lp);

        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextColor(0xFF9AA3B2);
        titleView.setTextSize(13);

        TextView valueView = new TextView(this);
        valueView.setText(value);
        valueView.setTextColor(android.graphics.Color.parseColor(color));
        valueView.setTextSize(28);
        valueView.setTypeface(null, android.graphics.Typeface.BOLD);

        card.addView(titleView);
        card.addView(valueView);
        container.addView(card);
    }

    private void addButton(String text, Runnable action) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setTextColor(0xFFFFFFFF);
        btn.setBackgroundColor(0xFF151515);
        btn.setOnClickListener(v -> action.run());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 12);
        btn.setLayoutParams(lp);
        container.addView(btn);
    }

    private void fetchStats() {
        new Thread(() -> {
            try {
                URL url = new URL(SERVER + "/api/devices");
                BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                JSONObject stats = new JSONObject(sb.toString());
                // update UI (simplified)
            } catch (Exception e) {}
        }).start();
    }
}
