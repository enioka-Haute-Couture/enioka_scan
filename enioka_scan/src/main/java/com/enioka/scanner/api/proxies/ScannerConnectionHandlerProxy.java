package com.enioka.scanner.api.proxies;

import android.os.Handler;
import android.os.Looper;

import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.api.callbacks.ScannerConnectionHandler;

public class ScannerConnectionHandlerProxy implements ScannerConnectionHandler {
    protected final Handler uiHandler;
    protected final ScannerConnectionHandler wrappedHandler;

    public ScannerConnectionHandlerProxy(final ScannerConnectionHandler wrappedHandler) {
        this.uiHandler = new Handler(Looper.getMainLooper());
        this.wrappedHandler = wrappedHandler;
    }

    @Override
    public void scannerConnectionProgress(final String providerKey, final String scannerKey, final String message) {
        uiHandler.post(() -> wrappedHandler.scannerConnectionProgress(providerKey, scannerKey, message));
    }

    @Override
    public void scannerCreated(final String providerKey, final String scannerKey, final Scanner s) {
        uiHandler.post(() -> wrappedHandler.scannerCreated(providerKey, scannerKey, s));
    }

    @Override
    public void noScannerAvailable() {
        uiHandler.post(wrappedHandler::noScannerAvailable);
    }

    @Override
    public void endOfScannerSearch() {
        uiHandler.post(wrappedHandler::endOfScannerSearch);
    }
}
