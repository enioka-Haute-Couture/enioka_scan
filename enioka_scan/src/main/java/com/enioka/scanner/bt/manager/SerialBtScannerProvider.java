package com.enioka.scanner.bt.manager;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.ParcelUuid;
import android.util.Log;

import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.api.ScannerProvider;
import com.enioka.scanner.api.ScannerSearchOptions;
import com.enioka.scanner.bt.api.BtSppScannerProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;

/**
 * Responsible for finding bluetooth devices and starting their initialization, converting them to {@link com.enioka.scanner.api.Scanner}.<br><br>
 * This is the entry point of the BT SPP/TIO SDK for the rest of the library.
 */
public class SerialBtScannerProvider implements ScannerProvider {
    public static final String PROVIDER_KEY = "BtSppSdk";

    // Cache of all available providers, only initialized once
    private static final List<com.enioka.scanner.bt.api.BtSppScannerProvider> scannerProviders = new ArrayList<>();
    // Filtered list of providers to respect the allowed/excluded providers options, reset on every scanner search
    private final List<com.enioka.scanner.bt.api.BtSppScannerProvider> sortedScannerProviders = new ArrayList<>();

    private ClassicBtAcceptConnectionThread server;
    private ProviderCallback providerCallback;
    private final Semaphore waitForScanners = new Semaphore(0);
    private int passiveScannersCount = 0;


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Interface with the rest of the library
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void getScanner(Context ctx, ProviderCallback cb, ScannerSearchOptions options) {
        this.providerCallback = cb;
        this.getDevices(ctx, options);
    }

    @Override
    public String getKey() {
        return PROVIDER_KEY;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Misc
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Allow new master BT SPP devices to connect to the Android device.
     */
    void resetListener(BluetoothAdapter bluetoothAdapter) {
        if (server == null || server.isDone()) {
            server = new ClassicBtAcceptConnectionThread(bluetoothAdapter, new ConnectionCallback(this), this);
            server.start();
        }
    }

    void stopMasterListener() {
        if (server != null && !server.isDone()) {
            server.cancel();
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Provider discovery
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Retrieve SPP BT providers through service intent.
     *
     * @param ctx a context used to retrieve a PackageManager
     */
    private static void getProviders(Context ctx) {
        if (!scannerProviders.isEmpty()) {
            return;
        }

        PackageManager pkManager = ctx.getPackageManager();

        Intent i = new Intent("com.enioka.scan.PROVIDE_SPP_SCANNER");
        List<ResolveInfo> ris = pkManager.queryIntentServices(i, PackageManager.GET_META_DATA);

        for (ResolveInfo ri : ris) {
            // This just avoids processing the same service twice, which could mean duplicate instances now that services are no longer used.
            if (ri.serviceInfo.applicationInfo.uid != ctx.getApplicationInfo().uid) {
                Log.d(PROVIDER_KEY, "Skipping duplicate provider " + ri.serviceInfo.name + " : does not match application UID (Service=" + ri.serviceInfo.applicationInfo.uid + " | Application=" + ctx.getApplicationInfo().uid + ")");
                continue;
            }

            try {
                final BtSppScannerProvider provider = (BtSppScannerProvider) Class.forName(ri.serviceInfo.name).newInstance();
                Log.i(PROVIDER_KEY, "\tSPP SDK compatible provider found: " + ri.serviceInfo.name);
                scannerProviders.add(provider);
            } catch (Exception e) {
                // Could not instantiate
            }
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Provider use: scanner search.
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void getDevices(Context ctx, ScannerSearchOptions options) {
        // Init cache of providers if needed.
        getProviders(ctx);

        // Filter providers based on options.
        sortedScannerProviders.clear();
        for (final BtSppScannerProvider provider : scannerProviders) {
            if (options.excludedProviderKeys != null && options.excludedProviderKeys.contains(provider.getKey())) {
                Log.d(PROVIDER_KEY, "Provider " + provider.getKey() + " skipped because blacklisted by option (excludes " + options.excludedProviderKeys + ")");
                continue;
            }
            if (options.allowedProviderKeys != null && !options.allowedProviderKeys.isEmpty() && !options.allowedProviderKeys.contains(provider.getKey())) {
                Log.d(PROVIDER_KEY, "Provider " + provider.getKey() + " skipped because not whitelisted by option (only allows " + options.allowedProviderKeys + ")");
                continue;
            }
            sortedScannerProviders.add(provider);
        }

        // Init bluetooth
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            return;
        }

        // Cancel discovery because it otherwise slows down the connection.
        btAdapter.cancelDiscovery();

        // Try to contact already paired devices (slave devices).
        if (options.allowInitialSearch) {
            Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
            for (BluetoothDevice bt : pairedDevices) {
                logDeviceInfo(bt);

                // Some devices may be already used by another SDK
                if (this.providerCallback.isAlreadyConnected(bt)) {
                    Log.i(PROVIDER_KEY, "Ignoring device - it is already connected to another app or SDK");
                }

                ScannerInternal btDevice;
                if (bt.getType() == BluetoothDevice.DEVICE_TYPE_CLASSIC) { // Only set for already paired devices.
                    // We only allow SPP devices.
                    boolean found = false;
                    if (bt.getUuids() == null) {
                        continue;
                    }
                    for (ParcelUuid uuid : bt.getUuids()) {
                        if (uuid.getUuid().equals(ClassicBtConnectToDeviceThread.SERVER_BT_SERVICE_UUID)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        continue;
                    }

                    // Launch device resolution
                    btDevice = new ClassicBtSppScanner(this, bt);
                } else {
                    // Launch device resolution
                    btDevice = new BleTerminalIODevice(ctx, bt);
                }
                btDevice.connect(new ConnectionCallback(this));
                passiveScannersCount++;
            }
        }

        // Start incoming SPP server listener (for master devices).
        if (options.allowLaterConnections) {
            server = new ClassicBtAcceptConnectionThread(btAdapter, new ConnectionCallback(this), this);
            server.start();
        }

        if (passiveScannersCount == 0) {
            this.providerCallback.onAllScannersCreated(PROVIDER_KEY);
        }

        // Done.
    }

    private void logDeviceInfo(BluetoothDevice bt) {
        // Compensate for lack of lambdas and String.join in API19.
        List<String> uuids = new ArrayList<>();
        String uuidString = "";
        if (bt.getUuids() != null) {
            for (ParcelUuid uuid : bt.getUuids()) {
                uuids.add(uuid.getUuid().toString());
                uuidString += uuid.getUuid().toString() + ", ";
            }
        }
        if (!uuids.isEmpty()) {
            uuidString = uuidString.substring(0, uuidString.length() - 3);
        }

        // Describe device.
        String desc = bt.getAddress() + " - Name: " + bt.getName() + " - Bond state: " + BtConstHelpers.getBondStateDescription(bt.getBondState()) /*+ bt.getType() + " - " */ + " - Features: " + uuidString + " - Mode " + bt.getType();
        Log.i(PROVIDER_KEY, desc);
        Log.i(PROVIDER_KEY, "Class major: " + BtConstHelpers.getBtMajorClassDescription(bt.getBluetoothClass().getMajorDeviceClass()) + " - Minor: " + BtConstHelpers.getBtClassDescription(bt.getBluetoothClass().getDeviceClass()));
    }

    /**
     * What to do when a connection is successful (i.e. socket opened or GATT server connected): try to resolve the BT SPP provider associated to the device.
     */
    private class ConnectionCallback implements ClassicBtConnectToDeviceThread.OnConnectedCallback {
        private ScannerInternal btDevice;
        private final SerialBtScannerProvider parentProvider;

        // btDevice can be null - created from socket in that case. (master scanner).
        private ConnectionCallback(SerialBtScannerProvider parentProvider) {
            this.parentProvider = parentProvider;
        }

        @Override
        public void connected(ScannerInternal scanner) {
            btDevice = scanner;
            Log.d(PROVIDER_KEY, "A new BT connection was made. Launching provider resolution.");

            new Thread(new ScannerProviderResolutionThread(btDevice, sortedScannerProviders, new ScannerProviderResolutionThread.ScannerResolutionCallback() {
                @Override
                public void onConnection(final Scanner scanner, com.enioka.scanner.bt.api.BtSppScannerProvider compatibleProvider) {
                    Log.i(PROVIDER_KEY, "Scanner " + btDevice.getName() + " was found compatible with provider " + compatibleProvider.getKey());

                    // Set the provider inside the BtSppScanner - it is needed for parsing data.
                    btDevice.setProvider(compatibleProvider);

                    providerCallback.onScannerCreated(getKey(), btDevice.getName(), scanner);
                    waitForScanners.release(1);
                    checkEnd();
                }

                @Override
                public void notCompatible(ScannerInternal device) {
                    Log.i(PROVIDER_KEY, "Scanner " + device + " could not be bound to a provider and will be disconnected");
                    btDevice.disconnect();
                    waitForScanners.release(1);
                    checkEnd();
                }
            })).start();
        }

        @Override
        public void failed() {
            Log.i(PROVIDER_KEY, "Failure to connect to scanner");
            waitForScanners.release(1);
            checkEnd();
        }

        private void checkEnd() {
            if (waitForScanners.tryAcquire(passiveScannersCount)) {
                Log.i(PROVIDER_KEY, "Slave BT SPP scanners are all initialized. Count is " + passiveScannersCount + ". Master scanners connect later.");
                providerCallback.onAllScannersCreated(getKey());
            }
        }
    }
}
