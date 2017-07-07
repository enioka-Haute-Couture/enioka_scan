package com.enioka.scanner.api;

import android.app.Activity;
import android.content.Context;

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
        void onConnectionSuccessful();

        void onConnectionFailure();
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
        void onData(List<Barcode> data);
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
     * Called once per application launch.
     *
     * @param ctx The application
     * @param cb1 a callback to call when data is read.
     */
    void initialize(Activity ctx, ScannerInitCallback cb0, ScannerDataCallback cb1, ScannerStatusCallback cb2, Mode mode);

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
}
