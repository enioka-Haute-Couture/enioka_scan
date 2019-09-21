package com.enioka.scanner.bt.api;

import android.os.Binder;

/**
 * Boilerplate code for bound services. MUST be used by {@link BtSppScannerProvider} implementations.
 */
public class BtSppScannerProviderServiceBinder extends Binder {
    private final BtSppScannerProvider providerInstance;

    public BtSppScannerProviderServiceBinder(BtSppScannerProvider providerInstance) {
        this.providerInstance = providerInstance;
    }

    public BtSppScannerProvider getService() {
        return providerInstance;
    }
}
