package com.enioka.scanner.sdk.camera;

/**
 * Data that must be analysed by a {@link FrameAnalyser}. It must be already cropped and rotated.
 */
class CroppedPicture {
    byte[] barcode;
    int croppedDataWidth, croppedDataHeight;
    long lumaSum = 0L;
}
