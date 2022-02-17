package com.enioka.scanner.helpers;

import android.os.Handler;
import android.os.Looper;

import com.enioka.scanner.api.Scanner;

/**
 * A helper to put all interactions with the caller on the UI thread.
 */
public class ScannerInitCallbackProxy implements Scanner.ScannerInitCallback{
    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private final Scanner.ScannerInitCallback encapsulatedHandler;

    public ScannerInitCallbackProxy(Scanner.ScannerInitCallback encapsulatedHandler) {
        this.encapsulatedHandler = encapsulatedHandler;
    }

    @Override
    public void onConnectionSuccessful(Scanner s) {
        uiHandler.post(() -> encapsulatedHandler.onConnectionSuccessful(s));
    }

    @Override
    public void onConnectionFailure(Scanner s) {
        uiHandler.post(() -> encapsulatedHandler.onConnectionFailure(s));
    }
}
