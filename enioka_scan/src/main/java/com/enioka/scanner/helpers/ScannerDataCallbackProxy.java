package com.enioka.scanner.helpers;

import android.os.Handler;
import android.os.Looper;

import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.api.ScannerConnectionHandler;
import com.enioka.scanner.data.Barcode;

import java.util.List;

/**
 * A helper to put all interactions with the caller on the UI thread.
 */
public class ScannerDataCallbackProxy implements Scanner.ScannerDataCallback {
    private final Handler handler;
    private final Scanner.ScannerDataCallback encapsulatedHandler;

    public ScannerDataCallbackProxy(Scanner.ScannerDataCallback encapsulatedHandler) {
        Looper looper = Looper.myLooper() != null ? Looper.myLooper() : Looper.getMainLooper();
        handler = new Handler(looper);
        this.encapsulatedHandler = encapsulatedHandler;
    }

    @Override
    public void onData(final Scanner s, final List<Barcode> data) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                encapsulatedHandler.onData(s, data);
            }
        });
    }
}
