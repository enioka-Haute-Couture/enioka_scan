package com.enioka.scanner.sdk.camera;

import android.content.Context;
import androidx.annotation.Nullable;
import android.util.Log;

import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.api.callbacks.ScannerStatusCallback;
import com.enioka.scanner.api.proxies.ScannerCommandCallbackProxy;
import com.enioka.scanner.api.proxies.ScannerDataCallbackProxy;
import com.enioka.scanner.api.proxies.ScannerInitCallbackProxy;
import com.enioka.scanner.api.proxies.ScannerStatusCallbackProxy;
import com.enioka.scanner.camera.CameraBarcodeScanView;
import com.enioka.scanner.data.Barcode;
import com.enioka.scanner.data.BarcodeType;
import com.enioka.scanner.helpers.Common;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 */
public class CameraBarcodeScanViewScanner implements Scanner, Scanner.WithBeepSupport, Scanner.WithIlluminationSupport, CameraBarcodeScanView.ResultHandler {
    private static final String LOG_TAG = "CamBarcodeScanVScanner";

    private final CameraBarcodeScanView scanner;
    private ScannerDataCallbackProxy dataDb;
    private final ScannerStatusCallback  statusCallback;

    public CameraBarcodeScanViewScanner(CameraBarcodeScanView cameraBarcodeScanView, ScannerDataCallbackProxy mHandler, final ScannerStatusCallbackProxy statusCallback, final Set<BarcodeType> symbologySelection) {
        this.dataDb = mHandler;
        this.scanner = cameraBarcodeScanView;

        scanner.setResultHandler(this);
        scanner.setTorch(false);
        for(BarcodeType symbology: symbologySelection) {
            scanner.addSymbology(symbology);
        }

        this.statusCallback = statusCallback;
        this.statusCallback.onStatusChanged(this, ScannerStatusCallback.Status.CONNECTED);
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
    public void initialize(final Context applicationContext, final ScannerInitCallbackProxy initCallback, final ScannerDataCallbackProxy dataCallback, final ScannerStatusCallbackProxy statusCallback, final Mode mode, final Set<BarcodeType> symbologySelection) {
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
    public void disconnect(@Nullable ScannerCommandCallbackProxy cb) {
        scanner.cleanUp();
        if (cb != null) {
            cb.onSuccess();
        }
    }

    @Override
    public void pause(@Nullable ScannerCommandCallbackProxy cb) {
        scanner.pauseCamera();
        if (cb != null) {
            cb.onSuccess();
        }
    }

    @Override
    public void resume(@Nullable ScannerCommandCallbackProxy cb) {
        scanner.resumeCamera();
        if (cb != null) {
            cb.onSuccess();
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // BEEPS
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void beepScanSuccessful(@Nullable ScannerCommandCallbackProxy cb) {
        Common.beepScanSuccessful();
        if (cb != null) {
            cb.onSuccess();
        }
    }

    @Override
    public void beepScanFailure(@Nullable ScannerCommandCallbackProxy cb) {
        Common.beepScanFailure();
        if (cb != null) {
            cb.onSuccess();
        }
    }

    @Override
    public void beepPairingCompleted(@Nullable ScannerCommandCallbackProxy cb) {
        Common.beepPairingCompleted();
        if (cb != null) {
            cb.onSuccess();
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // ILLUMINATION
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public WithIlluminationSupport getIlluminationSupport() {
        if (scanner.getSupportTorch())
            return this;
        return null;
    }

    @Override
    public void enableIllumination(@Nullable ScannerCommandCallbackProxy cb) {
        scanner.setTorch(true);
        if (cb != null) {
            cb.onSuccess();
        }
    }

    @Override
    public void disableIllumination(@Nullable ScannerCommandCallbackProxy cb) {
        scanner.setTorch(false);
        if (cb != null) {
            cb.onSuccess();
        }
    }

    @Override
    public void toggleIllumination(@Nullable ScannerCommandCallbackProxy cb) {
        scanner.setTorch(!scanner.getTorchOn());
        if (cb != null) {
            cb.onSuccess();
        }
    }

    @Override
    public boolean isIlluminationOn() {
        return scanner.getTorchOn();
    }
}
