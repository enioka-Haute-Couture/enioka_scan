package com.enioka.scanner.sdk.zebra;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.data.Barcode;
import com.enioka.scanner.data.BarcodeType;
import com.zebra.scannercontrol.DCSSDKDefs;
import com.zebra.scannercontrol.DCSScannerInfo;
import com.zebra.scannercontrol.FirmwareUpdateEvent;
import com.zebra.scannercontrol.IDcsSdkApiDelegate;
import com.zebra.scannercontrol.RMDAttributes;
import com.zebra.scannercontrol.SDKHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Scanner provider for external BT Zebra (real Zebra, not Symbol) devices.
 */
class BtZebraScanner implements Scanner, IDcsSdkApiDelegate {
    private static final String LOG_TAG = "BtZebraScanner";

    private Scanner selfScanner = this;

    private static final Map<Integer, BarcodeType> barcodeTypesMapping;

    static {
        barcodeTypesMapping = new HashMap<>();
        barcodeTypesMapping.put(1, BarcodeType.CODE39);
        barcodeTypesMapping.put(3, BarcodeType.CODE128);
        barcodeTypesMapping.put(4, BarcodeType.DIS25);
        barcodeTypesMapping.put(6, BarcodeType.INT25);
        barcodeTypesMapping.put(11, BarcodeType.EAN13);
    }

    static final ArrayList<Integer> authorizedSymobolgies;

    static {
        authorizedSymobolgies = new ArrayList<>();
        authorizedSymobolgies.add(RMDAttributes.RMD_ATTR_SYM_CODE_39);
        authorizedSymobolgies.add(RMDAttributes.RMD_ATTR_SYM_CODE_128);
        authorizedSymobolgies.add(RMDAttributes.RMD_ATTR_SYM_DISCRETE_2_OF_5);
        authorizedSymobolgies.add(RMDAttributes.RMD_ATTR_SYM_INTERLEAVED_2_OF_5);
        authorizedSymobolgies.add(RMDAttributes.RMD_ATTR_SYM_EAN_13_JAN_13);
    }

    static final ArrayList<Integer> unauthorizedSymbologies;

    static {
        unauthorizedSymbologies = new ArrayList<>();
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_UPC_A);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_UPC_E);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_UPC_E_1);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_EAN_8_JAN_8);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_BOOKLAND_EAN);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_UCC_EAN_128);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_CODE_93);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_CODE_11);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_CHINESE_2_OF_5);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_CODABAR);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_MSI);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_CODE_32);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_DATAMATRIXQR);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_PDF);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_ISBN);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_UCC_COUPON_EXTENDED);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_ISSN_EAN);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_ISBT_128);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_TRIOPTIC_CODE_39);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_MATRIX_2_OF_5);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_KOREAN_3_OF_5);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_GS1_DATABAR_14);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_GS1_DATABAR_LIMITED);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_GS1_DATABAR_EXPANDED);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_MICROPDF417);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_MAXICODE);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_QR_CODE);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_MICRO_QR_CODE);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_AZTEC);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_HAN_XIN_CODE);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_AUSTRALIAN_POST);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_US_PLANET);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_US_POSTNET);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_KIX_CODE);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_USPS_4CB);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_UK_POSTAL);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_JAPAN_POST);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_FICS);
    }

    private static final int BEEP_HIGH_SHORT_1 = 0;
    private static final int BEEP_LOW_LONG_2 = 16;
    private static final int BEEP_HIGH_LOW_HIGH = 24;

    private SDKHandler sdkHandler;
    private List<DCSScannerInfo> mScannerInfoList;

    private Integer scannerId;
    private int notificationsMask;

    private Scanner.ScannerDataCallback dataCb = null;
    private Scanner.ScannerStatusCallback statusCb = null;
    private Scanner.Mode mode;

    BtZebraScanner(SDKHandler h) {
        this.sdkHandler = h;
    }

    @Override
    public void initialize(Activity ctx, final ScannerInitCallback cb0, final ScannerDataCallback cb1, ScannerStatusCallback cb2, Mode mode) {
        this.dataCb = cb1;
        this.statusCb = cb2;
        this.mode = mode;

        // Instantiate SDKHandler for barcode scanner
        if (sdkHandler == null) {
            sdkHandler = new SDKHandler(ctx);
            sdkHandler.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_BT_NORMAL);
        }
        sdkHandler.dcssdkSetDelegate(this);

        notificationsMask = 0;
        // We would like to subscribe to all scanner available/not-available events
        notificationsMask |=
                DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SCANNER_APPEARANCE.value |
                        DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SCANNER_DISAPPEARANCE.value;
        // We would like to subscribe to all scanner connection events
        notificationsMask |=
                DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SESSION_ESTABLISHMENT.value |
                        DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SESSION_TERMINATION.value;
        // We would like to subscribe to all barcode events
        notificationsMask |= DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_BARCODE.value;
        // subscribe to events set in notification mask
        sdkHandler.dcssdkSubsribeForEvents(notificationsMask);

        connectToAvailableScanner(new ScannerInitCallback() {
            @Override
            public void onConnectionSuccessful(Scanner s) {
                cb0.onConnectionSuccessful(s);
            }

            @Override
            public void onConnectionFailure(Scanner s) {
                cb0.onConnectionFailure(s);
            }
        });
    }

    @Override
    public void setDataCallBack(ScannerDataCallback cb) {
        this.dataCb = cb;
    }

    public boolean checkActiveScanners() {
        mScannerInfoList = new ArrayList<>();

        Log.i(LOG_TAG, "dcssdkGetActiveScannersList : " + sdkHandler.dcssdkGetActiveScannersList(mScannerInfoList));
        if (mScannerInfoList != null) {
            Log.i(LOG_TAG, "Active scanners :");
            for (DCSScannerInfo scannerInfo : mScannerInfoList) {
                Log.i(LOG_TAG, "ScannerID :" + scannerInfo.getScannerID());
                Log.i(LOG_TAG, "ScannerName :" + scannerInfo.getScannerName());
            }
            return mScannerInfoList.size() != 0;
        }
        return false;
    }

    private boolean connectToAvailableScanner(ScannerInitCallback callback) {
        mScannerInfoList = new ArrayList<>();

        // Connect to first available scanner
        Log.i(LOG_TAG, "dcssdkGetAvailableScannersList :" + sdkHandler.dcssdkGetAvailableScannersList(mScannerInfoList));
        if (mScannerInfoList.size() != 0) {
            DCSScannerInfo scannerInfo = mScannerInfoList.get(0);
            scannerId = scannerInfo.getScannerID();
            Log.i(LOG_TAG, "Available scanner :");
            Log.i(LOG_TAG, " ScannerID :" + scannerId);
            Log.i(LOG_TAG, " ScannerName :" + scannerInfo.getScannerName());
            new BtConnectScannerAsync(
                    callback
                    , sdkHandler
                    , scannerId
                    , this)
                    .execute();
            return true;
        } else {
            // no available scanners
            return false;
        }
    }

    @Override
    public void disconnect() {
        if (scannerId != null) {
            Log.i(LOG_TAG, "disconnect");
            sdkHandler.dcssdkTerminateCommunicationSession(scannerId);
            sdkHandler.dcssdkUnsubsribeForEvents(notificationsMask);
            sdkHandler.dcssdkClose(this);
        }
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void beepScanSuccessful() {
        String inXML = "<inArgs><scannerID>" + scannerId + "</scannerID><cmdArgs><arg-int>" +
                +BEEP_HIGH_SHORT_1 + "</arg-int></cmdArgs></inArgs>";

        new ExecuteCommandAsync(DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_SET_ACTION, null).execute(inXML);
    }

    @Override
    public void beepScanFailure() {
        String inXML = "<inArgs><scannerID>" + scannerId + "</scannerID><cmdArgs><arg-int>" +
                +BEEP_LOW_LONG_2 + "</arg-int></cmdArgs></inArgs>";

        new ExecuteCommandAsync(DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_SET_ACTION, null).execute(inXML);
    }

    @Override
    public void beepPairingCompleted() {
        String inXML = "<inArgs><scannerID>" + scannerId + "</scannerID><cmdArgs><arg-int>" +
                +BEEP_HIGH_LOW_HIGH + "</arg-int></cmdArgs></inArgs>";

        new ExecuteCommandAsync(DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_SET_ACTION, null).execute(inXML);
    }

    /**
     * Send a command "inXml" to the scanner, output in "outXml"
     */
    static boolean executeCommand(SDKHandler sdkHandler, int scannerId, DCSSDKDefs.DCSSDK_COMMAND_OPCODE opCode, String inXML, StringBuilder outXML) {
        if (sdkHandler != null) {
            SystemClock.sleep(10); // Required for the buggy scanner to acknowledge the command
            if (outXML == null) {
                outXML = new StringBuilder();
            }
            DCSSDKDefs.DCSSDK_RESULT result = sdkHandler.dcssdkExecuteCommandOpCodeInXMLForScanner(opCode, inXML, outXML, scannerId);
            Log.i(LOG_TAG, "executeCommand : inXml = " + inXML + " - outXml = " + outXML);
            if (result == DCSSDKDefs.DCSSDK_RESULT.DCSSDK_RESULT_SUCCESS)
                return true;
            else if (result == DCSSDKDefs.DCSSDK_RESULT.DCSSDK_RESULT_FAILURE)
                return false;
        }
        return false;
    }

    /**
     * Background task for executeCommands
     */
    private class ExecuteCommandAsync extends AsyncTask<String, Integer, Boolean> {
        DCSSDKDefs.DCSSDK_COMMAND_OPCODE opcode;
        StringBuilder outXML;

        ExecuteCommandAsync(DCSSDKDefs.DCSSDK_COMMAND_OPCODE opcode, StringBuilder outXML) {
            this.opcode = opcode;
            this.outXML = outXML;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            return executeCommand(sdkHandler, scannerId, opcode, strings[0], outXML);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
        }
    }

    /**
     * "Barcode Event" notification informs about reception of a particular barcode of a particular type from a particular active scanner.
     */
    @Override
    public void dcssdkEventBarcode(byte[] barcodeData, int barcodeType, int fromScannerID) {
        Log.i(LOG_TAG, "dcssdkEventBarcode");
        Log.i(LOG_TAG, "************ " + barcodeType);

        if (dataCb != null) {
            final List<Barcode> res = new ArrayList<>(1);
            BarcodeType type = barcodeTypesMapping.get(barcodeType);
            if (type == null) {
                type = BarcodeType.UNKNOWN;
            }
            res.add(new Barcode(new String(barcodeData).trim(), type));

            // Use a handler from the main message loop to run on the UI thread, as dcssdkEventBarcode is called by another thread.
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    dataCb.onData(selfScanner, res);
                }
            });
        }
    }

    /**
     * "Session Established" notification informs about appearance of a particular active scanner.
     */
    @Override
    public void dcssdkEventCommunicationSessionEstablished(DCSScannerInfo activeScanner) {
        Log.i(LOG_TAG, "dcssdkEventCommunicationSessionEstablished");
    }

    /**
     * "Session Terminated" notification informs about disappearance of a particular active scanner
     */
    @Override
    public void dcssdkEventCommunicationSessionTerminated(int scannerID) {
        Log.i(LOG_TAG, "dcssdkEventCommunicationSessionTerminated");
    }

    /**
     * "Firmware Update Event" notification informs about status in firmware update process
     */
    @Override
    public void dcssdkEventFirmwareUpdate(FirmwareUpdateEvent firmwareUpdateEvent) {
        Log.i(LOG_TAG, "dcssdkEventFirmwareUpdate");
    }

    /**
     * "Image Event" notification is triggered when an active imaging scanner captures images in image mode.
     */
    @Override
    public void dcssdkEventImage(byte[] imageData, int fromScannerID) {
        Log.i(LOG_TAG, "dcssdkEventImage");
    }

    /**
     * "Device Arrival" notification informs about appearance of a particular available scanner.
     */
    @Override
    public void dcssdkEventScannerAppeared(DCSScannerInfo availableScanner) {
        Log.i(LOG_TAG, "dcssdkEventScannerAppeared");
    }

    /**
     * "Device Disappeared" notification informs about disappearance of a particular available scanner.
     */
    @Override
    public void dcssdkEventScannerDisappeared(int scannerID) {
        Log.i(LOG_TAG, "dcssdkEventScannerDisappeared");
    }

    /**
     * "Video Event" notification is triggered when an active imaging scanner captures video in video mode
     */
    @Override
    public void dcssdkEventVideo(byte[] videoFrame, int fromScannerID) {
        Log.i(LOG_TAG, "dcssdkEventVideo");
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

    @Override
    public String getProviderKey() {
        return BtZebraProvider.PROVIDER_NAME;
    }
}
