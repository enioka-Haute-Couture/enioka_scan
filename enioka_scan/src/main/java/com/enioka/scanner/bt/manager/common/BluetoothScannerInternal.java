package com.enioka.scanner.bt.manager.common;

import com.enioka.scanner.bt.api.BtSppScannerProvider;
import com.enioka.scanner.bt.api.BluetoothScanner;

/**
 * An abstraction used internally to address the different kinds of BT devices (BLE TIO, classic)
 */
public interface BluetoothScannerInternal extends BluetoothScanner {
    void connect(final OnConnectedCallback callback);

    void setProvider(BtSppScannerProvider provider);
}
