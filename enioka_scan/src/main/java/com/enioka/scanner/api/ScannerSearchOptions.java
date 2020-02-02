package com.enioka.scanner.api;

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
     * If true, some providers may retrieve scanners after initial search.
     */
    public boolean allowLaterConnections = true;

    /**
     * If true, the providers which need a pairing done by their own SDKs (like a BLE on the fly pairing) will be allowed to do so.
     */
    public boolean allowPairingFlow = false;

    /**
     * Restrict search to this list of providers. Ignored if null or empty.
     */
    public Set<String> allowedProviderKeys = null;

    public static ScannerSearchOptions defaultOptions() {
        return new ScannerSearchOptions();
    }

    public ScannerSearchOptions getAllAvailableScanners() {
        returnOnlyFirst = false;
        return this;
    }
}
