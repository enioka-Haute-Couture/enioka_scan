package com.enioka.scanner.bt;

import android.util.Log;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public class BtSocketStreamReader extends Thread implements Closeable {
    private static final String LOG_TAG = "InternalBtDevice";

    private final InputStream inputStream;
    private final BtDeviceInputHandler defaultInputHandler;
    private byte[] buffer;

    BtSocketStreamReader(InputStream inputStream, BtDeviceInputHandler defaultInputHandler) {
        this.inputStream = inputStream;
        this.defaultInputHandler = defaultInputHandler;
    }

    @Override
    public void run() {
        buffer = new byte[1024];
        int byteCount;

        Log.i(LOG_TAG, "Socket input stream is connected");

        // Keep listening to the InputStream until an exception occurs.
        while (true) {
            try {
                byteCount = this.inputStream.read(buffer);
                Log.d(LOG_TAG, "Read bytes count " + byteCount);
                this.defaultInputHandler.process(buffer, 0, byteCount);
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
