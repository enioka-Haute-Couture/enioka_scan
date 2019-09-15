package com.enioka.scanner.bt;

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
    boolean canManageDevice(BtDevice device);

    /**
     * The {@link BtInputHandler} which should be used to parse results.
     */
    BtInputHandler getInputHandler();
}
