package com.enioka.scanner;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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
import com.enioka.scanner.helpers.BtScannerConnectionRegistry;
import com.enioka.scanner.helpers.ProviderServiceHolder;
import com.enioka.scanner.helpers.ProviderServiceMeta;
import com.enioka.scanner.helpers.ScannerConnectionHandlerProxy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;

/**
 * The factory for laser scanners.
 */
public final class LaserScanner {
    private static final String LOG_TAG = "LaserScanner";

    /**
     * The list of available scanner providers, as declared inside their metadata.
     */
    private static final Map<String, ProviderServiceMeta> declaredProviderServices = new HashMap<>();

    /**
     * The list of scanner providers which could actually be created.
     */
    private static final Set<ProviderServiceHolder> providerServices = new HashSet<>();

    private static BtScannerConnectionRegistry btRegistry = new BtScannerConnectionRegistry();


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
     * Provider search: provider services which should answer. Initialized at service count, provider search ends when we can acquire that many permits.
     */
    private static Semaphore waitingForConnection = new Semaphore(0);

    /**
     * Provider search: Helper internal callback.
     */
    private static OnProvidersDiscovered cb;


    /**
     * Callbacks for bound service connections (used for scanner provider discovery).
     */
    private static ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to ScannerProvider, cast the IBinder and get ScannerProvider instance
            ScannerProviderBinder binder = (ScannerProviderBinder) service;
            ScannerProvider provider = binder.getService();

            ProviderServiceMeta meta = declaredProviderServices.get(className.getClassName());
            if (meta == null) {
                Log.e(LOG_TAG, "A provider service was connected... but was not expected! This is a bug");
                return;
            }
            meta.setProviderKey(provider.getKey());

            String additionalComments = "";
            if (meta.isBluetooth()) {
                additionalComments += " It is a BT provider.";
            }

            Log.i(LOG_TAG, "ScannerProvider service " + provider.getClass().getSimpleName() + " was registered." + additionalComments);
            providerServices.add(new ProviderServiceHolder(provider, meta));

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
            // This just avoids processing the same service twice but does not stop the lib from binding to a service from another app using the same lib.
            if (ri.serviceInfo.applicationInfo.uid != ctx.getApplicationInfo().uid) {
                Log.d(LOG_TAG, "Skipping candidate service " + ri.serviceInfo.name + " : does not match application UID (Service=" + ri.serviceInfo.applicationInfo.uid + " | Application=" + ctx.getApplicationInfo().uid + ")");
                continue;
            }

            ComponentName name = new ComponentName(ri.serviceInfo.packageName, ri.serviceInfo.name);
            Intent boundServiceIntent = new Intent();
            boundServiceIntent.setClassName(ctx, name.getClassName());

            ProviderServiceMeta meta = new ProviderServiceMeta(ri.serviceInfo);
            declaredProviderServices.put(name.getClassName(), meta);

            Log.d(LOG_TAG, "Trying to bind to service " + name.getClassName() + (meta.isBluetooth() ? " - BT    " : " - not BT") + " - " + meta.getPriority());
            try {
                ctx.bindService(boundServiceIntent, connection, Context.BIND_AUTO_CREATE);
            } catch (Exception e) {
                declaredProviderServices.remove(name.getClassName());
                try {
                    waitingForConnection.acquire(1);
                } catch (InterruptedException ex) {
                    // Who cares.
                }
                Log.w(LOG_TAG, "Could not bind to service - usual cause is missing SDK from classpath");
            }
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
        if (providerServices.isEmpty()) {
            getProviders(ctx, new OnProvidersDiscovered() {
                @Override
                public void discoveryDone() {
                    startLaserSearchInProviders(ctx, handler, options);
                }
            });
        } else {
            // We are here if this was called a second time. In this case just launch scanner search from existing providers.
            startLaserSearchInProviders(ctx, handler, options);
        }
    }

    /**
     * Some options have an influence on whether a provider should be used or not. All the afferent rules are inside this method.
     */
    private static boolean shouldProviderBeUsed(ProviderServiceHolder psh, ScannerSearchOptions options) {
        if (psh.getMeta().isBluetooth() && !options.useBlueTooth) {
            return false;
        }
        if (options.excludedProviderKeys != null && options.excludedProviderKeys.contains(psh.getMeta().getProviderKey())) {
            return false;
        }
        if (options.allowedProviderKeys != null && !options.allowedProviderKeys.isEmpty() && !options.allowedProviderKeys.contains(psh.getMeta().getProviderKey())) {
            return false;
        }
        return true;
    }

    private static void startLaserSearchInProviders(final Context ctx, ScannerConnectionHandler handler, final ScannerSearchOptions options) {
        final ScannerConnectionHandler handlerProxy = new ScannerConnectionHandlerProxy(handler);

        if (options.useBlueTooth) {
            btRegistry.register(ctx);
        }

        // Trivial
        if (providerServices.isEmpty()) {
            Log.i(LOG_TAG, "There are no laser scanners available at all");
            handlerProxy.noScannerAvailable();
            handlerProxy.endOfScannerSearch();
            return;
        }

        // BT disabled?
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null || !btAdapter.isEnabled()) {
            Log.w(LOG_TAG, "BT is disabled by the user. All providers using BT will not be enabled. Enabling BT is not the library's responsibility.");
            options.useBlueTooth = false;
        }

        // Count providers which should actually be used.
        providersExpectedToAnswerCount = 0;
        for (ProviderServiceHolder psh : new ArrayList<>(providerServices)) {
            if (shouldProviderBeUsed(psh, options)) {
                providersExpectedToAnswerCount++;
            }
        }

        providersHavingAnswered.drainPermits();
        final ScannerProvider.ProviderCallback providerCallback = getProviderCallback(handlerProxy, options);

        // Interrogate all providers, grouped by priority. (higher priority comes first).
        Log.i(LOG_TAG, "There are " + providersExpectedToAnswerCount + " providers which are going to be invoked for fresh laser scanners");
        List<ProviderServiceHolder> sortedProviders = new ArrayList<>(providerServices); // iterate on a copy to avoid concurrent list modifications from other threads
        Collections.sort(sortedProviders, Collections.reverseOrder()); // priority then name
        int previousPriority = sortedProviders.isEmpty() ? 0 : sortedProviders.get(0).getMeta().getPriority();
        int launchedThreads = 0;
        for (final ProviderServiceHolder psh : sortedProviders) {
            if (!shouldProviderBeUsed(psh, options)) {
                continue;
            }

            if (previousPriority != psh.getMeta().getPriority()) {
                // New group! wait.
                Log.i(LOG_TAG, "Waiting for the end of priority group " + previousPriority);
                try {
                    providersHavingAnswered.acquire(launchedThreads);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                providersHavingAnswered.release(launchedThreads);
                Log.i(LOG_TAG, "Starting scanner search in priority group " + psh.getMeta().getPriority());
            }

            new Thread() {
                public void run() {
                    if (psh.getMeta().isBluetooth()) {
                        try {
                            btResolutionMutex.acquire();
                            Log.d(LOG_TAG, "Acquired BT mutex for provider " + psh.getProvider().getKey());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            return;
                        }
                    }

                    Log.i(LOG_TAG, "Starting search on provider " + psh.getProvider().getKey());
                    psh.getProvider().getScanner(ctx, providerCallback, options);
                }
            }.start();

            launchedThreads++;
            previousPriority = psh.getMeta().getPriority();
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

                Log.i(LOG_TAG, providerKey + " scanner found. Id " + scannerKey + ".");
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
                synchronized (LaserScanner.providerServices) {
                    ProviderServiceHolder toRemove = null;
                    for (ProviderServiceHolder sph : LaserScanner.providerServices) {
                        if (sph.getProvider().getKey().equals(providerKey)) {
                            toRemove = sph;
                            break;
                        }
                    }
                    if (toRemove != null) {
                        LaserScanner.providerServices.remove(toRemove);
                    }

                    if (LaserScanner.providerServices.isEmpty()) {
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

            @Override
            public boolean isAlreadyConnected(BluetoothDevice device) {
                return btRegistry.isAlreadyConnected(device);
            }

            private void checkEnd(String providerKey) {
                // Free BT mutex - another BT provider may run. (copy set: concurrent usage otherwise)
                for (ProviderServiceMeta psm : declaredProviderServices.values()) {
                    // key null happens when the provider was not initialized (it is not selected or is excluded by the user)
                    // We could iterate providerServices instead but this would force us to lock it to avoid concurrent modifications.
                    if (psm.getProviderKey() != null && psm.getProviderKey().equals(providerKey) && psm.isBluetooth()) {
                        btResolutionMutex.release();
                    }
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
