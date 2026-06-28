package com.testapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class AccountManageActivity extends Activity {
    private LinearLayout container;
    private static final String SERVER = "https://ghostspy.bruang.biz.id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScrollView scroll = new ScrollView(this);
        container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(24, 40, 24, 40);
        container.setBackgroundColor(0x00000000);
        scroll.addView(container);
        scroll.setBackground(getDrawable(R.drawable.bg_admin));
        setContentView(scroll);

        TextView title = new TextView(this);
        title.setText("👥 Manajemen Akun");
        title.setTextColor(0xFFFFFFFF);
        title.setTextSize(22);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setPadding(0, 0, 0, 24);
        container.addView(title);

        Button createBtn = new Button(this);
        createBtn.setText("+ Buat Akun Baru");
        createBtn.setTextColor(0xFFFFFFFF);
        createBtn.setBackground(getDrawable(R.drawable.btn_admin));
        createBtn.setOnClickListener(v -> showCreateDialog());
        container.addView(createBtn);

        TextView spacer = new TextView(this);
        spacer.setHeight(24);
        container.addView(spacer);

        // Ambil daftar akun dari server (dummy dulu)
        loadAccounts();
    }

    private void loadAccounts() {
        // Dummy data sementara
        List<String[]> dummy = new ArrayList<>();
        dummy.add(new String[]{"user1@test.com", "Device123", "Online"});
        dummy.add(new String[]{"user2@test.com", "Device456", "Offline"});
        displayAccounts(dummy);
    }

    private void displayAccounts(List<String[]> accounts) {
        for (String[] acc : accounts) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackground(getDrawable(R.drawable.card_admin));
            card.setPadding(20, 20, 20, 20);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, 12);
            card.setLayoutParams(lp);

            TextView emailView = new TextView(this);
            emailView.setText("📧 " + acc[0]);
            emailView.setTextColor(0xFFFFFFFF);
            emailView.setTextSize(15);

            TextView deviceView = new TextView(this);
            deviceView.setText("📱 Device: " + acc[1]);
            deviceView.setTextColor(0xFF9AA3B2);

            TextView statusView = new TextView(this);
            statusView.setText("Status: " + acc[2]);
            statusView.setTextColor(acc[2].equals("Online") ? 0xFF00E676 : 0xFF9AA3B2);

            card.addView(emailView);
            card.addView(deviceView);
            card.addView(statusView);

            container.addView(card);
        }
    }

    private void showCreateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Buat Akun Baru");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 16, 32, 16);

        EditText emailInput = new EditText(this);
        emailInput.setHint("Email");
        emailInput.setTextColor(0xFFFFFFFF);
        emailInput.setBackground(getDrawable(R.drawable.card_admin));

        EditText passwordInput = new EditText(this);
        passwordInput.setHint("Password");
        passwordInput.setTextColor(0xFFFFFFFF);
        passwordInput.setBackground(getDrawable(R.drawable.card_admin));

        layout.addView(emailInput);
        layout.addView(passwordInput);

        builder.setView(layout);
        builder.setPositiveButton("Buat", (d, w) -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            if (!email.isEmpty() && !password.isEmpty()) {
                Toast.makeText(this, "Akun " + email + " dibuat!", Toast.LENGTH_SHORT).show();
                // Nanti sambungkan ke API
            }
        });
        builder.setNegativeButton("Batal", null);
        builder.show();
    }
}
