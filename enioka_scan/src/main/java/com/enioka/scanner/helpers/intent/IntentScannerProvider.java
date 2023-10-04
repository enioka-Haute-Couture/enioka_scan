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
import com.enioka.scanner.helpers.Common;

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
            Log.d("IntentProvider", "Provider " + getKey() + ": Checking specific devices `" + specificDevices + "`");
            boolean found = false;

            for (String device : specificDevices) {
                if (android.os.Build.MODEL.equals(device)) {
                    found = true;
                    Log.d("IntentProvider", "Provider " + getKey() + ": Specific devices `" + device + "` found");
                    break;
                }
            }

            if (!found) {
                compatible = false;
            }
        }

        if (intentToTest != null) {
            Log.d("IntentProvider", "Provider " + getKey() + ": Checking intent `" + intentToTest + "`");
            if (!Common.checkIntentListener(intentToTest, ctx)) {
                Log.d("IntentProvider", "Provider " + getKey() + ": Intent `" + intentToTest + "` not found");
                compatible = false;
            }
        }

        if (appPackageToTest != null) {
            Log.d("IntentProvider", "Provider " + getKey() + ": Checking appPackage `" + appPackageToTest + "`");
            PackageManager pkManager = ctx.getPackageManager();
            try {
                if (!pkManager.getApplicationInfo(appPackageToTest, 0).enabled) {
                    compatible = false;
                    Log.d("IntentProvider", "Provider " + getKey() + ": AppPackage `" + appPackageToTest + "` not found");
                }
            } catch (PackageManager.NameNotFoundException e) {
                compatible = false;
                Log.d("IntentProvider", "Provider " + getKey() + ": AppPackage `" + appPackageToTest + "` not found");
            }
        }

        if (serviceToTest != null) {
            Log.d("IntentProvider", "Provider " + getKey() + ": Checking service `" + serviceToTest + "`");
            Intent i = new Intent();
            i.setComponent(new ComponentName(serviceToTest.split("/")[0], serviceToTest.split("/")[1]));
            try {
                boolean success;
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
                    success = ctx.startForegroundService(i) != null;
                } else {
                    success = ctx.startService(i) != null;
                }
                if (!success) {
                    Log.d("IntentProvider", "Provider " + getKey() + ": Service `" + serviceToTest + "` not found");
                    compatible = false;
                }
            } catch (Exception e) {
                Log.d("IntentProvider", "Provider " + getKey() + ": Could not start service " + serviceToTest, e);
                compatible = false;
            }
        }

        if (compatible) {
            Log.d("IntentProvider", "Provider " + getKey() + " compatible");
            cb.onScannerCreated(getKey(), "internal", createNewScanner(ctx, options));
            cb.onAllScannersCreated(getKey());
        } else {
            Log.d("IntentProvider", "Provider " + getKey() + " not compatible");
            cb.onProviderUnavailable(getKey());
        }
    }

    protected abstract Scanner createNewScanner(Context ctx, ScannerSearchOptions options);
}
