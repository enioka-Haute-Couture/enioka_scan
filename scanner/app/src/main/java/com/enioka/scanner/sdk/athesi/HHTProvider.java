package com.enioka.scanner.sdk.athesi;

import android.content.Context;

import com.enioka.scanner.api.ScannerProvider;
import com.enioka.scanner.api.ScannerSearchOptions;

/**
 * Provider for the HHT Wrapper Layer
 */
public class HHTProvider implements ScannerProvider {
    private static final String LOG_TAG = "HHTProvider";
    static final String PROVIDER_NAME = "Athesi HHT internal scanner";

    @Override
    public void getScanner(Context ctx, final ProviderCallback cb, final ScannerSearchOptions options) {
        // Check if SPA43. Brutal - we cannot detect an intent receiver as they are not declared in the manifest of the HHT service...
        if (!android.os.Build.MODEL.equals("SPA43LTE")) {
            cb.onProviderUnavailable(PROVIDER_NAME);
            return;
        }

        cb.onScannerCreated(PROVIDER_NAME, "internal", new HHTScanner());
    }

    @Override
    public String getKey() {
        return PROVIDER_NAME;
    }
}
