package com.enioka.scanner.helpers.intent;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.api.ScannerProvider;
import com.enioka.scanner.api.ScannerProviderBinder;
import com.enioka.scanner.api.ScannerSearchOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Many devices actually only allow to communicate with a scanner through an Android service
 * (be it a system service or a service provided by another app).<br>
 * This class factors all the boilerplate code to discover such services.
 */
public abstract class IntentScannerProvider extends Service implements ScannerProvider {

    private final IBinder binder = new ScannerProviderBinder(this);

    protected String intentToTest = null;

    protected List<String> specificDevices = new ArrayList<>(0);

    protected String appPackageToTest = null;

    protected void configureProvider() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public String getKey() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void getScanner(Context ctx, final ProviderCallback cb, final ScannerSearchOptions options) {
        configureProvider();

        boolean compatible = true;

        if (specificDevices != null && !specificDevices.isEmpty()) {
            boolean found = false;

            for (String device : specificDevices) {
                if (android.os.Build.MODEL.equals(device)) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                cb.onProviderUnavailable(getKey());
                compatible = false;
            }
        }

        if (intentToTest != null) {
            Intent i = new Intent(intentToTest);
            PackageManager pkManager = ctx.getPackageManager();
            List<ResolveInfo> services = pkManager.queryIntentServices(i, PackageManager.GET_META_DATA);
            if (services.isEmpty()) {
                compatible = false;
            }
        }

        if (appPackageToTest != null) {
            PackageManager pkManager = ctx.getPackageManager();
            try {
                if (!pkManager.getApplicationInfo(appPackageToTest, 0).enabled) {
                    compatible = false;
                }
            } catch (PackageManager.NameNotFoundException e) {
                compatible = false;
            }
        }

        if (compatible) {
            cb.onScannerCreated(getKey(), "internal", createNewScanner(ctx));
            cb.onAllScannersCreated(getKey());
        } else {
            cb.onProviderUnavailable(getKey());
        }
    }

    protected abstract Scanner createNewScanner(Context ctx);
}
