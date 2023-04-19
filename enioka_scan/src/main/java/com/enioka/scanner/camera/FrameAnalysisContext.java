package com.enioka.scanner.camera;

import android.media.Image;

/**
 * All the data needed by a frame analysis.
 */
class FrameAnalysisContext {
    byte[] frame;

    Image image;

    float camViewMeasuredHeight, camViewMeasuredWidth;
    float cameraWidth, cameraHeight;
    boolean vertical;

    // Targeting rectangle
    int x1, y1, x2, y2, x3, y3, x4, y4; // 1 = top left, 2 = top right, 3 = bottom right, 4 = bottom left.
}
