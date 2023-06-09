package com.enioka.scanner.api;

import android.content.Intent;
import android.os.Bundle;

import com.enioka.scanner.service.ScannerServiceApi;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * A property bag tweaking the scanner search method. Some providers may ignore these options.
 */
public class ScannerSearchOptions {
    /**
     * If a scanner is known but not available, wait for it. If false, consider the scanner unavailable immediately.
     */
    public boolean waitDisconnected = true;

    /**
     * If true, will only return the first scanner available (or reporting it may become available if {@link #waitDisconnected} is true). If false, all scanners available are returned.
     */
    public boolean returnOnlyFirst = true;

    /**
     * If true, bluetooth devices will be searched for scanners.
     */
    public boolean useBlueTooth = true;

    /**
     * If true, providers using another application for controlling scanners will be allowed. This is important as most of the time, it is impossible to say if these apps are actually installed, and therefore it is impossible to detect the presence of an actual laser scanner or not.
     */
    public boolean allowIntentDevices = false;

    /**
     * If true, some providers may retrieve scanners after initial search.
     */
    public boolean allowLaterConnections = true;

    /**
     * If true, some providers may retrieve scanners during initial search.
     */
    public boolean allowInitialSearch = true;

    /**
     * If true, the providers which need a pairing done by their own SDKs (like a BLE on the fly pairing) will be allowed to do so.
     */
    public boolean allowPairingFlow = false;

    /**
     * Restrict search to this list of providers. Ignored if null or empty.
     */
    public Set<String> allowedProviderKeys = null;

    /**
     * The providers inside this list will never be used. Ignored if null or empty.
     */
    public Set<String> excludedProviderKeys = null;

    public static ScannerSearchOptions defaultOptions() {
        return new ScannerSearchOptions();
    }

    public ScannerSearchOptions getAllAvailableScanners() {
        returnOnlyFirst = false;
        return this;
    }

    /**
     * Sets the option's attributes based on a given intent's extras.
     * The expected extras are those used by the Scanner Service API.
     *
     * @param intent The intent to translate
     */
    public ScannerSearchOptions fromIntentExtras(final Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            waitDisconnected = extras.getBoolean(ScannerServiceApi.EXTRA_SEARCH_WAIT_DISCONNECTED_BOOLEAN, waitDisconnected);
            returnOnlyFirst = extras.getBoolean(ScannerServiceApi.EXTRA_SEARCH_RETURN_ONLY_FIRST_BOOLEAN, returnOnlyFirst);
            useBlueTooth = extras.getBoolean(ScannerServiceApi.EXTRA_SEARCH_ALLOW_BT_BOOLEAN, useBlueTooth);
            allowIntentDevices = extras.getBoolean(ScannerServiceApi.EXTRA_SEARCH_ALLOW_INTENT_BOOLEAN, allowIntentDevices);
            allowLaterConnections = extras.getBoolean(ScannerServiceApi.EXTRA_SEARCH_KEEP_SEARCHING_BOOLEAN, allowLaterConnections);
            allowPairingFlow = extras.getBoolean(ScannerServiceApi.EXTRA_SEARCH_ALLOW_PAIRING_FLOW_BOOLEAN, allowPairingFlow);
            allowInitialSearch = extras.getBoolean(ScannerServiceApi.EXTRA_SEARCH_ALLOW_INITIAL_SEARCH_BOOLEAN, allowInitialSearch);

            String[] allowedProviderKeysArray = extras.getStringArray(ScannerServiceApi.EXTRA_SEARCH_ALLOWED_PROVIDERS_STRING_ARRAY);
            if (allowedProviderKeysArray != null && allowedProviderKeysArray.length > 0) {
                allowedProviderKeys = new HashSet<>();
                allowedProviderKeys.addAll(Arrays.asList(allowedProviderKeysArray));
            }

            String[] excludedProviderKeysArray = extras.getStringArray(ScannerServiceApi.EXTRA_SEARCH_EXCLUDED_PROVIDERS_STRING_ARRAY);
            if (excludedProviderKeysArray != null && excludedProviderKeysArray.length > 0) {
                excludedProviderKeys = new HashSet<>();
                excludedProviderKeys.addAll(Arrays.asList(excludedProviderKeysArray));
            }
        }

        return this;
    }

    /**
     * Sets an intent's extras based on the option's attributes.
     * The expected extras are those used by the Scanner Service API.
     *
     * @param intent The intent to update
     */
    public void toIntentExtras(final Intent intent) {
        intent.putExtra(ScannerServiceApi.EXTRA_SEARCH_WAIT_DISCONNECTED_BOOLEAN, waitDisconnected);
        intent.putExtra(ScannerServiceApi.EXTRA_SEARCH_RETURN_ONLY_FIRST_BOOLEAN, returnOnlyFirst);
        intent.putExtra(ScannerServiceApi.EXTRA_SEARCH_ALLOW_BT_BOOLEAN, useBlueTooth);
        intent.putExtra(ScannerServiceApi.EXTRA_SEARCH_KEEP_SEARCHING_BOOLEAN, allowLaterConnections);
        intent.putExtra(ScannerServiceApi.EXTRA_SEARCH_ALLOW_INITIAL_SEARCH_BOOLEAN, allowInitialSearch);
        intent.putExtra(ScannerServiceApi.EXTRA_SEARCH_ALLOW_PAIRING_FLOW_BOOLEAN, allowPairingFlow);
        if (allowedProviderKeys != null && allowedProviderKeys.size() > 0) {
            intent.putExtra(ScannerServiceApi.EXTRA_SEARCH_ALLOWED_PROVIDERS_STRING_ARRAY, allowedProviderKeys.toArray(new String[0]));
        }
        if (excludedProviderKeys != null && excludedProviderKeys.size() > 0) {
            intent.putExtra(ScannerServiceApi.EXTRA_SEARCH_EXCLUDED_PROVIDERS_STRING_ARRAY, excludedProviderKeys.toArray(new String[0]));
        }
    }
}
