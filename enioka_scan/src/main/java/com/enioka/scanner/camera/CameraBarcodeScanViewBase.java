package com.enioka.scanner.camera;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.enioka.scanner.data.BarcodeType;

import java.util.ArrayList;
import java.util.List;

public abstract class CameraBarcodeScanViewBase extends FrameLayout implements ScannerCallback {
    protected List<BarcodeType> symbologies = new ArrayList<BarcodeType>();
    protected ResultHandler handler;
    protected boolean torchOn = false;
    protected boolean failed = false;

    protected Resolution resolution = new Resolution(getContext());
    protected byte[] lastPreviewData;

    protected FrameAnalyserManager frameAnalyser;

    public CameraBarcodeScanViewBase(@NonNull Context context) {
        super(context);
    }

    public CameraBarcodeScanViewBase(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Default is simply CODE_128. Use the Symbol static fields to specify a symbology.
     *
     * @param barcodeType the symbology
     */
    public void addSymbology(BarcodeType barcodeType) {
        this.symbologies.add(barcodeType);
        if (frameAnalyser != null) {
            frameAnalyser.addSymbology(barcodeType);
        }
    }

    public void setResultHandler(ResultHandler handler) {
        this.handler = handler;
    }

    /**
     * Switch on or switch off the torch mode
     *
     * @param value indicate if the torch mode must be switched on (true) or off (false)
     */
    // TODO: finish this
    public void setTorch(boolean value) {
        if (failed) {
            return;
        }

        //Camera.Parameters prms = this.cam.getParameters();
        //setTorch(prms, value);
        //setCameraParameters(prms);
    }

    // abstract void setTorchInternal(boolean value);

    public interface ResultHandler {
        void handleScanResult(String result, BarcodeType type);
    }

    public abstract void cleanUp();

    public abstract void pauseCamera();

    public abstract void resumeCamera();

    public abstract boolean getSupportTorch();

    public abstract boolean getTorchOn();

    public void analyserCallback(final String result, final BarcodeType type, byte[] previewData) {
        if (resolution.usePreviewForPhoto) {
            lastPreviewData = previewData;
        }

        /*if (!keepScanning) {
            this.closeCamera();
        }*/

        // Return result on main thread
        this.post(() -> {
            if (CameraBarcodeScanViewBase.this.handler != null) {
                CameraBarcodeScanViewBase.this.handler.handleScanResult(result, type);
            }
        });
    }
}
