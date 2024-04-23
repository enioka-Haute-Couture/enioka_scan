package com.enioka.scanner.api.proxies;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.Nullable;

import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.api.callbacks.ScannerStatusCallback;

public class ScannerStatusCallbackProxy implements ScannerStatusCallback {
    protected final Handler uiHandler;
    protected final ScannerStatusCallback wrappedHandler;

    public ScannerStatusCallbackProxy(final ScannerStatusCallback wrappedHandler) {
        this.uiHandler = new Handler(Looper.getMainLooper());
        this.wrappedHandler = wrappedHandler;
    }

    @Override
    public void onStatusChanged(@Nullable final Scanner scanner, final Status newStatus) {
        uiHandler.post(() -> wrappedHandler.onStatusChanged(scanner, newStatus));
    }
}
