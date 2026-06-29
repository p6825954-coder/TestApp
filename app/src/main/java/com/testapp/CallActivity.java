package com.testapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.widget.*;
import java.util.Locale;

public class CallActivity extends Activity {
    private TextView statusText, durationText;
    private SeekBar progressBar, volumeBar;
    private Button playBtn, pauseBtn, stopBtn, muteBtn;
    private EditText urlInput;
    private LinearLayout historyContainer;
    private boolean isMuted = false;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF090909);
        setContentView(root);

        // Header
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setBackgroundColor(0xAA171717);
        header.setPadding(16, 12, 16, 12);

        Button backBtn = new Button(this);
        backBtn.setText("←");
        backBtn.setTextColor(0xFFFFFFFF);
        backBtn.setBackgroundColor(0x00000000);
        backBtn.setOnClickListener(v -> finish());
        header.addView(backBtn);

        TextView title = new TextView(this);
        title.setText("🎵 Audio Player");
        title.setTextColor(0xFFFFFFFF);
        title.setTextSize(18);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        title.setLayoutParams(titleParams);
        header.addView(title);

        Button settingsBtn = new Button(this);
        settingsBtn.setText("⚙️");
        settingsBtn.setTextColor(0xFFFFFFFF);
        settingsBtn.setBackgroundColor(0x00000000);
        header.addView(settingsBtn);
        root.addView(header);

        // Kartu Utama
        LinearLayout mainCard = new LinearLayout(this);
        mainCard.setOrientation(LinearLayout.VERTICAL);
        mainCard.setGravity(Gravity.CENTER);
        mainCard.setBackground(getDrawable(R.drawable.card_admin));
        mainCard.setPadding(20, 20, 20, 20);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(16, 16, 16, 8);
        mainCard.setLayoutParams(cardParams);

        // Thumbnail / ikon
        TextView thumb = new TextView(this);
        thumb.setText("🎼");
        thumb.setTextSize(64);
        thumb.setGravity(Gravity.CENTER);
        mainCard.addView(thumb);

        // Judul & sumber
        TextView titleAudio = new TextView(this);
        titleAudio.setText("Judul Audio");
        titleAudio.setTextColor(0xFFFFFFFF);
        titleAudio.setTextSize(16);
        titleAudio.setTypeface(null, android.graphics.Typeface.BOLD);
        titleAudio.setGravity(Gravity.CENTER);
        mainCard.addView(titleAudio);

        TextView source = new TextView(this);
        source.setText("Sumber: URL");
        source.setTextColor(0xFF9AA3B2);
        source.setTextSize(12);
        source.setGravity(Gravity.CENTER);
        mainCard.addView(source);

        // Status & durasi
        statusText = new TextView(this);
        statusText.setText("Siap");
        statusText.setTextColor(0xFF00E676);
        statusText.setGravity(Gravity.CENTER);
        mainCard.addView(statusText);

        durationText = new TextView(this);
        durationText.setText("00:00 / 00:00");
        durationText.setTextColor(0xFFFFFFFF);
        durationText.setGravity(Gravity.CENTER);
        mainCard.addView(durationText);

        root.addView(mainCard);

        // Input URL
        urlInput = new EditText(this);
        urlInput.setHint("🔗 Masukkan URL file audio...");
        urlInput.setTextColor(0xFFFFFFFF);
        urlInput.setBackgroundColor(0xFF171717);
        urlInput.setPadding(16, 12, 16, 12);
        LinearLayout.LayoutParams urlParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        urlParams.setMargins(16, 8, 16, 8);
        urlInput.setLayoutParams(urlParams);
        root.addView(urlInput);

        // Tombol kontrol (bulat)
        LinearLayout controlRow = new LinearLayout(this);
        controlRow.setOrientation(LinearLayout.HORIZONTAL);
        controlRow.setGravity(Gravity.CENTER);
        controlRow.setPadding(0, 8, 0, 8);

        playBtn = createControlBtn("▶");
        controlRow.addView(playBtn);

        pauseBtn = createControlBtn("⏸");
        controlRow.addView(pauseBtn);

        stopBtn = createControlBtn("⏹");
        controlRow.addView(stopBtn);

        muteBtn = createControlBtn("🔊");
        muteBtn.setOnClickListener(v -> {
            isMuted = !isMuted;
            muteBtn.setText(isMuted ? "🔇" : "🔊");
        });
        controlRow.addView(muteBtn);

        root.addView(controlRow);

        // Slider progress
        progressBar = new SeekBar(this);
        progressBar.setMax(100);
        progressBar.setProgress(0);
        LinearLayout.LayoutParams progParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        progParams.setMargins(16, 4, 16, 4);
        progressBar.setLayoutParams(progParams);
        root.addView(progressBar);

        // Slider volume
        volumeBar = new SeekBar(this);
        volumeBar.setMax(100);
        volumeBar.setProgress(80);
        LinearLayout.LayoutParams volParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        volParams.setMargins(16, 0, 16, 8);
        volumeBar.setLayoutParams(volParams);
        root.addView(volumeBar);

        // Visualizer dummy (5 bar)
        LinearLayout visualizer = new LinearLayout(this);
        visualizer.setOrientation(LinearLayout.HORIZONTAL);
        visualizer.setGravity(Gravity.CENTER);
        visualizer.setPadding(16, 8, 16, 8);
        for (int i = 0; i < 5; i++) {
            ProgressBar bar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
            bar.setMax(100);
            bar.setProgress((i+1)*20);
            bar.setBackgroundColor(0xFF00E676);
            LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(0, 48, 1);
            barParams.setMargins(2, 0, 2, 0);
            bar.setLayoutParams(barParams);
            visualizer.addView(bar);
        }
        root.addView(visualizer);

        // Riwayat
        TextView historyTitle = new TextView(this);
        historyTitle.setText("Riwayat");
        historyTitle.setTextColor(0xFFFFFFFF);
        historyTitle.setTextSize(14);
        historyTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        historyTitle.setPadding(16, 12, 16, 4);
        root.addView(historyTitle);

        historyContainer = new LinearLayout(this);
        historyContainer.setOrientation(LinearLayout.VERTICAL);
        historyContainer.setPadding(16, 0, 16, 16);

        // Dummy history
        addHistoryItem("song1.mp3", "3:45", "Hari ini");
        addHistoryItem("podcast.wav", "12:10", "Kemarin");
        root.addView(historyContainer);
    }

    private Button createControlBtn(String icon) {
        Button btn = new Button(this);
        btn.setText(icon);
        btn.setTextColor(0xFFFFFFFF);
        btn.setBackgroundColor(0xFF00E676);
        btn.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(56, 56);
        params.setMargins(8, 0, 8, 0);
        btn.setLayoutParams(params);
        return btn;
    }

    private void addHistoryItem(String name, String duration, String date) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.HORIZONTAL);
        item.setBackground(getDrawable(R.drawable.card_admin));
        item.setPadding(12, 10, 12, 10);
        item.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 6);
        item.setLayoutParams(lp);

        TextView icon = new TextView(this);
        icon.setText("🎵");
        icon.setTextSize(20);
        icon.setPadding(0, 0, 12, 0);
        item.addView(icon);

        LinearLayout infoCol = new LinearLayout(this);
        infoCol.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        infoCol.setLayoutParams(infoParams);

        TextView nameView = new TextView(this);
        nameView.setText(name);
        nameView.setTextColor(0xFFFFFFFF);
        nameView.setTextSize(14);

        TextView durView = new TextView(this);
        durView.setText(duration + " • " + date);
        durView.setTextColor(0xFF9AA3B2);
        durView.setTextSize(11);

        infoCol.addView(nameView);
        infoCol.addView(durView);
        item.addView(infoCol);

        historyContainer.addView(item);
    }
}
