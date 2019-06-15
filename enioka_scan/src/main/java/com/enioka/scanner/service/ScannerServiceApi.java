package com.enioka.scanner.service;

import android.app.Activity;

/**
 * The public API of the {@link ScannerService}. Obtained by binding to the service.
 */
public interface ScannerServiceApi {

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
}
