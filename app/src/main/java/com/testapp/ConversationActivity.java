package com.testapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONObject;

public class ConversationActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String address = getIntent().getStringExtra("address");
        String body = getIntent().getStringExtra("body");
        String date = getIntent().getStringExtra("date");

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF0B0B0B);
        root.setPadding(16, 32, 16, 32);

        TextView title = new TextView(this);
        title.setText("Percakapan dengan " + address);
        title.setTextColor(0xFFFFFFFF);
        title.setTextSize(18);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        root.addView(title);

        // Tampilkan dummy bubble chat
        LinearLayout bubble = new LinearLayout(this);
        bubble.setBackground(getDrawable(R.drawable.card_admin));
        bubble.setPadding(16, 12, 16, 12);
        bubble.setGravity(Gravity.END);
        TextView msgView = new TextView(this);
        msgView.setText(body);
        msgView.setTextColor(0xFFFFFFFF);
        msgView.setTextSize(14);
        bubble.addView(msgView);
        root.addView(bubble);

        TextView timeView = new TextView(this);
        timeView.setText(date);
        timeView.setTextColor(0xFF9AA3B2);
        timeView.setTextSize(10);
        root.addView(timeView);

        setContentView(root);
    }
}
