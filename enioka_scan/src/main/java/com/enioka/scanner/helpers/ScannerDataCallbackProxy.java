package com.enioka.scanner.helpers;

import android.os.Handler;
import android.os.Looper;

import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.data.Barcode;

import java.util.List;

/**
 * A helper to put all interactions with the caller on the UI thread.
 */
public class ScannerDataCallbackProxy implements Scanner.ScannerDataCallback {
    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private final Scanner.ScannerDataCallback encapsulatedHandler;

    public ScannerDataCallbackProxy(Scanner.ScannerDataCallback encapsulatedHandler) {
        this.encapsulatedHandler = encapsulatedHandler;
    }

    @Override
    public void onData(final Scanner s, final List<Barcode> data) {
        uiHandler.post(() -> encapsulatedHandler.onData(s, data));
    }
}
