package com.testapp;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class AccountManageActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tv = new TextView(this);
        tv.setText("Manajemen Akun (Coming Soon)");
        tv.setTextSize(24);
        tv.setTextColor(0xFFFFFFFF);
        tv.setBackgroundColor(0xFF080808);
        setContentView(tv);
    }
}
