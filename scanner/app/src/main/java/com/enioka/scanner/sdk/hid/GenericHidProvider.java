package com.enioka.scanner.sdk.hid;

import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;

import com.enioka.scanner.R;
import com.enioka.scanner.api.ScannerProvider;
import com.enioka.scanner.api.ScannerSearchOptions;

public class GenericHidProvider implements ScannerProvider {
    static final String LOG_TAG = "GenericHidProvider";

    @Override
    public void getScanner(Context ctx, ProviderCallback cb, ScannerSearchOptions options) {
        if (ctx.getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS) {
            // We may have a BT keyboard connected
            Log.i(LOG_TAG, "A BT keyboard seems to be connected");

            cb.onProvided(LOG_TAG, "HID", new GenericHidScanner());
        }
    }

    @Override
    public String getKey() {
        return LOG_TAG;
    }
}
