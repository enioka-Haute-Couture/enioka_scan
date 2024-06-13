package com.enioka.scanner.bt.api;

import com.enioka.scanner.api.Scanner;

/**
 * A specialized form of {@link com.enioka.scanner.api.ScannerProvider} for Bluetooth Serial Port Profile scanners.<br>
 * Implementations are all thread-safe.
 */
public interface BtSppScannerProvider extends BarcodePairing {
    /**
     * Tests whether a scanner is compatible with the provider. Must return under 50ms.
     *
     * @param device a connected device, ready to run commands.
     * @return true if compatible.
     */
    void canManageDevice(BluetoothScanner device, ManagementCallback callback);

    /**
     * The unique key which identifies this provider.
     *
     * @return the key
     */
    String getKey();

    /**
     * The {@link ScannerDataParser} which should be used to parse results.
     */
    ScannerDataParser getInputHandler();

    /**
     * A way to signal that this provider can or cannot manage a device.
     */
    interface ManagementCallback {
        void canManage(Scanner libraryScanner);

        void cannotManage();
    }
}
