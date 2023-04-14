package com.enioka.scanner.camera;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

import me.dm7.barcodescanner.core.DisplayUtils;

/**
 * Experimental. Should NOT be used.
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CameraBarcodeScanViewV2 extends CameraBarcodeScanViewBase implements SurfaceHolder.Callback {
    private String cameraId;

    private CameraCaptureSession captureSession;
    private CaptureRequest captureRequest;
    private CameraDevice cameraDevice;
    private ImageReader imageReader;
    private int cameraNativeOrientation;

    private boolean isTorchSupported = false;
    private Integer controlModeScene = null;
    private Integer controlModeAf = null;

    private Range<Integer> previewFpsRange;


    /**
     * A {@link Handler} for running technical tasks in the background.
     */
    private Handler backgroundHandler;
    private HandlerThread backgroundThread;


    public CameraBarcodeScanViewV2(@NonNull Context context) {
        super(context);
        init();
    }

    public CameraBarcodeScanViewV2(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // Create resolution manager.
        resolution = new Resolution(getContext());
        resolution.useAdaptiveResolution = false; // TODO.

        // The real scanner
        reinitialiseFrameAnalyser();

        // A thread for dealing with camera technical operations.
        startBackgroundThread();

        // This will add the preview view. Its completion callback calls selectCameraParameters, then openCamera (and initOverlay), which has a completion callback which starts the preview.
        initLayout();
    }

    private void initLayout() {
        if (this.camPreviewSurfaceView != null) {
            return;
        }

        camPreviewSurfaceView = new SurfaceView(getContext());
        this.addView(camPreviewSurfaceView);
        camPreviewSurfaceView.getHolder().addCallback(this); // this calls callback when ready.
    }

    // called by the surface created callback.
    private void initOverlay() {
        // Place the target rectangle in the center of the screen.
        DisplayMetrics metrics = this.getContext().getResources().getDisplayMetrics();
        //float xdpi = metrics.xdpi;
        float ydpi = metrics.ydpi;
        int actualLayoutWidth, actualLayoutHeight;

        if (this.isInEditMode()) {
            actualLayoutWidth = this.getMeasuredWidth();
            actualLayoutHeight = this.getMeasuredHeight();
        } else {
            actualLayoutWidth = this.camPreviewSurfaceView.getMeasuredWidth();
            actualLayoutHeight = this.camPreviewSurfaceView.getMeasuredHeight();
        }

        int y1 = (int) (actualLayoutHeight / 2 - RECT_HEIGHT / 2 / MM_INSIDE_INCH * ydpi);
        int y3 = (int) (actualLayoutHeight / 2 + RECT_HEIGHT / 2 / MM_INSIDE_INCH * ydpi);

        View targetView = new TargetView(this.getContext());
        FrameLayout.LayoutParams prms = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, (int) (RECT_HEIGHT / MM_INSIDE_INCH * ydpi));
        prms.setMargins(0, y1, 0, 0);

        cropRect.top = y1;
        cropRect.bottom = y3;
        cropRect.left = (int) (actualLayoutWidth * 0.1);
        cropRect.right = (int) (actualLayoutWidth * 0.9);

        Log.i(TAG, "Setting targeting rect at " + cropRect);
        this.addView(targetView, prms);
    }

    private void selectCameraParameters() {
        // Should be tested before but let's check for sanity's sake anyway.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            throw new RuntimeException("cannot use Camera2 API on a pre-Lollipop device");
        }
        CameraManager manager = (CameraManager) getContext().getSystemService(android.content.Context.CAMERA_SERVICE);
        if (manager == null) {
            throw new RuntimeException("cannot use Camera2 API on this device - null CameraManager");
        }

        // Select camera
        CameraCharacteristics characteristics = null;
        StreamConfigurationMap map = null;
        try {
            // Iterate all cameras and select the best one for barcode scanning purposes.
            for (String cameraId : manager.getCameraIdList()) {
                characteristics = manager.getCameraCharacteristics(cameraId);

                // We only want back cameras.
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing == null || facing != CameraCharacteristics.LENS_FACING_BACK) {
                    continue;
                }

                map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if (map == null) {
                    continue;
                }

                this.cameraId = cameraId;

                // Level?
                Integer hardwareLevel = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
                hardwareLevel = hardwareLevel == null ? -1 : hardwareLevel;
                switch (hardwareLevel) {
                    case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL:
                        Log.d(TAG, "INFO_SUPPORTED_HARDWARE_LEVEL FULL");
                        break;
                    case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY:
                        Log.d(TAG, "INFO_SUPPORTED_HARDWARE_LEVEL LEGACY");
                        break;
                    case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED:
                        Log.d(TAG, "INFO_SUPPORTED_HARDWARE_LEVEL LIMITED");
                        break;
                    default:
                        Log.d(TAG, "INFO_SUPPORTED_HARDWARE_LEVEL UNKNOWN");
                        break;
                }

                // tweak it.
                Boolean hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                isTorchSupported = hasFlash == null ? false : hasFlash;
                Log.i(TAG, "Camera supports flash: " + isTorchSupported);
                selectResolution(map.getOutputSizes(ImageFormat.YUV_420_888));
                Log.i(TAG, "Camera uses preview resolution: " + resolution.currentPreviewResolution.x + "*" + resolution.currentPreviewResolution.y);
                previewFpsRange = selectFpsRange(characteristics);
                Log.i(TAG, "Camera uses FPS range: " + previewFpsRange);

                // Scene. This controls many things, including FPS.
                int[] sceneModes = characteristics.get(CameraCharacteristics.CONTROL_AVAILABLE_SCENE_MODES);
                if (sceneModes != null && sceneModes.length > 0 && (sceneModes[0] != CameraCharacteristics.CONTROL_SCENE_MODE_DISABLED)) {
                    ArrayList<Integer> sceneModesList = new ArrayList<>(sceneModes.length);
                    for (int i : sceneModes) {
                        sceneModesList.add(i);
                    }

                    if (sceneModesList.contains(CameraCharacteristics.CONTROL_SCENE_MODE_SPORTS)) {
                        controlModeScene = CaptureRequest.CONTROL_SCENE_MODE_SPORTS;
                        Log.i(TAG, "Using scene mode: CONTROL_SCENE_MODE_SPORTS");
                    } else if (sceneModesList.contains(CameraCharacteristics.CONTROL_SCENE_MODE_ACTION)) {
                        controlModeScene = CaptureRequest.CONTROL_SCENE_MODE_ACTION;
                        Log.i(TAG, "Using scene mode: CONTROL_SCENE_MODE_ACTION");
                    } else if (sceneModesList.contains(CameraCharacteristics.CONTROL_SCENE_MODE_STEADYPHOTO)) {
                        controlModeScene = CaptureRequest.CONTROL_SCENE_MODE_STEADYPHOTO;
                        Log.i(TAG, "Using scene mode: CONTROL_SCENE_MODE_STEADYPHOTO");
                    } else {
                        Log.i(TAG, "Using scene mode: none");
                    }
                }

                // AutoFocus (AF)
                int[] afModesArray = characteristics.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES);
                if (afModesArray != null && afModesArray.length > 0) {
                    ArrayList<Integer> modes = new ArrayList<>(afModesArray.length);
                    for (int i : afModesArray) {
                        modes.add(i);
                    }

                    if (modes.contains(CameraCharacteristics.CONTROL_AF_MODE_CONTINUOUS_PICTURE)) {
                        controlModeAf = CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE;
                        Log.i(TAG, "Usig AF mode: CONTROL_AF_MODE_CONTINUOUS_PICTURE");
                    } else if (modes.contains(CameraCharacteristics.CONTROL_AF_MODE_MACRO)) {
                        controlModeAf = CaptureRequest.CONTROL_AF_MODE_MACRO;
                        Log.i(TAG, "Usig AF mode: CONTROL_AF_MODE_MACRO");
                    } else if (modes.contains(CameraCharacteristics.CONTROL_AF_MODE_AUTO)) {
                        controlModeAf = CaptureRequest.CONTROL_AF_MODE_AUTO;
                        Log.i(TAG, "Usig AF mode: CONTROL_AF_MODE_AUTO");
                    } else {
                        Log.i(TAG, "Usig AF mode: none");
                    }
                }

                // Misc
                cameraNativeOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);

                // GO
                break;
            }
        } catch (CameraAccessException e) {
            throw new RuntimeException(e);
        }

        if (characteristics == null) {
            throw new RuntimeException("no suitable camera found on device");
        }
    }

    private void selectResolution(Size[] choices) {
        for (Size s : choices) {
            resolution.supportedPreviewResolutions.add(new Point(s.getWidth(), s.getHeight()));
        }
        ViewHelpersResolution.setPreviewResolution(getContext(), resolution, this.camPreviewSurfaceView);
    }

    private Range<Integer> selectFpsRange(CameraCharacteristics characteristics) {
        Range<Integer>[] ranges = characteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);
        if (ranges == null || ranges.length == 0) {
            return new Range<>(10, 30);
        }

        Range<Integer> result = ranges[0];
        for (Range<Integer> range : ranges) {
            int upper = range.getUpper();
            if (upper > result.getUpper() && range.getLower() >= 8) {
                result = range;
            }

        }

        return result;
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            throw new RuntimeException("missing use camera permission");
        }

        CameraManager manager = (CameraManager) getContext().getSystemService(android.content.Context.CAMERA_SERVICE);
        if (manager == null) {
            throw new RuntimeException("cannot use Camera2 API on this device - null CameraManager");
        }
        try {
            manager.openCamera(this.cameraId, this.stateCallback, backgroundHandler);
        } catch (CameraAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private synchronized void closeCamera() {
        if (null != captureSession) {
            captureSession.close();
            captureSession = null;
        }
        if (null != cameraDevice) {
            cameraDevice.close();
            cameraDevice = null;
        }
        if (null != imageReader) {
            imageReader.close();
            imageReader = null;
        }
        if (null != frameAnalyser) {
            frameAnalyser.close();
            frameAnalyser = null;
        }
        stopBackgroundThread();
    }

    public void cleanUp() {
// TODO
    }

    public void pauseCamera() {
        // TODO
    }

    public void resumeCamera() {
        // TODO
    }

    public boolean getSupportTorch() {
        // TODO
        return true;
    }

    public boolean getTorchOn() {
        // TODO
        return false;
    }


    ///////////////////////////////////////////////////////////////////////////
    // Background thread for handling camera-related events
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Starts a background thread and its {@link Handler}.
     */
    private void startBackgroundThread() {
        backgroundThread = new HandlerThread("CameraBackground");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    private void stopBackgroundThread() {
        backgroundThread.quitSafely();
        try {
            backgroundThread.join();
            backgroundThread = null;
            backgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    ///////////////////////////////////////////////////////////////////////////
    // SurfaceView holder callbacks (main init)
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        // This will simply select the camera
        selectCameraParameters();

        // Set the surface buffer size
        surfaceHolder.setFixedSize(resolution.currentPreviewResolution.x, resolution.currentPreviewResolution.y);

        // Go for camera
        if (!isInEditMode()) {
            openCamera(); // will in turn start the preview.
        }

        // Add targeting rectangle (must be done after everything is drawn and dimensions are known)
        initOverlay();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        Log.i(TAG, "surface changed");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        Log.i(TAG, "surface destroyed");
        closeCamera();
    }


    ///////////////////////////////////////////////////////////////////////////
    // CameraDevice.StateCallback
    ///////////////////////////////////////////////////////////////////////////

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            // Start preview when camera is actually opened
            CameraBarcodeScanViewV2.this.cameraDevice = cameraDevice;
            startPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            cameraDevice.close();
            CameraBarcodeScanViewV2.this.cameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {
            cameraDevice.close();
            CameraBarcodeScanViewV2.this.cameraDevice = null;
            Toast toast = Toast.makeText(getContext(), "camera error " + i, Toast.LENGTH_LONG);
            toast.show();
        }
    };

    private void startPreview() {
        Surface surface = this.camPreviewSurfaceView.getHolder().getSurface();

        try {
            imageReader = ImageReader.newInstance(resolution.currentPreviewResolution.x, resolution.currentPreviewResolution.y, ImageFormat.YUV_420_888, Runtime.getRuntime().availableProcessors() * 2);
            imageReader.setOnImageAvailableListener(imageCallback, backgroundHandler);

            final CaptureRequest.Builder captureRequestBuilder = this.cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            captureRequestBuilder.addTarget(imageReader.getSurface());

            this.cameraDevice.createCaptureSession(Arrays.asList(surface, imageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    if (null == cameraDevice) {
                        return;
                    }

                    // If here, preview session is open and we can start the actual preview.
                    captureSession = cameraCaptureSession;

                    if (controlModeScene != null) {
                        captureRequestBuilder.set(CaptureRequest.CONTROL_SCENE_MODE, controlModeScene);
                        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_USE_SCENE_MODE);
                    } else {
                        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
                    }

                    //captureRequestBuilder.set(CaptureRequest.CONTROL_AE_LOCK, false);
                    //captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
                    //captureRequestBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CmmmmmmmmmmmaptureRequest.CONTROL_AWB_MODE_AUTO);

                    captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, controlModeAf);
                    //captureRequestBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, previewFpsRange);


                    //captureRequestBuilder.set(CaptureRequest., CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

                    // GO for preview
                    captureRequest = captureRequestBuilder.build();

                    try {
                        captureSession.setRepeatingRequest(captureRequest, null, backgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast toast = Toast.makeText(getContext(), "camera error - could not set preview", Toast.LENGTH_LONG);
                    toast.show();
                }
            }, null);
        } catch (CameraAccessException e) {
            throw new RuntimeException(e);
        }
    }


    ///////////////////////////////////////////////////////////////////////////
    // Image reader
    ///////////////////////////////////////////////////////////////////////////

    ImageReader.OnImageAvailableListener imageCallback = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image image = reader.acquireLatestImage();
            if (image == null) {
                return;
            }

            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);

            FrameAnalysisContext ctx = new FrameAnalysisContext();
            ctx.frame = bytes;
            ctx.camViewMeasuredWidth = camPreviewSurfaceView.getMeasuredWidth();
            ctx.camViewMeasuredHeight = camPreviewSurfaceView.getMeasuredHeight();
            ctx.vertical = DisplayUtils.getScreenOrientation(getContext()) == 1;
            ctx.cameraHeight = resolution.currentPreviewResolution.y;
            ctx.cameraWidth = resolution.currentPreviewResolution.x;
            ctx.x1 = cropRect.left;
            ctx.x2 = cropRect.right;
            ctx.x3 = ctx.x2;
            ctx.x4 = ctx.x1;
            ctx.y1 = cropRect.top;
            ctx.y2 = ctx.y1;
            ctx.y3 = cropRect.bottom;
            ctx.y4 = ctx.y3;

            frameAnalyser.handleFrame(ctx);

            image.close();
        }
    };


    ///////////////////////////////////////////////////////////////////////////
    // Life cycle handlers
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void giveBufferBack(FrameAnalysisContext analysisContext) {

    }

    @Override
    public void setPreviewResolution(Point newResolution) {

    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Helpers
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public int getCameraDisplayOrientation() {
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

        return (cameraNativeOrientation - degrees + 360) % 360;
    }
}
