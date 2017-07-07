package com.enioka.scanner.camera;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;

/**
 * This class contains everything that should be displayed upon a ZbarScanView.
 * It actually delegates all drawing to ZbarScanView. It only exists to avoid hacking too much the
 * FrameLayout which does not call the OnDraw method by default.
 */
class ZbarScanViewOverlay extends View {
    public ZbarScanView dad;

    public ZbarScanViewOverlay(Context context, ZbarScanView dad) {
        super(context);
        this.dad = dad;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            dad.initRect();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        dad.drawHud(canvas);
    }
}
