package com.enioka.scanner.sdk.zebra;

import static com.enioka.scanner.helpers.Common.findMacAddress;
import static com.enioka.scanner.helpers.Common.buildBarcode;

import android.content.Context;
import androidx.annotation.Nullable;

import android.graphics.Bitmap;
import android.util.Log;
import android.util.Xml;

import com.enioka.scanner.api.ScannerLedColor;
import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.api.callbacks.ScannerStatusCallback;
import com.enioka.scanner.api.proxies.ScannerCommandCallbackProxy;
import com.enioka.scanner.api.proxies.ScannerDataCallbackProxy;
import com.enioka.scanner.api.proxies.ScannerInitCallbackProxy;
import com.enioka.scanner.api.proxies.ScannerStatusCallbackProxy;
import com.enioka.scanner.data.Barcode;
import com.enioka.scanner.data.BarcodeType;
import com.google.zxing.WriterException;
import com.zebra.scannercontrol.DCSSDKDefs;
import com.zebra.scannercontrol.SDKHandler;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Scanner provider for external BT Zebra (Symbol, Motorola...) devices.
 */
class BtZebraScanner extends BtZebraPairing implements Scanner, Scanner.WithBeepSupport, Scanner.WithIlluminationSupport, Scanner.WithLedSupport {
    private static final String LOG_TAG = "BtZebraProvider";

    private static final int BEEP_HIGH_SHORT_1 = 0;
    private static final int BEEP_LOW_LONG_2 = 16;
    private static final int BEEP_HIGH_LOW_HIGH = 24;

    private SDKHandler sdkHandler;
    private Integer scannerId;

    private ScannerDataCallbackProxy dataCb = null;
    private ScannerStatusCallbackProxy statusCb = null;


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // INIT
    ////////////////////////////////////////////////////////////////////////////////////////////////

    BtZebraScanner(SDKHandler h, int scannerId) {
        this.sdkHandler = h;
        this.scannerId = scannerId;
    }

    @Override
    public void initialize(final Context applicationContext, final ScannerInitCallbackProxy initCallback, final ScannerDataCallbackProxy dataCallback, final ScannerStatusCallbackProxy statusCallback, final Mode mode, final Set<BarcodeType> symbologeSelection) {
        this.dataCb = dataCallback;
        this.statusCb = statusCallback;

        sdkHandler.dcssdkEnableAutomaticSessionReestablishment(true, scannerId);

        String inXML = "<inArgs><scannerID>" + scannerId + "</scannerID></inArgs>";

        // Try to enable the scanner
        new BtZebraAsyncTask(sdkHandler, scannerId, DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_DEVICE_SCAN_ENABLE, new BtZebraAsyncTask.BtZebraAsyncTaskCallback() {
            @Override
            public void onSuccess(String resultString) {
                initCallback.onConnectionSuccessful(BtZebraScanner.this);
                Log.d(LOG_TAG, "Finished init of scanner Zebra BT of ID " + scannerId);
            }

            @Override
            public void onFailure() {
                initCallback.onConnectionFailure(BtZebraScanner.this);
                Log.e(LOG_TAG, "FAILED init of scanner Zebra BT of ID " + scannerId);
            }
        }).execute(inXML);
    }

    @Override
    public void setDataCallBack(ScannerDataCallbackProxy cb) {
        this.dataCb = cb;
    }

    @Override
    public void disconnect(@Nullable ScannerCommandCallbackProxy cb) {
        if (scannerId != null) {
            Log.i(LOG_TAG, "disconnect");
            sdkHandler.dcssdkTerminateCommunicationSession(scannerId);
            //sdkHandler.dcssdkUnsubsribeForEvents(notificationsMask);
            //sdkHandler.dcssdkClose();
            if (cb != null) {
                cb.onSuccess();
            }
        } else if (cb != null) {
            cb.onFailure();
        }
    }

    void reconnected() {
        if (statusCb != null) {
            statusCb.onStatusChanged(this, ScannerStatusCallback.Status.CONNECTED);
        }
    }

    void disconnected() {
        if (statusCb != null) {
            statusCb.onStatusChanged(this, ScannerStatusCallback.Status.DISCONNECTED);
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // DATA
    ////////////////////////////////////////////////////////////////////////////////////////////////

    void onData(byte[] barcodeData, int barcodeType) {
        Log.i(LOG_TAG, "dcssdkEventBarcode type " + barcodeType + " - data length is " + barcodeData.length);

        if (dataCb != null) {
            final List<Barcode> res = new ArrayList<>(1);
            BarcodeType type = BtZebraDataTranslator.sdk2Api(barcodeType);
            if (type == null) {
                type = BarcodeType.UNKNOWN;
            }
            res.add(new Barcode(new String(barcodeData).trim(), type));

            // Use a handler from the main message loop to run on the UI thread, as dcssdkEventBarcode is called by another thread.
            dataCb.onData(BtZebraScanner.this, res);
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // ILLUMINATION
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private boolean illuminated = true;

    @Override
    public void enableIllumination(@Nullable ScannerCommandCallbackProxy cb) {
        String inXML = "<inArgs><scannerID>" + scannerId + "</scannerID></inArgs>";
        BtZebraAsyncTask.fireAndForgetCommand(sdkHandler, scannerId, DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_DEVICE_AIM_ON, inXML);
        illuminated = true;
        if (cb != null) {
            cb.onSuccess();
        }
    }

    @Override
    public void disableIllumination(@Nullable ScannerCommandCallbackProxy cb) {
        String inXML = "<inArgs><scannerID>" + scannerId + "</scannerID></inArgs>";
        BtZebraAsyncTask.fireAndForgetCommand(sdkHandler, scannerId, DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_DEVICE_AIM_OFF, inXML);
        illuminated = false;
        if (cb != null) {
            cb.onSuccess();
        }
    }

    @Override
    public void toggleIllumination(@Nullable ScannerCommandCallbackProxy cb) {
        beepScanSuccessful(cb);
        if (illuminated) {
            disableIllumination(cb);
        } else {
            enableIllumination(cb);
        }
    }

    @Override
    public boolean isIlluminationOn() {
        return illuminated;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // LED
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void ledColorOn(ScannerLedColor color, @Nullable ScannerCommandCallbackProxy cb) {
        int attrCode;
        if (color == ScannerLedColor.GREEN) {
            attrCode = 43;
        } else if (color == ScannerLedColor.RED) {
            attrCode = 47;
        } else {
            attrCode = 45;
        }

        String inXML = "<inArgs><scannerID>" + scannerId + "</scannerID><cmdArgs><arg-int>" + attrCode + "</arg-int></cmdArgs></inArgs>";
        BtZebraAsyncTask.fireAndForgetCommand(sdkHandler, scannerId, DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_SET_ACTION, inXML);

        if (cb != null) {
            cb.onSuccess();
        }
    }

    @Override
    public void ledColorOff(ScannerLedColor color, @Nullable ScannerCommandCallbackProxy cb) {
        int attrCode;
        if (color == ScannerLedColor.GREEN) {
            attrCode = 42;
        } else if (color == ScannerLedColor.RED) {
            attrCode = 48;
        } else {
            attrCode = 46;
        }

        String inXML = "<inArgs><scannerID>" + scannerId + "</scannerID><cmdArgs><arg-int>" + attrCode + "</arg-int></cmdArgs></inArgs>";
        BtZebraAsyncTask.fireAndForgetCommand(sdkHandler, scannerId, DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_SET_ACTION, inXML);

        if (cb != null) {
            cb.onSuccess();
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // PAUSE/RESUME
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void pause(@Nullable ScannerCommandCallbackProxy cb) {
        String inXML = "<inArgs><scannerID>" + scannerId + "</scannerID></inArgs>";
        BtZebraAsyncTask.fireAndForgetCommand(sdkHandler, scannerId, DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_DEVICE_SCAN_DISABLE, inXML);
        if (cb != null) {
            cb.onSuccess();
        }
    }

    @Override
    public void resume(@Nullable ScannerCommandCallbackProxy cb) {
        String inXML = "<inArgs><scannerID>" + scannerId + "</scannerID></inArgs>";
        BtZebraAsyncTask.fireAndForgetCommand(sdkHandler, scannerId, DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_DEVICE_SCAN_ENABLE, inXML);
        if (cb != null) {
            cb.onSuccess();
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // BEEP
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void beepScanSuccessful(@Nullable ScannerCommandCallbackProxy cb) {
        String inXML = "<inArgs><scannerID>" + scannerId + "</scannerID><cmdArgs><arg-int>" +
                +BEEP_HIGH_SHORT_1 + "</arg-int></cmdArgs></inArgs>";

        BtZebraAsyncTask.fireAndForgetCommand(sdkHandler, scannerId, DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_SET_ACTION, inXML);
        if (cb != null) {
            cb.onSuccess();
        }
    }

    @Override
    public void beepScanFailure(@Nullable ScannerCommandCallbackProxy cb) {
        String inXML = "<inArgs><scannerID>" + scannerId + "</scannerID><cmdArgs><arg-int>" +
                +BEEP_LOW_LONG_2 + "</arg-int></cmdArgs></inArgs>";

        BtZebraAsyncTask.fireAndForgetCommand(sdkHandler, scannerId, DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_SET_ACTION, inXML);
        if (cb != null) {
            cb.onSuccess();
        }
    }

    @Override
    public void beepPairingCompleted(@Nullable ScannerCommandCallbackProxy cb) {
        String inXML = "<inArgs><scannerID>" + scannerId + "</scannerID><cmdArgs><arg-int>" +
                +BEEP_HIGH_LOW_HIGH + "</arg-int></cmdArgs></inArgs>";

        BtZebraAsyncTask.fireAndForgetCommand(sdkHandler, scannerId, DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_SET_ACTION, inXML);
        if (cb != null) {
            cb.onSuccess();
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // MISC
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public String getProviderKey() {
        return BtZebraProvider.PROVIDER_KEY;
    }

    // FIXME: software trigger scanner feature ?
    private void pullTrigger() {
        String inXML = "<inArgs><scannerID>" + scannerId + "</scannerID></inArgs>";
        BtZebraAsyncTask.fireAndForgetCommand(sdkHandler, scannerId, DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_DEVICE_PULL_TRIGGER, inXML);
    }

    private void debugDumpParameters() {
        String inXML = "<inArgs><scannerID>" + scannerId + "</scannerID></inArgs>";
        BtZebraAsyncTask ea = new BtZebraAsyncTask(sdkHandler, scannerId, DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_RSM_ATTR_GETALL, new BtZebraAsyncTask.BtZebraAsyncTaskCallback() {
            @Override
            public void onSuccess(String resultString) {
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
                } catch (XmlPullParserException | IOException e) {
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

                    BtZebraAsyncTask.fireAndForgetCommand(sdkHandler, scannerId, DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_RSM_ATTR_GET, in_xml);
                }
            }

            @Override
            public void onFailure() {
                // Nothing to do, debug method
            }
        });
        ea.execute(inXML);
    }
}
