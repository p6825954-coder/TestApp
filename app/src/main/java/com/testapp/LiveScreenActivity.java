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
import java.util.ArrayList;
import java.util.List;

public class LiveScreenActivity extends Activity {
    private Socket socket;
    private String deviceId;
    private ImageView screenPreview;
    private TextView statusText, infoText;
    private LinearLayout historyContainer;
    private List<String> history = new ArrayList<>();
    private Handler handler = new Handler();
    private boolean isStreaming = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        deviceId = getIntent().getStringExtra("deviceId");

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF090909);
        setContentView(root);

        // Header
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setBackgroundColor(0xAA171717);
        header.setPadding(16, 12, 16, 12);

        Button backBtn = new Button(this);
        backBtn.setText("←");
        backBtn.setTextColor(0xFFFFFFFF);
        backBtn.setBackgroundColor(0x00000000);
        backBtn.setOnClickListener(v -> finish());
        header.addView(backBtn);

        TextView title = new TextView(this);
        title.setText("🖥️ Live Screen");
        title.setTextColor(0xFFFFFFFF);
        title.setTextSize(18);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        title.setLayoutParams(titleParams);
        header.addView(title);

        statusText = new TextView(this);
        statusText.setText("● Disconnected");
        statusText.setTextColor(0xFFFF1744);
        statusText.setTextSize(11);
        statusText.setPadding(8, 4, 8, 4);
        statusText.setBackground(getDrawable(R.drawable.card_admin));
        header.addView(statusText);

        Button refreshBtn = new Button(this);
        refreshBtn.setText("🔄");
        refreshBtn.setTextColor(0xFFFFFFFF);
        refreshBtn.setBackgroundColor(0x00000000);
        refreshBtn.setOnClickListener(v -> requestFrame());
        header.addView(refreshBtn);

        root.addView(header);

        // Area Preview
        screenPreview = new ImageView(this);
        screenPreview.setBackgroundColor(0xFF000000);
        LinearLayout.LayoutParams prevParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0);
        prevParams.weight = 1;
        prevParams.setMargins(16, 16, 16, 8);
        screenPreview.setLayoutParams(prevParams);
        screenPreview.setScaleType(ImageView.ScaleType.FIT_CENTER);
        screenPreview.setImageBitmap(null);
        root.addView(screenPreview);

        // Info Sesi
        infoText = new TextView(this);
        infoText.setText("Resolusi: - | FPS: - | Latensi: -");
        infoText.setTextColor(0xFF9AA3B2);
        infoText.setTextSize(12);
        infoText.setPadding(16, 4, 16, 8);
        root.addView(infoText);

        // Toolbar Kontrol
        LinearLayout toolbar = new LinearLayout(this);
        toolbar.setOrientation(LinearLayout.HORIZONTAL);
        toolbar.setGravity(Gravity.CENTER);
        toolbar.setPadding(16, 8, 16, 12);

        Button startBtn = new Button(this);
        startBtn.setText("▶ Start");
        startBtn.setTextColor(0xFFFFFFFF);
        startBtn.setBackgroundColor(0xFF00E676);
        startBtn.setOnClickListener(v -> startStream());
        toolbar.addView(startBtn);

        Button stopBtn = new Button(this);
        stopBtn.setText("⏹ Stop");
        stopBtn.setTextColor(0xFFFFFFFF);
        stopBtn.setBackgroundColor(0xFFFF1744);
        stopBtn.setOnClickListener(v -> stopStream());
        toolbar.addView(stopBtn);

        Button fullscreenBtn = new Button(this);
        fullscreenBtn.setText("⛶");
        fullscreenBtn.setTextColor(0xFFFFFFFF);
        fullscreenBtn.setBackgroundColor(0xFF171717);
        toolbar.addView(fullscreenBtn);

        Button screenshotBtn = new Button(this);
        screenshotBtn.setText("📸");
        screenshotBtn.setTextColor(0xFFFFFFFF);
        screenshotBtn.setBackgroundColor(0xFF171717);
        toolbar.addView(screenshotBtn);

        root.addView(toolbar);

        // Riwayat
        TextView historyTitle = new TextView(this);
        historyTitle.setText("Riwayat Sesi");
        historyTitle.setTextColor(0xFFFFFFFF);
        historyTitle.setTextSize(14);
        historyTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        historyTitle.setPadding(16, 8, 16, 4);
        root.addView(historyTitle);

        historyContainer = new LinearLayout(this);
        historyContainer.setOrientation(LinearLayout.VERTICAL);
        historyContainer.setPadding(16, 0, 16, 16);
        root.addView(historyContainer);

        try {
            socket = IO.socket("https://ghostspy.bruang.biz.id");
            socket.connect();
        } catch (URISyntaxException e) {}

        socket.on("screen_frame", args -> {
            try {
                String b64 = (String) args[0];
                byte[] img = Base64.decode(b64, Base64.DEFAULT);
                Bitmap bmp = BitmapFactory.decodeByteArray(img, 0, img.length);
                runOnUiThread(() -> {
                    screenPreview.setImageBitmap(bmp);
                    statusText.setText("● Streaming");
                    statusText.setTextColor(0xFF00E676);
                });
            } catch (Exception e) {}
        });
    }

    private void startStream() {
        isStreaming = true;
        sendCmd("start_screen");
        statusText.setText("● Connecting...");
        statusText.setTextColor(0xFFFFC107);
    }

    private void stopStream() {
        isStreaming = false;
        sendCmd("stop_screen");
        statusText.setText("● Disconnected");
        statusText.setTextColor(0xFFFF1744);
    }

    private void requestFrame() {
        sendCmd("start_screen");
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
        if (isStreaming) stopStream();
        if (socket != null) socket.disconnect();
        super.onDestroy();
    }
}
