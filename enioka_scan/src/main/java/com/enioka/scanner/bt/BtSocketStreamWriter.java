package com.enioka.scanner.bt;

import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * As some write operations can be blocking, we use a dedicated thread. There is a single write thread running at a given time.
 */
class BtSocketStreamWriter {
    private static final String LOG_TAG = "InternalBtDevice";

    private final OutputStream outputStream;
    private final ExecutorService pool;

    private Semaphore commandAllowed = new Semaphore(1);

    BtSocketStreamWriter(OutputStream outputStream) {
        this.outputStream = outputStream;
        this.pool = Executors.newFixedThreadPool(1);
    }

    synchronized void write(byte[] buffer, int offset, int length, boolean ackType) {
        pool.submit(new BtSocketStreamWriterTask(this.outputStream, buffer, offset, length, this, ackType));
    }

    void write(byte[] buffer, int offset, int length) {
        write(buffer, offset, length, false);
    }

    void write(byte[] buffer) {
        write(buffer, false);
    }

    void write(byte[] buffer, boolean ackType) {
        write(buffer, 0, buffer.length, ackType);
    }

    void write(int data) {
        write(ByteBuffer.allocate(4).putInt(data).array());
    }

    void write(String data) {

        write(data.getBytes(Charset.forName("ASCII")));
    }

    void endOfCommand() {
        this.commandAllowed.release();
    }

    void waitForCommandAllowed() throws InterruptedException {
        this.commandAllowed.acquire();
    }
}
