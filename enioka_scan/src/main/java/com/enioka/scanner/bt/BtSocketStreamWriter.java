package com.enioka.scanner.bt;

import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * As some write operations can be blocking, we use a dedicated thread. There is a single write thread running at a given time.
 */
public class BtSocketStreamWriter {
    private static final String LOG_TAG = "InternalBtDevice";

    private final OutputStream outputStream;
    private final ExecutorService pool;

    private Semaphore commandAllowed = new Semaphore(1);

    BtSocketStreamWriter(OutputStream outputStream) {
        this.outputStream = outputStream;
        this.pool = Executors.newFixedThreadPool(1);
    }

    public synchronized void write(byte[] buffer, int offset, int length) {
        pool.submit(new BtSocketStreamWriterTask(this.outputStream, buffer, offset, length, this));
    }

    public void write(byte[] buffer) {
        write(buffer, 0, buffer.length);
    }

    public void write(int data) {
        write(ByteBuffer.allocate(4).putInt(data).array());
    }

    public void write(String data) {

        write(data.getBytes(Charset.forName("ASCII")));
    }

    void endOfCommand() {
        this.commandAllowed.release();
    }

    void waitForCommandAllowed() throws InterruptedException {
        this.commandAllowed.acquire();
    }
}
