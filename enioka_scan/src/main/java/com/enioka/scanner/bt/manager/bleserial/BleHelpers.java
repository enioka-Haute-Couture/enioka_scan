package com.enioka.scanner.bt.manager.bleserial;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import com.enioka.scanner.bt.manager.data.BleSubscriptionType;
import com.enioka.scanner.bt.manager.data.GattAttribute;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

class BleHelpers {
    private static final String LOG_TAG = "BtSppSdk";


    private static void enableNotification(BluetoothGatt gatt, UUID characteristicId, BleSubscriptionType type) {
        BluetoothGattService service = gatt.getService(GattAttribute.TERMINAL_IO_SERVICE.id);
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(characteristicId);
        if (characteristic != null) {
            gatt.setCharacteristicNotification(characteristic, true);
            characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(GattAttribute.CLIENT_CHARACTERISTIC_CONFIGURATION.id);
            descriptor.setValue(type == BleSubscriptionType.NOTIFICATION ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
            gatt.writeDescriptor(descriptor);
        } else {
            Log.e(LOG_TAG, "Trying to register listener on non-existing characteristic " + characteristicId);
        }
    }

    static void subscribeToCharacteristic(BluetoothGatt gatt, BluetoothGattService service, GattAttribute attribute, BleSubscriptionType type) {
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(attribute.id);
        if (characteristic == null) {
            Log.e(LOG_TAG, "Weird error: device is missing a required characteristic");
            gatt.disconnect();
            throw new RuntimeException("oops"); // TODO.
        }
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(GattAttribute.CLIENT_CHARACTERISTIC_CONFIGURATION.id);
        if (descriptor == null) {
            Log.e(LOG_TAG, "Weird error: device is missing a required characteristic");
            gatt.disconnect();
            throw new RuntimeException("oops"); // TODO.
        }

        Log.i(LOG_TAG, "Subscribing to " + type + " on characteristic " + attribute.name());
        BleHelpers.enableNotification(gatt, characteristic.getUuid(), type);
    }

    /**
     * Simple dump of all known services and attributes in the log. Tries to display names instead of IDs.
     *
     * @param gatt the profile.
     */
    static void logServices(BluetoothGatt gatt) {
        for (BluetoothGattService service : gatt.getServices()) {
            Log.i(LOG_TAG, "\tDevice has service id " + service.getUuid() + " (" + GattAttribute.getAttributeName(service.getUuid()) + ")");

            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                Log.i(LOG_TAG, "\t\tCharacteristic " + characteristic.getUuid() + " (" + GattAttribute.getAttributeName(characteristic.getUuid()) + "). Cached value: " + (characteristic.getValue() != null ? new String(characteristic.getValue(), StandardCharsets.UTF_8) : "null"));
            }
        }
        Log.d(LOG_TAG, "End of services for device " + gatt.getDevice().getName());
    }

    /**
     * Check if the system has a BLE adapter AND if it is enabled. Does not check for permissions.
     *
     * @param ctx a valid context
     * @return true if BLE is ready to use
     */
    static boolean isBleSupported(final Context ctx) {
        if (!ctx.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            return false;
        }

        final BluetoothManager bluetoothManager = (BluetoothManager) ctx.getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager == null) {
            return false;
        }
        final BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }
}
