package com.enioka.scanner.camera;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
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
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.util.Arrays;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class ZCamera2 extends FrameLayout implements SurfaceHolder.Callback {
    private static final String TAG = "Camera2Scanner";
    protected static final int RECT_HEIGHT = 10;
    protected static final float MM_INSIDE_INCH = 25.4f;

    Context context;

    protected SurfaceView surfaceView = null;
    private Rect cropRect = new Rect();

    private String cameraId;
    private CameraCaptureSession captureSession;
    private CaptureRequest captureRequest;
    private CameraDevice cameraDevice;
    private ImageReader imageReader;

    private boolean isTorchSupported = false;
    private Size previewResolution;
    private Range<Integer> previewFpsRange;

    /**
     * A {@link Handler} for running technical tasks in the background.
     */
    private Handler backgroundHandler;
    private HandlerThread backgroundThread;


    public ZCamera2(@NonNull Context context) {
        super(context);
        this.context = context;
        init();
    }

    public ZCamera2(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    private void init() {
        //initOverlay();
        //return;
        startBackgroundThread();
        // This will simply select the camera
        selectCameraParameters();
        // This will add the preview view. Its completion callback calls openCamera (and initOverlay), which has a completion callback which starts the preview.
        initLayout();
    }

    private void initLayout() {
        initSurface();
    }

    private void initSurface() {
        if (this.surfaceView != null) {
            return;
        }

        surfaceView = new SurfaceView(context);
        this.addView(surfaceView);
        surfaceView.getHolder().addCallback(this); // this calls callback when ready.
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
            actualLayoutWidth = this.surfaceView.getMeasuredWidth();
            actualLayoutHeight = this.surfaceView.getMeasuredHeight();
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
        CameraManager manager = (CameraManager) context.getSystemService(android.content.Context.CAMERA_SERVICE);
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
                previewResolution = selectResolution(map.getOutputSizes(ImageFormat.YUV_420_888));
                Log.i(TAG, "Camera uses preview resolution: " + previewResolution.getWidth() + "*" + previewResolution.getHeight());
                previewFpsRange = selectFpsRange(characteristics);
                Log.i(TAG, "Camera uses FPS range: " + previewFpsRange);

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


    private Size selectResolution(Size[] choices) {
        return new Size(1440, 1080);
        //return new Size(800, 600);
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
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            throw new RuntimeException("missing use camera permission");
        }

        CameraManager manager = (CameraManager) context.getSystemService(android.content.Context.CAMERA_SERVICE);
        if (manager == null) {
            throw new RuntimeException("cannot use Camera2 API on this device - null CameraManager");
        }
        try {
            manager.openCamera(this.cameraId, this.stateCallback, backgroundHandler);
        } catch (CameraAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void closeCamera() {
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
        stopBackgroundThread();
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
        surfaceHolder.setFixedSize(this.previewResolution.getWidth(), this.previewResolution.getHeight());
        if (!isInEditMode()) {
            openCamera(); // will in turn start the preview.
        }
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
            ZCamera2.this.cameraDevice = cameraDevice;
            startPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            cameraDevice.close();
            ZCamera2.this.cameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {
            cameraDevice.close();
            ZCamera2.this.cameraDevice = null;
            Toast toast = Toast.makeText(context, "camera error " + i, Toast.LENGTH_LONG);
            toast.show();
        }
    };

    private void startPreview() {
        Surface surface = this.surfaceView.getHolder().getSurface();

        try {
            imageReader = ImageReader.newInstance(previewResolution.getWidth(), previewResolution.getHeight(), ImageFormat.YUV_420_888, 40);
            imageReader.setOnImageAvailableListener(imageCallback, backgroundHandler);

            final CaptureRequest.Builder captureRequestBuilder = this.cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            captureRequestBuilder.addTarget(imageReader.getSurface());

            this.cameraDevice.createCaptureSession(Arrays.asList(surface, imageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            if (null == cameraDevice) {
                                return;
                            }

                            // If here, preview session is open and we can start the actual preview.
                            captureSession = cameraCaptureSession;
                            captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
                            captureRequestBuilder.set(CaptureRequest.CONTROL_AE_LOCK, false);
                            captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
                            captureRequestBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_AUTO);
                            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                            captureRequestBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, previewFpsRange);

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
                            Toast toast = Toast.makeText(context, "camera error - could not set preview", Toast.LENGTH_LONG);
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
            Log.i(TAG, "RRRRRRRRRRRR");

            Image image = reader.acquireNextImage();
            if (image == null) {
                return;
            }
            //image.setCropRect(cropRect);

           /* ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            Log.i(TAG, "" + bytes.length);
            buffer.get(bytes);*/
            image.close();
        }
    };


    ///////////////////////////////////////////////////////////////////////////
    // Life cycle handlers
    ///////////////////////////////////////////////////////////////////////////


}
