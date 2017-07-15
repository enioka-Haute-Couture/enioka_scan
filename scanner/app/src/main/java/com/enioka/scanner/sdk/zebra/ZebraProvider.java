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
public class ZebraProvider implements ScannerProvider {
    private static final String LOG_TAG = "ZebraProvider";
    private static final String PROVIDER_NAME = "Zebra Bluetooth";

    @Override
    public void getScanner(Context ctx, ProviderCallback cb, ScannerSearchOptions options) {
        SDKHandler sdkHandler = new SDKHandler(ctx);
        sdkHandler.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_BT_NORMAL);

        // Connect to first available scanner
        List<DCSScannerInfo> mScannerInfoList = new ArrayList<>();
        Log.i(LOG_TAG, "dcssdkGetAvailableScannersList :" + sdkHandler.dcssdkGetAvailableScannersList(mScannerInfoList));

        if (mScannerInfoList.size() == 0) {
            cb.onProvided(PROVIDER_NAME, null, null);
            sdkHandler.dcssdkClose(null);
        } else {
            cb.onProvided(PROVIDER_NAME, "t", new ZebraScanner(sdkHandler));
        }
    }
}
