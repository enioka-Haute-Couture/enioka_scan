package com.enioka.scanner.api;

import android.app.Activity;

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
    void initialize(Activity ctx, ScannerInitCallback initCallback, ScannerDataCallback dataCallback, ScannerStatusCallback statusCallback, Mode mode);

}
