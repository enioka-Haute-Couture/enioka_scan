package com.enioka.scanner.api.callbacks;

import com.enioka.scanner.LaserScanner;
import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.api.ScannerSearchOptions;

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
    void scannerConnectionProgress(final String providerKey, final String scannerKey, final String message);

    /**
     * Called when a scanner was found and created. Depending on {@link ScannerSearchOptions#returnOnlyFirst} may be called multiple times.
     *
     * @param scannerKey a unique identifier for the scanner being connected.
     * @param s          the new scanner. Never null.
     */
    void scannerCreated(final String providerKey, final String scannerKey, final Scanner s);

    /**
     * Called when there is no scanner available on the device. {@link #endOfScannerSearch()} is always called after this.
     */
    void noScannerAvailable();

    /**
     * Called when the search for scanners in the different providers is over.
     */
    void endOfScannerSearch();
}
