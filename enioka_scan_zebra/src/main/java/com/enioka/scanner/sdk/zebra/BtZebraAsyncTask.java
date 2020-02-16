package com.enioka.scanner.sdk.zebra;

import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;

import com.zebra.scannercontrol.DCSSDKDefs;
import com.zebra.scannercontrol.SDKHandler;

class BtZebraAsyncTask extends AsyncTask<String, Integer, BtZebraAsyncTask.BtZebraAsyncTaskResult> {
    private static final String LOG_TAG = "BtZebraProvider";

    interface BtZebraAsyncTaskCallback {
        void onSuccess(String resultString);

        void onFailure();
    }

    class BtZebraAsyncTaskResult {
        boolean success = false;
        String resultText;
    }

    private final DCSSDKDefs.DCSSDK_COMMAND_OPCODE opcode;
    private final BtZebraAsyncTaskCallback callingThreadCb;
    private final int scannerId;
    private final SDKHandler sdkHandler;

    private BtZebraAsyncTaskResult result = new BtZebraAsyncTaskResult();

    BtZebraAsyncTask(SDKHandler sdkHandler, int scannerId, DCSSDKDefs.DCSSDK_COMMAND_OPCODE opcode) {
        this(sdkHandler, scannerId, opcode, null);
    }

    BtZebraAsyncTask(SDKHandler sdkHandler, int scannerId, DCSSDKDefs.DCSSDK_COMMAND_OPCODE opcode, BtZebraAsyncTaskCallback callingThreadCb) {
        this.opcode = opcode;
        this.callingThreadCb = callingThreadCb;
        this.scannerId = scannerId;
        this.sdkHandler = sdkHandler;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected BtZebraAsyncTaskResult doInBackground(String... strings) {
        StringBuilder outXML = new StringBuilder();
        result.success = executeCommand(sdkHandler, scannerId, opcode, strings[0], outXML);

        if (result.success) {
            result.resultText = outXML.toString();
        }
        return result;
    }

    @Override
    protected void onPostExecute(BtZebraAsyncTaskResult result) {
        if (result != null && result.success && this.callingThreadCb != null) {
            this.callingThreadCb.onSuccess(result.resultText);
        }
        if (result != null && !result.success && this.callingThreadCb != null) {
            this.callingThreadCb.onFailure();
        }
    }

    /**
     * Send a command "inXml" to the scanner. No result expected. Static - can be called synchronously, outside a BtZebraAsyncTask.
     */
    static boolean executeCommand(SDKHandler sdkHandler, int scannerId, DCSSDKDefs.DCSSDK_COMMAND_OPCODE opCode, String inXML) {
        return executeCommand(sdkHandler, scannerId, opCode, inXML, null);
    }

    /**
     * Send a command "inXml" to the scanner, output in "outXml".
     */
    private static boolean executeCommand(SDKHandler sdkHandler, int scannerId, DCSSDKDefs.DCSSDK_COMMAND_OPCODE opCode, String inXML, StringBuilder outXML) {
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

    static void fireAndForgetCommand(SDKHandler sdkHandler, int scannerId, DCSSDKDefs.DCSSDK_COMMAND_OPCODE opcode, String inXML) {
        new BtZebraAsyncTask(sdkHandler, scannerId, opcode).execute(inXML);
    }
}
