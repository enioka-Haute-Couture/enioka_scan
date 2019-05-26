package com.enioka.scanner.service;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.enioka.scanner.LaserScanner;
import com.enioka.scanner.R;
import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.api.ScannerBackground;
import com.enioka.scanner.api.ScannerConnectionHandler;
import com.enioka.scanner.api.ScannerForeground;
import com.enioka.scanner.api.ScannerSearchOptions;
import com.enioka.scanner.data.Barcode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A bound service handling all the different scanner life cycles. Should usually be bound to the app itself.
 */
public class ScannerService extends Service implements ScannerConnectionHandler, Scanner.ScannerInitCallback, Scanner.ScannerDataCallback, Scanner.ScannerStatusCallback {

    protected final static String LOG_TAG = "ScannerService";

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
        public ScannerService getService() {
            // Return this instance of LocalService so clients can call public methods
            return ScannerService.this;
        }
    }

    // Binder given to clients
    private final IBinder binder = new LocalBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        Log.i(LOG_TAG, "Starting scanner service");
        super.onCreate();
        this.initLaserScannerSearch();
    }

    @Override
    public void onDestroy() {
        this.disconnect();
        super.onDestroy();
    }


    ////////////////////////////////////////////////////////////////////////////
    // SCANNER PROVIDER CONNECTION HANDLERS
    ////////////////////////////////////////////////////////////////////////////

    protected void initLaserScannerSearch() {
        LaserScanner.getLaserScanner(this.getApplicationContext(), this, ScannerSearchOptions.defaultOptions().getAllAvailableScanners());
    }

    @Override
    public void scannerConnectionProgress(String providerKey, String scannerKey, String message) {
        onStatusChanged(providerKey + " reports " + message);
    }

    @Override
    public void scannerCreated(String providerKey, String scannerKey, Scanner s) {
        Log.d(LOG_TAG, "Service has received a new scanner from provider " + providerKey + " - key is " + scannerKey);

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
    public void onStatusChanged(String newStatus) {
        Log.d(LOG_TAG, "Status change: " + newStatus);
        for (BackgroundScannerClient client : this.clients) {
            client.onStatusChanged(newStatus);
        }
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
}