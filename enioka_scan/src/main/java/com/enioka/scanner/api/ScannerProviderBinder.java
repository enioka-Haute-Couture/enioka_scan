package com.enioka.scanner.api;

import android.os.Binder;

/**
 * Boilerplate code for bound services.
 */
public class ScannerProviderBinder extends Binder {
    private final ScannerProvider scannerProvider;

    public ScannerProviderBinder(ScannerProvider scannerProvider) {
        this.scannerProvider = scannerProvider;
    }

    public ScannerProvider getService() {
        return this.scannerProvider;
    }
}
