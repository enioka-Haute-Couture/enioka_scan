package com.enioka.scanner.sdk.honeywell;

import android.content.Context;
import android.os.AsyncTask;
import androidx.annotation.Nullable;
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
import com.honeywell.aidc.InvalidScannerNameException;
import com.honeywell.aidc.ScannerUnavailableException;
import com.honeywell.aidc.UnsupportedPropertyException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

    private final static Map<BarcodeType, String> api2aidc = new HashMap<>();

    static {
        aidc2Api.put("j", BarcodeType.CODE128);
        aidc2Api.put("b", BarcodeType.CODE39);
        aidc2Api.put("f", BarcodeType.DIS25);
        aidc2Api.put("e", BarcodeType.INT25);
        aidc2Api.put("d", BarcodeType.EAN13);
        aidc2Api.put("s", BarcodeType.QRCODE);
        aidc2Api.put("z", BarcodeType.AZTEC);
        aidc2Api.put("A", BarcodeType.AUS_POST);
        aidc2Api.put("B", BarcodeType.BRITISH_POST);
        aidc2Api.put("C", BarcodeType.CANADIAN_POST);
        aidc2Api.put("D", BarcodeType.EAN8);
        aidc2Api.put("E", BarcodeType.UPCE);
        aidc2Api.put("H", BarcodeType.HAN_XIN);
        aidc2Api.put("J", BarcodeType.JAPAN_POST);
        aidc2Api.put("K", BarcodeType.DUTCH_POST);
        aidc2Api.put("Q", BarcodeType.CHINA_POST);
        aidc2Api.put("a", BarcodeType.CODABAR);
        aidc2Api.put("c", BarcodeType.UPCA);
        aidc2Api.put("g", BarcodeType.MSI);
        aidc2Api.put("h", BarcodeType.CODE11);
        aidc2Api.put("i", BarcodeType.CODE93);
        aidc2Api.put("r", BarcodeType.PDF417);
        aidc2Api.put("w", BarcodeType.DATAMATRIX);
        aidc2Api.put("x", BarcodeType.MAXICODE);
        aidc2Api.put("y", BarcodeType.GS1_DATABAR);
        aidc2Api.put("{", BarcodeType.GS1_DATABAR_LIMITED);
        aidc2Api.put("|", BarcodeType.GS1_128);
        aidc2Api.put("}", BarcodeType.GS1_DATABAR_EXPANDED);
        aidc2Api.put("?", BarcodeType.KOREA_POST);

        api2aidc.put(BarcodeType.CODE128, BarcodeReader.PROPERTY_CODE_128_ENABLED);
        api2aidc.put(BarcodeType.INT25, BarcodeReader.PROPERTY_INTERLEAVED_25_ENABLED);
        api2aidc.put(BarcodeType.DIS25, BarcodeReader.PROPERTY_STANDARD_25_ENABLED);
        api2aidc.put(BarcodeType.CODE39, BarcodeReader.PROPERTY_CODE_39_ENABLED);
        api2aidc.put(BarcodeType.EAN13, BarcodeReader.PROPERTY_EAN_13_ENABLED);
        api2aidc.put(BarcodeType.QRCODE, BarcodeReader.PROPERTY_QR_CODE_ENABLED);
        api2aidc.put(BarcodeType.AZTEC, BarcodeReader.PROPERTY_AZTEC_ENABLED);
        api2aidc.put(BarcodeType.CHINA_POST, BarcodeReader.PROPERTY_CHINA_POST_ENABLED);
        api2aidc.put(BarcodeType.CODABAR, BarcodeReader.PROPERTY_CODABAR_ENABLED);
        api2aidc.put(BarcodeType.CODE11, BarcodeReader.PROPERTY_CODE_11_ENABLED);
        api2aidc.put(BarcodeType.CODE93, BarcodeReader.PROPERTY_CODE_93_ENABLED);
        api2aidc.put(BarcodeType.DATAMATRIX, BarcodeReader.PROPERTY_DATAMATRIX_ENABLED);
        api2aidc.put(BarcodeType.EAN8, BarcodeReader.PROPERTY_EAN_8_ENABLED);
        api2aidc.put(BarcodeType.MAXICODE, BarcodeReader.PROPERTY_MAXICODE_ENABLED);
        api2aidc.put(BarcodeType.PDF417, BarcodeReader.PROPERTY_PDF_417_ENABLED);
        api2aidc.put(BarcodeType.UPCE, BarcodeReader.PROPERTY_UPC_E_ENABLED);
        api2aidc.put(BarcodeType.KOREA_POST, BarcodeReader.PROPERTY_KOREAN_POST_ENABLED);
        // To be tested
        api2aidc.put(BarcodeType.BRITISH_POST, BarcodeReader.POSTAL_2D_MODE_BPO);
        api2aidc.put(BarcodeType.GS1_128, BarcodeReader.PROPERTY_GS1_128_ENABLED);
        api2aidc.put(BarcodeType.MSI, BarcodeReader.PROPERTY_MSI_ENABLED);
        api2aidc.put(BarcodeType.UPCA, BarcodeReader.PROPERTY_UPC_A_ENABLE);
        // To be tested
        api2aidc.put(BarcodeType.GS1_DATABAR, BarcodeReader.PROPERTY_RSS_ENABLED);
        api2aidc.put(BarcodeType.GS1_DATABAR_LIMITED, BarcodeReader.PROPERTY_RSS_LIMITED_ENABLED);
        api2aidc.put(BarcodeType.GS1_DATABAR_EXPANDED, BarcodeReader.PROPERTY_RSS_EXPANDED_ENABLED);
        api2aidc.put(BarcodeType.AUS_POST, BarcodeReader.POSTAL_2D_MODE_AUSTRALIA);
        api2aidc.put(BarcodeType.DUTCH_POST, BarcodeReader.POSTAL_2D_MODE_DUTCH);
        api2aidc.put(BarcodeType.CANADIAN_POST, BarcodeReader.POSTAL_2D_MODE_CANADA);
        api2aidc.put(BarcodeType.JAPAN_POST, BarcodeReader.POSTAL_2D_MODE_JAPAN);
        api2aidc.put(BarcodeType.CODE39_FULL_ASCII, BarcodeReader.PROPERTY_CODE_39_FULL_ASCII_ENABLED);
        api2aidc.put(BarcodeType.HAN_XIN, BarcodeReader.PROPERTY_HAX_XIN_ENABLED);
    }

    AIDCScanner(AidcManager manager) {
        this.mgr = manager;
    }


    @Override
    public void initialize(final Context applicationContext, final ScannerInitCallbackProxy initCallback, final ScannerDataCallbackProxy dataCallback, final ScannerStatusCallbackProxy statusCallback, final Mode mode, final Set<BarcodeType> symbologySelection) {
        this.dataCb = dataCallback;
        this.statusCb = statusCallback;

        try {
            this.reader = mgr.createBarcodeReader();
        } catch (InvalidScannerNameException e) {
            initCallback.onConnectionFailure(this);
        }
        this.reader.addBarcodeListener(this);

        try {
            this.reader.setProperty(BarcodeReader.PROPERTY_TRIGGER_CONTROL_MODE,
                    BarcodeReader.TRIGGER_CONTROL_MODE_AUTO_CONTROL);
        } catch (UnsupportedPropertyException e) {
            initCallback.onConnectionFailure(this);
        }


        Map<String, Object> properties = new HashMap<String, Object>();
        // Set Symbologies On/Off
        for(Map.Entry<BarcodeType, String> entry: api2aidc.entrySet()) {
            properties.put(entry.getValue(), symbologySelection.contains(entry.getKey()));
        }

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
