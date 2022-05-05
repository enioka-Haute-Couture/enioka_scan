package com.enioka.scanner.sdk.athesi.SPA43LTE;

import android.content.Context;

import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.api.ScannerSearchOptions;
import com.enioka.scanner.helpers.intent.IntentScannerProvider;

/**
 * Provider for the HHT Wrapper Layer, supports old Athesi SPA43 type integrated scanners (only tested with the SPA43 LTE).
 */
public class AthesiHHTProvider extends IntentScannerProvider {
    public static final String PROVIDER_KEY = "AthesiHHTProvider";

    @Override
    protected void configureProvider() {
        specificDevices.add("SPA43LTE");
    }

    @Override
    public String getKey() {
        return PROVIDER_KEY;
    }

    @Override
    protected Scanner createNewScanner(Context ctx, ScannerSearchOptions options) {
        return new AthesiHHTScanner();
    }
}
