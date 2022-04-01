package com.enioka.scanner.api;

import android.content.Context;

import com.enioka.scanner.api.callbacks.ScannerStatusCallback;
import com.enioka.scanner.api.proxies.ScannerDataCallbackProxy;
import com.enioka.scanner.api.proxies.ScannerInitCallbackProxy;
import com.enioka.scanner.api.proxies.ScannerStatusCallbackProxy;

/**
 * A Scanner which does not need anything from the foreground.
 */
public interface ScannerBackground extends Scanner {
    /**
     * Called once per application launch.
     */
    void initialize(final Context applicationContext, final ScannerInitCallbackProxy initCallback, final ScannerDataCallbackProxy dataCallback, final ScannerStatusCallbackProxy statusCallback, final Mode mode);
}
