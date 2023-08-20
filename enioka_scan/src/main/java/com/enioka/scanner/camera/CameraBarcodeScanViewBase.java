package com.enioka.scanner.camera;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.enioka.scanner.R;
import com.enioka.scanner.data.BarcodeType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

abstract class CameraBarcodeScanViewBase<T> extends FrameLayout implements ScannerCallback<T>, CameraPreviewSurfaceView.Callback {
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
    protected final ConcurrentLinkedQueue<byte[]> croppedImageBufferQueue = new ConcurrentLinkedQueue<>();

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
        initLayout();
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
        this.resolution.useAdaptiveResolution = styledAttributes.getBoolean(R.styleable.CameraBarcodeScanView_useAdaptiveResolution, true);
        this.resolution.storePreferredResolution = styledAttributes.getBoolean(R.styleable.CameraBarcodeScanView_storePreferredResolution, true);
        this.resolution.maxResolutionY = styledAttributes.getInteger(R.styleable.CameraBarcodeScanView_maxResolutionY, 1080);
        this.resolution.maxDistortionRatio = styledAttributes.getFloat(R.styleable.CameraBarcodeScanView_maxDistortionRatio, 0.3f);
        initLayout();
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

    protected void initializeFrameAnalyzerIfNeeded() {
        if (frameAnalyser == null) {
            reinitialiseFrameAnalyser();
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

    protected void initLayout() {
        if (this.camPreviewSurfaceView != null) {
            Log.d(TAG, "Layout is already initialized, nothing to do");
            return;
        }

        Log.d(TAG, "Creating camera view layout");

        if (!this.isInEditMode()) {
            // ZBar is a native library
            System.loadLibrary("iconv");
            reinitialiseFrameAnalyser();
        }

        // If the preview does not take all the space
        this.setBackgroundColor(Color.BLACK);

        // The view holding the preview. This will in turn (camHolder.addCallback) call setUpCamera.
        if (this.camPreviewSurfaceView == null) {
            camPreviewSurfaceView = new CameraPreviewSurfaceView(getContext(), styledAttributes, this);
            FrameLayout.LayoutParams prms = this.generateDefaultLayoutParams();
            prms.gravity = Gravity.CENTER;
            camPreviewSurfaceView.setLayoutParams(prms);
            this.addView(camPreviewSurfaceView);
        }
        camPreviewSurfaceView.getHolder().addCallback(this);

        // For the preview inside the IDE - callbacks will not be called in the IDE
        if (isInEditMode()) {
            // In normal mode done in surface created callback, but no callback in edit mode.
            computeCropRectangle();
            addTargetView();
        }
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        if (this.targetView == null) {
            computeCropRectangle();
            addTargetView();
        }
    }

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
                top = p.getInt("y" + getDeviceOrientationRelativeToDeviceNaturalOrientation(), 0);
            } catch (Exception e) {
                Log.w(TAG, "Could not retrieve preferences");
            }
        }

        // Target rectangle dimensions
        DisplayMetrics metrics = this.getContext().getResources().getDisplayMetrics();
        ydpi = metrics.ydpi;

        int actualLayoutWidth, actualLayoutHeight;
        if (this.isInEditMode()) {
            actualLayoutWidth = this.getMeasuredWidth();
            actualLayoutHeight = this.getMeasuredHeight();
        } else {
            actualLayoutWidth = this.camPreviewSurfaceView.getMeasuredWidth();
            actualLayoutHeight = this.camPreviewSurfaceView.getMeasuredHeight();
        }

        float rectHeightPixelsViewCoordinates = (float) RECT_HEIGHT / MM_INSIDE_INCH * ydpi;

        // Sanity check (happens because of stored results with previous versions)
        if (top < rectHeightPixelsViewCoordinates || top > actualLayoutHeight - rectHeightPixelsViewCoordinates) {
            top = (int) (actualLayoutHeight / 2 - rectHeightPixelsViewCoordinates / 2);
        }

        cropRect.top = top;
        cropRect.bottom = (int) (top + rectHeightPixelsViewCoordinates);
        cropRect.left = (int) (actualLayoutWidth * 0.1);
        cropRect.right = (int) (actualLayoutWidth * 0.9);

        Log.i(TAG, "Setting targeting rect at (left,top - right,bottom) " + cropRect);
        Log.i(TAG, "Targeting view is positioned inside a w*h " + actualLayoutWidth + "*" + actualLayoutHeight + " view");
        Log.i(TAG, "The preview surface view is at (top, left, bottom, right) " + camPreviewSurfaceView.getTop() + "," + camPreviewSurfaceView.getLeft() + " - " + camPreviewSurfaceView.getBottom() + "," + camPreviewSurfaceView.getRight());
    }

    /**
     * Adds the targeting view to layout. Must be called after computeCropRectangle was called. Separate from computeCropRectangle
     * because: we want to add this view last, in order to put it on top. (and we need to calculate the crop rectangle early).
     */
    protected void addTargetView() {
        float rectHeightPixelsViewCoordinates = (float) RECT_HEIGHT / MM_INSIDE_INCH * ydpi;

        final View targetView = new TargetView(this.getContext(), this.styledAttributes);
        targetView.setId(R.id.barcode_scanner_camera_view);
        final FrameLayout.LayoutParams prms = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, (int) rectHeightPixelsViewCoordinates);
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
                        ViewHelpersPreferences.storePreferences(getContext(), "y" + getDeviceOrientationRelativeToDeviceNaturalOrientation(), cropRect.top);
                        break;
                }

                return false;
            });
        }
    }

    /**
     * Cropping method (according to the targeting rectangle)
     */
    protected CroppedPicture extractBarcodeRectangle(byte[] frame, int length) {
        CroppedPicture res = new CroppedPicture();

        // Data characteristics
        int dataWidth = resolution.currentPreviewResolution.x;
        int dataHeight = resolution.currentPreviewResolution.y;

        // Preview size (used later to project targeting view onto the image buffer)
        int camViewMeasuredWidth = camPreviewSurfaceView.getMeasuredWidth();
        int camViewMeasuredHeight = camPreviewSurfaceView.getMeasuredHeight();

        // Sanity check
        if (frame.length < length || dataWidth * dataHeight != length) {
            // This happens when the resolution/orientation has just changed
            res.barcode = new byte[0];
            return res;
        }

        // Rotate and crop the scan area. (only keep Y in the YUV image)
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            // French (vertical) - crop & rotate

            // The rectangle is in view coordinates.
            float yRatio = (float) dataHeight / camViewMeasuredWidth;  // Photo pixels per preview surface pixel. Width because: 90° rotated.
            float xRatio = (float) dataWidth / camViewMeasuredHeight;

            // Boundaries
            int realY1 = (int) ((camViewMeasuredWidth - cropRect.right) * yRatio);
            int realY3 = (int) ((camViewMeasuredWidth - cropRect.left) * yRatio);
            int realX1 = (int) (cropRect.top * xRatio);
            int realX3 = (int) (cropRect.bottom * xRatio);

            if (realY1 < 1) {
                realY1 = 1;
            }
            if (realY3 > dataHeight) {
                realY3 = dataHeight;
            }
            if (realX1 < 0) {
                realX1 = 1;
            }
            if (realX3 > dataWidth) {
                realX3 = dataWidth;
            }

            // Cropped barcode data buffer
            res.croppedDataHeight = (1 + realX3 - realX1);
            res.croppedDataWidth = (1 + realY3 - realY1);
            if (res.croppedDataHeight * res.croppedDataWidth < 0) {
                // Ignore - Happens when the orientation has just changed and we analyze an horizontal buffer
                Log.w(TAG, "Corrupted buffer");
                res.barcode = new byte[0];
                return res;
            }
            res.barcode = getCroppedImageBuffer(res.croppedDataWidth * res.croppedDataHeight);

            // Copy and rotate the buffer
            int i = 0;
            for (int w = realX1; w <= realX3; w++) {
                for (int h = realY3; h > realY1 + 10; h--) {
                    res.barcode[i++] = frame[(h - 1) * (dataWidth) + w];
                    res.lumaSum += res.barcode[i - 1] & 0xff;
                }
            }
        } else {
            // Italian (horizontal). No need to rotate - just crop.
            float yRatio = (float) dataHeight / camViewMeasuredHeight;  // Photo pixels per preview surface pixel.
            float xRatio = (float) dataWidth / camViewMeasuredWidth;

            // Boundaries
            int realY1 = (int) (cropRect.top * yRatio);
            int realY3 = (int) (cropRect.bottom * yRatio);
            int realX1 = (int) (cropRect.left * xRatio);
            int realX3 = (int) (cropRect.right * xRatio);

            if (realY1 < 1) {
                realY1 = 1;
            }
            if (realY3 > dataHeight) {
                realY3 = dataHeight;
            }
            if (realX1 < 0) {
                realX1 = 1;
            }
            if (realX3 > dataWidth) {
                realX3 = dataWidth;
            }

            // Cropped barcode data buffer
            res.croppedDataWidth = (1 + realX3 - realX1);
            res.croppedDataHeight = (1 + realY3 - realY1);
            if (res.croppedDataHeight * res.croppedDataWidth < 0) {
                // Ignore - Happens when the orientation has just changed and we analyze a vertical buffer
                Log.w(TAG, "Corrupted buffer");
                res.barcode = new byte[0];
                return res;
            }
            res.barcode = getCroppedImageBuffer(res.croppedDataWidth * res.croppedDataHeight);

            // Copy data without rotation.
            int i = 0;
            for (int h = realY1; h <= realY3; h++) {
                for (int w = realX1; w <= realX3; w++) {
                    res.barcode[i++] = frame[h * dataWidth + w];
                    res.lumaSum += res.barcode[i - 1] & 0xff;
                }
            }
        }

        // Log.d(TAG, "Camera resolution is w*h " + dataWidth + "*" + dataHeight + " (" + dataWidth * dataHeight + "), preview pane is w*h " + camViewMeasuredWidth + "*" + camViewMeasuredHeight + " buffer is " + frame.length + " - cropped is w*h " + res.croppedDataWidth + "*" + res.croppedDataHeight + " according to " + cropRect);
        return res;
    }

    private byte[] getCroppedImageBuffer(int desiredLength) {
        byte[] res;

        while (true) {
            res = croppedImageBufferQueue.poll();
            if (res == null) {
                // Queue is empty, create a new buffer
                Log.d(TAG, "Creating new cropped buffer (MB) " + (desiredLength / 1024 / 1024));
                return new byte[desiredLength];
            }
            if (res.length != desiredLength) {
                Log.i(TAG, "Discarding old cropped buffer of length " + res.length + " (requested " + desiredLength + ")");
            } else {
                return res;
            }
        }
    }

    @Override
    public void giveBufferBack(FrameAnalysisContext<T> analysisContext) {
        croppedImageBufferQueue.add(analysisContext.croppedPicture.barcode);
        giveBufferBackInternal(analysisContext);
    }

    protected abstract void giveBufferBackInternal(FrameAnalysisContext<T> analysisContext);

    public abstract int getCameraOrientationRelativeToDeviceNaturalOrientation();

    /**
     * @return 0 if facing back, 1 if facing front.
     */
    abstract int getCameraFace();

    public int getDeviceOrientationRelativeToDeviceNaturalOrientation() {
        WindowManager wm = (WindowManager) this.getContext().getSystemService(Context.WINDOW_SERVICE);
        if (wm == null) {
            Log.w(TAG, "could not get the window manager");
            return 0;
        }
        Display display = wm.getDefaultDisplay();
        int rotation = display.getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
                return 0;
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 270;
            default:
                throw new RuntimeException("illegal orientation");
        }
    }

    public int getCameraDisplayOrientation() {
        int deviceDegrees = getDeviceOrientationRelativeToDeviceNaturalOrientation();
        int cameraDegrees = getCameraOrientationRelativeToDeviceNaturalOrientation();

        int result;
        if (getCameraFace() == 1) {
            result = (cameraDegrees + deviceDegrees) % 360;
            result = (360 - result) % 360;
        } else {
            result = (cameraDegrees - deviceDegrees + 360) % 360;
        }
        return result;
    }

    @Override
    public Point getCurrentCameraResolution() {
        return this.resolution != null && this.resolution.currentPreviewResolution != null ? this.resolution.currentPreviewResolution : null;
    }
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

}
