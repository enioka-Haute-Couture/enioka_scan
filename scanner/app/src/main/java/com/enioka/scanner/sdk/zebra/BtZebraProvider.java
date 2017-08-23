package com.enioka.scanner.sdk.zebra;

import android.content.Context;
import android.util.Log;

import com.enioka.scanner.api.ScannerProvider;
import com.enioka.scanner.api.ScannerSearchOptions;
import com.zebra.scannercontrol.DCSSDKDefs;
import com.zebra.scannercontrol.DCSScannerInfo;
import com.zebra.scannercontrol.SDKHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Provider for the BT Zebra SDK.
 */
public class BtZebraProvider implements ScannerProvider {
    private static final String LOG_TAG = "BtZebraProvider";
    private static final String PROVIDER_NAME = "Zebra Bluetooth";

    @Override
    public void getScanner(Context ctx, ProviderCallback cb, ScannerSearchOptions options) {
        Log.i(LOG_TAG, "Starting scanner search");
        SDKHandler sdkHandler = new SDKHandler(ctx);
        sdkHandler.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_BT_NORMAL);

        // Connect to available scanner
        List<DCSScannerInfo> mScannerInfoList = new ArrayList<>();
        Log.i(LOG_TAG, "dcssdkGetAvailableScannersList :" + sdkHandler.dcssdkGetAvailableScannersList(mScannerInfoList) + " " + mScannerInfoList.size());

        boolean scannerFound = false;
        for (DCSScannerInfo s : mScannerInfoList) {
            /*if (s.getScannerModel() == null) {
                // The stupid API actually lists all BT devices. Only Zebra devices should have a model.
                continue;
            }*/
            cb.onProvided(PROVIDER_NAME, "" + s.getScannerID(), new BtZebraScanner(sdkHandler));
            scannerFound = true;
        }

        if (!scannerFound) {
            Log.i(LOG_TAG, "Not Zebra BT devices connected to this device");
            cb.onProvided(PROVIDER_NAME, null, null);
            sdkHandler.dcssdkClose(null);
        }
    }
}
