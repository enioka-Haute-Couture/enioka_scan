package com.enioka.scanner.bt;

public interface BtDeviceInputHandler {
    /**
     * Do something with the data returned by the bluetooth device. Not called on the UI thread.
     *
     * @param buffer     byte array containing the data from 0 to dataLength
     * @param dataLength number of useful bytes inside the buffer
     */
    void process(byte[] buffer, int offset, int dataLength);

    /**
     * Called when the data has finished reading (terminator reached).
     */
    void endOfTransmission();
}
