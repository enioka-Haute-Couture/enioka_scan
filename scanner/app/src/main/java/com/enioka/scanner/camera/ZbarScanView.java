package com.enioka.scanner.camera;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.enioka.scanner.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
    private float ydpi;
    float dragStartY, dragCropTop, dragCropBottom;
    private static final String TAG = "BARCODE";

    private Context context;
    private Camera cam;
    protected SurfaceView camView;
    private ResultHandler handler;
    //protected int x1, y1, x2, y2, x3, y3, x4, y4; // 1 = top left, 2 = top right, 3 = bottom right, 4 = bottom left.
    protected Rect cropRect = new Rect(); // The "targeting" rectangle.
    private boolean hasExposureCompensation = false;
    private boolean torchOn = false;
    protected boolean scanningStarted = true;
    private boolean failed = false;

    private boolean usePreviewForPicture = true;
    boolean allowTargetDrag = true;
    private byte[] lastPreviewData;
    private boolean useAdaptiveResolution = true;

    private List<Camera.Size> allowedPreviewSizes = new ArrayList<>(20);
    private Camera.Size previewSize;

    private FrameAnalyserManager frameAnalyser;


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Stupid constructors
    public ZbarScanView(Context context) {
        super(context);
        this.context = context;
        initOnce(context);
    }

    public ZbarScanView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.context = context;
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

        // Take photo on click instead of continuous scan
        // this.setOnClickListener(this);

        // If the preview does not take all the space
        this.setBackgroundColor(Color.BLACK);

        // The view holding the preview. This will in turn (camHolder.addCallback) call setUpCamera.
        if (this.camView == null) {
            camView = new SurfaceView(context);
            this.addView(camView);
        }
        camView.getHolder().addCallback(this);
    }

    /**
     * Default is simply CODE_128. Use the Symbol static fields to specify a symbology.
     *
     * @param s the ID of the symbology (ZBAR coding)
     */
    public void addSymbology(int s) {
        if (frameAnalyser != null) {
            frameAnalyser.addSymbology(s);
        }
    }

    /**
     * After this call the camera is selected, with correct parameters and open, ready to be plugged on a preview pane.
     */
    private void setUpCamera() {
        // Camera pane init
        if (this.cam != null || this.isInEditMode()) {
            return;
        }

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

        // Focus & Metering areas
        setAreas(prms);

        // Exposure
        if (prms.isAutoExposureLockSupported()) {
            Log.d(TAG, "Auto exposure lock is supported and value is: " + prms.getAutoExposureLock());
            //prms.setAutoExposureLock(true);
        }
        if (prms.getMaxExposureCompensation() > 0 && prms.getMinExposureCompensation() < 0) {
            Log.i(TAG, "Exposure compensation is supported with limits [" + prms.getMinExposureCompensation() + ";" + prms.getMaxExposureCompensation() + "]");
            hasExposureCompensation = true;
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

        // Scene mode. Try to select a mode which will ensure a high FPS rate.
        List<String> supportedSceneModes = prms.getSupportedSceneModes();
        if (supportedSceneModes != null) {
            Log.d(TAG, "Supported scene modes: " + supportedSceneModes.toString());
        }
        if (supportedSceneModes != null && supportedSceneModes.contains(Camera.Parameters.SCENE_MODE_BARCODE)) {
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

        // Set flash mode to torch if supported
        setTorch(prms, torchOn);

        //////////////////////////////////////
        // Preview Resolution
        setPreviewResolution(prms);

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
        Camera.Size prevSize = null; // prms.getPreferredPreviewSizeForVideo();

        // Look for a resolution not too far from the view ratio.
        float preferredRatio = (float) this.camView.getMeasuredHeight() / (float) this.camView.getMeasuredWidth();
        if (preferredRatio < 1) {
            preferredRatio = 1 / preferredRatio;
        }
        Log.i(TAG, "Looking for the ideal preview resolution. View ratio is " + preferredRatio);
        boolean goodMatchFound = false;

        // First simply list resolutions (debug display & sorted res list creation)
        for (Camera.Size s : rezs) {
            Log.d(TAG, "\tsupports preview resolution " + s.width + "*" + s.height + " - " + ((float) s.width / (float) s.height));

            if (Math.abs((float) s.width / (float) s.height - preferredRatio) < 0.3f) {
                allowedPreviewSizes.add(s);
            }
        }
        Collections.sort(allowedPreviewSizes, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size o1, Camera.Size o2) {
                return o1.width < o2.width ? -1 : o1.width == o2.width ? 0 : 1;
            }
        });
        Log.v(TAG, "Allowed preview sizes (acceptable ratio): ");
        for (Camera.Size s : allowedPreviewSizes) {
            Log.v(TAG, "\t" + s.width + "*" + s.height + " - " + ((float) s.width / (float) s.height));
        }

        // Select the best resolution.
        // First try with only the preferred ratio.
        for (Camera.Size s : rezs) {
            if (prevSize == null || (s.width > prevSize.width) && Math.abs((float) s.width / (float) s.height - preferredRatio) < 0.1f) {
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

        if (prevSize == null) {
            throw new RuntimeException("no suitable preview resolution");
        }
        prms.setPreviewSize(prevSize.width, prevSize.height);


        // COMPAT HACKS
        this.usePreviewForPicture = false;
        switch (android.os.Build.MODEL) {
            case "LG-H340n":
                prms.setPreviewSize(1600, 1200);
                Log.i(TAG, "LG-H340n specific - using hard-coded preview resolution" + prms.getPreviewSize().width + "*" + prms.getPreviewSize().height + ". Ratio is " + ((float) prms.getPreviewSize().width / prms.getPreviewSize().height) + " (requested ratio was " + preferredRatio + ")");
                this.useAdaptiveResolution = false;
                break;
            case "SPA43LTE":
                if (preferredRatio < 1.5) {
                    prms.setPreviewSize(1440, 1080); // Actual working max, 1.33 ratio. No higher rez works.
                } else {
                    prms.setPreviewSize(1280, 720);
                }
                this.usePreviewForPicture = true;
                this.useAdaptiveResolution = false;
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

        // Set a denormalized field - this is used widely in the class.
        this.previewSize = prms.getPreviewSize();
    }

    private void setPictureResolution(Camera.Parameters prms) {
        // A preview resolution is often a picture resolution, so start with this.
        // Then, look for any higher resolution with the same ratio

        // Note the ratio looked for is the preview ratio, not the view ratio as it may be different.
        float preferredRatio = (float) this.previewSize.width / (float) this.previewSize.height;
        Log.i(TAG, "Looking for the ideal photo resolution. View ratio is " + preferredRatio);

        List<Camera.Size> sizes = prms.getSupportedPictureSizes();
        Camera.Size pictureSize = null, smallestSize = sizes.get(0), betterChoiceWrongRatio = null;
        boolean foundWithGoodRatio = false;

        for (Camera.Size s : sizes) {
            // Is preview resolution a picture resolution?
            if (s.width == previewSize.width && s.height == previewSize.height) {
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
        float camResRatio = (float) prms.getPictureSize().width / (float) prms.getPictureSize().height;
        Log.i(TAG, "Using picture resolution " + pictureSize.width + "*" + pictureSize.height + ". Ratio is " + camResRatio + ". (Preferred ratio was " + preferredRatio + ")");
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

    /**
     * Will change the resolution according to the analysis FPS rate.
     */
    synchronized void onWorryingFps(boolean low) {
        if (this.cam == null || !useAdaptiveResolution) {
            return;
        }

        // Current resolution index?
        int currentResolutionIndex = -1;
        int i = -1;
        for (Camera.Size size : allowedPreviewSizes) {
            i++;
            if (size.width == previewSize.width && size.height == previewSize.height) {
                currentResolutionIndex = i;
                break;
            }
        }

        // Checks
        if (currentResolutionIndex == -1) {
            // Happens when the chosen resolution does not have the correct ratio.
            Log.d(TAG, "Out of bounds FPS but no suitable alternative resolution available");
            return;
        }
        int indexShift;
        if (low) {
            if (currentResolutionIndex == 0) {
                // We already use the lowest resolution possible
                Log.d(TAG, "Low analysis FPS but already on the lowest possible resolution");
                return;
            }
            indexShift = -1;
        } else {
            if (currentResolutionIndex == allowedPreviewSizes.size() - 1) {
                // We already use the lowest resolution possible
                Log.d(TAG, "High analysis FPS but already on the highest possible resolution");
                return;
            }
            indexShift = +1;
        }

        // We have a correct new preview resolution!
        Camera.Size newRez = allowedPreviewSizes.get(currentResolutionIndex + indexShift);
        Log.i(TAG, "Changing preview resolution from " + previewSize.width + "*" + previewSize.height + " to " + newRez.width + "*" + newRez.height);

        // Set it
        Camera.Parameters prms = this.cam.getParameters();
        pauseCamera();
        previewSize = newRez;
        prms.setPreviewSize(newRez.width, newRez.height);
        setCameraParameters(prms);
        resumeCamera();
    }

    int getPreviewLineCount() {
        return previewSize.height;
    }

    /**
     * Called when the FPS is near the lower limit. Used to check if it would not be advisable to lower resolution.
     */
    void onLowishFps() {
        if (previewSize.height > 1080) {
            onWorryingFps(true);
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
     * Indicate if the torch mode is handled or not
     *
     * @return A true value if the torch mode supported, false otherwise
     */
    public boolean getSupportTorch() {
        if (failed) {
            return false;
        }
        if (this.cam == null || this.isInEditMode()) {
            return true;
        }
        Camera.Parameters prms = this.cam.getParameters();
        return getSupportTorch(prms);
    }

    /**
     * @return true if torch is on
     */
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
                // prms.setExposureCompensation(prms.getMinExposureCompensation() + 1);
            }
        } else if (support) {
            prms.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            if (this.hasExposureCompensation) {
                //prms.setExposureCompensation((prms.getMaxExposureCompensation() + prms.getMinExposureCompensation()) / 2 - 1);
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
            return;
        }
        Camera.Parameters prms = this.cam.getParameters();
        setTorch(prms, value);
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
    // Target area handling

    /**
     * Sets up the central targeting rectangle. Must be called after surface init.
     */
    private void computeCropRectangle() {
        // First, top may be a user preference
        Activity a = this.getActivity();
        int top = -1;
        if (a != null) {
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
            actualLayoutWidth = this.camView.getMeasuredWidth();
            actualLayoutHeight = this.camView.getMeasuredHeight();
        }

        int y1, y3;
        if (top != -1) {
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
    private void addTargetView() {
        final View targetView = new TargetView(this.getContext());
        targetView.setId(R.id.barcode_scanner_camera_view);
        final FrameLayout.LayoutParams prms = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, (int) (RECT_HEIGHT / MM_INSIDE_INCH * ydpi));
        prms.setMargins(0, cropRect.top, 0, 0);

        Log.i(TAG, "Targeting overlay added");
        this.addView(targetView, prms);

        targetView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
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
                        if (newTop > 0 && newBottom < ZbarScanView.this.camView.getHeight()) {
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
                        storePreferences("y" + getCameraDisplayOrientation(), cropRect.top);
                        break;
                }

                return false;
            }
        });
    }

    private void storePreferences(String key, int y) {
        Activity a = this.getActivity();
        if (a == null) {
            return;
        }

        SharedPreferences p = a.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor e = p.edit();
        e.putInt(key, y);
        e.apply();
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
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Reactions to preview pane being created/modified/destroyed.
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        computeCropRectangle();
        if (this.cam == null) {
            setUpCamera();
            surfaceHolder.setFixedSize(this.previewSize.width, this.previewSize.height);
        }

        try {
            this.cam.setPreviewDisplay(surfaceHolder);
            this.cam.startPreview();
            if (scanningStarted) {
                this.cam.setPreviewCallback(this);
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not start camera preview and preview data analysis", e);
        }
        addTargetView();
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
                if (ZbarScanView.this.handler != null) {
                    ZbarScanView.this.handler.handleScanResult(result, type);
                }
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
