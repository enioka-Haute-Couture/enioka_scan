package com.enioka.scanner.sdk.athesi.SPA43LTE;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import androidx.annotation.Nullable;
import android.util.Log;

import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.api.proxies.ScannerCommandCallbackProxy;
import com.enioka.scanner.data.Barcode;
import com.enioka.scanner.data.BarcodeType;
import com.enioka.scanner.helpers.intent.IntentScanner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Scanner interface for HHT Wrapper Layer (i.e. SPA43)
 */
public class AthesiSPA43LTEScanner extends IntentScanner<String> implements Scanner.WithTriggerSupport {
    private static final String LOG_TAG = "AthesiHHTScanner";

    // Initial parameters for SOFTSCANTRIGGER action.
    private static final List<String> initialSettingsSoftScan;

    static {
        initialSettingsSoftScan = new ArrayList<>();
        initialSettingsSoftScan.add(DataWedge.ENABLE_TRIGGERBUTTON);
        initialSettingsSoftScan.add(DataWedge.DISABLE_VIBRATE);
        initialSettingsSoftScan.add(DataWedge.ENABLE_BEEP);
    }

    // Initial symbology parameters.
    private static final List<AthesiSPA43LTESymbology> activeSymbologies = new ArrayList<>();

    private static final Uri scannerSettingsUri = Uri.parse("content://com.oem.startup.ScannerParaProvider/settings");

    @Override
    protected void configureProvider() {
        broadcastIntentFilters.add("DATA_SCAN");
        disableScanner = newIntent(DataWedge.SCANNERINPUTPLUGIN, DataWedge.EXTRA_PARAMETER, DataWedge.DISABLE_PLUGIN);
        enableScanner = newIntent(DataWedge.SCANNERINPUTPLUGIN, DataWedge.EXTRA_PARAMETER, DataWedge.ENABLE_PLUGIN);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // SOFTWARE TRIGGERS
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void pressScanTrigger(@Nullable ScannerCommandCallbackProxy cb) {
        broadcastIntent(DataWedge.SOFTSCANTRIGGER, DataWedge.EXTRA_PARAMETER, DataWedge.START_SCANNING);
        if (cb != null) {
            cb.onSuccess();
        }
    }

    @Override
    public void releaseScanTrigger(@Nullable ScannerCommandCallbackProxy cb) {
        broadcastIntent(DataWedge.SOFTSCANTRIGGER, DataWedge.EXTRA_PARAMETER, DataWedge.STOP_SCANNING);
        if (cb != null) {
            cb.onSuccess();
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void configureAfterInit(Context ctx) {
        // Enable the service
        broadcastIntent(DataWedge.SCANNERINPUTPLUGIN, DataWedge.EXTRA_PARAMETER, DataWedge.ENABLE_PLUGIN);

        // Set trigger and buzzer settings
        for (String initialSetting : initialSettingsSoftScan) {
            broadcastIntent(DataWedge.SOFTSCANTRIGGER, DataWedge.EXTRA_PARAMETER, initialSetting);
        }

        // Set symbologies
        for (BarcodeType symbology : this.symbologies) {
            activeSymbologies.add(AthesiSPA43LTESymbology.getSymbology(symbology));
        }

        syncConfig(ctx);
    }

    @Override
    public void disconnect(@Nullable ScannerCommandCallbackProxy cb) {
        broadcastIntent(DataWedge.SOFTSCANTRIGGER, DataWedge.EXTRA_PARAMETER, DataWedge.DISABLE_TRIGGERBUTTON);
        broadcastIntent(DataWedge.SCANNERINPUTPLUGIN, DataWedge.EXTRA_PARAMETER, DataWedge.DISABLE_PLUGIN);
        super.disconnect(cb);
    }

    private void syncConfig(Context ctx) {
        ContentResolver r = ctx.getContentResolver();
        Cursor c = r.query(scannerSettingsUri, null, null, null, null);
        if (c == null) {
            Log.e(LOG_TAG, "Cannot find the shared scanner settings");
            throw new RuntimeException("scanner settings exception");
        }

        Set<String> confChanges = new HashSet<>();
        Set<String> ignored = new HashSet<>();
        int numRow = c.getCount();
        String name;
        boolean enabled;
        AthesiSPA43LTESymbology sym;
        c.moveToFirst();

        while (numRow > 0) {
            name = c.getString(c.getColumnIndexOrThrow("scanner_name"));
            enabled = c.getString(c.getColumnIndexOrThrow("scanner_para")).equals("enabled");

            //Log.d(LOG_TAG, "Configuration item " + name + " - " + c.getString(c.getColumnIndex("scanner_para")));
            if (name.startsWith("Scanner_")) {
                sym = AthesiSPA43LTESymbology.getSymbology(name);
                if (sym == null && enabled && !ignored.contains(name)) {
                    Log.w(LOG_TAG, "Scanner reports a symbology unknown to the lib is enabled - cannot disable it. Add it to the lib. " + name);
                    ignored.add(name);
                }
                if (sym != null && activeSymbologies.contains(sym) && !enabled) {
                    // Bad. Symbology should be enabled.
                    // Log.i(LOG_TAG, "Enabling symbology " + sym.type.code);
                    confChanges.add(sym.activation);
                } else if (sym != null && !activeSymbologies.contains(sym) && enabled) {
                    // Bad. Should be disabled.
                    // Log.i(LOG_TAG, "Disabling symbology " + sym.type.code);
                    confChanges.add(sym.deactivation);
                }
            }

            c.moveToNext();
            numRow--;
        }

        c.close();

        // Apply changes.
        broadcastIntent(DataWedge.SCANNERINPUTPLUGIN, DataWedge.EXTRA_PARAMETERS, confChanges.toArray(new String[0]));
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // EXTERNAL INTENT SERVICE CALLBACK
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onReceive(final Context context, final Intent intent) {
        String barcode = intent.getStringExtra(DataWedge.DATA_STRING);
        int type = intent.getIntExtra(DataWedge.DATA_TYPE, 0);

        AthesiSPA43LTESymbology s = AthesiSPA43LTESymbology.getSymbology(type);
        BarcodeType bt;
        if (s == null) {
            bt = BarcodeType.UNKNOWN;
        } else {
            bt = s.type;
        }

        List<Barcode> barcodes = new ArrayList<>();
        barcodes.add(new Barcode(barcode, bt));
        if (dataCb != null) {
            dataCb.onData(this, barcodes);
        }
    }

    @Override
    public String getProviderKey() {
        return AthesiSPA43LTEProvider.PROVIDER_KEY;
    }
}
