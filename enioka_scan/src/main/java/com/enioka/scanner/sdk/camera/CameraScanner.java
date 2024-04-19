package com.enioka.scanner.sdk.camera;

import android.view.View;

import com.enioka.scanner.api.proxies.ScannerDataCallbackProxy;
import com.enioka.scanner.api.proxies.ScannerStatusCallbackProxy;
import com.enioka.scanner.data.BarcodeType;

import java.util.Set;

/**
 * The interface to implement by the camera scanner provider (via the camera scanner SDK).
 */
public interface CameraScanner {
    /**
     * Returns the ID of the camera view in the layout.
     */
    public int getCameraViewId();

    /**
     * Returns the ID of the layout containing the camera view.
     */
    public int getLayoutIdCamera();

    /**
     * Returns the ID of the view that toggles the scanner library reader.
     */
    public int getScannerToggleViewId();

    /**
     * Returns the ID of the view that toggles the pause of the scanner.
     */
    public int getScannerTogglePauseId();

    /**
     * Called to initialize the camera scanner with the given view and callbacks.
     */
    public void getCameraScanner(View cameraBarcodeScanView, ScannerDataCallbackProxy mHandler, final ScannerStatusCallbackProxy statusCallback, final Set<BarcodeType> symbologySelection);

    /**
     * Returns whether the camera scanner is initialized.
     */
    public boolean isCameraScannerInitialized();

    /**
     * Resets the camera scanner.
     */
    public void reset();

    /**
     * Toggles the illumination of the camera.
     */
    public void toggleIllumination();

    /**
     * Disconnects the camera.
     */
    public void disconnect();

    /**
     * Pauses the camera.
     */
    public void pauseCamera(View cameraView);

    /**
     * Resumes the camera.
     */
    public void resumeCamera(View cameraView);

    /**
     * Returns whether the illumination is on.
     */
    public boolean isIlluminationOn();

    /**
     * Sets the reader engine of the camera scanner.
     */
    public void setReaderMode(View cameraView, boolean readerMode);
}
