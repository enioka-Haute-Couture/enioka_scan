package com.enioka.scanner.camera;

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
        android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        initScanner();

        FrameAnalysisContext ctx = null;
        while (ctx == null || ctx.frame != null) {
            try {
                ctx = queue.take();
            } catch (InterruptedException e) {
                // Just stop.
                break;
            }
            if (ctx != null && ctx.frame != null) {
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

    static class BarcodeRectangleData {
        byte[] barcode;
        int croppedDataWidth, croppedDataHeight;
        long lumaSum = 0L;
    }

    BarcodeRectangleData extractBarcodeRectangle(FrameAnalysisContext ctx) {
        BarcodeRectangleData res = new BarcodeRectangleData();

        // Data characteristics
        int dataWidth = (int) ctx.cameraWidth;
        int dataHeight = (int) ctx.cameraHeight;

        // The rectangle is in view coordinates.
        float yRatio = (float) dataWidth / ctx.camViewMeasuredHeight;  // Photo pixels per preview surface pixel. Width because: 90° rotated.
        float xRatio = (float) dataHeight / ctx.camViewMeasuredWidth;
        int realY1 = (int) (ctx.y1 * yRatio);
        int realY3 = (int) (ctx.y3 * yRatio);
        int realX1 = (int) (ctx.x1 * xRatio);
        int realX3 = (int) (ctx.x3 * xRatio);

        // Rotate and crop the scan area. (only keep Y in the YUV image)
        if (ctx.vertical) {
            // French (vertical) - crop & rotate
            res.barcode = new byte[(1 + realX3 - realX1) * (1 + realY3 - realY1)];

            int i = 0;
            for (int w = realY1; w <= realY3; w++) {
                for (int h = realX3 - 1; h >= realX1; h--) {
                    res.barcode[i++] = ctx.frame[h * dataWidth + w];
                    res.lumaSum += res.barcode[i - 1] & 0xff;
                }
            }

            res.croppedDataWidth = (1 + realX3 - realX1);
            res.croppedDataHeight = (1 + realY3 - realY1);
        } else {
            // Italian (horizontal). No need to rotate - just crop.
            yRatio = (float) dataHeight / ctx.camViewMeasuredHeight;  // Photo pixels per preview surface pixel.
            xRatio = (float) dataWidth / ctx.camViewMeasuredWidth;

            realY1 = (int) (ctx.y1 * yRatio);
            realY3 = (int) (ctx.y3 * yRatio);
            realX1 = (int) (ctx.x1 * xRatio);
            realX3 = (int) (ctx.x3 * xRatio);

            res.croppedDataWidth = (1 + realX3 - realX1);
            res.croppedDataHeight = (1 + realY3 - realY1);

            res.barcode = new byte[res.croppedDataWidth * res.croppedDataHeight];
            int i = 0;
            for (int h = realY1; h <= realY3; h++) {
                for (int w = realX1; w <= realX3; w++) {
                    res.barcode[i++] = ctx.frame[h * dataWidth + w];
                    res.lumaSum += res.barcode[i - 1] & 0xff;
                }
            }
        }

        return res;
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
