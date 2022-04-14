package com.enioka.scanner.api.proxies;

import android.os.Handler;
import android.os.Looper;

import com.enioka.scanner.api.callbacks.ProviderDiscoveredCallback;

public class ProviderDiscoveredCallbackProxy implements ProviderDiscoveredCallback {
    protected Handler uiHandler;
    protected ProviderDiscoveredCallback wrappedHandler;

    public ProviderDiscoveredCallbackProxy(final ProviderDiscoveredCallback wrappedHandler) {
        this.uiHandler = new Handler(Looper.getMainLooper());
        this.wrappedHandler = wrappedHandler;
    }

    @Override
    public void onDiscoveryDone() {
        uiHandler.post(wrappedHandler::onDiscoveryDone);
    }
}
