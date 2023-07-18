package com.enioka.scanner.camera;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Environment;
import android.os.Process;
import android.util.Log;

import com.enioka.scanner.data.BarcodeType;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
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

    /**
     * Debug method to show more easily what is analysed
     *
     * @param ctx
     */
    @SuppressWarnings("unused") // debug method to be enabled when needed.
    protected synchronized static void storeBufferAsPng(FrameAnalysisContext ctx, String name) {
        File sdCardFile = new File(Environment.getExternalStorageDirectory() + "/" + name);

        // Recreate NV21 format (YUV) in black. We only have the luminance, we are missing U and V at the end of the buffer.
        //byte[] buffer = ctx.croppedPicture.barcode;
        byte[] buffer = Arrays.copyOf(ctx.croppedPicture.barcode, (int) (ctx.croppedPicture.barcode.length * 1.5));
        //Arrays.fill(buffer, ctx.croppedPicture.barcode.length, (int) (ctx.croppedPicture.barcode.length * 1.5) - 1, (byte) 0x00);

        YuvImage i = new YuvImage(buffer, ImageFormat.NV21, ctx.croppedPicture.croppedDataWidth, ctx.croppedPicture.croppedDataHeight, null);

        OutputStream s = null;
        try {
            s = new FileOutputStream(sdCardFile, false);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        i.compressToJpeg(new Rect(0, 0, ctx.croppedPicture.croppedDataWidth, ctx.croppedPicture.croppedDataHeight), 100, s);
        try {
            s.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
