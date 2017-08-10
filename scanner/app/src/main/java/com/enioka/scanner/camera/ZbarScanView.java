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
import android.text.TextUtils;
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

import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import me.dm7.barcodescanner.core.DisplayUtils;

/**
 * Helper view that encapsulates the ZBar barcode analysis engine.
 * To be directly reused in layouts.
 * We are using deprecated Camera API because old Android.
 */
@SuppressWarnings("deprecation")
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
    private ResultValidator validator;
    private ResultValidatorAsync validatorAsync;
    private boolean beepBeforeValidation = false;
    protected Paint targetRectPaint, guideLinePaint, filterOutPaint, autoFocusPaint;
    protected int x1, y1, x2, y2, x3, y3, x4, y4; // 1 = top left, 2 = top right, 3 = bottom right, 4 = bottom left.
    private float camResRatio;
    private boolean manualAutoFocus = false;
    private boolean torchOn = false;
    private ZbarScanViewOverlay overlay;
    protected boolean scanningStarted = true;
    private boolean failed = false;

    private boolean usePreviewForPicture = true;
    private byte[] lastPreviewData;
    private Camera.Size previewSize;

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

            // Barcode analyzer (properties: 0 = all symbologies, 256 = config, 3 = value)
            this.scanner = new ImageScanner();
            this.scanner.setConfig(0, 256, 0); // 256 =   ZBAR_CFG_X_DENSITY (disable vertical scanning)
            //this.scanner.setConfig(0, 257, 3); // 257 =  ZBAR_CFG_Y_DENSITY (skip 2 out of 3 lines)
            this.scanner.setConfig(0, 0, 0); //  0 = ZBAR_CFG_ENABLE (disable all symbologies)
            this.scanner.setConfig(Symbol.CODE128, 0, 1); //  0 = ZBAR_CFG_ENABLE (enable symbology 128)
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
        this.scanner.setConfig(s, 0, 1);
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
                Log.i(TAG, "Camera is being opened");
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
                areas.add(new Camera.Area(new Rect(-10, -10, 10, 10), 1000));
                prms.setFocusAreas(areas);
                Log.d(TAG, "Using a central focus area");
            }

            // Metering areas
            if (prms.getMaxNumMeteringAreas() > 0) {
                List<Camera.Area> areas = new ArrayList<>();
                areas.add(new Camera.Area(new Rect(-1000, -50, 1000, 50), 1000));
                prms.setMeteringAreas(areas);
                Log.d(TAG, "Using a central metering area");
            }

            // Exposure
            if (prms.isAutoExposureLockSupported()) {
                Log.d(TAG, "Auto exposure lock is supported and value is: " + prms.getAutoExposureLock());
                //prms.setAutoExposureLock(true);
            }
            if (prms.getMaxExposureCompensation() > 0 && prms.getMinExposureCompensation() > 0) {
                Log.d(TAG, "Exposure compensation is supported with limits [" + prms.getMinExposureCompensation() + ";" + prms.getMaxExposureCompensation() + "]");
            } else {
                Log.d(TAG, "Exposure compensation is not supported with limits [" + prms.getMinExposureCompensation() + ";" + prms.getMaxExposureCompensation() + "]");
            }
            if (prms.getWhiteBalance() != null) {
                Log.d(TAG, "white balance is supported with modes: " + prms.getSupportedWhiteBalance() + ". Selected is: " + prms.getWhiteBalance());
                //prms.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_DAYLIGHT);
            }

            // Antibanding
            if (prms.getAntibanding() != null) {
                Log.d(TAG, "Antibanding is supported and is " + prms.getAntibanding());
                //prms.setAntibanding(Camera.Parameters.ANTIBANDING_OFF);
            }

            // Stabilization
            if (prms.isVideoStabilizationSupported()) {
                Log.d(TAG, "Video stabilization is supported and is: " + prms.getVideoStabilization());
            }

            // A YUV format. NV21 is always available, no need to check if it is supported
            prms.setPreviewFormat(ImageFormat.NV21);

            // Set focus mode to FOCUS_MODE_CONTINUOUS_PICTURE if supported
            List<String> supportedFocusModes = prms.getSupportedFocusModes();
            Log.d(TAG, "Supported focus modes: " + supportedFocusModes.toString());
            if (supportedFocusModes.contains("mw_continuous-picture")) {
                prms.setFocusMode("mw_continuous-picture");
                Log.d(TAG, "supportedFocusModes - mw_continuous-picture supported and selected");
            } else if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                prms.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                Log.d(TAG, "supportedFocusModes - continuous-picture supported and selected");
            } else if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                prms.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                Log.d(TAG, "supportedFocusModes - auto supported and selected");
                manualAutoFocus = true;
            } else if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_MACRO)) {
                prms.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
                Log.d(TAG, "supportedFocusModes - macro supported and selected");
            } else {
                Log.d(TAG, "no autofocus supported");
            }

            // Scene mode
            List<String> supportedSceneModes = prms.getSupportedSceneModes();
            if (supportedSceneModes != null && supportedSceneModes.contains(Camera.Parameters.SCENE_MODE_BARCODE)) {
                Log.d(TAG, "supportedSceneModes - scene mode barcode supported and selected");
                prms.setSceneMode(Camera.Parameters.SCENE_MODE_BARCODE);
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
                Log.d(TAG, "supports preview resolution " + s.width + "*" + s.height + " - " + ((float) s.width / (float) s.height));
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
            prms.setPreviewSize(prevSize.width, prevSize.height);

            // COMPAT HACKS
            if (android.os.Build.MODEL.equals("LG-H340n")) {
                prms.setPreviewSize(1600, 1200);
                Log.d(TAG, "LG-H340n specific - using hard-coded preview resolution" + prms.getPreviewSize().width + "*" + prms.getPreviewSize().height + ". Ratio is " + ((float) prms.getPreviewSize().width / prms.getPreviewSize().height));
            } else if (android.os.Build.MODEL.equals("SPA43LTE")) {
                if (preferredRatio < 1.5) {
                    prms.setPreviewSize(1440, 1080); // Actual working max, 1.33 ratio. No higher rez works.
                } else {
                    prms.setPreviewSize(1280, 720);
                }
                Log.d(TAG, "SPA43LTE specific - using hard-coded preview resolution" + prms.getPreviewSize().width + "*" + prms.getPreviewSize().height + ". Ratio is " + ((float) prms.getPreviewSize().width / prms.getPreviewSize().height));
            } else {
                Log.d(TAG, "Using preview resolution " + prevSize.width + "*" + prevSize.height + ". Ratio is " + ((float) prevSize.width / (float) prevSize.height));
            }

            this.previewSize = prms.getPreviewSize();
            this.usePreviewForPicture = previewSize.height >= 1080;

            //////////////////////////////////////
            // Picture resolution

            // A preview resolution is often a picture resolution, so start with this.
            // Then, look for any higher resolution with the same ratio
            List<Camera.Size> sizes = prms.getSupportedPictureSizes();
            Camera.Size pictureSize = null, smallestSize = sizes.get(0), betterChoiceWrongRatio = null;
            for (Camera.Size s : sizes) {
                // Is preview resolution a picture resolution?
                if (s.width == prevSize.width && s.height == prevSize.height) {
                    pictureSize = s;
                }
                if (s.width < smallestSize.width) {
                    smallestSize = s;
                }
            }
            if (pictureSize == null) {
                pictureSize = smallestSize;
            }

            boolean foundWithGoodRatio = false;
            for (Camera.Size size : sizes) {
                Log.d(TAG, "supports picture resolution " + size.width + "*" + size.height + " - " + ((float) prevSize.width / (float) prevSize.height));
                if (Math.abs((float) size.width / (float) size.height - preferredRatio) < 0.1f && size.width > pictureSize.width && size.width <= 2048 && size.height <= 1536) {
                    pictureSize = size;
                    foundWithGoodRatio = true;
                }
                if (size.width > pictureSize.width && size.width <= 2048 && size.height <= 1536) {
                    betterChoiceWrongRatio = size;
                }
            }
            if (!foundWithGoodRatio) {
                pictureSize = betterChoiceWrongRatio;
            }
            prms.setPictureSize(pictureSize.width, pictureSize.height);
            camResRatio = (float) prms.getPictureSize().width / (float) prms.getPictureSize().height;
            Log.d(TAG, "Using picture resolution " + pictureSize.width + "*" + pictureSize.height + ". Ratio is " + camResRatio + ". (Preferred ratio was " + preferredRatio + ")");

            // Set camera
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
        } else if (support) {
            prms.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
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
            this.cam = Camera.open();
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
                                camera.setOneShotPreviewCallback(t);
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
                this.cam.setOneShotPreviewCallback(this);
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

    private static Calendar last = Calendar.getInstance();
    private Integer waitingForValidations = 0;
    private Calendar focusTime = Calendar.getInstance();
    private String latestBarcodeValue = "";

    // THE main method.
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (!scanningStarted) {
            return;
        }
        //Log.i(TAG, "Starting analysis on " + data.length + " bytes of camera data");

        Calendar now = Calendar.getInstance();
        long msSinceLast = now.getTimeInMillis() - last.getTimeInMillis();

        byte[] previewData = data;

        String symData;
        int symType;

        // Data characteristics
        int dataWidth = camera.getParameters().getPreviewSize().width;
        int dataHeight = camera.getParameters().getPreviewSize().height;

        // The rectangle is in view coordinates.
        float yratio = (float) dataWidth / (float) this.camView.getMeasuredHeight();  // Photo pixels per preview surface pixel. Width because: 90° rotated.
        float xratio = (float) dataHeight / (float) this.camView.getMeasuredWidth();
        int realy1 = (int) (y1 * yratio);
        int realy3 = (int) (y3 * yratio);
        int realx1 = (int) (x1 * xratio);
        int realx3 = (int) (x3 * xratio);

        int croppedDataWidth, croppedDataHeight;

        // Rotate and crop.
        if (DisplayUtils.getScreenOrientation(this.getContext()) == 1) {
            // French (vertical) - crop & rotate
            byte[] barcode = new byte[(1 + realx3 - realx1) * (1 + realy3 - realy1)];

            int i = 0;
            for (int w = realy1; w <= realy3; w++) {
                for (int h = realx3 - 1; h >= realx1; h--) {
                    barcode[i++] = data[h * dataWidth + w];
                }
            }

            //noinspection SuspiciousNameCombination
            croppedDataWidth = (1 + realx3 - realx1);
            croppedDataHeight = (1 + realy3 - realy1);
            data = barcode;
        } else {
            // Italian (horizontal). No need to rotate - just crop.
            yratio = (float) dataHeight / (float) this.camView.getMeasuredHeight();  // Photo pixels per preview surface pixel.
            xratio = (float) dataWidth / (float) this.camView.getMeasuredWidth();

            realy1 = (int) (y1 * yratio);
            realy3 = (int) (y3 * yratio);
            realx1 = (int) (x1 * xratio);
            realx3 = (int) (x3 * xratio);

            croppedDataWidth = (1 + realx3 - realx1);
            croppedDataHeight = (1 + realy3 - realy1);

            byte[] barcode = new byte[croppedDataWidth * croppedDataHeight];
            int i = 0;
            for (int h = realy1; h <= realy3; h++) {
                for (int w = realx1; w <= realx3; w++) {
                    barcode[i++] = data[h * dataWidth + w];
                }
            }

            data = barcode;
        }

        // Analysis
        Image pic = new Image(croppedDataWidth, croppedDataHeight, "Y800");
        pic.setData(data);
        //pic.setCrop(0, realy1, dataWidth, realy3 - realy1); // Left, top, width, height
        boolean hasBeeped = false;
        if (this.scanner.scanImage(pic) != 0) {
            // There is a result! Extract it.
            if (this.handler != null) {
                SymbolSet var15 = this.scanner.getResults();
                Iterator i$ = var15.iterator();

                Set<String> foundStrings = new HashSet<>();
                while (i$.hasNext()) {
                    Symbol sym = (Symbol) i$.next();
                    symData = sym.getData();
                    symType = sym.getType();

                    if (msSinceLast < MS_SINCE_LAST && symData.equals(latestBarcodeValue)) {
                        // Same as the last (and very recent) barcode read
                        break;
                    } else if (!TextUtils.isEmpty(symData) && !foundStrings.contains(symData)) {
                        beepOk();
                        latestBarcodeValue = symData;
                        last = now;
                        foundStrings.add(symData);

                        if (!hasBeeped && this.beepBeforeValidation) {
                            hasBeeped = true;
                            //beepWaiting();
                        }
                        if (this.validatorAsync != null) {
                            synchronized (waitingForValidations) {
                                // Store the preview frame for using as picture
                                if (usePreviewForPicture) {
                                    lastPreviewData = previewData;
                                }
                                waitingForValidations++;
                                this.validatorAsync.validateResultAsync(symData, symType);
                            }
                        } else {
                            if (this.validator != null && !this.validator.validateResult(symData, symType)) {
                                continue;
                            }
                            if (endValidationAnalysis(true, symData)) {
                                return;
                            }
                        }
                    }
                }

                // No results (at least with sync validation) so rearm analysis.
                if (waitingForValidations == 0) {
                    camera.setOneShotPreviewCallback(this);
                }
            }
        } else {
            // No results at all: always rearm.
            final ZbarScanView t = this;
            Calendar limit = Calendar.getInstance();
            limit.add(Calendar.SECOND, -1);
            if (manualAutoFocus && focusTime.before(limit)) {
                focusTime = now;
                this.cam.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        Log.d(TAG, "Focus " + success);
                        try {
                            camera.setOneShotPreviewCallback(t);
                        } catch (RuntimeException e) {
                            // Happens when camera is already released - not an issue, it means scan is over.
                        }
                    }
                });
            } else {
                camera.setOneShotPreviewCallback(this);
            }
        }
    }

    public void giveValidationResult(boolean res, String value) {
        giveValidationResult(res, value, false);
    }

    public void giveValidationResult(boolean res, String value, boolean keepScanning) {
        if (this.validatorAsync == null) {
            throw new IllegalArgumentException("Cannot call giveValidationResult when validator is synchronous");
        }

        synchronized (this.waitingForValidations) {
            this.waitingForValidations--;
            endValidationAnalysis(res, value, keepScanning);
            // enforce technical checks
            if (this.cam != null && this.waitingForValidations == 0 && (!res || keepScanning)) {
                // No game - let's try again.
                this.cam.setOneShotPreviewCallback(this);
            }
        }
    }

    private boolean endValidationAnalysis(boolean valid, String res) {
        return endValidationAnalysis(valid, res, false);
    }

    private boolean endValidationAnalysis(boolean valid, String res, boolean keepScanning) {
        if (!valid) {
            lastPreviewData = null;
            return false;
        }

        // Victory beep!
        // beepOk();
        if (!keepScanning) {
            this.closeCamera();
        }
        Log.d(TAG, res);

        // Return result
        this.handler.handleScanResult(res);
        return true;
    }

    public void pauseCamera() {
        if (this.cam != null) {
            this.cam.stopPreview();
        }
    }

    public void resumeCamera() {
        if (this.cam != null) {
            this.cam.startPreview();
            if (scanningStarted) {
                this.cam.setOneShotPreviewCallback(this);
            }
        }
    }

    public void startScanner() {
        Log.d(VIEW_LOG_TAG, "Scanning started");
        this.scanningStarted = true;
        this.cam.setOneShotPreviewCallback(this);
    }

    public void pauseScanner() {
        Log.d(VIEW_LOG_TAG, "Scanning stopped");
        this.scanningStarted = false;
    }

    public static void beepOk() {
        ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
        tg.startTone(ToneGenerator.TONE_PROP_PROMPT, 100);
    }

    public static void beepWaiting() {
        ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
        tg.startTone(ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE, 300);
    }

    public static void beepKo() {
        ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
        tg.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 300);
    }
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Clean up methods
    public void cleanUp() {
        if (this.cam != null) {
            Log.i(TAG, "Removing all camera callbacks and stopping it");
            this.cam.setOneShotPreviewCallback(null);
            this.cam.setPreviewCallback(null);
            this.cam.stopPreview();
            this.setOnClickListener(null);
            this.lastPreviewData = null;
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

    public void setBeepBeforeValidation() {
        this.beepBeforeValidation = true;
    }

    public void setResultHandler(ResultHandler handler) {
        this.handler = handler;
    }

    public void setResultValidator(ResultValidator handler) {
        this.validator = handler;
    }

    public void setAsyncResultValidator(ResultValidatorAsync handler) {
        this.validatorAsync = handler;
    }

    public interface ResultHandler {
        void handleScanResult(String result);
    }

    public interface ResultValidator {
        boolean validateResult(String result, int type);
    }

    public interface ResultValidatorAsync {
        void validateResultAsync(String result, int type);
    }

    //
    ////////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Camera as a camera!

    public boolean isUsePreviewForPicture() {
        return usePreviewForPicture;
    }

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
