package com.enioka.scanner.sdk.honeywelloss.integrated;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.data.Barcode;
import com.enioka.scanner.helpers.intent.IntentScanner;
import com.enioka.scanner.sdk.honeywelloss.SymbologyId;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class HoneywellOssIntegratedScanner extends IntentScanner<String> implements Scanner.WithTriggerSupport, Scanner.WithIlluminationSupport {
    private static final String LOG_TAG = "HoneywellIntegratedScan";
    private boolean isIlluminationOn = false;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void configureProvider() {
        final Bundle properties = new Bundle();
        properties.putBoolean(HoneywellIntents.PROPERTY_DATA_INTENT_BOOLEAN, true);
        properties.putString(HoneywellIntents.PROPERTY_DATA_INTENT_ACTION, HoneywellIntents.EVENT_BARCODE_READ);

        broadcastIntentFilters.add(HoneywellIntents.EVENT_BARCODE_READ);
        disableScanner = new Intent(HoneywellIntents.ACTION_RELEASE_SCANNER);
        enableScanner = new Intent(HoneywellIntents.ACTION_CLAIM_SCANNER)
                .putExtra(HoneywellIntents.EXTRA_SCANNER, HoneywellIntents.EXTRA_SCANNER_USE_IMAGER) // To remove if we also want to support rings, but needs testing
                .putExtra(HoneywellIntents.EXTRA_PROFILE, HoneywellIntents.EXTRA_PROFILE_USE_DEFAULT)
                .putExtra(HoneywellIntents.EXTRA_PROPERTIES, properties);
    }

    @Override
    protected void configureAfterInit(Context ctx) {
        resume();
    }

    @Override
    public void disconnect() {
        pause();
        super.disconnect();
    }

    @Override
    public String getProviderKey() {
        return HoneywellOssIntegratedScannerProvider.PROVIDER_KEY;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // EXTERNAL INTENT SERVICE CALLBACK
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onReceive(Context context, Intent intent) {
        final String barcodeString = intent.getStringExtra(HoneywellIntents.EXTRA_DATA);
        final String barcodeType = intent.getStringExtra(HoneywellIntents.EXTRA_CODEID);

        final List<Barcode> barcodes = new ArrayList<>();
        barcodes.add(new Barcode(barcodeString, SymbologyId.toBarcodeType(barcodeType)));

        Log.d(LOG_TAG, "Received barcode from integrated scanner " + intent.getStringExtra(HoneywellIntents.EXTRA_NAME)
                + "\nData: " + barcodeType
                + "\nBytes: " + Arrays.toString(intent.getByteArrayExtra(HoneywellIntents.EXTRA_DATABYTES))
                + "\nHoneywell Symbology ID: " + barcodeType + "(" + SymbologyId.toBarcodeType(barcodeType) + ")");

        if (dataCb != null) {
            dataCb.onData(this, barcodes);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // FEATURE: ILLUMINATION
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void enableIllumination() {
        broadcastIntent(HoneywellIntents.ACTION_CONTROL_SCANNER, new HashMap<String, String>() {{
            put(HoneywellIntents.EXTRA_SCAN, String.valueOf(false));
            put(HoneywellIntents.EXTRA_LIGHT, String.valueOf(true));
        }});
        isIlluminationOn = true;
    }

    @Override
    public void disableIllumination() {
        broadcastIntent(HoneywellIntents.ACTION_CONTROL_SCANNER, new HashMap<String, String>() {{
            put(HoneywellIntents.EXTRA_SCAN, String.valueOf(false));
            put(HoneywellIntents.EXTRA_LIGHT, String.valueOf(false));
        }});
        isIlluminationOn = false;
    }

    @Override
    public void toggleIllumination() {
        if (isIlluminationOn) {
            disableIllumination();
        } else {
            enableIllumination();
        }
    }

    @Override
    public boolean isIlluminationOn() {
        return isIlluminationOn;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // FEATURE: SOFTWARE TRIGGERS
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void pressScanTrigger() {
        broadcastIntent(HoneywellIntents.ACTION_CONTROL_SCANNER, HoneywellIntents.EXTRA_SCAN, true);
    }

    @Override
    public void releaseScanTrigger() {
        broadcastIntent(HoneywellIntents.ACTION_CONTROL_SCANNER, HoneywellIntents.EXTRA_SCAN, false);
    }
}
