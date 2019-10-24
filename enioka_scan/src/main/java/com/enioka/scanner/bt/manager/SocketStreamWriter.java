package com.enioka.scanner.bt.manager;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * As some write operations can be blocking, we use a dedicated thread. There is a single write thread running at a given time.
 */
class SocketStreamWriter implements Closeable {
    private static final String LOG_TAG = "BtSppSdk";

    private final BtSppScanner device;
    private final OutputStream outputStream;
    private final ExecutorService pool;

    private Semaphore commandAllowed = new Semaphore(1);

    SocketStreamWriter(OutputStream outputStream, BtSppScanner device) {
        this.outputStream = outputStream;
        this.device = device;
        this.pool = Executors.newFixedThreadPool(1);
    }

    synchronized void write(byte[] buffer, int offset, int length, boolean ackType) {
        pool.submit(new SocketStreamWriterTask(this.outputStream, buffer, offset, length, this, ackType));
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

    void onConnectionFailure() {
        this.device.onConnectionFailure();
    }

    @Override
    public void close() throws IOException {
        if (pool != null) {
            pool.shutdown();
            try {
                pool.awaitTermination(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (outputStream != null) {
            outputStream.close();
        }
    }
}
