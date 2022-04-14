package com.enioka.scanner.bt.manager;

import com.enioka.scanner.bt.api.BtSppScannerProvider;
import com.enioka.scanner.bt.api.BluetoothScanner;

/**
 * An abstraction used internally to address the different kinds of BT devices (BLE TIO, classic)
 */
public interface BluetoothScannerInternal extends BluetoothScanner {
    void connect(final ClassicBtConnectToDeviceThread.OnConnectedCallback callback);

    void setProvider(BtSppScannerProvider provider);
}
