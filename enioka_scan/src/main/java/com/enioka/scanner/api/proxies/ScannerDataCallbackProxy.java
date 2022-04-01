package com.enioka.scanner.api.proxies;

import android.os.Handler;
import android.os.Looper;

import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.api.callbacks.ScannerDataCallback;
import com.enioka.scanner.data.Barcode;

import java.util.List;

public class ScannerDataCallbackProxy implements ScannerDataCallback {
    protected Handler uiHandler;
    protected ScannerDataCallback wrappedHandler;

    public ScannerDataCallbackProxy(final ScannerDataCallback wrappedHandler) {
        this.uiHandler = new Handler(Looper.getMainLooper());
        this.wrappedHandler = wrappedHandler;
    }

    @Override
    public void onData(final Scanner s, final List<Barcode> data) {
        uiHandler.post(() -> wrappedHandler.onData(s, data));
    }
}
