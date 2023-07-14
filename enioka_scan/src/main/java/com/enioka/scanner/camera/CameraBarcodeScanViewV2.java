package com.enioka.scanner.camera;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.MeteringRectangle;
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
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

import me.dm7.barcodescanner.core.DisplayUtils;

/**
 * V2 implementation of the camera view. Default implementation.
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
class CameraBarcodeScanViewV2 extends CameraBarcodeScanViewBase implements SurfaceHolder.Callback {
    private String cameraId;
    private CameraManager cameraManager;


    private CameraCaptureSession captureSession;
    private CaptureRequest.Builder captureRequestBuilder;
    private CaptureRequest captureRequest;
    private CameraDevice cameraDevice;
    private ImageReader imageReader;

    private Integer controlModeAf = null;
    private Integer controlModeAb = null;
    private int afZones = 0;

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
        Log.d(TAG, "Camera2 specific initialization start");

        // Create resolution manager.
        resolution = new Resolution(getContext());

        // The real scanner
        reinitialiseFrameAnalyser();

        // A thread for dealing with camera technical operations.
        startBackgroundThread();

        // This will add the preview view. Its completion callback calls selectCameraParameters, then openCamera (and initOverlay), which has a completion callback which starts the preview.
        initLayout();
    }

    private void initLayout() {
        Log.d(TAG, "Creating layout");

        if (this.camPreviewSurfaceView != null) {
            Log.d(TAG, "Layout is already initialized, nothing to do");
            return;
        }

        camPreviewSurfaceView = new SurfaceView(getContext());
        camPreviewSurfaceView.getHolder().addCallback(this); // this calls callback when ready.
        this.addView(camPreviewSurfaceView);

        if (isInEditMode()) {
            // In normal mode done in surface created callback, but no callback in edit mode.
            computeCropRectangle();
            addTargetView();
        }
    }

    private void selectCameraParameters() {
        // Should be tested before but let's check for sanity's sake anyway.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            throw new RuntimeException("cannot use Camera2 API on a pre-Lollipop device");
        }
        cameraManager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);
        if (cameraManager == null) {
            throw new RuntimeException("cannot use Camera2 API on this device - null CameraManager");
        }

        // Select camera
        CameraCharacteristics characteristics = null;
        StreamConfigurationMap map;
        try {
            // Iterate all cameras and select the best one for barcode scanning purposes.
            for (String cameraId : cameraManager.getCameraIdList()) {
                characteristics = cameraManager.getCameraCharacteristics(cameraId);

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
                isTorchSupported = hasFlash != null && hasFlash;
                Log.i(TAG, "Camera supports flash: " + isTorchSupported);
                selectResolution(map.getOutputSizes(ImageFormat.YUV_420_888));
                Log.i(TAG, "Camera uses preview resolution: " + resolution.currentPreviewResolution.x + "*" + resolution.currentPreviewResolution.y);
                previewFpsRange = selectFpsRange(characteristics);
                Log.i(TAG, "Camera uses FPS range: " + previewFpsRange);

                // AutoFocus (AF)
                int[] afModesArray = characteristics.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES);
                if (afModesArray != null && afModesArray.length > 0) {
                    ArrayList<Integer> modes = new ArrayList<>(afModesArray.length);
                    for (int i : afModesArray) {
                        modes.add(i);
                    }

                    if (modes.contains(CameraCharacteristics.CONTROL_AF_MODE_CONTINUOUS_PICTURE)) {
                        controlModeAf = CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE;
                        Log.i(TAG, "Using AF mode: CONTROL_AF_MODE_CONTINUOUS_PICTURE");
                    } else if (modes.contains(CameraCharacteristics.CONTROL_AF_MODE_CONTINUOUS_VIDEO)) {
                        controlModeAf = CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO;
                        Log.i(TAG, "Using AF mode: CONTROL_AF_MODE_CONTINUOUS_VIDEO");
                    } else if (modes.contains(CameraCharacteristics.CONTROL_AF_MODE_MACRO)) {
                        controlModeAf = CaptureRequest.CONTROL_AF_MODE_MACRO;
                        Log.i(TAG, "Using AF mode: CONTROL_AF_MODE_MACRO");
                    } else if (modes.contains(CameraCharacteristics.CONTROL_AF_MODE_AUTO)) {
                        controlModeAf = CaptureRequest.CONTROL_AF_MODE_AUTO;
                        Log.i(TAG, "Using AF mode: CONTROL_AF_MODE_AUTO");
                    } else {
                        Log.i(TAG, "Using AF mode: none");
                    }
                } else {
                    Log.i(TAG, "No AF mode available");
                }

                // Zones
                if (controlModeAf != null) {
                    Integer zones = characteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AF);
                    if (zones != null) {
                        afZones = zones;
                    } else {
                        afZones = 0;
                    }
                }
                Log.i(TAG, "Supported autofocus zones: " + afZones);

                // Anti-banding - just look for auto.
                int[] abModesArray = characteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_ANTIBANDING_MODES);
                if (abModesArray != null && abModesArray.length > 0) {
                    ArrayList<Integer> modes = new ArrayList<>(abModesArray.length);
                    for (int i : abModesArray) {
                        modes.add(i);
                    }

                    if (modes.contains(CameraCharacteristics.CONTROL_AE_ANTIBANDING_MODE_AUTO)) {
                        controlModeAb = CaptureRequest.CONTROL_AE_ANTIBANDING_MODE_AUTO;
                        Log.i(TAG, "Using AB mode: CONTROL_AE_ANTIBANDING_MODE_AUTO");
                    }
                }

                // GO
                break;
            }
        } catch (CameraAccessException e) {
            throw new RuntimeException(e);
        }

        if (characteristics == null) {
            throw new RuntimeException("no suitable camera found on device");
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cameraManager.registerTorchCallback(new CameraManager.TorchCallback() {
                @Override
                public void onTorchModeChanged(@NonNull String cameraId, boolean enabled) {
                    if (cameraId.equals(CameraBarcodeScanViewV2.this.cameraId)) {
                        Log.d(TAG, "Torch status has changed to: " + enabled);
                        CameraBarcodeScanViewV2.this.isTorchOn = enabled;
                    }
                }
            }, null);
        } else {
            Log.w(TAG, "API version is too low to detect if the torch is on or not");
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

    private MeteringRectangle getMeteringZone() {
        // TODO: use cropRect
        // Coordinate system is 0 topleft/bottomright
        int x0 = this.resolution.currentPreviewResolution.x / 2 - 50;
        int y0 = this.resolution.currentPreviewResolution.y / 2 - 50;
        int x1 = this.resolution.currentPreviewResolution.x / 2 + 50;
        int y1 = this.resolution.currentPreviewResolution.y / 2 + 50;
        Log.d(TAG, "Using metering zone (" + x0 + "," + y0 + ") (" + x1 + "," + y1 + ")");
        return new MeteringRectangle(x0, y0, x1 - x0, y1 - y0, 1000);
    }

    private void openCamera() {
        Log.d(TAG, "Trying to open camera from CameraManager");
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            throw new RuntimeException("missing use camera permission");
        }

        CameraManager manager = (CameraManager) getContext().getSystemService(android.content.Context.CAMERA_SERVICE);
        if (manager == null) {
            throw new RuntimeException("cannot use Camera2 API on this device - null CameraManager");
        }

        if (backgroundThread == null) {
            startBackgroundThread();
        }

        try {
            manager.openCamera(this.cameraId, this.stateCallback, backgroundHandler);
        } catch (CameraAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private synchronized void closeCamera() {
        Log.d(TAG, "Starting camera release");
        if (null != captureSession) {
            // This should not be needed, as doc states session is closed when camera is closed but anyway...
            // So we actually have reversed logics: session's closing closes in turn the camera.
            Log.d(TAG, " * Closing camera capture session");

            captureSession.close();
            captureSession = null;
        }

        // capture session is closed above.
        // capture session onClose callback closes image reader
        // capture session onClose callback closes camera
        // camera onClose callback closes backgroundThread
        // camera onClose callback closes analyzers
    }

    @Override
    public void cleanUp() {
        // Do not do anything. Cleaning up is actually triggered by destroying the surface view. No need for manual call.
    }

    public void pauseCamera() {
        if (this.captureSession != null) {
            this.imageReader.close();
            this.imageReader = null;
            this.captureSession.close();
            this.captureSession = null;
        }
    }

    @Override
    public void resumeCamera() {
        startPreview();
    }

    @Override
    void setTorchInternal(boolean value) {
        if (captureSession == null) {
            return;
        }
        try {
            if (value) {
                captureRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH);
            } else {
                captureRequestBuilder.set(CaptureRequest.FLASH_MODE, null);
            }
            if (captureSession != null) {
                captureSession.stopRepeating();
                captureSession.setRepeatingRequest(captureRequestBuilder.build(), null, backgroundHandler);
            }
            isTorchOn = value;
        } catch (CameraAccessException e) {
            isTorchOn = false;
            failed = true;
            throw new RuntimeException(e);
        }
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
        backgroundThread = null;
        backgroundHandler = null;
    }


    ///////////////////////////////////////////////////////////////////////////
    // SurfaceView holder callbacks (main init)
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Log.d(TAG, "Preview surface created, camera will be initialized soon");

        // This will simply select the camera
        selectCameraParameters();
        // Set the surface buffer size
        surfaceHolder.setFixedSize(resolution.currentPreviewResolution.x, resolution.currentPreviewResolution.y);

        // Go for camera. Camera object is given in callback.
        if (!isInEditMode()) {
            openCamera();
        }

        // Add targeting rectangle (must be done after everything is drawn and dimensions are known)
        computeCropRectangle();
        if (this.targetView == null) {
            addTargetView();
        }

        // Preview is started in onChange, after camera is plugged in.
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        Log.i(TAG, "surface changed");
        surfaceHolder.setFixedSize(resolution.currentPreviewResolution.x, resolution.currentPreviewResolution.y);
        startPreview();
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
            Log.v(TAG, "CameraDevice.StateCallback.onOpened");
            // Start preview when camera is actually opened
            CameraBarcodeScanViewV2.this.cameraDevice = cameraDevice;
            startPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            Log.v(TAG, "CameraDevice.StateCallback.onDisconnected");
            cameraDevice.close();
            CameraBarcodeScanViewV2.this.cameraDevice = null;
        }

        @Override
        public void onClosed(@NonNull CameraDevice camera) {
            Log.v(TAG, "CameraDevice.StateCallback.onClosed");
            super.onClosed(camera);

            if (null != backgroundThread) {
                Log.d(TAG, " * Background camera thread is closing");
                stopBackgroundThread();
            }

            if (null != frameAnalyser) {
                Log.d(TAG, " * Closing analyzers");
                frameAnalyser.close();
                frameAnalyser = null;
            }

            Log.i(TAG, "Camera scanner view has finished releasing all camera resources");
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {
            Log.e(TAG, "CameraDevice.StateCallback.onError on device " + i);
            cameraDevice.close();
            CameraBarcodeScanViewV2.this.cameraDevice = null;
            Toast toast = Toast.makeText(getContext(), "camera error " + i, Toast.LENGTH_LONG);
            toast.show();
        }
    };

    /**
     * Try to start the preview. Fails silently if surface or camera are not ready yet.
     */
    private void startPreview() {
        if (this.camPreviewSurfaceView == null) {
            Log.d(TAG, "Preview surface not ready yet");
            return;
        }
        if (this.cameraDevice == null) {
            Log.d(TAG, "Camera device not ready yet");
            return;
        }
        if (this.imageReader != null) {
            Log.d(TAG, "Image reader already created");
            return;
        }

        Log.i(TAG, "Initializing or reinitializing preview analysis loop");

        //this.camPreviewSurfaceView.getHolder().setFixedSize(resolution.currentPreviewResolution.x, resolution.currentPreviewResolution.y);
        Surface previewSurface = this.camPreviewSurfaceView.getHolder().getSurface();

        try {
            imageReader = ImageReader.newInstance(resolution.currentPreviewResolution.x, resolution.currentPreviewResolution.y, ImageFormat.YUV_420_888, frameAnalyser.maxBuffersInConcurrentUse()+1);
            imageReader.setOnImageAvailableListener(imageCallback, backgroundHandler);

            Log.d(TAG, "Capture session creation begins");
            this.cameraDevice.createCaptureSession(
                    Arrays.asList(previewSurface, imageReader.getSurface())
                    , cameraCaptureSessionStateCallback
                    , backgroundHandler);

            Log.d(TAG, "Configuration request sent");
        } catch (CameraAccessException e) {
            Log.e(TAG, "Configuration request could not be created " + e.getMessage());
            throw new RuntimeException(e);
        } catch (NullPointerException e) {
            Log.d(TAG, "Trying to start preview after camera shutdown - ignored.");
            return;
        }

        Log.d(TAG, "Preview analysis loop start method done");
    }

    private final CameraCaptureSession.StateCallback cameraCaptureSessionStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull final CameraCaptureSession cameraCaptureSession) {
            Log.i(TAG, "Capture session is now configured and will start the capture request loop " + cameraCaptureSession.hashCode());

            // Sanity check
            if (null == cameraDevice) {
                Log.e(TAG, "cannot start a session - camera not initialized");
                return;
            }

            // Only now redraw surface (otherwise deadlock)
            //CameraBarcodeScanViewV2.this.camPreviewSurfaceView.getHolder().setFixedSize(resolution.currentPreviewResolution.x, resolution.currentPreviewResolution.y);

            // We need our threads
            if (frameAnalyser == null) {
                reinitialiseFrameAnalyser();
            }

            // If here, preview session is open and we can start the actual preview.
            CameraBarcodeScanViewV2.this.captureSession = cameraCaptureSession;

            // Create builder
            try {
                captureRequestBuilder = CameraBarcodeScanViewV2.this.cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            } catch (CameraAccessException | IllegalStateException e) {
                Log.e(TAG, "cannot use createCaptureRequest " + e);
                cameraCaptureSession.close();
                return;
            }

            // Capture targets
            captureRequestBuilder.addTarget(CameraBarcodeScanViewV2.this.camPreviewSurfaceView.getHolder().getSurface());
            captureRequestBuilder.addTarget(imageReader.getSurface());

            // Full auto (without this AF & AE are mostly disabled)
            captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);

            // AB
            if (controlModeAb != null) {
                captureRequestBuilder.set(CaptureRequest.CONTROL_AE_ANTIBANDING_MODE, controlModeAb);
            }

            // AF & AE
            if (afZones > 0) {
                captureRequestBuilder.set(CaptureRequest.CONTROL_AF_REGIONS, new MeteringRectangle[]{getMeteringZone()});
            }
            if (controlModeAf != null) {
                Log.d(TAG, "Setting AF mode to " + controlModeAf);
                captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, controlModeAf);
            }

            //captureRequestBuilder.set(CaptureRequest.CONTROL_AE_LOCK, false);
            //captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
            //captureRequestBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_AUTO);

            // TODO: FPS
            //captureRequestBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, previewFpsRange);

            // GO for preview
            CameraBarcodeScanViewV2.this.captureRequest = captureRequestBuilder.build();

            try {
                captureSession.setRepeatingRequest(CameraBarcodeScanViewV2.this.captureRequest, null, backgroundHandler);
            } catch (CameraAccessException | IllegalStateException e) {
                Log.w(TAG, "Camera loop start has failed, this is usually due to changing resolution too fast. Error was: " + e.getMessage(), e);
                CameraBarcodeScanViewV2.this.closeCamera();
            }

            Log.i(TAG, "Camera repeating capture request was set up");
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            Log.e(TAG, "Capture session configuration failed " + session.hashCode());
            Toast toast = Toast.makeText(getContext(), "camera error - could not set preview", Toast.LENGTH_LONG);
            toast.show();
        }

        @Override
        public void onActive(@NonNull CameraCaptureSession session) {
            Log.d(TAG, "Capture session is getting active " + session.hashCode());
        }

        @Override
        public void onReady(@NonNull CameraCaptureSession session) {
            Log.d(TAG, "Capture session has nothing to process " + session.hashCode());
        }

        @Override
        public void onClosed(@NonNull CameraCaptureSession session) {
            Log.i(TAG, "Capture session has closed " + session.hashCode());
            if (imageReader != null) {
                Log.d(TAG, " * ImageReader closing");
                imageReader.close();
                imageReader = null;
            }
            captureSession = null;

            Log.d(TAG, " * Camera device closing");
            cameraDevice.close();
            cameraDevice = null;
        }
    };


    ///////////////////////////////////////////////////////////////////////////
    // Image reader
    ///////////////////////////////////////////////////////////////////////////

    final ImageReader.OnImageAvailableListener imageCallback = reader -> {
        Image image;
        try {
            image = reader.acquireLatestImage();
        } catch (IllegalStateException e) {
            Log.w(TAG, "Too many images being borrowed from reader", e);
            return;
        }
        if (image == null) {
            return;
        }

        // Get luminance buffer.
        try {
            // Sanity check
            assert (image.getPlanes().length == 3);

            ByteBuffer buffer = image.getPlanes()[0].getBuffer(); // Y.
            byte[] bytes = new byte[buffer.remaining()]; // TODO: reuse buffers.
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
            ctx.image = image;

            frameAnalyser.handleFrame(ctx);
        } catch (IllegalStateException e) {
            if (e.getMessage().equals("buffer is inaccessible")) {
                // This happens due to hardware bugs, and should be ignored.
                Log.w(TAG, "buffer is inaccessible - skipping frame", e);
                try {
                    image.close();
                } catch (Exception e2) {
                    // We do not care - what is important is to try to give the buffer back
                }
            } else if (e.getMessage().equals("Image is already closed")) {
                // Ignore - happens when we close the camera on some devices.
            } else {
                // Just crash
                throw e;
            }
        }
    };


    ///////////////////////////////////////////////////////////////////////////
    // Life cycle handlers
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void giveBufferBack(FrameAnalysisContext analysisContext) {
        analysisContext.image.close();
    }

    @Override
    public void setPreviewResolution(Point newResolution) {
        Log.d(TAG, "New preview resolution set");
        resolution.currentPreviewResolution = newResolution;
        this.startPreview();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Helpers
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private CameraCharacteristics getCharacteristics() {
        if (this.cameraId == null || this.cameraManager == null) {
            throw new IllegalStateException("camera not yet initialized, cannot get its characteristics");
        }
        try {
            return this.cameraManager.getCameraCharacteristics(this.cameraId);
        } catch (CameraAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getCameraDisplayOrientation() {
        Integer o = getCharacteristics().get(CameraCharacteristics.SENSOR_ORIENTATION);
        return o != null ? (180 - o % 360) : 0;
    }
}
