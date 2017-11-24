package com.enioka.scanner.camera;

import android.os.Process;
import android.text.TextUtils;
import android.util.Log;

import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Analyse frames with ZBar and set result. Listens on a queue. Stops when queue contains null.
 */
class FrameAnalyser implements Runnable {
    private static final String TAG = "BARCODE";

    private ImageScanner scanner;
    private FrameAnalyserManager parent;
    private BlockingQueue<FrameAnalysisContext> queue;
    private Semaphore end = new Semaphore(0);

    FrameAnalyser(BlockingQueue<FrameAnalysisContext> queue, FrameAnalyserManager parent) {
        this.parent = parent;
        this.queue = queue;

        Log.i(TAG, "Analyser is ready inside pool");
    }

    @Override
    public void run() {
        Log.i(TAG, "run is called " + Thread.currentThread().getId());
        android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        initScanner();

        boolean first = true;
        FrameAnalysisContext ctx = null;
        while (ctx == null || ctx.frame != null) {
            try {
                ctx = queue.take();
                first = false;
            } catch (InterruptedException e) {
                // Just stop.
                break;
            }
            if (ctx != null && ctx.frame != null) {
                onPreviewFrame(ctx);
            }
        }

        Log.i(TAG, "Frame analyser is closing");
        end.release();
    }

    private void initScanner() {
        // Barcode analyzer (properties: 0 = all symbologies, 256 = config, 3 = value)
        this.scanner = new ImageScanner();
        this.scanner.setConfig(0, 256, 0); // 256 =   ZBAR_CFG_X_DENSITY (disable vertical scanning)
        //this.scanner.setConfig(0, 257, 3); // 257 =  ZBAR_CFG_Y_DENSITY (skip 2 out of 3 lines)
        this.scanner.setConfig(0, 0, 0); //  0 = ZBAR_CFG_ENABLE (disable all symbologies)
        this.scanner.setConfig(Symbol.CODE128, 0, 1); //  0 = ZBAR_CFG_ENABLE (enable symbology 128)
    }

    void awaitTermination(int units, TimeUnit unit) throws InterruptedException {
        end.tryAcquire(units, unit);
    }

    private void onPreviewFrame(FrameAnalysisContext ctx) {
        long start = System.nanoTime();

        //Log.i(TAG, "New frame analysis");
        String symData;
        int symType;

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

        int croppedDataWidth, croppedDataHeight;

        // Rotate and crop.
        if (ctx.vertical) {
            // French (vertical) - crop & rotate
            byte[] barcode = new byte[(1 + realX3 - realX1) * (1 + realY3 - realY1)];

            int i = 0;
            for (int w = realY1; w <= realY3; w++) {
                for (int h = realX3 - 1; h >= realX1; h--) {
                    barcode[i++] = ctx.frame[h * dataWidth + w];
                }
            }

            //noinspection SuspiciousNameCombination
            croppedDataWidth = (1 + realX3 - realX1);
            croppedDataHeight = (1 + realY3 - realY1);
            ctx.frame = barcode;
        } else {
            // Italian (horizontal). No need to rotate - just crop.
            yRatio = (float) dataHeight / ctx.camViewMeasuredHeight;  // Photo pixels per preview surface pixel.
            xRatio = (float) dataWidth / ctx.camViewMeasuredWidth;

            realY1 = (int) (ctx.y1 * yRatio);
            realY3 = (int) (ctx.y3 * yRatio);
            realX1 = (int) (ctx.x1 * xRatio);
            realX3 = (int) (ctx.x3 * xRatio);

            croppedDataWidth = (1 + realX3 - realX1);
            croppedDataHeight = (1 + realY3 - realY1);

            byte[] barcode = new byte[croppedDataWidth * croppedDataHeight];
            int i = 0;
            for (int h = realY1; h <= realY3; h++) {
                for (int w = realX1; w <= realX3; w++) {
                    barcode[i++] = ctx.frame[h * dataWidth + w];
                }
            }

            ctx.frame = barcode;
        }

        // Analysis
        Image pic = new Image(croppedDataWidth, croppedDataHeight, "Y800");
        pic.setData(ctx.frame);

        //pic.setCrop(0, realY1, dataWidth, realY3 - realY1); // Left, top, width, height
        if (this.scanner.scanImage(pic) > 0) {
            // There is a result! Extract it.
            SymbolSet var15 = this.scanner.getResults();
            Iterator i$ = var15.iterator();

            Set<String> foundStrings = new HashSet<>();
            while (i$.hasNext()) {
                Symbol sym = (Symbol) i$.next();
                symData = sym.getData();
                symType = sym.getType();

                if (!TextUtils.isEmpty(symData) && !foundStrings.contains(symData)) {
                    foundStrings.add(symData);
                    parent.handleResult(symData, symType, ctx.frame);
                }
            }
        }
        parent.fpsCounter();
        Log.v(TAG, "Took ms: " + (System.nanoTime() - start) / 1000000);
    }
}
