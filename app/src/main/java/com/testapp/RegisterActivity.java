package com.testapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class RegisterActivity extends Activity {
    private EditText emailInput, passwordInput, confirmInput;
    private Button registerBtn;
    private TextView statusText;
    private static final String SERVER = "https://ghostspy.bruang.biz.id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmInput = findViewById(R.id.confirmInput);
        registerBtn = findViewById(R.id.registerBtn);
        statusText = findViewById(R.id.statusText);

        registerBtn.setOnClickListener(v -> doRegister());
    }

    private void doRegister() {
        String email = emailInput.getText().toString().trim();
        String pass = passwordInput.getText().toString().trim();
        String confirm = confirmInput.getText().toString().trim();

        if (email.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
            statusText.setText("⚠️ Isi semua field");
            return;
        }
        if (!pass.equals(confirm)) {
            statusText.setText("⚠️ Password tidak cocok");
            return;
        }

        String deviceId = android.provider.Settings.Secure.getString(
            getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        statusText.setText("⏳ Membuat akun...");

        new Thread(() -> {
            try {
                URL url = new URL(SERVER + "/api/register");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject body = new JSONObject();
                body.put("email", email);
                body.put("password", pass);
                body.put("device_id", deviceId);
                OutputStream os = conn.getOutputStream();
                os.write(body.toString().getBytes());
                os.flush();

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                JSONObject res = new JSONObject(sb.toString());

                runOnUiThread(() -> {
                    if (res.getString("status").equals("ok")) {
                        statusText.setText("✅ " + res.getString("message") + "\nSilakan login.");
                        // Kembali ke login setelah 2 detik
                        new android.os.Handler().postDelayed(() -> finish(), 2000);
                    } else {
                        statusText.setText("❌ " + res.getString("message"));
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> statusText.setText("❌ Koneksi gagal"));
            }
        }).start();
    }
}
