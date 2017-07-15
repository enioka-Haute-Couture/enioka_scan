package com.enioka.scanner.sdk.hht;

import android.content.Context;

import com.enioka.scanner.api.ScannerProvider;
import com.enioka.scanner.api.ScannerSearchOptions;

/**
 * Provider for the HHT Wrapper Layer
 */
public class HHTProvider implements ScannerProvider {
    private static final String LOG_TAG = "HHTProvider";
    private static final String PROVIDER_NAME = "Athesi HHT internal scanner";

    @Override
    public void getScanner(Context ctx, final ProviderCallback cb, final ScannerSearchOptions options) {
        // Check if SPA43
        if (!android.os.Build.MODEL.equals("SPA43LTE")) {
            cb.onProvided(PROVIDER_NAME, null, null);
            return;
        }

        cb.onProvided(PROVIDER_NAME, "p", new HHTScanner());
    }
}
