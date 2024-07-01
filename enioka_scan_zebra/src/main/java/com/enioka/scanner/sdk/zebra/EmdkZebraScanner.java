package com.enioka.scanner.sdk.zebra;

import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.enioka.scanner.R;
import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.api.callbacks.ScannerStatusCallback;
import com.enioka.scanner.api.proxies.ScannerCommandCallbackProxy;
import com.enioka.scanner.api.proxies.ScannerDataCallbackProxy;
import com.enioka.scanner.api.proxies.ScannerInitCallbackProxy;
import com.enioka.scanner.api.proxies.ScannerStatusCallbackProxy;
import com.enioka.scanner.data.Barcode;
import com.enioka.scanner.data.BarcodeType;
import com.enioka.scanner.helpers.Common;
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
import java.util.Set;

/**
 * Zebra implementation for internal SYMBOL (not real Zebra) scanners.
 */
public class EmdkZebraScanner implements Scanner, Scanner.WithBeepSupport, EMDKManager.EMDKListener, com.symbol.emdk.barcode.Scanner.StatusListener, com.symbol.emdk.barcode.Scanner.DataListener {
    private final static String LOG_TAG = "ScannerZebra";

    private final Scanner selfScanner = this;
    private boolean waitingForResult = false;
    private ScannerDataCallbackProxy dataCb = null;
    private ScannerStatusCallback statusCb = null;
    private ScannerInitCallbackProxy initCb = null;
    private Scanner.Mode mode;
    private Set<BarcodeType> symbologies;

    private Context ctx;

    private final static Map<ScanDataCollection.LabelType, BarcodeType> symbol2Api = new HashMap<>();


    static {
        symbol2Api.put(ScanDataCollection.LabelType.CODE128, BarcodeType.CODE128);
        symbol2Api.put(ScanDataCollection.LabelType.CODE39, BarcodeType.CODE39);
        symbol2Api.put(ScanDataCollection.LabelType.D2OF5, BarcodeType.DIS25);
        symbol2Api.put(ScanDataCollection.LabelType.I2OF5, BarcodeType.INT25);
        symbol2Api.put(ScanDataCollection.LabelType.EAN13, BarcodeType.EAN13);
        symbol2Api.put(ScanDataCollection.LabelType.QRCODE, BarcodeType.QRCODE);
        symbol2Api.put(ScanDataCollection.LabelType.AZTEC, BarcodeType.AZTEC);
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
            initCb.onConnectionFailure(this);
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
            initCb.onConnectionFailure(this);
        }

        // Toast to indicate that the user can now start scanning
        if (statusCb != null) {
            statusCb.onStatusChanged(this, ScannerStatusCallback.Status.READY);
        }
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
                initCb.onConnectionFailure(this);
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
            for (BarcodeType symbology : symbologies) {
                switch (symbology) {
                    case DIS25:
                        cfg.decoderParams.d2of5.enabled = true;
                        break;
                    case CODE128:
                        cfg.decoderParams.code128.enabled = true;
                        break;
                    case CODE39:
                        cfg.decoderParams.code39.enabled = true;
                        break;
                    case QRCODE:
                        cfg.decoderParams.qrCode.enabled = true;
                        break;
                    case INT25:
                        cfg.decoderParams.i2of5.enabled = true;
                        break;
                    case EAN13:
                        cfg.decoderParams.ean13.enabled = true;
                        break;
                    case AZTEC:
                        cfg.decoderParams.aztec.enabled = true;
                        break;
                }
            }
            scanner.setConfig(cfg);

            // First read - ready to scan after these calls.
            waitingForResult = true;
            scanner.read();
            if (initCb != null) {
                initCb.onConnectionSuccessful(this);
            }
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
                    statusStr = r.getString(R.string.scanner_status_PAUSED);

                    if (waitingForResult && !scanner.isReadPending()) {
                        // Happens when the user has pressed the button, then did not scan anything. Rearm.
                        try {
                            scanner.read();
                        } catch (ScannerException | NullPointerException e) {
                            Log.w(LOG_TAG, "A scan was interrupted because (if null, means scanner has paused)", e);
                            statusStr = "Failed";
                        }
                    }
                    break;

                // Scanner is SCANNING
                case SCANNING:
                    statusStr = r.getString(R.string.scanner_status_SCANNING);
                    break;

                // Scanner is waiting for trigger press
                case WAITING:
                    statusStr = r.getString(R.string.scanner_status_READY);
                    break;

                // Scanner is not enabled
                case DISABLED:
                    statusStr = r.getString(R.string.scanner_status_DISABLED);
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
                if (result.equals(r.getString(R.string.scanner_status_PAUSED)))
                    statusCb.onStatusChanged(selfScanner, ScannerStatusCallback.Status.PAUSED);
                else if (result.equals(r.getString(R.string.scanner_status_SCANNING)))
                    statusCb.onStatusChanged(selfScanner, ScannerStatusCallback.Status.SCANNING);
                else if (result.equals(r.getString(R.string.scanner_status_READY)))
                    statusCb.onStatusChanged(selfScanner, ScannerStatusCallback.Status.READY);
                else if (result.equals(r.getString(R.string.scanner_status_DISABLED)))
                    statusCb.onStatusChanged(selfScanner, ScannerStatusCallback.Status.DISABLED);
                else
                    statusCb.onStatusChanged(selfScanner, ScannerStatusCallback.Status.UNKNOWN);
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
                            BarcodeType type = symbol2Api.get(data.getLabelType());
                            if (type == null) {
                                type = BarcodeType.UNKNOWN;
                            }
                            res.add(new Barcode(barcodeData, type));
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
                dataCb.onData(selfScanner, result);
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
    public void initialize(final Context applicationContext, final ScannerInitCallbackProxy initCallback, final ScannerDataCallbackProxy dataCallback, final ScannerStatusCallbackProxy statusCallback, final Mode mode, final Set<BarcodeType> symbologySelection) {
        this.initCb = initCallback;
        this.dataCb = dataCallback;
        this.statusCb = statusCallback;
        this.mode = mode;
        this.symbologies = symbologySelection;
        initEMDK(applicationContext);
    }

    @Override
    public void setDataCallBack(ScannerDataCallbackProxy cb) {
        this.dataCb = cb;
    }

    @Override
    public void disconnect(@Nullable ScannerCommandCallbackProxy cb) {
        if (this.scanner != null) {
            try {
                this.scanner.release();
            } catch (ScannerException e) {
                Log.i(LOG_TAG, "Error when releasing the scanner", e);
                // Just ignore, we are quitting.
                //if (cb != null) {
                //    cb.onFailure();
                //}
            }
            this.scanner = null;
        }
        if (this.emdkManager != null) {
            Log.i(LOG_TAG, "Releasing EMDK service");
            this.emdkManager.release();
        }
        if (cb != null) {
            cb.onSuccess();
        }
    }

    @Override
    public void pause(@Nullable ScannerCommandCallbackProxy cb) {
        waitingForResult = false;
        try {
            if (scanner.isReadPending()) {
                scanner.cancelRead();
            }
        } catch (ScannerException e) {
            if (cb != null) {
                cb.onFailure();
            }
            throw new RuntimeException(e);
        }
        if (cb != null) {
            cb.onSuccess();
        }
    }

    @Override
    public void resume(@Nullable ScannerCommandCallbackProxy cb) {
        waitingForResult = true;
        try {
            if (!scanner.isReadPending()) {
                scanner.read();
            }
        } catch (ScannerException e) {
            if (cb != null) {
                cb.onFailure();
            }
            throw new RuntimeException(e);
        }
        if (cb != null) {
            cb.onSuccess();
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // BEEPS
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void beepScanSuccessful(@Nullable ScannerCommandCallbackProxy cb) {
        Common.beepScanSuccessful();
        if (cb != null) {
            cb.onSuccess();
        }
    }

    @Override
    public void beepScanFailure(@Nullable ScannerCommandCallbackProxy cb) {
        Common.beepScanFailure();
        if (cb != null) {
            cb.onSuccess();
        }
    }

    @Override
    public void beepPairingCompleted(@Nullable ScannerCommandCallbackProxy cb) {
        Common.beepPairingCompleted();
        if (cb != null) {
            cb.onSuccess();
        }
    }

    @Override
    public String getProviderKey() {
        return EmdkZebraProvider.PROVIDER_KEY;
    }
}

