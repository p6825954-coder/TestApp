package com.testapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginActivity extends Activity {
    private EditText emailInput, passwordInput;
    private Button loginBtn, registerBtn;
    private TextView statusText;
    private static final String SERVER = "https://ghostspy.bruang.biz.id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginBtn = findViewById(R.id.loginBtn);
        registerBtn = findViewById(R.id.registerBtn);
        statusText = findViewById(R.id.statusText);

        SharedPreferences prefs = getSharedPreferences("ghostspy", MODE_PRIVATE);
        String token = prefs.getString("token", "");
        String email = prefs.getString("email", "");
        String role = prefs.getString("role", "");
        if (!token.isEmpty() && !email.isEmpty()) {
            if (role.equals("admin")) {
                startActivity(new Intent(this, AdminActivity.class));
            } else {
                startActivity(new Intent(this, MainActivity.class));
            }
            finish();
            return;
        }

        loginBtn.setOnClickListener(v -> doLogin());
        registerBtn.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void doLogin() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        if (email.isEmpty() || password.isEmpty()) {
            statusText.setText("⚠️ Isi semua field");
            return;
        }

        String deviceId = android.provider.Settings.Secure.getString(
            getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        statusText.setText("⏳ Menghubungi server...");

        new Thread(() -> {
            try {
                URL url = new URL(SERVER + "/api/login");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject body = new JSONObject();
                body.put("email", email);
                body.put("password", password);
                body.put("device_id", deviceId);
                OutputStream os = conn.getOutputStream();
                os.write(body.toString().getBytes());
                os.flush();

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                JSONObject res = new JSONObject(sb.toString());

                if (res.getString("status").equals("ok")) {
                    String role = res.optString("role", "user");
                    SharedPreferences prefs = getSharedPreferences("ghostspy", MODE_PRIVATE);
                    prefs.edit()
                        .putString("token", res.optString("token", ""))
                        .putString("email", email)
                        .putString("role", role)
                        .apply();

                    runOnUiThread(() -> {
                        statusText.setText("✅ Login berhasil!");
                        if (role.equals("admin")) {
                            startActivity(new Intent(this, AdminActivity.class));
                        } else {
                            startActivity(new Intent(this, MainActivity.class));
                        }
                        finish();
                    });
                } else {
                    runOnUiThread(() -> statusText.setText("❌ " + res.getString("message")));
                }
            } catch (Exception e) {
                runOnUiThread(() -> statusText.setText("❌ Koneksi gagal"));
            }
        }).start();
    }
}
