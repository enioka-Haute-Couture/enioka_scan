package com.enioka.scanner.sdk.zbar;

import android.app.Activity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.camera.ZbarScanView;
import com.enioka.scanner.data.Barcode;
import com.enioka.scanner.data.BarcodeType;

import net.sourceforge.zbar.Symbol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 */
public class ScannerZbarViewImpl implements Scanner, ZbarScanView.ResultHandler, ZbarScanView.ResultValidatorAsync {
    private static final String LOG_TAG = "ScannerZbarViewImpl";

    private static final Map<Integer, BarcodeType> barcodeTypesMapping;

    static {
        barcodeTypesMapping = new HashMap<>();
        barcodeTypesMapping.put(Symbol.CODE39, BarcodeType.CODE39);
        barcodeTypesMapping.put(Symbol.CODE128, BarcodeType.CODE128);
        barcodeTypesMapping.put(Symbol.I25, BarcodeType.INT25);
        barcodeTypesMapping.put(Symbol.EAN13, BarcodeType.EAN13);
    }

    private ZbarScanView scanner;
    private ScannerDataCallback dataDb;

    public ScannerZbarViewImpl(ZbarScanView zbarScanView, ScannerDataCallback mHandler) {
        this.dataDb = mHandler;

        this.scanner = zbarScanView;
        //128 enabled by default
        scanner.addSymbology(Symbol.CODE39);
        scanner.addSymbology(Symbol.EAN13);
        scanner.addSymbology(Symbol.I25);
        scanner.setResultHandler(this);
        scanner.setAsyncResultValidator(this);
        scanner.setTorch(false);
        scanner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.v(LOG_TAG, "Autofocus");
                scanner.triggerAutoFocus();
                return false;
            }
        });
    }

    @Override
    public void initialize(Activity ctx, ScannerInitCallback cb0, ScannerDataCallback cb1, ScannerStatusCallback cb2, Mode mode) {
        // Do nothing. The Zbar implementation is special, as it is built directly and not through the LaserScanner.
        cb0.onConnectionSuccessful();
    }

    @Override
    public void setDataCallBack(ScannerDataCallback cb) {
        //
    }

    public boolean checkActiveScanners() {
        return false;
    }

    // Not used.
    public boolean connectToAvailableScanner(ScannerInitCallback callback) {
        return false;
    }

    public void disconnect() {
    }

    @Override
    public void pause() {
        scanner.pauseCamera();
    }

    @Override
    public void resume() {
        scanner.resumeCamera();
    }

    public void beepScanSuccessful() {
        ZbarScanView.beepOk();
    }

    public void beepScanFailure() {
        ZbarScanView.beepKo();
    }

    public void beepPairingCompleted() {
        ZbarScanView.beepWaiting();
    }

    @Override
    public void handleScanResult(String result) {
    }

    @Override
    public void validateResultAsync(final String code, final int type) {
        Log.d(LOG_TAG, "validateResultAsync " + code + " - " + type);
        scanner.giveValidationResult(true, code, true);
        if (dataDb != null) {
            List<Barcode> res = new ArrayList<>(1);
            res.add(new Barcode(code.trim(), barcodeTypesMapping.get(type)));
            dataDb.onData(res);
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
}
