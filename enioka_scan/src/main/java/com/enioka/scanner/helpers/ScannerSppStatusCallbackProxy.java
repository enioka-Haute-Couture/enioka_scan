package com.enioka.scanner.helpers;

import android.os.Handler;
import android.os.Looper;

import com.enioka.scanner.bt.api.Scanner;

/**
 * A helper to put all interactions with the caller on the UI thread.
 */
public class ScannerSppStatusCallbackProxy implements  Scanner.SppScannerStatusCallback {
    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private final Scanner.SppScannerStatusCallback encapsulatedHandler;

    public ScannerSppStatusCallbackProxy(Scanner.SppScannerStatusCallback encapsulatedHandler) {
        this.encapsulatedHandler = encapsulatedHandler;
    }

    @Override
    public void onScannerConnected() {
        uiHandler.post(encapsulatedHandler::onScannerConnected);
    }

    @Override
    public void onScannerReconnecting() {
        uiHandler.post(encapsulatedHandler::onScannerReconnecting);
    }

    @Override
    public void onScannerDisconnected() {
        uiHandler.post(encapsulatedHandler::onScannerDisconnected);
    }
}
