package com.enioka.scanner.camera;

import android.graphics.Point;
import android.util.Log;

import com.enioka.scanner.data.BarcodeType;
import com.enioka.scanner.helpers.Common;

import java.util.ArrayDeque;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Holder for analyser thread pool.
 */
class FrameAnalyserManager {
    private static final int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors(); // Wrong on old devices, not an issue for us.
    private static final String TAG = "BARCODE";

    private static final int BEGIN_FPS_ANALYSIS_DELAY_SECONDS = 1;
    private static final int INBETWEEN_ANALYSIS_DELAY_SECONDS = 3;
    private static final int LOWER_FPS_THRESHOLD = 10;
    private static final int LOWER_COMFORTABLE_FPS_THRESHOLD = 15;
    private static final int UPPER_FPS_THRESHOLD = 20;
    private static final int SUCCESSIVE_THRESHOLD_VIOLATIONS_THRESHOLD = 2;

    // Success callback
    private final ScannerCallback parent;
    private final Resolution resolution;

    // FPS counter variables
    private long latestIntervalStart = 0;
    private long latestLog = 0;
    private int countLatestSecond = 0;

    // Seconds in the different resolutions
    private final Map<Point, Long> resolutionUsage = new HashMap<>(20);
    private Point mostUsedResolution;

    // FPS callback. Note: all thresholds are used with "greater than", not "greater or equal to".
    private long analyserStart = 0;
    private long latestFpsReaction = 0;
    private int successiveThresholdViolationsCount = 0;

    // Deduplication variables
    private Calendar latestResultTime = Calendar.getInstance();
    private String latestBarcodeRead = "";
    private static final int DOUBLE_READ_THRESHOLD_MS = 500;

    /**
     * Analyzer pool.
     */
    private final Queue<FrameAnalyser> analysers = new ArrayDeque<>(NUMBER_OF_CORES);
    /**
     * Entry queue. There at most as many buffers waiting for analysis as there are workers.
     * (corresponds to the worst case when all workers finish analysis at the same time and require an new buffer to process)
     * <br><br>This queue is key to uncouple the rate of buffer production (camera preview FPS) and
     * the analysis rate (depending on CPU power) - buffers too many are simply discarded and we
     * skip analysing frames if the camera goes too fast for our analyzers to keep up.
     */
    private final BlockingQueue<FrameAnalysisContext> queue = new ArrayBlockingQueue<>(NUMBER_OF_CORES, false);


    FrameAnalyserManager(ScannerCallback parent, Resolution bag, CameraReader readerToUse) {
        this.parent = parent;
        this.resolution = bag;

        // Misc initializations
        mostUsedResolution = bag.preferredPreviewResolution;
        if (mostUsedResolution == null) {
            mostUsedResolution = new Point(0, 0);
        }

        // Create threads
        FrameAnalyser frameAnalyser;
        for (int i = 0; i < workerCount(); i++) {
            if (readerToUse == CameraReader.ZBAR) {
                frameAnalyser = new ZBarFrameAnalyser(queue, this);
            } else {
                frameAnalyser = new ZXingFrameAnalyser(queue, this);
            }
            new Thread(frameAnalyser).start();
            analysers.add(frameAnalyser);
        }

        Log.i(TAG, "Analyser pool initialized with " + workerCount() + " threads");
    }

    void handleFrame(FrameAnalysisContext ctx) {
        if (!queue.offer(ctx)) {
            Log.d(TAG, "Discarding image");
            endOfFrame(ctx);
        }
    }

    protected void endOfFrame(FrameAnalysisContext ctx) {
        parent.giveBufferBack(ctx);
    }

    void close() {
        for (int i = 0; i < analysers.size(); i++) {
            queue.offer(new FrameAnalysisContext());
        }
        for (FrameAnalyser frameAnalyser : analysers) {
            try {
                if (!frameAnalyser.awaitTermination(2, TimeUnit.SECONDS)) {
                    Log.w(TAG, "An analyser did not quit in time");
                }
            } catch (InterruptedException e) {
                // nothing to do - dying anyway.
            }
        }
        analysers.clear();
        queue.clear();
    }

    // Note this method is not synchronised as it is called on each frame and this would be costly.
    // So the FPS indication may be slightly off, which is not an issue as precision is not needed.
    void fpsCounter(int pictureY) {
        long currentTime = System.nanoTime();
        if (analyserStart == 0) {
            analyserStart = currentTime;
        }

        countLatestSecond++;

        if (latestLog > latestIntervalStart + 1000000000) {
            float fps = 1000000000 * (float) countLatestSecond / (currentTime - latestIntervalStart);
            Log.d(TAG, "FPS: " + fps + " - Pool size: " + analysers.size() + ". Current analysis queue depth: "
                    + queue.size() + ". Current res: " + resolution.currentPreviewResolution.x + "*" + resolution.currentPreviewResolution.y
                    + ". Statistics are: " + resolutionUsage);
            latestIntervalStart = currentTime;
            countLatestSecond = 0;

            // Do not change resolution if low luminosity - it means the camera is inside a pocket.
            // On low luminosity, the FPS drops dramatically, so we need this to avoid useless resolution jumps.
            if (pictureY < 70) {
                return;
            }

            // Increment time passed in this resolution.
            Point currentResolution = resolution.currentPreviewResolution;
            Long previousTimeValue = resolutionUsage.get(currentResolution);
            if (previousTimeValue == null) {
                previousTimeValue = 0L;
            }
            resolutionUsage.put(currentResolution, previousTimeValue + 1);

            // Update most used resolution
            Point tmpMostUsed = new Point(0, 0);
            long maxUsages = 0L;
            for (Map.Entry<Point, Long> pair : resolutionUsage.entrySet()) {
                if (pair.getValue() > maxUsages) {
                    maxUsages = pair.getValue();
                    tmpMostUsed = pair.getKey();
                }
            }
            boolean inMostUsedResolution = false;
            if (tmpMostUsed != mostUsedResolution) {
                mostUsedResolution = tmpMostUsed;
                resolution.persistDefaultPreviewResolution(mostUsedResolution);
            }
            if (mostUsedResolution == currentResolution) {
                inMostUsedResolution = true;
            }

            // Only do FPS analysis a few seconds after actual scan start, and use a stabilization period between callback calls.
            if (currentTime > analyserStart + BEGIN_FPS_ANALYSIS_DELAY_SECONDS * 1000000000L && currentTime > latestFpsReaction + INBETWEEN_ANALYSIS_DELAY_SECONDS * 1000000000L) {
                if (fps < LOWER_FPS_THRESHOLD // Low FPS
                        || fps > UPPER_FPS_THRESHOLD // High FPS
                        || (fps < LOWER_COMFORTABLE_FPS_THRESHOLD && resolution.currentPreviewResolution.y > 1080)) // low FPS on high resolution )
                {
                    successiveThresholdViolationsCount++;

                    // Only change resolution after a few violations (and give a boost to the most used resolution)
                    if ((!inMostUsedResolution && successiveThresholdViolationsCount > SUCCESSIVE_THRESHOLD_VIOLATIONS_THRESHOLD) ||
                            (inMostUsedResolution && successiveThresholdViolationsCount > SUCCESSIVE_THRESHOLD_VIOLATIONS_THRESHOLD * 2)) {

                        Point newResolution = this.resolution.getNextPreviewResolution(fps < LOWER_COMFORTABLE_FPS_THRESHOLD);
                        if (newResolution != null) {
                            parent.setPreviewResolution(newResolution);
                        }

                        // Definitely remove resolutions a bit too high.
                        if (fps < LOWER_COMFORTABLE_FPS_THRESHOLD && currentResolution.y > 1080) {
                            resolution.removeResolution(currentResolution);
                        }

                        latestFpsReaction = currentTime;
                        successiveThresholdViolationsCount = 0;

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
     void handleResult(String result, BarcodeType symType, byte[] imagePreview) {
        if (result == null || result.isEmpty()) {
            return;
        }

        synchronized (this) {
            if (latestBarcodeRead.equals(result) && Calendar.getInstance().getTimeInMillis() < latestResultTime.getTimeInMillis() + DOUBLE_READ_THRESHOLD_MS) {
                // Ignore this result, its the same barcode that was just read.
                return;
            }
            latestBarcodeRead = result;
            latestResultTime = Calendar.getInstance();
        }

        // Do NOT put beep inside the synchronized block - deadlock in recent Android versions.
        Common.beepScanSuccessful();
        Log.d(TAG, "barcode read: " + result);
        parent.analyserCallback(result, symType, imagePreview);
    }

    /**
     * Default is simply CODE_128. Use the Symbol static fields to specify a symbology.
     *
     * @param barcodeType the symbology to add to the allowed symbologies. Less is best in terms of performance.
     */
    void addSymbology(BarcodeType barcodeType) {
        for (FrameAnalyser frameAnalyser : analysers) {
            frameAnalyser.addSymbology(barcodeType);
        }
    }

    int workerCount() {
        return NUMBER_OF_CORES;
    }

    int maxBuffersInConcurrentUse() {
        // At most there are workerCount buffers being processed inside the worker + a full waiting queue.
        // The +1 is here because there may be a buffer trying to enter the queue
        return workerCount() * 2 + 1;
    }

    void cancelPendingWork() {
        this.queue.clear();
    }
}
