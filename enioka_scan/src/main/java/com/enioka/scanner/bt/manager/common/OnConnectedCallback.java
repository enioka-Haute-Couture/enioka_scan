package com.enioka.scanner.bt.manager.common;

/**
 * A callback used when a device has succeeded or failed to connect.
 */
public interface OnConnectedCallback {
    void connected(BluetoothScannerInternal scanner);

    void failed();
}