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
     * @param count The amount of initialized scanners.
     */
    void onScannerInitEnded(int count);

    /**
     * Callback used when a scanner successfully reads data.
     * @param data The barcodes read by the scanner.
     */
    void onData(List<Barcode> data);
}
