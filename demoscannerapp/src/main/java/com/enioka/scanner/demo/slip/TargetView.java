package com.enioka.scanner.demo.slip;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

/**
 * Targeting rectangles for slip scan.
 */
public class TargetView extends com.enioka.scanner.camera.TargetView {
    protected static final String TAG = "TargetView";

    /**
     * Store the value of the gauge frame paint
     */
    protected Paint frameRectPaint;

    protected Rect frame = new Rect();

    public TargetView(Context context) {
        super(context);
    }

    public TargetView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TargetView(Context context, Rect target, Rect frame) {
        super(context);
        this.frame = frame;
        this.targetRect = target;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        // Do nothing. This removes computations from parent.
    }

    @Override
    protected void init() {
        super.init();

        frameRectPaint = new Paint();
        frameRectPaint.setColor(Color.GREEN);
        frameRectPaint.setStrokeWidth(4);
        frameRectPaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawRect(frame, frameRectPaint);
    }
}
