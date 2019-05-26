package com.enioka.scanner.api;

import android.app.Activity;
import android.content.Context;

/**
 * A Scanner which does not need anything from the foreground.
 */
public interface ScannerBackground extends Scanner {
    /**
     * Called once per application launch.
     */
    void initialize(Context applicationContext, ScannerInitCallback initCallback, ScannerDataCallback dataCallback, ScannerStatusCallback statusCallback, Mode mode);

}
