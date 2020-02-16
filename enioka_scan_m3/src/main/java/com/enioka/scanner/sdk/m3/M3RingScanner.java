package com.enioka.scanner.sdk.m3;


import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.enioka.scanner.api.Color;
import com.enioka.scanner.api.ScannerBackground;
import com.enioka.scanner.data.Barcode;
import com.enioka.scanner.data.BarcodeType;
import com.m3.ringscanner.ScannerIDCallback;
import com.m3.ringscanner.ScannerReceiver;
import com.m3.ringscanner.ScannerVersionCallback;
import com.m3.ringscannersdk.RingScannerService;

import java.util.ArrayList;
import java.util.List;

class M3RingScanner implements ScannerBackground {
    private static final String LOG_TAG = "M3RingScanner";

    private RingScannerService scanner;
    private ScannerDataCallback dataCallback;
    private ScannerStatusCallback statusCallback;
    private ScannerInitCallback initCallback;
    private Context context;

    M3RingScanner(RingScannerService scannerService) {
        this.scanner = scannerService;
    }

    @Override
    public void initialize(Context applicationContext, ScannerInitCallback initCallback, ScannerDataCallback dataCallback, ScannerStatusCallback statusCallback, Mode mode) {
        this.dataCallback = dataCallback;
        this.statusCallback = statusCallback;
        this.initCallback = initCallback;
        this.context = applicationContext;

        Log.i(LOG_TAG, "Start of initialization of M3 ring scanner.");

        configureScanner();
        scanner.setReadable(true);
        scanner.getCallbackMessage(new ScannerReceiver.Stub() {
            @Override
            public void message(final String s) {
                barcodeDataReceived(s);
            }
        });

        scanner.getCallbackMessage(new ScannerVersionCallback.Stub() {
            @Override
            public void callback(final String s) {
                Log.i(LOG_TAG, "M3 ring scanner connected. Version is " + s);
                //M3RingScanner.this.initCallback.onConnectionSuccessful(M3RingScanner.this);
            }
        });

        scanner.getCallbackMessage(new ScannerIDCallback.Stub() {
            @Override
            public void callback(final String s) {
                Log.i(LOG_TAG, "M3 ring scanner connected. Scanner IS is " + s);
            }
        });

        //TODO: put this in callback when it works (SDK bug)
        M3RingScanner.this.initCallback.onConnectionSuccessful(M3RingScanner.this);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // KOAMTAC CALLBACKS & METHODS
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void barcodeDataReceived(String data) {
        if (dataCallback != null) {
            Barcode barcode = new Barcode(data, BarcodeType.UNKNOWN);

            final List<Barcode> res = new ArrayList<>(1);
            res.add(barcode);

            // Use a handler from the main message loop to run on the UI thread, as this method is called by another thread.
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    dataCallback.onData(M3RingScanner.this, res);
                }
            });
        }
    }

    private void configureScanner() {
        scanner.setPrefix("");
        scanner.setEndCharacter(0);
        scanner.setSleepTime(10);
        scanner.setSoundVolume(3);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void setDataCallBack(ScannerDataCallback cb) {
        this.dataCallback = cb;
    }

    @Override
    public void disconnect() {
        if (this.scanner != null) {
            this.scanner.unbindService();
        }
    }

    @Override
    public void pause() {
        statusCallback.onStatusChanged(context.getString(com.enioka.scanner.R.string.scanner_status_disabled));
        this.scanner.setReadable(false);
    }

    @Override
    public void resume() {
        statusCallback.onStatusChanged(context.getString(com.enioka.scanner.R.string.scanner_status_waiting));
        this.scanner.setReadable(true);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // BEEPS
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void beepScanSuccessful() {

    }

    @Override
    public void beepScanFailure() {

    }

    @Override
    public void beepPairingCompleted() {

    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // ILLUMINATION
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void enableIllumination() {
    }

    @Override
    public void disableIllumination() {
    }

    @Override
    public void toggleIllumination() {
    }

    @Override
    public boolean isIlluminationOn() {
        return false;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // LED
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void ledColorOn(Color color) {
    }

    @Override
    public void ledColorOff(Color color) {

    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // FUNCTION SUPPORT
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean supportsIllumination() {
        return false;
    }

    @Override
    public String getProviderKey() {
        return M3RingScannerProvider.PROVIDER_KEY;
    }
}