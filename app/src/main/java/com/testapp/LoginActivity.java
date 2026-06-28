package com.testapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.*;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginActivity extends Activity {
    private EditText emailInput, passwordInput;
    private Button loginBtn, buyBtn;
    private TextView statusText, titleText;
    private WebView gifWebView;
    private static final String SERVER = "https://ghostspy.bruang.biz.id";
    private String gifUrl = "https://i.ibb.co.com/5gVKbDbV/68747470733a2f2f73332e616d617a6f6e6177732e636f6d2f776174747061642d6d656469612d736572766963652f53746f.gif";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginBtn = findViewById(R.id.loginBtn);
        buyBtn = findViewById(R.id.buyBtn);
        statusText = findViewById(R.id.statusText);
        titleText = findViewById(R.id.titleText);
        gifWebView = findViewById(R.id.gifWebView);

        // Animasi berkedip pada judul
        AlphaAnimation anim = new AlphaAnimation(0.4f, 1.0f);
        anim.setDuration(1200);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
        titleText.startAnimation(anim);

        // Load GIF di WebView
        setupGifView();

        // Cek sudah login sebelumnya
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

        loginBtn.setOnClickListener(v -> {
            // Animasi tombol mengecil dulu
            ScaleAnimation scale = new ScaleAnimation(
                    1.0f, 0.95f, 1.0f, 0.95f,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            scale.setDuration(100);
            scale.setRepeatMode(Animation.REVERSE);
            scale.setRepeatCount(1);
            v.startAnimation(scale);

            doLogin();
        });

        buyBtn.setOnClickListener(v -> {
            // Bisa diarahkan ke website pembelian atau RegisterActivity
            Toast.makeText(this, "Fitur beli akun akan segera hadir", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupGifView() {
        WebSettings ws = gifWebView.getSettings();
        ws.setJavaScriptEnabled(false);
        ws.setLoadWithOverviewMode(true);
        ws.setUseWideViewPort(true);
        gifWebView.setWebViewClient(new WebViewClient());
        gifWebView.setBackgroundColor(0x00000000);
        gifWebView.loadUrl(gifUrl);
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

                if (res.optString("status").equals("ok")) {
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
                    runOnUiThread(() -> statusText.setText("❌ Anda Belum Jadi Member"));
                }
            } catch (Exception e) {
                runOnUiThread(() -> statusText.setText("❌ Anda Belum Jadi Member"));
            }
        }).start();
    }
}
