package com.enioka.scanner.sdk.honeywelloss.integrated;

import android.content.Context;

import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.api.ScannerSearchOptions;
import com.enioka.scanner.helpers.intent.IntentScannerProvider;

/**
 * Provider for the Honeywell EDA52 Integrated Scanner
 */
public class HoneywellOssIntegratedScannerProvider extends IntentScannerProvider {
    public static final String PROVIDER_KEY = "HoneywellOssIntegratedProvider";

    @Override
    public void configureProvider() {
        specificDevices.add("EDA52");
    }

    @Override
    public String getKey() {
        return PROVIDER_KEY;
    }

    @Override
    protected Scanner createNewScanner(Context ctx, ScannerSearchOptions options) {
        return new HoneywellOssIntegratedScanner();
    }
}
