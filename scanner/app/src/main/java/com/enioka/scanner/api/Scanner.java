package com.enioka.scanner.api;

import android.app.Activity;

import com.enioka.scanner.data.Barcode;

import java.util.List;

/**
 * The interface to implement by a laser scanner provider.
 */
public interface Scanner {
    enum Mode {
        /**
         * The scanner stops after one successful read. It must be rearmed.
         */
        SINGLE_SCAN,
        /**
         * The scanner waits for result post-treatment and auto rearms.
         */
        CONTINUOUS_SCAN,
        /**
         * The scanner is always ready to scan, not waiting for any result analysis. Results may be sent in batch.
         */
        BATCH
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // CALLBACKS
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Callback handling scanner init events
     */
    interface ScannerInitCallback {
        void onConnectionSuccessful(Scanner s);

        void onConnectionFailure(Scanner s);
    }

    /**
     * Callback to deal with data read by the scanner.
     */
    interface ScannerDataCallback {
        /**
         * Note: called on the UI thread.
         *
         * @param data the data read by the reader.
         */
        void onData(Scanner s, List<Barcode> data);
    }

    interface ScannerStatusCallback {
        /**
         * Called whenever the scanner has changed status.
         *
         * @param newStatus
         */
        void onStatusChanged(String newStatus);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Change ScannerDataCallback
     *
     * @param cb a callback to call when data is read.
     */
    void setDataCallBack(ScannerDataCallback cb);

    /**
     * Disconnect scanner from the App (the app does not need the scanner anymore)
     */
    void disconnect();

    /**
     * The app keeps the scanner for itself but does not need it immediately. It may free whatever resources it has, or ignore this call.
     */
    void pause();

    /**
     * Reverse the effects of {@link #pause()}. The scanner is once again ready to scan after this call. Status callback should be called if needed. Idempotent.
     */
    void resume();


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // BEEPS
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Short high beep to indicate successful scan
     */
    void beepScanSuccessful();

    /**
     * Long low beep to indicate unsuccessful scan
     */
    void beepScanFailure();

    /**
     * Different beep to indicate a completed barcode pairing
     */
    void beepPairingCompleted();


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // ILLUMINATION
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * If the device used has a way to illuminate the target, enable it. Idempotent.
     */
    void enableIllumination();

    /**
     * Reverse of {@link #enableIllumination()}
     */
    void disableIllumination();

    /**
     * See {@link #enableIllumination()}
     */
    void toggleIllumination();

    /**
     * True if the illumination method is activated.
     */
    boolean isIlluminationOn();


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // FUNCTION SUPPORT
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * True if at least one method of illumination (torch, laser...) is supported).
     */
    boolean supportsIllumination();

    String getProviderKey();
}
