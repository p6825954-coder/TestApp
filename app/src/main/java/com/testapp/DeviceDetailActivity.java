package com.testapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.*;
import java.util.Locale;

public class DeviceDetailActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String deviceId = getIntent().getStringExtra("deviceId");
        String model = getIntent().getStringExtra("deviceModel");
        String battery = getIntent().getStringExtra("battery");
        String network = getIntent().getStringExtra("network");
        String androidVer = getIntent().getStringExtra("android");
        String ip = getIntent().getStringExtra("ip");
        String region = getIntent().getStringExtra("region");

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
        title.setText("Detail Perangkat");
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
        header.addView(refreshBtn);

        root.addView(header);

        // ScrollView konten
        ScrollView scroll = new ScrollView(this);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(16, 16, 16, 16);

        // Card Info Perangkat
        content.addView(createCard("📱 Informasi Perangkat",
            "Nama: " + model + "\n" +
            "Model: " + model + "\n" +
            "Merek: " + getBrand(model) + "\n" +
            "Android: " + androidVer + "\n" +
            "IP: " + ip + "\n" +
            "Region: " + region));

        // Card Baterai & Jaringan
        content.addView(createCard("🔋 Baterai & Jaringan",
            "Level: " + battery + "%\n" +
            "Jaringan: " + network + "\n" +
            "Status: Online"));

        // Card Penyimpanan (dummy)
        content.addView(createCard("💾 Penyimpanan",
            "RAM: 8 GB\n" +
            "Internal: 128 GB\n" +
            "Tersedia: 45 GB"));

        // Tombol Kontrol
        Button controlBtn = new Button(this);
        controlBtn.setText("🎛️ Buka Kontrol Perangkat");
        controlBtn.setTextColor(0xFFFFFFFF);
        controlBtn.setBackground(getDrawable(R.drawable.btn_admin));
        controlBtn.setOnClickListener(v -> {
            Intent i = new Intent(this, ControlActivity.class);
            i.putExtra("deviceId", deviceId);
            i.putExtra("deviceModel", model);
            i.putExtra("battery", battery);
            i.putExtra("network", network);
            startActivity(i);
        });
        content.addView(controlBtn);

        scroll.addView(content);
        root.addView(scroll);

        // Bottom Nav
        LinearLayout bottomNav = new LinearLayout(this);
        bottomNav.setOrientation(LinearLayout.HORIZONTAL);
        bottomNav.setGravity(Gravity.CENTER);
        bottomNav.setBackgroundColor(0xFF171717);
        bottomNav.setPadding(0, 8, 0, 8);

        String[] navItems = {"🏠 Dashboard", "📱 Devices", "🗺️ Maps", "📊 Activity", "⚙️ Settings"};
        for (String item : navItems) {
            TextView navText = new TextView(this);
            navText.setText(item);
            navText.setTextColor(0xFF9AA3B2);
            navText.setTextSize(10);
            navText.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams navParams = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            navText.setLayoutParams(navParams);
            bottomNav.addView(navText);
        }
        root.addView(bottomNav);
    }

    private LinearLayout createCard(String title, String content) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(getDrawable(R.drawable.card_admin));
        card.setPadding(20, 16, 20, 16);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 12);
        card.setLayoutParams(params);

        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextColor(0xFFFFFFFF);
        titleView.setTextSize(16);
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);
        titleView.setPadding(0, 0, 0, 8);
        card.addView(titleView);

        TextView contentView = new TextView(this);
        contentView.setText(content);
        contentView.setTextColor(0xFF9AA3B2);
        contentView.setTextSize(13);
        contentView.setLineSpacing(4, 1);
        card.addView(contentView);

        return card;
    }

    private String getBrand(String model) {
        if (model.toLowerCase().contains("realme")) return "Realme";
        if (model.toLowerCase().contains("redmi")) return "Xiaomi";
        if (model.toLowerCase().contains("samsung")) return "Samsung";
        return "Unknown";
    }
}
