package com.enioka.scanner.demo.slip;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.enioka.scanner.camera.ZbarScanView;

/**
 * Build a view for packing slip scanning
 */
@SuppressWarnings("deprecation")
public class SlipScanView extends ZbarScanView implements Camera.PictureCallback {

    private static final String TAG = "SLIP";

    private PictureHandler pictureHandler;
    private Rect frame = new Rect();

    /**
     * Create an new instance of SlipScanView
     *
     * @param context instance of current context
     */
    public SlipScanView(Context context) {
        super(context);
        scanningStarted = false;
        this.setAllowTargetDrag(false);
        initOnce();
    }

    /**
     * Create an new instance of SlipScanView
     *
     * @param context      instance of current context
     * @param attributeSet instance of attrbuteset
     */
    public SlipScanView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        scanningStarted = false;
        this.setAllowTargetDrag(false);
        initOnce();
    }

    /**
     * Initialize objects
     */
    private void initOnce() {

    }

    @Override
    protected void addTargetView() {
        final View targetView = new TargetView(this.getContext(), cropRect, frame);
        targetView.setId(com.enioka.scanner.R.id.barcode_scanner_camera_view);
        final LayoutParams prms = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        //prms.setMargins(0, cropRect.top, 0, 0);

        Log.i(TAG, "Targeting overlay added");
        this.addView(targetView, prms);
    }

    /**
     * Calculate the size of the frame and modify the size of the parent hud
     */
    @Override
    protected void computeCropRectangle() {
        super.computeCropRectangle();

        int actualLayoutWidth, actualLayoutHeight;
        if (this.isInEditMode()) {
            actualLayoutWidth = this.getMeasuredWidth();
            actualLayoutHeight = this.getMeasuredHeight();
        } else {
            actualLayoutWidth = this.camView.getMeasuredWidth();
            actualLayoutHeight = this.camView.getMeasuredHeight();
        }

        // Red "barcode" rectangle
        cropRect.top = (int) (actualLayoutHeight / 2 - RECT_HEIGHT / 2 / MM_INSIDE_INCH * ydpi);
        cropRect.bottom = (int) (actualLayoutHeight / 2 + RECT_HEIGHT / 2 / MM_INSIDE_INCH * ydpi);
        cropRect.left = (int) (actualLayoutWidth * 0.3);
        cropRect.right = (int) (actualLayoutWidth * 0.7);

        // Green "whole slip" rectangle
        int rectHeight = Math.round(actualLayoutWidth * 0.4f); // 0.4 is the packing slip ratio
        frame.left = 0;
        frame.right = actualLayoutWidth;
        frame.top = (int) (actualLayoutHeight / 2f - rectHeight / 2f);
        frame.bottom = (int) (actualLayoutHeight / 2f + rectHeight / 2f);

        Log.i(TAG, "Setting targeting rect 1 at " + cropRect);
        Log.i(TAG, "Setting targeting rect 2 at " + frame);
    }

    /**
     * Trigger an picture by the camera in JPEG format
     */
    public void takePicture() {
        this.resumeCamera();
        this.takePicture(this);
    }

    /**
     * Called when image data is available after a picture is taken.
     * The format of the data depends on the context of the callback
     * and {@link Camera.Parameters} settings.
     *
     * @param data   a byte array of the picture data
     * @param camera the Camera service object
     */
    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        Camera.Size pictureSize;
        Camera.Parameters prms = camera.getParameters();

        if (this.isUsingPreviewForPhoto()) {
            pictureSize = prms.getPreviewSize();
        } else {
            pictureSize = prms.getPictureSize();
        }

        pauseCamera();
        pictureHandler.onPictureTaken(data, pictureSize.width, pictureSize.height);
    }

    /**
     * Return the frame adjusted with the picture size
     *
     * @param width  Picture width to map
     * @param height Picture height to map
     * @return rectangle shape of the preview frame
     */
    public Rect getAdjustedFrameBounds(int width, int height) {
        float yRatio = (float) height / (float) this.camView.getMeasuredHeight();  // Photo pixels per preview surface pixel.
        int realY1 = (int) (frame.top * yRatio);
        int realY3 = (int) (frame.bottom * yRatio);
        return new Rect(0, realY1, width, realY3);
    }

    /**
     * Return the frame adjusted with the picture size
     *
     * @param width  Picture width to map
     * @param height Picture height to map
     * @return rectangle shape of the preview frame
     */
    public Rect getAdjustedScannerBounds(int width, int height) {
        float yratio = (float) height / (float) this.camView.getMeasuredHeight();  // Photo pixels per preview surface pixel.
        float xratio = (float) width / (float) this.camView.getMeasuredWidth();
        int realy1 = (int) (cropRect.top * yratio);
        int realy3 = (int) (cropRect.bottom * yratio);
        int realx1 = (int) (cropRect.left * xratio);
        int realx3 = (int) (cropRect.right * xratio);
        return new Rect(realx1, realy1, realx3, realy3);
    }

    public void setPictureHandler(PictureHandler handler) {
        this.pictureHandler = handler;
    }

    public interface PictureHandler {
        boolean onPictureTaken(byte[] result, int width, int height);
    }
}
