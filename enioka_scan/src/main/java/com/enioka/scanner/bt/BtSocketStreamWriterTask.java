package com.enioka.scanner.bt;

import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * Actual socket writing. Used in executor.
 */
public class BtSocketStreamWriterTask implements Runnable {
    private static final String LOG_TAG = "InternalBtDevice";

    private final OutputStream outputStream;
    private final byte[] buffer;
    private final int offset;
    private final int length;

    BtSocketStreamWriterTask(OutputStream outputStream, byte[] buffer, int offset, int length) throws IOException {
        this.outputStream = outputStream;
        this.buffer = buffer;
        this.length = length;
        this.offset = offset;
    }

    @Override
    public void run() {
        try {
            Log.d(LOG_TAG, "writing " + this.length + " bytes on output stream: " + LoggingInputHandler.byteArrayToHex(this.buffer, length));
            this.outputStream.write(buffer, offset, length);
            this.outputStream.flush();
            Log.d(LOG_TAG, "writing done");
        } catch (IOException e) {
            Log.d(LOG_TAG, "Output stream failure", e);
        }
    }
}
