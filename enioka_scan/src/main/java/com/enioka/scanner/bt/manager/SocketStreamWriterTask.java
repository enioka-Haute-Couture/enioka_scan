package com.enioka.scanner.bt.manager;

import android.util.Log;

import com.enioka.scanner.bt.api.Helpers;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Actual socket writing. Used in executor.
 */
class SocketStreamWriterTask implements Runnable {
    private static final String LOG_TAG = "BtSppSdk";

    private final OutputStream outputStream;
    private final byte[] buffer;
    private final int offset;
    private final int length;
    private final boolean ackType;
    private final SocketStreamWriter parent;

    SocketStreamWriterTask(OutputStream outputStream, byte[] buffer, int offset, int length, SocketStreamWriter writer, boolean ackType) {
        this.outputStream = outputStream;
        this.buffer = buffer;
        this.length = length;
        this.offset = offset;
        this.parent = writer;
        this.ackType = ackType;
    }

    @Override
    public void run() {
        try {
            if (!this.ackType) {
                this.parent.waitForCommandAllowed();
            }
            Log.d(LOG_TAG, "writing " + this.length + " bytes on output stream: " + Helpers.byteArrayToHex(this.buffer, length));
            this.outputStream.write(buffer, offset, length);
            this.outputStream.flush();
            Log.d(LOG_TAG, "writing done");
        } catch (IOException e) {
            Log.d(LOG_TAG, "Output stream failure", e);
        } catch (InterruptedException e) {
            Log.d(LOG_TAG, "A write was abandoned before starting as thread was interrupted", e);
        }
    }
}
