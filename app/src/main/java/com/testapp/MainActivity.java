package com.testapp;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import io.socket.client.IO;
import io.socket.client.Socket;
import org.json.JSONArray;
import org.json.JSONObject;
import java.net.URISyntaxException;

public class MainActivity extends Activity {
    private Socket socket;
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.statusText);
        statusText.setText("Menghubungkan...");

        try {
            socket = IO.socket("https://ghostspy.bruang.biz.id");
        } catch (URISyntaxException e) {
            statusText.setText("Error URL: " + e.getMessage());
            return;
        }

        socket.on(Socket.EVENT_CONNECT, args -> {
            runOnUiThread(() -> statusText.setText("Terhubung! Meminta data..."));
            socket.emit("get_devices"); // Minta daftar perangkat
        });

        socket.on("devices_list", args -> {
            try {
                JSONArray devices = (JSONArray) args[0];
                StringBuilder sb = new StringBuilder();
                sb.append("📱 Perangkat Online:\n");
                for (int i = 0; i < devices.length(); i++) {
                    JSONObject dev = devices.getJSONObject(i);
                    sb.append("• ").append(dev.getString("id"))
                      .append(" - ").append(dev.getString("model"))
                      .append(" (").append(dev.optString("android", "??")).append(")\n");
                }
                runOnUiThread(() -> statusText.setText(sb.toString()));
            } catch (Exception e) {
                runOnUiThread(() -> statusText.setText("Error parsing data"));
            }
        });

        socket.on(Socket.EVENT_CONNECT_ERROR, args -> {
            runOnUiThread(() -> statusText.setText("Gagal konek"));
        });

        socket.connect();
    }

    @Override
    protected void onDestroy() {
        if (socket != null) socket.disconnect();
        super.onDestroy();
    }
}
