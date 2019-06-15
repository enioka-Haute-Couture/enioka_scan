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
     * @param cb1 a callback to call when data is read.
     */
    void initialize(Activity ctx, ScannerInitCallback cb0, ScannerDataCallback cb1, ScannerStatusCallback cb2, Mode mode);

}
