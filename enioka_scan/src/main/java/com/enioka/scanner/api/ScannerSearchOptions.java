package com.enioka.scanner.api;

/**
 * A property bag tweaking the scanner search method. Some providers may ignore these options.
 */
public class ScannerSearchOptions {
    /**
     * If a scanner is known but not available, wait for it. If false, consider the scanner unavailable immediately.
     */
    public boolean waitDisconnected = true;

    /**
     * If true, will only return the first scanner available (or reporting it may become available if {@link #waitDisconnected} is true). If false, all scanners available are returned.
     */
    public boolean returnOnlyFirst = true;

    /**
     * If true (the default) will try to prevent a screen shutdown. If false, system or activity defaults are used.
     */
    public boolean keepScreenOn = true;

    /**
     * If true, bluetooth devices will be searched for scanners.
     */
    public boolean useBlueTooth = false;

    public static ScannerSearchOptions defaultOptions() {
        return new ScannerSearchOptions();
    }

    public ScannerSearchOptions keepScreenOn() {
        keepScreenOn = true;
        return this;
    }

    public ScannerSearchOptions keepScreenOn(boolean b) {
        keepScreenOn = b;
        return this;
    }

    public ScannerSearchOptions getAllAvailableScanners() {
        returnOnlyFirst = false;
        return this;
    }
}
