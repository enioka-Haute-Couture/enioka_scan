package com.enioka.scanner.bt.manager.classicserial;

import android.util.Log;

import com.enioka.scanner.bt.api.Helpers;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * Actual socket writing. Used in executor.
 */
class ClassicBtSocketStreamWriterTask implements Runnable {
    private static final String LOG_TAG = "BtSppSdk";

    private final OutputStream outputStream;
    private final byte[] buffer;
    private final int offset;
    private final int length;
    private final boolean ackType;
    private final ClassicBtSocketStreamWriter parent;

    ClassicBtSocketStreamWriterTask(OutputStream outputStream, byte[] buffer, int offset, int length, ClassicBtSocketStreamWriter writer, boolean ackType) {
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
            Log.d(LOG_TAG, "writing " + this.length + " bytes on output stream: " + Helpers.byteArrayToHex(this.buffer, length) + "  ASCII: " + new String(buffer, 0, this.length, Charset.forName("ASCII")));
            this.outputStream.write(buffer, offset, length);
            this.outputStream.flush();
            Log.d(LOG_TAG, "writing done");
        } catch (IOException e) {
            Log.d(LOG_TAG, "Output stream failure", e);
            this.parent.onConnectionFailure();
        } catch (InterruptedException e) {
            Log.d(LOG_TAG, "A write was abandoned before starting as thread was interrupted", e);
        }
    }
}
