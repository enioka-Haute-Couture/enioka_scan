package com.enioka.scanner;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.api.ScannerConnectionHandler;
import com.enioka.scanner.api.ScannerProvider;
import com.enioka.scanner.api.ScannerSearchOptions;
import com.enioka.scanner.sdk.hht.HHTProvider;
import com.enioka.scanner.sdk.honeywell.AIDCProvider;
import com.enioka.scanner.sdk.symbol.SymbolProvider;
import com.enioka.scanner.sdk.zebra.ZebraProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The factory for laser scanners.
 */
public final class LaserScanner {
    private static final String LOG_TAG = "LaserScanner";

    /**
     * The list of available scanner providers. (manual for now => no useless complicated plugin system)
     */
    private static final List<ScannerProvider> laserProviders = new ArrayList<>(Arrays.asList(new SymbolProvider(), new ZebraProvider(), new HHTProvider(), new AIDCProvider()));
    private static Boolean scannerFound = false;

    /**
     * Private constructor to prevent ever creating an object from this class.
     */
    private LaserScanner() {
    }

    // TODO: verb without options.

    /**
     * Get a new laser scanner. The scanner is provided through a callback. There is a specific callback when no scanner is available.
     *
     * @param ctx     the activity wishing to retrieve a scanner. Must be an actual activity, not simply the application context.
     * @param handler the callback.
     * @param options parameters for scanner search.
     */
    public static void getLaserScanner(Activity ctx, final ScannerConnectionHandler handler, final ScannerSearchOptions options) {
        // Ask for permissions.
        boolean arePermissionsGranted = ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(ctx, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(ctx, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED;
        if (!arePermissionsGranted) {
            ActivityCompat.requestPermissions(ctx, new String[]{Manifest.permission.CAMERA, Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN}, 1789);
        }

        // Trivial
        if (laserProviders.isEmpty()) {
            Log.i(LOG_TAG, "There are no laser scanners available at all");
            handler.noScannerAvailable();
            return;
        }

        // Now create a scanner. (iterate on a copy to avoid concurrent list modifications)
        scannerFound = false;
        for (final ScannerProvider sp : new ArrayList<>(laserProviders)) {
            sp.getScanner(ctx, new ScannerProvider.ProviderCallback() {
                @Override
                public void onProvided(String providerKey, String scannerKey, Scanner s) {
                    if (s != null) {
                        Log.i(LOG_TAG, providerKey + " scanner found. Id " + scannerKey);
                        handler.scannerConnectionProgress(providerKey, scannerKey, "scanner found.");

                        synchronized (LaserScanner.class) {
                            if (!scannerFound || !options.returnOnlyFirst) {
                                handler.scannerCreated(providerKey, scannerKey, s);
                                scannerFound = true;
                            }
                        }
                    } else {
                        Log.i(LOG_TAG, "Scanner provider " + providerKey + " reports there is no available scanner compatible with it");
                        handler.scannerConnectionProgress(providerKey, null, "No " + providerKey + " scanners available.");

                        // Remove the provider for ever - that way future searches are faster.
                        synchronized (laserProviders) {
                            LaserScanner.laserProviders.remove(sp);
                            if (LaserScanner.laserProviders.isEmpty()) {
                                Log.i(LOG_TAG, "There are no laser scanners available at all");
                                handler.noScannerAvailable();
                            }
                        }
                    }
                }

                @Override
                public void connectionProgress(String providerKey, String scannerKey, String message) {
                    handler.scannerConnectionProgress(providerKey, scannerKey, message);
                }
            }, options);
        }
    }
}
