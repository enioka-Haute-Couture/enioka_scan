package com.enioka.scanner.service;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

/**
 * A very simple helper intending to simplify boilerplate code when using the {@link ScannerService} at the application level.
 * Useless if not using the service but using directly {@link com.enioka.scanner.LaserScanner}.
 * Also only covers the most simple cases - for all more advanced needs you need to bind the service yourself!
 */
public class ScannerServiceBinderHelper {
    private ScannerServiceApi scannerService;

    // No instantiation possible beside #bind.
    private ScannerServiceBinderHelper() {
    }

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ScannerService.LocalBinder binder = (ScannerService.LocalBinder) service;
            scannerService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            scannerService = null;
        }
    };

    /**
     * Service default configuration, used everywhere else. A new bundle is returned on each call.
     */
    public static Bundle defaultServiceConfiguration() {
        Bundle res = new Bundle();
        res.putBoolean(ScannerServiceApi.EXTRA_SEARCH_ALLOW_BT_BOOLEAN, false);
        res.putBoolean(ScannerServiceApi.EXTRA_SEARCH_ALLOW_INTENT_BOOLEAN, false);
        res.putBoolean(ScannerServiceApi.EXTRA_SEARCH_KEEP_SEARCHING_BOOLEAN, false);

        return res;
    }

    private void actuallyBind(Application a, Bundle all) {
        Intent intent = new Intent(a, ScannerService.class);
        intent.putExtras(all);
        a.bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    public static ScannerServiceBinderHelper bind(Application a, Bundle extra) {
        ScannerServiceBinderHelper res = new ScannerServiceBinderHelper();
        Bundle b = defaultServiceConfiguration();
        b.putAll(extra);
        res.actuallyBind(a, b);

        return res;
    }

    public static ScannerServiceBinderHelper bind(Application a) {
        return bind(a, new Bundle());
    }

    public ScannerServiceApi getScannerService() {
        if (scannerService == null) {
            throw new IllegalStateException("Service is not bound yet");
        }
        return scannerService;
    }

    public void disconnect() {
        if (scannerService != null) {
            scannerService.disconnect();
        }
    }
}
