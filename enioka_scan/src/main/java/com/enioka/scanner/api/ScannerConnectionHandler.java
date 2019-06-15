package com.enioka.scanner.api;

import com.enioka.scanner.LaserScanner;

/**
 * Interface to implement in order to be able to retrieve scanners created by {@link LaserScanner} (the main scanner factory).
 */
public interface ScannerConnectionHandler {
    /**
     * A SDK-specific message signaling a change during the search for a scanner.
     *
     * @param scannerKey a unique identifier for the scanner being connected.
     * @param message    a SDK-specific message (localized)
     */
    public void scannerConnectionProgress(String providerKey, String scannerKey, String message);

    /**
     * Called when a scanner was found and created. Depending on {@link ScannerSearchOptions#returnOnlyFirst} may be called multiple times.
     *
     * @param scannerKey a unique identifier for the scanner being connected.
     * @param s          the new scanner. Never null.
     */
    public void scannerCreated(String providerKey, String scannerKey, Scanner s);

    /**
     * Called when there is no scanner available on the device.
     */
    void noScannerAvailable();

    /**
     * Called when the search for scanners in the different providers is over
     */
    void endOfScannerSearch();
}
