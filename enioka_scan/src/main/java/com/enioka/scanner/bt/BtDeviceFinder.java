package com.enioka.scanner.bt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Responsible for finding bluetooth devices and starting their initialization, converting them to {@link BtDevice} and {@link com.enioka.scanner.api.Scanner}.
 */
public class BtDeviceFinder {
    private static final String LOG_TAG = "InternalBtDevice";
    private static AcceptBtConnectionThread server;

    private static List<BtSppScannerProvider> scannerProviders = new ArrayList<>();
    private static List<ServiceConnection> connections = new ArrayList<>();


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
            BtSppScannerProvider provider = binder.getService();
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

    public static void getDevices(Context ctx) {
        // Init providers if needed.
        getProviders(ctx);

        // Init bluetooth
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            return;
        }

        // Try to contact already paired devices.
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        for (BluetoothDevice bt : pairedDevices) {
            /////////////////////////////////////////////////////////////
            // Log

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
            String desc = bt.getAddress() + " - Name: " + bt.getName() + " - Bond state: " + BtConstHelpers.getBondStateDescription(bt.getBondState()) /*+ bt.getType() + " - " */ + " - Features: " + uuidString;
            Log.i(LOG_TAG, desc);
            Log.i(LOG_TAG, "Class major: " + BtConstHelpers.getBtMajorClassDescription(bt.getBluetoothClass().getMajorDeviceClass()) + " - Minor: " + BtConstHelpers.getBtClassDescription(bt.getBluetoothClass().getDeviceClass()));

            // Sample name start: GS, Voyager, RS6000

            // end log
            /////////////////////////////////////////////////////////////


            // We only allow SPP devices.
            if (!uuids.contains(ConnectToBtDeviceThread.SERVER_BT_SERVICE_UUID.toString())) {
                continue;
            }

            // Launch device resolution
            BtDevice btDevice = new BtDevice(bt);
            btDevice.connect(btAdapter, new ConnectionCallback(btDevice));
        }

        // Start incoming SPP server listener.
        server = new AcceptBtConnectionThread(btAdapter, new ConnectionCallback(null));
        server.start();

        // Done.
    }

    /**
     * What to do when a connection is successful (i.e. socket opened): try to resolve the provider associated to the device.
     */
    private static class ConnectionCallback implements ConnectToBtDeviceThread.OnConnectedCallback {
        private BtDevice btDevice;

        // btDevice cas be null - created from socket in that case. (master scanner).
        private ConnectionCallback(BtDevice btDevice) {
            this.btDevice = btDevice;
        }

        @Override
        public void connected(BluetoothSocket bluetoothSocket) {
            if (btDevice == null) {
                Log.d(LOG_TAG, "A new BT connection was made (scanner is master). Launching provider resolution.");
                btDevice = new BtDevice(bluetoothSocket);
            } else {
                Log.d(LOG_TAG, "A new BT connection was made (scanner is slave). Launching provider resolution.");
            }

            new Thread(new BtSppScannerResolutionThread(btDevice, scannerProviders, new BtSppScannerResolutionThread.ScannerResolutionCallback() {
                @Override
                public void onConnection(BtDevice scanner, BtSppScannerProvider compatibleProvider) {
                    Log.i(LOG_TAG, "Scanner " + btDevice.getName() + " was found compatible with provider " + compatibleProvider.getClass().getSimpleName());

                    // Set the provider inside the BtDevice - it is needed for parsing data.
                    btDevice.setProvider(compatibleProvider);
                }

                @Override
                public void notCompatible(BtDevice device) {
                    Log.i(LOG_TAG, "Scanner " + device + " could not be bound to a provider");
                }
            })).start();
        }

        @Override
        public void failed() {
            Log.i(LOG_TAG, "Failure to connect to scanner");
        }
    }
}
