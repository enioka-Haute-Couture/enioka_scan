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

import com.enioka.scanner.api.Scanner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Helpers to find bound devices.
 */
public class BtDeviceFinder {
    private static final String LOG_TAG = "BtDeviceFinder";
    private static BtDevice temp;
    private static AcceptBtConnectionThread server;

    private static List<BtSppScannerProvider> scannerProviders = new ArrayList<>();
    private static List<ServiceConnection> connections = new ArrayList<>();

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
            }
        }
    };

    /**
     * Retrieve SPP BT providers through service intent.
     *
     * @param ctx a context used to retrieve a PackageManager
     */
    public static void getProviders(Context ctx) {
        PackageManager pkManager = ctx.getPackageManager();

        Intent i = new Intent("com.enioka.scan.PROVIDE_SCANNER");
        List<ResolveInfo> ris = pkManager.queryIntentServices(i, PackageManager.GET_META_DATA);

        for (ResolveInfo ri : ris) {
            ComponentName name = new ComponentName(ri.serviceInfo.packageName, ri.serviceInfo.name);
            Intent boundServiceIntent = new Intent();
            boundServiceIntent.setClassName(ctx, name.getClassName());

            ctx.bindService(boundServiceIntent, connection, Context.BIND_AUTO_CREATE);
        }
    }

    public static Set<BluetoothDevice> getDevices() {
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            return new HashSet<>();
        }

        BluetoothDevice tmp2 = null;
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        for (BluetoothDevice bt : pairedDevices) {
            // Compensate for lack of lambdas and String.join in API16.
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

            // Launch device resolution
            final BtDevice btDevice = new BtDevice(bt);
            btDevice.connect(btAdapter, new ConnectToBtDeviceThread.OnConnectedCallback() {
                @Override
                public void connected(BluetoothSocket bluetoothSocket) {
                    new Thread(new BtSppScannerResolutionThread(btDevice, scannerProviders, new BtSppScannerResolutionThread.ScannerResolutionCallback() {
                        @Override
                        public void onConnection(Scanner scanner) {
                            Log.i(LOG_TAG, "A scanner was found compatible with a provider!");
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
            });
        }

        server = new AcceptBtConnectionThread(btAdapter, new ConnectToBtDeviceThread.OnConnectedCallback() {
            @Override
            public void connected(BluetoothSocket bluetoothSocket) {
                Log.i(LOG_TAG, "Received new BT incoming connection");
                temp = new BtDevice(bluetoothSocket);
            }

            @Override
            public void failed() {
                Log.i(LOG_TAG, "WWWWWWWWWWWWW");
            }
        });
        server.start();

        return pairedDevices;
    }
}
