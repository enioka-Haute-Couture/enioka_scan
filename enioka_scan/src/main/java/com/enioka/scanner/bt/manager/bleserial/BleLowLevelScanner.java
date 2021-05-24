package com.enioka.scanner.bt.manager.bleserial;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.util.Log;

import com.enioka.scanner.bt.api.BtSppScannerProvider;
import com.enioka.scanner.bt.api.Command;
import com.enioka.scanner.bt.api.DataSubscriptionCallback;
import com.enioka.scanner.bt.manager.common.OnConnectedCallback;
import com.enioka.scanner.bt.manager.common.ScannerInternal;
import com.enioka.scanner.bt.manager.data.BtConnectionType;
import com.enioka.scanner.bt.manager.data.GattAttribute;

/**
 * Main entry point for BLE devices. Responsible for handling connection.
 */
public class BleLowLevelScanner implements ScannerInternal {
    private static final String LOG_TAG = "BtSppSdk";

    // Device
    private final BluetoothDevice btDevice;
    private BluetoothGatt gatt;
    protected BluetoothGattCallback gattCallback;
    private final String deviceName;

    // Relations with providers
    /**
     * The scanner driver.
     */
    private BtSppScannerProvider scannerProvider;


    ///////////////////////////////////////////////////////////////////////////
    // Construction
    ///////////////////////////////////////////////////////////////////////////

    public BleLowLevelScanner(BluetoothDevice btDevice, BluetoothGattCallback gattCallback) {
        this.btDevice = btDevice;
        this.deviceName = btDevice.getName();
        this.gattCallback = gattCallback;
    }


    ///////////////////////////////////////////////////////////////////////////
    // Connection stuff
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void connect(final OnConnectedCallback callback) {
        this.callback = callback;

        if (btDevice.getType() != BluetoothDevice.DEVICE_TYPE_LE && btDevice.getType() != BluetoothDevice.DEVICE_TYPE_DUAL) {
            Log.i(LOG_TAG, "Trying to connect to GATT with a non-BLE device " + this.deviceName);
            callback.failed();
            return;
        }

        Log.i(LOG_TAG, "Starting connection to device " + this.deviceName);

        
        btDevice.connectGatt(ctx, true, gattCallback);
    }


    @Override
    public void disconnect() {
        if (gatt != null) {
            this.gatt.disconnect();
            this.gatt = null;
            this.gattCallback = null;
        }
    }


    ///////////////////////////////////////////////////////////////////////////
    // Other lifecycle methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void registerStatusCallback(SppScannerStatusCallback statusCallback) {

    }


    ///////////////////////////////////////////////////////////////////////////
    // Relations with providers
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void setProvider(BtSppScannerProvider provider) {
        this.scannerProvider = provider;
    }

    @Override
    public String getName() {
        return this.deviceName;
    }

    @Override
    public BtConnectionType getConnectionType() {
        return BtConnectionType.BLE;
    }


    ///////////////////////////////////////////////////////////////////////////
    // Commands
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public <T> void runCommand(Command<T> command, DataSubscriptionCallback<T> subscription) {

    }

    @Override
    public <T> void registerSubscription(DataSubscriptionCallback<T> subscription, Class<? extends T> targetType) {

    }
}
