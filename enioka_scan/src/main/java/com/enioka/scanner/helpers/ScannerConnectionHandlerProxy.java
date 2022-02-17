package com.enioka.scanner.helpers;

import android.os.Handler;
import android.os.Looper;

import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.api.ScannerConnectionHandler;

/**
 * A helper to put all interactions with the caller on the UI thread.
 */
public class ScannerConnectionHandlerProxy implements ScannerConnectionHandler {
    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private final ScannerConnectionHandler encapsulatedHandler;

    public ScannerConnectionHandlerProxy(ScannerConnectionHandler encapsulatedHandler) {
        this.encapsulatedHandler = encapsulatedHandler;
    }

    @Override
    public void scannerConnectionProgress(final String providerKey, final String scannerKey, final String message) {
        uiHandler.post(() -> encapsulatedHandler.scannerConnectionProgress(providerKey, scannerKey, message));
    }

    @Override
    public void scannerCreated(final String providerKey, final String scannerKey, final Scanner s) {
        uiHandler.post(() -> encapsulatedHandler.scannerCreated(providerKey, scannerKey, s));
    }

    @Override
    public void noScannerAvailable() {
        uiHandler.post(encapsulatedHandler::noScannerAvailable);
    }

    @Override
    public void endOfScannerSearch() {
        uiHandler.post(encapsulatedHandler::endOfScannerSearch);
    }
}
