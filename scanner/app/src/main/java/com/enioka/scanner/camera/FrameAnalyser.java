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
    private Set<Integer> symbologies = new HashSet<>(10);

    FrameAnalyser(BlockingQueue<FrameAnalysisContext> queue, FrameAnalyserManager parent) {
        this.parent = parent;
        this.queue = queue;

        Log.i(TAG, "Analyser is ready inside pool");
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
                    // Happens on resolution changes which result in an inconsistent context.
                }
            }
        }

        Log.i(TAG, "Frame analyser is closing");
        end.release();
    }

    private synchronized void initScanner() {
        // Barcode analyzer (properties: 0 = all symbologies, 256 = config, 3 = value)
        this.scanner = new ImageScanner();
        //this.scanner.setConfig(0, 256, 0); // 256 =   ZBAR_CFG_X_DENSITY (disable vertical scanning)
        //this.scanner.setConfig(0, 257, 3); // 257 =  ZBAR_CFG_Y_DENSITY (skip 2 out of 3 lines)
        this.scanner.setConfig(0, 0, 0); //  0 = ZBAR_CFG_ENABLE (disable all symbologies)

        // Enable select symbologies
        symbologies.add(Symbol.CODE128);
        for (int symbology : symbologies) {
            this.scanner.setConfig(symbology, 0, 1);
        }
    }

    /**
     * Default is simply CODE_128. Use the Symbol static fields to specify a symbology. Can be used before init end.
     *
     * @param s the ID of the symbology (ZBAR coding)
     */
    synchronized void addSymbology(int s) {
        symbologies.add(s);
        if (scanner != null) {
            this.scanner.setConfig(s, 0, 1);
        }
    }

    void awaitTermination(int units, TimeUnit unit) throws InterruptedException {
        end.tryAcquire(units, unit);
    }

    private void onPreviewFrame(FrameAnalysisContext ctx) {
        long start = System.nanoTime();

        //Log.i(TAG, "New frame analysis");
        String symData;
        int symType;
        long lumaSum = 0L;

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
        byte[] barcode;

        // Rotate and crop the scan area. (only keep Y in the YUV image)
        if (ctx.vertical) {
            // French (vertical) - crop & rotate
            barcode = new byte[(1 + realX3 - realX1) * (1 + realY3 - realY1)];

            int i = 0;
            for (int w = realY1; w <= realY3; w++) {
                for (int h = realX3 - 1; h >= realX1; h--) {
                    barcode[i++] = ctx.frame[h * dataWidth + w];
                    lumaSum += barcode[i - 1] & 0xff;
                }
            }

            //noinspection SuspiciousNameCombination
            croppedDataWidth = (1 + realX3 - realX1);
            croppedDataHeight = (1 + realY3 - realY1);
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

            barcode = new byte[croppedDataWidth * croppedDataHeight];
            int i = 0;
            for (int h = realY1; h <= realY3; h++) {
                for (int w = realX1; w <= realX3; w++) {
                    barcode[i++] = ctx.frame[h * dataWidth + w];
                    lumaSum += barcode[i - 1] & 0xff;
                }
            }
        }

        // Analysis
        Image pic = new Image(croppedDataWidth, croppedDataHeight, "Y800");
        pic.setData(barcode);

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
        int luma = (int) ((double) lumaSum / (croppedDataWidth * croppedDataHeight));
        parent.fpsCounter(luma);
        parent.endOfFrame(ctx);
        Log.v(TAG, "Took ms: " + (System.nanoTime() - start) / 1000000);
    }
}
