package com.enioka.scanner.sdk.honeywell;

import android.content.Context;
import android.util.Log;

import com.enioka.scanner.api.ScannerProvider;
import com.enioka.scanner.api.ScannerSearchOptions;
import com.honeywell.aidc.AidcManager;

/**
 * For the Honeywell PDA "Android data collection (AIDC)" SDK.
 */
public class AIDCProvider implements ScannerProvider {
    private static final String PROVIDER_NAME = "HONEYWELL_AIDC";
    private static final String LOG_TAG = "AIDCProvider";

    @Override
    public void getScanner(Context ctx, final ProviderCallback cb, final ScannerSearchOptions options) {
        AidcManager.create(ctx, new AidcManager.CreatedCallback() {
            @Override
            public void onCreated(AidcManager aidcManager) {
                if (aidcManager != null && options.waitDisconnected && aidcManager.listBarcodeDevices().size() > 0) {
                    Log.i(LOG_TAG, "A connected or disconnected barcode device was detected");
                    cb.onProvided(PROVIDER_NAME, "i", new AIDCScanner(aidcManager));
                    return;
                } else if (aidcManager != null && !options.waitDisconnected && aidcManager.listConnectedBarcodeDevices().size() > 0) {
                    Log.i(LOG_TAG, "A connected barcode device was detected");
                    cb.onProvided(PROVIDER_NAME, "j", new AIDCScanner(aidcManager));
                    return;
                }
                cb.onProvided(PROVIDER_NAME, null, null);
            }
        });
    }
}
