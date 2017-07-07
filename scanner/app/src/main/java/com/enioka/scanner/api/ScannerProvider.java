package com.enioka.scanner.api;

import android.content.Context;

import com.enioka.scanner.api.Scanner;

/**
 * Methods to implement to be able to provide a Scanner instance.
 */
public interface ScannerProvider {
    /**
     * Return a new Scanner if the device is compatible with the type of scanners handled by this provider. Null otherwise.<br>
     * Must be callable from any device, even if not compatible - so beware of library loading (you may want to use reflection inside this method).
     */
    Scanner getScanner(Context ctx);
}
