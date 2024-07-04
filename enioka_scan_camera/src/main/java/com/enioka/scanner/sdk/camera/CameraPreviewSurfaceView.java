package com.enioka.scanner.sdk.camera;

import static com.enioka.scanner.sdk.camera.CameraScannerProvider.AspectRatioMode;

import android.content.Context;
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
    private AspectRatioMode aspectRatioMode;

    interface Callback extends SurfaceHolder.Callback {
        Point getCurrentCameraResolution();

        int getCameraOrientationRelativeToDeviceNaturalOrientation();

        int getDeviceOrientationRelativeToDeviceNaturalOrientation();

        void resetTargetPosition();
    }

    public CameraPreviewSurfaceView(Context context, AspectRatioMode mode, Callback parent) {
        super(context);
        this.parent = parent;
        this.aspectRatioMode = mode;
    }

    protected void setPreviewRatioMode(AspectRatioMode mode) {
        this.aspectRatioMode = mode;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int parentImposedWidthPx = MeasureSpec.getSize(widthMeasureSpec);
        int parentImposedHeightPx = MeasureSpec.getSize(heightMeasureSpec);

        if (parent.getCurrentCameraResolution() == null || aspectRatioMode == AspectRatioMode.FILL_WITH_STRETCH) {
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
                if (aspectRatioMode == AspectRatioMode.FILL_WITH_BLACK_BARS) {
                    setMeasuredDimension(parentImposedWidthPx, (int) (parentImposedWidthPx / dataRatio));
                } else if (aspectRatioMode == AspectRatioMode.FILL_WITH_CROP) {
                    setMeasuredDimension((int) (parentImposedHeightPx * dataRatio), parentImposedHeightPx);
                }
            } else if (aspectRatioMode == AspectRatioMode.FILL_WITH_BLACK_BARS) {
                setMeasuredDimension((int) (parentImposedHeightPx * dataRatio), parentImposedHeightPx);
            } else if (aspectRatioMode == AspectRatioMode.FILL_WITH_CROP) {
                setMeasuredDimension(parentImposedWidthPx, (int) (parentImposedWidthPx / dataRatio));
            }
        }
        parent.resetTargetPosition();
    }
}
