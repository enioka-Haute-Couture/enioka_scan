package com.enioka.scanner.bt.manager.common;

import android.bluetooth.BluetoothAdapter;

/**
 * Some scanners connect as masters onto the slave Android device.
 * This means the Android device must have an open listener.
 * This interface provides callback for providers to influence this listener lifecycle.
 */
public interface SerialBtScannerPassiveConnectionManager {
    /**
     * Signals the provider that all passive connection listeners must be restarted. Used to allow reconnection of a master device in case of accidental disconnection.
     *
     * @param bluetoothAdapter
     */
    void resetListener(BluetoothAdapter bluetoothAdapter);

    /**
     * Signal the provider that it is no longer necessary to keep passive connection listeners open.
     * This is called when a master device has successfully connected - in this case the BT listener should be closed as it is a battery hog.
     */
    void stopMasterListener();
}
