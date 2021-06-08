package com.enioka.scanner.sdk.bluebird;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import com.enioka.scanner.data.Barcode;
import com.enioka.scanner.data.BarcodeType;
import com.enioka.scanner.helpers.intent.IntentScanner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Scanner provider for BlueBird integrated devices.<br>
 * TODO: symbology configuration.
 */
public class BluebirdScanner extends IntentScanner<Integer> {
    private static final String LOG_TAG = "BluebirdScanner";

    @Override
    protected void configureProvider() {
        broadcastIntentFilters.add("kr.co.bluebird.android.bbapi.action.BARCODE_CALLBACK_DECODING_DATA");

        disableTrigger = newIntent("kr.co.bluebird.android.bbapi.action.BARCODE_SET_TRIGGER", "EXTRA_INT_DATA2", 0);
        enableTrigger = newIntent("kr.co.bluebird.android.bbapi.action.BARCODE_SET_TRIGGER", "EXTRA_INT_DATA2", 1);

        sdk2Api.put(5, BarcodeType.EAN13);
        sdk2Api.put(8, BarcodeType.CODE39);
        sdk2Api.put(10, BarcodeType.CODE128);
        sdk2Api.put(11, BarcodeType.INT25);
    }

    @Override
    protected void configureAfterInit(Context ctx) {
        broadcastIntent("kr.co.bluebird.android.bbapi.action.BARCODE_OPEN", "EXTRA_INT_DATA3", 1); // last param is a request ID.
        broadcastIntent(enableTrigger);
    }

    // Not really needed for now. This registers a callback on action return.
    @SuppressWarnings("unused")
    private void registerActionResultListener(Context ctx) {
        IntentFilter requestFilter = new IntentFilter();
        requestFilter.addAction("kr.co.bluebird.android.bbapi.action.BARCODE_CALLBACK_REQUEST_SUCCESS");
        requestFilter.addAction("kr.co.bluebird.android.bbapi.action.BARCODE_CALLBACK_REQUEST_FAILED");
        ctx.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int requestId = intent.getIntExtra("EXTRA_INT_DATA3", -1);
                int errorCode = intent.getIntExtra("EXTRA_INT_DATA2", -1);
                if (errorCode > 0) {
                    Log.e(LOG_TAG, "Error in action " + requestId + " - return code is " + errorCode);
                } else {
                    Log.d(LOG_TAG, "Action " + requestId + " was executed OK");
                }
            }
        }, requestFilter);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void disconnect() {
        broadcastIntent(disableTrigger);
        broadcastIntent("kr.co.bluebird.android.bbapi.action.BARCODE_CLOSE", "EXTRA_INT_DATA3", 2);
        super.disconnect();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // EXTERNAL INTENT SERVICE CALLBACK
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onReceive(final Context context, final Intent intent) {
        String barcode;

        // Debug helper.
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            for (String key : bundle.keySet()) {
                Log.d(LOG_TAG, key + " : " + (bundle.get(key) != null ? bundle.get(key) : "NULL"));
            }
        }

        // Extract data
        if (intent.hasExtra("EXTRA_BARCODE_DECODING_DATA")) {
            barcode = new String(intent.getByteArrayExtra("EXTRA_BARCODE_DECODING_DATA")).trim();
        } else {
            return;
        }

        // Extract type
        int typeI = intent.getIntExtra("EXTRA_INT_DATA2", -1);
        BarcodeType type = getType(typeI);

        // Done.
        List<Barcode> barcodes = new ArrayList<>();
        barcodes.add(new Barcode(barcode, type));
        if (dataCb != null) {
            dataCb.onData(this, barcodes);
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // INVENTORY
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public String getStatus(String key) {
        return null;
    }

    public Map<String, String> getStatus() {
        return new HashMap<>();
    }

    @Override
    public String getProviderKey() {
        return BluebirdProvider.PROVIDER_NAME;
    }
}
