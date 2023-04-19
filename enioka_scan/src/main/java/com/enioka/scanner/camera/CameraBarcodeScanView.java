package com.enioka.scanner.camera;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;

/**
 * Helper view that encapsulates the ZBar (default) and ZXing (option) barcode analysis engines.
 * To be directly reused in layouts.
 * This view either uses Camera 1 or Camera 2 API according to the Android version.
 */
public class CameraBarcodeScanView extends FrameLayout {
    protected final static String LOG_TAG = "ScannerActivity";

    private final CameraApiLevel api;
    private CameraBarcodeScanViewBase proxiedView;

    public CameraBarcodeScanView(@NonNull Context context) {
        super(context);
        api = guessBestApiLevel();

        setLayout(null);
    }

    public CameraBarcodeScanView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        api = guessBestApiLevel();

        setLayout(attrs);
    }

    private CameraApiLevel guessBestApiLevel() {
        CameraApiLevel res = CameraApiLevel.Camera1;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            CameraManager cameraManager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);
            if (cameraManager == null) {
                Log.i(LOG_TAG, "Device supposedly supports camera V2 but cannot open camera manager - not using it");
                return CameraApiLevel.Camera1;
            }

            CameraCharacteristics characteristics = null;
            StreamConfigurationMap map = null;
            try {
                for (String cameraId : cameraManager.getCameraIdList()) {
                    characteristics = cameraManager.getCameraCharacteristics(cameraId);

                    // We only want back cameras.
                    Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                    if (facing == null || facing != CameraCharacteristics.LENS_FACING_BACK) {
                        continue;
                    }

                    // Do not try V2 if this is only a legacy device, support is usually subpar.
                    if (characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL) == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
                        Log.i(LOG_TAG, "Device supports camera V2 but only in legacy mode - not using it");
                        return CameraApiLevel.Camera1;
                    }
                }
            } catch (CameraAccessException e) {
                throw new RuntimeException(e);
            }

            return CameraApiLevel.Camera2;
        }

        return CameraApiLevel.Camera1;
    }

    private void setLayout(AttributeSet attrs) {
        switch (api) {
            case Camera1:
                proxiedView = new CameraBarcodeScanViewV1(getContext(), attrs);
                break;
            case Camera2:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    proxiedView = new CameraBarcodeScanViewV2(getContext(), attrs);
                }
                break;
        }
        Log.i(LOG_TAG, "Using camera version: " + proxiedView.getClass().getName());
        this.addView(proxiedView);
    }

    public CameraBarcodeScanViewBase getProxiedView() {
        return proxiedView;
    }
}
