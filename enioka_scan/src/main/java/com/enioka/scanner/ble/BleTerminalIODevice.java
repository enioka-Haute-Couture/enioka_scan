package com.enioka.scanner.ble;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.Semaphore;

public class BleTerminalIODevice implements BleStateMachineDevice {
    private static final String LOG_TAG = "BtSppSdk";

    // Device
    private BluetoothGatt gatt;
    private String deviceName;
    private String deviceAddress;
    private BluetoothDevice btDevice;

    // Credits
    private Semaphore clientCredits = new Semaphore(0);
    private Semaphore serverCredits = new Semaphore(0);
    private static final byte MIN_SERVER_CREDITS = 20;
    private static final byte SERVER_CREDITS_ALLOCATION = 40;

    // State
    private enum TioState {
        INITIAL, SUBSCRIBED_TO_CREDIT, SUBSCRIBED_TO_DATA, READY
    }

    private TioState currentState = TioState.INITIAL;

    BleTerminalIODevice(BluetoothGatt gatt) {
        this.gatt = gatt;
        this.btDevice = gatt.getDevice();
        this.deviceName = this.btDevice.getName();
        this.deviceAddress = this.btDevice.getAddress();
    }

    static boolean isCompatibleWith(BluetoothGatt gatt) {
        return gatt.getService(GattAttribute.TERMINAL_IO_SERVICE.id) != null;
    }

    @Override
    public void onEvent(BleEvent event) {
        // State 0 is: has just been retrieved from BT stack.

        // State 0
        BluetoothGattService service = gatt.getService(GattAttribute.TERMINAL_IO_SERVICE.id);
        if (service == null) {
            Log.w(LOG_TAG, "A device was created before service discovery - likely a bug. Restarting discovery.");
            gatt.discoverServices();
            return;
        }

        // Advance state according to event.
        if (event.nature.equals(BleEventNature.RESET)) {
            currentState = TioState.INITIAL;
        }
        switch (currentState) {
            case INITIAL:
                if (event.nature == BleEventNature.DESCRIPTOR_WRITE_SUCCESS && event.parentAttribute == GattAttribute.TERMINAL_IO_UART_CREDITS_TX) {
                    currentState = TioState.SUBSCRIBED_TO_CREDIT;
                }
                break;
            case SUBSCRIBED_TO_CREDIT:
                if (event.nature == BleEventNature.DESCRIPTOR_WRITE_SUCCESS && event.parentAttribute == GattAttribute.TERMINAL_IO_UART_DATA_TX) {
                    currentState = TioState.SUBSCRIBED_TO_DATA;
                }
                break;
            case SUBSCRIBED_TO_DATA:
                if (event.nature == BleEventNature.CHARACTERISTIC_WRITE_SUCCESS && event.targetAttribute == GattAttribute.TERMINAL_IO_UART_CREDITS_RX) {
                    currentState = TioState.READY;
                }
                break;
            case READY:
                // Nothing to do.
                break;
        }


        // Trigger specific actions on each state in order to progress inside the TIO lifecycle.
        switch (currentState) {
            case INITIAL:
                BleManager.subscribeToCharacteristic(gatt, service, GattAttribute.TERMINAL_IO_UART_CREDITS_TX, BleSubscriptionType.INDICATION);
                return;
            case SUBSCRIBED_TO_CREDIT:
                BleManager.subscribeToCharacteristic(gatt, service, GattAttribute.TERMINAL_IO_UART_DATA_TX, BleSubscriptionType.NOTIFICATION);
                return;
            case SUBSCRIBED_TO_DATA:
                sendCreditsToServerIfNeeded(gatt);
                return;
            case READY:
                sendCreditsToServerIfNeeded(gatt);
                if (event.nature == BleEventNature.CHARACTERISTIC_CHANGED_SUCCESS && event.targetAttribute == GattAttribute.TERMINAL_IO_UART_DATA_TX) {
                    handleData(event);
                }
                if (event.nature == BleEventNature.CHARACTERISTIC_CHANGED_SUCCESS && event.targetAttribute == GattAttribute.TERMINAL_IO_UART_CREDITS_TX) {
                    handleCredit(event);
                }
                break;
        }
    }

    private void handleData(BleEvent event) {
        Log.i(LOG_TAG, "Received data " + new String(event.data, StandardCharsets.UTF_8));
    }

    private void handleCredit(BleEvent event) {
        Log.i(LOG_TAG, "Received client credits " + event.data[0]);
        clientCredits.release(event.data[0]);
    }

    private boolean sendCreditsToServerIfNeeded(BluetoothGatt gatt) {
        if (serverCredits.availablePermits() >= MIN_SERVER_CREDITS) {
            return false;
        }

        BluetoothGattService service = gatt.getService(GattAttribute.TERMINAL_IO_SERVICE.id);
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(GattAttribute.TERMINAL_IO_UART_CREDITS_RX.id);
        if (characteristic != null) {
            Log.i(LOG_TAG, "Sending credits to remote device");
            characteristic.setValue(new byte[]{SERVER_CREDITS_ALLOCATION});
            gatt.writeCharacteristic(characteristic);
            serverCredits.release(SERVER_CREDITS_ALLOCATION);
            return true;
        }
        return false;
    }
}
