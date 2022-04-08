package com.enioka.scanner.sdk.athesi;

import android.content.Context;

import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.api.ScannerSearchOptions;
import com.enioka.scanner.helpers.intent.IntentScannerProvider;

/**
 * Provider for the HHT Wrapper Layer
 */
public class HHTProvider extends IntentScannerProvider {
    public static final String PROVIDER_NAME = "AthesiHHTProvider";

    @Override
    protected void configureProvider() {
        specificDevices.add("SPA43LTE");
    }

    @Override
    public String getKey() {
        return PROVIDER_NAME;
    }

    @Override
    protected Scanner createNewScanner(Context ctx, ScannerSearchOptions options) {
        return new HHTScanner();
    }
}
