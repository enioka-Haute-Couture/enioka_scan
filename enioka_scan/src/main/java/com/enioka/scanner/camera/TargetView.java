package com.enioka.scanner.camera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * The "target visor" view to be drawn on top of the scanner view.
 */
class TargetView extends View {
    protected static final String TAG = "BARCODE";

    protected Paint targetRectPaint, guideLinePaint;
    protected Rect wholeView = new Rect();
    protected Rect targetRect = new Rect();

    public TargetView(Context context) {
        super(context);
        init();
    }

    public TargetView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    protected void init() {
        targetRectPaint = new Paint();
        targetRectPaint.setColor(Color.RED);
        targetRectPaint.setStrokeWidth(5);
        targetRectPaint.setStyle(Paint.Style.STROKE);

        guideLinePaint = new Paint();
        guideLinePaint.setColor(Color.RED);
        guideLinePaint.setStyle(Paint.Style.STROKE);
        guideLinePaint.setPathEffect(new DashPathEffect(new float[]{5, 10, 15, 20}, 0));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (!changed) {
            return;
        }

        // The view takes all the space given
        wholeView.bottom = bottom;
        wholeView.top = top;
        wholeView.left = left;
        wholeView.right = right;

        // Target rect (relative to view coordinates, not parent).
        int width = right - left;
        targetRect.left = (int) (width * 0.1);
        targetRect.right = (int) (width * 0.9);
        targetRect.bottom = wholeView.bottom - wholeView.top;
        targetRect.top = 0;

        Log.i(TAG, "Left: " + left + " - Top : " + top + " - Right: " + right + " - bottom: " + bottom);
        Log.i(TAG, "target was laid out");
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawRect(targetRect, targetRectPaint);
        canvas.drawLine(0, (wholeView.bottom - wholeView.top) / 2, wholeView.right, (wholeView.bottom - wholeView.top) / 2, guideLinePaint);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }
}
