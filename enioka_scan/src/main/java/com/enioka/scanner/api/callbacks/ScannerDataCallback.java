package com.enioka.scanner.api.callbacks;

import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.data.Barcode;

import java.util.List;

/**
 * Callback to deal with data read by the scanner.
 */
public interface ScannerDataCallback {

    /**
     * Called whenever data is scanned by the scanner.
     * @param s The scanner that read the data.
     * @param data The data read by the reader.
     */
    void onData(final Scanner s, final List<Barcode> data);
}
