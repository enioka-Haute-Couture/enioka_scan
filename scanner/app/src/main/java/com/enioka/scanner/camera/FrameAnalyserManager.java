package com.enioka.scanner.camera;

import android.util.Log;

import java.util.ArrayDeque;
import java.util.Calendar;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.enioka.scanner.camera.ZbarScanView.beepOk;

/**
 * Holder for analyser thread pool.
 */
class FrameAnalyserManager {
    private static int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
    private static final String TAG = "BARCODE";

    // Success callback
    private ZbarScanView parent;

    // Pool components
    private ThreadPoolExecutor pool;

    // FPS counter variables
    private long latestIntervalStart = 0;
    private Calendar latestLog = Calendar.getInstance();
    private int countLatestSecond = 0;

    // Deduplication variables
    private Calendar latestResultTime = Calendar.getInstance();
    private String latestBarcodeRead = "";
    private static final int DOUBLE_READ_THRESHOLD_MS = 300;

    private Queue<FrameAnalyser> analysers = new ArrayDeque<>(NUMBER_OF_CORES);

    FrameAnalyserManager(ZbarScanView parent) {
        this.parent = parent;
        BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(25, true); // about 1 second in perfect conditions
        pool = new ThreadPoolExecutor(NUMBER_OF_CORES, NUMBER_OF_CORES, 1, TimeUnit.SECONDS, queue);
        pool.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());

        for (int i = 0; i < NUMBER_OF_CORES; i++) {
            //analysers.add(new FrameAnalyser(this));
        }

        Log.i(TAG, "Analyser pool initialized with " + NUMBER_OF_CORES + " threads");
    }

    void handleFrame(FrameAnalysisContext ctx) {
        Log.d(TAG, "BEFORE " + Thread.currentThread().getId());
        pool.submit(new FrameAnalyser(ctx, this));
        Log.d(TAG, "AFTER");
    }

    void close() {
        pool.shutdown();
        try {
            pool.awaitTermination(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            // nothing to do - dying anyway.
        }
    }


    /**
     * Called after each analysis, with null value if nothing found.
     */
    synchronized void handleResult(String result, int symType, byte[] imagePreview) {
        long currentTime = System.nanoTime();
        countLatestSecond++;
        if (latestLog.getTimeInMillis() * 1000000 > latestIntervalStart + 1000000000) {
            Log.d(TAG, "Scan result. FPS: " + 1000000000 * countLatestSecond / (currentTime - latestIntervalStart) + " - Pool size: " + pool.getPoolSize());
            latestIntervalStart = System.nanoTime();
            latestLog = Calendar.getInstance();
            countLatestSecond = 0;
        }

        if (result == null) {
            return;
        }

        if (latestBarcodeRead.equals(result) && Calendar.getInstance().getTimeInMillis() < latestResultTime.getTimeInMillis() + DOUBLE_READ_THRESHOLD_MS) {
            // Ignore this result, its the same barcode that was just read.
            return;
        }
        latestBarcodeRead = result;

        beepOk();
        parent.analyserCallback(result, symType, imagePreview);
    }
}
