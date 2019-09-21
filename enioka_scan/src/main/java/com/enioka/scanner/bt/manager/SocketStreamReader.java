package com.enioka.scanner.bt.manager;

import android.util.Log;

import com.enioka.scanner.bt.api.Helpers;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * A thread dedicated to listening data incoming from a socket.
 */
class SocketStreamReader extends Thread implements Closeable {
    private static final String LOG_TAG = "BtSppSdk";

    private final BtSppScanner device;
    private final InputStream inputStream;

    SocketStreamReader(InputStream inputStream, BtSppScanner device) {
        this.inputStream = inputStream;
        this.device = device;
    }

    @Override
    public void run() {
        byte[] buffer = new byte[1024];
        int byteCount;

        Log.i(LOG_TAG, "Socket input stream is connected");

        // Keep listening to the InputStream until an exception occurs.
        while (true) {
            try {
                byteCount = this.inputStream.read(buffer);
                if (byteCount > 0) {
                    Log.d(LOG_TAG, "Read " + byteCount + " bytes from device: " + Helpers.byteArrayToHex(buffer, byteCount) + " - ASCII: " + new String(buffer, 0, byteCount, Charset.forName("ASCII")));
                    this.device.handleInputBuffer(buffer, 0, byteCount);
                }
            } catch (IOException e) {
                Log.d(LOG_TAG, "Input stream was disconnected", e);
                break;
            }
        }
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }
}
