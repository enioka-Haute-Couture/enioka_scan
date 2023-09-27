package com.enioka.scanner.camera;

import android.graphics.Point;

import com.enioka.scanner.data.BarcodeType;

/**
 * Methods needed by the {@link FrameAnalyserManager}
 */
interface ScannerCallback<T> {
    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Main callbacks (analysis result handling)
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * To be called when there is a valid scan result.
     *
     * @param result      barcode read
     * @param type        barcode type (as in Symbol class)
     * @param previewData the context used for this analysis.
     */
    void analyserCallback(final String result, final BarcodeType type, FrameAnalysisContext previewData);

    /**
     * To be called after each analysis.
     *
     * @param analysisContext what was analysed
     */
    void giveBufferBack(FrameAnalysisContext<T> analysisContext);


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Resolution handling
    ////////////////////////////////////////////////////////////////////////////////////////////////

    void setPreviewResolution(Point newResolution);
}
