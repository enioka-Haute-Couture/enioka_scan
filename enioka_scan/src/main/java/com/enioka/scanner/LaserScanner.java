package com.enioka.scanner;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;

import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.api.ScannerProvider;
import com.enioka.scanner.api.ScannerSearchOptions;
import com.enioka.scanner.api.callbacks.ProviderDiscoveredCallback;
import com.enioka.scanner.api.callbacks.ScannerConnectionHandler;
import com.enioka.scanner.api.proxies.ProviderDiscoveredCallbackProxy;
import com.enioka.scanner.bt.manager.SerialBtScannerProvider;
import com.enioka.scanner.helpers.BtScannerConnectionRegistry;
import com.enioka.scanner.helpers.ScannerProviderHolder;
import com.enioka.scanner.helpers.ScannerProviderMeta;
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
    private static final Map<String, ScannerProviderMeta> declaredProviders = new HashMap<>();

    /**
     * The list of scanner providers which could actually be created.
     */
    private static final Set<ScannerProviderHolder> providers = new HashSet<>();

    private static final BtScannerConnectionRegistry btRegistry = new BtScannerConnectionRegistry();


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Construction
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Private constructor to prevent ever creating an instance from this class.
     */
    private LaserScanner() {}


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Scanner provider discovery
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Discovers scanner providers through service intent and retrieves them through reflection.
     * The providers are cached and do not need to be discovered again unless new entries are expected, it is a costly operation.
     *
     * @param ctx a context used to retrieve a PackageManager
     */
    public static void discoverProviders(Context ctx, ProviderDiscoveredCallback cb) {
        discoverProviders(ctx, new ProviderDiscoveredCallbackProxy(cb));
    }

    /**
     * Discovers scanner providers through service intent and retrieves them through reflection.
     * The providers are cached and do not need to be discovered again unless new entries are expected, it is a costly operation.
     *
     * @param ctx a context used to retrieve a PackageManager
     */
    public static void discoverProviders(Context ctx, ProviderDiscoveredCallbackProxy cb) {
        // Clear cache
        declaredProviders.clear();
        providers.clear();

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

            ScannerProviderMeta meta = new ScannerProviderMeta(ri.serviceInfo);
            declaredProviders.put(meta.getName(), meta);

            Log.d(LOG_TAG, "Trying to instantiate provider " + meta.getName() + (meta.isBluetooth() ? " - BT    " : " - not BT") + " - " + meta.getPriority());
            try {
                final ScannerProvider provider = (ScannerProvider) Class.forName(meta.getName()).newInstance();
                meta.setProviderKey(provider.getKey());
                providers.add(new ScannerProviderHolder(provider, meta));
                Log.d(LOG_TAG, "Provider " + provider.getKey() + " was successfully instantiated");

                if (provider.getKey().equals(SerialBtScannerProvider.PROVIDER_KEY)) {
                    // Initialize the bluetooth provider if available.
                    SerialBtScannerProvider.discoverProviders(ctx);
                }
            } catch (Exception e) {
                declaredProviders.remove(meta.getName());
                Log.w(LOG_TAG, "Could not instantiate provider - usual cause is missing SDK from classpath", e);
            }
        }

        Log.i(LOG_TAG, "Provider discovery done");
        cb.onDiscoveryDone();
    }

    /**
     * Returns the list of provider keys from the current provider cache, including bluetooth ones if available (the cache needs to have been initialized first).
     * Because of the way Bluetooth providers are handled, only the key String may be returned and not metadata.
     *
     * @return The list of cached provider keys, including bluetooth providers.
     */
    public static List<String> getProviderCache() {
        final List<String> providerKeys = new ArrayList<>();
        for (final ScannerProviderHolder provider : providers) {
            if (provider.getProvider().getKey().equals(SerialBtScannerProvider.PROVIDER_KEY)) {
                // BT providers
                providerKeys.addAll(((SerialBtScannerProvider)provider.getProvider()).getProviderCache());
            } else {
                // Regular provider, add to the list
                providerKeys.add(provider.getProvider().getKey());
            }
        }
        return providerKeys;
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
        getLaserScanner(ctx, new ScannerConnectionHandlerProxy(handler), options);
    }

    /**
     * Get a new laser scanner. The scanner is provided through a callback. There is a specific callback when no scanner is available.
     *
     * @param ctx     the activity wishing to retrieve a scanner. Must be an actual activity, not simply the application context.
     * @param handler the callback.
     * @param options parameters for scanner search.
     */
    public static void getLaserScanner(final Context ctx, final ScannerConnectionHandlerProxy handler, final ScannerSearchOptions options) {
        // Removing the BtSppSdk provider, as it is already handled by the useBluetooth option.
        if (options.allowedProviderKeys != null) {
            options.allowedProviderKeys.remove(SerialBtScannerProvider.PROVIDER_KEY);
        }
        if (options.excludedProviderKeys != null) {
            options.excludedProviderKeys.remove(SerialBtScannerProvider.PROVIDER_KEY);
        }

        if (providers.isEmpty()) {
            discoverProviders(ctx, () -> startLaserSearchInProviders(ctx, handler, options));
        } else {
            // We are here if this was called a second time. In this case just launch scanner search from existing providers.
            startLaserSearchInProviders(ctx, handler, options);
        }
    }

    /**
     * Some options have an influence on whether a provider should be used or not. All the afferent rules are inside this method.
     */
    private static boolean shouldProviderBeUsed(ScannerProviderHolder psh, ScannerSearchOptions options) {
        if (psh.getProvider().getKey().equals(SerialBtScannerProvider.PROVIDER_KEY) && options.useBlueTooth) {
            // Default BT provider is always allowed when bluetooth is on, another check on specific BT providers will be done when they are queried.
            return true;
        }
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

    private static void startLaserSearchInProviders(final Context ctx, ScannerConnectionHandlerProxy handler, final ScannerSearchOptions options) {
        scannerFound = false; // Scanners are not cached like providers so we need to reset this flag every time.
        Log.i(LOG_TAG, "Starting scanner search");

        // Trivial
        if (providers.isEmpty()) {
            Log.i(LOG_TAG, "There are no laser scanners available at all");
            handler.noScannerAvailable();
            handler.endOfScannerSearch();
            return;
        }

        if (options.useBlueTooth) {
            btRegistry.register(ctx);
        }

        // BT disabled?
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null || !btAdapter.isEnabled()) {
            Log.w(LOG_TAG, "BT is disabled by the user. All providers using BT will not be enabled. Enabling BT is not the library's responsibility.");
            options.useBlueTooth = false;
        }

        // Only keep providers which should actually be used.
        // Iterate on copies to avoid concurrent list modifications from other threads
        List<ScannerProviderHolder> sortedProviders = new ArrayList<>();
        providersExpectedToAnswerCount = 0;
        for (final ScannerProviderHolder psh : new ArrayList<>(providers)) {
            if (shouldProviderBeUsed(psh, options)) {
                Log.d(LOG_TAG, "Provider " + psh.getProvider().getKey() + " accepted");
                providersExpectedToAnswerCount++;
                sortedProviders.add(psh);
            }
        }
        Collections.sort(sortedProviders, Collections.reverseOrder()); // priority then name
        Log.i(LOG_TAG, "There are " + providersExpectedToAnswerCount + " providers which are going to be invoked for fresh laser scanners");

        providersHavingAnswered.drainPermits();
        final ScannerProvider.ProviderCallback providerCallback = getProviderCallback(handler, options);

        // Interrogate all providers, grouped by priority. (higher priority comes first).
        int previousPriority = sortedProviders.isEmpty() ? 0 : sortedProviders.get(0).getMeta().getPriority();
        int launchedThreads = 0;
        for (final ScannerProviderHolder psh : sortedProviders) {
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

    private static ScannerProvider.ProviderCallback getProviderCallback(final ScannerConnectionHandlerProxy handler, final ScannerSearchOptions options) {
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
                synchronized (LaserScanner.providers) {
                    ScannerProviderHolder toRemove = null;
                    for (ScannerProviderHolder sph : LaserScanner.providers) {
                        if (sph.getProvider().getKey().equals(providerKey)) {
                            toRemove = sph;
                            break;
                        }
                    }
                    if (toRemove != null) {
                        LaserScanner.providers.remove(toRemove);
                    }

                    if (LaserScanner.providers.isEmpty()) {
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
                for (ScannerProviderMeta psm : declaredProviders.values()) {
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
