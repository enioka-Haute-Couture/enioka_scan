package com.enioka.scanner.camera;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.enioka.scanner.R;

import net.sourceforge.zbar.ImageScanner;

import java.util.ArrayList;
import java.util.List;

import me.dm7.barcodescanner.core.DisplayUtils;

/**
 * Helper view that encapsulates the ZBar barcode analysis engine.
 * To be directly reused in layouts.
 * We are using deprecated Camera API because old Android.
 */
@SuppressWarnings({"deprecation", "unused"})
// Deprecation: using CameraV1. Unused: some methods only used in clients.
public class ZbarScanView extends FrameLayout implements Camera.PreviewCallback, SurfaceHolder.Callback {
    protected static final int RECT_HEIGHT = 10;
    protected static final float MM_INSIDE_INCH = 25.4f;
    private static final String TAG = "BARCODE";

    private static final int MS_SINCE_LAST = 1000;

    private Camera cam;
    protected SurfaceView camView;
    private SurfaceHolder camHolder;
    private ImageScanner scanner;
    private ResultHandler handler;
    protected Paint targetRectPaint, guideLinePaint, filterOutPaint, autoFocusPaint;
    protected int x1, y1, x2, y2, x3, y3, x4, y4; // 1 = top left, 2 = top right, 3 = bottom right, 4 = bottom left.
    private float camResRatio;
    private boolean manualAutoFocus = false;
    private boolean hasExposureCompensation = false;
    private boolean torchOn = false;
    private ZbarScanViewOverlay overlay;
    protected boolean scanningStarted = true;
    private boolean failed = false;

    private boolean usePreviewForPicture = true;
    boolean allowTargetDrag = true;
    private byte[] lastPreviewData;
    private Camera.Size previewSize;

    private FrameAnalyserManager frameAnalyser;


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Stupid constructors
    public ZbarScanView(Context context) {
        super(context);
        initOnce(context);
    }

    public ZbarScanView(Context context, AttributeSet attributeSet) {
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
            frameAnalyser = new FrameAnalyserManager(this);
        }

        // Target rectangle paint
        targetRectPaint = new Paint();
        targetRectPaint.setColor(Color.RED);
        targetRectPaint.setStrokeWidth(5);
        targetRectPaint.setStyle(Paint.Style.STROKE);

        autoFocusPaint = new Paint();
        autoFocusPaint.setColor(Color.RED);
        autoFocusPaint.setStrokeWidth(0);
        autoFocusPaint.setAlpha(126);
        autoFocusPaint.setStyle(Paint.Style.FILL);


        guideLinePaint = new Paint();
        guideLinePaint.setColor(Color.RED);
        guideLinePaint.setStyle(Paint.Style.STROKE);
        guideLinePaint.setPathEffect(new DashPathEffect(new float[]{5, 10, 15, 20}, 0));

        filterOutPaint = new Paint();
        filterOutPaint.setColor(0x60000000);

        // Take photo on click instead of continuous scan
        // this.setOnClickListener(this);

        // If the preview does not take all the space
        this.setBackgroundColor(Color.BLACK);

        // Then repeatable inits
        setUpCamera(context);
    }

    /**
     * Default is simply CODE_128. Use the Symbol static fields to specify a symbology.
     *
     * @param s the ID of the symbology (ZBAR coding)
     */
    public void addSymbology(int s) {
        //this.scanner.setConfig(s, 0, 1);
    }

    public void initRect() {
        // Target rectangle dimensions
        DisplayMetrics metrics = this.getContext().getResources().getDisplayMetrics();
        //float xdpi = metrics.xdpi;
        float ydpi = metrics.ydpi;

        int actualLayoutWidth, actualLayoutHeight;
        if (this.isInEditMode()) {
            actualLayoutWidth = this.getMeasuredWidth();
            actualLayoutHeight = this.getMeasuredHeight();
        } else {
            actualLayoutWidth = this.camView.getMeasuredWidth();
            actualLayoutHeight = this.camView.getMeasuredHeight();
        }

        if (!this.isInEditMode()) {
            x1 = (int) (actualLayoutWidth * 0.1) + (int) this.camView.getX();
            x2 = (int) (actualLayoutWidth * 0.9) + (int) this.camView.getX();
        } else {
            x1 = (int) (actualLayoutWidth * 0.1) + (int) this.getX();
            x2 = (int) (actualLayoutWidth * 0.9) + (int) this.getX();
        }
        x3 = x2;
        x4 = x1;

        y1 = (int) (actualLayoutHeight / 2 - RECT_HEIGHT / 2 / MM_INSIDE_INCH * ydpi);
        y2 = y1;

        y3 = (int) (actualLayoutHeight / 2 + RECT_HEIGHT / 2 / MM_INSIDE_INCH * ydpi);
        y4 = y3;

        Log.i(TAG, "x1 " + x1 + " - y1 " + y1 + " - x3 " + x3 + " - y3 " + y3);
    }


    public void setUpCamera(Context context) {
        // Camera pane init
        if (this.cam == null && !this.isInEditMode()) {
            try {
                Log.i(TAG, "Camera is being opened. Device is " + android.os.Build.MODEL);
                this.cam = Camera.open();
            } catch (final Exception e) {
                failed = true;
                new AlertDialog.Builder(context).setTitle(getResources().getString(R.string.scanner_zbar_open_error_title)).
                        setMessage(getResources().getString(R.string.scanner_zbar_open_error)).
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
                new AlertDialog.Builder(context).setTitle(getResources().getString(R.string.scanner_zbar_open_error_title)).
                        setMessage(getResources().getString(R.string.scanner_zbar_no_camera)).
                        setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                throw new RuntimeException("No camera on device");
                            }
                        }).show();
                return;
            }

            Camera.Parameters prms = this.cam.getParameters();

            // Focus
            int nbAreas = prms.getMaxNumFocusAreas();
            Log.d(TAG, "Camera supports " + nbAreas + " focus areas");
            if (nbAreas > 0) {
                List<Camera.Area> areas = new ArrayList<>();
                areas.add(new Camera.Area(new Rect(-800, -100, 800, 100), 1000));
                prms.setFocusAreas(areas);
                Log.i(TAG, "Using a central focus area");
            } else {
                Log.i(TAG, "No focus area used");
            }

            // Metering areas
            if (prms.getMaxNumMeteringAreas() > 0) {
                List<Camera.Area> areas = new ArrayList<>();
                areas.add(new Camera.Area(new Rect(-800, -50, 800, 50), 1000));
                prms.setMeteringAreas(areas);
                Log.i(TAG, "Using a central metering area");
            } else {
                Log.i(TAG, "No specific metering area available");
            }

            // Exposure
            if (prms.isAutoExposureLockSupported()) {
                Log.d(TAG, "Auto exposure lock is supported and value is: " + prms.getAutoExposureLock());
                //prms.setAutoExposureLock(true);
            }
            if (prms.getMaxExposureCompensation() > 0 && prms.getMinExposureCompensation() < 0) {
                Log.i(TAG, "Exposure compensation is supported with limits [" + prms.getMinExposureCompensation() + ";" + prms.getMaxExposureCompensation() + "]");
                hasExposureCompensation = true;
                prms.setExposureCompensation((prms.getMaxExposureCompensation() + prms.getMinExposureCompensation()) / 2 - 1);
                Log.i(TAG, "Exposure compensation set to " + prms.getExposureCompensation());
            } else {
                Log.i(TAG, "Exposure compensation is not supported with limits [" + prms.getMinExposureCompensation() + ";" + prms.getMaxExposureCompensation() + "]");
            }
            if (prms.getWhiteBalance() != null) {
                Log.i(TAG, "white balance is supported with modes: " + prms.getSupportedWhiteBalance() + ". Selected is: " + prms.getWhiteBalance());
                //prms.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_DAYLIGHT);
            }

            // Antibanding
            if (prms.getAntibanding() != null) {
                Log.i(TAG, "Antibanding is supported and is " + prms.getAntibanding());
                //prms.setAntibanding(Camera.Parameters.ANTIBANDING_OFF);
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
                manualAutoFocus = true;
            } else if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_MACRO)) {
                prms.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
                Log.i(TAG, "supportedFocusModes - macro supported and selected");
            } else {
                Log.i(TAG, "no autofocus supported");
            }

            // Scene mode
            List<String> supportedSceneModes = prms.getSupportedSceneModes();
            if (supportedSceneModes != null && supportedSceneModes.contains(Camera.Parameters.SCENE_MODE_BARCODE)) {
                Log.i(TAG, "supportedSceneModes - scene mode barcode supported and selected");
                prms.setSceneMode(Camera.Parameters.SCENE_MODE_BARCODE);
            } else if (supportedSceneModes != null && supportedSceneModes.contains(Camera.Parameters.SCENE_MODE_STEADYPHOTO)) {
                Log.i(TAG, "supportedSceneModes - scene mode SCENE_MODE_STEADYPHOTO supported and selected");
                prms.setSceneMode(Camera.Parameters.SCENE_MODE_STEADYPHOTO);
            }

            // Set flash mode to torch if supported
            setTorch(prms, torchOn);

            //////////////////////////////////////
            // Preview Resolution
            List<Camera.Size> rezs = prms.getSupportedPreviewSizes();
            Camera.Size prevSize = prms.getPreferredPreviewSizeForVideo();
            // Prefer 4/3 ratio is portrait mode, 16/9 in landscape.
            float preferredRatio = DisplayUtils.getScreenOrientation(this.getContext()) == 1 ? 4f / 3f : 16f / 9f;
            Log.i(TAG, "Looking for the ideal preview resolution. View ratio is " + preferredRatio);
            boolean goodMatchFound = false;

            // Simple debug display
            for (Camera.Size s : rezs) {
                Log.d(TAG, "\tsupports preview resolution " + s.width + "*" + s.height + " - " + ((float) s.width / (float) s.height));
            }

            if (prevSize == null || prevSize.width < 1024 || (float) prevSize.width / (float) prevSize.height - preferredRatio > 0.1f) {
                // First try with only the preferred ratio.
                for (Camera.Size s : rezs) {
                    if (s.width <= 1980 && s.height <= 1080 && (prevSize == null || (s.width > prevSize.width) && Math.abs((float) s.width / (float) s.height - preferredRatio) < 0.1f)) {
                        prevSize = s;
                        goodMatchFound = true;
                    }
                }

                // If not found, try with any ratio.
                if (!goodMatchFound) {
                    for (Camera.Size s : rezs) {
                        if (s.width <= 1980 && s.height <= 1080 && (prevSize == null || (s.width > prevSize.width))) {
                            prevSize = s;
                        }
                    }
                }
            }
            if (prevSize != null) {
                prms.setPreviewSize(prevSize.width, prevSize.height);
            } else {
                throw new RuntimeException("no suitable preview resolution");
            }

            // COMPAT HACKS
            this.usePreviewForPicture = false;
            switch (android.os.Build.MODEL) {
                case "LG-H340n":
                    prms.setPreviewSize(1600, 1200);
                    Log.i(TAG, "LG-H340n specific - using hard-coded preview resolution" + prms.getPreviewSize().width + "*" + prms.getPreviewSize().height + ". Ratio is " + ((float) prms.getPreviewSize().width / prms.getPreviewSize().height) + " (requested ratio was " + preferredRatio + ")");
                    break;
                case "SPA43LTE":
                    if (preferredRatio < 1.5) {
                        prms.setPreviewSize(1440, 1080); // Actual working max, 1.33 ratio. No higher rez works.
                    } else {
                        prms.setPreviewSize(1280, 720);
                    }
                    this.usePreviewForPicture = true;
                    Log.i(TAG, "SPA43LTE specific - using hard-coded preview resolution " + prms.getPreviewSize().width + "*" + prms.getPreviewSize().height + ". Ratio is " + ((float) prms.getPreviewSize().width / prms.getPreviewSize().height));
                    break;
                case "Archos Sense 50X":
                    if (preferredRatio < 1.5) {
                        //prms.setPreviewSize(800, 600);
                        prms.setPreviewSize(1440, 1080);
                    } else {
                        prms.setPreviewSize(1280, 720);
                    }
                    this.usePreviewForPicture = true;
                    Log.i(TAG, "Archos Sense 50X specific - using hard-coded preview resolution " + prms.getPreviewSize().width + "*" + prms.getPreviewSize().height + ". Ratio is " + ((float) prms.getPreviewSize().width / prms.getPreviewSize().height));
                    break;
                default:
                    Log.i(TAG, "Using preview resolution " + prevSize.width + "*" + prevSize.height + ". Ratio is " + ((float) prevSize.width / (float) prevSize.height));
                    this.usePreviewForPicture = prevSize.height >= 1080;
            }
            this.previewSize = prms.getPreviewSize();


            //////////////////////////////////////
            // Picture resolution

            // A preview resolution is often a picture resolution, so start with this.
            // Then, look for any higher resolution with the same ratio
            Log.i(TAG, "Looking for the ideal photo resolution. View ratio is " + preferredRatio);
            List<Camera.Size> sizes = prms.getSupportedPictureSizes();
            Camera.Size pictureSize = null, smallestSize = sizes.get(0), betterChoiceWrongRatio = null;
            boolean foundWithGoodRatio = false;

            for (Camera.Size s : sizes) {
                // Is preview resolution a picture resolution?
                if (s.width == prevSize.width && s.height == prevSize.height) {
                    pictureSize = s;
                    foundWithGoodRatio = true;
                }
                if (s.width < smallestSize.width) {
                    smallestSize = s;
                }
            }
            if (pictureSize == null) {
                pictureSize = smallestSize;
            }

            for (Camera.Size size : sizes) {
                Log.d(TAG, "\tsupports picture resolution " + size.width + "*" + size.height + " - " + ((float) size.width / (float) size.height));
                if (Math.abs((float) size.width / (float) size.height - preferredRatio) < 0.1f && size.width > pictureSize.width && size.width <= 2560 && size.height <= 1536) {
                    pictureSize = size;
                    foundWithGoodRatio = true;
                }
                if (size.width > pictureSize.width && size.width <= 2560 && size.height <= 1536) {
                    betterChoiceWrongRatio = size;
                }
            }
            if (!foundWithGoodRatio) {
                Log.d(TAG, "Could not find a photo resolution with requested ratio " + preferredRatio + ". Going with wrong ratio resolution");
                pictureSize = betterChoiceWrongRatio;
            }
            if (pictureSize == null) {
                throw new RuntimeException("no suitable photo resolution");
            }

            prms.setPictureSize(pictureSize.width, pictureSize.height);
            camResRatio = (float) prms.getPictureSize().width / (float) prms.getPictureSize().height;
            Log.i(TAG, "Using picture resolution " + pictureSize.width + "*" + pictureSize.height + ". Ratio is " + camResRatio + ". (Preferred ratio was " + preferredRatio + ")");


            //////////////////////////////////////
            // Perf hacks

            // We are using video...
            prms.setRecordingHint(true);

            // We need to best frame rate available
            int[] bestPreviewFpsRange = new int[]{0, 0};
            for (int[] fpsRange : prms.getSupportedPreviewFpsRange()) {
                if (fpsRange[0] >= bestPreviewFpsRange[0] && fpsRange[1] >= bestPreviewFpsRange[1]) {
                    bestPreviewFpsRange = fpsRange;
                }
            }
            if (bestPreviewFpsRange[1] > 0) {
                prms.setPreviewFpsRange(bestPreviewFpsRange[0], bestPreviewFpsRange[1]);
                Log.i(TAG, "Requesting preview FPS range at " + bestPreviewFpsRange[0] + "," + bestPreviewFpsRange[1]);
            }

            // Probably useless: don't use colors, as camera color interpolation destroys precision.
            prms.setColorEffect(Camera.Parameters.EFFECT_MONO);


            //////////////////////////////////////
            // Camera prms done
            setCameraParameters(prms);

            this.cam.setDisplayOrientation(getCameraDisplayOrientation());

            // The view holding the preview
            if (this.camView == null) {
                camView = new SurfaceView(context);
                this.addView(camView);
            }
            this.camHolder = camView.getHolder();
            this.camHolder.addCallback(this);
        }

        // Add the overlay upon the camera
        overlay = new ZbarScanViewOverlay(context, this);
        this.addView(overlay);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (this.cam == null && this.camView != null && !this.isInEditMode()) {
            int idealWidth = (int) (h / camResRatio);
            FrameLayout.LayoutParams ly = (FrameLayout.LayoutParams) camView.getLayoutParams();
            ly.width = idealWidth;
            ly.height = ViewGroup.LayoutParams.MATCH_PARENT;
            ly.gravity = Gravity.CENTER;
            camView.setLayoutParams(ly);
        }
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility == VISIBLE && this.cam != null) {
            this.cam.cancelAutoFocus();
        }
    }

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
     * Indicate if the torch mode is handled or not
     *
     * @return A true value if the torch mode supported, false otherwise
     */
    public boolean getSupportTorch() {
        if (failed) {
            return false;
        }
        if (this.cam == null && !this.isInEditMode()) {
            this.cam = Camera.open();
        }
        Camera.Parameters prms = this.cam.getParameters();
        return getSupportTorch(prms);
    }


    public boolean getTorchOn() {
        if (failed) {
            return false;
        }
        if (this.cam == null && !this.isInEditMode()) {
            this.cam = Camera.open();
        }
        Camera.Parameters prms = this.cam.getParameters();
        return prms.getFlashMode().equals(Camera.Parameters.FLASH_MODE_TORCH);
    }

    /**
     * Switch on or switch off the torch mode, but parameters are not applied
     *
     * @param prms  Instance of camera configuration
     * @param value indicate if the torch mode must be switched on (true) or off (false)
     */
    private void setTorch(Camera.Parameters prms, boolean value) {
        this.torchOn = value;
        boolean support = getSupportTorch(prms);
        if (support && value) {
            prms.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            if (this.hasExposureCompensation) {
                prms.setExposureCompensation(prms.getMinExposureCompensation() + 1);
            }
        } else if (support) {
            prms.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            if (this.hasExposureCompensation) {
                prms.setExposureCompensation((prms.getMaxExposureCompensation() + prms.getMinExposureCompensation()) / 2 - 1);
            }
        }
    }

    /**
     * Switch on or switch off the torch mode
     *
     * @param value indicate if the torch mode must be switched on (true) or off (false)
     */
    public void setTorch(boolean value) {
        if (failed) {
            return;
        }
        if (this.cam == null && !this.isInEditMode()) {
            this.cam = Camera.open(); // debug only
        }
        Camera.Parameters prms = this.cam.getParameters();
        setTorch(prms, value);
        setCameraParameters(prms);
    }

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

    /**
     * Force the camera to perform an autofocus
     */
    public void triggerAutoFocus() {
        if (manualAutoFocus) {
            final ZbarScanView t = this;
            overlay.invalidate();
            try {
                this.cam.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        Log.v(TAG, "onAutoFocus: done");
                        overlay.invalidate();
                        try {
                            if (scanningStarted) {
                                camera.setPreviewCallback(t);
                            }
                        } catch (RuntimeException e) {
                            // Can happen when the camera was released before this callback happens. Not an issue - the scan has already succeeded.
                        }
                    }
                });
            } catch (Exception e) {
                // Autofocus may fail from time to time (especially if called too soon)
            }
        }
    }
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Reactions to preview pane being created/modified/destroyed.
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (this.cam == null) {
            setUpCamera(this.getContext());
        }

        try {
            this.cam.setPreviewDisplay(this.camHolder);
            this.cam.startPreview();
            if (scanningStarted) {
                this.cam.setPreviewCallback(this);
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not start camera", e);
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
        if (!scanningStarted) {
            return;
        }

        FrameAnalysisContext ctx = new FrameAnalysisContext();
        ctx.frame = data;
        ctx.cameraHeight = camera.getParameters().getPreviewSize().height;
        ctx.cameraWidth = camera.getParameters().getPreviewSize().width;
        ctx.camViewMeasuredHeight = this.camView.getMeasuredHeight();
        ctx.camViewMeasuredWidth = this.camView.getMeasuredWidth();
        ctx.vertical = DisplayUtils.getScreenOrientation(this.getContext()) == 1;
        ctx.x1 = x1;
        ctx.x2 = x2;
        ctx.x3 = x3;
        ctx.x4 = x4;
        ctx.y1 = y1;
        ctx.y2 = y2;
        ctx.y3 = y3;
        ctx.y4 = y4;
        frameAnalyser.handleFrame(ctx);
    }

    void analyserCallback(final String result, final int type, byte[] previewData) {
        if (usePreviewForPicture) {
            lastPreviewData = previewData;
        }

        /*if (!keepScanning) {
            this.closeCamera();
        }*/

        // Return result on main thread
        this.post(new Runnable() {
            @Override
            public void run() {
                ZbarScanView.this.handler.handleScanResult(result, type);
            }
        });
    }
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Lifecycle external toggles
    public void pauseCamera() {
        if (this.cam != null) {
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

    public static void beepOk() {
        ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
        tg.startTone(ToneGenerator.TONE_PROP_PROMPT, 100);
        tg.release();
    }

    public static void beepWaiting() {
        ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
        tg.startTone(ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE, 300);
        tg.release();
    }

    public static void beepKo() {
        ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
        tg.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 300);
        tg.release();
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
            frameAnalyser.close();
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

    protected void drawHud(Canvas canvas) {
        if (x1 == 0) {
            this.initRect();
        }

        // Draw rectangle
        canvas.drawRect(x1, y1, x3, y3, targetRectPaint);

        // Guide horizontal line
        canvas.drawLine(0, y1 + (y3 - y1) / 2, this.getMeasuredWidth(), y1 + (y3 - y1) / 2, guideLinePaint);

        if (manualAutoFocus) {
            // Draw autofocus reticule
            canvas.drawRect(x1, y1, x3, y3, autoFocusPaint);
        }
        // Hide rest of the view
        //canvas.drawRect(0, 0, this.getMeasuredWidth(), y1, filterOutPaint);
        //canvas.drawRect(0, y3, this.getMeasuredWidth(), this.getMeasuredHeight(), filterOutPaint);
        //canvas.drawRect(0, y1, x4, y4, filterOutPaint);
        //canvas.drawRect(x2, y2, this.getMeasuredWidth(), y3, filterOutPaint);
    }

    public void setResultHandler(ResultHandler handler) {
        this.handler = handler;
    }

    public interface ResultHandler {
        void handleScanResult(String result, int type);
    }

    public void setAllowTargetDrag(boolean allowTargetDrag) {
        this.allowTargetDrag = allowTargetDrag;
    }
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Camera as a camera!
    public void takePicture(final Camera.PictureCallback callback) {
        if (usePreviewForPicture && lastPreviewData != null) {
            Log.d(TAG, "Picture from preview");
            final Camera camera = this.cam;
            new ConvertPreviewAsync(lastPreviewData, previewSize, new ConvertPreviewAsync.Callback() {
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
