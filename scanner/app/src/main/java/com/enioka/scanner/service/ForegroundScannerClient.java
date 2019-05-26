package com.enioka.scanner.service;

public interface ForegroundScannerClient extends BackgroundScannerClient {
    void onForegroundScannerInitEnded(int foregroundScannerCount, int backgroundScannerCount);
}
