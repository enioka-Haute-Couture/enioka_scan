package com.enioka.scanner.camera;

import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.AsyncTask;

import java.io.ByteArrayOutputStream;

/**
 * Convert Yuv Image format used in preview to Jpeg
 */
public class ConvertPreviewAsync extends AsyncTask<Void, Void, Void> {

    byte[] previewData;
    Point previewSize;
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    ConvertPreviewAsync.Callback cb;

    public ConvertPreviewAsync(byte[] previewData, Point previewSize, ConvertPreviewAsync.Callback cb) {
        this.previewData = previewData;
        this.previewSize = previewSize;
        this.cb = cb;
    }

    @Override
    protected Void doInBackground(Void... params) {
        YuvImage img = new YuvImage(this.previewData, ImageFormat.NV21, previewSize.x, previewSize.y, null);
        Rect imgRec = new Rect(0, 0, previewSize.x, previewSize.y);
        img.compressToJpeg(imgRec, 100, buffer);
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (cb != null) {
            cb.onDone(buffer.toByteArray());
        }
        return;
    }

    public interface Callback {
        void onDone(byte[] jpeg);
    }
}
