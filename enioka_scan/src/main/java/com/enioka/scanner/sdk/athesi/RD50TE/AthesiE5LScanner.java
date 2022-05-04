package com.enioka.scanner.sdk.athesi.RD50TE;

import android.content.Context;
import android.content.Intent;

import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.data.Barcode;
import com.enioka.scanner.data.BarcodeType;
import com.enioka.scanner.helpers.intent.IntentScanner;

import java.util.ArrayList;
import java.util.List;

/**
 * Scanner interface for modern Athesi scanners (i.e. E5L)
 */
public class AthesiE5LScanner extends IntentScanner<String> implements Scanner.WithTriggerSupport {
    private static final String LOG_TAG = "AthesiE5LScanner";

    @Override
    public void onReceive(Context context, Intent intent) {
        List<Barcode> barcodes = new ArrayList<>();
        barcodes.add(new Barcode(intent.getStringExtra(AthesiE5LIntents.BARCODE_DATA_EXTRA), BarcodeType.UNKNOWN));
        if (dataCb != null) {
            dataCb.onData(this, barcodes);
        }
    }

    @Override
    public String getProviderKey() {
        return AthesiE5LProvider.PROVIDER_KEY;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void configureProvider() {
        broadcastIntentFilters.add(AthesiE5LIntents.BARCODE_EVENT);
        disableTrigger = null; // FIXME
        enableTrigger = null; // FIXME
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // SOFTWARE TRIGGERS
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void pressScanTrigger() {
        broadcastIntent(AthesiE5LIntents.PRESS_TRIGGER);
    }

    @Override
    public void releaseScanTrigger() {
        broadcastIntent(AthesiE5LIntents.RELEASE_TRIGGER);
    }
}
