package com.enioka.scanner.service;

import com.enioka.scanner.api.callbacks.ScannerStatusCallback;
import com.enioka.scanner.data.Barcode;

import java.util.List;

/**
 * Interface describing a client of the Scanner Service API.
 */
public interface ScannerClient extends ScannerStatusCallback {
    /**
     * Callback used once the initialization of scanners is over.
     * This method is retroactively called on new clients if at least one scanner initialization happened.
     * @param count The amount of initialized scanners.
     */
    void onScannerInitEnded(int count);

    /**
     * Callback used once the discovery of providers is over, which also implies that the scanner service is ready to be used.
     * This method is retroactively called on new clients if at least one provider discovery happened.
     */
    void onProviderDiscoveryEnded();

    /**
     * Callback used when a scanner successfully reads data.
     * @param data The barcodes read by the scanner.
     */
    void onData(List<Barcode> data);
}
