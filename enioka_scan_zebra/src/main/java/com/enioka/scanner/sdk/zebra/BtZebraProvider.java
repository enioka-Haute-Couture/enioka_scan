package com.enioka.scanner.sdk.zebra;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.enioka.scanner.api.ScannerProvider;
import com.enioka.scanner.api.ScannerProviderBinder;
import com.enioka.scanner.api.ScannerSearchOptions;
import com.zebra.scannercontrol.DCSSDKDefs;
import com.zebra.scannercontrol.DCSScannerInfo;
import com.zebra.scannercontrol.SDKHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Provider for the BT Zebra SDK.
 */
public class BtZebraProvider extends Service implements ScannerProvider {
    private static final String LOG_TAG = "BtZebraProvider";
    static final String PROVIDER_NAME = "Zebra Bluetooth";

    private final IBinder binder = new ScannerProviderBinder(this);

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void getScanner(Context ctx, ProviderCallback cb, ScannerSearchOptions options) {
        Log.i(LOG_TAG, "Starting scanner search");

        if (!options.useBlueTooth) {
            cb.onProviderUnavailable(PROVIDER_NAME);
            return;
        }

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
            cb.onScannerCreated(PROVIDER_NAME, "" + s.getScannerID(), new BtZebraScanner(sdkHandler));
            scannerFound = true;
        }

        if (!scannerFound) {
            Log.i(LOG_TAG, "Not Zebra BT devices connected to this device");
            cb.onProviderUnavailable(PROVIDER_NAME); // Costly search. We do not want it to do it on each scanner search.
            sdkHandler.dcssdkClose();
        }
    }

    @Override
    public String getKey() {
        return PROVIDER_NAME;
    }


}
