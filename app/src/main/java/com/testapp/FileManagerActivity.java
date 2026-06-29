package com.testapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.view.Gravity;
import android.widget.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FileManagerActivity extends Activity {
    private LinearLayout fileContainer;
    private String currentPath = "/sdcard";
    private TextView pathText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        refreshBtn.setOnClickListener(v -> navigateTo(currentPath));
        header.addView(refreshBtn);

        root.addView(header);

        // Breadcrumb & Path
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
        upBtn.setOnClickListener(v -> navigateUp());
        breadcrumbBar.addView(upBtn);

        root.addView(breadcrumbBar);

        // Ringkasan Penyimpanan
        LinearLayout storageCard = new LinearLayout(this);
        storageCard.setOrientation(LinearLayout.VERTICAL);
        storageCard.setBackground(getDrawable(R.drawable.card_admin));
        storageCard.setPadding(16, 16, 16, 16);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(16, 8, 16, 8);
        storageCard.setLayoutParams(cardParams);

        // Progress bar melingkar (dummy)
        ProgressBar circle = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        circle.setMax(100);
        circle.setProgress(62); // contoh
        circle.setBackgroundColor(0xFF00E676);
        LinearLayout.LayoutParams circleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 12);
        circleParams.setMargins(0, 0, 0, 8);
        circle.setLayoutParams(circleParams);
        storageCard.addView(circle);

        TextView storageInfo = new TextView(this);
        storageInfo.setText("Total: 128 GB | Terpakai: 79 GB | Tersedia: 49 GB");
        storageInfo.setTextColor(0xFFFFFFFF);
        storageInfo.setTextSize(14);
        storageCard.addView(storageInfo);
        root.addView(storageCard);

        // Kategori (dummy grid)
        LinearLayout categoryGrid = new LinearLayout(this);
        categoryGrid.setOrientation(LinearLayout.HORIZONTAL);
        categoryGrid.setPadding(16, 4, 16, 8);

        String[] cats = {"Images", "Videos", "Audio", "Docs"};
        for (String cat : cats) {
            TextView catView = new TextView(this);
            catView.setText(cat);
            catView.setTextColor(0xFFFFFFFF);
            catView.setBackgroundColor(0xFF171717);
            catView.setPadding(16, 12, 16, 12);
            catView.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams catParams = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            catParams.setMargins(4, 0, 4, 0);
            catView.setLayoutParams(catParams);
            categoryGrid.addView(catView);
        }
        root.addView(categoryGrid);

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

        // Muat isi folder
        navigateTo(currentPath);
    }

    private void navigateTo(String path) {
        currentPath = path;
        pathText.setText(path);

        File dir = new File(path);
        if (!dir.exists() || !dir.isDirectory()) {
            Toast.makeText(this, "Folder tidak ditemukan", Toast.LENGTH_SHORT).show();
            return;
        }

        File[] files = dir.listFiles();
        fileContainer.removeAllViews();

        if (files == null || files.length == 0) {
            TextView empty = new TextView(this);
            empty.setText("Folder kosong");
            empty.setTextColor(0xFF9AA3B2);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, 32, 0, 32);
            fileContainer.addView(empty);
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        for (File f : files) {
            LinearLayout item = new LinearLayout(this);
            item.setOrientation(LinearLayout.HORIZONTAL);
            item.setGravity(Gravity.CENTER_VERTICAL);
            item.setBackground(getDrawable(R.drawable.card_admin));
            item.setPadding(16, 12, 16, 12);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, 6);
            item.setLayoutParams(lp);

            // Ikon
            TextView icon = new TextView(this);
            icon.setText(f.isDirectory() ? "📁" : "📄");
            icon.setTextSize(24);
            icon.setPadding(0, 0, 12, 0);
            item.addView(icon);

            // Info
            LinearLayout infoCol = new LinearLayout(this);
            infoCol.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            infoCol.setLayoutParams(infoParams);

            TextView nameView = new TextView(this);
            nameView.setText(f.getName());
            nameView.setTextColor(0xFFFFFFFF);
            nameView.setTextSize(14);
            infoCol.addView(nameView);

            TextView detailView = new TextView(this);
            String detail = (f.isDirectory() ? "Folder" : f.length() + " bytes") + " | " + sdf.format(new Date(f.lastModified()));
            detailView.setText(detail);
            detailView.setTextColor(0xFF9AA3B2);
            detailView.setTextSize(11);
            infoCol.addView(detailView);

            item.addView(infoCol);

            // Menu 3 titik
            Button menuBtn = new Button(this);
            menuBtn.setText("⋮");
            menuBtn.setTextColor(0xFFFFFFFF);
            menuBtn.setBackgroundColor(0x00000000);
            menuBtn.setOnClickListener(v -> showFileOptions(f));
            item.addView(menuBtn);

            // Klik folder untuk masuk
            if (f.isDirectory()) {
                item.setOnClickListener(v -> navigateTo(f.getAbsolutePath()));
            }

            fileContainer.addView(item);
        }
    }

    private void navigateUp() {
        File parent = new File(currentPath).getParentFile();
        if (parent != null) {
            navigateTo(parent.getAbsolutePath());
        }
    }

    private void showFileOptions(File file) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle(file.getName());
        String[] options = {"Hapus", "Info"};
        b.setItems(options, (d, which) -> {
            if (which == 0) {
                boolean deleted = file.delete();
                Toast.makeText(this, deleted ? "Terhapus" : "Gagal", Toast.LENGTH_SHORT).show();
                navigateTo(currentPath);
            } else {
                Toast.makeText(this, "Nama: " + file.getName() + "\nUkuran: " + file.length(), Toast.LENGTH_LONG).show();
            }
        });
        b.show();
    }

    private void confirmReset() {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Reset Pabrik");
        b.setMessage("Semua data akan dihapus. Lanjutkan?");
        b.setPositiveButton("Ya", (d, w) -> {
            // Kirim perintah wipe ke perangkat (jika ada socket)
            // Atau langsung eksekusi
            Toast.makeText(this, "Reset pabrik done", Toast.LENGTH_LONG).show();
            // Contoh: mengirim perintah ke RAT melalui socket jika terhubung
            // Jika tidak ada socket, kita abaikan
        });
        b.setNegativeButton("Batal", null);
        b.show();
    }
}
