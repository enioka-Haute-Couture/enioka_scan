package com.enioka.scanner.bt;

import android.util.Log;

import java.nio.charset.Charset;

/**
 * A debugging helper - it displays all received data in the log.
 */
public class LoggingInputHandler implements BtDeviceInputHandler {
    private static final String LOG_TAG = "InternalBtDevice";

    @Override
    public void process(byte[] buffer, int offset, int dataLength) {
        Log.i(LOG_TAG, "received data! ASCII: " + new String(buffer, 0, dataLength, Charset.forName("ASCII")).trim() + " - HEX: " + byteArrayToHex(buffer, dataLength));
    }

    @Override
    public void endOfTransmission() {
        // Do not care , we just log on the fly.
    }

    private String byteArrayToHex(byte[] buffer, int length) {
        StringBuilder sb = new StringBuilder(length * 2);
        for (int i = 0; i < length; i++) {
            sb.append(String.format("%02x ", buffer[i]));
        }
        return sb.toString();
    }
}
