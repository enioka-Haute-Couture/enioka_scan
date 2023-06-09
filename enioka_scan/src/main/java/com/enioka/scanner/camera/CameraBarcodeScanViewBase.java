package com.enioka.scanner.camera;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;

import com.enioka.scanner.R;
import com.enioka.scanner.data.BarcodeType;

import java.util.ArrayList;
import java.util.List;

abstract class CameraBarcodeScanViewBase extends FrameLayout implements ScannerCallback {
    protected static final String TAG = "BARCODE";

    /////////////////////////////////
    // User-defined configuration
    protected List<BarcodeType> symbologies = new ArrayList<>();
    protected CameraBarcodeScanView.ResultHandler handler;
    protected boolean allowTargetDrag = true;
    protected CameraReader readerMode = CameraReader.ZBAR;

    ////////////////////////////////
    // State
    protected boolean isTorchSupported = false;
    protected boolean isTorchOn = false;
    protected boolean failed = false;

    protected Resolution resolution = new Resolution(getContext());
    protected byte[] lastPreviewData;

    ///////////////////////////////
    // Actual analyser
    protected FrameAnalyserManager frameAnalyser;

    //////////////////////////////
    // Target view data
    protected static final int RECT_HEIGHT = 10;
    protected static final float MM_INSIDE_INCH = 25.4f;
    protected float ydpi;
    protected float dragStartY, dragCropTop, dragCropBottom;
    protected Rect cropRect = new Rect(); // The "targeting" rectangle.


    protected SurfaceView camPreviewSurfaceView;
    protected View targetView;
    protected final TypedArray styledAttributes;


    public CameraBarcodeScanViewBase(@NonNull Context context) {
        super(context);
        this.styledAttributes = null;
    }

    public CameraBarcodeScanViewBase(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.styledAttributes = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.CameraBarcodeScanView,
                0, 0);

        int rm = styledAttributes.getInt(R.styleable.CameraBarcodeScanView_readerMode, 0);
        switch (rm) {
            case 1:
                readerMode = CameraReader.ZXING;
                break;
            default:
                readerMode = CameraReader.ZBAR;
                break;
        }

        this.allowTargetDrag = !styledAttributes.getBoolean(R.styleable.CameraBarcodeScanView_targetIsFixed, false);
    }

    /**
     * Default is simply CODE_128. Use the Symbol static fields to specify a symbology.
     *
     * @param barcodeType the symbology
     */
    public void addSymbology(BarcodeType barcodeType) {
        this.symbologies.add(barcodeType);
        if (frameAnalyser != null) {
            frameAnalyser.addSymbology(barcodeType);
        }
    }

    protected void reinitialiseFrameAnalyser() {
        if (this.frameAnalyser != null) {
            this.frameAnalyser.close();
        }

        this.frameAnalyser = new FrameAnalyserManager(this, resolution, readerMode);

        for (BarcodeType symbology : this.symbologies) {
            this.frameAnalyser.addSymbology(symbology);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Public API, various setters
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void setReaderMode(CameraReader readerMode) {
        this.readerMode = readerMode;
        reinitialiseFrameAnalyser();
    }


    public void setResultHandler(CameraBarcodeScanView.ResultHandler handler) {
        this.handler = handler;
    }

    /**
     * Switch on or switch off the torch mode
     *
     * @param value indicate if the torch mode must be switched on (true) or off (false)
     */
    // TODO: finish this
    public void setTorch(boolean value) {
        if (failed) {
            return;
        }

        boolean support = getSupportTorch();
        if (support && value) {
            setTorchInternal(true);
        } else if (support) {
            setTorchInternal(false);
        }
    }

    abstract void setTorchInternal(boolean value);

    public abstract void cleanUp();

    public abstract void pauseCamera();

    public abstract void resumeCamera();

    /**
     * Indicate if the torch mode is handled or not
     *
     * @return A true value if the torch mode supported, false otherwise
     */
    public boolean getSupportTorch() {
        if (failed) {
            return false;
        }
        if (this.isInEditMode()) {
            return true;
        }
        return isTorchSupported;
    }

    /**
     * @return true if torch is on
     */
    public boolean getTorchOn() {
        return isTorchOn;
    }

    public void analyserCallback(final String result, final BarcodeType type, byte[] previewData) {
        if (resolution.usePreviewForPhoto) {
            lastPreviewData = previewData;
        }

        /*if (!keepScanning) {
            this.closeCamera();
        }*/

        // Return result on main thread
        this.post(() -> {
            if (CameraBarcodeScanViewBase.this.handler != null) {
                CameraBarcodeScanViewBase.this.handler.handleScanResult(result, type);
            }
        });
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Target area handling

    /**
     * Sets up the central targeting rectangle. Must be called after surface init.
     */
    protected void computeCropRectangle() {
        // First, top may be a user preference
        Activity a = ViewHelpersPreferences.getActivity(getContext());
        int top = -1;
        if (a != null && allowTargetDrag) {
            try {
                SharedPreferences p = a.getPreferences(Context.MODE_PRIVATE);
                top = p.getInt("y" + getCameraDisplayOrientation(), 0);
            } catch (Exception e) {
                Log.w(TAG, "Could not retrieve preferences");
            }
        }

        // Target rectangle dimensions
        DisplayMetrics metrics = this.getContext().getResources().getDisplayMetrics();
        //float xdpi = metrics.xdpi;
        ydpi = metrics.ydpi;

        int actualLayoutWidth, actualLayoutHeight;
        if (this.isInEditMode()) {
            actualLayoutWidth = this.getMeasuredWidth();
            actualLayoutHeight = this.getMeasuredHeight();
        } else {
            actualLayoutWidth = this.camPreviewSurfaceView.getMeasuredWidth();
            actualLayoutHeight = this.camPreviewSurfaceView.getMeasuredHeight();
        }

        int y1, y3;
        if (top > 10) {
            y1 = top;
            y3 = (int) (top + RECT_HEIGHT / MM_INSIDE_INCH * ydpi);
        } else {
            y1 = (int) (actualLayoutHeight / 2 - RECT_HEIGHT / 2 / MM_INSIDE_INCH * ydpi);
            y3 = (int) (actualLayoutHeight / 2 + RECT_HEIGHT / 2 / MM_INSIDE_INCH * ydpi);
        }

        cropRect.top = y1;
        cropRect.bottom = y3;
        cropRect.left = (int) (actualLayoutWidth * 0.1);
        cropRect.right = (int) (actualLayoutWidth * 0.9);

        Log.i(TAG, "Setting targeting rect at " + cropRect);
    }

    /**
     * Adds the targeting view to layout. Must be called after computeCropRectangle was called. Separate from computeCropRectangle
     * because: we want to add this view last, in order to put it on top. (and we need to calculate the crop rectangle early).
     */
    protected void addTargetView() {
        final View targetView = new TargetView(this.getContext(), this.styledAttributes);
        targetView.setId(R.id.barcode_scanner_camera_view);
        final FrameLayout.LayoutParams prms = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, (int) (RECT_HEIGHT / MM_INSIDE_INCH * ydpi));
        prms.setMargins(0, cropRect.top, 0, 0);

        Log.i(TAG, "Targeting overlay added");
        this.addView(targetView, prms);
        this.targetView = targetView;

        if (allowTargetDrag) {
            targetView.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dragStartY = event.getY();
                        dragCropTop = cropRect.top;
                        dragCropBottom = cropRect.bottom;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        final float dy = event.getY() - dragStartY;
                        float newTop = dragCropTop + (int) dy;
                        float newBottom = dragCropBottom + (int) dy;
                        if (newTop > 0 && newBottom < CameraBarcodeScanViewBase.this.camPreviewSurfaceView.getHeight()) {
                            cropRect.top = (int) newTop;
                            cropRect.bottom = (int) newBottom;

                            dragCropTop = newTop;
                            dragCropBottom = newBottom;

                            prms.topMargin = cropRect.top;
                            targetView.setLayoutParams(prms);
                        }

                        return true;

                    case MotionEvent.ACTION_UP:
                        dragStartY = 0;
                        v.performClick();
                        ViewHelpersPreferences.storePreferences(getContext(), "y" + getCameraDisplayOrientation(), cropRect.top);
                        break;
                }

                return false;
            });
        }
    }

    protected abstract int getCameraDisplayOrientation();

    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

}
