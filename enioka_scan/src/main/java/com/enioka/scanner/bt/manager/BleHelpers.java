package com.enioka.scanner.bt.manager;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

class BleHelpers {
    private static final String LOG_TAG = "BtSppSdk";

    // Common connection status codes. https://android.googlesource.com/platform/external/bluetooth/bluedroid/+/refs/heads/master/stack/include/gatt_api.h
    // 00 = programmatically disconnected
    // 08 = device out of range
    // 19 0x13 = disconnected by device/ connection terminate by peer user
    // 22 = issue with bond
    // 62 = device not found
    // 133 = device not found.

    public static void get(final Context ctx) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            internalGet(ctx);
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private static void internalGet(final Context ctx) {
        if (!isBleSupported(ctx)) {
            return;
        }

        final BluetoothManager bluetoothManager = (BluetoothManager) ctx.getSystemService(Context.BLUETOOTH_SERVICE);
        final BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        final HashSet<String> foundDevices = new HashSet<>();

        // We do NOT filter during scan - this is way too buggy on Android. We must filter on returned devices.
        List<ScanFilter> filters = new ArrayList<>();
        ScanSettings scanSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_BALANCED).build();

        final BluetoothLeScanner leScanner = bluetoothAdapter.getBluetoothLeScanner();
        final ScanCallback callback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                final BluetoothDevice btDevice = result.getDevice();
                if (btDevice == null) {
                    return;
                }
                if (foundDevices.contains(btDevice.getAddress())) {
                    return;
                }
                foundDevices.add(btDevice.getAddress());
                Log.i(LOG_TAG, "A new BT device was returned. Start of analysis: is it a usable BLE device? " + btDevice.getName() + " - " + btDevice.getAddress());

                BleTerminalIODevice device = new BleTerminalIODevice(ctx, btDevice);
                device.connect(new ClassicBtConnectToDeviceThread.OnConnectedCallback() {
                    @Override
                    public void connected(BluetoothScannerInternal scanner) {

                    }

                    @Override
                    public void failed() {

                    }
                });

            }
        };
        leScanner.startScan(filters, scanSettings, callback);

        // Stop scan handler quickly (it is very battery-intensive)
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.i(LOG_TAG, "End of BLE search");
                leScanner.stopScan(callback);
            }
        }, 10000);
    }

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
