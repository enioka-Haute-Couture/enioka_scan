package com.enioka.scanner.service;

import android.app.Activity;

import com.enioka.scanner.api.Color;
import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.sdk.athesi.DataWedge;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The public API of the {@link ScannerService}. Obtained by binding to the service.
 */
public interface ScannerServiceApi {

    ////////////////////////////////////////////////////////////////////////////
    // INTENT EXTRAS
    ////////////////////////////////////////////////////////////////////////////

    /**
     * If false, the service will only connect to wired, non-BT scanners. Default is true.
     */
    String EXTRA_BT_ALLOW_BT_BOOLEAN = "useBlueTooth";

    /**
     * If true, some providers may find scanners after initial search is done. For example, a master BT device may connect later.
     * Default is true. May be ignored by some providers.
     */
    String EXTRA_SEARCH_KEEP_SEARCHING_BOOLEAN = "allowLaterConnections";

    /**
     * If false, the initial search is skipped. Useful when it would be useless for the library to search for scanners, for example for master BT devices: no need to test already paired BT devices, for master BT scanners are often non-discoverable.
     * Default is true. May be ignored by some providers.
     */
    String EXTRA_SEARCH_ALLOW_INITIAL_SEARCH_BOOLEAN = "allowInitialSearch";

    /**
     * If true, the library is allowed to start pairing/configuration activities during initial search.
     * Default is false. Some providers require this (ProGlove for example).
     */
    String EXTRA_SEARCH_ALLOW_PAIRING_FLOW_BOOLEAN = "allowPairingFlow";

    /**
     * An array of provider keys (BluebirdProvider, ProgloveProvider...) allowed. All others are excluded.
     * Default is empty, meaning all providers allowed.
     */
    String EXTRA_SEARCH_ALLOWED_PROVIDERS_STRING_ARRAY = "allowedProviderKeys";

    /**
     * An array of provider keys (BluebirdProvider, ProgloveProvider...) which cannot be used. All others (or those inside EXTRA_SEARCH_ALLOWED_PROVIDERS_STRING_ARRAY) are allowed.
     * Default is empty, meaning non exclusions.
     */
    String EXTRA_SEARCH_EXCLUDED_PROVIDERS_STRING_ARRAY = "excludedProviderKeys";


    ////////////////////////////////////////////////////////////////////////////
    // HOOKS
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Binds all hooks and uses the given activity to enable possible scanners needing foreground control (such as HIDs).
     *
     * @param activity the activity requesting the registration
     * @param client   the callbacks used by the service to notify the client
     */
    void takeForegroundControl(final Activity activity, final ForegroundScannerClient client);

    /**
     * Hooks all callbacks to the given client.
     *
     * @param client a set of callbacks
     */
    void registerClient(BackgroundScannerClient client);

    /**
     * Un-hooks all callbacks to the given client.
     *
     * @param client a set of callbacks
     */
    void unregisterClient(BackgroundScannerClient client);


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // SOFTWARE TRIGGERS
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Simulates a press on a hardware-trigger, firing the beam that will read barcodes.
     */
    void pressScanTrigger();

    /**
     * Ends the effect of {@link #pressScanTrigger()}.
     */
    void releaseScanTrigger();


    ////////////////////////////////////////////////////////////////////////////
    // ILLUMINATION
    ////////////////////////////////////////////////////////////////////////////

    /**
     * @return true if at least one connected scanner supports illumination.
     */
    boolean anyScannerSupportsIllumination();

    /**
     * @return true if at least one connected scanner has illumination enabled.
     */
    boolean anyScannerHasIlluminationOn();

    /**
     * If illumination is enabled, stop it. if it is disabled, start it.
     */
    void toggleIllumination();


    ////////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Reverse the effects of {@link #pause()}. The scanners are once again ready to scan after this call. Idempotent.
     */
    void resume();

    /**
     * The service keeps the scanners for itself but does not need it immediately. It may free whatever resources it has, or ignore this call. Idempotent.
     */
    void pause();

    /**
     * Disconnect scanners from the App (the app does not need the scanner anymore).
     */
    void disconnect();

    /**
     * Will do the same as on service startup after resetting all open scanners. Returns immediately.
     */
    void restartScannerDiscovery();


    ////////////////////////////////////////////////////////////////////////////
    // BUZZER
    ////////////////////////////////////////////////////////////////////////////

    void beep();


    ////////////////////////////////////////////////////////////////////////////
    // LIGHTS
    ////////////////////////////////////////////////////////////////////////////

    void ledColorOn(Color color);

    void ledColorOff(Color color);

    Map<String, String> getFirstScannerStatus();

    String getFirstScannerStatus(String key);

    String getFirstScannerStatus(String key, boolean allowCache);

    List<Scanner> getConnectedScanners();
}
