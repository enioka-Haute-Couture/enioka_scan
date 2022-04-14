package com.enioka.scanner.service;

import com.enioka.scanner.api.callbacks.ScannerStatusCallback;
import com.enioka.scanner.data.Barcode;

import java.util.List;

public interface BackgroundScannerClient extends ScannerStatusCallback {
    void onBackgroundScannerInitEnded(int count);

    void onData(List<Barcode> data);
}
