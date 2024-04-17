package com.enioka.scanner.sdk.camera;

import android.os.Process;
import android.util.Log;

import com.enioka.scanner.data.BarcodeType;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * The actual encapsulation of an image barcode analyser. This base class provides a few common methods to help analyser integration.
 */
abstract class FrameAnalyser implements Runnable {
    protected static final String TAG = "BARCODE";

    BlockingQueue<FrameAnalysisContext> queue;
    private final Semaphore end = new Semaphore(0);

    protected final FrameAnalyserManager parent;

    protected FrameAnalyser(FrameAnalyserManager parent) {
        this.parent = parent;
    }

    @Override
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        initScanner();

        FrameAnalysisContext ctx = null;
        while (ctx == null || ctx.croppedPicture != null) {
            try {
                ctx = queue.take();
            } catch (InterruptedException e) {
                // Just stop.
                break;
            }
            if (ctx != null && ctx.croppedPicture != null) {
                try {
                    onPreviewFrame(ctx);
                } catch (Exception e) {
                    Log.w(TAG, "Analysis transient failure - buffer is ignored", e);
                    // Happens on resolution changes which result in an inconsistent context.
                } finally {
                    parent.endOfFrame(ctx);
                }
            }
        }

        Log.i(TAG, "Frame analyser is closing");
        end.release();
    }

    /**
     * Wait for the analyzer to die gracefully.
     *
     * @param units time to wait
     * @param unit  unit of time
     * @return false if ended in timeout, true otherwise
     * @throws InterruptedException if wait was interrupted
     */
    boolean awaitTermination(int units, TimeUnit unit) throws InterruptedException {
        return end.tryAcquire(units, unit);
    }

    protected abstract void onPreviewFrame(FrameAnalysisContext ctx);

    abstract void initScanner();

    /**
     * Default is simply CODE_128. Can be used before init end.
     *
     * @param barcodeType symbology to add
     */
    abstract void addSymbology(BarcodeType barcodeType);
}
