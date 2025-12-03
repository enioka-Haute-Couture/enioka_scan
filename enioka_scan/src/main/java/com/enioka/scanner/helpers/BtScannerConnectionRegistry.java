package com.enioka.scanner.helpers;

import static android.content.Context.RECEIVER_EXPORTED;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;

import java.io.Closeable;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This tracks BT devices which are already connected - this allows SDKs to avoid competing for already connected devices.
 */
public class BtScannerConnectionRegistry extends BroadcastReceiver implements Closeable {
    private static final String LOG_TAG = "LaserScanner";

    private Set<String> connectedDevices = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
    private Context ctx;
    private boolean registered = false;

    public void register(Context ctx) {
        this.ctx = ctx;
        connectedDevices.clear();

        if (registered) {
            return;
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ctx.registerReceiver(this, filter, RECEIVER_EXPORTED);
        } else {
            ctx.registerReceiver(this, filter);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

        if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
            //Device is now connected
            Log.d(LOG_TAG, "Device " + device.getName() + " has connected");
            connectedDevices.add(device.getAddress());
        } else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
            //Device is about to disconnect
            Log.d(LOG_TAG, "Device " + device.getName() + " is disconnecting");
            connectedDevices.remove(device.getAddress());
        } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
            //Device has disconnected
            Log.d(LOG_TAG, "Device " + device.getName() + " has disconnected");
            connectedDevices.remove(device.getAddress());
        }
    }

    public boolean isAlreadyConnected(BluetoothDevice device) {
        return connectedDevices.contains(device.getAddress());
    }

    @Override
    public void close() {
        if (ctx != null) {
            ctx.unregisterReceiver(this);
        }
        connectedDevices.clear();
    }
}
