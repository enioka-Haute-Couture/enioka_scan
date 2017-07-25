package com.enioka.scanner.camera;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;

/**
 * This class contains everything that should be displayed upon a ZbarScanView.
 * It actually delegates all drawing to ZbarScanView. It only exists to avoid hacking too much the
 * FrameLayout which does not call the OnDraw method by default.
 */
class ZbarScanViewOverlay extends View {
    public ZbarScanView dad;
    float dragStartX = 0, dragStartY = 0;
    private Context ctx;

    public ZbarScanViewOverlay(Context context, ZbarScanView dad) {
        super(context);
        setFocusable(true);
        this.dad = dad;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            dad.initRect();
        }

        Activity a = this.getActivity();
        if (a == null) {
            return;
        }

        SharedPreferences p = a.getPreferences(Context.MODE_PRIVATE);
        float y = p.getFloat("y", dad.y1);
        if (y != dad.y1 && y > 0 && y < this.getHeight()) {
            float dy = dad.y1 - y;
            dad.y1 -= dy;
            dad.y2 -= dy;
            dad.y3 -= dy;
            dad.y4 -= dy;
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        dad.drawHud(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (dragStartX == 0 && !(event.getX() > dad.x1 - 50 && event.getX() < dad.x2 + 50 && event.getY() > dad.y1 - 50 && event.getY() < dad.y3 + 50)) {
            // We only care about touch events inside the rectangle.
            return super.onTouchEvent(event);
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                dragStartX = event.getX();
                dragStartY = event.getY();
                return true;
            case MotionEvent.ACTION_MOVE:
                // final float dx = event.getX() - dragStartX;
                final float dy = event.getY() - dragStartY;

                dragStartX = event.getX();
                dragStartY = event.getY();

                /*dad.x1 += dx;
                dad.x2 += dx;
                dad.x3 += dx;
                dad.x4 += dx;*/

                if (dad.y1 + dy > 0 && dad.y1 + dy < this.getHeight()) {
                    dad.y1 += dy;
                    dad.y2 += dy;
                    dad.y3 += dy;
                    dad.y4 += dy;
                }

                invalidate();
                return true;

            case MotionEvent.ACTION_UP:
                dragStartY = 0;
                dragStartX = 0;
                return super.onTouchEvent(event);
        }

        return false;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        storePreferences(dad.y1);
    }

    private void storePreferences(float y) {
        Activity a = this.getActivity();
        if (a == null) {
            return;
        }

        SharedPreferences p = a.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor e = p.edit();
        e.putFloat("y", y);
        e.commit();
    }

    private Activity getActivity() {
        Context context = getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }
}
