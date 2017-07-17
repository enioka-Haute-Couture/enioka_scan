package com.enioka.scanner.sdk.honeywell;

import android.content.Context;
import android.util.Log;

import com.enioka.scanner.api.ScannerProvider;
import com.enioka.scanner.api.ScannerSearchOptions;
import com.enioka.scanner.helpers.Common;
import com.honeywell.aidc.AidcManager;
import com.honeywell.aidc.BarcodeReaderInfo;

/**
 * For the Honeywell PDA "Android data collection (AIDC)" SDK. This is actually the Intermec SDK.
 */
public class AIDCProvider implements ScannerProvider {
    private static final String PROVIDER_NAME = "HONEYWELL_AIDC";
    private static final String LOG_TAG = "AIDCProvider";

    @Override
    public void getScanner(Context ctx, final ProviderCallback cb, final ScannerSearchOptions options) {
        Log.i(LOG_TAG, "Starting scanner search");

        // Basic check.
        if (!Common.checkIntentListener("com.honeywell.decode.DecodeService", ctx)) {
            Log.i(LOG_TAG, "This is not an Honeywell/Intermec device");
            cb.onProvided(PROVIDER_NAME, null, null);
            return;
        }

        AidcManager.create(ctx, new AidcManager.CreatedCallback() {
            @Override
            public void onCreated(AidcManager aidcManager) {
                Log.i(LOG_TAG, "Manager created");
                if (aidcManager != null && options.waitDisconnected && aidcManager.listBarcodeDevices().size() > 0) {
                    for (BarcodeReaderInfo bri : aidcManager.listBarcodeDevices()) {
                        Log.i(LOG_TAG, "A connected or disconnected barcode device was detected: " + bri.getFriendlyName());
                        cb.onProvided(PROVIDER_NAME, bri.getScannerId(), new AIDCScanner(aidcManager));
                    }
                    return;
                } else if (aidcManager != null && !options.waitDisconnected && aidcManager.listConnectedBarcodeDevices().size() > 0) {
                    for (BarcodeReaderInfo bri : aidcManager.listConnectedBarcodeDevices()) {
                        Log.i(LOG_TAG, "A connected barcode device was detected: " + bri.getFriendlyName());
                        cb.onProvided(PROVIDER_NAME, bri.getScannerId(), new AIDCScanner(aidcManager));
                    }
                    return;
                }
                cb.onProvided(PROVIDER_NAME, null, null);
            }
        });
        Log.i(LOG_TAG, "End scanner search");
    }
}
