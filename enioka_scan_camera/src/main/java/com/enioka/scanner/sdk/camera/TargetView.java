package com.enioka.scanner.sdk.camera;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Rect;
import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.enioka.scanner.R;

/**
 * The "target visor" view to be drawn on top of the scanner view.
 */
class TargetView extends View {
    protected static final String TAG = "BARCODE";

    protected Paint targetRectPaint, guideLinePaint;
    protected Rect wholeView = new Rect();
    protected Rect targetRect = new Rect();

    @ColorInt
    protected final int rectColorActive;
    @ColorInt
    protected final int rectColorPaused;
    protected final int rectStrokeWidth;

    public TargetView(Context context, @Nullable TypedArray styledAttributes) {
        super(context);

        if (styledAttributes != null) {
            rectColorActive = styledAttributes.getColor(R.styleable.CameraBarcodeScanView_targetColorActive, Color.RED);
            rectColorPaused = styledAttributes.getColor(R.styleable.CameraBarcodeScanView_targetColorPaused, Color.GRAY);
            rectStrokeWidth = styledAttributes.getInteger(R.styleable.CameraBarcodeScanView_targetStrokeWidth, 5);
        } else {
            rectColorActive = Color.RED;
            rectColorPaused = Color.GRAY;
            rectStrokeWidth = 5;
        }

        init();
    }

    protected void init() {
        targetRectPaint = new Paint();
        targetRectPaint.setColor(rectColorActive);
        targetRectPaint.setStrokeWidth(rectStrokeWidth);
        targetRectPaint.setStyle(Paint.Style.STROKE);

        guideLinePaint = new Paint();
        guideLinePaint.setColor(Color.RED);
        guideLinePaint.setStyle(Paint.Style.STROKE);
        guideLinePaint.setPathEffect(new DashPathEffect(new float[]{5, 10, 15, 20}, 0));
    }

    public void pauseTarget() {
        targetRectPaint.setColor(rectColorPaused);
        guideLinePaint.setColor(rectColorPaused);
        invalidate(); // Force redraw
    }

    public void resumeTarget() {
        targetRectPaint.setColor(rectColorActive);
        guideLinePaint.setColor(Color.RED);
        invalidate(); // Force redraw
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
