package com.enioka.scanner.service;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.enioka.scanner.LaserScanner;
import com.enioka.scanner.R;
import com.enioka.scanner.api.Color;
import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.api.ScannerBackground;
import com.enioka.scanner.api.ScannerConnectionHandler;
import com.enioka.scanner.api.ScannerForeground;
import com.enioka.scanner.api.ScannerSearchOptions;
import com.enioka.scanner.data.Barcode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A bound service handling all the different scanner life cycles. Should usually be bound to the app itself.<br><br>
 * Note that the public API of this service is described in {@link ScannerServiceApi}, which the type obtained by binding to this service.
 * The other methods of this class should not be accessed by clients.
 */
public class ScannerService extends Service implements ScannerConnectionHandler, Scanner.ScannerInitCallback, Scanner.ScannerDataCallback, Scanner.ScannerStatusCallback, ScannerServiceApi {

    protected final static String LOG_TAG = "ScannerService";

    private Handler uiHandler;
    private boolean firstBind = true;

    /**
     * Scanner instances. They should never leak outside of this service.
     */
    protected final List<Scanner> scanners = new ArrayList<>(10);

    /**
     * A subset of {@link #scanners} with only foreground scanners. These are only initialized if the service has a foreground client.
     */
    protected final Set<ScannerForeground> foregroundScanners = new HashSet<>(2);

    /**
     * The registered clients (both background and foreground).
     */
    protected final Set<BackgroundScannerClient> clients = new HashSet<>(1);

    /**
     * A helper count of scanners currently being initialized. When it reaches 0, we are ready.
     */
    private AtomicInteger initializingScannersCount = new AtomicInteger(0);

    /**
     * True when all background scanners (which are only initialized once in the lifetime of the service) are OK.
     */
    private boolean backgroundScannersInitialized = false;

    /**
     * Callbacks which are run when there are no more scanners in the initializing state.
     */
    protected final List<EndOfInitCallback> endOfInitCallbacks = new ArrayList<>();

    /**
     * The options (which can be adapted from intent extra data) with which we look for scanners on this device.
     */
    private final ScannerSearchOptions scannerSearchOptions = ScannerSearchOptions.defaultOptions().getAllAvailableScanners();

    private interface EndOfInitCallback {
        void run();
    }


    ////////////////////////////////////////////////////////////////////////////
    // SERVICE BIND
    ////////////////////////////////////////////////////////////////////////////

    /**
     * We only allow app-local bind.
     */
    public class LocalBinder extends Binder {
        public ScannerServiceApi getService() {
            // Return this instance of ScannerService so clients can call public methods
            return ScannerService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            scannerSearchOptions.useBlueTooth = extras.getBoolean(ScannerServiceApi.EXTRA_BT_ALLOW_BT_BOOLEAN, scannerSearchOptions.useBlueTooth);
            scannerSearchOptions.allowLaterConnections = extras.getBoolean(ScannerServiceApi.EXTRA_SEARCH_KEEP_SEARCHING_BOOLEAN, scannerSearchOptions.allowLaterConnections);
            scannerSearchOptions.allowPairingFlow = extras.getBoolean(ScannerServiceApi.EXTRA_SEARCH_ALLOW_PAIRING_FLOW_BOOLEAN, scannerSearchOptions.allowPairingFlow);
            scannerSearchOptions.allowInitialSearch = extras.getBoolean(ScannerServiceApi.EXTRA_SEARCH_ALLOW_INITIAL_SEARCH_BOOLEAN, scannerSearchOptions.allowInitialSearch);

            String[] allowedProviderKeys = extras.getStringArray(ScannerServiceApi.EXTRA_SEARCH_ALLOWED_PROVIDERS_STRING_ARRAY);
            if (allowedProviderKeys != null && allowedProviderKeys.length > 0) {
                scannerSearchOptions.allowedProviderKeys = new HashSet<>();
                scannerSearchOptions.allowedProviderKeys.addAll(Arrays.asList(allowedProviderKeys));
            }

            String[] excludedProviderKeys = extras.getStringArray(ScannerServiceApi.EXTRA_SEARCH_EXCLUDED_PROVIDERS_STRING_ARRAY);
            if (excludedProviderKeys != null && excludedProviderKeys.length > 0) {
                scannerSearchOptions.excludedProviderKeys = new HashSet<>();
                scannerSearchOptions.excludedProviderKeys.addAll(Arrays.asList(excludedProviderKeys));
            }
        }
        if (firstBind) {
            this.initLaserScannerSearch();
        }
        firstBind = false;
        return new LocalBinder();
    }


    ////////////////////////////////////////////////////////////////////////////
    // SERVICE LIFECYCLE
    ////////////////////////////////////////////////////////////////////////////

    @Override
    public void onCreate() {
        Log.i(LOG_TAG, "Starting scanner service");
        super.onCreate();
        uiHandler = new Handler(getApplicationContext().getMainLooper());
    }

    @Override
    public void onDestroy() {
        Log.i(LOG_TAG, "Destroying scanner service");
        this.disconnect();
        super.onDestroy();
    }


    ////////////////////////////////////////////////////////////////////////////
    // SCANNER PROVIDER CONNECTION HANDLERS
    ////////////////////////////////////////////////////////////////////////////

    public void restartScannerDiscovery() {
        this.disconnect();
        this.initLaserScannerSearch();
    }

    protected synchronized void initLaserScannerSearch() {
        LaserScanner.getLaserScanner(this.getApplicationContext(), this, scannerSearchOptions);
    }

    @Override
    public void scannerConnectionProgress(String providerKey, String scannerKey, String message) {
        onStatusChanged(providerKey + " reports " + message);
    }

    @Override
    public void scannerCreated(String providerKey, String scannerKey, Scanner s) {
        Log.d(LOG_TAG, "Service has received a new scanner from provider " + providerKey + " and will initialize it. Its key is " + scannerKey);

        if (s instanceof ScannerBackground) {
            initializingScannersCount.incrementAndGet();
            ((ScannerBackground) s).initialize(this.getApplicationContext(), this, this, this, Scanner.Mode.BATCH);
        } else {
            Log.i(LOG_TAG, "This is a foreground scanner");
            // If foreground, only init it when we have an active activity.
            synchronized (foregroundScanners) {
                foregroundScanners.add((ScannerForeground) s);
            }
        }
    }

    @Override
    public void noScannerAvailable() {
        onStatusChanged(getResources().getString(R.string.scanner_service_no_compatible_sdk));
        checkInitializationEnd();
    }

    @Override
    public void endOfScannerSearch() {
        Log.i(LOG_TAG, scanners.size() + " scanners from the different SDKs have reported for duty. Waiting for the initialization of the " + (scanners.size() - foregroundScanners.size()) + " background scanners.");
        onStatusChanged(getResources().getString(R.string.scanner_service_sdk_search_end));
        checkInitializationEnd();
    }


    ////////////////////////////////////////////////////////////////////////////
    // SCANNER INIT HANDLERS
    ////////////////////////////////////////////////////////////////////////////

    @Override
    public void onConnectionSuccessful(Scanner s) {
        Log.i(LOG_TAG, "A scanner has successfully initialized from provider " + s.getProviderKey());
        this.scanners.add(s);
        onStatusChanged(getResources().getString(R.string.scanner_status_initialized));
        initializingScannersCount.decrementAndGet();
        checkInitializationEnd();
    }

    @Override
    public void onConnectionFailure(Scanner s) {
        Log.i(LOG_TAG, "A scanner has failed to initialize from provider " + s.getProviderKey());
        onStatusChanged(getResources().getString(R.string.scanner_status_initialization_failure));
        initializingScannersCount.decrementAndGet();
        checkInitializationEnd();
    }

    private void checkInitializationEnd() {
        synchronized (scanners) {
            if (initializingScannersCount.get() != 0) {
                // We wait for all scanners
                return;
            }

            // If here, laser init has ended.
            backgroundScannersInitialized = true;
            Log.i(LOG_TAG, "All found scanners have now ended their initialization (or failed to do so)");

            // Run callbacks
            List<EndOfInitCallback> cbs = new ArrayList<>(endOfInitCallbacks);
            endOfInitCallbacks.clear();
            for (EndOfInitCallback callback : cbs) {
                callback.run();
            }
        }
    }


    ////////////////////////////////////////////////////////////////////////////
    // BLAH BLAH HANDLERS
    ////////////////////////////////////////////////////////////////////////////

    @Override
    public void onStatusChanged(final String newStatus) {
        Log.d(LOG_TAG, "Status change: " + newStatus);
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                for (BackgroundScannerClient client : ScannerService.this.clients) {
                    client.onStatusChanged(newStatus);
                }
            }
        });
    }

    @Override
    public void onScannerDisconnected(Scanner s) {
        scanners.remove(s);
        if (s instanceof ScannerForeground) {
            foregroundScanners.remove(s);
        }
    }

    @Override
    public void onScannerReconnecting(Scanner s) {
        // Nothing to do.
    }

    ////////////////////////////////////////////////////////////////////////////
    // SCANNER DATA HANDLERS
    ////////////////////////////////////////////////////////////////////////////

    @Override
    public void onData(Scanner s, List<Barcode> data) {
        for (BackgroundScannerClient client : this.clients) {
            client.onData(data);
        }
    }


    ////////////////////////////////////////////////////////////////////////////
    // SERVICE API
    ////////////////////////////////////////////////////////////////////////////

    public void takeForegroundControl(final Activity activity, final ForegroundScannerClient client) {
        this.registerClient(client);

        Log.i(LOG_TAG, "Registering a new foreground activity");
        synchronized (scanners) {
            if (backgroundScannersInitialized && foregroundScanners.isEmpty()) {
                client.onForegroundScannerInitEnded(0, scanners.size());
                return;
            }

            if (backgroundScannersInitialized) {
                Log.i(LOG_TAG, "Reinitializing all foreground scanners");
                this.endOfInitCallbacks.add(new EndOfInitCallback() {
                    @Override
                    public void run() {
                        client.onForegroundScannerInitEnded(foregroundScanners.size(), scanners.size() - foregroundScanners.size());
                    }
                });
                for (ScannerForeground sf : foregroundScanners) {
                    ScannerService.this.initializingScannersCount.addAndGet(1);
                    sf.initialize(activity, ScannerService.this, ScannerService.this, ScannerService.this, Scanner.Mode.SINGLE_SCAN);
                }
            } else {
                this.endOfInitCallbacks.add(new EndOfInitCallback() {
                    @Override
                    public void run() {
                        Log.i(LOG_TAG, "Initializing all foreground scanners");
                        ScannerService.this.endOfInitCallbacks.add(new EndOfInitCallback() {
                            @Override
                            public void run() {
                                client.onForegroundScannerInitEnded(foregroundScanners.size(), scanners.size() - foregroundScanners.size());
                            }
                        });
                        for (ScannerForeground sf : foregroundScanners) {
                            ScannerService.this.initializingScannersCount.addAndGet(1);
                            sf.initialize(activity, ScannerService.this, ScannerService.this, ScannerService.this, Scanner.Mode.SINGLE_SCAN);
                        }
                    }
                });
            }
        }
    }

    public void registerClient(BackgroundScannerClient client) {
        this.clients.add(client);
    }

    public boolean anyScannerSupportsIllumination() {
        for (Scanner s : this.scanners) {
            if (s.supportsIllumination()) {
                return true;
            }
        }
        return false;
    }

    public boolean anyScannerHasIlluminationOn() {
        for (Scanner s : this.scanners) {
            if (s.isIlluminationOn()) {
                return true;
            }
        }
        return false;
    }

    public void toggleIllumination() {
        for (Scanner s : this.scanners) {
            s.toggleIllumination();
        }
    }

    public void resume() {
        for (Scanner s : this.scanners) {
            s.resume();
        }
    }

    public void pause() {
        for (Scanner s : this.scanners) {
            s.pause();
        }
    }

    public void disconnect() {
        for (Scanner s : this.scanners) {
            s.disconnect();
        }
    }

    public void beep() {
        for (Scanner s : this.scanners) {
            s.beepScanSuccessful();
        }
    }

    public void ledColorOn(Color color) {
        for (Scanner s : this.scanners) {
            s.ledColorOn(color);
        }
    }

    public void ledColorOff(Color color) {
        for (Scanner s : this.scanners) {
            s.ledColorOff(color);
        }
    }

    // On ajoute quand même ces apis pour le ScannerServiceAPI pour plus de control sur le contenu et on expose tous les autres scanners pour donner une liberter pour les clients de la librairie

    @Override
    public Map<String, String> getFirstScannerStatus() {
        if (!this.scanners.isEmpty()) {
            return this.scanners.get(0).getStatus();
        }
        return new HashMap<>();
    }

    @Override
    public String getFirstScannerStatus(String key) {
        if (!this.scanners.isEmpty()) {
            return this.scanners.get(0).getStatus(key);
        }
        return null;
    }

    @Override
    public String getFirstScannerStatus(String key, boolean allowCache) {
        if (!this.scanners.isEmpty()) {
            return this.scanners.get(0).getStatus(key, allowCache);
        }
        return null;
    }

    @Override
    public List<Scanner> getConnectedScanners() {
        return scanners;
    }
}
