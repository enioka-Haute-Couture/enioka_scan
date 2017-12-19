package com.enioka.scanner.camera;

import android.graphics.Point;

/**
 * Methods needed by the {@link FrameAnalyserManager}
 */
interface ScannerCallback {
    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Main callbacks (analysis result handling)
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * To be called when there is a valid scan result.
     *
     * @param result      barcode read
     * @param type        barcode type (as in Symbol class)
     * @param previewData the buffer used for this analysis.
     */
    void analyserCallback(final String result, final int type, byte[] previewData);

    /**
     * To be called after each analysis.
     *
     * @param analysisContext what was analysed
     */
    void giveBufferBack(FrameAnalysisContext analysisContext);


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Resolution handling
    ////////////////////////////////////////////////////////////////////////////////////////////////

    void onWorryingFps(boolean lowFps);
}
