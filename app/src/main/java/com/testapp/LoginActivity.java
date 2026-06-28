package com.testapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

public class LoginActivity extends Activity {
    private EditText emailInput, passwordInput;
    private Button loginBtn;
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginBtn = findViewById(R.id.loginBtn);
        statusText = findViewById(R.id.statusText);

        loginBtn.setOnClickListener(v -> {
            // Untuk sementara, langsung masuk ke MainActivity
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }
}
