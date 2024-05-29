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
        idResources.put("camera_view_id", R.id.camera_scan_view);
        idResources.put("layout_id_camera", R.layout.activity_camera);
        idResources.put("scanner_toggle_view_id", R.id.scanner_switch_zxing);
        idResources.put("scanner_toggle_pause_id", R.id.scanner_switch_pause);
        idResources.put("card_last_scan_id", R.id.card_camera_last_scan);
        idResources.put("constraint_layout_id", R.id.constraint_layout_main_activity);
        idResources.put("scanner_flashlight_id", R.id.scanner_flashlight);
        idResources.put("scanner_bt_provider_logs", R.id.scanner_bt_provider_logs);

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
    public void setPreviewRatioMode(View cameraView, int previewRatioMode) {
        if (cameraView instanceof CameraBarcodeScanView) {
            ((CameraBarcodeScanView) cameraView).setPreviewRatioMode(previewRatioMode);
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
}
