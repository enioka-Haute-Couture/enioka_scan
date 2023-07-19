package com.enioka.scanner.camera;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.enioka.scanner.R;

/**
 * A helper view for displaying the camera preview. It should not be needed in case we want the preview
 * to be stretched to the whole available surface, but with Camera V2 (and only V2) some dark magic
 * occurs when setting a SurfaceView dimensions (resulting in likely overscan on most devices), and
 * we need to override this behaviour in onMeasure.<br>
 * As a bonus, we enable the possibility to "fit to ratio" the preview (adding black bars, not cropping).
 */
class CameraPreviewSurfaceView extends SurfaceView {
    private final Callback parent;
    private final boolean respectCameraRatio;

    interface Callback extends SurfaceHolder.Callback {
        Point getCurrentCameraResolution();

        int getCameraOrientationRelativeToDeviceNaturalOrientation();

        int getDeviceOrientationRelativeToDeviceNaturalOrientation();
    }

    public CameraPreviewSurfaceView(Context context, TypedArray styledAttributes, Callback parent) {
        super(context);
        this.parent = parent;

        int rm = styledAttributes.getInt(R.styleable.CameraBarcodeScanView_previewRatioMode, 0);
        respectCameraRatio = rm == 1;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int parentImposedWidthPx = MeasureSpec.getSize(widthMeasureSpec);
        int parentImposedHeightPx = MeasureSpec.getSize(heightMeasureSpec);

        if (parent.getCurrentCameraResolution() == null || !respectCameraRatio) {
            setMeasuredDimension(parentImposedWidthPx, parentImposedHeightPx);
        } else {
            float dataRatio = ((float) parent.getCurrentCameraResolution().x) / parent.getCurrentCameraResolution().y;

            // What is the ratio main dimension?
            int relativeOrientation = (parent.getCameraOrientationRelativeToDeviceNaturalOrientation() - parent.getDeviceOrientationRelativeToDeviceNaturalOrientation() + 360) % 360;
            if (relativeOrientation == 0 || relativeOrientation == 180) {
                // Device and camera have currently the same orientation
            } else {
                dataRatio = 1 / dataRatio;
            }

            // We try to fit the whole camera buffer onto the surface
            if (parentImposedWidthPx < parentImposedHeightPx * dataRatio) {
                setMeasuredDimension(parentImposedWidthPx, (int) (parentImposedWidthPx / dataRatio));
            } else {
                setMeasuredDimension((int) (parentImposedHeightPx * dataRatio), parentImposedHeightPx);
            }
        }
    }
}
