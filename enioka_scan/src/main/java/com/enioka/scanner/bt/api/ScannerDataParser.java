package com.enioka.scanner.bt.api;

/**
 * The interface through which data from the scanner is poured into the device-specific adapter.
 */
public interface ScannerDataParser {
    /**
     * Do something with the data returned by the bluetooth device. Not called on the UI thread.
     *
     * @param buffer     byte array containing the data from 0 to dataLength
     * @param dataLength number of useful bytes inside the buffer
     */
    ParsingResult parse(byte[] buffer, int offset, int dataLength);
}
