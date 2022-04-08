package com.enioka.scanner.sdk.hid;

import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;

import com.enioka.scanner.api.ScannerProvider;
import com.enioka.scanner.api.ScannerSearchOptions;

public class GenericHidProvider implements ScannerProvider {
    static final String PROVIDER_NAME = "GenericHidProvider";

    @Override
    public void getScanner(Context ctx, ProviderCallback cb, ScannerSearchOptions options) {
        if (ctx.getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS) {
            // We may have a BT keyboard connected
            Log.i(PROVIDER_NAME, "A BT keyboard seems to be connected");

            cb.onScannerCreated(PROVIDER_NAME, "HID", new GenericHidScanner());
        }
        cb.onAllScannersCreated(PROVIDER_NAME);
    }

    @Override
    public String getKey() {
        return PROVIDER_NAME;
    }
}
