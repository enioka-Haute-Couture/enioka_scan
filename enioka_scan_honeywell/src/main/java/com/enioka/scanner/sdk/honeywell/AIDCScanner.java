package com.enioka.scanner.sdk.honeywell;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;

import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.api.callbacks.ScannerStatusCallback;
import com.enioka.scanner.api.proxies.ScannerCommandCallbackProxy;
import com.enioka.scanner.api.proxies.ScannerDataCallbackProxy;
import com.enioka.scanner.api.proxies.ScannerInitCallbackProxy;
import com.enioka.scanner.api.proxies.ScannerStatusCallbackProxy;
import com.enioka.scanner.data.Barcode;
import com.enioka.scanner.data.BarcodeType;
import com.honeywell.aidc.AidcManager;
import com.honeywell.aidc.BarcodeFailureEvent;
import com.honeywell.aidc.BarcodeReadEvent;
import com.honeywell.aidc.BarcodeReader;
import com.honeywell.aidc.ScannerUnavailableException;
import com.honeywell.aidc.UnsupportedPropertyException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Scanner implementation for Honeywell/Intermec AIDC SDK.
 */
public class AIDCScanner implements Scanner, BarcodeReader.BarcodeListener {
    private final static String LOG_TAG = "ScannerHoneywellAidc";

    private Scanner selfScanner = this;
    AidcManager mgr;
    ScannerDataCallbackProxy dataCb;
    ScannerStatusCallbackProxy statusCb;
    BarcodeReader reader;

    // Source is https://www.barcodesinc.com/news/?p=12768
    private final static Map<String, BarcodeType> aidc2Api = new HashMap<>();

    static {
        aidc2Api.put("j", BarcodeType.CODE128);
        aidc2Api.put("b", BarcodeType.CODE39);
        aidc2Api.put("f", BarcodeType.DIS25);
        aidc2Api.put("e", BarcodeType.INT25);
        aidc2Api.put("d", BarcodeType.EAN13);
    }

    AIDCScanner(AidcManager manager) {
        this.mgr = manager;
    }


    @Override
    public void initialize(final Context applicationContext, final ScannerInitCallbackProxy initCallback, final ScannerDataCallbackProxy dataCallback, final ScannerStatusCallbackProxy statusCallback, final Mode mode) {
        this.dataCb = dataCallback;
        this.statusCb = statusCallback;

        this.reader = mgr.createBarcodeReader();
        this.reader.addBarcodeListener(this);

        try {
            this.reader.setProperty(BarcodeReader.PROPERTY_TRIGGER_CONTROL_MODE,
                    BarcodeReader.TRIGGER_CONTROL_MODE_AUTO_CONTROL);
        } catch (UnsupportedPropertyException e) {
            initCallback.onConnectionFailure(this);
        }


        Map<String, Object> properties = new HashMap<String, Object>();
        // Set Symbologies On/Off
        properties.put(BarcodeReader.PROPERTY_CODE_128_ENABLED, true);
        properties.put(BarcodeReader.PROPERTY_GS1_128_ENABLED, true);
        properties.put(BarcodeReader.PROPERTY_INTERLEAVED_25_ENABLED, true);
        properties.put(BarcodeReader.PROPERTY_STANDARD_25_ENABLED, true);
        properties.put(BarcodeReader.PROPERTY_CODE_39_ENABLED, true);
        properties.put(BarcodeReader.PROPERTY_EAN_13_ENABLED, true);

        // Set Max Code 39 barcode length
        properties.put(BarcodeReader.PROPERTY_CODE_39_MAXIMUM_LENGTH, 50);

        // Turn on center decoding
        properties.put(BarcodeReader.PROPERTY_CENTER_DECODE, true);

        // Disable bad read response, handle in onFailureEvent
        properties.put(BarcodeReader.PROPERTY_NOTIFICATION_BAD_READ_ENABLED, false);

        // Apply the settings
        reader.setProperties(properties);
        try {
            reader.claim();
        } catch (ScannerUnavailableException e) {
            Log.e(LOG_TAG, "Connection error to scanner", e);
            initCallback.onConnectionFailure(this);
        }

        if (this.statusCb != null) {
            this.statusCb.onStatusChanged(this, ScannerStatusCallback.Status.READY);
        }
        Log.i(LOG_TAG, "Scanner initialized");

        initCallback.onConnectionSuccessful(this);
    }

    @Override
    public void setDataCallBack(ScannerDataCallbackProxy cb) {
        this.dataCb = cb;
    }

    @Override
    public void disconnect(@Nullable ScannerCommandCallbackProxy cb) {
        if (reader != null) {
            reader.removeBarcodeListener(this);
            reader.close();
            if (cb != null) {
                cb.onSuccess();
            }
        } else if (cb != null) {
            cb.onSuccess();
        }
    }

    @Override
    public void pause(@Nullable ScannerCommandCallbackProxy cb) {
        if (cb != null) {
            cb.onSuccess();
        }
    }

    @Override
    public void resume(@Nullable ScannerCommandCallbackProxy cb) {
        if (cb != null) {
            cb.onSuccess();
        }
    }

    private class AsyncResultHandler extends AsyncTask<BarcodeReadEvent, Void, Barcode> {

        @Override
        protected Barcode doInBackground(BarcodeReadEvent... barcodeReadEvents) {
            if (barcodeReadEvents == null || barcodeReadEvents.length == 0) {
                return null;
            }

            BarcodeReadEvent evt = barcodeReadEvents[0];
            BarcodeType type = aidc2Api.get(evt.getCodeId());
            if (type == null) {
                type = BarcodeType.UNKNOWN;
            }
            return new Barcode(evt.getBarcodeData(), type);
        }

        @Override
        protected void onPostExecute(Barcode barcode) {
            if (dataCb != null) {
                dataCb.onData(selfScanner, new ArrayList<Barcode>(Arrays.asList(barcode)));
            }
        }
    }

    @Override
    public void onBarcodeEvent(BarcodeReadEvent barcodeReadEvent) {
        Log.v(LOG_TAG, "New barcode read");
        (new AsyncResultHandler()).execute(barcodeReadEvent);
    }

    @Override
    public void onFailureEvent(BarcodeFailureEvent barcodeFailureEvent) {
        Log.e(LOG_TAG, "New barcode failure event: " + barcodeFailureEvent.toString());
    }

    @Override
    public String getProviderKey() {
        return AIDCProvider.PROVIDER_KEY;
    }
}
