package com.enioka.scanner.sdk.proglove;

import android.content.Context;

import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.api.ScannerSearchOptions;
import com.enioka.scanner.helpers.intent.IntentScannerProvider;
import com.enioka.scanner.sdk.bluebird.BluebirdScanner;

/**
 * Provider for Bluebird integrated scanners through an intent service.
 */
public class ProgloveProvider extends IntentScannerProvider {
    static final String PROVIDER_NAME = "ProgloveProvider";

    @Override
    protected void configureProvider() {
        appPackageToTest = "de.proglove.connect";
        //serviceToTest = "de.proglove.connect/de.proglove.core.services.ProGloveService";
    }

    @Override
    public String getKey() {
        return PROVIDER_NAME;
    }

    @Override
    protected Scanner createNewScanner(Context ctx, ScannerSearchOptions options) {
        return new ProgloveScanner(options);
    }
}
