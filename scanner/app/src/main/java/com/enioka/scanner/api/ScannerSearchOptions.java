package com.enioka.scanner.api;

/**
 * A property bag tweaking the scanner search method. Some providers may ignore the values provided.
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
}
