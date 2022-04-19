package com.enioka.scanner.api;

import android.content.Context;
import android.support.annotation.Nullable;

import com.enioka.scanner.api.callbacks.ScannerCommandCallback;
import com.enioka.scanner.api.callbacks.ScannerDataCallback;
import com.enioka.scanner.api.callbacks.ScannerInitCallback;
import com.enioka.scanner.api.callbacks.ScannerStatusCallback;
import com.enioka.scanner.api.proxies.ScannerCommandCallbackProxy;
import com.enioka.scanner.api.proxies.ScannerDataCallbackProxy;
import com.enioka.scanner.api.proxies.ScannerInitCallbackProxy;
import com.enioka.scanner.api.proxies.ScannerStatusCallbackProxy;

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


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Called once per application launch.
     */
    void initialize(final Context applicationContext, final ScannerInitCallbackProxy initCallback, final ScannerDataCallbackProxy dataCallback, final ScannerStatusCallbackProxy statusCallback, final Mode mode);

    /**
     * Called once per application launch, implicit wrapping with callback proxies.
     */
    default void initialize(final Context applicationContext, final ScannerInitCallback initCallback, final ScannerDataCallback dataCallback, final ScannerStatusCallback statusCallback, final Mode mode) {
        initialize(applicationContext,
                new ScannerInitCallbackProxy(initCallback),
                new ScannerDataCallbackProxy(dataCallback),
                new ScannerStatusCallbackProxy(statusCallback),
                mode);
    }

    /**
     * For logging and sorting purpose, this is the key of the SDK behind this scanner (same as {@link ScannerProvider#getKey()}.
     */
    String getProviderKey();

    /**
     * Change ScannerDataCallback
     *
     * @param cb a callback to call when data is read.
     */
    void setDataCallBack(ScannerDataCallbackProxy cb);

    /**
     * Disconnect scanner from the App (the app does not need the scanner anymore)
     */
    void disconnect(@Nullable ScannerCommandCallbackProxy cb);
    /**
     * Disconnect scanner from the App (the app does not need the scanner anymore)
     */
    default void disconnect(@Nullable ScannerCommandCallback cb) {
        disconnect(new ScannerCommandCallbackProxy(cb));
    }

    /**
     * The app keeps the scanner for itself but does not need it immediately. It may free whatever resources it has, or ignore this call.
     * FIXME: may be considered a feature not supported by all (e.g. GsSppScanner does not support it, PostechSppScanner may not support it)
     */
    void pause(@Nullable ScannerCommandCallbackProxy cb);
    /**
     * The app keeps the scanner for itself but does not need it immediately. It may free whatever resources it has, or ignore this call.
     */
    default void pause(@Nullable ScannerCommandCallback cb) {
        pause(new ScannerCommandCallbackProxy(cb));
    }

    /**
     * Reverse the effects of {@link #pause(ScannerCommandCallback)}. The scanner is once again ready to scan after this call. Status callback should be called if needed. Idempotent.
     * FIXME: may be considered a feature not supported by all (e.g. GsSppScanner does not support it, PostechSppScanner may not support it)
     */
    void resume(@Nullable ScannerCommandCallbackProxy cb);
    /**
     * Reverse the effects of {@link #pause(ScannerCommandCallback)}. The scanner is once again ready to scan after this call. Status callback should be called if needed. Idempotent.
     */
    default void resume(@Nullable ScannerCommandCallback cb) {
        resume(new ScannerCommandCallbackProxy(cb));
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // FEATURE SUPPORT
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Casts the current scanner to the WithBeepSupport interface if the feature is supported.
     * @return `this` if the feature is supported, or `null` if the feature is not supported.
     */
    default @Nullable WithBeepSupport getBeepSupport() {
        if (this instanceof WithBeepSupport) {
            return (WithBeepSupport) this;
        }
        return null;
    }

    /**
     * Casts the current scanner to the WithTriggerSupport interface if the feature is supported.
     * @return `this` if the feature is supported, or `null` if the feature is not supported.
     */
    default @Nullable WithTriggerSupport getTriggerSupport() {
        if (this instanceof WithTriggerSupport) {
            return (WithTriggerSupport) this;
        }
        return null;
    }

    /**
     * Casts the current scanner to the WithIlluminationSupport interface if the feature is supported.
     * @return `this` if the feature is supported, or `null` if the feature is not supported.
     */
    default @Nullable WithIlluminationSupport getIlluminationSupport() {
        if (this instanceof WithIlluminationSupport) {
            return (WithIlluminationSupport) this;
        }
        return null;
    }

    /**
     * Casts the current scanner to the WithLedSupport interface if the feature is supported.
     * @return `this` if the feature is supported, or `null` if the feature is not supported.
     */
    default @Nullable WithLedSupport getLedSupport() {
        if (this instanceof WithLedSupport) {
            return (WithLedSupport) this;
        }
        return null;
    }

    /**
     * Casts the current scanner to the WithInventorySupport interface if the feature is supported.
     * @return `this` if the feature is supported, or `null` if the feature is not supported.
     */
    default @Nullable WithInventorySupport getInventorySupport() {
        if (this instanceof WithInventorySupport) {
            return (WithInventorySupport) this;
        }
        return null;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // FEATURE: BEEPS
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Extra interface implemented by scanners that support beeps.
     */
    interface WithBeepSupport {
        /**
         * Short high beep to indicate successful scan
         */
        void beepScanSuccessful(@Nullable ScannerCommandCallbackProxy cb);
        /**
         * Short high beep to indicate successful scan
         */
        default void beepScanSuccessful(@Nullable ScannerCommandCallback cb) {
            beepScanSuccessful(new ScannerCommandCallbackProxy(cb));
        }

        /**
         * Long low beep to indicate unsuccessful scan
         */
        void beepScanFailure(@Nullable ScannerCommandCallbackProxy cb);
        /**
         * Long low beep to indicate unsuccessful scan
         */
        default void beepScanFailure(@Nullable ScannerCommandCallback cb) {
            beepScanFailure(new ScannerCommandCallbackProxy(cb));
        }

        /**
         * Different beep to indicate a completed barcode pairing
         */
        void beepPairingCompleted(@Nullable ScannerCommandCallbackProxy cb);
        /**
         * Different beep to indicate a completed barcode pairing
         */
        default void beepPairingCompleted(@Nullable ScannerCommandCallback cb) {
            beepPairingCompleted(new ScannerCommandCallbackProxy(cb));
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // FEATURE: SOFTWARE TRIGGERS
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Extra interface implemented by scanners that support software triggers.
     */
    interface WithTriggerSupport {
        /**
         * Simulates a press on a hardware-trigger, firing the beam that will read barcodes.
         */
        void pressScanTrigger(@Nullable ScannerCommandCallbackProxy cb);
        /**
         * Simulates a press on a hardware-trigger, firing the beam that will read barcodes.
         */
        default void pressScanTrigger(@Nullable ScannerCommandCallback cb) {
            pressScanTrigger(new ScannerCommandCallbackProxy(cb));
        }

        /**
         * Ends the effect of {@link #pressScanTrigger(ScannerCommandCallback)}.
         */
        void releaseScanTrigger(@Nullable ScannerCommandCallbackProxy cb);
        /**
         * Ends the effect of {@link #pressScanTrigger(ScannerCommandCallback)}.
         */
        default void releaseScanTrigger(@Nullable ScannerCommandCallback cb) {
            releaseScanTrigger(new ScannerCommandCallbackProxy(cb));
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // FEATURE: ILLUMINATION
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Extra interface implemented by scanners that support illumination.
     */
    interface WithIlluminationSupport {
        /**
         * If the device used has a way to illuminate the target, enable it. Idempotent.
         */
        void enableIllumination(@Nullable ScannerCommandCallbackProxy cb);
        /**
         * If the device used has a way to illuminate the target, enable it. Idempotent.
         */
        default void enableIllumination(@Nullable ScannerCommandCallback cb) {
            enableIllumination(new ScannerCommandCallbackProxy(cb));
        }

        /**
         * Reverse of {@link #enableIllumination(ScannerCommandCallback)}
         */
        void disableIllumination(@Nullable ScannerCommandCallbackProxy cb);
        /**
         * Reverse of {@link #enableIllumination(ScannerCommandCallback)}
         */
        default void disableIllumination(@Nullable ScannerCommandCallback cb) {
            disableIllumination(new ScannerCommandCallbackProxy(cb));
        }

        /**
         * See {@link #enableIllumination(ScannerCommandCallback)}
         */
        void toggleIllumination(@Nullable ScannerCommandCallbackProxy cb);
        /**
         * See {@link #enableIllumination(ScannerCommandCallback)}
         */
        default void toggleIllumination(@Nullable ScannerCommandCallback cb) {
            toggleIllumination(new ScannerCommandCallbackProxy(cb));
        }

        /**
         * True if the illumination method is activated.
         */
        boolean isIlluminationOn();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // FEATURE: LED
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Extra interface implemented by scanners that support LED customization.
     */
    interface WithLedSupport {
        /**
         * Turns a LED color on.
         */
        void ledColorOn(ScannerLedColor color, @Nullable ScannerCommandCallbackProxy cb);
        /**
         * Turns a LED color on.
         */
        default void ledColorOn(ScannerLedColor color, @Nullable ScannerCommandCallback cb) {
            ledColorOn(color, new ScannerCommandCallbackProxy(cb));
        }

        /**
         * Turns a LED color off.
         */
        void ledColorOff(ScannerLedColor color, @Nullable ScannerCommandCallbackProxy cb);
        /**
         * Turns a LED color off.
         */
        default void ledColorOff(ScannerLedColor color, @Nullable ScannerCommandCallback cb) {
            ledColorOff(color, new ScannerCommandCallbackProxy(cb));
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // FEATURE: INVENTORY
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Extra interface implemented by scanners that offer inventory information.
     */
    interface WithInventorySupport {
        String SCANNER_STATUS_SCANNER_SN = "SCANNER_STATUS_SCANNER_SN";
        String SCANNER_STATUS_SCANNER_MODEL = "SCANNER_STATUS_SCANNER_MODEL";
        String SCANNER_STATUS_BATTERY_SN = "SCANNER_STATUS_BATTERY_SN";
        String SCANNER_STATUS_BATTERY_MODEL = "SCANNER_STATUS_BATTERY_MODEL";
        String SCANNER_STATUS_BATTERY_WEAR = "SCANNER_STATUS_BATTERY_WEAR";
        String SCANNER_STATUS_BATTERY_CHARGE = "SCANNER_STATUS_BATTERY_CHARGE";
        String SCANNER_STATUS_FIRMWARE = "SCANNER_STATUS_FIRMWARE";
        String SCANNER_STATUS_BT_MAC = "SCANNER_STATUS_BT_MAC";

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
}
