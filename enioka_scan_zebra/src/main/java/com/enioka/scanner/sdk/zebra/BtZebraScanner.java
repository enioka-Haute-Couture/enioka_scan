package com.enioka.scanner.sdk.zebra;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.util.Xml;

import com.enioka.scanner.api.Color;
import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.api.ScannerForeground;
import com.enioka.scanner.data.Barcode;
import com.enioka.scanner.data.BarcodeType;
import com.zebra.scannercontrol.DCSSDKDefs;
import com.zebra.scannercontrol.DCSScannerInfo;
import com.zebra.scannercontrol.FirmwareUpdateEvent;
import com.zebra.scannercontrol.IDcsSdkApiDelegate;
import com.zebra.scannercontrol.RMDAttributes;
import com.zebra.scannercontrol.SDKHandler;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Scanner provider for external BT Zebra (real Zebra, not Symbol) devices.
 */
class BtZebraScanner implements ScannerForeground, IDcsSdkApiDelegate {
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
        sdkHandler.dcssdkSubsribeForEvents(notificationsMask); // | 20 | 21

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
            sdkHandler.dcssdkClose();
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // COMMAND ASYNC HANDLING METHODS
    ////////////////////////////////////////////////////////////////////////////////////////////////

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
            Log.i(LOG_TAG, "executeCommand " + opCode + " done with code " + result + ": inXml = " + inXML + " - outXml = " + outXML);
            if (result == DCSSDKDefs.DCSSDK_RESULT.DCSSDK_RESULT_SUCCESS)
                return true;
            else if (result == DCSSDKDefs.DCSSDK_RESULT.DCSSDK_RESULT_FAILURE)
                return false;
        }
        return false;
    }

    private interface ExecuteCommandAsyncCallback {
        void run(String resultString);
    }

    /**
     * Background task for executeCommands
     */
    private class ExecuteCommandAsync extends AsyncTask<String, Integer, Boolean> {
        DCSSDKDefs.DCSSDK_COMMAND_OPCODE opcode;
        StringBuilder outXML;
        ExecuteCommandAsyncCallback backgroundCb, foregroundCb;

        ExecuteCommandAsync(DCSSDKDefs.DCSSDK_COMMAND_OPCODE opcode, StringBuilder outXML) {
            this(opcode, outXML, null, null);
        }

        ExecuteCommandAsync(DCSSDKDefs.DCSSDK_COMMAND_OPCODE opcode, StringBuilder outXML, ExecuteCommandAsyncCallback backgroundCb, ExecuteCommandAsyncCallback foregroundCb) {
            this.opcode = opcode;
            this.outXML = outXML;
            this.backgroundCb = backgroundCb;
            this.foregroundCb = foregroundCb;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            boolean result = executeCommand(sdkHandler, scannerId, opcode, strings[0], outXML);

            if (result && this.backgroundCb != null) {
                this.backgroundCb.run(outXML.toString());
            }
            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result && this.foregroundCb != null) {
                this.foregroundCb.run(outXML.toString());
            }
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Driver callbacks
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * "Barcode Event" notification informs about reception of a particular barcode of a particular type from a particular active scanner.
     */
    @Override
    public void dcssdkEventBarcode(byte[] barcodeData, int barcodeType, int fromScannerID) {
        Log.i(LOG_TAG, "dcssdkEventBarcode type " + barcodeType + " - data length is " + barcodeData.length);

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
     * "Firmware Update Event" notification informs about status in firmware update parse
     */
    @Override
    public void dcssdkEventFirmwareUpdate(FirmwareUpdateEvent firmwareUpdateEvent) {
        Log.i(LOG_TAG, "dcssdkEventFirmwareUpdate");
    }

    @Override
    public void dcssdkEventAuxScannerAppeared(DCSScannerInfo dcsScannerInfo, DCSScannerInfo dcsScannerInfo1) {
        Log.i(LOG_TAG, "dcssdkEventAuxScannerAppeared");
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

    @Override
    public void dcssdkEventBinaryData(byte[] bytes, int i) {
        Log.i(LOG_TAG, "dcssdkEventBinaryData");
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // ILLUMINATION
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private boolean illuminated = true;

    @Override
    public void enableIllumination() {
        String inXML = "<inArgs><scannerID>" + scannerId + "</scannerID></inArgs>";
        new ExecuteCommandAsync(DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_DEVICE_AIM_ON, null).execute(inXML);
        illuminated = true;
    }

    @Override
    public void disableIllumination() {
        String inXML = "<inArgs><scannerID>" + scannerId + "</scannerID></inArgs>";
        new ExecuteCommandAsync(DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_DEVICE_AIM_OFF, null).execute(inXML);
        illuminated = false;

    }

    @Override
    public void toggleIllumination() {
        beepScanSuccessful();
        if (illuminated) {
            disableIllumination();
        } else {
            enableIllumination();
        }
    }

    @Override
    public boolean supportsIllumination() {
        return true;
    }

    @Override
    public boolean isIlluminationOn() {
        return false;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // LED
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void ledColorOn(Color color) {
        int attrCode;
        if (color == Color.GREEN) {
            attrCode = 43;
        } else if (color == Color.RED) {
            attrCode = 47;
        } else {
            attrCode = 45;
        }

        String inXML = "<inArgs><scannerID>" + scannerId + "</scannerID><cmdArgs><arg-int>" + attrCode + "</arg-int></cmdArgs></inArgs>";
        new ExecuteCommandAsync(DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_SET_ACTION, null).execute(inXML);
    }

    @Override
    public void ledColorOff(Color color) {
        int attrCode;
        if (color == Color.GREEN) {
            attrCode = 42;
        } else if (color == Color.RED) {
            attrCode = 48;
        } else {
            attrCode = 46;
        }

        String inXML = "<inArgs><scannerID>" + scannerId + "</scannerID><cmdArgs><arg-int>" + attrCode + "</arg-int></cmdArgs></inArgs>";
        new ExecuteCommandAsync(DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_SET_ACTION, null).execute(inXML);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // PAUSE/RESUME
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void pause() {
        String inXML = "<inArgs><scannerID>" + scannerId + "</scannerID></inArgs>";
        new ExecuteCommandAsync(DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_DEVICE_SCAN_DISABLE, null).execute(inXML);
    }

    @Override
    public void resume() {
        String inXML = "<inArgs><scannerID>" + scannerId + "</scannerID></inArgs>";
        new ExecuteCommandAsync(DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_DEVICE_SCAN_ENABLE, null).execute(inXML);

        pullTrigger();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // BEEP
    ////////////////////////////////////////////////////////////////////////////////////////////////

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


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // MISC
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public String getProviderKey() {
        return BtZebraProvider.PROVIDER_NAME;
    }

    private void pullTrigger() {
        String inXML = "<inArgs><scannerID>" + scannerId + "</scannerID></inArgs>";
        new ExecuteCommandAsync(DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_DEVICE_PULL_TRIGGER, null).execute(inXML);
    }

    private void debugDumpParameters() {
        String inXML = "<inArgs><scannerID>" + scannerId + "</scannerID></inArgs>";
        StringBuilder sb = new StringBuilder();
        ExecuteCommandAsync ea = new ExecuteCommandAsync(DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_RSM_ATTR_GETALL, sb, new ExecuteCommandAsyncCallback() {
            @Override
            public void run(String resultString) {
                List<String> paramNames = new ArrayList<>();

                try {
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                    parser.setInput(new StringReader(resultString));

                    String latestOpenedTag = "";

                    int eventType = parser.getEventType();
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        if (eventType == XmlPullParser.START_DOCUMENT) {
                            Log.d(LOG_TAG, "Start document");
                        } else if (eventType == XmlPullParser.START_TAG) {
                            //Log.d(LOG_TAG, "Start tag " + parser.getName());
                            latestOpenedTag = parser.getName();
                        } else if (eventType == XmlPullParser.END_TAG) {
                            //Log.d(LOG_TAG, "End tag " + parser.getName());
                            latestOpenedTag = "";
                        } else if (eventType == XmlPullParser.TEXT) {
                            //Log.d(LOG_TAG, "Text " + parser.getText());
                            if (latestOpenedTag.equals("attribute")) {
                                paramNames.add(parser.getText());
                            }
                        }
                        eventType = parser.next();
                    }
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                for (String prm : paramNames) {
                    Log.d(LOG_TAG, "Param ID found: " + prm);
                }

                // Ask param values per small blocks, else the driver explodes.
                int i = 0;
                while (i < paramNames.size()) {
                    String in_xml = "<inArgs><scannerID>" + scannerId + "</scannerID><cmdArgs><arg-xml><attrib_list>";

                    for (int j = 0; j < 10 && i < paramNames.size(); j++) {
                        String attributeName = paramNames.get(i++);
                        if (attributeName.equals("255")) {
                            continue;
                        }
                        in_xml += attributeName + ",";
                    }
                    in_xml = in_xml.substring(0, in_xml.length() - 2);
                    in_xml += "</attrib_list></arg-xml></cmdArgs></inArgs>";

                    new ExecuteCommandAsync(DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_RSM_ATTR_GET, null).execute(in_xml);
                }
            }
        }, null);
        ea.execute(inXML);
    }
}
