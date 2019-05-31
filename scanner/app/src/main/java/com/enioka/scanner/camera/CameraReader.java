package com.enioka.scanner.camera;

/**
 * The different barcode reading libraries available.
 */
public enum CameraReader {
    /**
     * Use ZBar. It is by far the fastest reader available and is the default.
     */
    ZBAR,
    /**
     * Use Zebra Crossing (ZXing), a slow but more accurate reader.
     */
    ZXING
}
