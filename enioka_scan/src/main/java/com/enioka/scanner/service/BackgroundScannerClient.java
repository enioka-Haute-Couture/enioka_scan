package com.enioka.scanner.service;

import com.enioka.scanner.data.Barcode;

import java.util.List;

public interface BackgroundScannerClient {
    void onStatusChanged(String newStatus);

    void onBackgroundScannerInitEnded(int count);

    void onData(List<Barcode> data);
}