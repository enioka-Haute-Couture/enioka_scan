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

    // FPS callback. Note: all thresholds are used with "greater than", not "greater or equal to".
    private long analyserStart = 0;
    private long latestFpsReaction = 0;
    private int successiveThresholdViolationsCount = 0;
    private static final int BEGIN_FPS_ANALYSIS_DELAY_SECONDS = 1;
    private static final int INBETWEEN_ANALYSIS_DELAY_SECONDS = 3;
    private static final int LOWER_FPS_THRESHOLD = 10;
    private static final int UPPER_FPS_THRESHOLD = 20;
    private static final int SUCCESSIVE_THRESHOLD_VIOLATIONS_THRESHOLD = 2;

    // Deduplication variables
    private Calendar latestResultTime = Calendar.getInstance();
    private String latestBarcodeRead = "";
    private static final int DOUBLE_READ_THRESHOLD_MS = 0;

    // Analyser pool
    private Queue<FrameAnalyser> analysers = new ArrayDeque<>(NUMBER_OF_CORES);
    private BlockingQueue<FrameAnalysisContext> queue = new ArrayBlockingQueue<>(2 * NUMBER_OF_CORES, false);

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
    // So the FPS indication may be slightly off, which is not an issue as precision is not needed.
    void fpsCounter() {
        long currentTime = System.nanoTime();
        if (analyserStart == 0) {
            analyserStart = currentTime;
        }

        countLatestSecond++;
        if (latestLog > latestIntervalStart + 1000000000) {
            float fps = 1000000000 * (float) countLatestSecond / (currentTime - latestIntervalStart);
            Log.d(TAG, "FPS: " + fps + " - Pool size: " + analysers.size() + ". Current analysis queue depth: " + queue.size());
            latestIntervalStart = currentTime;
            countLatestSecond = 0;

            // Only do FPS analysis a few seconds after actual scan start, and use a stabilization preiod between callback calls.
            if (currentTime > analyserStart + BEGIN_FPS_ANALYSIS_DELAY_SECONDS * 1000000000L && currentTime > latestFpsReaction + INBETWEEN_ANALYSIS_DELAY_SECONDS * 1000000000L) {
                if (fps < LOWER_FPS_THRESHOLD || fps > UPPER_FPS_THRESHOLD) {
                    successiveThresholdViolationsCount++;

                    if (successiveThresholdViolationsCount > SUCCESSIVE_THRESHOLD_VIOLATIONS_THRESHOLD) {
                        this.parent.onWorryingFps(fps < LOWER_FPS_THRESHOLD);
                        latestFpsReaction = currentTime;
                    }
                } else {
                    successiveThresholdViolationsCount = 0;
                }
            }
        }
        latestLog = currentTime + 1;
    }

    /**
     * Called after each successful analysis.
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
