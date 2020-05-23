package com.enioka.scanner.bt.manager;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.support.annotation.Nullable;
import android.util.Log;

import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.api.ScannerProvider;
import com.enioka.scanner.api.ScannerProviderBinder;
import com.enioka.scanner.api.ScannerSearchOptions;
import com.enioka.scanner.bt.api.BtSppScannerProviderServiceBinder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;

/**
 * Responsible for finding bluetooth devices and starting their initialization, converting them to {@link BtSppScanner} and {@link com.enioka.scanner.api.Scanner}.<br><br>
 * This is the entry point of the BT SPP SDK for the rest of the library.
 */
public class BtSppScannerProvider extends Service implements ScannerProvider {
    private static final String LOG_TAG = "BtSppSdk";

    private static List<com.enioka.scanner.bt.api.BtSppScannerProvider> scannerProviders = new ArrayList<>();
    private static List<ServiceConnection> connections = new ArrayList<>();

    private AcceptBtConnectionThread server;
    private ProviderCallback providerCallback;
    private Semaphore waitForScanners = new Semaphore(0);
    private int passiveScannersCount = 0;


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Bound service stuff
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private final IBinder binder = new ScannerProviderBinder(this);

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }


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
        return LOG_TAG;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Misc
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Allow new master BT SPP devices to connect to the Android device.
     */
    void resetListener(BluetoothAdapter bluetoothAdapter) {
        if (server == null || server.isDone()) {
            server = new AcceptBtConnectionThread(bluetoothAdapter, new ConnectionCallback(this), this);
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
     * Callbacks for bound service connections (useful since SPP providers are services).
     */
    private static ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            BtSppScannerProviderServiceBinder binder = (BtSppScannerProviderServiceBinder) service;
            com.enioka.scanner.bt.api.BtSppScannerProvider provider = binder.getService();
            Log.i(LOG_TAG, "Provider " + provider.getClass().getSimpleName() + " was registered");
            scannerProviders.add(provider);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            for (ServiceConnection sc : connections) {
                //if (connection.get)
                // TODO: remove from collection, close everything related to this provider.
            }
        }
    };

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
            ComponentName name = new ComponentName(ri.serviceInfo.packageName, ri.serviceInfo.name);
            Intent boundServiceIntent = new Intent();
            boundServiceIntent.setClassName(ctx, name.getClassName());

            ctx.bindService(boundServiceIntent, connection, Context.BIND_AUTO_CREATE);
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Provider use: scanner search.
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void getDevices(Context ctx, ScannerSearchOptions options) {
        // Init providers if needed.
        getProviders(ctx);

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
                    Log.i(LOG_TAG, "Ignoring device - it is already connected to another app or SDK");
                }

                ScannerInternal btDevice;
                if (bt.getType() == BluetoothDevice.DEVICE_TYPE_CLASSIC) { // Only set for already paired devices.
                    // We only allow SPP devices.
                    boolean found = false;
                    if (bt.getUuids() == null) {
                        continue;
                    }
                    for (ParcelUuid uuid : bt.getUuids()) {
                        if (uuid.getUuid().equals(ConnectToBtDeviceThread.SERVER_BT_SERVICE_UUID)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        continue;
                    }

                    // Launch device resolution
                    btDevice = new BtSppScanner(this, bt);
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
            server = new AcceptBtConnectionThread(btAdapter, new ConnectionCallback(this), this);
            server.start();
        }

        if (passiveScannersCount == 0) {
            this.providerCallback.onAllScannersCreated(LOG_TAG);
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
        Log.i(LOG_TAG, desc);
        Log.i(LOG_TAG, "Class major: " + BtConstHelpers.getBtMajorClassDescription(bt.getBluetoothClass().getMajorDeviceClass()) + " - Minor: " + BtConstHelpers.getBtClassDescription(bt.getBluetoothClass().getDeviceClass()));
    }

    /**
     * What to do when a connection is successful (i.e. socket opened or GATT server connected): try to resolve the BT SPP provider associated to the device.
     */
    private class ConnectionCallback implements ConnectToBtDeviceThread.OnConnectedCallback {
        private ScannerInternal btDevice;
        private final BtSppScannerProvider parentProvider;

        // btDevice can be null - created from socket in that case. (master scanner).
        private ConnectionCallback(BtSppScannerProvider parentProvider) {
            this.parentProvider = parentProvider;
        }

        @Override
        public void connected(ScannerInternal scanner) {
            btDevice = scanner;
            Log.d(LOG_TAG, "A new BT connection was made. Launching provider resolution.");

            new Thread(new ScannerResolutionThread(btDevice, scannerProviders, new ScannerResolutionThread.ScannerResolutionCallback() {
                @Override
                public void onConnection(final Scanner scanner, com.enioka.scanner.bt.api.BtSppScannerProvider compatibleProvider) {
                    Log.i(LOG_TAG, "Scanner " + btDevice.getName() + " was found compatible with provider " + compatibleProvider.getClass().getSimpleName());

                    // Set the provider inside the BtSppScanner - it is needed for parsing data.
                    btDevice.setProvider(compatibleProvider);

                    providerCallback.onScannerCreated(getKey(), btDevice.getName(), scanner);
                    waitForScanners.release(1);
                    checkEnd();
                }

                @Override
                public void notCompatible(ScannerInternal device) {
                    Log.i(LOG_TAG, "Scanner " + device + " could not be bound to a provider and will be disconnected");
                    btDevice.disconnect();
                    waitForScanners.release(1);
                    checkEnd();
                }
            })).start();
        }

        @Override
        public void failed() {
            Log.i(LOG_TAG, "Failure to connect to scanner");
            waitForScanners.release(1);
            checkEnd();
        }

        private void checkEnd() {
            if (waitForScanners.tryAcquire(passiveScannersCount)) {
                Log.i(LOG_TAG, "Slave BT SPP scanners are all initialized. Count is " + passiveScannersCount + ". Master scanners connect later.");
                providerCallback.onAllScannersCreated(getKey());
            }
        }
    }
}