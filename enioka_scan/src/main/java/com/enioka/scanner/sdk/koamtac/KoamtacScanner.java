package com.enioka.scanner.sdk.koamtac;


import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.enioka.scanner.R;
import com.enioka.scanner.api.Color;
import com.enioka.scanner.api.ScannerBackground;
import com.enioka.scanner.data.Barcode;

import java.util.ArrayList;
import java.util.List;

import koamtac.kdc.sdk.KDCBarcodeDataReceivedListener;
import koamtac.kdc.sdk.KDCConnectionListener;
import koamtac.kdc.sdk.KDCConstants;
import koamtac.kdc.sdk.KDCData;
import koamtac.kdc.sdk.KDCReader;
import koamtac.kdc.sdk.KDCSymbology;

class KoamtacScanner implements ScannerBackground, KDCBarcodeDataReceivedListener, KDCConnectionListener {

    private KDCReader scanner;
    private BluetoothDevice btDevice;
    private ScannerDataCallback dataCallback;
    private ScannerStatusCallback statusCallback;
    private ScannerInitCallback initCallback;
    private Context ctx;

    KoamtacScanner(BluetoothDevice device) {
        this.btDevice = device;
    }

    @Override
    public void initialize(Context applicationContext, ScannerInitCallback initCallback, ScannerDataCallback dataCallback, ScannerStatusCallback statusCallback, Mode mode) {
        this.ctx = applicationContext;
        this.dataCallback = dataCallback;
        this.statusCallback = statusCallback;
        this.initCallback = initCallback;

        scanner = new KDCReader(null, this, null, null, null, this, false);
        scanner.Connect(this.btDevice); // callbacks are called in ConnectionChanged method.
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // KOAMTACK CALLBACKS & METHODS
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void BarcodeDataReceived(KDCData kdcData) {
        if (dataCallback != null) {
            Barcode barcode = new Barcode(kdcData.GetData(), KoamtacDataTranslator.sdk2Api(kdcData.GetBarcodeSymbology()));

            final List<Barcode> res = new ArrayList<>(1);
            res.add(barcode);

            // Use a handler from the main message loop to run on the UI thread, as this method is called by another thread.
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    dataCallback.onData(KoamtacScanner.this, res);
                }
            });
        }
    }

    @Override
    public void ConnectionChanged(BluetoothDevice bluetoothDevice, int i) {
        String message = null;
        switch (i) {
            case KDCConstants.CONNECTION_STATE_CONNECTED:
                message = this.ctx.getResources().getString(R.string.scanner_status_connected);

                configureScanner();
                if (initCallback != null) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            initCallback.onConnectionSuccessful(KoamtacScanner.this);
                        }
                    });
                }

                break;
            case KDCConstants.CONNECTION_STATE_CONNECTING:
                message = this.ctx.getResources().getString(R.string.scanner_status_connecting);
                break;
            case KDCConstants.CONNECTION_STATE_FAILED:
                message = this.ctx.getResources().getString(R.string.scanner_status_initialization_failure);
                break;
            case KDCConstants.CONNECTION_STATE_INITIALIZING:
                message = this.ctx.getResources().getString(R.string.scanner_status_initializing);
                break;
            case KDCConstants.CONNECTION_STATE_INITIALIZING_FAILED:
                message = this.ctx.getResources().getString(R.string.scanner_status_initialization_failure);
                if (initCallback != null) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            initCallback.onConnectionFailure(KoamtacScanner.this);
                        }
                    });
                }
                break;
            case KDCConstants.CONNECTION_STATE_LISTEN:
                message = this.ctx.getResources().getString(R.string.scanner_status_listen);
                break;
            case KDCConstants.CONNECTION_STATE_LOST:
                message = this.ctx.getResources().getString(R.string.scanner_status_lost);
                break;
            case KDCConstants.CONNECTION_STATE_NONE:
                message = this.ctx.getResources().getString(R.string.scanner_status_unknown);
                break;
        }

        if (message == null) {
            return;
        }

        final String msg2 = message;
        if (this.statusCallback != null) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    statusCallback.onStatusChanged(msg2);
                }
            });
        }
    }

    private void configureScanner() {
        this.scanner.DisableAllSymbologies();
        KDCSymbology s = this.scanner.GetSymbology();
        s.Enable(KDCConstants.Symbology.CODE128, true);
        this.scanner.SetSymbology(s, scanner.GetKDCDeviceInfo());
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
        if (this.scanner != null && this.scanner.IsConnected()) {
            this.scanner.Disconnect();
        }
    }

    @Override
    public void pause() {
        this.scanner.LockButtons(KDCConstants.ButtonLockType.SCAN_BUTTON_ONLY);
    }

    @Override
    public void resume() {
        this.scanner.UnlockButtons(KDCConstants.ButtonLockType.SCAN_BUTTON_ONLY);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // BEEPS
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void beepScanSuccessful() {
        this.scanner.SetSuccessAlertBeep();
    }

    @Override
    public void beepScanFailure() {
        this.scanner.SetFailureAlertBeep();
    }

    @Override
    public void beepPairingCompleted() {
        this.scanner.SetSuccessAlertBeep();
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
        this.scanner.SetDisplayMessage(color.toString());
    }

    @Override
    public void ledColorOff(Color color) {
        this.scanner.SetDisplayMessage("");
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
        return KoamtacScannerProvider.PROVIDER_KEY;
    }


}
