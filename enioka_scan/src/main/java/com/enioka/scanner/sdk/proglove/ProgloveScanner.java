package com.enioka.scanner.sdk.proglove;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.enioka.scanner.api.Color;
import com.enioka.scanner.api.ScannerSearchOptions;
import com.enioka.scanner.data.Barcode;
import com.enioka.scanner.data.BarcodeType;
import com.enioka.scanner.helpers.intent.IntentScanner;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Scanner provider for ProGlove MARK II BT gloves.<br>
 * TODO: symbology configuration.
 */
public class ProgloveScanner extends IntentScanner<String> {
    private static final String LOG_TAG = "ProgloveScanner";
    private int connectionAttempts = 0;
    private ScannerSearchOptions options;
    private Timer beepTimer;

    ProgloveScanner(ScannerSearchOptions options) {
        this.options = options;
    }

    @Override
    protected void configureProvider() {
        broadcastIntentFilters.add("com.proglove.api.BARCODE");
        broadcastIntentFilters.add("com.proglove.api.SCANNER_STATE");

        sdk2Api.put("CODE 128", BarcodeType.CODE128);
        sdk2Api.put("CODE 39", BarcodeType.CODE39);
        sdk2Api.put("D25", BarcodeType.DIS25);
        sdk2Api.put("ITF", BarcodeType.INT25);
        sdk2Api.put("EAN-13", BarcodeType.EAN13);
    }

    @Override
    protected void configureAfterInit(Context ctx) {
        broadcastIntent("com.proglove.api.GET_SCANNER_STATE");
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void disconnect() {
        broadcastIntent("com.proglove.api.DISCONNECT");
        super.disconnect();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // EXTERNAL INTENT SERVICE CALLBACK
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }

        switch (intent.getAction()) {
            case "com.proglove.api.SCANNER_STATE":
                handleStatusIntent(intent);
                break;
            case "com.proglove.api.BARCODE":
                handleDataIntent(intent);
                break;
            default:
                Log.e(LOG_TAG, "Received unknown intent action " + intent.getAction());
                // Debug helper.
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    for (String key : bundle.keySet()) {
                        Log.e(LOG_TAG, key + " : " + (bundle.get(key) != null ? bundle.get(key) : "NULL"));
                    }
                }
        }
    }

    private void handleDataIntent(Intent intent) {
        String barcode;

        // Extract data
        if (intent.hasExtra("com.proglove.api.extra.BARCODE_DATA")) {
            barcode = intent.getStringExtra("com.proglove.api.extra.BARCODE_DATA").trim();
        } else {
            return;
        }

        // Extract type
        String typeI = intent.getStringExtra("com.proglove.api.extra.BARCODE_SYMBOLOGY");
        BarcodeType type = getType(typeI);

        // Done.
        List<Barcode> barcodes = new ArrayList<>();
        barcodes.add(new Barcode(barcode, type));
        if (dataCb != null) {
            dataCb.onData(this, barcodes);
        }
    }

    private void handleStatusIntent(Intent intent) {
        String status = intent.getStringExtra("com.proglove.api.extra.SCANNER_STATE");
        Log.d(LOG_TAG, "Received status update from scanner " + status);
        switch (status) {
            case "RECONNECTING":
                this.statusCb.onScannerReconnecting(this);
                break;
            case "ERROR":
            case "CONNECTING":
                this.statusCb.onStatusChanged(status);
                break;
            case "CONNECTED":
                this.statusCb.onStatusChanged(status);
                broadcastIntent("com.proglove.api.GET_CONFIG");
                break;
            case "SEARCHING":
            case "DISCONNECTED":
                if (options.allowPairingFlow && ++connectionAttempts <= 2) {
                    broadcastIntent("com.proglove.api.CONNECT");
                }
                break;
            default:
                this.statusCb.onStatusChanged("Unknown status " + status);
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // LED
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void ledColorOn(Color color) {
        int c = 0;
        switch (color) {
            case GREEN:
                c = 1;
                break;
            case RED:
                c = 2;
                break;
        }
        broadcastIntent("com.proglove.api.PLAY_FEEDBACK", "com.proglove.api.extra.FEEDBACK_SEQUENCE_ID", c);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // TRIGGER
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void pause() {
        broadcastIntent("com.proglove.api.BLOCK_TRIGGER");
        if (beepTimer == null) {
            beepTimer = new Timer();
            beepTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    broadcastIntent("com.proglove.api.PLAY_FEEDBACK", "com.proglove.api.extra.FEEDBACK_SEQUENCE_ID", 4);
                }
            }, 0, 2000);
        }
    }

    @Override
    public void resume() {
        if (beepTimer != null) {
            beepTimer.cancel();
            beepTimer = null;
        }
        broadcastIntent("com.proglove.api.UNBLOCK_TRIGGER");
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // MISC
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public String getProviderKey() {
        return ProgloveProvider.PROVIDER_NAME;
    }
}
