package com.enioka.scanner.bt.manager.common;

import com.enioka.scanner.bt.api.BtSppScannerProvider;
import com.enioka.scanner.bt.api.Scanner;
import com.enioka.scanner.bt.manager.data.BtConnectionType;

/**
 * An abstraction used internally to address the different kinds of BT devices (BLE TIO, classic)
 */
public interface ScannerInternal extends Scanner {
    void connect(final OnConnectedCallback callback);

    void setProvider(BtSppScannerProvider provider);

    BtConnectionType getConnectionType();
}
