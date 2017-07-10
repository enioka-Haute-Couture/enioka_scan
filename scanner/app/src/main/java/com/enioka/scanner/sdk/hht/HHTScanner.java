package com.enioka.scanner.sdk.hht;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.util.Log;

import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.camera.ZbarScanView;
import com.enioka.scanner.data.Barcode;
import com.enioka.scanner.data.BarcodeType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Scanner provider for HTT Wrapper Layer (i.e. SPA43)
 */
public class HHTScanner extends BroadcastReceiver implements Scanner {
    private static final String LOG_TAG = "HHTScanner";

    private static final Map<Integer, BarcodeType> barcodeTypesMapping;

    static {
        barcodeTypesMapping = new HashMap<>();
        barcodeTypesMapping.put(1, BarcodeType.CODE39);
        barcodeTypesMapping.put(3, BarcodeType.CODE128);
        barcodeTypesMapping.put(4, BarcodeType.DIS25);
        barcodeTypesMapping.put(6, BarcodeType.INT25);
        barcodeTypesMapping.put(11, BarcodeType.EAN13);
    }

    private static final List<String> initialSettingsSoftScan;

    static {
        initialSettingsSoftScan = new ArrayList<>();
        initialSettingsSoftScan.add(DataWedge.ENABLE_TRIGGERBUTTON);
        initialSettingsSoftScan.add(DataWedge.DISABLE_VIBRATE);
        initialSettingsSoftScan.add(DataWedge.ENABLE_BEEP);
    }

    private static final List<String> symbologies;

    static {
        symbologies = new ArrayList<>();
        symbologies.add(DataWedge.ENABLE_CODE39);
        symbologies.add(DataWedge.ENABLE_CODE128);
        symbologies.add(DataWedge.ENABLE_D25);
        symbologies.add(DataWedge.ENABLE_I25);
        symbologies.add(DataWedge.ENABLE_EAN13);

        symbologies.add(DataWedge.DISABLE_CNVT_CODE39_TO_32);
        symbologies.add(DataWedge.DISABLE_CODE32_PREFIX);
        symbologies.add(DataWedge.DISABLE_CODE39_VER_CHK_DGT);
        symbologies.add(DataWedge.DISABLE_CODE39_REPORT_CHK_DGT);
        symbologies.add(DataWedge.DISABLE_CODE39_FULL_ASCII);
        symbologies.add(DataWedge.DISABLE_TRIOPTIC);
        symbologies.add(DataWedge.DISABLE_CODABAR);
        symbologies.add(DataWedge.DISABLE_CODABAR_CLSI);
        symbologies.add(DataWedge.DISABLE_CODABAR_NOTIS);
        symbologies.add(DataWedge.DISABLE_EAN128);
        symbologies.add(DataWedge.DISABLE_ISBT_128);
        symbologies.add(DataWedge.DISABLE_ISBT_CONCAT);
        symbologies.add(DataWedge.DISABLE_ISBT_TABLE);
        symbologies.add(DataWedge.DISABLE_CODE11);
        symbologies.add(DataWedge.DISABLE_CODE11_VER_CHK_DGT);
        symbologies.add(DataWedge.DISABLE_CODE11_REPORT_CHK_DGT);
        symbologies.add(DataWedge.DISABLE_NEC25);
        symbologies.add(DataWedge.DISABLE_S25IATA);
        symbologies.add(DataWedge.DISABLE_S25INDUSTRIAL);
        symbologies.add(DataWedge.DISABLE_I25_VER_CHK_DGT);
        symbologies.add(DataWedge.DISABLE_I25_REPORT_CHK_DGT);
        symbologies.add(DataWedge.DISABLE_CNVT_I25_TO_EAN13);
        symbologies.add(DataWedge.DISABLE_CODE93);
        symbologies.add(DataWedge.DISABLE_UPCA);
        symbologies.add(DataWedge.DISABLE_UPCA_REPORT_CHK_DGT);
        symbologies.add(DataWedge.DISABLE_UPCA_PREAMBLE);
        symbologies.add(DataWedge.DISABLE_UPCA_PREAMBLE);
        symbologies.add(DataWedge.DISABLE_UPCE);
        symbologies.add(DataWedge.DISABLE_UPCE_REPORT_CHK_DGT);
        symbologies.add(DataWedge.DISABLE_UPCE_PREAMBLE);
        symbologies.add(DataWedge.DISABLE_CNVT_UPCE_TO_UPCA);
        symbologies.add(DataWedge.DISABLE_UPCE1);
        symbologies.add(DataWedge.DISABLE_UPCE1_REPORT_CHK_DGT);
        symbologies.add(DataWedge.DISABLE_UPCE1_PREAMBLE);
        symbologies.add(DataWedge.DISABLE_CNVT_UPCE1_TO_UPCA);
        symbologies.add(DataWedge.DISABLE_EAN8);
        symbologies.add(DataWedge.DISABLE_EAN8_ZEROEXTEND);
        symbologies.add(DataWedge.DISABLE_EAN13_SUPP);
        symbologies.add(DataWedge.DISABLE_BOOKLAND_ISBN);
        symbologies.add(DataWedge.DISABLE_BOOKLAND_EAN);
        symbologies.add(DataWedge.DISABLE_UCC_EXT_CODE);
        symbologies.add(DataWedge.DISABLE_ISSN_EAN);
        symbologies.add(DataWedge.DISABLE_MSI);
        symbologies.add(DataWedge.DISABLE_MSI_REPORT_CHK_DGT);
        symbologies.add(DataWedge.DISABLE_RSS_14);
        symbologies.add(DataWedge.DISABLE_RSS_LIM);
        symbologies.add(DataWedge.DISABLE_RSS_EXP);
        symbologies.add(DataWedge.DISABLE_RSS_TO_UPC);
        symbologies.add(DataWedge.DISABLE_COMPOSITE_CCC);
        symbologies.add(DataWedge.DISABLE_COMPOSITE_CCAB);
        symbologies.add(DataWedge.DISABLE_COMPOSITE_TLC39);
        symbologies.add(DataWedge.DISABLE_COMPOSITE_RSS);
        symbologies.add(DataWedge.DISABLE_CHINA);
        symbologies.add(DataWedge.DISABLE_KOREAN35);
        symbologies.add(DataWedge.DISABLE_MATRIX25);
        symbologies.add(DataWedge.DISABLE_MATRIX25_REDUN);
        symbologies.add(DataWedge.DISABLE_MATRIX25_VER_CHK_DGT);
        symbologies.add(DataWedge.DISABLE_MATRIX25_CHK_DGT);
        symbologies.add(DataWedge.DISABLE_US_POSTNET);
        symbologies.add(DataWedge.DISABLE_US_PLANET);
        symbologies.add(DataWedge.DISABLE_US_POSTAL_CHK_DGT);
        symbologies.add(DataWedge.DISABLE_UK_POSTAL);
        symbologies.add(DataWedge.DISABLE_UK_POSTAL_CHK_DGT);
        symbologies.add(DataWedge.DISABLE_JAPAN_POSTAL);
        symbologies.add(DataWedge.DISABLE_AUSTRALIA_POST);
        symbologies.add(DataWedge.DISABLE_KIX_CODE);
        symbologies.add(DataWedge.DISABLE_ONE_CODE);
        symbologies.add(DataWedge.DISABLE_UPU_FICS_POSTAL);
        symbologies.add(DataWedge.DISABLE_PDF417);
        symbologies.add(DataWedge.DISABLE_MICROPDF417);
        symbologies.add(DataWedge.DISABLE_CODE128EML);
        symbologies.add(DataWedge.DISABLE_DATAMATRIX);
        symbologies.add(DataWedge.DISABLE_MAXICODE);
        symbologies.add(DataWedge.DISABLE_QRCODE);
        symbologies.add(DataWedge.DISABLE_MICROQR);
        symbologies.add(DataWedge.DISABLE_AZTEC);
        symbologies.add(DataWedge.DISABLE_HAN_XIN);
    }

    private Activity ctx;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private Scanner.ScannerDataCallback dataCb = null;
    private Scanner.ScannerStatusCallback statusCb = null;
    private Scanner.Mode mode;

    @Override
    public void initialize(Activity ctx, ScannerInitCallback cb0, ScannerDataCallback cb1, ScannerStatusCallback cb2, Mode mode) {
        this.ctx = ctx;
        this.dataCb = cb1;
        this.statusCb = cb2;
        this.mode = mode;

        IntentFilter intentFilter = new IntentFilter("DATA_SCAN");
        ctx.registerReceiver(this, intentFilter);

        Intent intent;

        /* FIXME
        PackageManager packageManager = ctx.getApplication().getPackageManager();
        intent = new Intent();
        intent.setAction(DataWedge.SCANNERINPUTPLUGIN);
        Log.i(LOG_TAG, "queryBroadcastReceivers " + packageManager. queryBroadcastReceivers(intent, 0));
        if (packageManager. queryBroadcastReceivers(intent, 0).size() == 0) {
            cb0.onConnectionFailure();
        }*/

        intent = new Intent();
        intent.setAction(DataWedge.SCANNERINPUTPLUGIN);
        intent.putExtra(DataWedge.EXTRA_PARAMETER, DataWedge.ENABLE_PLUGIN);
        ctx.sendBroadcast(intent);

        // Set initial settings
        for(String initialSetting : initialSettingsSoftScan) {
            intent.setAction(DataWedge.SOFTSCANTRIGGER);
            intent.putExtra(DataWedge.EXTRA_PARAMETER, initialSetting);
            ctx.sendBroadcast(intent);
        }

        for(String symbology : symbologies) {
            intent.setAction(DataWedge.SCANNERINPUTPLUGIN);
            intent.putExtra(DataWedge.EXTRA_PARAMETER, symbology);
            ctx.sendBroadcast(intent);
        }

        cb0.onConnectionSuccessful();
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

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // BEEPS
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void beepScanSuccessful() {
        ZbarScanView.beepOk();
    }

    public void beepScanFailure() {
        ZbarScanView.beepKo();
    }

    public void beepPairingCompleted() {
        ZbarScanView.beepWaiting();
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

        List<Barcode> barcodes = new ArrayList<>();
        barcodes.add(new Barcode(barcode, barcodeTypesMapping.get(type)));
        dataCb.onData(barcodes);
    }
}
