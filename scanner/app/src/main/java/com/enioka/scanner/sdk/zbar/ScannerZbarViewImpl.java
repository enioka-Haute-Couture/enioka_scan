package com.enioka.scanner.sdk.zbar;

import android.app.Activity;
import android.util.Log;

import com.enioka.scanner.api.ScannerForeground;
import com.enioka.scanner.camera.ZbarScanView;
import com.enioka.scanner.data.Barcode;
import com.enioka.scanner.data.BarcodeType;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class ScannerZbarViewImpl implements ScannerForeground, ZbarScanView.ResultHandler {
    private static final String LOG_TAG = "ScannerZbarViewImpl";

    private ZbarScanView scanner;
    private ScannerDataCallback dataDb;

    public ScannerZbarViewImpl(ZbarScanView zbarScanView, ScannerDataCallback mHandler) {
        this.dataDb = mHandler;

        this.scanner = zbarScanView;

        scanner.addSymbology(BarcodeType.CODE128);
        scanner.addSymbology(BarcodeType.CODE39);
        scanner.addSymbology(BarcodeType.EAN13);
        scanner.addSymbology(BarcodeType.INT25);

        scanner.setResultHandler(this);
        scanner.setTorch(false);
    }

    @Override
    public void initialize(Activity ctx, ScannerInitCallback cb0, ScannerDataCallback cb1, ScannerStatusCallback cb2, Mode mode) {
        // Do nothing. The Zbar implementation is special, as it is built directly and not through the LaserScanner.
        cb0.onConnectionSuccessful(this);
    }

    @Override
    public void setDataCallBack(ScannerDataCallback cb) {
        //
    }

    @Override
    public void disconnect() {
        scanner.cleanUp();
    }

    @Override
    public void pause() {
        scanner.pauseCamera();
    }

    @Override
    public void resume() {
        scanner.resumeCamera();
    }

    @Override
    public void beepScanSuccessful() {
        ZbarScanView.beepOk();
    }

    @Override
    public void beepScanFailure() {
        ZbarScanView.beepKo();
    }

    @Override
    public void beepPairingCompleted() {
        ZbarScanView.beepWaiting();
    }

    @Override
    public void handleScanResult(String code, BarcodeType barcodeType) {
        Log.v(LOG_TAG, "handleScanResult " + code + " - " + barcodeType);
        if (dataDb != null) {
            List<Barcode> res = new ArrayList<>(1);
            res.add(new Barcode(code.trim(), barcodeType));
            dataDb.onData(this, res);
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // ILLUMINATION
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void enableIllumination() {
        scanner.setTorch(true);
    }

    @Override
    public void disableIllumination() {
        scanner.setTorch(false);
    }

    @Override
    public void toggleIllumination() {
        scanner.setTorch(!scanner.getTorchOn());
    }

    @Override
    public boolean supportsIllumination() {
        return scanner.getSupportTorch();
    }

    @Override
    public boolean isIlluminationOn() {
        return scanner.getTorchOn();
    }

    @Override
    public String getProviderKey() {
        return "ZBAR";
    }
}
