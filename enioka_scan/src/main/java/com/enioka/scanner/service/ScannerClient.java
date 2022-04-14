package com.enioka.scanner.service;

import com.enioka.scanner.api.callbacks.ScannerStatusCallback;
import com.enioka.scanner.data.Barcode;

import java.util.List;

public interface ScannerClient extends ScannerStatusCallback {
    void onScannerInitEnded(int count);

    void onData(List<Barcode> data);
}
