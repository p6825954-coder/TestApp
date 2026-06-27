package com.testapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import io.socket.client.IO;
import io.socket.client.Socket;
import org.json.JSONArray;
import org.json.JSONObject;
import java.net.URISyntaxException;

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
            statusText.setText("❌ Error: " + e.getMessage());
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
                runOnUiThread(() -> {
                    deviceContainer.removeAllViews();
                    for (int i = 0; i < devices.length(); i++) {
                        try {
                            JSONObject dev = devices.getJSONObject(i);
                            // Buat item
                            LinearLayout item = new LinearLayout(MainActivity.this);
                            item.setOrientation(LinearLayout.VERTICAL);
                            item.setBackgroundColor(0xFF1a1a1a);
                            item.setPadding(24, 24, 24, 24);
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            );
                            params.setMargins(0, 0, 0, 16);
                            item.setLayoutParams(params);

                            TextView idView = new TextView(MainActivity.this);
                            idView.setText("🆔 " + dev.getString("id"));
                            idView.setTextColor(0xFF00ff41);
                            idView.setTextSize(16);

                            TextView modelView = new TextView(MainActivity.this);
                            modelView.setText("📱 " + dev.getString("model") + " | Android " + dev.optString("android", "??"));
                            modelView.setTextColor(0xFFFFFFFF);
                            modelView.setTextSize(14);

                            TextView lastView = new TextView(MainActivity.this);
                            lastView.setText("🕒 " + dev.optString("last_seen", "unknown"));
                            lastView.setTextColor(0xFFAAAAAA);
                            lastView.setTextSize(12);

                            item.addView(idView);
                            item.addView(modelView);
                            item.addView(lastView);
                            deviceContainer.addView(item);
                        } catch (Exception e) {}
                    }
                    statusText.setText("🟢 ONLINE | " + devices.length() + " perangkat");
                });
            } catch (Exception e) {
                runOnUiThread(() -> statusText.setText("⚠️ Error parsing data"));
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
