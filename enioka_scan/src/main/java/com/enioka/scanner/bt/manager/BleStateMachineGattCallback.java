package com.enioka.scanner.bt.manager;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.util.Log;

import java.nio.charset.StandardCharsets;

/**
 * The callback used by Android GATT server when it must signal events to one of our "state machine" instances.
 */
class BleStateMachineGattCallback extends BluetoothGattCallback {
    private static final String LOG_TAG = "BtSppSdk";

    private final BleStateMachineDevice scanner;
    private final OnConnectedCallback callback;

    private boolean firstConnectionDone = false;

    BleStateMachineGattCallback(BleStateMachineDevice parent, OnConnectedCallback callback) {
        this.scanner = parent;
        this.callback = callback;
    }

    // TODO: check if this could not become an event like any other.
    interface OnConnectedCallback {
        /**
         * Called when the scanner is connected the first time.
         */
        void onConnected(BluetoothGatt gatt);

        /**
         * Called when the first connection fails.
         */
        void onConnectionFailure();
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            switch (newState) {
                case BluetoothGatt.STATE_DISCONNECTED:
                    Log.i(LOG_TAG, "BLE scanner disconnected successfully " + gatt.getDevice().getName() + " - " + gatt.getDevice().getAddress());
                    break;
                case BluetoothGatt.STATE_CONNECTED:
                    Log.i(LOG_TAG, "BLE scanner connected successfully. " + gatt.getDevice().getName() + " - " + gatt.getDevice().getAddress());
                    if (gatt.getServices().isEmpty()) {
                        Log.i(LOG_TAG, "Starting service discovery. " + gatt.getDevice().getName() + " - " + gatt.getDevice().getAddress());
                        gatt.discoverServices();
                    } else {
                        Log.i(LOG_TAG, "Service discovery already done. " + gatt.getDevice().getName() + " - " + gatt.getDevice().getAddress());
                        if (!firstConnectionDone) {
                            callback.onConnected(gatt);
                            firstConnectionDone = true;
                        } else {
                            scanner.onEvent(BleStateMachineDevice.BleEvent.RESET_EVENT);
                        }
                    }
                    break;
                default:
                    Log.i(LOG_TAG, "Weird new connection state: " + newState);
                    break;
            }
        } else {
            Log.e(LOG_TAG, "Error when communicating with GATT server. Status is " + status + ". New state is " + newState + ". Device: " + gatt.getDevice().getName() + " - " + gatt.getDevice().getAddress());
            if (!firstConnectionDone) {
                callback.onConnectionFailure();
            }
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        if (status != BluetoothGatt.GATT_SUCCESS) {
            Log.e(LOG_TAG, "Error when discovering services. Status is " + status + ". Device " + gatt.getDevice().getName() + " - " + gatt.getDevice().getAddress());
            return;
        }
        Log.i(LOG_TAG, "Services discovered for device " + gatt.getDevice().getName() + " - " + gatt.getDevice().getAddress());
        BleHelpers.logServices(gatt);
        firstConnectionDone = true;
        callback.onConnected(gatt);
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            Log.i(LOG_TAG, "\t\tRead characteristic " + characteristic.getUuid() + " known name " + GattAttribute.getAttributeName(characteristic.getUuid()) + " cached value " + (characteristic.getValue() != null ? new String(characteristic.getValue(), StandardCharsets.UTF_8) : "null"));
        }
        scanner.onEvent(new BleStateMachineDevice.BleEvent(characteristic, BleStateMachineDevice.BleEventNature.CHARACTERISTIC_READ_SUCCESS));
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        if (status != BluetoothGatt.GATT_SUCCESS) {
            Log.e(LOG_TAG, "Error when writing characteristic. Status is " + status + ". Characteristic " + characteristic.getUuid());
        }
        Log.i(LOG_TAG, "Success writing characteristic " + characteristic.getUuid());
        scanner.onEvent(new BleStateMachineDevice.BleEvent(characteristic, BleStateMachineDevice.BleEventNature.CHARACTERISTIC_WRITE_SUCCESS));
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        Log.i(LOG_TAG, "\t\tChanged characteristic " + characteristic.getUuid() + " (" + GattAttribute.getAttributeName(characteristic.getUuid()) + ") cached value " + (characteristic.getValue() != null ? new String(characteristic.getValue(), StandardCharsets.UTF_8) : "null"));
        scanner.onEvent(new BleStateMachineDevice.BleEvent(characteristic, BleStateMachineDevice.BleEventNature.CHARACTERISTIC_CHANGED_SUCCESS));
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        if (status != BluetoothGatt.GATT_SUCCESS) {
            Log.e(LOG_TAG, "Error when writing descriptor. Status is " + status + ". Descriptor is " + descriptor.getUuid() + " for characteristic " + descriptor.getCharacteristic().getUuid());
        } else {
            Log.d(LOG_TAG, "Success writing descriptor " + descriptor.getUuid() + " for characteristic " + descriptor.getCharacteristic().getUuid());
            //handleDescriptorChange(gatt, descriptor);
            scanner.onEvent(new BleStateMachineDevice.BleEvent(descriptor, BleStateMachineDevice.BleEventNature.DESCRIPTOR_WRITE_SUCCESS));
        }
    }
}
