package com.testapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class MatrixRainView extends View {
    private Paint paint = new Paint();
    private boolean running = true;

    public MatrixRainView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setColor(0x00000000); // transparan
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // Kosong, tidak ada animasi matrix
    }

    public void stopRain() { running = false; }
    public void startRain() { running = true; }
}
