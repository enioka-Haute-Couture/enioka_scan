package com.enioka.scanner.sdk.hid;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.enioka.scanner.api.ScannerProvider;
import com.enioka.scanner.api.ScannerProviderBinder;
import com.enioka.scanner.api.ScannerSearchOptions;

public class GenericHidProvider extends Service implements ScannerProvider {
    static final String LOG_TAG = "GenericHidProvider";

    private final IBinder binder = new ScannerProviderBinder(this);

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void getScanner(Context ctx, ProviderCallback cb, ScannerSearchOptions options) {
        if (ctx.getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS) {
            // We may have a BT keyboard connected
            Log.i(LOG_TAG, "A BT keyboard seems to be connected");

            cb.onScannerCreated(LOG_TAG, "HID", new GenericHidScanner());
        }
        cb.onAllScannersCreated(LOG_TAG);
    }

    @Override
    public String getKey() {
        return LOG_TAG;
    }
}
