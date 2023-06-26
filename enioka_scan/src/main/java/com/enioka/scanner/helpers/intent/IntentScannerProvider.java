package com.enioka.scanner.helpers.intent;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.util.Log;

import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.api.ScannerProvider;
import com.enioka.scanner.api.ScannerSearchOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Many devices actually only allow to communicate with a scanner through an Android service
 * (be it a system service or a service provided by another app).<br>
 * This class factors all the boilerplate code to discover such services.
 */
public abstract class IntentScannerProvider implements ScannerProvider {

    protected String intentToTest = null;

    protected List<String> specificDevices = new ArrayList<>(0);

    protected String appPackageToTest = null;

    protected String serviceToTest = null;

    protected void configureProvider() {
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

        if (serviceToTest != null) {
            Intent i = new Intent();
            i.setComponent(new ComponentName(serviceToTest.split("/")[0], serviceToTest.split("/")[1]));
            try {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
                    ctx.startForegroundService(i);
                } else {
                    ctx.startService(i);
                }
            } catch (Exception e) {
                Log.i("LaserScanner", "could not start service " + serviceToTest, e);
                compatible = false;
            }
        }

        if (compatible) {
            cb.onScannerCreated(getKey(), "internal", createNewScanner(ctx, options));
            cb.onAllScannersCreated(getKey());
        } else {
            cb.onProviderUnavailable(getKey());
        }
    }

    protected abstract Scanner createNewScanner(Context ctx, ScannerSearchOptions options);
}
