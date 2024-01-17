package com.enioka.scanner.camera;

import static com.enioka.scanner.helpers.Permissions.PERMISSIONS_CAMERA;
import static com.enioka.scanner.helpers.Permissions.hasPermissionSet;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
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
import android.util.AttributeSet;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.view.SurfaceHolder;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * V2 implementation of the camera view. Default implementation.
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
class CameraBarcodeScanViewV2 extends CameraBarcodeScanViewBase<Image> {
    private static final Semaphore criticalInitBlock = new Semaphore(1);
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
    private boolean stopping = false;

    /**
     * Unbounded cache of image buffers.
     */
    private final ConcurrentLinkedQueue<byte[]> imageBufferQueue = new ConcurrentLinkedQueue<>();

    public CameraBarcodeScanViewV2(@NonNull Context context) {
        super(context);
        init();
    }

    public CameraBarcodeScanViewV2(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Checks all attributes necessary for camera operations and returns true if any is null.
     * None of these attributes are expected to be null unless the camera is either uninitialized, paused or closed.
     *
     * @param caller The name of the caller function, for logging
     * @return true if any camera-related attribute is null.
     */
    private boolean anyAttributeMissing(String caller) {
        if (cameraManager == null) {
            Log.w(TAG, caller + ": No cameraManager instance, make sure camera was properly initialized");
            return true;
        }
        if (captureSession == null) {
            Log.w(TAG, caller + ": No captureSession instance, make sure camera was properly initialized and `pauseCamera()` or `closeCamera()` were not called previously");
            return true;
        }
        if (captureRequestBuilder == null) {
            Log.w(TAG, caller + ": No captureRequestBuilder instance, make sure camera was properly initialized and `closeCamera()` was not called previously");
            return true;
        }
        if (cameraDevice == null) {
            Log.w(TAG, caller + ": No cameraDevice instance, make sure camera was properly initialized and not used by another application or view, and `closeCamera()` was not called previously");
            return true;
        }
        if (imageReader == null) {
            Log.w(TAG, caller + ": No imageReader instance, make sure camera was properly initialized and `pauseCamera()` or `closeCamera()` were not called previously");
            return true;
        }
        return false;
    }

    private void init() {
        Log.d(TAG, "Camera2 specific initialization start");

        // A thread for dealing with camera technical operations.
        startBackgroundThread();
    }

    private void acquire() {
        try {
            if (!criticalInitBlock.tryAcquire(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException("cannot acquire camera semaphore - it may be locked by another activity/fragment");
            }
        } catch (InterruptedException e) {
            if (Thread.currentThread().isInterrupted()) {
                throw new RuntimeException(e);
            }
        }
    }

    private void release() {
        criticalInitBlock.release();
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
        Log.i(TAG, "Selected camera id: " + this.cameraId);

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
        if (resolution.currentPreviewResolution != null) {
            Log.d(TAG, "Reusing previously chosen resolution");
            return;
        }
        for (Size s : choices) {
            resolution.supportedPreviewResolutions.add(new Point(s.getWidth(), s.getHeight()));
        }
        ViewHelpersResolution.setPreviewResolution(getContext(), resolution, this);
    }

    private Range<Integer> selectFpsRange(CameraCharacteristics characteristics) {
        Range<Integer>[] ranges = characteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);
        if (ranges == null || ranges.length == 0) {
            return new Range<>(10, 30);
        }

        Log.d(TAG, "Supported FPS ranges:");
        Range<Integer> result = ranges[0];
        for (Range<Integer> range : ranges) {
            Log.d(TAG, "\t" + range.getLower() + "," + range.getUpper());
            if (range.getLower() >= result.getLower() && range.getUpper() >= result.getUpper() && range.getLower() * 1.5 > range.getUpper()) {
                result = range;
            }
        }

        Log.i(TAG, "Requesting preview FPS range at " + result.getLower() + "," + result.getUpper());
        return result;
    }

    private MeteringRectangle getMeteringZone() {
        // Coordinate system is 0 topleft / 1 bottomright
        int x0 = this.cropRect.left;
        int y0 = this.cropRect.top;
        int x1 = this.cropRect.right;
        int y1 = this.cropRect.bottom;
        Log.d(TAG, "Using metering zone (" + x0 + "," + y0 + ") (" + x1 + "," + y1 + ")");
        return new MeteringRectangle(x0, y0, x1 - x0, y1 - y0, 1000);
    }

    protected void refreshAutofocusZone() {
        if (anyAttributeMissing("refreshAutofocusZone")) {
            return;
        }

        if (afZones > 0) {
            CameraCaptureSession.CaptureCallback captureHandler = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    if (request.getTag() == "FOCUS_TAG") {
                        Log.d(TAG, "Camera focus change was successful.");
                        try {
                            // Remove the AF trigger from the builder to continue repeating captures
                            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, controlModeAf);
                            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, null);
                            captureSession.setRepeatingRequest(captureRequestBuilder.build(), null, null);
                        } catch (CameraAccessException e) {
                            Log.w(TAG, "Camera loop continuation has failed, this is usually due to changing resolution too fast. Error was: " + e.getMessage(), e);
                            CameraBarcodeScanViewV2.this.closeCamera();
                        }
                    }
                }

                @Override
                public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
                    super.onCaptureFailed(session, request, failure);
                    Log.w(TAG, "Camera focus change has failed.");
                    CameraBarcodeScanViewV2.this.closeCamera();
                }
            };

            try {
                // Stop current autofocus process
                captureSession.stopRepeating();
                captureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraCharacteristics.CONTROL_AF_TRIGGER_CANCEL);
                captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CameraCharacteristics.CONTROL_AF_MODE_OFF);
                captureSession.capture(captureRequestBuilder.build(), captureHandler, backgroundHandler);

                // Update the autofocus zone
                captureRequestBuilder.set(CaptureRequest.CONTROL_AF_REGIONS, new MeteringRectangle[]{getMeteringZone()});
                captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraCharacteristics.CONTROL_MODE_AUTO);
                // While CONTROL_AF_MODE_CONTINUOUS_PICTURE is usually faster, it yields poor
                // results whenever the focus zone changes, both for newer and older devices.
                // CONTROL_AF_MODE_AUTO handles focus area changes much better.
                // The original mode will be set back as soon as the manual focus is done, which means the manual focus will likely be overridden
                // immediately, but it will force the focus to refresh itself, which gives it a chance to find the expected level.
                captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CameraCharacteristics.CONTROL_AF_MODE_AUTO);
                captureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraCharacteristics.CONTROL_AF_TRIGGER_START);
                captureRequestBuilder.setTag("FOCUS_TAG");
                captureSession.capture(captureRequestBuilder.build(), captureHandler, backgroundHandler);
            } catch (CameraAccessException | IllegalStateException e) {
                Log.w(TAG, "Camera loop update has failed, this is usually due to changing resolution too fast. Error was: " + e.getMessage(), e);
                CameraBarcodeScanViewV2.this.closeCamera();
            }
        }
    }

    private void openCamera() {
        Log.d(TAG, "Trying to open camera from CameraManager " + this.hashCode());
        if (!hasPermissionSet(getContext(), PERMISSIONS_CAMERA)) {
            throw new RuntimeException("missing use camera permission");
        }
        acquire();

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
        Log.d(TAG, "Starting camera release " + this.hashCode());

        if (stopping) {
            Log.d(TAG, "Camera release start request skipped because already closing");
            return;
        }

        // Stop feeding the analyzers at once (we will wait for them in the end)
        stopping = true;
        if (null != frameAnalyser) {
            frameAnalyser.cancelPendingWork();
        }

        if (null != captureSession) {
            Log.d(TAG, " * Closing camera capture session");

            captureSession.close();
            captureSession = null;
        }

        if (null != captureRequestBuilder) {
            Log.d(TAG, " * Clearing camera capture builder");

            captureRequestBuilder.removeTarget(this.camPreviewSurfaceView.getHolder().getSurface());
            captureRequestBuilder = null;
        }

        if (null != cameraDevice) {
            Log.d(TAG, " * Camera device closing");
            cameraDevice.close();
            cameraDevice = null;
        }

        if (imageReader != null) {
            Log.d(TAG, " * ImageReader closing");
            imageReader.close();
            imageReader = null;
        }

        Log.d(TAG, "All close requests sent " + this.hashCode());

        // capture session is closed above.
        // reader is closed above.
        // camera device  is closed above.
        // camera onClose callback closes backgroundThread
        // camera onClose callback closes analyzers
    }

    @Override
    public void cleanUp() {
        this.closeCamera();
    }

    @Override
    public void pauseCamera() {
        // This version actually simply stops the capture session, but keeps the camera open.
        // Preview is frozen when session is closed.
        // TODO: add an actual pause, by stopping the repeating loop of the session instead of destroying it.
        if (this.captureSession != null) {
            Log.i(TAG, "Stopping capture session");
            try {
                this.imageReader.close();
            } catch (IllegalArgumentException e) {
                Log.i(TAG, "image reader would not close");
            }
            this.imageReader = null;
            try {
                this.captureSession.close();
            } catch (IllegalArgumentException e) {
                Log.i(TAG, "captureSession would not close");
            }
            this.captureSession = null;
        }
        post(() -> {
            if (this.targetView != null) {
                this.targetView.pauseTarget();
            }
        });
    }

    @Override
    public void resumeCamera() {
        startPreview();
        post(() -> {
            if (this.targetView != null) {
                this.targetView.resumeTarget();
            }
        });
    }

    @Override
    void setTorchInternal(boolean value) {
        if (anyAttributeMissing("setTorchInternal")) {
            return;
        }

        try {
            if (value) {
                captureRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH);
            } else {
                captureRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
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
        Log.d(TAG, "Starting background thread handler " + this.hashCode());
        backgroundThread = new HandlerThread("CameraBackground");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    private void stopBackgroundThread() {
        Log.d(TAG, "Stopping background thread handler " + this.hashCode());
        backgroundThread.quitSafely();
        backgroundThread = null;
        backgroundHandler = null;
    }


    ///////////////////////////////////////////////////////////////////////////
    // SurfaceView holder callbacks (main init)
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        super.surfaceCreated(surfaceHolder);

        Log.d(TAG, "Preview surface created, camera will be initialized soon " + this.hashCode());

        // This will select the camera and all the resolution, exposure, AF... parameters.
        selectCameraParameters();

        // Set the preview surface buffer size
        camPreviewSurfaceView.post(() -> surfaceHolder.setFixedSize(resolution.currentPreviewResolution.x, resolution.currentPreviewResolution.y));
        camPreviewSurfaceView.post(camPreviewSurfaceView::requestLayout);

        // Go for camera. Camera object is given in callback.
        if (!isInEditMode()) {
            // Do it on the view thread to ensure the resize is already taken into account.
            camPreviewSurfaceView.post(this::openCamera);
        }

        // Preview is actually started in CameraDevice.StateCallback.onOpened, after camera is plugged in.
        // This is triggered by the openCamera() call above.
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int pixelFormat, int newWidth, int newHeight) {
        Log.i(TAG, "surface changed " + this.hashCode());
        camPreviewSurfaceView.post(this::startPreview);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        Log.i(TAG, "surface destroyed " + this.hashCode());
        closeCamera();
    }


    ///////////////////////////////////////////////////////////////////////////
    // CameraDevice.StateCallback
    ///////////////////////////////////////////////////////////////////////////

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            Log.v(TAG, "CameraDevice.StateCallback.onOpened " + CameraBarcodeScanViewV2.this.hashCode());
            // Start preview when camera is actually opened
            CameraBarcodeScanViewV2.this.cameraDevice = cameraDevice;
            startPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            Log.v(TAG, "CameraDevice.StateCallback.onDisconnected " + CameraBarcodeScanViewV2.this.hashCode());
            cameraDevice.close();
            CameraBarcodeScanViewV2.this.cameraDevice = null;
        }

        @Override
        public void onClosed(@NonNull CameraDevice camera) {
            Log.v(TAG, "CameraDevice.StateCallback.onClosed " + CameraBarcodeScanViewV2.this.hashCode());
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

            if (camPreviewSurfaceView != null && camPreviewSurfaceView.getHolder() != null && camPreviewSurfaceView.getHolder().getSurface() != null) {
                // Bug #108 - if surface is not released, camera will be closed when surface is destroyed. Really. Prevents other activities to use camera.
                camPreviewSurfaceView.getHolder().getSurface().release();
            }

            imageBufferQueue.clear();
            croppedImageBufferQueue.clear();

            stopping = false;
            release();
            Log.i(TAG, "Camera scanner view has finished releasing all camera resources " + CameraBarcodeScanViewV2.this.hashCode());
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
    private synchronized void startPreview() {
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

        // We need the worker count to init the preview
        initializeFrameAnalyzerIfNeeded();

        try {
            imageReader = ImageReader.newInstance(resolution.currentPreviewResolution.x, resolution.currentPreviewResolution.y, ImageFormat.YUV_420_888, frameAnalyser.maxBuffersInConcurrentUse() + 1);
            imageReader.setOnImageAvailableListener(imageCallback, backgroundHandler);

            Log.d(TAG, "Capture session creation begins");
            this.cameraDevice.createCaptureSession(
                    Arrays.asList(
                            this.camPreviewSurfaceView.getHolder().getSurface(),
                            imageReader.getSurface())
                    , cameraCaptureSessionStateCallback
                    , backgroundHandler);

            Log.d(TAG, "Configuration request sent");
        } catch (CameraAccessException e) {
            Log.e(TAG, "Configuration request could not be created " + e.getMessage());
            throw new RuntimeException(e);
        } catch (NullPointerException e) {
            Log.d(TAG, "Trying to start preview after camera shutdown - ignored.", e);
            return;
        }

        Log.d(TAG, "Preview analysis loop start method done");
    }

    private final CameraCaptureSession.StateCallback cameraCaptureSessionStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull final CameraCaptureSession cameraCaptureSession) {
            Log.i(TAG, "Capture session is now configured and will start the capture request loop " + CameraBarcodeScanViewV2.this.hashCode());

            // Sanity check
            if (null == cameraDevice) {
                Log.e(TAG, "cannot start a session - camera not initialized");
                return;
            }

            try {
                // We need our threads
                initializeFrameAnalyzerIfNeeded();

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
                if (imageReader == null) {
                    Log.d(TAG, "stopping init - view is being stopped");
                    return;
                }
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

                captureRequestBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, previewFpsRange);

                // GO for preview
                CameraBarcodeScanViewV2.this.captureRequest = captureRequestBuilder.build();

                try {
                    captureSession.setRepeatingRequest(CameraBarcodeScanViewV2.this.captureRequest, null, backgroundHandler);
                } catch (CameraAccessException | IllegalStateException e) {
                    Log.w(TAG, "Camera loop start has failed, this is usually due to changing resolution too fast. Error was: " + e.getMessage(), e);
                    CameraBarcodeScanViewV2.this.closeCamera();
                }
            } catch (final NullPointerException e) {
                Log.e(TAG, "Camera was destroyed while the initialization process was running", e);
            }

            Log.i(TAG, "Camera repeating capture request was set up " + CameraBarcodeScanViewV2.this.hashCode());
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            Log.e(TAG, "Capture session configuration failed " + session.hashCode());
            Toast toast = Toast.makeText(getContext(), "camera error - could not set preview", Toast.LENGTH_LONG);
            toast.show();
        }

        @Override
        public void onActive(@NonNull CameraCaptureSession session) {
            Log.d(TAG, "Capture session is getting active " + CameraBarcodeScanViewV2.this.hashCode());
        }

        @Override
        public void onReady(@NonNull CameraCaptureSession session) {
            Log.d(TAG, "Capture session has nothing to process " + CameraBarcodeScanViewV2.this.hashCode());
        }

        @Override
        public void onClosed(@NonNull CameraCaptureSession session) {
            Log.i(TAG, "Capture session has closed " + session.hashCode());
        }
    };


    ///////////////////////////////////////////////////////////////////////////
    // Image reader
    ///////////////////////////////////////////////////////////////////////////

    final ImageReader.OnImageAvailableListener imageCallback = reader -> {
        if (stopping) {
            // no need to fetch images while we are stopping everything
            return;
        }

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

        // Create the analysis context containing the luminance plane
        FrameAnalysisContext<Image> ctx = new FrameAnalysisContext<>();
        try {
            // Sanity check
            if (image.getPlanes().length != 3 || image.getPlanes()[0] == null) {
                image.close();
                return;
            }

            // Sanity check
            if (image.getPlanes()[0].getPixelStride() != 1 || image.getPlanes()[0].getBuffer() == null) {
                throw new RuntimeException("not a luminance buffer");
            }

            // Luminance
            ByteBuffer luminanceBuffer = image.getPlanes()[0].getBuffer(); // Y.

            // Get the byte array and crop it.
            if (luminanceBuffer.hasArray()) {
                ctx.croppedPicture = this.extractBarcodeRectangle(luminanceBuffer.array(), luminanceBuffer.remaining());
            } else {
                // We cannot crop a ByteBuffer directly - it is not a from/length operation, so we need the whole buffer.
                byte[] tempBuffer = getCachedImageBuffer(luminanceBuffer.remaining());
                luminanceBuffer.get(tempBuffer);
                ctx.croppedPicture = this.extractBarcodeRectangle(tempBuffer, tempBuffer.length);
                imageBufferQueue.add(tempBuffer);
            }

            // We want to keep a reference to the image in order to close it when we are done with it.
            ctx.originalImage = image;

            // Submit to the analyser (in another thread)
            frameAnalyser.handleFrame(ctx);
        } catch (IllegalStateException e) {
            Log.w("exception", e);
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

    private final AtomicInteger bufferCount = new AtomicInteger(0);

    private byte[] getCachedImageBuffer(int desiredLength) {
        byte[] res;

        while (true) {
            res = imageBufferQueue.poll();
            if (res == null) {
                // Queue is empty, create a new buffer
                Log.d(TAG, "Creating new buffer (MB) " + ((int) (desiredLength / 1024 / 1024)) + " count is " + bufferCount.incrementAndGet());
                return new byte[desiredLength];
            }
            if (res.length != desiredLength) {
                Log.i(TAG, "Discarding old buffer of length " + res.length + " (requested " + desiredLength + ") " + bufferCount.decrementAndGet());
            } else {
                return res;
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Life cycle handlers
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void giveBufferBackInternal(FrameAnalysisContext<Image> analysisContext) {
        analysisContext.originalImage.close();
    }

    @Override
    public void setPreviewResolution(Point newResolution) {
        Log.d(TAG, "New preview resolution set");
        pauseCamera();
        resolution.currentPreviewResolution = newResolution;
        camPreviewSurfaceView.post(() -> camPreviewSurfaceView.getHolder().setFixedSize(resolution.currentPreviewResolution.x, resolution.currentPreviewResolution.y));
        camPreviewSurfaceView.post(camPreviewSurfaceView::requestLayout);
        // startPreview is implicit in surfaceChanged
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
    public int getCameraOrientationRelativeToDeviceNaturalOrientation() {
        Integer o = getCharacteristics().get(CameraCharacteristics.SENSOR_ORIENTATION);
        return o != null ? (180 - o % 360) : 0;
    }

    @Override
    int getCameraFace() {
        Integer o = getCharacteristics().get(CameraCharacteristics.LENS_FACING);
        switch (o) {
            case CameraCharacteristics.LENS_FACING_FRONT:
                return 1;
            case CameraCharacteristics.LENS_FACING_BACK:
                return 0;
            default:
                throw new IllegalStateException("wrong camera face");
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Camera as a camera
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private YuvImage mediaImageToYuvImage(Image image) {
        /*
        Source: https://blog.minhazav.dev/how-to-convert-yuv-420-sp-android.media.Image-to-Bitmap-or-jpeg/#how-to-convert-yuv_420_888-image-to-yuvimage
         */
        if (image.getFormat() != ImageFormat.YUV_420_888) {
            throw new IllegalArgumentException("Invalid image format");
        }

        int width = image.getWidth();
        int height = image.getHeight();

        // Order of U/V channel guaranteed, read more:
        // https://developer.android.com/reference/android/graphics/ImageFormat#YUV_420_888
        Image.Plane yPlane = image.getPlanes()[0];
        Image.Plane uPlane = image.getPlanes()[1];
        Image.Plane vPlane = image.getPlanes()[2];

        ByteBuffer yBuffer = yPlane.getBuffer();
        ByteBuffer uBuffer = uPlane.getBuffer();
        ByteBuffer vBuffer = vPlane.getBuffer();

        // Full size Y channel and quarter size U+V channels.
        int numPixels = (int) (width * height * 1.5f);
        byte[] nv21 = new byte[numPixels];
        int index = 0;

        // Copy Y channel.
        int yRowStride = yPlane.getRowStride();
        int yPixelStride = yPlane.getPixelStride();
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                nv21[index++] = yBuffer.get(y * yRowStride + x * yPixelStride);
            }
        }

        // Copy VU data; NV21 format is expected to have YYYYVU packaging.
        // The U/V planes are guaranteed to have the same row stride and pixel stride.
        int uvRowStride = uPlane.getRowStride();
        int uvPixelStride = uPlane.getPixelStride();
        int uvWidth = width / 2;
        int uvHeight = height / 2;

        for (int y = 0; y < uvHeight; ++y) {
            for (int x = 0; x < uvWidth; ++x) {
                int bufferIndex = (y * uvRowStride) + (x * uvPixelStride);
                // V channel.
                nv21[index++] = vBuffer.get(bufferIndex);
                // U channel.
                nv21[index++] = uBuffer.get(bufferIndex);
            }
        }
        return new YuvImage(
                nv21, ImageFormat.NV21, width, height, null);
    }

    @Override
    public byte[] getLatestSuccessfulScanJpeg() {
        if (lastSuccessfulScanData != null) {
            Log.d(TAG, "Convert last saved image to JPEG");
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            YuvImage img = mediaImageToYuvImage((Image) lastSuccessfulScanData.originalImage);
            Rect imgRect = new Rect(0, 0, resolution.currentPreviewResolution.x, resolution.currentPreviewResolution.y);
            img.compressToJpeg(imgRect, 100, buffer);

            /* // Save as file
            File sdCardFile = new File(Environment.getExternalStorageDirectory() + "/" + "scanV2_" + new Random().nextInt() + ".jpg");
            OutputStream s = null;
            try {
                s = new FileOutputStream(sdCardFile, false);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            img.compressToJpeg(imgRect, 100, s);
            try {
                s.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            */

            return buffer.toByteArray();
        } else {
            Log.d(TAG, "No saved image");
            return null;
        }
    }
}
