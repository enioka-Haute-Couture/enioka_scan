package com.enioka.scanner.service;

import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.api.ScannerSearchOptions;

import java.util.List;

/**
 * The public API of the {@link ScannerService}. Obtained by binding to the service.
 */
public interface ScannerServiceApi {

    ////////////////////////////////////////////////////////////////////////////
    // SEARCH OPTIONS INTENT EXTRAS
    ////////////////////////////////////////////////////////////////////////////

    /**
     * If true, the service will start searching for scanners immediately upon binding.
     * If false, the service will only discover available providers without requesting scanners immediately.
     * Default is true.
     */
    String EXTRA_START_SEARCH_ON_SERVICE_BIND = "startSearchOnServiceBind";

    ////////////////////////////////////////////////////////////////////////////
    // SEARCH OPTIONS INTENT EXTRAS
    ////////////////////////////////////////////////////////////////////////////

    /**
     * If true, if a scanner is known but not available, wait for it. If false, consider the scanner unavailable immediately. Default is true.
     */
    String EXTRA_SEARCH_WAIT_DISCONNECTED_BOOLEAN = "waitDisconnected";

    /**
     * If true, will only return the first scanner available. If false, all scanners available are returned. Default is false.
     */
    String EXTRA_SEARCH_RETURN_ONLY_FIRST_BOOLEAN = "returnOnlyFirst";

    /**
     * If false, the service will only connect to wired, non-BT scanners. Default is true.
     */
    String EXTRA_SEARCH_ALLOW_BT_BOOLEAN = "useBlueTooth";

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
     * For providers supported by the library by default, the keys can be found as the attribute `ScannerProviderClass.PROVIDER_KEY`.
     */
    String EXTRA_SEARCH_ALLOWED_PROVIDERS_STRING_ARRAY = "allowedProviderKeys";

    /**
     * An array of provider keys (BluebirdProvider, ProgloveProvider...) which cannot be used. All others (or those inside EXTRA_SEARCH_ALLOWED_PROVIDERS_STRING_ARRAY) are allowed.
     * Default is empty, meaning non exclusions.
     * For providers supported by the library by default, the keys can be found as the attribute `ScannerProviderClass.PROVIDER_KEY`.
     */
    String EXTRA_SEARCH_EXCLUDED_PROVIDERS_STRING_ARRAY = "excludedProviderKeys";


    ////////////////////////////////////////////////////////////////////////////
    // CLIENT HOOKS
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Hooks all callbacks to the given client.
     *
     * @param client a set of callbacks
     */
    void registerClient(ScannerClient client);

    /**
     * Un-hooks all callbacks to the given client.
     *
     * @param client a set of callbacks
     */
    void unregisterClient(ScannerClient client);


    ////////////////////////////////////////////////////////////////////////////
    // SCANNER SEARCH
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Disconnects all currently-connected scanners then starts the initialization process all over again.
     */
    void restartScannerDiscovery();

    /**
     * Clears the cache of discovered scanner providers and re-starts their discovery. This is a costly operation.
     */
    void restartProviderDiscovery();

    /**
     * Returns the list of keys of currently-available scanner providers, useful to tune scanner search options at runtime.
     */
    List<String> getAvailableProviders();

    /**
     * Updates the service's scanner search options.
     */
    void updateScannerSearchOptions(final ScannerSearchOptions newOptions);


    ////////////////////////////////////////////////////////////////////////////
    // SCANNER LIFECYCLE
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Reverse the effects of {@link #pause()}. The scanners are once again ready to scan after this call. Idempotent.
     * FIXME: technically related to the scanner lifecycle, but just a call to Scanner API methods through Scanner Service. Is getConnectedScanners() not enough for that ?
     */
    void resume();

    /**
     * The service keeps the scanners for itself but does not need it immediately. It may free whatever resources it has, or ignore this call. Idempotent.
     * FIXME: technically related to the scanner lifecycle, but just a call to Scanner API methods through Scanner Service. Is getConnectedScanners() not enough for that ?
     */
    void pause();

    /**
     * Disconnect all scanners from the service (the app does not need the scanner anymore).
     * In case scanners are needed again, {@link #restartScannerDiscovery()} will need to be called first.
     * FIXME: technically related to the scanner lifecycle, but just a call to Scanner API methods through Scanner Service. Is getConnectedScanners() not enough for that ?
     *        still needed internally to handle re-discovery / destruction of the service.
     */
    void disconnect();


    ////////////////////////////////////////////////////////////////////////////
    // SCANNER ACCESS
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Returns the list of currently-connected scanners, allowing their manipulation through the {@link com.enioka.scanner.api.Scanner} API.
     */
    List<Scanner> getConnectedScanners();
}
