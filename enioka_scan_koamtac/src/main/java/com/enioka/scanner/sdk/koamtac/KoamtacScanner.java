package com.enioka.scanner.sdk.koamtac;


import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.enioka.scanner.R;
import com.enioka.scanner.api.Color;
import com.enioka.scanner.api.ScannerBackground;
import com.enioka.scanner.api.ScannerStatusCallback;
import com.enioka.scanner.data.Barcode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import koamtac.kdc.sdk.KDCBarcodeDataReceivedListener;
import koamtac.kdc.sdk.KDCConnectionListener;
import koamtac.kdc.sdk.KDCConstants;
import koamtac.kdc.sdk.KDCData;
import koamtac.kdc.sdk.KDCDevice;
import koamtac.kdc.sdk.KDCErrorListener;
import koamtac.kdc.sdk.KDCReader;
import koamtac.kdc.sdk.KDCSymbology;

class KoamtacScanner implements ScannerBackground, KDCBarcodeDataReceivedListener, KDCConnectionListener, KDCErrorListener {
    private static final String LOG_TAG = "KoamtacScanner";

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

        Log.i(LOG_TAG, "Start of initialization of Koamtac scanner " + btDevice.getName() + ". SDK version " + KDCReader.GetKDCReaderVersion());

        scanner = new KDCReader(null, this, null, null, null, this, false);

        scanner.EnableAutoConnectionMode(false);
        scanner.SetContext(applicationContext); // Compulsory for BLE.
        scanner.SetKDCErrorListener(this);

        scanner.SetConnectionMode(KDCConstants.ConnectionMode.BLUETOOTH_SMART);

        /*scanner.EnableAttachType(true);
        scanner.EnableAttachSerialNumber(true);
        scanner.EnableAttachTimestamp(true);
        scanner.EnableAttachLocation(true);
        scanner.SetDataDelimiter(KDCConstants.DataDelimiter.SEMICOLON);
        scanner.SetRecordDelimiter(KDCConstants.RecordDelimiter.LF);*/

        if (scanner.IsConnected()) {
            scanner.Disconnect();
        }
        Log.d(LOG_TAG, "Calling connect");
        scanner.Connect(this.btDevice);  // callbacks are called in ConnectionChanged method.
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // KOAMTAC CALLBACKS & METHODS
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
        final ScannerStatusCallback.Status status;
        switch (i) {
            case KDCConstants.CONNECTION_STATE_CONNECTED:
                status = ScannerStatusCallback.Status.CONNECTED;

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
                status = ScannerStatusCallback.Status.CONNECTING;
                break;
            case KDCConstants.CONNECTION_STATE_FAILED:
                status = ScannerStatusCallback.Status.FAILURE;
                break;
            case KDCConstants.CONNECTION_STATE_INITIALIZING:
                status = ScannerStatusCallback.Status.INITIALIZING;
                break;
            case KDCConstants.CONNECTION_STATE_INITIALIZING_FAILED:
                status = ScannerStatusCallback.Status.FAILURE;
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
                status = ScannerStatusCallback.Status.WAITING;
                break;
            case KDCConstants.CONNECTION_STATE_LOST:
                status = ScannerStatusCallback.Status.DISCONNECTED;
                break;
            case KDCConstants.CONNECTION_STATE_NONE:
            default:
                // Not doing anything.
                status = ScannerStatusCallback.Status.UNKNOWN;
                break;
        }

        if (status == ScannerStatusCallback.Status.UNKNOWN) {
            return;
        }

        if (this.statusCallback != null) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    statusCallback.onStatusChanged(KoamtacScanner.this, status);
                }
            });
        }
    }

    private void configureScanner() {
        this.scanner.DisableAllSymbologies();
        KDCSymbology s = this.scanner.GetSymbology();
        s.Enable(KDCConstants.Symbology.CODE128, true);
        s.Enable(KDCConstants.Symbology.I2OF5, true);
        s.Enable(KDCConstants.Symbology.CODE35, true);
        this.scanner.SetSymbology(s, scanner.GetKDCDeviceInfo());
    }

    @Override
    public void ErrorReceived(KDCDevice<?> kdcDevice, int i) {
        Log.e(LOG_TAG, "ERROR in KDC SCANNER: " + i);
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
        if (this.scanner != null) {
            this.scanner.Dispose();
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

    @Override
    public String getStatus(String key) {
        return null;
    }

    @Override
    public String getStatus(String key, boolean allowCache) {
        return null;
    }

    @Override
    public Map<String, String> getStatus() {
        return null;
    }
}
