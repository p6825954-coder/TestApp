package com.testapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ContactsActivity extends Activity {
    private LinearLayout contactList;
    private EditText searchInput;

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
        header.setBackgroundColor(0xAA171717);
        header.setPadding(16, 12, 16, 12);

        Button backBtn = new Button(this);
        backBtn.setText("←");
        backBtn.setTextColor(0xFFFFFFFF);
        backBtn.setBackgroundColor(0x00000000);
        backBtn.setOnClickListener(v -> finish());
        header.addView(backBtn);

        TextView title = new TextView(this);
        title.setText("👥 Kontak");
        title.setTextColor(0xFFFFFFFF);
        title.setTextSize(20);
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

        // Search Bar
        searchInput = new EditText(this);
        searchInput.setHint("🔍 Cari kontak...");
        searchInput.setTextColor(0xFFFFFFFF);
        searchInput.setBackgroundColor(0xFF171717);
        searchInput.setPadding(16, 12, 16, 12);
        LinearLayout.LayoutParams searchParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        searchParams.setMargins(16, 12, 16, 8);
        searchInput.setLayoutParams(searchParams);
        searchInput.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(android.text.Editable s) {
                filterContacts(s.toString());
            }
        });
        root.addView(searchInput);

        // Statistik
        LinearLayout statsBar = new LinearLayout(this);
        statsBar.setOrientation(LinearLayout.HORIZONTAL);
        statsBar.setPadding(16, 4, 16, 12);

        statsBar.addView(createStatCard("Total", "12"));
        statsBar.addView(createStatCard("Favorit", "3"));
        statsBar.addView(createStatCard("Baru", "1"));
        statsBar.addView(createStatCard("Grup", "2"));
        root.addView(statsBar);

        // Daftar Kontak
        ScrollView scroll = new ScrollView(this);
        contactList = new LinearLayout(this);
        contactList.setOrientation(LinearLayout.VERTICAL);
        contactList.setPadding(16, 0, 16, 16);
        scroll.addView(contactList);
        root.addView(scroll);

        // Data dummy
        String[][] dummyContacts = {
            {"Alice", "08123456789", "Mobile"},
            {"Bob", "08123456780", "Home"},
            {"Charlie", "08123456781", "Work"},
            {"Diana", "08123456782", "Mobile"},
            {"Evan", "08123456783", "Work"},
            {"Fay", "08123456784", "Mobile"},
            {"George", "08123456785", "Home"},
            {"Hannah", "08123456786", "Work"},
            {"Ivan", "08123456787", "Mobile"},
            {"Judy", "08123456788", "Home"},
            {"Kevin", "08123456789", "Work"},
            {"Laura", "08123456790", "Mobile"}
        };

        for (String[] c : dummyContacts) {
            contactList.addView(createContactCard(c[0], c[1], c[2]));
        }

        // Indeks Huruf (A-Z) – kita letakkan di sisi kanan (dummy, tidak fungsional penuh)
        // Untuk sederhana, kita skip.
    }

    private LinearLayout createStatCard(String label, String value) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setBackgroundColor(0xFF171717);
        card.setPadding(12, 8, 12, 8);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        params.setMargins(4, 0, 4, 0);
        card.setLayoutParams(params);

        TextView valueView = new TextView(this);
        valueView.setText(value);
        valueView.setTextColor(0xFFFFFFFF);
        valueView.setTextSize(18);
        valueView.setTypeface(null, android.graphics.Typeface.BOLD);
        valueView.setGravity(Gravity.CENTER);

        TextView labelView = new TextView(this);
        labelView.setText(label);
        labelView.setTextColor(0xFF9AA3B2);
        labelView.setTextSize(11);
        labelView.setGravity(Gravity.CENTER);

        card.addView(valueView);
        card.addView(labelView);
        return card;
    }

    private LinearLayout createContactCard(String name, String phone, String type) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setBackground(getDrawable(R.drawable.card_admin));
        card.setPadding(16, 16, 16, 16);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 8);
        card.setLayoutParams(lp);

        // Avatar
        TextView avatar = new TextView(this);
        avatar.setText(name.substring(0, 1).toUpperCase());
        avatar.setTextColor(0xFFFFFFFF);
        avatar.setBackgroundColor(0xFF00E676);
        avatar.setGravity(Gravity.CENTER);
        avatar.setWidth(48);
        avatar.setHeight(48);
        card.addView(avatar);

        // Info
        LinearLayout infoCol = new LinearLayout(this);
        infoCol.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        infoParams.setMargins(12, 0, 12, 0);
        infoCol.setLayoutParams(infoParams);

        TextView nameView = new TextView(this);
        nameView.setText(name);
        nameView.setTextColor(0xFFFFFFFF);
        nameView.setTextSize(16);
        nameView.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView phoneView = new TextView(this);
        phoneView.setText(phone + " • " + type);
        phoneView.setTextColor(0xFF9AA3B2);
        phoneView.setTextSize(12);

        infoCol.addView(nameView);
        infoCol.addView(phoneView);
        card.addView(infoCol);

        // Tombol Favorit & Menu
        Button favBtn = new Button(this);
        favBtn.setText("⭐");
        favBtn.setTextColor(0xFFFFFFFF);
        favBtn.setBackgroundColor(0x00000000);
        card.addView(favBtn);

        Button menuBtn = new Button(this);
        menuBtn.setText("⋮");
        menuBtn.setTextColor(0xFFFFFFFF);
        menuBtn.setBackgroundColor(0x00000000);
        menuBtn.setOnClickListener(v -> showContactDetail(name, phone, type));
        card.addView(menuBtn);

        return card;
    }

    private void filterContacts(String query) {
        for (int i = 0; i < contactList.getChildCount(); i++) {
            LinearLayout card = (LinearLayout) contactList.getChildAt(i);
            LinearLayout infoCol = (LinearLayout) card.getChildAt(1);
            TextView nameView = (TextView) infoCol.getChildAt(0);
            String name = nameView.getText().toString();
            if (query.isEmpty() || name.toLowerCase().contains(query.toLowerCase())) {
                card.setVisibility(android.view.View.VISIBLE);
            } else {
                card.setVisibility(android.view.View.GONE);
            }
        }
    }

    private void showContactDetail(String name, String phone, String type) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Detail Kontak");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(24, 16, 24, 16);
        layout.setBackgroundColor(0xFF181818);

        TextView nameView = new TextView(this);
        nameView.setText(name);
        nameView.setTextColor(0xFFFFFFFF);
        nameView.setTextSize(20);
        nameView.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView infoView = new TextView(this);
        infoView.setText("📞 " + phone + "\n📁 " + type);
        infoView.setTextColor(0xFF9AA3B2);
        infoView.setTextSize(14);

        layout.addView(nameView);
        layout.addView(infoView);

        builder.setView(layout);
        builder.setPositiveButton("Tutup", null);
        builder.show();
    }
}
