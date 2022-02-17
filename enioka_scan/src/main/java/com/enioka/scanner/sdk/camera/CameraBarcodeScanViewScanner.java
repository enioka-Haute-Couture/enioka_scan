package com.enioka.scanner.sdk.camera;

import android.app.Activity;
import android.util.Log;

import com.enioka.scanner.api.Color;
import com.enioka.scanner.api.ScannerForeground;
import com.enioka.scanner.camera.CameraBarcodeScanView;
import com.enioka.scanner.data.Barcode;
import com.enioka.scanner.data.BarcodeType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class CameraBarcodeScanViewScanner implements ScannerForeground, CameraBarcodeScanView.ResultHandler {
    private static final String LOG_TAG = "CamBarcodeScanVScanner";

    private CameraBarcodeScanView scanner;
    private ScannerDataCallback dataDb;

    public CameraBarcodeScanViewScanner(CameraBarcodeScanView cameraBarcodeScanView, ScannerDataCallback mHandler) {
        this.dataDb = mHandler;

        this.scanner = cameraBarcodeScanView;

        scanner.addSymbology(BarcodeType.CODE128);
        scanner.addSymbology(BarcodeType.CODE39);
        scanner.addSymbology(BarcodeType.EAN13);
        scanner.addSymbology(BarcodeType.INT25);

        scanner.setResultHandler(this);
        scanner.setTorch(false);
    }

    @Override
    public void initialize(Activity ctx, ScannerInitCallback initCallback, ScannerDataCallback dataCallback, ScannerStatusCallback statusCallback, Mode mode) {
        // Do nothing. The camera view implementation is special, as it is built directly and not through the LaserScanner.
        initCallback.onConnectionSuccessful(this);
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
        CameraBarcodeScanView.beepOk();
    }

    @Override
    public void beepScanFailure() {
        CameraBarcodeScanView.beepKo();
    }

    @Override
    public void beepPairingCompleted() {
        CameraBarcodeScanView.beepWaiting();
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
    public void ledColorOn(Color color) {
    }

    @Override
    public void ledColorOff(Color color) {
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // INVENTORY
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public String getStatus(String key) {
        return null;
    }

    public String getStatus(String key, boolean allowCache) {
        return null;
    }

    public Map<String, String> getStatus() {
        return new HashMap<>();
    }

    @Override
    public String getProviderKey() {
        return "CAMERA_SCANNER";
    }

}
