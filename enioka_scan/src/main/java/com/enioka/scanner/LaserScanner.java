package com.enioka.scanner;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;

import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.api.callbacks.ScannerConnectionHandler;
import com.enioka.scanner.api.ScannerProvider;
import com.enioka.scanner.api.ScannerSearchOptions;
import com.enioka.scanner.helpers.BtScannerConnectionRegistry;
import com.enioka.scanner.helpers.ProviderServiceHolder;
import com.enioka.scanner.helpers.ProviderServiceMeta;
import com.enioka.scanner.api.proxies.ScannerConnectionHandlerProxy;

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

    private static final BtScannerConnectionRegistry btRegistry = new BtScannerConnectionRegistry();


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
     * Discovers scanner providers through service intent and retrieves them through reflection.
     *
     * @param ctx a context used to retrieve a PackageManager
     */
    private static void getProviders(Context ctx, OnProvidersDiscovered cb) {
        Log.i(LOG_TAG, "Starting provider discovery");
        PackageManager pkManager = ctx.getPackageManager();
        Intent i = new Intent("com.enioka.scan.PROVIDE_SCANNER");
        List<ResolveInfo> ris = pkManager.queryIntentServices(i, PackageManager.GET_META_DATA);

        Log.i(LOG_TAG, "There are " + ris.size() + " scanner provider(s) available before filtering duplicates");

        for (ResolveInfo ri : ris) {
            // This just avoids processing the same service twice, which could mean duplicate instances now that services are no longer used.
            if (ri.serviceInfo.applicationInfo.uid != ctx.getApplicationInfo().uid) {
                Log.d(LOG_TAG, "Skipping duplicate provider " + ri.serviceInfo.name + " : does not match application UID (Service=" + ri.serviceInfo.applicationInfo.uid + " | Application=" + ctx.getApplicationInfo().uid + ")");
                continue;
            }

            ProviderServiceMeta meta = new ProviderServiceMeta(ri.serviceInfo);
            declaredProviderServices.put(meta.getName(), meta);

            Log.d(LOG_TAG, "Trying to instantiate provider " + meta.getName() + (meta.isBluetooth() ? " - BT    " : " - not BT") + " - " + meta.getPriority());
            try {
                final ScannerProvider provider = (ScannerProvider) Class.forName(meta.getName()).newInstance();
                meta.setProviderKey(provider.getKey());
                providerServices.add(new ProviderServiceHolder(provider, meta));
                Log.d(LOG_TAG, "Provider " + provider.getKey() + " was successfully instantiated");
            } catch (Exception e) {
                declaredProviderServices.remove(meta.getName());
                Log.w(LOG_TAG, "Could not instantiate provider - usual cause is missing SDK from classpath", e);
            }
        }

        Log.i(LOG_TAG, "Provider discovery done");
        cb.discoveryDone();
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
    private static final Semaphore btResolutionMutex = new Semaphore(1);

    /**
     * Scanner search: how many providers are expected to make a contribution, even if empty, to the search.
     */
    private static int providersExpectedToAnswerCount;

    /**
     * Scanner search: how many scanner providers have already made their contribution (event if empty) to the search. Search stops when it equals {@link #providersExpectedToAnswerCount}.
     */
    private static final Semaphore providersHavingAnswered = new Semaphore(0);

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
            getProviders(ctx, () -> startLaserSearchInProviders(ctx, handler, options));
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
            Log.d(LOG_TAG, "Provider " + psh.getMeta().getProviderKey() + " skipped because bluetooth option is disabled");
            return false;
        }
        if (options.excludedProviderKeys != null && options.excludedProviderKeys.contains(psh.getMeta().getProviderKey())) {
            Log.d(LOG_TAG, "Provider " + psh.getMeta().getProviderKey() + " skipped because blacklisted by option (excludes " + options.excludedProviderKeys + ")");
            return false;
        }
        if (options.allowedProviderKeys != null && !options.allowedProviderKeys.isEmpty() && !options.allowedProviderKeys.contains(psh.getMeta().getProviderKey())) {
            Log.d(LOG_TAG, "Provider " + psh.getMeta().getProviderKey() + " skipped because not whitelisted by option (only allows " + options.allowedProviderKeys + ")");
            return false;
        }
        return true;
    }

    private static void startLaserSearchInProviders(final Context ctx, ScannerConnectionHandler handler, final ScannerSearchOptions options) {
        Log.i(LOG_TAG, "Starting scanner search");
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
                Log.d(LOG_TAG, "Provider " + psh.getProvider() + "accepted");
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
