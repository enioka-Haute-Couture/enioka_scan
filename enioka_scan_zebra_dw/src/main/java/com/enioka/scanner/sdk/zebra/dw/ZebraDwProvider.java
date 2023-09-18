package com.enioka.scanner.sdk.zebra.dw;

import android.content.Context;

import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.api.ScannerSearchOptions;
import com.enioka.scanner.helpers.intent.IntentScannerProvider;

/**
 * Provider for Zebra devices using the DataWedge service.
 */
public class ZebraDwProvider extends IntentScannerProvider {
    public static final String PROVIDER_KEY = "ZebraDwProvider";

    @Override
    protected void configureProvider() {
        serviceToTest = "com.symbol.datawedge/.ScanningService";
    }

    @Override
    public String getKey() {
        return PROVIDER_KEY;
    }

    @Override
    protected Scanner createNewScanner(Context ctx, ScannerSearchOptions options) {
        return new ZebraDwScanner();
    }
}
