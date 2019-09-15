package com.enioka.scanner.sdk.honeywell;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.enioka.scanner.api.ScannerProvider;
import com.enioka.scanner.api.ScannerProviderBinder;
import com.enioka.scanner.api.ScannerSearchOptions;
import com.enioka.scanner.helpers.Common;
import com.honeywell.aidc.AidcManager;
import com.honeywell.aidc.BarcodeReaderInfo;

/**
 * For the Honeywell PDA "Android data collection (AIDC)" SDK. This is actually the Intermec SDK.
 */
public class AIDCProvider extends Service implements ScannerProvider {
    static final String PROVIDER_NAME = "HONEYWELL_AIDC";
    private static final String LOG_TAG = "AIDCProvider";

    private final IBinder binder = new ScannerProviderBinder(this);

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void getScanner(Context ctx, final ProviderCallback cb, final ScannerSearchOptions options) {
        Log.i(LOG_TAG, "Starting scanner search");

        // Basic check.
        if (!Common.checkIntentListener("com.honeywell.decode.DecodeService", ctx)) {
            Log.i(LOG_TAG, "This is not an Honeywell/Intermec device");
            cb.onProviderUnavailable(PROVIDER_NAME);
            return;
        }

        AidcManager.create(ctx, new AidcManager.CreatedCallback() {
            @Override
            public void onCreated(AidcManager aidcManager) {
                Log.i(LOG_TAG, "Manager created");
                if (aidcManager != null && options.waitDisconnected && aidcManager.listBarcodeDevices().size() > 0) {
                    for (BarcodeReaderInfo bri : aidcManager.listBarcodeDevices()) {
                        Log.i(LOG_TAG, "A connected or disconnected barcode device was detected: " + bri.getFriendlyName());
                        cb.onScannerCreated(PROVIDER_NAME, bri.getScannerId(), new AIDCScanner(aidcManager));
                    }
                    return;
                } else if (aidcManager != null && !options.waitDisconnected && aidcManager.listConnectedBarcodeDevices().size() > 0) {
                    for (BarcodeReaderInfo bri : aidcManager.listConnectedBarcodeDevices()) {
                        Log.i(LOG_TAG, "A connected barcode device was detected: " + bri.getFriendlyName());
                        cb.onScannerCreated(PROVIDER_NAME, bri.getScannerId(), new AIDCScanner(aidcManager));
                    }
                    return;
                }
                cb.onScannerCreated(PROVIDER_NAME, null, null);
            }
        });
        Log.i(LOG_TAG, "End scanner search");
    }

    @Override
    public String getKey() {
        return PROVIDER_NAME;
    }
}
