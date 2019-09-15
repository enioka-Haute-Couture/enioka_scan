package com.enioka.scanner;

import android.content.Context;
import android.util.Log;

import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.api.ScannerConnectionHandler;
import com.enioka.scanner.api.ScannerProvider;
import com.enioka.scanner.api.ScannerSearchOptions;
import com.enioka.scanner.bt.BtDeviceFinder;
import com.enioka.scanner.sdk.athesi.HHTProvider;
import com.enioka.scanner.sdk.hid.GenericHidProvider;
import com.enioka.scanner.sdk.honeywell.AIDCProvider;
import com.enioka.scanner.sdk.koamtac.KoamtacScannerProvider;
import com.enioka.scanner.sdk.zebra.BtZebraProvider;
import com.enioka.scanner.sdk.zebra.EmdkZebraProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * The factory for laser scanners.
 */
public final class LaserScanner {
    private static final String LOG_TAG = "LaserScanner";

    /**
     * The list of available scanner providers. (manual for now => no useless complicated plugin system)
     */
    private static final Set<ScannerProvider> laserProviders = new HashSet<>(Arrays.asList((ScannerProvider) new GenericHidProvider()));
    private static Boolean scannerFound = false;

    /**
     * Private constructor to prevent ever creating an instance from this class.
     */
    private LaserScanner() {
    }

    /**
     * Get a new laser scanner. The scanner is provided through a callback. There is a specific callback when no scanner is available.
     *
     * @param ctx     the activity wishing to retrieve a scanner. Must be an actual activity, not simply the application context.
     * @param handler the callback.
     * @param options parameters for scanner search.
     */
    public static void getLaserScanner(Context ctx, final ScannerConnectionHandler handler, final ScannerSearchOptions options) {
        /*if (options.keepScreenOn) {
            ctx.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }*/

        BtDeviceFinder.getProviders(ctx);
        BtDeviceFinder.getDevices();

        // Trivial
        if (laserProviders.isEmpty()) {
            Log.i(LOG_TAG, "There are no laser scanners available at all");
            handler.noScannerAvailable();
            handler.endOfScannerSearch();
            return;
        }

        // Now create a scanner. (iterate on a copy to avoid concurrent list modifications)
        scannerFound = false;
        final int expectedProviderCallbacks = laserProviders.size();
        final Set<String> providersHavingAnswered = new HashSet<>();
        for (final ScannerProvider sp : new ArrayList<>(laserProviders)) {
            sp.getScanner(ctx, new ScannerProvider.ProviderCallback() {
                @Override
                public void onScannerCreated(String providerKey, String scannerKey, Scanner s) {
                    if (s == null) {
                        Log.e(LOG_TAG, "Provider  " + providerKey + "has returned a null scanner!");
                        return;
                    }

                    Log.i(LOG_TAG, providerKey + " scanner found. Id " + scannerKey);
                    handler.scannerConnectionProgress(providerKey, scannerKey, "scanner found.");

                    synchronized (LaserScanner.class) {
                        if (!scannerFound || !options.returnOnlyFirst) {
                            handler.scannerCreated(providerKey, scannerKey, s);
                            scannerFound = true;
                        }
                    }

                    checkEnd(providerKey);
                }

                @Override
                public void connectionProgress(String providerKey, String scannerKey, String message) {
                    handler.scannerConnectionProgress(providerKey, scannerKey, message);
                }

                @Override
                public void onProviderUnavailable(String providerKey) {
                    Log.i(LOG_TAG, "Scanner provider " + providerKey + " reports it is not compatible with the device and will be disabled");
                    handler.scannerConnectionProgress(providerKey, null, "No " + providerKey + " scanners available.");

                    // Remove the provider for ever - that way future searches are faster.
                    synchronized (LaserScanner.laserProviders) {
                        LaserScanner.laserProviders.remove(sp);
                        if (LaserScanner.laserProviders.isEmpty()) {
                            Log.i(LOG_TAG, "There are no laser scanners available at all");
                            handler.noScannerAvailable();
                        }
                    }

                    checkEnd(providerKey);
                }

                @Override
                public void onAllScannersCreated(String providerKey) {
                    Log.i(LOG_TAG, "Scanner provider " + providerKey + " reports it is compatible with current device and has created all its scanners");
                    handler.scannerConnectionProgress(providerKey, null, "Provider " + providerKey + " has finished initializing.");

                    checkEnd(providerKey);
                }

                private void checkEnd(String providerKey) {
                    // The end?
                    providersHavingAnswered.add(providerKey);
                    synchronized (LaserScanner.class) {
                        if (providersHavingAnswered.size() == expectedProviderCallbacks) {
                            handler.endOfScannerSearch();
                        }
                    }
                }
            }, options);
        }
    }
}
