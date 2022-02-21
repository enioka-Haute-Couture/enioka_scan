package com.enioka.scanner.sdk.honeywell;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.enioka.scanner.api.Color;
import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.api.ScannerBackground;
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
public class AIDCScanner implements ScannerBackground, BarcodeReader.BarcodeListener {
    private final static String LOG_TAG = "ScannerHoneywellAidc";

    private Scanner selfScanner = this;
    AidcManager mgr;
    ScannerDataCallback dataCb;
    ScannerStatusCallback statusCb;
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
    public void initialize(Context applicationContext, ScannerInitCallback cb0, ScannerDataCallback cb1, ScannerStatusCallback cb2, Mode mode) {
        this.dataCb = cb1;
        this.statusCb = cb2;

        this.reader = mgr.createBarcodeReader();
        this.reader.addBarcodeListener(this);

        try {
            this.reader.setProperty(BarcodeReader.PROPERTY_TRIGGER_CONTROL_MODE,
                    BarcodeReader.TRIGGER_CONTROL_MODE_AUTO_CONTROL);
        } catch (UnsupportedPropertyException e) {
            cb0.onConnectionFailure(this);
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
            cb0.onConnectionFailure(this);
        }

        if (this.statusCb != null) {
            this.statusCb.onStatusChanged(this, ScannerStatusCallback.Status.READY, "Scanner is ready");
        }
        Log.i(LOG_TAG, "Scanner initialized");

        cb0.onConnectionSuccessful(this);
    }

    @Override
    public void setDataCallBack(ScannerDataCallback cb) {
        this.dataCb = cb;
    }

    @Override
    public void disconnect() {
        if (reader != null) {
            reader.removeBarcodeListener(this);
            reader.close();
        }
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void beepScanSuccessful() {

    }

    @Override
    public void beepScanFailure() {

    }

    @Override
    public void beepPairingCompleted() {

    }

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

    @Override
    public boolean supportsIllumination() {
        return false;
    }

    @Override
    public void ledColorOn(Color color) {
    }

    @Override
    public void ledColorOff(Color color) {
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
        return AIDCProvider.PROVIDER_NAME;
    }
}
