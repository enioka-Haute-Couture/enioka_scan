package com.enioka.scanner.sdk.camera;

import android.content.Context;
import android.util.Log;

import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.api.proxies.ScannerDataCallbackProxy;
import com.enioka.scanner.api.proxies.ScannerInitCallbackProxy;
import com.enioka.scanner.api.proxies.ScannerStatusCallbackProxy;
import com.enioka.scanner.camera.CameraBarcodeScanView;
import com.enioka.scanner.data.Barcode;
import com.enioka.scanner.data.BarcodeType;
import com.enioka.scanner.helpers.Common;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class CameraBarcodeScanViewScanner implements Scanner, Scanner.WithBeepSupport, Scanner.WithIlluminationSupport, CameraBarcodeScanView.ResultHandler {
    private static final String LOG_TAG = "CamBarcodeScanVScanner";

    private CameraBarcodeScanView scanner;
    private ScannerDataCallbackProxy dataDb;

    public CameraBarcodeScanViewScanner(CameraBarcodeScanView cameraBarcodeScanView, ScannerDataCallbackProxy mHandler) {
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
    public void handleScanResult(String code, BarcodeType barcodeType) {
        // TODO: could use a timer to pause the scanning for a second as it otherwise keeps detecting the same barcode
        Log.v(LOG_TAG, "handleScanResult " + code + " - " + barcodeType);
        if (dataDb != null) {
            List<Barcode> res = new ArrayList<>(1);
            res.add(new Barcode(code.trim(), barcodeType));
            dataDb.onData(this, res);
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // SCANNER LIFECYCLE
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void initialize(final Context applicationContext, final ScannerInitCallbackProxy initCallback, final ScannerDataCallbackProxy dataCallback, final ScannerStatusCallbackProxy statusCallback, final Mode mode) {
        // Do nothing. The camera view implementation is special, as it is built directly and not through the LaserScanner.
        initCallback.onConnectionSuccessful(this);
    }

    @Override
    public String getProviderKey() {
        return "CAMERA_SCANNER";
    }

    @Override
    public void setDataCallBack(ScannerDataCallbackProxy cb) {
        dataDb = cb;
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


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // BEEPS
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void beepScanSuccessful() {
        Common.beepScanSuccessful();
    }

    @Override
    public void beepScanFailure() {
        Common.beepScanFailure();
    }

    @Override
    public void beepPairingCompleted() {
        Common.beepPairingCompleted();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // ILLUMINATION
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public WithIlluminationSupport getIlluminationSupport() {
        if(scanner.getSupportTorch())
            return this;
        return null;
    }

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
    public boolean isIlluminationOn() {
        return scanner.getTorchOn();
    }
}
