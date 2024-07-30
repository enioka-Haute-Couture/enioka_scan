package com.enioka.scanner.sdk.camera;

import android.view.View;

import com.enioka.scanner.api.proxies.ScannerDataCallbackProxy;
import com.enioka.scanner.api.proxies.ScannerStatusCallbackProxy;
import com.enioka.scanner.data.BarcodeType;

import java.util.HashMap;
import java.util.Set;

/**
 * Provider for integrated camera scanner functionality, implementing the CameraScanner interface.
 */
public class CameraProvider implements CameraScannerProvider {
    /**
     * Camera scanner
     */
    protected CameraBarcodeScanViewScanner cameraScanner;

    @Override
    public HashMap<String, Integer> getIdResources() {
        HashMap<String, Integer> idResources = new HashMap<>();
        idResources.put("camera_view_id", R.id.cameraScanView);
        idResources.put("layout_id_camera", R.layout.activity_camera);
        idResources.put("scanner_toggle_view_id", R.id.scannerSwitchZxing);
        idResources.put("scanner_toggle_pause_id", R.id.scannerSwitchPause);
        idResources.put("card_last_scan_id", R.id.cardCameraLastScan);
        idResources.put("constraint_layout_id", R.id.constraintLayoutMainActivity);
        idResources.put("scanner_flashlight_id", R.id.scannerFlashlight);
        idResources.put("scanner_bt_provider_logs", R.id.scannerBtProviderLogs);

        return idResources;
    }

    @Override
    public void getCameraScanner(View cameraBarcodeScanView, ScannerDataCallbackProxy mHandler, final ScannerStatusCallbackProxy statusCallback, final Set<BarcodeType> symbologySelection) {
        this.cameraScanner = new CameraBarcodeScanViewScanner((CameraBarcodeScanView) cameraBarcodeScanView, mHandler, statusCallback, symbologySelection);
    }

    @Override
    public boolean isCameraScannerInitialized() {
        return this.cameraScanner != null;
    }

    @Override
    public void reset() {
        this.cameraScanner = null;
    }

    @Override
    public void toggleIllumination() {
        if (this.cameraScanner == null) {
            throw new IllegalStateException("Camera scanner not initialized");
        }
        this.cameraScanner.toggleIllumination();
    }

    @Override
    public void disconnect() {
        if (this.cameraScanner == null) {
            throw new IllegalStateException("Camera scanner not initialized");
        }
        this.cameraScanner.disconnect();
    }

    @Override
    public void pauseCamera(View cameraView) {
        if (cameraView instanceof CameraBarcodeScanView) {
            ((CameraBarcodeScanView) cameraView).pauseCamera();
        }
    }

    @Override
    public void resumeCamera(View cameraView) {
        if (cameraView instanceof CameraBarcodeScanView) {
            ((CameraBarcodeScanView) cameraView).resumeCamera();
        }
    }

    @Override
    public boolean isIlluminationOn() {
        if (this.cameraScanner == null) {
            return false;
        }
        return this.cameraScanner.isIlluminationOn();
    }

    @Override
    public void setPreviewRatioMode(View cameraView, AspectRatioMode mode) {
        if (cameraView instanceof CameraBarcodeScanView) {
            ((CameraBarcodeScanView) cameraView).setPreviewRatioMode(mode);
        } else {
            throw new IllegalArgumentException("cameraView must be an instance of CameraBarcodeScanView");
        }
    }

    @Override
    public void setReaderMode(View cameraView, boolean readerMode) {
        if (cameraView instanceof CameraBarcodeScanView) {
            ((CameraBarcodeScanView) cameraView).setReaderMode(readerMode ? CameraReader.ZXING : CameraReader.ZBAR);
        } else {
            throw new IllegalArgumentException("cameraView must be an instance of CameraBarcodeScanView");
        }
    }

    @Override
    public void orientationChanged(View cameraView) {
        if (cameraView instanceof CameraBarcodeScanView) {
            ((CameraBarcodeScanView) cameraView).orientationChanged();
        } else {
            throw new IllegalArgumentException("cameraView must be an instance of CameraBarcodeScanView");
        }
    }

    @Override
    public void setTargetPosition(View cameraView, float y) {
        if (cameraView instanceof CameraBarcodeScanView) {
            ((CameraBarcodeScanView) cameraView).setTargetPosition(y);
        } else {
            throw new IllegalArgumentException("cameraView must be an instance of CameraBarcodeScanView");
        }
    }

    @Override
    public void setTargetDimension(View cameraView, float width, float height) {
        if (cameraView instanceof CameraBarcodeScanView) {
            ((CameraBarcodeScanView) cameraView).setTargetDimension(width, height);
        } else {
            throw new IllegalArgumentException("cameraView must be an instance of CameraBarcodeScanView");
        }
    }
}
