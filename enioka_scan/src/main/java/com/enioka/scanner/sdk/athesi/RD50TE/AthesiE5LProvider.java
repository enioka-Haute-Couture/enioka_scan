package com.enioka.scanner.sdk.athesi.RD50TE;

import android.content.Context;

import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.api.ScannerSearchOptions;
import com.enioka.scanner.helpers.intent.IntentScannerProvider;

/**
 * Provider for Athesi E5L scanners.
 * May use similar intents as other modern Athesi scanners but only the E5L is accepted as it is the only one that could be tested.
 */
public class AthesiE5LProvider extends IntentScannerProvider {
    public static final String PROVIDER_KEY = "AthesiE5LProvider";

    @Override
    protected void configureProvider() {
        specificDevices.add("RD50TE");
    }

    @Override
    public String getKey() {
        return PROVIDER_KEY;
    }

    @Override
    protected Scanner createNewScanner(Context ctx, ScannerSearchOptions options) {
        return new AthesiE5LScanner();
    }
}
