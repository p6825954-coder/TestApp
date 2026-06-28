package com.testapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class LoginActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Langsung pindah ke MainActivity
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
