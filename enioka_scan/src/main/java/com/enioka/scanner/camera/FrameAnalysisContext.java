package com.enioka.scanner.camera;

import android.media.Image;

/**
 * All the data needed for a frame analysis.
 * It also contains all the necessary data for buffer reuse between frames (closing Images, ...)<br>
 * TODO: merge with CroppedPicture one day.
 */
class FrameAnalysisContext<T> {
    CroppedPicture croppedPicture;

    T originalImage;
}
