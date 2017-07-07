package com.enioka.scanner.sdk.zebra;

import android.os.AsyncTask;
import android.util.Log;

import com.enioka.scanner.api.Scanner;
import com.zebra.scannercontrol.DCSSDKDefs;
import com.zebra.scannercontrol.RMDAttributes;
import com.zebra.scannercontrol.SDKHandler;

/**
 */
class ConnectScannerAsync extends AsyncTask<Void, Void, Boolean> {
    private static final String LOG_TAG = "ConnectScannerAsync";

    private Scanner.ScannerInitCallback callback;
    private SDKHandler sdkHandler;
    private int scannerId;

    public ConnectScannerAsync(Scanner.ScannerInitCallback callback, SDKHandler sdkHandler, int scannerId) {
        this.callback = callback;
        this.sdkHandler = sdkHandler;
        this.scannerId = scannerId;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.i(LOG_TAG, "Connecting To scanner...");
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        DCSSDKDefs.DCSSDK_RESULT result = DCSSDKDefs.DCSSDK_RESULT.DCSSDK_RESULT_FAILURE;
        if (sdkHandler != null) {
            result = sdkHandler.dcssdkEstablishCommunicationSession(scannerId);
        }
        if (result == DCSSDKDefs.DCSSDK_RESULT.DCSSDK_RESULT_SUCCESS) {
            String inXML;
            // Set initial settings
            // High volume and medium frequency
            inXML = "<inArgs><scannerID>" + scannerId + "</scannerID><cmdArgs><arg-xml><attrib_list>" +
                    "<attribute><id>" + RMDAttributes.RMD_ATTR_BEEPER_FREQUENCY + "</id><datatype>B</datatype>" +
                    "<value>" + RMDAttributes.RMD_ATTR_VALUE_BEEPER_FREQ_MEDIUM + "</value></attribute>"
                    + "</attrib_list></arg-xml></cmdArgs></inArgs>";

            ZebraScanner.executeCommand(sdkHandler, scannerId, DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_RSM_ATTR_SET, inXML, null);

            inXML = "<inArgs><scannerID>" + scannerId + "</scannerID><cmdArgs><arg-xml><attrib_list>" +
                    "<attribute><id>" + RMDAttributes.RMD_ATTR_BEEPER_VOLUME + "</id><datatype>B</datatype>" +
                    "<value>" + RMDAttributes.RMD_ATTR_VALUE_BEEPER_VOLUME_HIGH + "</value></attribute>" +
                    "</attrib_list></arg-xml></cmdArgs></inArgs>";

            ZebraScanner.executeCommand(sdkHandler, scannerId, DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_RSM_ATTR_SET, inXML, null);

            // Set authorized symbologies
            for (int unauthorizedSymbology : ZebraScanner.unauthorizedSymbologies) {
                inXML = "<inArgs><scannerID>" + scannerId + "</scannerID><cmdArgs><arg-xml><attrib_list>";
                inXML += "<attribute><id>" + unauthorizedSymbology + "</id><datatype>F</datatype><value>" + false + "</value></attribute>";
                inXML += "</attrib_list></arg-xml></cmdArgs></inArgs>";
                ZebraScanner.executeCommand(sdkHandler, scannerId, DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_RSM_ATTR_SET, inXML, null);
            }

            for (int authorizedSymbology : ZebraScanner.authorizedSymobolgies) {
                inXML = "<inArgs><scannerID>" + scannerId + "</scannerID><cmdArgs><arg-xml><attrib_list>";
                inXML += "<attribute><id>" + authorizedSymbology + "</id><datatype>F</datatype><value>" + true + "</value></attribute>";
                inXML += "</attrib_list></arg-xml></cmdArgs></inArgs>";
                ZebraScanner.executeCommand(sdkHandler, scannerId, DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_RSM_ATTR_SET, inXML, null);
            }

            return true;
        } else if (result == DCSSDKDefs.DCSSDK_RESULT.DCSSDK_RESULT_FAILURE) {
            return false;
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        if (result) {
            if (callback != null) {
                callback.onConnectionSuccessful();
            }
        } else {
            if (callback != null) {
                callback.onConnectionFailure();
            }
        }
    }
}

