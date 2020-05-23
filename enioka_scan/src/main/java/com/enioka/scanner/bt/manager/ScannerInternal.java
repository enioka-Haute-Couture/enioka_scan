package com.enioka.scanner.bt.manager;

import com.enioka.scanner.bt.api.BtSppScannerProvider;
import com.enioka.scanner.bt.api.Scanner;

public interface ScannerInternal extends Scanner {
    void connect(final ConnectToBtDeviceThread.OnConnectedCallback callback);

    void setProvider(BtSppScannerProvider provider);
}
