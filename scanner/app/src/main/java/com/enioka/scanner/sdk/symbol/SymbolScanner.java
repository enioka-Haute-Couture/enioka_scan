package com.enioka.scanner.sdk.symbol;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.data.Barcode;
import com.enioka.scanner.data.BarcodeType;
import com.enioka.scanner.helpers.Common;
import com.geodis.mobicop.eniokascan.R;
import com.symbol.emdk.EMDKManager;
import com.symbol.emdk.EMDKResults;
import com.symbol.emdk.barcode.BarcodeManager;
import com.symbol.emdk.barcode.ScanDataCollection;
import com.symbol.emdk.barcode.ScannerConfig;
import com.symbol.emdk.barcode.ScannerException;
import com.symbol.emdk.barcode.ScannerResults;
import com.symbol.emdk.barcode.StatusData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Zebra implementation for internal SYMBOL (not real Zebra) scanners.
 */
public class SymbolScanner implements Scanner, EMDKManager.EMDKListener, com.symbol.emdk.barcode.Scanner.StatusListener, com.symbol.emdk.barcode.Scanner.DataListener {
    private final static String LOG_TAG = "ScannerZebra";

    private boolean waitingForResult = false;
    private Scanner.ScannerDataCallback dataCb = null;
    private Scanner.ScannerStatusCallback statusCb = null;
    private Scanner.ScannerInitCallback initCb = null;
    private Scanner.Mode mode;

    private Context ctx;

    private final static Map<ScanDataCollection.LabelType, BarcodeType> symbol2Api = new HashMap<>();

    static {
        symbol2Api.put(ScanDataCollection.LabelType.CODE128, BarcodeType.CODE128);
        symbol2Api.put(ScanDataCollection.LabelType.CODE39, BarcodeType.CODE39);
        symbol2Api.put(ScanDataCollection.LabelType.D2OF5, BarcodeType.DIS25);
        symbol2Api.put(ScanDataCollection.LabelType.I2OF5, BarcodeType.INT25);
        symbol2Api.put(ScanDataCollection.LabelType.EAN13, BarcodeType.EAN13);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // SDK fields
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * The main entry point for all Zebra APIs.
     */
    private EMDKManager emdkManager;

    /**
     * The scanner factory.
     */
    private BarcodeManager barcodeManager = null;

    /**
     * The actual barcode scanner object (camera, laser...)
     */
    private com.symbol.emdk.barcode.Scanner scanner = null;


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // SDK init
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Initialize the Symbol SDK.
     */
    private void initEMDK(Context ctx) {
        this.ctx = ctx;

        // The EMDKManager object will be created and returned in the callback.
        EMDKResults results = EMDKManager.getEMDKManager(ctx, this);

        // Check the return status of getEMDKManager and update the status TextView accordingly
        if (results.statusCode != EMDKResults.STATUS_CODE.SUCCESS) {
            initCb.onConnectionFailure();
        }
    }

    @Override
    public void onOpened(EMDKManager emdkManager) {
        this.emdkManager = emdkManager;

        try {
            // Call this method to enable Scanner and its listeners
            initializeScanner();
        } catch (ScannerException e) {
            e.printStackTrace();
            initCb.onConnectionFailure();
        }

        // Toast to indicate that the user can now start scanning
        Toast.makeText(ctx, "Press Hard Scan Button to start scanning...", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClosed() {
        // The EMDK closed abruptly. // Clean up the objects created by EMDK manager
        if (this.emdkManager != null) {
            this.emdkManager.release();
            this.emdkManager = null;
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Scanner configuration
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * All elements needed to start the scanner. Idempotent. Requires EMDK to be initialized.
     * After this method, the scanner can be used by pressing the hardware button.
     */
    private void initializeScanner() throws ScannerException {

        if (scanner == null) {
            // Get the Barcode Manager object
            barcodeManager = (BarcodeManager) this.emdkManager.getInstance(EMDKManager.FEATURE_TYPE.BARCODE);

            // Get default scanner defined on the device
            scanner = barcodeManager.getDevice(BarcodeManager.DeviceIdentifier.DEFAULT);

            if (scanner == null) {
                initCb.onConnectionFailure();
                return;
            }

            // Add data and status listeners
            scanner.addDataListener(this);
            scanner.addStatusListener(this);

            // Hard trigger. When this mode is set, the user has to manually press the trigger on the device after issuing the read call.
            scanner.triggerType = com.symbol.emdk.barcode.Scanner.TriggerType.HARD;

            // Enable the scanner (cannot access configuration before enabling it)
            scanner.enable();

            // Parameters - symbologies
            ScannerConfig cfg = scanner.getConfig();
            cfg.decoderParams.canadianPostal.enabled = false;
            cfg.decoderParams.australianPostal.enabled = false;
            cfg.decoderParams.aztec.enabled = false;
            cfg.decoderParams.chinese2of5.enabled = false;
            cfg.decoderParams.codaBar.enabled = false;
            cfg.decoderParams.code11.enabled = true;
            cfg.decoderParams.code39.enabled = true;
            cfg.decoderParams.code93.enabled = false;
            cfg.decoderParams.code128.enabled = true;
            cfg.decoderParams.compositeAB.enabled = false;
            cfg.decoderParams.compositeC.enabled = false;
            cfg.decoderParams.d2of5.enabled = false;
            cfg.decoderParams.dataMatrix.enabled = false;
            cfg.decoderParams.dutchPostal.enabled = false;
            cfg.decoderParams.ean8.enabled = false;
            cfg.decoderParams.ean13.enabled = false;
            cfg.decoderParams.gs1Databar.enabled = true;
            cfg.decoderParams.gs1DatabarExp.enabled = false;
            cfg.decoderParams.gs1DatabarLim.enabled = false;
            cfg.decoderParams.hanXin.enabled = false;
            cfg.decoderParams.i2of5.enabled = true;
            cfg.decoderParams.japanesePostal.enabled = false;
            cfg.decoderParams.korean3of5.enabled = false;
            cfg.decoderParams.mailMark.enabled = false;
            cfg.decoderParams.matrix2of5.enabled = false;
            cfg.decoderParams.maxiCode.enabled = false;
            cfg.decoderParams.microPDF.enabled = false;
            cfg.decoderParams.microQR.enabled = false;
            cfg.decoderParams.msi.enabled = false;
            cfg.decoderParams.pdf417.enabled = false;
            cfg.decoderParams.qrCode.enabled = false;
            cfg.decoderParams.signature.enabled = false;
            cfg.decoderParams.tlc39.enabled = false;
            cfg.decoderParams.triOptic39.enabled = false;
            cfg.decoderParams.ukPostal.enabled = false;
            cfg.decoderParams.upca.enabled = false;
            cfg.decoderParams.upce0.enabled = false;
            cfg.decoderParams.upce1.enabled = false;
            cfg.decoderParams.us4State.enabled = false;
            cfg.decoderParams.us4StateFics.enabled = false;
            cfg.decoderParams.usPlanet.enabled = false;
            cfg.decoderParams.usPostNet.enabled = false;
            cfg.decoderParams.webCode.enabled = false;

            //scanner.setConfig(cfg);

            // First read - ready to scan after these calls.
            waitingForResult = true;
            scanner.read();
            initCb.onConnectionSuccessful();
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Status changes (paused, asleep...)
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onStatus(StatusData statusData) {
        new AsyncStatusUpdate().execute(statusData);
    }

    /**
     * AsyncTask that configures the current state of scanner on background
     * thread and gives the result to the UI thread
     */
    private class AsyncStatusUpdate extends AsyncTask<StatusData, Void, String> {
        Resources r;

        @Override
        protected String doInBackground(StatusData... params) {
            String statusStr = "";

            // Get the current state of scanner in background
            StatusData statusData = params[0];
            StatusData.ScannerStates state = statusData.getState();

            switch (state) {
                // Scanner is IDLE
                case IDLE:
                    statusStr = r.getString(R.string.scanner_status_idle);

                    if (waitingForResult) {
                        // Happens when the user has pressed the button, then did not scan anything. Rearm.
                        try {
                            scanner.read();
                        } catch (ScannerException e) {
                            e.printStackTrace();
                        }
                    }
                    break;

                // Scanner is SCANNING
                case SCANNING:
                    statusStr = r.getString(R.string.scanner_status_scanning);
                    break;

                // Scanner is waiting for trigger press
                case WAITING:
                    statusStr = r.getString(R.string.scanner_status_waiting);
                    break;

                // Scanner is not enabled
                case DISABLED:
                    statusStr = r.getString(R.string.scanner_status_disabled);
                    break;

                default:
                    break;
            }

            // Return result to populate on UI thread
            return statusStr;
        }

        @Override
        protected void onPostExecute(String result) {
            if (statusCb != null) {
                Toast.makeText(ctx, "Scanner changed status: " + result, Toast.LENGTH_SHORT);
                statusCb.onStatusChanged(result);
            }
        }

        @Override
        protected void onPreExecute() {
            r = ctx.getResources();
        }

        @Override
        protected void onProgressUpdate(Void... values) {

        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Data handling
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onData(ScanDataCollection scanDataCollection) {
        new AsyncDataUpdate().execute(scanDataCollection);
    }

    /**
     * AsyncTask that configures the scanned data on background
     * thread and updated the result on UI thread with scanned data and type of
     * label
     **/
    private class AsyncDataUpdate extends AsyncTask<ScanDataCollection, Void, List<Barcode>> {
        @Override
        protected List<Barcode> doInBackground(ScanDataCollection... params) {
            // Status string that contains both barcode data and type of barcode that is being scanned

            List<Barcode> res = new ArrayList<>();
            try {
                /*if (scanner.isReadPending()) {
                    return res;
                }*/

                // Allow a new scan?
                if (mode == Mode.BATCH) {
                    resume();
                } else {
                    waitingForResult = false;
                }

                ScanDataCollection scanDataCollection = params[0];

                // The ScanDataCollection object gives scanning result and the
                // collection of ScanData. So check the data and its status
                if (scanDataCollection != null && scanDataCollection.getResult() == ScannerResults.SUCCESS) {
                    ArrayList<ScanDataCollection.ScanData> scanData = scanDataCollection.getScanData();

                    // Iterate through scanned data and prepare the statusStr
                    for (ScanDataCollection.ScanData data : scanData) {
                        // Get the scanned data
                        String barcodeData = data.getData();

                        // Get the type of label being scanned
                        // ScanDataCollection.LabelType labelType = data.getLabelType();

                        // Handle result
                        if (!TextUtils.isEmpty(barcodeData)) {
                            res.add(new Barcode(barcodeData, symbol2Api.get(data.getLabelType())));
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (res.size() == 0) {
                waitingForResult = true;
                resume();
            }

            // Return result to be handled on UI thread
            return res;
        }

        @Override
        protected void onPostExecute(List<Barcode> result) {
            if (dataCb != null && result.size() > 0) {
                dataCb.onData(result);
            }
        }

        @Override
        protected void onPreExecute() {
        }


        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Public interface
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void initialize(Activity ctx, ScannerInitCallback cb0, ScannerDataCallback cb1, ScannerStatusCallback cb2, Mode mode) {
        this.initCb = cb0;
        this.dataCb = cb1;
        this.statusCb = cb2;
        this.mode = mode;
        initEMDK(ctx);
    }

    @Override
    public void disconnect() {
        if (this.scanner != null) {
            try {
                this.scanner.release();
            } catch (ScannerException e) {
                Log.i(LOG_TAG, "Error when releasing the scanner", e);
                // Just ignore, we are quitting.
            }
            this.scanner = null;
        }
        if (this.emdkManager != null) {
            this.emdkManager.release();
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // BEEPS
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void beepScanSuccessful() {
        Common.beepScanSuccessful();
    }

    @Override
    public void beepScanFailure() {
        Common.beepScanFailure();
    }

    @Override
    public void beepPairingCompleted() {
        Common.beepPairingCompleted();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // ILLUMINATION
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void enableIllumination() {
        //TODO.
    }

    @Override
    public void disableIllumination() {
        //TODO.
    }

    @Override
    public void toggleIllumination() {
        //TODO
    }

    @Override
    public boolean supportsIllumination() {
        return false;
    }

    @Override
    public boolean isIlluminationOn() {
        return false;
    }

    public void resume() {
        waitingForResult = true;
        try {
            if (!scanner.isReadPending()) {
                scanner.read();
            }
        } catch (ScannerException e) {
            throw new RuntimeException(e);
        }
    }
}

