package com.enioka.scanner.helpers;

import android.os.Handler;
import android.os.Looper;

import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.api.ScannerConnectionHandler;

/**
 * A helper to put all interactions with the caller on the UI thread.
 */
public class ScannerConnectionHandlerProxy implements ScannerConnectionHandler {
    private final Handler handler;
    private final ScannerConnectionHandler encapsulatedHandler;

    public ScannerConnectionHandlerProxy(ScannerConnectionHandler encapsulatedHandler) {
        Looper looper = Looper.myLooper() != null ? Looper.myLooper() : Looper.getMainLooper();
        handler = new Handler(looper);
        this.encapsulatedHandler = encapsulatedHandler;
    }

    @Override
    public void scannerConnectionProgress(final String providerKey, final String scannerKey, final String message) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                encapsulatedHandler.scannerConnectionProgress(providerKey, scannerKey, message);
            }
        });
    }

    @Override
    public void scannerCreated(final String providerKey, final String scannerKey, final Scanner s) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                encapsulatedHandler.scannerCreated(providerKey, scannerKey, s);
            }
        });
    }

    @Override
    public void noScannerAvailable() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                encapsulatedHandler.noScannerAvailable();
            }
        });
    }

    @Override
    public void endOfScannerSearch() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                encapsulatedHandler.endOfScannerSearch();
            }
        });
    }
}
