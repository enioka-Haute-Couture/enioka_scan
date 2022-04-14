package com.enioka.scanner.api.callbacks;

import com.enioka.scanner.api.Scanner;

/**
 * Callback handling scanner init events
 */
public interface ScannerInitCallback {

    /**
     * Called whenever a scanner successfully connected.
     * @param s The connected scanner.
     */
    void onConnectionSuccessful(final Scanner s);

    /**
     * Called whenever a scanner could not connect.
     * @param s The scanner that could not connect.
     */
    void onConnectionFailure(final Scanner s);
}
