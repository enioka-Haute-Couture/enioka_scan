package com.enioka.scanner.sdk.camera;

import android.view.View;

import com.enioka.scanner.api.proxies.ScannerDataCallbackProxy;
import com.enioka.scanner.api.proxies.ScannerStatusCallbackProxy;
import com.enioka.scanner.data.BarcodeType;

import java.util.HashMap;
import java.util.Set;

/**
 * The interface to implement by the camera scanner provider (via the camera scanner SDK).
 */
public interface CameraScannerProvider {
    /**
     * Returns hashmap of the ID resources.
     * Contains the following keys:
     * - layout_id_camera: The ID of the layout containing the camera view.
     * - camera_view_id: The ID of the camera view in the layout.
     * - scanner_toggle_view_id: The ID of the view that toggles the scanner library reader.
     * - scanner_toggle_pause_id: The ID of the view that toggles the pause of the scanner.
     * - card_last_scan_id: ID of the card view that displays the last scan.
     * - constraint_layout_id: The ID of the constraint layout inside the camera layout.
     * - scanner_flashlight_id: The ID of the optional ImageButton on which to press to toggle the flashlight/illumination.
     * - scanner_bt_keyboard_id: The ID of the optional ImageButton on which to press to manually switch to keyboard mode.
     */
    public HashMap<String, Integer> getIdResources();

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
