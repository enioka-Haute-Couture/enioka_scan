package com.enioka.scanner.bt.api;

/**
 * A specialized form of {@link com.enioka.scanner.api.ScannerProvider} for Bluetooth Serial Port Profile scanners.<br>
 * Implementations are all thread-safe.
 */
public interface BtSppScannerProvider {
    /**
     * Tests whether a scanner is compatible with the provider. Must return under 50ms.
     *
     * @param device a connected device, ready to run commands.
     * @return true if compatible.
     */
    void canManageClassicDevice(Scanner device, ManagementCallback callback);

    void canManageBleDevice(Scanner device, ManagementCallback callback);

    /**
     * The {@link ScannerDataParser} which should be used to parse results.
     */
    ScannerDataParser getInputHandler();

    /**
     * A way to signal that this provider can or cannot manage a device.
     */
    interface ManagementCallback {
        void canManage(com.enioka.scanner.api.Scanner libraryScanner);

        void cannotManage();
    }
}
