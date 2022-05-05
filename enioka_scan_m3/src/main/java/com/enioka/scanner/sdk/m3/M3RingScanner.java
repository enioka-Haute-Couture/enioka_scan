package com.enioka.scanner.sdk.m3;


import android.content.Context;
import android.util.Log;

import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.api.callbacks.ScannerStatusCallback;
import com.enioka.scanner.api.proxies.ScannerCommandCallbackProxy;
import com.enioka.scanner.api.proxies.ScannerDataCallbackProxy;
import com.enioka.scanner.api.proxies.ScannerInitCallbackProxy;
import com.enioka.scanner.api.proxies.ScannerStatusCallbackProxy;
import com.enioka.scanner.data.Barcode;
import com.enioka.scanner.data.BarcodeType;
import com.m3.ringscanner.ScannerIDCallback;
import com.m3.ringscanner.ScannerReceiver;
import com.m3.ringscanner.ScannerVersionCallback;
import com.m3.ringscannersdk.RingScannerService;

import java.util.ArrayList;
import java.util.List;

class M3RingScanner implements Scanner {
    private static final String LOG_TAG = "M3RingScanner";

    private RingScannerService scanner;
    private ScannerDataCallbackProxy dataCallback;
    private ScannerStatusCallbackProxy statusCallback;
    private ScannerInitCallbackProxy initCallback;
    private Context context;

    M3RingScanner(RingScannerService scannerService) {
        this.scanner = scannerService;
    }

    @Override
    public void initialize(final Context applicationContext, final ScannerInitCallbackProxy initCallback, final ScannerDataCallbackProxy dataCallback, final ScannerStatusCallbackProxy statusCallback, final Mode mode) {
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
            dataCallback.onData(M3RingScanner.this, res);
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
    public void setDataCallBack(ScannerDataCallbackProxy cb) {
        this.dataCallback = cb;
    }

    @Override
    public void disconnect(@Nullable ScannerCommandCallbackProxy cb) {
        if (this.scanner != null) {
            this.scanner.unbindService();
            if (cb != null) {
                cb.onSuccess();
            }
        } else if (cb != null) {
            cb.onFailure();
        }
    }

    @Override
    public void pause(@Nullable ScannerCommandCallbackProxy cb) {
        statusCallback.onStatusChanged(this, ScannerStatusCallback.Status.DISABLED);
        this.scanner.setReadable(false);
        if (cb != null) {
            cb.onSuccess();
        }
    }

    @Override
    public void resume(@Nullable ScannerCommandCallbackProxy cb) {
        statusCallback.onStatusChanged(this, ScannerStatusCallback.Status.READY);
        this.scanner.setReadable(true);
        if (cb != null) {
            cb.onSuccess();
        }
    }

    @Override
    public String getProviderKey() {
        return M3RingScannerProvider.PROVIDER_KEY;
    }
}
