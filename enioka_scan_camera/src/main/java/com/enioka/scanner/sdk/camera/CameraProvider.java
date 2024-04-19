package com.enioka.scanner.sdk.camera;

import android.view.View;

import com.enioka.scanner.api.proxies.ScannerDataCallbackProxy;
import com.enioka.scanner.api.proxies.ScannerStatusCallbackProxy;
import com.enioka.scanner.data.BarcodeType;

import java.util.Set;

/**
 * Provider for integrated camera scanner functionality, implementing the CameraScanner interface.
 */
public class CameraProvider implements CameraScanner {
    /**
     * Camera scanner
     */
    protected CameraBarcodeScanViewScanner cameraScanner;

    @Override
    public int getCameraViewId() {
        return R.id.camera_scan_view;
    }

    @Override
    public int getLayoutIdCamera() {
        return R.layout.activity_main_alt;
    }

    @Override
    public int getScannerToggleViewId() {
        return R.id.scanner_switch_zxing;
    }

    @Override
    public int getScannerTogglePauseId() {
        return R.id.scanner_switch_pause;
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
    public void setReaderMode(View cameraView, boolean readerMode) {
        if (cameraView instanceof CameraBarcodeScanView) {
            ((CameraBarcodeScanView) cameraView).setReaderMode(readerMode ? CameraReader.ZXING : CameraReader.ZBAR);
        } else {
            throw new IllegalArgumentException("cameraView must be an instance of CameraBarcodeScanView");
        }
    }
}
