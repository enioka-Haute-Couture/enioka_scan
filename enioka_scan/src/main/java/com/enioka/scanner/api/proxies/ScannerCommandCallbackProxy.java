package com.enioka.scanner.api.proxies;

import android.os.Handler;
import android.os.Looper;

import com.enioka.scanner.api.callbacks.ScannerCommandCallback;

public class ScannerCommandCallbackProxy implements ScannerCommandCallback {
    protected Handler uiHandler;
    protected ScannerCommandCallback wrappedHandler;

    public ScannerCommandCallbackProxy(ScannerCommandCallback wrappedHandler) {
        this.uiHandler = new Handler(Looper.getMainLooper());
        this.wrappedHandler = wrappedHandler;
    }

    @Override
    public void onSuccess() {
        uiHandler.post(wrappedHandler::onSuccess);
    }

    @Override
    public void onFailure() {
        uiHandler.post(wrappedHandler::onFailure);
    }

    @Override
    public void onTimeout() {
        uiHandler.post(wrappedHandler::onTimeout);
    }
}
