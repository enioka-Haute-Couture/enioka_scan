package com.enioka.scanner.sdk.proglove;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.Nullable;
import android.util.Log;

import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.api.ScannerLedColor;
import com.enioka.scanner.api.ScannerSearchOptions;
import com.enioka.scanner.api.callbacks.ScannerStatusCallback;
import com.enioka.scanner.api.proxies.ScannerCommandCallbackProxy;
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
public class ProgloveScanner extends IntentScanner<String> implements Scanner.WithLedSupport {
    private static final String LOG_TAG = "ProgloveScanner";
    private static final int MAX_CONNECTION_ATTEMPTS = 10;

    private boolean firstServiceResponseReceived = false;
    private int connectionAttempts = 0;
    private boolean connected = false;
    private ScannerSearchOptions options;
    private Timer beepTimer;

    ProgloveScanner(ScannerSearchOptions options) {
        this.options = options;
    }

    @Override
    protected void configureProvider() {
        broadcastIntentFilters.add("com.proglove.api.BARCODE");
        broadcastIntentFilters.add("com.proglove.api.SCANNER_STATE");

        // Full list https://developers.proglove.com/insight-mobile/android/latest/Symbologies.html
        sdk2Api.put("CODE 128", BarcodeType.CODE128);
        sdk2Api.put("CODE 39", BarcodeType.CODE39);
        sdk2Api.put("CODABAR", BarcodeType.CODABAR);
        sdk2Api.put("D25", BarcodeType.DIS25);
        sdk2Api.put("ITF", BarcodeType.INT25);
        sdk2Api.put("CODE 93", BarcodeType.CODE93);
        sdk2Api.put("UPCA", BarcodeType.UPCA);
        sdk2Api.put("UPCE 3", BarcodeType.UPCE);
        sdk2Api.put("EAN-8", BarcodeType.EAN8);
        sdk2Api.put("EAN-13", BarcodeType.EAN13);
        sdk2Api.put("CODE 11", BarcodeType.CODE11);
        sdk2Api.put("MSI", BarcodeType.MSI);
        sdk2Api.put("GS1-128", BarcodeType.GS1_128);
        // Need to be tested
        sdk2Api.put("UPCE1", BarcodeType.UPCE);
        sdk2Api.put("PDF-417", BarcodeType.PDF417);
        sdk2Api.put("CODE 39 FULL ASCII", BarcodeType.CODE39);
        // Need to be  tested
        sdk2Api.put("NW7", BarcodeType.CODABAR);
        // Need to be  tested
        sdk2Api.put("ISBT-128", BarcodeType.CODE128);
        sdk2Api.put("DATA MATRIX", BarcodeType.DATAMATRIX);
        sdk2Api.put("QR CODE", BarcodeType.QRCODE);
        // Need to be  tested
        sdk2Api.put("ISBT-128 CONCAT.", BarcodeType.CODE128);
        sdk2Api.put("POSTAL (JAPAN)", BarcodeType.JAPAN_POST);
        sdk2Api.put("POSTAL (AUSTRALIA)", BarcodeType.AUS_POST);
        sdk2Api.put("POSTAL (DUTCH)", BarcodeType.DUTCH_POST);
        sdk2Api.put("MAXICODE", BarcodeType.MAXICODE);
        sdk2Api.put("POSTBAR (CA)", BarcodeType.CANADIAN_POST);
        sdk2Api.put("POSTAL (UK)", BarcodeType.BRITISH_POST);
        sdk2Api.put("AZTEC CODE", BarcodeType.AZTEC);
        sdk2Api.put("AZTEC RUNE CODE", BarcodeType.AZTEC_RUNE);
        sdk2Api.put("GS1 DATABAR-14", BarcodeType.GS1_DATABAR);
        sdk2Api.put("GS1 DATABAR LIMITED", BarcodeType.GS1_DATABAR_LIMITED);
        sdk2Api.put("GS1 DATABAR EXPANDED", BarcodeType.GS1_DATABAR_EXPANDED);
    }

    @Override
    protected void configureAfterInit(Context ctx) {
        broadcastIntent("com.proglove.api.GET_SCANNER_STATE");

        // Service may not be started. Fire an event after some time to check we have received an answer from it, and force start it if needed.
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!ProgloveScanner.this.firstServiceResponseReceived) {
                    displayPgPairingActivity();
                    broadcastIntent("com.proglove.api.GET_SCANNER_STATE");
                }
            }
        }, 2000);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void stopSchedule() {
        if (beepTimer != null) {
            beepTimer.cancel();
            beepTimer = null;
        }
        broadcastIntent("com.proglove.api.UNBLOCK_TRIGGER", "com.proglove.api.extra.REPLACE_QUEUE", true);
    }

    @Override
    public void pause(@Nullable ScannerCommandCallbackProxy cb) {
        if (!connected) {
            return;
        }
        broadcastIntent("com.proglove.api.BLOCK_TRIGGER", "com.proglove.api.extra.REPLACE_QUEUE", true);
        if (beepTimer == null) {
            beepTimer = new Timer();
            beepTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    broadcastIntent("com.proglove.api.PLAY_FEEDBACK", "com.proglove.api.extra.FEEDBACK_SEQUENCE_ID", 4);
                }
            }, 0, 3000);
        }
        if (cb != null) {
            cb.onSuccess();
        }
    }

    @Override
    public void resume(@Nullable ScannerCommandCallbackProxy cb) {
        stopSchedule();
        if (cb != null) {
            cb.onSuccess();
        }
    }

    @Override
    public void disconnect(@Nullable ScannerCommandCallbackProxy cb) {
        stopSchedule();
        broadcastIntent("com.proglove.api.DISCONNECT");
        super.disconnect(cb);
        if (cb != null) {
            cb.onSuccess();
        }
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
        firstServiceResponseReceived = true;
        String status = intent.getStringExtra("com.proglove.api.extra.SCANNER_STATE");
        Log.d(LOG_TAG, "Received status update from scanner " + status);
        switch (status) {
            case "RECONNECTING":
                this.statusCb.onStatusChanged(this, ScannerStatusCallback.Status.RECONNECTING);
                connected = false;
                this.connectionAttempts++;

                if (connectionAttempts > MAX_CONNECTION_ATTEMPTS) {
                    this.connectionAttempts = 0;
                    broadcastIntent("com.proglove.api.DISCONNECT");
                }

                // Request a new status - scanner may have reconnected.
                requestScannerState(500);

                break;
            case "ERROR":
            case "CONNECTING":
                this.statusCb.onStatusChanged(this, ScannerStatusCallback.Status.CONNECTING);
                break;
            case "CONNECTED":
                this.statusCb.onStatusChanged(this, ScannerStatusCallback.Status.CONNECTED);
                this.connected = true;
                this.connectionAttempts = 0;
                break;
            case "SEARCHING":
                // Just ignore this transient state. Just get status once in a while in order to know when scanner is connected.
                requestScannerState(500);
                break;
            case "DISCONNECTED":
                connected = false;

                if (options.allowPairingFlow && ++connectionAttempts <= MAX_CONNECTION_ATTEMPTS) {
                    // Simply start the PG activity - service may not be up, this will force its start.
                    displayPgPairingActivity();
                } else {
                    Log.w(LOG_TAG, "Given up on connecting to PG scanner - too many tries");
                }
                break;
            default:
                this.statusCb.onStatusChanged(this, ScannerStatusCallback.Status.UNKNOWN);
        }
    }

    private void requestScannerState(int msDelay) {
        if (msDelay > 0) {
            try {
                Thread.sleep(msDelay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        broadcastIntent("com.proglove.api.GET_SCANNER_STATE");
    }

    private void displayPgPairingActivity() {
        ComponentName cn = new ComponentName("de.proglove.connect", "de.proglove.coreui.activities.PairingActivity");
        Intent i = new Intent();
        i.setComponent(cn);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // LED
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void ledColorOn(ScannerLedColor color, @Nullable ScannerCommandCallbackProxy cb) {
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
        if (cb != null) {
            cb.onSuccess();
        }
    }

    @Override
    public void ledColorOff(ScannerLedColor color, @Nullable ScannerCommandCallbackProxy cb) {
        // FIXME
        if (cb != null) {
            cb.onSuccess();
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // MISC
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public String getProviderKey() {
        return ProgloveProvider.PROVIDER_KEY;
    }
}
