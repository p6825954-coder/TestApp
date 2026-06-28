package com.testapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import io.socket.client.IO;
import io.socket.client.Socket;
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

        // Inisialisasi socket dengan try-catch
        try {
            socket = IO.socket("https://ghostspy.bruang.biz.id");
        } catch (URISyntaxException e) {
            statusText.setText("❌ URL Error");
            return;
        }

        // Event: terhubung
        socket.on(Socket.EVENT_CONNECT, args -> {
            runOnUiThread(() -> statusText.setText("🟢 ONLINE"));
        });

        // Event: gagal konek
        socket.on(Socket.EVENT_CONNECT_ERROR, args -> {
            runOnUiThread(() -> statusText.setText("🔴 OFFLINE"));
        });

        socket.connect();

        // Tampilkan 2 device dummy dulu
        addDeviceCard("REALME RMX3939 ANDROID 15", "192.168.1.5", true);
        addDeviceCard("REDMI 23053RN02A ANDROID 15", "10.0.0.12", false);
    }

    private void addDeviceCard(String model, String ip, boolean online) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(0xFF1A1A2E);
        card.setPadding(20, 20, 20, 20);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 12);
        card.setLayoutParams(lp);

        TextView modelView = new TextView(this);
        modelView.setText("📱 " + model);
        modelView.setTextColor(0xFFFFFFFF);
        modelView.setTextSize(16);

        TextView ipView = new TextView(this);
        ipView.setText("IP: " + ip + (online ? " 🟢" : " 🔴"));
        ipView.setTextColor(0xFF9AA3B2);
        ipView.setTextSize(12);

        card.addView(modelView);
        card.addView(ipView);

        card.setOnClickListener(v -> {
            Intent i = new Intent(this, ControlActivity.class);
            i.putExtra("deviceId", model);
            startActivity(i);
        });

        deviceContainer.addView(card);
    }

    @Override
    protected void onDestroy() {
        if (socket != null) socket.disconnect();
        super.onDestroy();
    }
}
