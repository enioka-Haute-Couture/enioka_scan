package com.enioka.scanner.sdk.m3;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.util.Log;

import com.enioka.scanner.api.ScannerProvider;
import com.enioka.scanner.api.ScannerProviderBinder;
import com.enioka.scanner.api.ScannerSearchOptions;
import com.m3.ringscannersdk.RingScannerService;


public class M3RingScannerProvider extends Service implements ScannerProvider {
    private static final String LOG_TAG = "M3RingScannerProvider";
    static String PROVIDER_KEY = "M3RingScannerProvider";

    private final IBinder binder = new ScannerProviderBinder(this);

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void getScanner(Context ctx, final ProviderCallback cb, ScannerSearchOptions options) {
        // This provider needs a M3 SDK to work (this is an AIDL proxy, much easier to reuse a pre-made one)
        try {
            this.getClassLoader().loadClass("com.m3.ringscannersdk.RingScannerService");
        } catch (ClassNotFoundException e) {
            Log.d(LOG_TAG, "M3 ring scanner SDK is not present - skipping M3 ring scanners");
            cb.onProviderUnavailable(PROVIDER_KEY);
        }

        // This provider only works if the "Ring Scanner" app is installed.
        PackageManager pkManager = ctx.getPackageManager();
        try {
            if (!pkManager.getApplicationInfo("com.m3.ringscanner", 0).enabled) {
                cb.onProviderUnavailable(PROVIDER_KEY);
                Log.d(LOG_TAG, "M3 ring scanner app is present but disabled - skipping M3 ring scanners");
                return;
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(LOG_TAG, "M3 ring scanner app is not present - skipping M3 ring scanners");
            cb.onProviderUnavailable(PROVIDER_KEY);
            return;
        }

        // Now try to bind the service exposed by the M3 app.
        RingScannerService service = new RingScannerService() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder binderService) {
                super.onServiceConnected(name, binderService);

                cb.onScannerCreated(PROVIDER_KEY, "M3RING", new M3RingScanner(this));
                cb.onAllScannersCreated(PROVIDER_KEY);
            }

            @Override
            public void onNullBinding(ComponentName name) {
                cb.onProviderUnavailable(PROVIDER_KEY);
            }
        };

        service.bindService(ctx);
    }

    @Override
    public String getKey() {
        return PROVIDER_KEY;
    }
}
