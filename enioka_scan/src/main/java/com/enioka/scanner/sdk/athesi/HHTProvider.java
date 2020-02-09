package com.enioka.scanner.sdk.athesi;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.api.ScannerProvider;
import com.enioka.scanner.api.ScannerProviderBinder;
import com.enioka.scanner.api.ScannerSearchOptions;
import com.enioka.scanner.helpers.intent.IntentScannerProvider;

import java.util.ArrayList;

/**
 * Provider for the HHT Wrapper Layer
 */
public class HHTProvider extends IntentScannerProvider {
    static final String PROVIDER_NAME = "Athesi HHT internal scanner";

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
