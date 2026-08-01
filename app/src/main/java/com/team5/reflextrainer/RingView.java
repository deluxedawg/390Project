package com.team5.reflextrainer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class RingView extends View {

    private final Paint targetPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // progress 0..1 : 1 = far out (just spawned), 0 = collapsed onto target (the beat)
    private float progress = 1f;
    private boolean active = false;

    public RingView(Context c, AttributeSet a) {
        super(c, a);
        targetPaint.setStyle(Paint.Style.STROKE);
        targetPaint.setStrokeWidth(6f);
        targetPaint.setColor(Color.parseColor("#8C96A6"));   // grey target

        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setStrokeWidth(10f);
        ringPaint.setColor(Color.parseColor("#29FF88"));     // green shrinking ring, closes in on the beat
    }

    public void setProgress(float p) {
        this.progress = Math.max(0f, Math.min(1f, p));
        this.active = true;
        invalidate();
    }

    public void clearRing() {
        this.active = false;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        float targetR = getWidth() * 0.28f;     // fixed target ring radius
        float maxR = getWidth() * 0.48f;         // spawn radius

        // target (where the ring should land)
        canvas.drawCircle(cx, cy, targetR, targetPaint);

        // shrinking ring
        if (active) {
            float r = targetR + (maxR - targetR) * progress;
            canvas.drawCircle(cx, cy, r, ringPaint);
        }
    }
}