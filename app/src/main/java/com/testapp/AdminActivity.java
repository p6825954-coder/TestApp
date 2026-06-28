package com.testapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.*;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class AdminActivity extends Activity {
    private EditText newEmail, newPassword, deleteEmail;
    private Button createBtn, deleteBtn, dashboardBtn;
    private TextView statusText;
    private static final String SERVER = "https://ghostspy.bruang.biz.id";
    private static final String ADMIN_AUTH = "admin@ghostspy.com:admin123";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        newEmail = findViewById(R.id.newEmail);
        newPassword = findViewById(R.id.newPassword);
        deleteEmail = findViewById(R.id.deleteEmail);
        createBtn = findViewById(R.id.createBtn);
        deleteBtn = findViewById(R.id.deleteBtn);
        dashboardBtn = findViewById(R.id.dashboardBtn);
        statusText = findViewById(R.id.adminStatus);

        createBtn.setOnClickListener(v -> createUser());
        deleteBtn.setOnClickListener(v -> deleteUser());
        dashboardBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
        });
    }

    private void createUser() {
        String email = newEmail.getText().toString().trim();
        String password = newPassword.getText().toString().trim();
        if (email.isEmpty() || password.isEmpty()) {
            statusText.setText("⚠️ Isi email & password");
            return;
        }
        statusText.setText("⏳ Membuat akun...");
        new Thread(() -> {
            try {
                URL url = new URL(SERVER + "/api/admin/create-user");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Basic " +
                    Base64.encodeToString(ADMIN_AUTH.getBytes(), Base64.NO_WRAP));
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
                runOnUiThread(() -> {
                    if (res.optString("status").equals("ok")) {
                        statusText.setText("✅ " + res.optString("message"));
                        newEmail.setText("");
                        newPassword.setText("");
                    } else {
                        statusText.setText("❌ " + res.optString("message"));
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> statusText.setText("❌ Koneksi gagal"));
            }
        }).start();
    }

    private void deleteUser() {
        String email = deleteEmail.getText().toString().trim();
        if (email.isEmpty()) {
            statusText.setText("⚠️ Masukkan email yang akan dihapus");
            return;
        }
        statusText.setText("⏳ Menghapus akun...");
        new Thread(() -> {
            try {
                URL url = new URL(SERVER + "/api/admin/delete-user");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Basic " +
                    Base64.encodeToString(ADMIN_AUTH.getBytes(), Base64.NO_WRAP));
                conn.setDoOutput(true);
                JSONObject body = new JSONObject();
                body.put("email", email);
                OutputStream os = conn.getOutputStream();
                os.write(body.toString().getBytes());
                os.flush();
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                JSONObject res = new JSONObject(sb.toString());
                runOnUiThread(() -> {
                    if (res.optString("status").equals("ok")) {
                        statusText.setText("✅ " + res.optString("message"));
                        deleteEmail.setText("");
                    } else {
                        statusText.setText("❌ " + res.optString("message"));
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> statusText.setText("❌ Koneksi gagal"));
            }
        }).start();
    }
}
