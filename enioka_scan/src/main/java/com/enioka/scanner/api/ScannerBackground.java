package com.enioka.scanner.api;

import android.content.Context;

import com.enioka.scanner.helpers.ScannerDataCallbackProxy;
import com.enioka.scanner.helpers.ScannerInitCallbackProxy;
import com.enioka.scanner.helpers.ScannerStatusCallbackProxy;

/**
 * A Scanner which does not need anything from the foreground.
 */
public interface ScannerBackground extends Scanner {
    /**
     * Called once per application launch.
     */
    void initialize(Context applicationContext, ScannerInitCallbackProxy initCallback, ScannerDataCallbackProxy dataCallback, ScannerStatusCallbackProxy statusCallback, Mode mode);

}
