package com.enioka.scanner.api;

import android.support.annotation.Nullable;

import com.enioka.scanner.data.Barcode;

import java.util.List;
import java.util.Map;

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

    public static final String SCANNER_STATUS_SCANNER_SN = "SCANNER_STATUS_SCANNER_SN";
    public static final String SCANNER_STATUS_SCANNER_MODEL = "SCANNER_STATUS_SCANNER_MODEL";
    public static final String SCANNER_STATUS_BATTERY_SN = "SCANNER_STATUS_BATTERY_SN";
    public static final String SCANNER_STATUS_BATTERY_MODEL = "SCANNER_STATUS_BATTERY_MODEL";
    public static final String SCANNER_STATUS_BATTERY_WEAR = "SCANNER_STATUS_BATTERY_WEAR";
    public static final String SCANNER_STATUS_BATTERY_CHARGE = "SCANNER_STATUS_BATTERY_CHARGE";
    public static final String SCANNER_STATUS_FIRMWARE = "SCANNER_STATUS_FIRMWARE";
    public static final String SCANNER_STATUS_BT_MAC = "SCANNER_STATUS_BT_MAC";


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

    /**
     * Callback to deal with scanner status changes.
     */
    interface ScannerStatusCallback {
        /**
         * Enum describing the scanner's lifecycle and current status.
         * Some elements may not be used depending on which scanner SDK is used.
         */
        enum Status {
            /** The scanner is waiting for a connection. */
            WAITING,
            /** The scanner is in the process of connecting. */
            CONNECTING,
            /** The scanner disconnected but is trying to reconnect. */
            RECONNECTING,
            /** The scanner has finished connecting. */
            CONNECTED,
            /** The scanner is in the process of initializing. */
            INITIALIZING,
            /** The scanner has finished initializing. */
            INITIALIZED,
            /** The scanner is ready to scan and waiting to be used. */
            READY,
            /** The scanner is in the process of scanning. */
            SCANNING,
            /** The scanner is connected, initialized and enabled but not ready to scan. */
            IDLE,
            /** The scanner is connected and initialized but has been disabled and cannot be used. */
            DISABLED,
            /** The scanner is no longer available after a critical error occurred, usually during connection or initialization. */
            FAILURE,
            /** The scanner disconnected and can no longer be used. */
            DISCONNECTED,
            /** The scanner is in an unknown status. */
            UNKNOWN
        }

        /**
         * Called whenever the scanner has changed status.
         * @param scanner The updated scanner. May be null if the scanner has not yet been created.
         * @param newStatus The new status.
         * @param statusDetails The associated status details.
         */
        void onStatusChanged(@Nullable final Scanner scanner, final Status newStatus, final String statusDetails);
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
    // LED
    ////////////////////////////////////////////////////////////////////////////////////////////////

    void ledColorOn(Color color);

    void ledColorOff(Color color);


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // FUNCTION SUPPORT
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * True if at least one method of illumination (torch, laser...) is supported).
     */
    boolean supportsIllumination();

    /**
     * For logging and sorting purpose, this is the key of the SDK behing this scanner (same as {@link ScannerProvider#getKey()}.
     */
    String getProviderKey();


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // INVENTORY
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Get an inventory/status value. For example battery serial number, device MAC, etc. Keys are usually constants exported by drivers. A list can be obtained with {@link #getStatus()}. Data returned may come from a local cache.
     *
     * @param key requested key
     * @return corresponding value or null if key is not supported by this scanner.
     */
    String getStatus(String key);

    /**
     * Get an inventory/status value. For example battery serial number, device MAC, etc. Keys are usually constants exported by drivers. A list can be obtained with {@link #getStatus()}.
     *
     * @param key        requested key
     * @param allowCache if false the driver is not allowed to use a cache and MUST fetch fresh data from the device.
     * @return corresponding value or null if key is not supported by this scanner.
     */
    String getStatus(String key, boolean allowCache);

    /**
     * @return all inventory/status data known by the scanner. May be empty but not null.
     */
    Map<String, String> getStatus();
}
