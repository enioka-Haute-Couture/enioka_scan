package com.enioka.scanner.camera;

/**
 * All the data needed for a frame analysis.
 * It also contains all the necessary data for buffer reuse between frames (closing Images, ...)<br>
 * TODO: merge with CroppedPicture one day.
 */
class FrameAnalysisContext<T> {
    CroppedPicture croppedPicture;

    T originalImage; // byte[] for CameraV1, android.media.Image for CameraV2
}
