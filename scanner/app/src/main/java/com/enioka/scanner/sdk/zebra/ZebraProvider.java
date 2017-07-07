package com.enioka.scanner.sdk.zebra;

import android.content.Context;
import android.util.Log;

import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.api.ScannerProvider;
import com.zebra.scannercontrol.DCSSDKDefs;
import com.zebra.scannercontrol.DCSScannerInfo;
import com.zebra.scannercontrol.SDKHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Provider for the BT Zebra SDK.
 */
public class ZebraProvider implements ScannerProvider {
    private static final String LOG_TAG = "ZebraProvider";

    @Override
    public Scanner getScanner(Context ctx) {
        SDKHandler sdkHandler = new SDKHandler(ctx);
        sdkHandler.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_BT_NORMAL);

        // Connect to first available scanner
        List<DCSScannerInfo> mScannerInfoList = new ArrayList<>();
        Log.i(LOG_TAG, "dcssdkGetAvailableScannersList :" + sdkHandler.dcssdkGetAvailableScannersList(mScannerInfoList));

        if (mScannerInfoList.size() == 0) {
            return null;
        } else {
            return new ZebraScanner(sdkHandler);
        }
    }
}
