package com.enioka.scanner.sdk.bluebird;

import android.content.Context;

import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.api.ScannerSearchOptions;
import com.enioka.scanner.helpers.intent.IntentScannerProvider;

/**
 * Provider for Bluebird integrated scanners through an intent service.
 */
public class BluebirdProvider extends IntentScannerProvider {
    public static final String PROVIDER_NAME = "BluebirdProvider";

    @Override
    protected void configureProvider() {
        //intentToTest = "kr.co.bluebird.android.bbapi.action.BARCODE_OPEN";
        appPackageToTest = "kr.co.bluebird.android.bbsettings";
    }

    @Override
    public String getKey() {
        return PROVIDER_NAME;
    }

    @Override
    protected Scanner createNewScanner(Context ctx, ScannerSearchOptions options) {
        return new BluebirdScanner();
    }
}
