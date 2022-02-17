package com.enioka.scanner.helpers;

import android.os.Handler;
import android.os.Looper;

import com.enioka.scanner.api.Scanner;

/**
 * A helper to put all interactions with the caller on the UI thread.
 */
public class ScannerStatusCallbackProxy implements Scanner.ScannerStatusCallback {
    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private final Scanner.ScannerStatusCallback encapsulatedHandler;

    public ScannerStatusCallbackProxy(Scanner.ScannerStatusCallback encapsulatedHandler) {
        this.encapsulatedHandler = encapsulatedHandler;
    }

    @Override
    public void onStatusChanged(String newStatus) {
        uiHandler.post(() -> encapsulatedHandler.onStatusChanged(newStatus));
    }

    @Override
    public void onScannerReconnecting(Scanner s) {
        uiHandler.post(() -> encapsulatedHandler.onScannerReconnecting(s));
    }

    @Override
    public void onScannerDisconnected(Scanner s) {
        uiHandler.post(() -> encapsulatedHandler.onScannerDisconnected(s));
    }
}
