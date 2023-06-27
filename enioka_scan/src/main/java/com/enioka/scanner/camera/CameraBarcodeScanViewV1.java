package com.enioka.scanner.camera;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.enioka.scanner.R;

import java.util.ArrayList;
import java.util.List;

import me.dm7.barcodescanner.core.DisplayUtils;

/**
 * Helper view that encapsulates the ZBar (default) and ZXing (option) barcode analysis engines.
 * To be directly reused in layouts.
 * We are using deprecated Camera API because old Android.
 */
@SuppressWarnings({"unused"})
// Deprecation: using CameraV1. Unused: some methods only used in clients.
class CameraBarcodeScanViewV1 extends CameraBarcodeScanViewBase implements Camera.PreviewCallback, SurfaceHolder.Callback {
    private Camera cam;

    protected boolean scanningStarted = true;

    private int previewBufferSize;


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Stupid constructors
    public CameraBarcodeScanViewV1(Context context) {
        super(context);
        initOnce(context);
    }

    public CameraBarcodeScanViewV1(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        initOnce(context);
    }
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Layout and camera initialization
    public void initOnce(Context context) {
        if (!this.isInEditMode()) {
            // ZBar is a native library
            System.loadLibrary("iconv");
            reinitialiseFrameAnalyser();
        }

        // If the preview does not take all the space
        this.setBackgroundColor(Color.BLACK);

        // The view holding the preview. This will in turn (camHolder.addCallback) call setUpCamera.
        if (this.camPreviewSurfaceView == null) {
            camPreviewSurfaceView = new SurfaceView(context);
            this.addView(camPreviewSurfaceView);
        }
        camPreviewSurfaceView.getHolder().addCallback(this);
    }

    protected boolean isUsingPreviewForPhoto() {
        return this.resolution.usePreviewForPhoto;
    }

    /**
     * After this call the camera is selected, with correct parameters and open, ready to be plugged on a preview pane.
     */
    private void setUpCamera() {
        // Camera pane init
        if (this.cam != null || this.isInEditMode()) {
            return;
        }

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            throw new RuntimeException("missing use camera permission");
        }

        try {
            Log.i(TAG, "Camera is being opened. Device is " + android.os.Build.MODEL);
            this.cam = Camera.open();
        } catch (final Exception e) {
            failed = true;
            e.printStackTrace();
            new AlertDialog.Builder(getContext()).setTitle(getResources().getString(R.string.scanner_camera_open_error_title)).
                    setMessage(getResources().getString(R.string.scanner_camera_open_error)).
                    setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            throw e;
                        }
                    }).show();
            return;
        }

        if (this.cam == null) {
            failed = true;
            new AlertDialog.Builder(getContext()).setTitle(getResources().getString(R.string.scanner_camera_open_error_title)).
                    setMessage(getResources().getString(R.string.scanner_camera_no_camera)).
                    setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            throw new RuntimeException("No camera on device");
                        }
                    }).show();
            return;
        }

        reinitialiseFrameAnalyser();

        Camera.Parameters prms = this.cam.getParameters();

        // Torch mode supported?
        isTorchSupported = getSupportTorch(prms);
        if (isTorchSupported) {
            isTorchOn = getTorchOn();
        }

        // Scene mode. Try to select a mode which will ensure a high FPS rate.
        // Currently disabled, as on many devices, this disables autofocus without any way to detect it.
        List<String> supportedSceneModes = prms.getSupportedSceneModes();
        if (supportedSceneModes != null) {
            Log.d(TAG, "Supported scene modes: " + supportedSceneModes.toString());
        }
       /* if (supportedSceneModes != null && supportedSceneModes.contains(Camera.Parameters.SCENE_MODE_BARCODE)) {
            Log.i(TAG, "supportedSceneModes - scene mode barcode supported and selected");
            prms.setSceneMode(Camera.Parameters.SCENE_MODE_BARCODE);
        } else if (supportedSceneModes != null && supportedSceneModes.contains(Camera.Parameters.SCENE_MODE_ACTION)) {
            Log.i(TAG, "supportedSceneModes - scene mode SCENE_MODE_ACTION supported and selected");
            prms.setSceneMode(Camera.Parameters.SCENE_MODE_ACTION);
        } else if (supportedSceneModes != null && supportedSceneModes.contains(Camera.Parameters.SCENE_MODE_SPORTS)) { // actually same as action
            Log.i(TAG, "supportedSceneModes - scene mode SCENE_MODE_SPORTS supported and selected");
            prms.setSceneMode(Camera.Parameters.SCENE_MODE_SPORTS);
        } else if (supportedSceneModes != null && supportedSceneModes.contains(Camera.Parameters.SCENE_MODE_STEADYPHOTO)) {
            Log.i(TAG, "supportedSceneModes - scene mode SCENE_MODE_STEADYPHOTO supported and selected");
            prms.setSceneMode(Camera.Parameters.SCENE_MODE_STEADYPHOTO);
        }

        // Immediately set parameters, as the scene mode changes many parameters.
        setCameraParameters(prms);
        prms = this.cam.getParameters();*/

        // Focus & Metering areas
        setAreas(prms);

        // Exposure
        if (prms.isAutoExposureLockSupported()) {
            Log.d(TAG, "Auto exposure lock is supported and value is: " + prms.getAutoExposureLock());
            //prms.setAutoExposureLock(true);
        }
        if (prms.getMaxExposureCompensation() > 0 && prms.getMinExposureCompensation() < 0) {
            Log.i(TAG, "Exposure compensation is supported with limits [" + prms.getMinExposureCompensation() + ";" + prms.getMaxExposureCompensation() + "]");
            //prms.setExposureCompensation((prms.getMaxExposureCompensation() + prms.getMinExposureCompensation()) / 2 - 1);
            Log.i(TAG, "Exposure compensation set to " + prms.getExposureCompensation());
        } else {
            Log.i(TAG, "Exposure compensation is not supported with limits [" + prms.getMinExposureCompensation() + ";" + prms.getMaxExposureCompensation() + "]");
        }
        if (prms.getWhiteBalance() != null) {
            Log.i(TAG, "White balance is supported with modes: " + prms.getSupportedWhiteBalance() + ". Selected is: " + prms.getWhiteBalance());
            //prms.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_DAYLIGHT);
        }

        // Antibanding
        if (prms.getAntibanding() != null) {
            Log.i(TAG, "Antibanding is supported and is " + prms.getAntibanding());
            prms.setAntibanding(Camera.Parameters.ANTIBANDING_AUTO);
        }

        // Stabilization
        if (prms.isVideoStabilizationSupported()) {
            Log.i(TAG, "Video stabilization is supported and will be set to true - currently is: " + prms.getVideoStabilization());
            prms.setVideoStabilization(true);
        } else {
            Log.i(TAG, "Video stabilization is not supported");
        }

        // A YUV format. NV21 is always available, no need to check if it is supported
        prms.setPreviewFormat(ImageFormat.NV21);
        resolution.bytesPerPixel = ImageFormat.getBitsPerPixel(prms.getPreviewFormat()) / 8f;

        // Set focus mode to FOCUS_MODE_CONTINUOUS_PICTURE if supported
        List<String> supportedFocusModes = prms.getSupportedFocusModes();
        Log.d(TAG, "Supported focus modes: " + supportedFocusModes.toString());
        if (supportedFocusModes.contains("mw_continuous-picture")) {
            prms.setFocusMode("mw_continuous-picture");
            Log.i(TAG, "supportedFocusModes - mw_continuous-picture supported and selected");
        } else if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            prms.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            Log.i(TAG, "supportedFocusModes - continuous-picture supported and selected");
        } else if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            prms.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            Log.i(TAG, "supportedFocusModes - auto supported and selected");
        } else if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_MACRO)) {
            prms.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
            Log.i(TAG, "supportedFocusModes - macro supported and selected");
        } else {
            Log.i(TAG, "no autofocus supported");
        }

        // Set flash mode to torch if supported
        if (isTorchOn) {
            prms.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        } else {
            prms.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        }

        //////////////////////////////////////
        // Preview Resolution
        setPreviewResolution(prms);
        setInitialBuffers(prms);

        //////////////////////////////////////
        // Picture resolution
        setPictureResolution(prms);

        //////////////////////////////////////
        // Perf hacks

        // We are using video...
        //prms.setRecordingHint(true);

        // We need to best frame rate available
        int[] bestPreviewFpsRange = new int[]{0, 0};
        Log.d(TAG, "Supported FPS ranges:");
        for (int[] fpsRange : prms.getSupportedPreviewFpsRange()) {
            Log.d(TAG, "\t" + fpsRange[0] + "," + fpsRange[1]);
            if (fpsRange[0] >= bestPreviewFpsRange[0] && fpsRange[1] >= bestPreviewFpsRange[1] && fpsRange[0] * 1.5 > fpsRange[1]) {
                bestPreviewFpsRange = fpsRange;
            }
        }
        if (bestPreviewFpsRange[1] > 0) {
            prms.setPreviewFpsRange(bestPreviewFpsRange[0], bestPreviewFpsRange[1]);
            Log.i(TAG, "Requesting preview FPS range at " + bestPreviewFpsRange[0] + "," + bestPreviewFpsRange[1]);
        }


        //////////////////////////////////////
        // Camera prms done
        setCameraParameters(prms);

        this.cam.setDisplayOrientation(getCameraDisplayOrientation());
    }

    /**
     * Sets all variables related to camera preview size.
     *
     * @param prms camera to set.
     */
    private void setPreviewResolution(Camera.Parameters prms) {
        List<Camera.Size> rezs = prms.getSupportedPreviewSizes();
        List<Point> resolutions = new ArrayList<>(rezs.size());
        for (Camera.Size size : rezs) {
            resolution.supportedPreviewResolutions.add(new Point(size.width, size.height));
        }

        // Resolution selection
        ViewHelpersResolution.setPreviewResolution(getContext(), resolution, this.camPreviewSurfaceView);

        // We now have a preview resolution for sure. (exception otherwise)

        // COMPAT HACKS
        resolution.usePreviewForPhoto = false;
        switch (android.os.Build.MODEL) {
            case "LG-H340n":
                resolution.currentPreviewResolution = new Point(1600, 1200);
                resolution.useAdaptiveResolution = false;
                resolution.usePreviewForPhoto = true;
                Log.i(TAG, "LG-H340n specific - using hard-coded preview resolution" + prms.getPreviewSize().width + "*" + prms.getPreviewSize().height + ". Ratio is " + ((float) prms.getPreviewSize().width / prms.getPreviewSize().height));
                break;
            case "SPA43LTE":
                resolution.currentPreviewResolution = new Point(1280, 720);
                resolution.useAdaptiveResolution = false;
                resolution.usePreviewForPhoto = true;
                Log.i(TAG, "SPA43LTE specific - using hard-coded preview resolution " + prms.getPreviewSize().width + "*" + prms.getPreviewSize().height + ". Ratio is " + ((float) prms.getPreviewSize().width / prms.getPreviewSize().height));
                break;
            case "Archos Sense 50X":
                resolution.currentPreviewResolution = new Point(1280, 720);
                resolution.useAdaptiveResolution = false;
                resolution.usePreviewForPhoto = true;
                Log.i(TAG, "Archos Sense 50X specific - using hard-coded preview resolution " + prms.getPreviewSize().width + "*" + prms.getPreviewSize().height + ". Ratio is " + ((float) prms.getPreviewSize().width / prms.getPreviewSize().height));
                break;
            default:
                Log.i(TAG, "Using preview resolution " + resolution.currentPreviewResolution.x + "*" +
                        resolution.currentPreviewResolution.y + ". Ratio is " +
                        ((float) resolution.currentPreviewResolution.x / ((float) resolution.currentPreviewResolution.y)));
                resolution.usePreviewForPhoto = resolution.currentPreviewResolution.y >= 1080;
        }

        // Set a denormalized field - this is used widely in the class.
        prms.setPreviewSize(resolution.currentPreviewResolution.x, resolution.currentPreviewResolution.y);
    }

    private void setPictureResolution(Camera.Parameters prms) {
        List<Camera.Size> rezs = prms.getSupportedPictureSizes();
        List<Point> resolutions = new ArrayList<>(rezs.size());
        for (Camera.Size size : rezs) {
            resolution.supportedPhotoResolutions.add(new Point(size.width, size.height));
        }
        ViewHelpersResolution.setPictureResolution(resolution);
        prms.setPictureSize(resolution.currentPhotoResolution.x, resolution.currentPhotoResolution.y);
    }

    private void setAreas(Camera.Parameters prms) {
        // Metering areas are relative to -1000,-1000 -> 1000,1000 rectangle.
        // Translate the targeting rectangle to this coordinate system.
        int top = cropRect.top; // layoutPrms.topMargin;
        int bottom = cropRect.bottom; // layoutPrms.height + top;
        int height = this.getMeasuredHeight();
        Rect target = new Rect(-800, (int) ((float) top / height * 2000) - 1000, 800, (int) ((float) bottom / height * 2000) - 1000);

        // Metering area: for aperture computations.
        if (prms.getMaxNumMeteringAreas() > 0) {
            List<Camera.Area> areas = new ArrayList<>();
            areas.add(new Camera.Area(target, 1000));
            prms.setMeteringAreas(areas);
            Log.i(TAG, "Using a central metering area: " + target);
        } else {
            Log.i(TAG, "No specific metering area available");
        }

        // Focus area
        int nbAreas = prms.getMaxNumFocusAreas();
        Log.d(TAG, "Camera supports " + nbAreas + " focus areas");
        if (nbAreas > 0) {
            List<Camera.Area> areas = new ArrayList<>();
            areas.add(new Camera.Area(target, 1000));
            prms.setFocusAreas(areas);
            Log.i(TAG, "Using a central focus area: " + target);
        } else {
            Log.i(TAG, "No focus area available");
        }
    }

    public void setPreviewResolution(Point newResolution) {
        Camera.Parameters prms = this.cam.getParameters();
        pauseCamera();
        resolution.currentPreviewResolution = newResolution;
        prms.setPreviewSize(newResolution.x, newResolution.y);
        setCameraParameters(prms);
        setInitialBuffers(prms);
        resumeCamera();
    }

    private void setInitialBuffers(Camera.Parameters prms) {
        previewBufferSize = (int) (resolution.currentPreviewResolution.x * resolution.currentPreviewResolution.y * ImageFormat.getBitsPerPixel(prms.getPreviewFormat()) / 8f);
        for (int i = 0; i < Runtime.getRuntime().availableProcessors() * 2; i++) {
            this.cam.addCallbackBuffer(new byte[previewBufferSize]);
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Torch

    /**
     * Indicate if the torch mode is handled or not
     *
     * @param prms Instance of camera configuration
     * @return A true value if the torch mode supported, false otherwise
     */
    private boolean getSupportTorch(Camera.Parameters prms) {
        List<String> supportedFlashModes = prms.getSupportedFlashModes();
        if (supportedFlashModes.contains(Camera.Parameters.FLASH_MODE_TORCH)) {
            Log.d(TAG, "supportedFlashModes - torch supported");
            return true;
        }
        return false;
    }


    /**
     * @return true if torch is on
     */
    @Override
    public boolean getTorchOn() {
        if (failed) {
            return false;
        }
        if (this.cam == null && !this.isInEditMode()) {
            return false;
        }
        Camera.Parameters prms = this.cam.getParameters();
        return prms.getFlashMode().equals(Camera.Parameters.FLASH_MODE_TORCH);
    }


    @Override
    void setTorchInternal(boolean value) {
        Camera.Parameters prms = this.cam.getParameters();
        if (value) {
            prms.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        } else {
            prms.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        }
        setCameraParameters(prms);
    }

    // torch
    ////////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Misc

    /**
     * Apply given parameters to the camera
     *
     * @param prms Instance of camera configuration to apply
     */
    public void setCameraParameters(Camera.Parameters prms) {
        try {
            this.cam.setParameters(prms);
        } catch (Exception e) {
            throw new RuntimeException(TAG + "- SetUpCamera : Could not set camera parameters ", e);
        }
    }
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Reactions to preview pane being created/modified/destroyed.
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        computeCropRectangle();
        if (this.cam == null) {
            setUpCamera();
            surfaceHolder.setFixedSize(resolution.currentPreviewResolution.x, resolution.currentPreviewResolution.y);
        }

        try {
            this.cam.setPreviewDisplay(surfaceHolder);
            this.cam.startPreview();
            if (scanningStarted) {
                this.cam.setPreviewCallbackWithBuffer(this);
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not start camera preview and preview data analysis", e);
        }
        if (this.targetView == null) {
            addTargetView();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // TODO: check if this can happen.
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        cleanUp();
    }
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Frame & barcode analysis

    // THE main method.
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (!scanningStarted || data == null || data.length == 0) {
            return;
        }

        FrameAnalysisContext ctx = new FrameAnalysisContext();
        ctx.frame = data;
        ctx.cameraHeight = camera.getParameters().getPreviewSize().height;
        ctx.cameraWidth = camera.getParameters().getPreviewSize().width;
        ctx.camViewMeasuredHeight = this.camPreviewSurfaceView.getMeasuredHeight();
        ctx.camViewMeasuredWidth = this.camPreviewSurfaceView.getMeasuredWidth();
        ctx.vertical = DisplayUtils.getScreenOrientation(this.getContext()) == 1;
        ctx.x1 = cropRect.left;
        ctx.x2 = cropRect.right;
        ctx.x3 = ctx.x2;
        ctx.x4 = ctx.x1;
        ctx.y1 = cropRect.top;
        ctx.y2 = ctx.y1;
        ctx.y3 = cropRect.bottom;
        ctx.y4 = ctx.y3;
        frameAnalyser.handleFrame(ctx);
    }


    public void giveBufferBack(FrameAnalysisContext ctx) {
        if (ctx.frame.length == previewBufferSize) {
            this.cam.addCallbackBuffer(ctx.frame);
        }
    }
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Lifecycle external toggles
    public void pauseCamera() {
        if (this.cam != null) {
            this.cam.setPreviewCallbackWithBuffer(null);
            this.cam.stopPreview();
        }
    }

    public void resumeCamera() {
        if (this.cam != null) {
            this.cam.startPreview();
            if (scanningStarted) {
                this.cam.setPreviewCallback(this);
            }
        }
    }

    public void startScanner() {
        Log.d(VIEW_LOG_TAG, "Scanning started");
        this.scanningStarted = true;
        this.cam.setPreviewCallback(this);
    }

    public void pauseScanner() {
        Log.d(VIEW_LOG_TAG, "Scanning stopped");
        this.scanningStarted = false;
    }
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Clean up methods
    public void cleanUp() {
        if (this.cam != null) {
            Log.i(TAG, "Removing all camera callbacks and stopping it");
            this.cam.setPreviewCallback(null);
            this.cam.stopPreview();
            this.setOnClickListener(null);
            this.lastPreviewData = null;
            this.frameAnalyser.close();
            this.frameAnalyser = null;
        }
        closeCamera();
    }

    void closeCamera() {
        if (this.cam == null) {
            return;
        }
        Log.i(TAG, "Camera is being released");
        this.cam.release();
        this.cam = null;
    }

    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Helpers
    public int getCameraDisplayOrientation() {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(0, info);
        WindowManager wm = (WindowManager) this.getContext().getSystemService(Context.WINDOW_SERVICE);
        if (wm == null) {
            Log.w(TAG, "could not get the window manager");
            return 0;
        }
        Display display = wm.getDefaultDisplay();
        int rotation = display.getRotation();
        short degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;
        } else {
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }


    //
    ////////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Camera as a camera!
    public void takePicture(final Camera.PictureCallback callback) {
        if (resolution.usePreviewForPhoto && lastPreviewData != null) {
            Log.d(TAG, "Picture from preview");
            final Camera camera = this.cam;
            new ConvertPreviewAsync(lastPreviewData, resolution.currentPreviewResolution, new ConvertPreviewAsync.Callback() {
                @Override
                public void onDone(byte[] jpeg) {
                    callback.onPictureTaken(jpeg, camera);
                }
            }).execute();
        } else {
            Log.d(TAG, "Picture from camera");
            this.cam.takePicture(null, null, callback);
        }
    }
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////
}
