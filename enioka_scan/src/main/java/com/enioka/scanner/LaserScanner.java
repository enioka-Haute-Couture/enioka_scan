package com.enioka.scanner;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.IBinder;
import android.util.Log;

import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.api.ScannerConnectionHandler;
import com.enioka.scanner.api.ScannerProvider;
import com.enioka.scanner.api.ScannerProviderBinder;
import com.enioka.scanner.api.ScannerSearchOptions;
import com.enioka.scanner.helpers.ScannerConnectionHandlerProxy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;

/**
 * The factory for laser scanners.
 */
public final class LaserScanner {
    private static final String LOG_TAG = "LaserScanner";

    /**
     * The list of available scanner providers.
     */
    private static final Set<ScannerProvider> laserProviders = new HashSet<>();

    /**
     * A subset of all providers, identified by key. Only BT providers are here. May be empty.
     */
    private static final Set<String> btProviderKeys = new HashSet<>();


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Construction
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Private constructor to prevent ever creating an instance from this class.
     */
    private LaserScanner() {
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Scanner provider discovery
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Provider search: provider services which should answer. Initialized at service count, provider search ends when we can aquire that many permits.
     */
    private static Semaphore waitingForConnection = new Semaphore(0);

    /**
     * Provider search: Helper internal callback.
     */
    private static OnProvidersDiscovered cb;

    /**
     * BT provider must be marked after their resolution as they are later handled differently. This contains the class names of the BT providers to allow this.
     */
    private static final Set<String> btProviderClassNames = new HashSet<>();

    /**
     * Callbacks for bound service connections (used for scanner provider discovery).
     */
    private static ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to ScannerProvider, cast the IBinder and get ScannerProvider instance
            ScannerProviderBinder binder = (ScannerProviderBinder) service;
            ScannerProvider provider = binder.getService();

            String additionalComments = "";
            if (btProviderClassNames.contains(className.getClassName())) {
                additionalComments += " It is a BT provider.";
                btProviderKeys.add(provider.getKey());
            }

            Log.i(LOG_TAG, "ScannerProvider service " + provider.getClass().getSimpleName() + " was registered." + additionalComments);
            laserProviders.add(provider);

            try {
                waitingForConnection.acquire(1);
                if (waitingForConnection.availablePermits() == 0) {
                    // All scanner providers have reported for duty, launch scanner search.
                    // TODO: we should not wait for all providers to be available to start looking for scanners.
                    cb.discoveryDone();
                }
            } catch (InterruptedException e) {
                Log.e(LOG_TAG, "Could not wait for provider service initialization. Ignoring.", e);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.i(LOG_TAG, "scanner provider was disconnected");
        }
    };

    /**
     * Retrieve scanner providers through service intent.
     *
     * @param ctx a context used to retrieve a PackageManager
     */
    private static void getProviders(Context ctx, OnProvidersDiscovered cb) {
        Log.i(LOG_TAG, "Starting service discovery");
        LaserScanner.cb = cb;
        PackageManager pkManager = ctx.getPackageManager();
        Intent i = new Intent("com.enioka.scan.PROVIDE_SCANNER");
        List<ResolveInfo> ris = pkManager.queryIntentServices(i, PackageManager.GET_META_DATA);

        waitingForConnection.release(ris.size());
        Log.i(LOG_TAG, "There are " + ris.size() + " scanner provider service(s) available");

        for (ResolveInfo ri : ris) {
            ComponentName name = new ComponentName(ri.serviceInfo.packageName, ri.serviceInfo.name);
            Intent boundServiceIntent = new Intent();
            boundServiceIntent.setClassName(ctx, name.getClassName());

            if (ri.serviceInfo.metaData != null && ri.serviceInfo.metaData.getBoolean("bluetooth")) {
                Log.d(LOG_TAG, "Trying to bind to service " + name.getClassName() + " - this provider uses bluetooth");
                btProviderClassNames.add(name.getClassName());
            } else {
                Log.d(LOG_TAG, "Trying to bind to service " + name.getClassName());
            }

            ctx.bindService(boundServiceIntent, connection, Context.BIND_AUTO_CREATE);
        }
    }

    private interface OnProvidersDiscovered {
        void discoveryDone();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Get scanner through providers
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Scanner search: only one BT provider may look for scanners at the same time, as the underlying SDKs often make the assumption they are alone in the world.
     */
    private static Semaphore btResolutionMutex = new Semaphore(1);

    /**
     * Scanner search: how many providers are expected to make a contribution, even if empty, to the search.
     */
    private static int providersExpectedToAnswerCount;

    /**
     * Scanner search: how many scanner providers have already made their contribution (event if empty) to the search. Search stops when it equals {@link #providersExpectedToAnswerCount}.
     */
    private static Semaphore providersHavingAnswered = new Semaphore(0);

    /**
     * Scanner search: used to determine the first scanner returned.
     */
    private static Boolean scannerFound = false;

    /**
     * Get a new laser scanner. The scanner is provided through a callback. There is a specific callback when no scanner is available.
     *
     * @param ctx     the activity wishing to retrieve a scanner. Must be an actual activity, not simply the application context.
     * @param handler the callback.
     * @param options parameters for scanner search.
     */
    public static void getLaserScanner(final Context ctx, final ScannerConnectionHandler handler, final ScannerSearchOptions options) {
        getProviders(ctx, new OnProvidersDiscovered() {
            @Override
            public void discoveryDone() {
                startLaserSearchInProviders(ctx, handler, options);
            }
        });
    }

    private static void startLaserSearchInProviders(final Context ctx, ScannerConnectionHandler handler, final ScannerSearchOptions options) {
        /*if (options.keepScreenOn) {
            ctx.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }*/

        final ScannerConnectionHandler handlerProxy = new ScannerConnectionHandlerProxy(handler);

        // Trivial
        if (laserProviders.isEmpty()) {
            Log.i(LOG_TAG, "There are no laser scanners available at all");
            handlerProxy.noScannerAvailable();
            handlerProxy.endOfScannerSearch();
            return;
        }

        // BT can be disabled.
        for (final ScannerProvider sp : new ArrayList<>(laserProviders)) {
            if (!options.useBlueTooth && btProviderKeys.contains(sp.getKey())) {
                laserProviders.remove(sp);
            }
        }

        // Now create a scanner. (iterate on a copy to avoid concurrent list modifications from other threads)
        providersExpectedToAnswerCount = laserProviders.size();
        providersHavingAnswered.drainPermits();
        final ScannerProvider.ProviderCallback providerCallback = getProviderCallback(handlerProxy, options);

        Log.i(LOG_TAG, "There are " + providersExpectedToAnswerCount + " providers which are going to be invoked for fresh laser scanners");
        for (final ScannerProvider sp : new ArrayList<>(laserProviders)) {
            new Thread() {
                public void run() {
                    if (btProviderKeys.contains(sp.getKey())) {
                        try {
                            Log.d(LOG_TAG, "Acquiring BT mutex for provider " + sp.getKey());
                            btResolutionMutex.acquire();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            return;
                        }
                    }

                    Log.i(LOG_TAG, "Starting search on provider " + sp.getKey());
                    sp.getScanner(ctx, providerCallback, options);
                }
            }.start();
        }
    }

    private static ScannerProvider.ProviderCallback getProviderCallback(final ScannerConnectionHandler handler, final ScannerSearchOptions options) {
        return new ScannerProvider.ProviderCallback() {
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
                    ScannerProvider toRemove = null;
                    for (ScannerProvider sp : LaserScanner.laserProviders) {
                        if (sp.getKey().equals(providerKey)) {
                            toRemove = sp;
                            break;
                        }
                    }
                    if (toRemove != null) {
                        LaserScanner.laserProviders.remove(toRemove);
                    }

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
                // Free BT mutex - another BT provider may run.
                if (btProviderKeys.contains(providerKey)) {
                    btResolutionMutex.release();
                }

                // The end?
                providersHavingAnswered.release();
                if (providersHavingAnswered.tryAcquire(providersExpectedToAnswerCount)) {
                    handler.endOfScannerSearch();
                }
            }
        };
    }
}
