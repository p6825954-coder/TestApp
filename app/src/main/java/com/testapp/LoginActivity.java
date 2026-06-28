package com.testapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class LoginActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Button masuk = (Button) findViewById(android.R.id.button1); // ambil button pertama
        if (masuk != null) masuk.setOnClickListener(v -> startActivity(new Intent(this, DashboardActivity.class)));
        // Fallback: klik di mana saja
        findViewById(android.R.id.content).setOnClickListener(v -> startActivity(new Intent(this, DashboardActivity.class)));
    }
}
