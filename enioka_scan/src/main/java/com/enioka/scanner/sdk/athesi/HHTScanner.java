package com.enioka.scanner.sdk.athesi;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.enioka.scanner.R;
import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.api.ScannerBackground;
import com.enioka.scanner.camera.CameraBarcodeScanView;
import com.enioka.scanner.data.Barcode;
import com.enioka.scanner.data.BarcodeType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Scanner provider for HHT Wrapper Layer (i.e. SPA43)
 */
public class HHTScanner extends BroadcastReceiver implements ScannerBackground {
    private static final String LOG_TAG = "HHTScanner";

    // Initial parameters for SOFTSCANTRIGGER action.
    private static final List<String> initialSettingsSoftScan;

    static {
        initialSettingsSoftScan = new ArrayList<>();
        initialSettingsSoftScan.add(DataWedge.ENABLE_TRIGGERBUTTON);
        initialSettingsSoftScan.add(DataWedge.DISABLE_VIBRATE);
        initialSettingsSoftScan.add(DataWedge.ENABLE_BEEP);
    }

    // Initial symbology parameters.
    private static final List<HHTSymbology> activeSymbologies = new ArrayList<>();

    static {
        activeSymbologies.add(HHTSymbology.CODE39);
        activeSymbologies.add(HHTSymbology.CODE128);
        activeSymbologies.add(HHTSymbology.INT25);
        activeSymbologies.add(HHTSymbology.EAN13);
        activeSymbologies.add(HHTSymbology.QRCODE);
    }

    private static final Uri scannerSettingsUri = Uri.parse("content://com.oem.startup.ScannerParaProvider/settings");

    private Context ctx;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private Scanner.ScannerDataCallback dataCb = null;
    private Scanner.ScannerStatusCallback statusCb = null;
    private Scanner.Mode mode;


    @Override
    public void initialize(Context ctx, ScannerInitCallback cb0, ScannerDataCallback cb1, ScannerStatusCallback cb2, Mode mode) {
        this.ctx = ctx;
        this.dataCb = cb1;
        this.statusCb = cb2;
        this.mode = mode;

        IntentFilter intentFilter = new IntentFilter("DATA_SCAN");
        ctx.registerReceiver(this, intentFilter);

        Intent intent = new Intent();
        intent.setAction(DataWedge.SCANNERINPUTPLUGIN);
        intent.putExtra(DataWedge.EXTRA_PARAMETER, DataWedge.ENABLE_PLUGIN);
        ctx.sendBroadcast(intent);

        // Set trigger and buzzer settings
        for (String initialSetting : initialSettingsSoftScan) {
            intent.setAction(DataWedge.SOFTSCANTRIGGER);
            intent.putExtra(DataWedge.EXTRA_PARAMETER, initialSetting);
            ctx.sendBroadcast(intent);
        }

        // Set symbologies
        syncConfig();

        // Done - signal client.
        if (cb0 != null) {
            cb0.onConnectionSuccessful(this);
        }

        if (cb2 != null) {
            cb2.onStatusChanged(ctx.getString(R.string.scanner_status_waiting));
        }
    }

    @Override
    public void setDataCallBack(ScannerDataCallback cb) {
        this.dataCb = cb;
    }

    @Override
    public void disconnect() {
        Intent TriggerButtonIntent = new Intent();
        TriggerButtonIntent.setAction(DataWedge.SOFTSCANTRIGGER);
        TriggerButtonIntent.putExtra(DataWedge.EXTRA_PARAMETER, DataWedge.DISABLE_TRIGGERBUTTON);
        ctx.sendBroadcast(TriggerButtonIntent);

        Intent i = new Intent();
        i.setAction(DataWedge.SCANNERINPUTPLUGIN);
        i.putExtra(DataWedge.EXTRA_PARAMETER, DataWedge.DISABLE_PLUGIN);
        ctx.sendBroadcast(i);

        ctx.unregisterReceiver(this);
    }

    @Override
    public void pause() {
        Log.d(LOG_TAG, "Sending intent to scanner to disable the trigger");
        Intent i = new Intent();
        i.setAction(DataWedge.SOFTSCANTRIGGER);
        i.putExtra(DataWedge.EXTRA_PARAMETERS, new String[]{DataWedge.DISABLE_TRIGGERBUTTON, DataWedge.STOP_SCANNING});
        ctx.sendBroadcast(i);
    }

    @Override
    public void resume() {
        Log.d(LOG_TAG, "Sending intent to scanner to enable the trigger");
        Intent i = new Intent();
        i.setAction(DataWedge.SOFTSCANTRIGGER);
        i.putExtra(DataWedge.EXTRA_PARAMETERS, new String[]{DataWedge.ENABLE_TRIGGERBUTTON, DataWedge.START_SCANNING});
        ctx.sendBroadcast(i);
    }

    private void syncConfig() {
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
        Boolean enabled;
        HHTSymbology sym;
        c.moveToFirst();

        while (numRow > 0) {
            name = c.getString(c.getColumnIndex("scanner_name"));
            enabled = c.getString(c.getColumnIndex("scanner_para")).equals("enabled");

            //Log.d(LOG_TAG, "Configuration item " + name + " - " + c.getString(c.getColumnIndex("scanner_para")));
            if (name.startsWith("Scanner_")) {
                sym = HHTSymbology.getSymbology(name);
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
        Intent intent = new Intent();
        intent.setAction(DataWedge.SCANNERINPUTPLUGIN);
        intent.putExtra(DataWedge.EXTRA_PARAMETERS, confChanges.toArray(new String[0]));
        ctx.sendBroadcast(intent);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // BEEPS
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void beepScanSuccessful() {
        CameraBarcodeScanView.beepOk();
    }

    public void beepScanFailure() {
        CameraBarcodeScanView.beepKo();
    }

    public void beepPairingCompleted() {
        CameraBarcodeScanView.beepWaiting();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // ILLUMINATION
    ////////////////////////////////////////////////////////////////////////////////////////////////

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

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // FUNCTION SUPPORT
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean supportsIllumination() {
        return false;
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        String barcode = intent.getStringExtra(DataWedge.DATA_STRING);
        int type = intent.getIntExtra(DataWedge.DATA_TYPE, 0);

        HHTSymbology s = HHTSymbology.getSymbology(type);
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
        return HHTProvider.PROVIDER_NAME;
    }
}
