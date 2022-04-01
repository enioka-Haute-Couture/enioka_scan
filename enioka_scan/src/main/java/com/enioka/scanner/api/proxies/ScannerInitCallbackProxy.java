package com.enioka.scanner.api.proxies;

import android.os.Handler;
import android.os.Looper;

import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.api.callbacks.ScannerInitCallback;

public class ScannerInitCallbackProxy implements ScannerInitCallback {
    protected final Handler uiHandler;
    protected final ScannerInitCallback wrappedHandler;

    public ScannerInitCallbackProxy(ScannerInitCallback wrappedHandler) {
        this.uiHandler = new Handler(Looper.getMainLooper());
        this.wrappedHandler = wrappedHandler;
    }

    @Override
    public void onConnectionSuccessful(final Scanner s) {
        uiHandler.post(() -> wrappedHandler.onConnectionSuccessful(s));
    }

    @Override
    public void onConnectionFailure(final Scanner s) {
        uiHandler.post(() -> wrappedHandler.onConnectionFailure(s));
    }
}
