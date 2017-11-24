package com.enioka.scanner.camera;

import android.util.Log;

import java.util.ArrayDeque;
import java.util.Calendar;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static com.enioka.scanner.camera.ZbarScanView.beepOk;

/**
 * Holder for analyser thread pool.
 */
class FrameAnalyserManager {
    private static int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors(); // Wrong on old devices, not an issue for us.
    private static final String TAG = "BARCODE";

    // Success callback
    private ZbarScanView parent;

    // FPS counter variables
    private long latestIntervalStart = 0;
    private long latestLog = 0;
    private int countLatestSecond = 0;

    // Deduplication variables
    private Calendar latestResultTime = Calendar.getInstance();
    private String latestBarcodeRead = "";
    private static final int DOUBLE_READ_THRESHOLD_MS = 0;

    // Analyser pool
    private Queue<FrameAnalyser> analysers = new ArrayDeque<>(NUMBER_OF_CORES);
    private BlockingQueue<FrameAnalysisContext> queue = new ArrayBlockingQueue<>(25, true); // about 1 second in perfect conditions

    FrameAnalyserManager(ZbarScanView parent) {
        this.parent = parent;

        for (int i = 0; i < NUMBER_OF_CORES; i++) {
            FrameAnalyser frameAnalyser = new FrameAnalyser(queue, this);
            new Thread(frameAnalyser).start();
            analysers.add(frameAnalyser);
        }

        Log.i(TAG, "Analyser pool initialized with " + NUMBER_OF_CORES + " threads");
    }

    void handleFrame(FrameAnalysisContext ctx) {
        queue.offer(ctx);
    }

    void close() {
        for (int i = 0; i < analysers.size(); i++) {
            queue.offer(new FrameAnalysisContext());
        }
        for (FrameAnalyser frameAnalyser : analysers) {
            try {
                frameAnalyser.awaitTermination(2, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                // nothing to do - dying anyway.
            }
        }
        analysers.clear();
        queue.clear();
    }

    // Note this method is not synchronised as it is called on each frame and this would be costly.
    // So the FPS indication may be slightly off, which is not an issue as it is only an indication.
    void fpsCounter() {
        long currentTime = System.nanoTime();
        countLatestSecond++;
        if (latestLog > latestIntervalStart + 1000000000) {
            Log.d(TAG, "FPS: " + 1000000000 * (float) countLatestSecond / (currentTime - latestIntervalStart) + " - Pool size: " + analysers.size());
            latestIntervalStart = currentTime;
            countLatestSecond = 0;
        }
        latestLog = currentTime + 1;
    }

    /**
     * Called after each analysis, with null value if nothing found.
     */
    synchronized void handleResult(String result, int symType, byte[] imagePreview) {
        if (result == null) {
            return;
        }

        if (latestBarcodeRead.equals(result) && Calendar.getInstance().getTimeInMillis() < latestResultTime.getTimeInMillis() + DOUBLE_READ_THRESHOLD_MS) {
            // Ignore this result, its the same barcode that was just read.
            return;
        }
        latestBarcodeRead = result;
        latestResultTime = Calendar.getInstance();

        beepOk();
        parent.analyserCallback(result, symType, imagePreview);
    }
}
