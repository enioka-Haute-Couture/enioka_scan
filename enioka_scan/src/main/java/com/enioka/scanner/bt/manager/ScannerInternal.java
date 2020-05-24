package com.enioka.scanner.bt.manager;

import com.enioka.scanner.bt.api.BtSppScannerProvider;
import com.enioka.scanner.bt.api.Scanner;

/**
 * An abstraction used internally to address the different kinds of BT devices (BLE TIO, classic)
 */
public interface ScannerInternal extends Scanner {
    void connect(final ClassicBtConnectToDeviceThread.OnConnectedCallback callback);

    void setProvider(BtSppScannerProvider provider);
}
