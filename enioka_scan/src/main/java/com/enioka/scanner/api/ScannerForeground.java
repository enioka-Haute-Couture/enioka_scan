package com.enioka.scanner.api;

import android.app.Activity;

import com.enioka.scanner.helpers.ScannerDataCallbackProxy;
import com.enioka.scanner.helpers.ScannerInitCallbackProxy;
import com.enioka.scanner.helpers.ScannerStatusCallbackProxy;

/**
 * A Scanner which can only work in relation with an Activity.
 */
public interface ScannerForeground extends Scanner {
    /**
     * Called once per application launch.
     *
     * @param ctx The application
     * @param dataCallback a callback to call when data is read.
     */
    void initialize(Activity ctx, ScannerInitCallbackProxy initCallback, ScannerDataCallbackProxy dataCallback, ScannerStatusCallbackProxy statusCallback, Mode mode);

}
