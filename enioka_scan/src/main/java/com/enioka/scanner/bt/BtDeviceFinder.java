package com.enioka.scanner.bt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.ParcelUuid;
import android.util.Log;

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

            if (bt.getName().startsWith("GS")) {
                tmp2 = bt;
            }
        }

        temp = new BtDevice(tmp2, btAdapter);
        temp.connect();

        return pairedDevices;
    }
}
